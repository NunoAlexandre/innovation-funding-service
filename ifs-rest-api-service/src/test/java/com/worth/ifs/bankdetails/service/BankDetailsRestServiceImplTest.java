package com.worth.ifs.bankdetails.service;

import com.worth.ifs.BaseRestServiceUnitTest;
import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.bankdetails.resource.ProjectBankDetailsStatusSummary;
import com.worth.ifs.commons.rest.RestResult;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static com.worth.ifs.bankdetails.builder.ProjectBankDetailsStatusSummaryBuilder.newProjectBankDetailsStatusSummary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class BankDetailsRestServiceImplTest extends BaseRestServiceUnitTest<BankDetailsRestServiceImpl> {
    private final String projectRestURL = "/project";
    @Override
    protected BankDetailsRestServiceImpl registerRestServiceUnderTest() {
        BankDetailsRestServiceImpl bankDetailsRestService = new BankDetailsRestServiceImpl();
        ReflectionTestUtils.setField(bankDetailsRestService, "projectRestURL", projectRestURL);
        return bankDetailsRestService;
    }

    @Test
    public void testGetById(){
        Long projectId = 123L;
        Long bankDetailsId = 1L;
        BankDetailsResource returnedResponse = newBankDetailsResource().build();
        setupGetWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details?bankDetailsId=" + bankDetailsId, BankDetailsResource.class, returnedResponse);
        BankDetailsResource response = service.getByProjectIdAndBankDetailsId(projectId, bankDetailsId).getSuccessObject();
        assertEquals(response, returnedResponse);
    }

    @Test
    public void testGetBankDetailsByProjectAndOrganisation(){
        Long projectId = 123L;
        Long organisationId = 100L;
        BankDetailsResource returnedResponse = newBankDetailsResource().build();
        setupGetWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details?organisationId=" + organisationId, BankDetailsResource.class, returnedResponse);
        BankDetailsResource response = service.getBankDetailsByProjectAndOrganisation(projectId, organisationId).getSuccessObject();
        assertEquals(response, returnedResponse);
    }

    @Test
    public void testGetBankDetailsByProjectAndOrganisationReturnsNotFoundWhenBankDetailsDontExist(){
        Long projectId = 123L;
        Long organisationId = 100L;
        setupGetWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details?organisationId=" + organisationId, BankDetailsResource.class, null, NOT_FOUND);
        RestResult<BankDetailsResource> response = service.getBankDetailsByProjectAndOrganisation(projectId, organisationId);
        assertTrue(response.isFailure());
        assertEquals(response.getFailure().getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void testUpdateBankDetails(){
        Long projectId = 123L;
        BankDetailsResource bankDetailsResource = newBankDetailsResource().build();
        setupPostWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details", bankDetailsResource, OK);
        RestResult result = service.updateBankDetails(projectId, bankDetailsResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testSubmitBankDetails(){
        Long projectId = 123L;
        BankDetailsResource bankDetailsResource = newBankDetailsResource().build();
        setupPutWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details", bankDetailsResource, OK);
        RestResult result = service.submitBankDetails(projectId, bankDetailsResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testgetBankDetailsByProject(){
        Long projectId = 123L;
        ProjectBankDetailsStatusSummary projectBankDetailsStatusSummary = newProjectBankDetailsStatusSummary().build();
        setupGetWithRestResultExpectations(projectRestURL + "/" + projectId + "/bank-details/status-summary", ProjectBankDetailsStatusSummary.class, projectBankDetailsStatusSummary, OK);
        RestResult<ProjectBankDetailsStatusSummary> response = service.getBankDetailsStatusSummaryByProject(projectId);
        assertTrue(response.isSuccess());
        assertEquals(projectBankDetailsStatusSummary, response.getSuccessObject());
    }
}
