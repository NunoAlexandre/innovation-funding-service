package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.BuilderAmendFunctions;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.builder.CostCategoryBuilder;
import com.worth.ifs.project.builder.CostCategoryGroupBuilder;
import com.worth.ifs.project.builder.CostCategoryTypeBuilder;
import com.worth.ifs.project.builder.ProjectBuilder;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.finance.domain.*;
import com.worth.ifs.project.finance.resource.CostCategoryResource;
import com.worth.ifs.project.finance.resource.CostCategoryTypeResource;
import com.worth.ifs.project.resource.ProjectOrganisationCompositeId;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.resource.SpendProfileTableResource;
import com.worth.ifs.user.domain.Organisation;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.finance.resource.cost.FinanceRowType.ACADEMIC;
import static com.worth.ifs.finance.resource.cost.FinanceRowType.LABOUR;
import static com.worth.ifs.finance.resource.cost.FinanceRowType.MATERIALS;
import static com.worth.ifs.project.builder.CostCategoryBuilder.newCostCategory;
import static com.worth.ifs.project.builder.CostCategoryGroupBuilder.newCostCategoryGroup;
import static com.worth.ifs.project.builder.CostCategoryResourceBuilder.newCostCategoryResource;
import static com.worth.ifs.project.builder.CostCategoryTypeBuilder.newCostCategoryType;
import static com.worth.ifs.project.builder.CostCategoryTypeResourceBuilder.newCostCategoryTypeResource;
import static com.worth.ifs.project.builder.ProjectBuilder.newProject;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.project.finance.domain.TimeUnit.MONTH;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;
import static com.worth.ifs.util.MapFunctions.asMap;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ProjectFinanceServiceImplTest extends BaseServiceUnitTest<ProjectFinanceServiceImpl> {

    @Mock
    private CostCategoryTypeStrategy costCategoryTypeStrategy;

    @Mock
    private SpendProfileCostCategorySummaryStrategy spendProfileCostCategorySummaryStrategy;

    @Test
    public void testGenerateSpendProfile() {

        Long projectId = 123L;

        Project project = newProject().withId(projectId).withDuration(3L).build();
        Organisation organisation1 = newOrganisation().build();
        Organisation organisation2 = newOrganisation().build();

        // First cost category type and everything that goes with it.
        CostCategory type1Cat1 = newCostCategory().withName(LABOUR.getName()).build();
        CostCategoryResource type1Cat1Resource = newCostCategoryResource().withName(type1Cat1.getName()).with(id(type1Cat1.getId())).build();


        CostCategory type1Cat2 = newCostCategory().withName(MATERIALS.getName()).build();
        CostCategoryResource type1Cat2Resource = newCostCategoryResource().withName(type1Cat2.getName()).with(id(type1Cat2.getId())).build();

        CostCategoryType costCategoryType1 =
                newCostCategoryType()
                        .withName("Type 1")
                        .withCostCategoryGroup(
                                newCostCategoryGroup()
                                        .withDescription("Group 1")
                                .withCostCategories(asList(type1Cat1, type1Cat2))
                                        .build())
                        .build();

        CostCategoryTypeResource costCategoryType1Resource = newCostCategoryTypeResource().with(id(costCategoryType1.getId())).build();

        // Second cost category type and everything that goes with it.
        CostCategory type2Cat1 = newCostCategory().withName(ACADEMIC.getName()).build();
        CostCategoryResource type2Cat1Resource = newCostCategoryResource().withName(type2Cat1.getName()).with(id(type2Cat1.getId())).build();

        CostCategoryType costCategoryType2 = newCostCategoryType()
                        .withName("Type 2")
                        .withCostCategoryGroup(
                                newCostCategoryGroup()
                                        .withDescription("Group 2")
                                        .withCostCategories(asList(type2Cat1))
                                        .build())
                        .build();

        CostCategoryTypeResource costCategoryType2Resource = newCostCategoryTypeResource().with(id(costCategoryType2.getId())).build();

        // set basic repository lookup expectations
        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisation1.getId())).thenReturn(organisation1);
        when(organisationRepositoryMock.findOne(organisation2.getId())).thenReturn(organisation2);
        when(costCategoryRepositoryMock.findOne(type1Cat1.getId())).thenReturn(type1Cat1);
        when(costCategoryRepositoryMock.findOne(type1Cat2.getId())).thenReturn(type1Cat2);
        when(costCategoryRepositoryMock.findOne(type2Cat1.getId())).thenReturn(type2Cat1);
        when(costCategoryTypeRepositoryMock.findOne(costCategoryType1.getId())).thenReturn(costCategoryType1);
        when(costCategoryTypeRepositoryMock.findOne(costCategoryType2.getId())).thenReturn(costCategoryType2);

        // setup expectations for getting project users to infer the partner organisations
        List<ProjectUserResource> projectUsers =
                newProjectUserResource().withOrganisation(organisation1.getId(), organisation2.getId()).build(2);
        when(projectServiceMock.getProjectUsers(projectId)).thenReturn(serviceSuccess(projectUsers));

        // setup expectations for finding finance figures per Cost Category from which to generate the spend profile
        when(spendProfileCostCategorySummaryStrategy.getCostCategorySummaries(project.getId(), organisation1.getId())).thenReturn(serviceSuccess(
                new SpendProfileCostCategorySummaries(
                        asList(
                                new SpendProfileCostCategorySummary(type1Cat1Resource, new BigDecimal("100.00"), project.getDurationInMonths()),
                                new SpendProfileCostCategorySummary(type1Cat2Resource, new BigDecimal("200.00"), project.getDurationInMonths())),
                        costCategoryType1Resource)));

        when(spendProfileCostCategorySummaryStrategy.getCostCategorySummaries(project.getId(), organisation2.getId())).thenReturn(serviceSuccess(
                new SpendProfileCostCategorySummaries(
                        singletonList(new SpendProfileCostCategorySummary(type2Cat1Resource, new BigDecimal("300.66"), project.getDurationInMonths())),
                        costCategoryType2Resource)));

        List<Cost> expectedOrganisation1EligibleCosts = asList(
                new Cost("100").withCategory(type1Cat1),
                new Cost("200").withCategory(type1Cat2));

        List<Cost> expectedOrganisation1SpendProfileFigures = asList(
                new Cost("34").withCategory(type1Cat1).withTimePeriod(0, MONTH, 1, MONTH),
                new Cost("33").withCategory(type1Cat1).withTimePeriod(1, MONTH, 1, MONTH),
                new Cost("33").withCategory(type1Cat1).withTimePeriod(2, MONTH, 1, MONTH),
                new Cost("66").withCategory(type1Cat2).withTimePeriod(0, MONTH, 1, MONTH),
                new Cost("67").withCategory(type1Cat2).withTimePeriod(1, MONTH, 1, MONTH),
                new Cost("67").withCategory(type1Cat2).withTimePeriod(2, MONTH, 1, MONTH));

        SpendProfile expectedOrganisation1Profile = new SpendProfile(organisation1, project, costCategoryType1,
                expectedOrganisation1EligibleCosts, expectedOrganisation1SpendProfileFigures, false);

        List<Cost> expectedOrganisation2EligibleCosts = singletonList(
                new Cost("301").withCategory(type2Cat1));

        List<Cost> expectedOrganisation2SpendProfileFigures = asList(
                new Cost("101").withCategory(type2Cat1).withTimePeriod(0, MONTH, 1, MONTH),
                new Cost("100").withCategory(type2Cat1).withTimePeriod(1, MONTH, 1, MONTH),
                new Cost("100").withCategory(type2Cat1).withTimePeriod(2, MONTH, 1, MONTH));

        SpendProfile expectedOrganisation2Profile = new SpendProfile(organisation2, project, costCategoryType2,
                expectedOrganisation2EligibleCosts, expectedOrganisation2SpendProfileFigures, false);

        when(spendProfileRepositoryMock.save(spendProfileExpectations(expectedOrganisation1Profile))).thenReturn(null);
        when(spendProfileRepositoryMock.save(spendProfileExpectations(expectedOrganisation2Profile))).thenReturn(null);

        ServiceResult<Void> generateResult = service.generateSpendProfile(projectId);
        assertTrue(generateResult.isSuccess());

        verify(spendProfileRepositoryMock).save(spendProfileExpectations(expectedOrganisation1Profile));
        verify(spendProfileRepositoryMock).save(spendProfileExpectations(expectedOrganisation2Profile));
        verifyNoMoreInteractions(spendProfileRepositoryMock);
    }

    @Test
    public void saveSpendProfileWhenCostsAreFractional() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        table.setMonthlyCostsPerCategoryMap(asMap(
                "Labour", asList(new BigDecimal("30.44"), new BigDecimal("30"), new BigDecimal("40")),
                "Materials", asList(new BigDecimal("70"), new BigDecimal("50.10"), new BigDecimal("60")),
                "Other costs", asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("10.31"))));

        ServiceResult<Void> result = service.saveSpendProfile(projectOrganisationCompositeId, table);

        assertTrue(result.isFailure());

        List<Error> errors = result.getFailure().getErrors();

        assertTrue(errors.size() == 3);

        // Assert that the error messages are for correct categories and correct month(s) based on the input
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Labour", 1), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Materials", 2), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Other costs", 3), HttpStatus.BAD_REQUEST)));

    }

    @Test
    public void saveSpendProfileWhenCostsAreLessThanZero() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        table.setMonthlyCostsPerCategoryMap(asMap(
                "Labour", asList(new BigDecimal("0"), new BigDecimal("00"), new BigDecimal("-1")),
                "Materials", asList(new BigDecimal("70"), new BigDecimal("-2"), new BigDecimal("60")),
                "Other costs", asList(new BigDecimal("50"), new BigDecimal("1"), new BigDecimal("-33"))));

        ServiceResult<Void> result = service.saveSpendProfile(projectOrganisationCompositeId, table);

        assertTrue(result.isFailure());

        List<Error> errors = result.getFailure().getErrors();

        assertTrue(errors.size() == 3);

        // Assert that the error messages are for correct categories and correct month(s) based on the input
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_LESS_THAN_ZERO_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Labour", 3), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_LESS_THAN_ZERO_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Materials", 2), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_LESS_THAN_ZERO_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Other costs", 3), HttpStatus.BAD_REQUEST)));

    }

    @Test
    public void saveSpendProfileWhenCostsAreGreaterThanOrEqualToMillion() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        table.setMonthlyCostsPerCategoryMap(asMap(
                "Labour", asList(new BigDecimal("1000000"), new BigDecimal("30"), new BigDecimal("40")),
                "Materials", asList(new BigDecimal("999999"), new BigDecimal("1000001"), new BigDecimal("60")),
                "Other costs", asList(new BigDecimal("50"), new BigDecimal("2000000"), new BigDecimal("10"))));

        ServiceResult<Void> result = service.saveSpendProfile(projectOrganisationCompositeId, table);

        assertTrue(result.isFailure());

        List<Error> errors = result.getFailure().getErrors();

        assertTrue(errors.size() == 3);

        // Assert that the error messages are for correct categories and correct month(s) based on the input
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_MORE_THAN_MILLION_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Labour", 1), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_MORE_THAN_MILLION_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Materials", 2), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_MORE_THAN_MILLION_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Other costs", 2), HttpStatus.BAD_REQUEST)));

    }

    @Test
    public void saveSpendProfileWhenCostsAreFractionalLessThanZeroOrGreaterThanMillion() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        table.setMonthlyCostsPerCategoryMap(asMap(
                "Labour", asList(new BigDecimal("30.12"), new BigDecimal("30"), new BigDecimal("40")),
                "Materials", asList(new BigDecimal("70"), new BigDecimal("-30"), new BigDecimal("60")),
                "Other costs", asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("1000001"))));

        ServiceResult<Void> result = service.saveSpendProfile(projectOrganisationCompositeId, table);

        assertTrue(result.isFailure());

        List<Error> errors = result.getFailure().getErrors();

        assertTrue(errors.size() == 3);

        // Assert that the error messages are for correct categories and correct month(s) based on the input
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Labour", 1), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_LESS_THAN_ZERO_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Materials", 2), HttpStatus.BAD_REQUEST)));
        assertTrue(errors.contains(
                new Error(SPEND_PROFILE_COST_MORE_THAN_MILLION_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Other costs", 3), HttpStatus.BAD_REQUEST)));

    }

    @Test
    public void saveSpendProfileEnsureSpendProfileDomainIsCorrectlyUpdated() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        table.setEligibleCostPerCategoryMap(asMap(
                "Labour", new BigDecimal("100"),
                "Materials", new BigDecimal("180"),
                "Other costs", new BigDecimal("55")));

        table.setMonthlyCostsPerCategoryMap(asMap(
                "Labour", asList(new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("40")),
                "Materials", asList(new BigDecimal("70"), new BigDecimal("50"), new BigDecimal("60")),
                "Other costs", asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("0"))));


        List<Cost> spendProfileFigures = buildCostsForCategories(Arrays.asList("Labour", "Materials", "Other costs"), 3);

        SpendProfile spendProfileInDB = new SpendProfile(null, null, null, Collections.emptyList(), spendProfileFigures, false);

        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(spendProfileInDB);

        // Before the call (ie before the SpendProfile is updated), ensure that the values are set to 1
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 0, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 1, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 2, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 0, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 1, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 2, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 0, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 1, BigDecimal.ONE);
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 2, BigDecimal.ONE);

        ServiceResult<Void> result = service.saveSpendProfile(projectOrganisationCompositeId, table);

        assertTrue(result.isSuccess());

        // Assert that the SpendProfile domain is correctly updated
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 0, new BigDecimal("30"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 1, new BigDecimal("30"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Labour", 2, new BigDecimal("40"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 0, new BigDecimal("70"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 1, new BigDecimal("50"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Materials", 2, new BigDecimal("60"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 0, new BigDecimal("50"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 1, new BigDecimal("5"));
        assertCostForCategoryForGivenMonth(spendProfileInDB, "Other costs", 2, new BigDecimal("0"));

        verify(spendProfileRepositoryMock).save(spendProfileInDB);

    }


    @Test
    public void markSpendProfileWhenActualTotalsGreaterThanEligibleCosts() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        Project projectInDB = ProjectBuilder.newProject()
                .withDuration(3L)
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .build();

        SpendProfile spendProfileInDB = createSpendProfile(projectInDB,
                // eligible costs
                asMap(
                        "Labour", new BigDecimal("100"),
                        "Materials", new BigDecimal("180"),
                        "Other costs", new BigDecimal("55")),

                // Spend Profile costs
                asMap(
                        "Labour", asList(new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("50")),
                        "Materials", asList(new BigDecimal("70"), new BigDecimal("50"), new BigDecimal("60")),
                        "Other costs", asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("0")))
        );

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);

        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(spendProfileInDB);

        ServiceResult<Void> result = service.markSpendProfile(projectOrganisationCompositeId, true);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE));

    }

    @Test
    public void markSpendProfileSuccessWhenActualTotalsAreLessThanEligibleCosts() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        Project projectInDB = ProjectBuilder.newProject()
                .withDuration(3L)
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .build();

        SpendProfile spendProfileInDB = createSpendProfile(projectInDB,
                // eligible costs
                asMap(
                        "Labour", new BigDecimal("100"),
                        "Materials", new BigDecimal("180"),
                        "Other costs", new BigDecimal("55")),

                // Spend Profile costs
                asMap(
                        "Labour", asList(new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("40")),
                        "Materials", asList(new BigDecimal("70"), new BigDecimal("10"), new BigDecimal("60")),
                        "Other costs", asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("0")))
        );

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);

        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(spendProfileInDB);

        ServiceResult<Void> result = service.markSpendProfile(projectOrganisationCompositeId, true);

        assertTrue(result.isSuccess());

    }

    private SpendProfile createSpendProfile(Project projectInDB, Map<String, BigDecimal> eligibleCostsMap, Map<String, List<BigDecimal>> spendProfileCostsMap) {

        CostCategoryType costCategoryType = createCostCategoryType();

        List<Cost> eligibleCosts = buildEligibleCostsForCategories(eligibleCostsMap, costCategoryType.getCostCategoryGroup());

        List<Cost> spendProfileFigures = buildCostsForCategoriesWithGivenValues(spendProfileCostsMap, 3, costCategoryType.getCostCategoryGroup());

        SpendProfile spendProfileInDB = new SpendProfile(null, projectInDB, costCategoryType, eligibleCosts, spendProfileFigures, true);

        return spendProfileInDB;

    }

    private CostCategoryType createCostCategoryType() {

        CostCategoryGroup costCategoryGroup = createCostCategoryGroup();

        CostCategoryType costCategoryType = new CostCategoryType("Cost Category Type for Categories Labour, Materials, Other costs", costCategoryGroup);

        return costCategoryType;

    }

    private CostCategoryGroup createCostCategoryGroup() {

        List<CostCategory> costCategories = createCostCategories(Arrays.asList("Labour", "Materials", "Other costs"));

        CostCategoryGroup costCategoryGroup = new CostCategoryGroup("Cost Category Group for Categories Labour, Materials, Other costs", costCategories);

        return costCategoryGroup;
    }

    private List<CostCategory> createCostCategories(List<String> categories) {

        List<CostCategory> costCategories = new ArrayList<>();

        categories.stream().forEach(category -> {
            CostCategory costCategory = new CostCategory();
            costCategory.setName(category);

            costCategories.add(costCategory);

        });

        return costCategories;
    }

    private List<Cost> buildEligibleCostsForCategories(Map<String, BigDecimal> categoryCost, CostCategoryGroup costCategoryGroup) {

        List<Cost> eligibleCostForAllCategories = new ArrayList<>();

        categoryCost.forEach((category, value) -> {

            eligibleCostForAllCategories.add(createEligibleCost(category, value, costCategoryGroup));

        });

        return eligibleCostForAllCategories;

    }

    private Cost createEligibleCost(String category, BigDecimal value, CostCategoryGroup costCategoryGroup) {

        CostCategory costCategory = new CostCategory();
        costCategory.setName(category);
        costCategory.setCostCategoryGroup(costCategoryGroup);

        Cost cost = new Cost();
        cost.setCostCategory(costCategory);
        cost.setValue(value);

        return cost;

    }

    private void assertCostForCategoryForGivenMonth(SpendProfile spendProfileInDB, String category, Integer whichMonth, BigDecimal expectedValue) {

        boolean thisCostShouldExist = spendProfileInDB.getSpendProfileFigures().getCosts().stream()
                .anyMatch(cost -> cost.getCostCategory().getName().equalsIgnoreCase(category)
                        && cost.getCostTimePeriod().getOffsetAmount().equals(whichMonth)
                        && cost.getValue().equals(expectedValue));
        Assert.assertTrue(thisCostShouldExist);
    }

    private List<Cost> buildCostsForCategories(List<String> categories, int totalMonths) {

        List<Cost> costForAllCategories = new ArrayList<>();

        categories.forEach(category -> {

            // Intentionally insert in the reverse order of months to ensure that the sorting functionality actually works
            for (int index = totalMonths - 1; index >= 0; index--) {
                costForAllCategories.add(createCost(category, index, BigDecimal.ONE, null));
            }
        });

        return costForAllCategories;

    }

    private List<Cost> buildCostsForCategoriesWithGivenValues(Map<String, List<BigDecimal>> categoryCosts, int totalMonths, CostCategoryGroup costCategoryGroup) {

        List<Cost> costForAllCategories = new ArrayList<>();

        categoryCosts.forEach((category, costs) -> {

            for (int index = 0; index < totalMonths; index++) {
                costForAllCategories.add(createCost(category, index, costs.get(index), costCategoryGroup));
            }
        });

        return costForAllCategories;

    }

    private Cost createCost(String category, Integer offsetAmount, BigDecimal value, CostCategoryGroup costCategoryGroup) {

        CostCategory costCategory = new CostCategory();
        costCategory.setName(category);
        costCategory.setCostCategoryGroup(costCategoryGroup);

        //CostTimePeriod(Integer offsetAmount, TimeUnit offsetUnit, Integer durationAmount, TimeUnit durationUnit)
        CostTimePeriod costTimePeriod = new CostTimePeriod(offsetAmount, TimeUnit.MONTH, 1, TimeUnit.MONTH);

        Cost cost = new Cost();
        cost.setCostCategory(costCategory);
        cost.setCostTimePeriod(costTimePeriod);
        cost.setValue(value);

        return cost;

    }

    private SpendProfile spendProfileExpectations(SpendProfile expectedSpendProfile) {
        return createLambdaMatcher(spendProfile -> {

            assertEquals(expectedSpendProfile.getOrganisation(), spendProfile.getOrganisation());
            assertEquals(expectedSpendProfile.getProject(), spendProfile.getProject());
            assertEquals(expectedSpendProfile.getCostCategoryType(), spendProfile.getCostCategoryType());

            CostGroup expectedEligibles = expectedSpendProfile.getEligibleCosts();
            CostGroup actualEligibles = spendProfile.getEligibleCosts();
            assertCostGroupsEqual(expectedEligibles, actualEligibles);

            CostGroup expectedSpendFigures = expectedSpendProfile.getSpendProfileFigures();
            CostGroup actualSpendFigures = spendProfile.getSpendProfileFigures();
            assertCostGroupsEqual(expectedSpendFigures, actualSpendFigures);
        });
    }

    private void assertCostGroupsEqual(CostGroup expected, CostGroup actual) {
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getCosts().size(), actual.getCosts().size());
        expected.getCosts().forEach(expectedCost ->
                assertTrue(simpleFindFirst(actual.getCosts(), actualCost -> costsMatch(expectedCost, actualCost)).isPresent())
        );
    }

    private boolean costsMatch(Cost expectedCost, Cost actualCost) {
        try {
            CostGroup expectedCostGroup = expectedCost.getCostGroup();
            CostGroup actualCostGroup = actualCost.getCostGroup();
            assertEquals(expectedCostGroup != null, actualCostGroup != null);

            if (expectedCostGroup != null) {
                assertEquals(expectedCostGroup.getDescription(), actualCostGroup.getDescription());
            }

            assertEquals(expectedCost.getValue(), actualCost.getValue());
            assertEquals(expectedCost.getCostCategory(), actualCost.getCostCategory());

            CostTimePeriod expectedTimePeriod = expectedCost.getCostTimePeriod();
            CostTimePeriod actualTimePeriod = actualCost.getCostTimePeriod();
            assertEquals(expectedTimePeriod != null, actualTimePeriod != null);

            if (expectedTimePeriod != null) {
                assertEquals(expectedTimePeriod.getOffsetAmount(), actualTimePeriod.getOffsetAmount());
                assertEquals(expectedTimePeriod.getOffsetUnit(), actualTimePeriod.getOffsetUnit());
                assertEquals(expectedTimePeriod.getDurationAmount(), actualTimePeriod.getDurationAmount());
                assertEquals(expectedTimePeriod.getDurationUnit(), actualTimePeriod.getDurationUnit());
            }

            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    @Override
    protected ProjectFinanceServiceImpl supplyServiceUnderTest() {
        return new ProjectFinanceServiceImpl();
    }
}
