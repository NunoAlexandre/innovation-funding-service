package org.innovateuk.ifs.project.finance.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.project.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.finance.domain.CostCategory;
import org.innovateuk.ifs.project.finance.domain.FinanceCheck;
import org.innovateuk.ifs.project.finance.domain.FinanceCheckProcess;
import org.innovateuk.ifs.project.finance.domain.SpendProfile;
import org.innovateuk.ifs.project.finance.resource.*;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.innovateuk.ifs.workflow.resource.State;
import org.innovateuk.threads.resource.FinanceChecksSectionType;
import org.innovateuk.threads.resource.QueryResource;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.builder.DefaultCostCategoryBuilder.newDefaultCostCategory;
import static org.innovateuk.ifs.finance.builder.GrantClaimCostBuilder.newGrantClaim;
import static org.innovateuk.ifs.finance.builder.GrantClaimCostCategoryBuilder.newGrantClaimCostCategory;
import static org.innovateuk.ifs.finance.builder.LabourCostBuilder.newLabourCost;
import static org.innovateuk.ifs.finance.builder.LabourCostCategoryBuilder.newLabourCostCategory;
import static org.innovateuk.ifs.finance.builder.MaterialsCostBuilder.newMaterials;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostBuilder.newOtherFunding;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostCategoryBuilder.newOtherFundingCostCategory;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static org.innovateuk.ifs.finance.resource.category.LabourCostCategory.WORKING_DAYS_PER_YEAR;
import static org.innovateuk.ifs.finance.resource.category.OtherFundingCostCategory.OTHER_FUNDING;
import static org.innovateuk.ifs.project.builder.CostBuilder.newCost;
import static org.innovateuk.ifs.project.builder.CostCategoryBuilder.newCostCategory;
import static org.innovateuk.ifs.project.builder.CostGroupBuilder.newCostGroup;
import static org.innovateuk.ifs.project.builder.CostGroupResourceBuilder.newCostGroupResource;
import static org.innovateuk.ifs.project.builder.CostResourceBuilder.newCostResource;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.project.builder.SpendProfileBuilder.newSpendProfile;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckBuilder.newFinanceCheck;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckProcessBuilder.newFinanceCheckProcess;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckResourceBuilder.newFinanceCheckResource;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.innovateuk.ifs.workflow.domain.ActivityType.PROJECT_SETUP_FINANCE_CHECKS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class FinanceCheckServiceImplTest extends BaseServiceUnitTest<FinanceCheckServiceImpl> {

    @Test
    public void testGetByProjectAndOrganisationNotFound() {
        // Set up
        Long projectId = 1L;
        Long organisationId = 2L;
        ProjectOrganisationCompositeId compositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        when(financeCheckRepositoryMock.findByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(null);
        // Method under test
        ServiceResult<FinanceCheckResource> result = service.getByProjectAndOrganisation(compositeId);
        // Assertions
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(FinanceCheck.class, compositeId)));
    }

    @Test
    public void testGetByProjectAndOrganisation() {
        // Set up
        Long projectId = 1L;
        Long organisationId = 2L;
        ProjectOrganisationCompositeId compositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        FinanceCheck financeCheck = newFinanceCheck().
                withProject(newProject().with(id(projectId)).build()).
                withOrganisation(newOrganisation().with(id(organisationId)).build()).
                withCostGroup(newCostGroup().
                        withCosts(newCost().
                                withValue("1", "2").
                                withCostCategory(
                                        newCostCategory().
                                                withName("cat 1", "cat 2").
                                                buildArray(2, CostCategory.class)).
                                build(2)).
                        build()).
                build();
        // Method under test
        when(financeCheckRepositoryMock.findByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(financeCheck);
        ServiceResult<FinanceCheckResource> result = service.getByProjectAndOrganisation(compositeId);
        // Assertions - basically testing the deserialisation into resource objects
        assertTrue(result.isSuccess());
        assertEquals(financeCheck.getId(), result.getSuccessObject().getId());
        assertEquals(financeCheck.getOrganisation().getId(), result.getSuccessObject().getOrganisation());
        assertEquals(financeCheck.getProject().getId(), result.getSuccessObject().getProject());
        assertEquals(financeCheck.getCostGroup().getId(), result.getSuccessObject().getCostGroup().getId());
        assertEquals(financeCheck.getCostGroup().getCosts().size(), result.getSuccessObject().getCostGroup().getCosts().size());
        assertEquals(financeCheck.getCostGroup().getCosts().get(0).getCostCategory().getId(), result.getSuccessObject().getCostGroup().getCosts().get(0).getCostCategory().getId());
    }

    @Test
    public void testSaveFinanceCheck(){
        Long projectId = 1L;
        Long organisationId = 2L;

        User loggedInUser = newUser().build();
        UserResource loggedInUserResource = newUserResource().withId(loggedInUser.getId()).build();
        Organisation organisation = newOrganisation().withId(organisationId).withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisation).build();
        FinanceCheckResource financeCheckResource = newFinanceCheckResource().withProject(projectId).withOrganisation(organisationId).build();
        FinanceCheck financeCheck = newFinanceCheck().withOrganisation(newOrganisation().withId(organisationId).build()).withProject(newProject().withId(projectId).build()).build();
        when(userRepositoryMock.findOne(loggedInUserResource.getId())).thenReturn(loggedInUser);
        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(partnerOrganisation);
        when(financeCheckWorkflowHandlerMock.financeCheckFiguresEdited(partnerOrganisation, loggedInUser)).thenReturn(true);
        when(financeCheckRepositoryMock.findByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(financeCheck);
        setLoggedInUser(loggedInUserResource);

        ServiceResult result = service.save(financeCheckResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveFinanceCheckValidationFail(){
        Long projectId = 1L;
        Long organisationId = 2L;

        Organisation organisation = newOrganisation().withId(organisationId).withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisation).build();

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().
                withProject(projectId).
                withOrganisation(organisationId).
                withCostGroup(newCostGroupResource().
                        withCosts(newCostResource().
                                withValue(new BigDecimal("1.01"), null, new BigDecimal("-1"), new BigDecimal("1.00")).build(4)).
                        build()).
                build();

        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(partnerOrganisation);

        ServiceResult result = service.validate(financeCheckResource);

        assertTrue(result.isFailure());
        assertEquals(3, result.getErrors().size());
        assertEquals(3, result.getErrors().size());
        assertTrue(result.getErrors().contains(new Error(FINANCE_CHECKS_COST_LESS_THAN_ZERO, HttpStatus.BAD_REQUEST)));
        assertTrue(result.getErrors().contains(new Error(FINANCE_CHECKS_CONTAINS_FRACTIONS_IN_COST,  HttpStatus.BAD_REQUEST)));
        assertTrue(result.getErrors().contains(new Error(FINANCE_CHECKS_COST_NULL, HttpStatus.BAD_REQUEST)));
    }

    @Test
    public void testSaveFinanceCheckValidationPass(){
        Long projectId = 1L;
        Long organisationId = 2L;

        Organisation organisation = newOrganisation().withId(organisationId).withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisation).build();

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().
                withProject(projectId).
                withOrganisation(organisationId).
                withCostGroup(newCostGroupResource().
                        withCosts(newCostResource().
                                withValue(new BigDecimal("1.00"), BigDecimal.ZERO, BigDecimal.ONE).build(3)).
                        build()).
                build();

        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(partnerOrganisation);

        ServiceResult result = service.validate(financeCheckResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveFinanceCheckValidationPassOnUseOfDecimalsForAcademicPartners(){
        Long projectId = 1L;
        Long organisationId = 2L;

        Organisation organisation = newOrganisation().withId(organisationId).withOrganisationType(OrganisationTypeEnum.ACADEMIC).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisation).build();

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().
                withProject(projectId).
                withOrganisation(organisationId).
                withCostGroup(newCostGroupResource().
                        withCosts(newCostResource().
                                withValue(new BigDecimal("1.10"), BigDecimal.ZERO, BigDecimal.ONE).build(3)).
                        build()).
                build();

        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(partnerOrganisation);

        ServiceResult result = service.validate(financeCheckResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveFinanceCheckWhenWorkflowStepFails(){
        Long projectId = 1L;
        Long organisationId = 2L;

        User loggedInUser = newUser().build();
        UserResource loggedInUserResource = newUserResource().withId(loggedInUser.getId()).build();
        Organisation organisation = newOrganisation().withId(organisationId).withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisation).build();
        FinanceCheckResource financeCheckResource = newFinanceCheckResource().withProject(projectId).withOrganisation(organisationId).build();
        FinanceCheck financeCheck = newFinanceCheck().withOrganisation(newOrganisation().withId(organisationId).build()).withProject(newProject().withId(projectId).build()).build();
        when(userRepositoryMock.findOne(loggedInUserResource.getId())).thenReturn(loggedInUser);
        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(partnerOrganisation);
        when(financeCheckWorkflowHandlerMock.financeCheckFiguresEdited(partnerOrganisation, loggedInUser)).thenReturn(false);
        when(financeCheckRepositoryMock.findByProjectIdAndOrganisationId(projectId, organisationId)).thenReturn(financeCheck);
        setLoggedInUser(loggedInUserResource);

        ServiceResult<Void> result = service.save(financeCheckResource);

        assertTrue(result.getFailure().is(FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW));
    }

    @Test
    public void testApprove() {

        User loggedInUser = newUser().build();
        UserResource loggedInUserResource = newUserResource().withId(loggedInUser.getId()).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().build();

        when(userRepositoryMock.findOne(loggedInUserResource.getId())).thenReturn(loggedInUser);
        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(123L, 456L)).thenReturn(partnerOrganisation);
        when(financeCheckWorkflowHandlerMock.approveFinanceCheckFigures(partnerOrganisation, loggedInUser)).thenReturn(true);

        setLoggedInUser(loggedInUserResource);

        ServiceResult<Void> result = service.approve(123L, 456L);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testApproveButWorkflowStepFails() {

        User loggedInUser = newUser().build();
        UserResource loggedInUserResource = newUserResource().withId(loggedInUser.getId()).build();
        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().build();

        when(userRepositoryMock.findOne(loggedInUserResource.getId())).thenReturn(loggedInUser);
        when(partnerOrganisationRepositoryMock.findOneByProjectIdAndOrganisationId(123L, 456L)).thenReturn(partnerOrganisation);
        when(financeCheckWorkflowHandlerMock.approveFinanceCheckFigures(partnerOrganisation, loggedInUser)).thenReturn(false);

        setLoggedInUser(loggedInUserResource);

        ServiceResult<Void> result = service.approve(123L, 456L);
        assertTrue(result.getFailure().is(FINANCE_CHECKS_CANNOT_PROGRESS_WORKFLOW));
    }

    @Test
    public void testGetFinanceCheckSummary(){

        Long projectId = 123L;
        Long applicationId = 456L;

        Competition competition = newCompetition().build();
        Application application = newApplication().withId(applicationId).withCompetition(competition).build();
        Project project = newProject().withId(projectId).withApplication(application).withDuration(6L).build();

        Organisation[] organisations = newOrganisation().
                withOrganisationType(OrganisationTypeEnum.BUSINESS, OrganisationTypeEnum.ACADEMIC, OrganisationTypeEnum.BUSINESS).
                buildArray(3, Organisation.class);

        List<PartnerOrganisation> partnerOrganisations = newPartnerOrganisation().
                withProject(project).
                withOrganisation(organisations).
                build(3);

        User projectFinanceUser = newUser().withFirstName("Project").withLastName("Finance").build();
        Optional<SpendProfile> spendProfile = Optional.of(newSpendProfile().withGeneratedBy(projectFinanceUser).withGeneratedDate(new GregorianCalendar()).build());
        List<ProjectFinanceResource> projectFinanceResourceList = newProjectFinanceResource().build(3);
        ProjectTeamStatusResource projectTeamStatus = newProjectTeamStatusResource().build();

        FinanceCheckProcess process = newFinanceCheckProcess().withModifiedDate(new GregorianCalendar()).build();
        ActivityState pendingState = new ActivityState(PROJECT_SETUP_FINANCE_CHECKS, State.PENDING);
        process.setActivityState(pendingState);
        ProjectUserResource projectUser = newProjectUserResource().build();
        UserResource user = newUserResource().build();
        ViabilityResource viability1 = new ViabilityResource(Viability.APPROVED, ViabilityRagStatus.AMBER);
        ViabilityResource viability2 = new ViabilityResource(Viability.NOT_APPLICABLE, ViabilityRagStatus.UNSET);
        ViabilityResource viability3 = new ViabilityResource(Viability.REVIEW, ViabilityRagStatus.UNSET);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(partnerOrganisationRepositoryMock.findByProjectId(projectId)).thenReturn(partnerOrganisations);
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(projectId, partnerOrganisations.get(0).getOrganisation().getId())).thenReturn(spendProfile);
        when(projectFinanceRowServiceMock.financeChecksTotals(project.getId())).thenReturn(serviceSuccess(projectFinanceResourceList));
        when(projectServiceMock.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(serviceSuccess(projectTeamStatus));
        when(financeCheckProcessRepository.findOneByTargetId(partnerOrganisations.get(0).getId())).thenReturn(process);
        when(financeCheckProcessRepository.findOneByTargetId(partnerOrganisations.get(1).getId())).thenReturn(process);
        when(financeCheckProcessRepository.findOneByTargetId(partnerOrganisations.get(2).getId())).thenReturn(process);
        when(projectUserMapperMock.mapToResource(process.getParticipant())).thenReturn(projectUser);
        when(userMapperMock.mapToResource(process.getInternalParticipant())).thenReturn(user);

        when(projectFinanceServiceMock.getViability(new ProjectOrganisationCompositeId(projectId, organisations[0].getId()))).thenReturn(serviceSuccess(viability1));
        when(projectFinanceServiceMock.getViability(new ProjectOrganisationCompositeId(projectId, organisations[1].getId()))).thenReturn(serviceSuccess(viability2));
        when(projectFinanceServiceMock.getViability(new ProjectOrganisationCompositeId(projectId, organisations[2].getId()))).thenReturn(serviceSuccess(viability3));

        ProjectFinanceResource[] projectFinanceResources = newProjectFinanceResource().withId(234L, 345L, 456L).withOrganisation(organisations[0].getId(), organisations[1].getId(), organisations[2].getId()).buildArray(3, ProjectFinanceResource.class);
        when(projectFinanceServiceMock.getProjectFinances(projectId)).thenReturn(ServiceResult.serviceSuccess(Arrays.asList(projectFinanceResources)));
        QueryResource queryResource1 = new QueryResource(12L, 23L, new ArrayList<>(), FinanceChecksSectionType.ELIGIBILITY, "Title" , true, LocalDateTime.now());
        QueryResource queryResource2 = new QueryResource(12L, 23L, new ArrayList<>(), FinanceChecksSectionType.ELIGIBILITY, "Title" , false, LocalDateTime.now());
        when(projectFinanceQueriesService.findAll(234L)).thenReturn(serviceSuccess(Arrays.asList(queryResource1)));
        when(projectFinanceQueriesService.findAll(345L)).thenReturn(serviceSuccess(new ArrayList<>()));
        when(projectFinanceQueriesService.findAll(456L)).thenReturn(serviceSuccess(Arrays.asList(queryResource2)));
        ServiceResult<FinanceCheckSummaryResource> result = service.getFinanceCheckSummary(projectId);
        assertTrue(result.isSuccess());

        FinanceCheckSummaryResource summary = result.getSuccessObject();
        List<FinanceCheckPartnerStatusResource> partnerStatuses = summary.getPartnerStatusResources();
        assertEquals(3, partnerStatuses.size());

        FinanceCheckPartnerStatusResource organisation1Results = partnerStatuses.get(0);
        assertEquals(Viability.APPROVED, organisation1Results.getViability());
        assertEquals(viability1.getViabilityRagStatus(), organisation1Results.getViabilityRagStatus());
        assertTrue(organisation1Results.isAwaitingResponse());

        FinanceCheckPartnerStatusResource organisation2Results = partnerStatuses.get(1);
        assertEquals(Viability.NOT_APPLICABLE, organisation2Results.getViability());
        assertEquals(ViabilityRagStatus.UNSET, organisation2Results.getViabilityRagStatus());
        assertFalse(organisation2Results.isAwaitingResponse());

        FinanceCheckPartnerStatusResource organisation3Results = partnerStatuses.get(2);
        assertEquals(Viability.REVIEW, organisation3Results.getViability());
        assertEquals(viability3.getViabilityRagStatus(), organisation3Results.getViabilityRagStatus());
        assertFalse(organisation3Results.isAwaitingResponse());
    }

    @Test
    public void testGetFinanceCheckEligibility(){

        Long projectId = 123L;
        Long applicationId = 456L;
        Long organisationId = 789L;

        Competition competition = newCompetition().build();
        Application application = newApplication().withId(applicationId).withCompetition(competition).withDurationInMonths(5L).build();
        Project project = newProject().withId(projectId).withApplication(application).withDuration(6L).withName("Project1").build();

        Organisation organisation = newOrganisation().
                withOrganisationType(OrganisationTypeEnum.BUSINESS).withId(organisationId).withName("Organisation1").build();


        Map<FinanceRowType, FinanceRowCostCategory> projectFinances = createProjectFinance();

        Map<FinanceRowType, FinanceRowCostCategory> applicationFinances = asMap(
                FinanceRowType.LABOUR, newLabourCostCategory().withCosts(
                        newLabourCost().
                                withGrossAnnualSalary(new BigDecimal("1.0"), BigDecimal.ZERO).
                                withDescription("Developers", WORKING_DAYS_PER_YEAR).
                                withLabourDays(1, 200).
                                build(2)).
                        build(),
                FinanceRowType.MATERIALS, newDefaultCostCategory().withCosts(
                        newMaterials().
                                withCost(new BigDecimal("1.0")).
                                withQuantity(1).
                                build(1)).
                        build(),
                FinanceRowType.OTHER_FUNDING, newOtherFundingCostCategory().withCosts(
                        newOtherFunding().
                                withOtherPublicFunding("Yes", "").
                                withFundingSource(OTHER_FUNDING, "other funding").
                                withFundingAmount(null, BigDecimal.valueOf(2)).
                                build(2)).
                        build());

        projectFinances.forEach((type, category) -> category.calculateTotal());
        applicationFinances.forEach((type, category) -> category.calculateTotal());

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource().withFinanceOrganisationDetails(applicationFinances).withGrantClaimPercentage(25).build();

        ProjectFinanceResource projectFinanceResource = newProjectFinanceResource().
                withProject(projectId).
                withOrganisation(organisation.getId()).
                withFinanceOrganisationDetails(projectFinances).
                build();


        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisationId)).thenReturn(organisation);
        when(projectFinanceRowServiceMock.financeChecksDetails(projectId, organisationId)).thenReturn(serviceSuccess(projectFinanceResource));
        when(financeRowServiceMock.financeDetails(applicationId, organisationId)).thenReturn(serviceSuccess(applicationFinanceResource));

        ServiceResult<FinanceCheckEligibilityResource> result = service.getFinanceCheckEligibilityDetails(projectId, organisationId);
        assertTrue(result.isSuccess());

        FinanceCheckEligibilityResource eligibility = result.getSuccessObject();

        assertTrue(eligibility.getDurationInMonths() == 5L);
        assertTrue(new BigDecimal("5000033.33").compareTo(eligibility.getTotalCost()) == 0);
        assertTrue(new BigDecimal("25").compareTo(eligibility.getPercentageGrant()) == 0);
        assertTrue((new BigDecimal("5000033.33").multiply(new BigDecimal("0.25"))).compareTo(eligibility.getFundingSought()) == 0);
        assertTrue(new BigDecimal("1000").compareTo(eligibility.getOtherPublicSectorFunding()) == 0);
        assertTrue((new BigDecimal("4999033.33").subtract(new BigDecimal("5000033.33").multiply(new BigDecimal("0.25")))).compareTo(eligibility.getContributionToProject()) == 0);
    }

    @Test
    public void testGetFinanceCheckEligibilityNoProjectFinances(){

        Long projectId = 123L;
        Long applicationId = 456L;
        Long organisationId = 789L;

        Competition competition = newCompetition().build();
        Application application = newApplication().withId(applicationId).withCompetition(competition).withDurationInMonths(5L).build();
        Project project = newProject().withId(projectId).withApplication(application).withDuration(6L).withName("Project1").build();

        Organisation organisation = newOrganisation().
                withOrganisationType(OrganisationTypeEnum.BUSINESS).withId(organisationId, organisationId + 1L).withName("Organisation1").build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisationId)).thenReturn(organisation);
        when(projectFinanceRowServiceMock.financeChecksDetails(projectId, organisationId)).thenReturn(serviceFailure(GENERAL_NOT_FOUND));

        ServiceResult<FinanceCheckEligibilityResource> result = service.getFinanceCheckEligibilityDetails(projectId, organisationId);
        assertTrue(result.isFailure());

    }

    @Test
    public void testGetFinanceCheckEligibilityNoApplicationFinances(){

        Long projectId = 123L;
        Long applicationId = 456L;
        Long organisationId = 789L;

        Competition competition = newCompetition().build();
        Application application = newApplication().withId(applicationId).withCompetition(competition).withDurationInMonths(5L).build();
        Project project = newProject().withId(projectId).withApplication(application).withDuration(6L).withName("Project1").build();

        Organisation organisation = newOrganisation().
                withOrganisationType(OrganisationTypeEnum.BUSINESS).withId(organisationId).withName("Organisation1").build();

        Map<FinanceRowType, FinanceRowCostCategory> projectFinances = createProjectFinance();
        ProjectFinanceResource projectFinanceResource = newProjectFinanceResource().
                withProject(projectId).
                withOrganisation(organisation.getId()).
                withFinanceOrganisationDetails(projectFinances).
                build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisationId)).thenReturn(organisation);
        when(projectFinanceRowServiceMock.financeChecksDetails(projectId, organisationId)).thenReturn(serviceSuccess(projectFinanceResource));
        when(financeRowServiceMock.financeDetails(applicationId, organisationId)).thenReturn(serviceFailure(GENERAL_NOT_FOUND));

        ServiceResult<FinanceCheckEligibilityResource> result = service.getFinanceCheckEligibilityDetails(projectId, organisationId);
        assertTrue(result.isFailure());

    }

    @Override
    protected FinanceCheckServiceImpl supplyServiceUnderTest() {

        FinanceCheckServiceImpl impl = new FinanceCheckServiceImpl();
        return impl;
    }

    private Map<FinanceRowType, FinanceRowCostCategory> createProjectFinance() {
        return asMap(
                FinanceRowType.LABOUR, newLabourCostCategory().withCosts(
                        newLabourCost().
                                withGrossAnnualSalary(new BigDecimal("10000000"), BigDecimal.ZERO).
                                withDescription("Developers", WORKING_DAYS_PER_YEAR).
                                withLabourDays(100, 200).
                                build(2)).
                        build(),
                FinanceRowType.MATERIALS, newDefaultCostCategory().withCosts(
                        newMaterials().
                                withCost(new BigDecimal("33.33")).
                                withQuantity(1).
                                build(1)).
                        build(),
                FinanceRowType.FINANCE, newGrantClaimCostCategory().withCosts(
                        newGrantClaim().
                                withGrantClaimPercentage(30).
                                build(1)).
                        build(),
                FinanceRowType.OTHER_FUNDING, newOtherFundingCostCategory().withCosts(
                        newOtherFunding().
                                withOtherPublicFunding("Yes", "").
                                withFundingSource(OTHER_FUNDING, "Other funder").
                                withFundingAmount(null, BigDecimal.valueOf(1000)).
                                build(2)).
                        build());
    }
}
