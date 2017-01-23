package org.innovateuk.ifs.project.finance.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.project.finance.resource.Eligibility;
import org.innovateuk.ifs.project.finance.resource.EligibilityResource;
import org.innovateuk.ifs.project.finance.resource.EligibilityStatus;
import org.innovateuk.ifs.project.finance.resource.Viability;
import org.innovateuk.ifs.project.finance.resource.ViabilityResource;
import org.innovateuk.ifs.project.finance.resource.ViabilityStatus;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.SpendProfileCSVResource;
import org.innovateuk.ifs.project.resource.SpendProfileTableResource;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectFinanceResourceListType;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

public class ProjectFinanceRestServiceImplTest extends BaseRestServiceUnitTest<ProjectFinanceRestServiceImpl> {

    private static final String projectFinanceRestURL = "/project";

    @Override
    protected ProjectFinanceRestServiceImpl registerRestServiceUnderTest() {
        return new ProjectFinanceRestServiceImpl();
    }

    @Test
    public void testGenerateSpendProfile() {

        setupPostWithRestResultExpectations("/project/123/spend-profile/generate", Void.class, null, null, CREATED);
        service.generateSpendProfile(123L);
        setupPostWithRestResultVerifications("/project/123/spend-profile/generate", Void.class, null);
    }

    @Test
    public void saveSpendProfile() {

        Long projectId = 1L;
        Long organisationId = 2L;

        SpendProfileTableResource table = new SpendProfileTableResource();

        setupPostWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile",
                table,
                OK);

        RestResult<Void> result = service.saveSpendProfile(projectId, organisationId,  table);
        setupPostWithRestResultVerifications("/project/1/partner-organisation/2/spend-profile", Void.class, table);


        assertTrue(result.isSuccess());

    }

    @Test
    public void markSpendProfileComplete() {

        Long projectId = 1L;
        Long organisationId = 2L;

        setupPostWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile/complete",
                OK);

        RestResult<Void> result = service.markSpendProfileComplete(projectId, organisationId);
        setupPostWithRestResultVerifications("/project/1/partner-organisation/2/spend-profile/complete", Void.class, null);

        assertTrue(result.isSuccess());
    }

    @Test
    public void markSpendProfileIncomplete() {

        Long projectId = 1L;
        Long organisationId = 2L;

        setupPostWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile/incomplete",
                OK);

        RestResult<Void> result = service.markSpendProfileIncomplete(projectId, organisationId);
        setupPostWithRestResultVerifications("/project/1/partner-organisation/2/spend-profile/incomplete", Void.class, null);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testCompleteSpendProfilesReview() {

        Long projectId = 1L;

        setupPostWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/complete-spend-profiles-review/",
                OK);

        RestResult<Void> result = service.completeSpendProfilesReview(projectId);

        setupPostWithRestResultVerifications("/project/1/complete-spend-profiles-review/", Void.class, null);

        assertTrue(result.isSuccess());
    }

    @Test
    public void getSpendProfileCSV() {

        Long projectId = 1L;
        Long organisationId = 1L;

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile-csv", SpendProfileCSVResource.class, null);

        RestResult<SpendProfileCSVResource> result = service.getSpendProfileCSV(projectId, organisationId);

        assertTrue(result.isSuccess());
    }

    @Test
    public void acceptOrRejectSpendProfile() {
        Long projectId = 1L;
        setupPostWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/spend-profile/approval/" + ApprovalType.APPROVED,
                OK);

        RestResult<Void> result = service.acceptOrRejectSpendProfile(projectId, ApprovalType.APPROVED);
        setupPostWithRestResultVerifications("/project/1/spend-profile/approval/APPROVED", Void.class, null);

        assertTrue(result.isSuccess());
    }

    @Test
    public void getSpendProfileStatusByProjectId() {
        Long projectId = 1L;

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/spend-profile/approval",
                ApprovalType.class,
                ApprovalType.APPROVED,
                OK);

        RestResult<ApprovalType> result = service.getSpendProfileStatusByProjectId(projectId);
        assertTrue(result.isSuccess());
        assertEquals(ApprovalType.APPROVED, result.getSuccessObject());
    }

    @Test
    public void testGetProjectFinances() {

        Long projectId = 123L;

        List<ProjectFinanceResource> results = newProjectFinanceResource().build(2);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/project-finances", projectFinanceResourceListType(), results);

        RestResult<List<ProjectFinanceResource>> result = service.getProjectFinances(projectId);

        assertEquals(results, result.getSuccessObject());
    }

    @Test
    public void testGetViability() {

        ViabilityResource viability = new ViabilityResource(Viability.APPROVED, ViabilityStatus.GREEN);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/viability", ViabilityResource.class, viability);

        RestResult<ViabilityResource> results = service.getViability(123L, 456L);

        assertEquals(Viability.APPROVED, results.getSuccessObject().getViability());
        assertEquals(ViabilityStatus.GREEN, results.getSuccessObject().getViabilityStatus());
    }

    @Test
    public void testSaveViability() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/viability/" +
                Viability.APPROVED.name() + "/" + ViabilityStatus.RED.name();

        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveViability(123L, 456L, Viability.APPROVED, ViabilityStatus.RED);

        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testGetEligibility() {

        EligibilityResource eligibility = new EligibilityResource(Eligibility.APPROVED, EligibilityStatus.GREEN);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/eligibility", EligibilityResource.class, eligibility);

        RestResult<EligibilityResource> results = service.getEligibility(123L, 456L);

        assertEquals(Eligibility.APPROVED, results.getSuccessObject().getEligibility());
        assertEquals(EligibilityStatus.GREEN, results.getSuccessObject().getEligibilityStatus());
    }

    @Test
    public void testSaveEligibility() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/eligibility/" +
                Eligibility.APPROVED.name() + "/" + EligibilityStatus.RED.name();

        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveEligibility(123L, 456L, Eligibility.APPROVED, EligibilityStatus.RED);

        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testIsCreditReportConfirmed() {

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/credit-report", Boolean.class, true);
        RestResult<Boolean> results = service.isCreditReportConfirmed(123L, 456L);
        assertTrue(results.getSuccessObject());
    }

    @Test
    public void testSaveCreditReportConfirmed() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/credit-report/true";
        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveCreditReportConfirmed(123L, 456L, true);
        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testGetProjectFinance() {

        Long projectId = 123L;

        Long organisationId = 456L;

        ProjectFinanceResource expectedResult = newProjectFinanceResource().build();

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/organisation/" + organisationId + "/financeDetails", ProjectFinanceResource.class, expectedResult);

        RestResult<ProjectFinanceResource> result = service.getProjectFinance(projectId, organisationId);

        assertEquals(expectedResult, result.getSuccessObject());
    }
}
