package com.worth.ifs.project.finance.transactional;

import au.com.bytecode.opencsv.CSVWriter;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.LocalDateResource;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.finance.domain.*;
import com.worth.ifs.project.finance.repository.SpendProfileRepository;
import com.worth.ifs.project.repository.ProjectRepository;
import com.worth.ifs.project.resource.*;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.util.CollectionFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private static final String CSV_MONTH = "Month";
    private static final String CSV_TOTAL = "TOTAL";
    private static final String CSV_ELIGIBLE_COST_TOTAL = "Eligible Costs Total";
    private static final String CSV_FILE_NAME_FORMAT = "%s_Spend_Profile_%s.csv";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

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
    public ServiceResult<Void> approveOrRejectSpendProfile(Long projectId, ApprovalType approvalType) {
        updateApprovalOfSpendProfile(projectId, approvalType);
        return serviceSuccess();
    }

    @Override
    public ServiceResult<ApprovalType> getSpendProfileStatusByProjectId(Long projectId) {
        List<SpendProfile> spendProfiles = getSpendProfileByProjectId(projectId);

        if(spendProfiles.stream().anyMatch(spendProfile -> spendProfile.getApproval().equals(ApprovalType.REJECTED))) {
           return serviceSuccess(ApprovalType.REJECTED);
        } else if (spendProfiles.stream().allMatch(spendProfile -> spendProfile.getApproval().equals(ApprovalType.APPROVED))) {
           return serviceSuccess(ApprovalType.APPROVED);
        }

        return serviceSuccess(ApprovalType.UNSET);
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

    @Override
    public ServiceResult<SpendProfileCSVResource> getSpendProfileCSV(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
        SpendProfileTableResource spendProfileTableResource = getSpendProfileTable(projectOrganisationCompositeId).getSuccessObject();
        try {
            return serviceSuccess(generateSpendProfileCSVData(spendProfileTableResource, projectOrganisationCompositeId));
        } catch (IOException ioe) {
            return serviceFailure(SPEND_PROFILE_CSV_GENERATION_FAILURE);
        }
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
            return saveSpendProfileData(projectOrganisationCompositeId, table, complete);
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

    private void updateApprovalOfSpendProfile(Long projectId, ApprovalType approvalType) {
        List<SpendProfile> spendProfiles = spendProfileRepository.findByProjectId(projectId);
        spendProfiles.forEach(spendProfile -> spendProfile.setApproval(approvalType));

        spendProfileRepository.save(spendProfiles);
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

            SpendProfile spendProfile = new SpendProfile(organisation, project, costCategoryType, eligibleCosts, spendProfileCosts, false, ApprovalType.UNSET);
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


    private List<SpendProfile> getSpendProfileByProjectId(Long projectId) {
        return spendProfileRepository.findByProjectId(projectId);
    }

    private SpendProfileCSVResource generateSpendProfileCSVData(SpendProfileTableResource spendProfileTableResource,
                                                                ProjectOrganisationCompositeId projectOrganisationCompositeId) throws IOException {
        Map<String, BigDecimal> categoryToActualTotal = buildSpendProfileActualTotalsForAllCategories(spendProfileTableResource);
        List<BigDecimal> totalForEachMonth = buildTotalForEachMonth(spendProfileTableResource);
        BigDecimal totalOfAllActualTotals = buildTotalOfTotals(categoryToActualTotal);
        BigDecimal totalOfAllEligibleTotals = buildTotalOfTotals(spendProfileTableResource.getEligibleCostPerCategoryMap());
        Organisation organisation = organisationRepository.findOne(projectOrganisationCompositeId.getOrganisationId());

        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);
        ArrayList<String[]> rows = new ArrayList<>();
        ArrayList<String> monthsRow = new ArrayList<>();
        monthsRow.add(CSV_MONTH);
        spendProfileTableResource.getMonths().forEach(
                value -> monthsRow.add(value.getLocalDate().toString()));
        monthsRow.add(CSV_TOTAL);
        monthsRow.add(CSV_ELIGIBLE_COST_TOTAL);
        rows.add(monthsRow.stream().toArray(String[]::new));

        ArrayList<String> byCategory = new ArrayList<>();
        spendProfileTableResource.getMonthlyCostsPerCategoryMap().forEach((category, values)-> {
            byCategory.add(category);
            values.forEach(val -> {
                byCategory.add(val.toString());
            });
            byCategory.add(categoryToActualTotal.get(category).toString());
            byCategory.add(spendProfileTableResource.getEligibleCostPerCategoryMap().get(category).toString());
            rows.add(byCategory.stream().toArray(String[]::new));
            byCategory.clear();
        });

        ArrayList<String> totals = new ArrayList<>();
        totals.add(CSV_TOTAL);
        totalForEachMonth.forEach(value -> totals.add(value.toString()));
        totals.add(totalOfAllActualTotals.toString());
        totals.add(totalOfAllEligibleTotals.toString());
        rows.add(totals.stream().toArray(String[]::new));
        csvWriter.writeAll(rows);
        csvWriter.close();

        SpendProfileCSVResource spendProfileCSVResource = new SpendProfileCSVResource();
        spendProfileCSVResource.setCsvData(stringWriter.toString());
        spendProfileCSVResource.setFileName(generateSpendProfileFileName(organisation.getName()));

        return spendProfileCSVResource;
    }

    private String generateSpendProfileFileName(String organisationName) {
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return String.format(CSV_FILE_NAME_FORMAT, organisationName, dateFormat.format(date));
    }

    private Map<String, BigDecimal> buildSpendProfileActualTotalsForAllCategories(SpendProfileTableResource table) {
        return CollectionFunctions.simpleLinkedMapValue(table.getMonthlyCostsPerCategoryMap(),
                (List<BigDecimal> monthlyCosts) -> monthlyCosts.stream().reduce(BigDecimal.ZERO, (d1, d2) -> d1.add(d2)));
    }

    private List<BigDecimal> buildTotalForEachMonth(SpendProfileTableResource table) {
        Map<String, List<BigDecimal>> monthlyCostsPerCategoryMap = table.getMonthlyCostsPerCategoryMap();
        List<BigDecimal> totalForEachMonth = Stream.generate(() -> BigDecimal.ZERO).limit(table.getMonths().size()).collect(Collectors.toList());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        totalForEachMonth.forEach(totalForThisMonth -> {
            int index = atomicInteger.getAndIncrement();
            monthlyCostsPerCategoryMap.forEach((category, value) -> {
                BigDecimal costForThisMonthForCategory = value.get(index);
                if (totalForEachMonth.get(index) != null) {
                    totalForEachMonth.set(index, totalForEachMonth.get(index).add(costForThisMonthForCategory));
                } else {
                    totalForEachMonth.set(index, costForThisMonthForCategory);
                }
            });
        });
        return totalForEachMonth;
    }

    private BigDecimal buildTotalOfTotals(Map<String, BigDecimal> input) {
        return input.values().stream().reduce(BigDecimal.ZERO, (d1, d2) -> d1.add(d2));
    }

    private List<Cost> findMultipleMatchingCostsByCategory(CostGroup spendProfileFigures, CostCategory category) {
        return simpleFilter(spendProfileFigures.getCosts(), f -> f.getCostCategory().equals(category));
    }

    private Cost findSingleMatchingCostByCategory(CostGroup eligibleCosts, CostCategory category) {
        return simpleFindFirst(eligibleCosts.getCosts(), f -> f.getCostCategory().equals(category)).get();
    }
}
