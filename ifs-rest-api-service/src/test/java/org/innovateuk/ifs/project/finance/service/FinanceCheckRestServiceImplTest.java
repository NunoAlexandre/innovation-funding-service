package org.innovateuk.ifs.project.finance.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;

import org.innovateuk.ifs.project.finance.resource.FinanceCheckEligibilityResource;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;

public class FinanceCheckRestServiceImplTest extends BaseRestServiceUnitTest<FinanceCheckRestServiceImpl> {

    @Override
    protected FinanceCheckRestServiceImpl registerRestServiceUnderTest() {
        return new FinanceCheckRestServiceImpl();
    }

    @Test
    @Ignore
    public void testApprove() {
        setupPostWithRestResultExpectations("/project/123/partner-organisation/456/finance-check/approve", OK);
        service.approveFinanceCheck(123L, 456L);
        setupPostWithRestResultVerifications("/project/123/partner-organisation/456/finance-check/approve", Void.class, null);
    }

    @Test
    @Ignore
    public void testGetFinanceCheckApprovalStatus() {
        //TODO nuno
        //FinanceCheckState currentState, ProjectUserResource participant, UserResource internalParticipant, LocalDateTime modifiedDate, boolean canApprove
        FinanceCheckProcessResource processStatus = new FinanceCheckProcessResource(null, null, null,null, false);
        setupGetWithRestResultExpectations("/project/123/partner-organisation/456/finance-check/status", FinanceCheckProcessResource.class, processStatus);

        Assert.assertEquals(processStatus, service.getFinanceCheckApprovalStatus(123L, 456L).getSuccessObject());
    }

    @Test
    @Ignore
    public void testGetFinanceCheckEligibility() {
        FinanceCheckEligibilityResource processStatus = new FinanceCheckEligibilityResource();
        setupGetWithRestResultExpectations("/project/123/partner-organisation/456/finance-check/eligibility", FinanceCheckEligibilityResource.class, processStatus);

        Assert.assertEquals(processStatus, service.getFinanceCheckEligibility(123L, 456L).getSuccessObject());
    }
}
