package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.LocalDateResource;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.finance.domain.*;
import com.worth.ifs.project.finance.repository.SpendProfileRepository;
import com.worth.ifs.project.repository.ProjectRepository;
import com.worth.ifs.project.resource.ProjectOrganisationCompositeId;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.resource.SpendProfileResource;
import com.worth.ifs.project.resource.SpendProfileTableResource;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.commons.error.Error.fieldError;
import static com.worth.ifs.commons.service.ServiceResult.*;
import static com.worth.ifs.project.finance.domain.TimeUnit.MONTH;
import static com.worth.ifs.util.CollectionFunctions.*;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Service dealing with Project finance operations
 */
@Service
public class ProjectFinanceServiceImpl extends BaseTransactionalService implements ProjectFinanceService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SpendProfileRepository spendProfileRepository;

    @Autowired
    private CostCategoryTypeStrategy costCategoryTypeStrategy;

    @Autowired
    private SpendProfileCostCategorySummaryStrategy spendProfileCostCategorySummaryStrategy;

    @Override
    public ServiceResult<Void> generateSpendProfile(Long projectId) {

        return getProject(projectId).andOnSuccess(project ->
               projectService.getProjectUsers(projectId).andOnSuccess(projectUsers -> {
                   List<Long> organisationIds = removeDuplicates(simpleMap(projectUsers, ProjectUserResource::getOrganisation));
                   return generateSpendProfileForPartnerOrganisations(project, organisationIds);
               })
        );
    }

    @Override
    public ServiceResult<SpendProfileTableResource> getSpendProfileTable(ProjectOrganisationCompositeId projectOrganisationCompositeId) {

        return find(spendProfile(projectOrganisationCompositeId.getProjectId(), projectOrganisationCompositeId.getOrganisationId()),
                project(projectOrganisationCompositeId.getProjectId())).andOnSuccess((spendProfile, project) -> {

            List<CostCategory> costCategories = spendProfile.getCostCategoryType().getCostCategories();

            CostGroup eligibleCosts = spendProfile.getEligibleCosts();
            CostGroup spendProfileFigures = spendProfile.getSpendProfileFigures();

            Map<String, BigDecimal> eligibleCostsPerCategory =
                    simpleToLinkedMap(
                            costCategories,
                            CostCategory::getName,
                            category -> findSingleMatchingCostByCategory(eligibleCosts, category).getValue());

            Map<String, List<Cost>> spendProfileCostsPerCategory =
                    simpleToLinkedMap(
                            costCategories,
                            CostCategory::getName,
                            category -> findMultipleMatchingCostsByCategory(spendProfileFigures, category));

            LocalDate startDate = spendProfile.getProject().getTargetStartDate();
            int durationInMonths = spendProfile.getProject().getDurationInMonths().intValue();

            List<LocalDate> months = IntStream.range(0, durationInMonths).mapToObj(startDate::plusMonths).collect(toList());
            List<LocalDateResource> monthResources = simpleMap(months, LocalDateResource::new);

            Map<String, List<BigDecimal>> spendFiguresPerCategoryOrderedByMonth =
                    simpleLinkedMapValue(spendProfileCostsPerCategory, costs -> orderCostsByMonths(costs, months, project.getTargetStartDate()));

            SpendProfileTableResource table = new SpendProfileTableResource();
            table.setMonths(monthResources);
            table.setEligibleCostPerCategoryMap(eligibleCostsPerCategory);
            table.setMonthlyCostsPerCategoryMap(spendFiguresPerCategoryOrderedByMonth);
            table.setMarkedAsComplete(spendProfile.isMarkedAsComplete());
            checkTotalForMonthsAndAddToTable(table);
            return serviceSuccess(table);
        });
    }

    private List<Cost> findMultipleMatchingCostsByCategory(CostGroup spendProfileFigures, CostCategory category) {
        return simpleFilter(spendProfileFigures.getCosts(), f -> f.getCostCategory().equals(category));
    }

    private Cost findSingleMatchingCostByCategory(CostGroup eligibleCosts, CostCategory category) {
        return simpleFindFirst(eligibleCosts.getCosts(), f -> f.getCostCategory().equals(category)).get();
    }

    @Override
    public ServiceResult<SpendProfileResource> getSpendProfile(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
        return getSpendProfileEntity(projectOrganisationCompositeId.getProjectId(), projectOrganisationCompositeId.getOrganisationId())
                .andOnSuccessReturn(profile -> {
            SpendProfileResource resource = new SpendProfileResource();
            resource.setId(profile.getId());
            return resource;
        });
    }

    @Override
    public ServiceResult<Void> saveSpendProfile(ProjectOrganisationCompositeId projectOrganisationCompositeId, SpendProfileTableResource table) {
        return validateSpendProfileCosts(table)
                .andOnSuccess(() -> saveSpendProfileData(projectOrganisationCompositeId, table, false)); // We have to save the data even if the totals don't match
    }

    @Override
    public ServiceResult<Void> markSpendProfile(ProjectOrganisationCompositeId projectOrganisationCompositeId, Boolean complete) {
        SpendProfileTableResource table = getSpendProfileTable(projectOrganisationCompositeId).getSuccessObject();
        if(complete && table.getValidationMessages().hasErrors()){ // validate before marking as complete
            return serviceFailure(SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE);
        } else {
            return serviceSuccess();
        }
    }

    private ServiceResult<Void> validateSpendProfileCosts(SpendProfileTableResource table) {

        List<Error> incorrectCosts = checkCostsForAllCategories(table);

        if (incorrectCosts.isEmpty()) {
            return serviceSuccess();
        } else {
            return serviceFailure(incorrectCosts);
        }
    }

    private List<Error> checkCostsForAllCategories(SpendProfileTableResource table) {

        Map<String, List<BigDecimal>> monthlyCostsPerCategoryMap = table.getMonthlyCostsPerCategoryMap();

        List<Error> incorrectCosts = new ArrayList<>();

        for (Map.Entry<String, List<BigDecimal>> entry : monthlyCostsPerCategoryMap.entrySet()) {
            String category = entry.getKey();
            List<BigDecimal> monthlyCosts = entry.getValue();

            int index = 0;
            for (BigDecimal cost : monthlyCosts) {
                isCostValid(cost, category, index, incorrectCosts);
                index++;
            }

        }

        return incorrectCosts;
    }

    private void isCostValid(BigDecimal cost, String category, int index, List<Error> incorrectCosts) {

        checkFractionalCost(cost, category, index, incorrectCosts);

        checkCostLessThanZero(cost, category, index, incorrectCosts);

        checkCostGreaterThanOrEqualToMillion(cost, category, index, incorrectCosts);
    }

    private void checkFractionalCost(BigDecimal cost, String category, int index, List<Error> incorrectCosts) {

        if (cost.scale() > 0) {
            incorrectCosts.add(new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList(category, index + 1), HttpStatus.BAD_REQUEST));
        }
    }

    private void checkCostLessThanZero(BigDecimal cost, String category, int index, List<Error> incorrectCosts) {

        if (-1 == cost.compareTo(BigDecimal.ZERO)) { // Indicates that the cost is less than zero
            incorrectCosts.add(new Error(SPEND_PROFILE_COST_LESS_THAN_ZERO_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList(category, index + 1), HttpStatus.BAD_REQUEST));
        }
    }

    private void checkCostGreaterThanOrEqualToMillion(BigDecimal cost, String category, int index, List<Error> incorrectCosts) {

        if (-1 != cost.compareTo(new BigDecimal("1000000"))) { // Indicates that the cost million or more
            incorrectCosts.add(new Error(SPEND_PROFILE_COST_MORE_THAN_MILLION_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList(category, index + 1), HttpStatus.BAD_REQUEST));
        }
    }

    private ServiceResult<Void> saveSpendProfileData(ProjectOrganisationCompositeId projectOrganisationCompositeId, SpendProfileTableResource table, boolean markAsComplete) {

        SpendProfile spendProfile = spendProfileRepository.findOneByProjectIdAndOrganisationId(
                projectOrganisationCompositeId.getProjectId(), projectOrganisationCompositeId.getOrganisationId());

        spendProfile.setMarkedAsComplete(markAsComplete);

        updateSpendProfileCosts(spendProfile, table);

        spendProfileRepository.save(spendProfile);

        return serviceSuccess();
    }

    private void updateSpendProfileCosts(SpendProfile spendProfile, SpendProfileTableResource table) {

        Map<String, List<BigDecimal>> monthlyCostsPerCategoryMap = table.getMonthlyCostsPerCategoryMap();

        for (Map.Entry<String, List<BigDecimal>> entry : monthlyCostsPerCategoryMap.entrySet()) {

            String category = entry.getKey();
            List<BigDecimal> monthlyCosts = entry.getValue();

            updateSpendProfileCostsForCategory(category, monthlyCosts, spendProfile);

        }
    }

    private void updateSpendProfileCostsForCategory(String category, List<BigDecimal> monthlyCosts, SpendProfile spendProfile) {

        List<Cost> filteredAndSortedCostsToUpdate = spendProfile.getSpendProfileFigures().getCosts().stream()
                .filter(cost -> cost.getCostCategory().getName().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(cost -> cost.getCostTimePeriod().getOffsetAmount()))
                .collect(Collectors.toList());

        int index = 0;
        for (Cost costToUpdate : filteredAndSortedCostsToUpdate) {
            costToUpdate.setValue(monthlyCosts.get(index));
            index++;
        }
    }

    private void checkTotalForMonthsAndAddToTable(SpendProfileTableResource table) {

        Map<String, List<BigDecimal>> monthlyCostsPerCategoryMap = table.getMonthlyCostsPerCategoryMap();
        Map<String, BigDecimal> eligibleCostPerCategoryMap = table.getEligibleCostPerCategoryMap();

        List<Error> categoriesWithIncorrectTotal = new ArrayList<>();

        for (Map.Entry<String, List<BigDecimal>> entry : monthlyCostsPerCategoryMap.entrySet()) {
            String category = entry.getKey();
            List<BigDecimal> monthlyCosts = entry.getValue();

            BigDecimal actualTotalCost = monthlyCosts.stream().reduce(BigDecimal.ZERO, (d1, d2) -> d1.add(d2));
            BigDecimal expectedTotalCost = eligibleCostPerCategoryMap.get(category);

            if (actualTotalCost.compareTo(expectedTotalCost) == 1) {
                categoriesWithIncorrectTotal.add(fieldError(category, actualTotalCost, SPEND_PROFILE_TOTAL_FOR_ALL_MONTHS_DOES_NOT_MATCH_ELIGIBLE_TOTAL_FOR_SPECIFIED_CATEGORY.getErrorKey()));
            }
        }

        ValidationMessages validationMessages = new ValidationMessages(categoriesWithIncorrectTotal);
        validationMessages.setObjectName("SPEND_PROFILE");
        table.setValidationMessages(validationMessages);
    }

    private List<BigDecimal> orderCostsByMonths(List<Cost> costs, List<LocalDate> months, LocalDate startDate) {
        return simpleMap(months, month -> findCostForMonth(costs, month, startDate));
    }

    private BigDecimal findCostForMonth(List<Cost> costs, LocalDate month, LocalDate startDate) {
        Optional<Cost> matching = simpleFindFirst(costs, cost -> cost.getCostTimePeriod().getStartDate(startDate).equals(month));
        return matching.map(Cost::getValue).orElse(BigDecimal.ZERO);
    }

    private Supplier<ServiceResult<SpendProfile>> spendProfile(Long projectId, Long organisationId) {
        return () -> getSpendProfileEntity(projectId, organisationId);
    }

    private ServiceResult<SpendProfile> getSpendProfileEntity(Long projectId, Long organisationId) {
        return find(spendProfileRepository.findOneByProjectIdAndOrganisationId(projectId, organisationId), notFoundError(SpendProfile.class, projectId, organisationId));
    }

    private ServiceResult<Void> generateSpendProfileForPartnerOrganisations(Project project, List<Long> organisationIds) {

        List<ServiceResult<Void>> generationResults = simpleMap(organisationIds, organisationId -> spendProfileCostCategorySummaryStrategy.getCostCategorySummaries(project.getId(), organisationId).
                andOnSuccess(summaryPerCategory ->
                        generateSpendProfileForOrganisation(project.getId(), organisationId, summaryPerCategory)));

        return processAnyFailuresOrSucceed(generationResults);
    }

    private ServiceResult<Void> generateSpendProfileForOrganisation(
            Long projectId,
            Long organisationId,
            List<SpendProfileCostCategorySummary> summaryPerCategory) {

        return find(project(projectId), organisation(organisationId)).andOnSuccess(
                (project, organisation) -> generateSpendProfileForOrganisation(summaryPerCategory, project, organisation));
    }

    private ServiceResult<Void> generateSpendProfileForOrganisation(List<SpendProfileCostCategorySummary> summaryPerCategory, Project project, Organisation organisation) {

        return costCategoryTypeStrategy.getOrCreateCostCategoryTypeForSpendProfile(project.getId(), organisation.getId()).
                andOnSuccessReturnVoid(costCategoryType -> {

            List<Cost> eligibleCosts = generateEligibleCosts(summaryPerCategory, costCategoryType);
            List<Cost> spendProfileCosts = generateSpendProfileFigures(summaryPerCategory, project, costCategoryType);

            SpendProfile spendProfile = new SpendProfile(organisation, project, costCategoryType, eligibleCosts, spendProfileCosts, false);
            spendProfileRepository.save(spendProfile);
        });
    }

    private List<Cost> generateSpendProfileFigures(List<SpendProfileCostCategorySummary> summaryPerCategory, Project project, CostCategoryType costCategoryType) {

        List<List<Cost>> spendProfileCostsPerCategory = simpleMap(summaryPerCategory, summary -> {

            CostCategory matchingCategory = findMatchingCostCategory(costCategoryType, summary);

            return IntStream.range(0, project.getDurationInMonths().intValue()).mapToObj(i -> {

                BigDecimal costValueForThisMonth = i == 0 ? summary.getFirstMonthSpend() : summary.getOtherMonthsSpend();

                return new Cost(costValueForThisMonth).
                           withCategory(matchingCategory).
                           withTimePeriod(i, MONTH, 1, MONTH);

            }).collect(toList());
        });

        return flattenLists(spendProfileCostsPerCategory);
    }

    private List<Cost> generateEligibleCosts(List<SpendProfileCostCategorySummary> summaryPerCategory, CostCategoryType costCategoryType) {

        return simpleMap(summaryPerCategory, summary -> {

            CostCategory matchingCategory = findMatchingCostCategory(costCategoryType, summary);
            return new Cost(summary.getTotal().setScale(0, ROUND_HALF_UP)).withCategory(matchingCategory);
        });
    }

    private CostCategory findMatchingCostCategory(CostCategoryType costCategoryType, SpendProfileCostCategorySummary summary) {
        return simpleFindFirst(costCategoryType.getCostCategories(), cat -> cat.getName().equals(summary.getCategory().getName())).get();
    }

    private Supplier<ServiceResult<Project>> project(Long id) {
        return () -> getProject(id);
    }

    private ServiceResult<Project> getProject(Long id) {
        return find(projectRepository.findOne(id), notFoundError(Project.class, id));
    }

}
