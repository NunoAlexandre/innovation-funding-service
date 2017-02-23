package org.innovateuk.ifs.finance.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.applicationFinanceResourceListType;


import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpStatus.OK;

public class ApplicationFinanceRestServiceMocksTest extends BaseRestServiceUnitTest<ApplicationFinanceRestServiceImpl> {
    private static final String applicationFinanceRestURL = "/applicationfinance";

    @Override
    protected ApplicationFinanceRestServiceImpl registerRestServiceUnderTest() {
        ApplicationFinanceRestServiceImpl financeService = new ApplicationFinanceRestServiceImpl();
        return financeService;
    }

    @Test
    public void test_getApplicationFinance_forApplicationIdAndOrganisationId() {

        ApplicationFinanceResource returnedResponse = new ApplicationFinanceResource();

        setupGetWithRestResultExpectations(applicationFinanceRestURL + "/findByApplicationOrganisation/123/456", ApplicationFinanceResource.class, returnedResponse);

        ApplicationFinanceResource finance = service.getApplicationFinance(123L, 456L).getSuccessObject();
        Assert.assertEquals(returnedResponse, finance);
    }

    @Test
    public void test_getApplicationFinance_forApplicationIdAndOrganisationId_nullSafe() {
        assertNull(service.getApplicationFinance(123L, null));
        assertNull(service.getApplicationFinance(null, 456L));
        assertNull(service.getApplicationFinance(null, null));
    }

    @Test
    public void test_getApplicationFinances_forApplicationId() {
        List<ApplicationFinanceResource> returnedResponse = Arrays.asList(1,2,3).stream().map(i -> new ApplicationFinanceResource()).collect(Collectors.toList());//.build(3);
        setupGetWithRestResultExpectations(applicationFinanceRestURL + "/findByApplication/123", applicationFinanceResourceListType(), returnedResponse);
        List<ApplicationFinanceResource> finances = service.getApplicationFinances(123L).getSuccessObject();
        assertEquals(returnedResponse, finances);
    }

    @Test
    public void test_getApplicationFinances_forApplicationId_nullSafe() {
        assertNull(service.getApplicationFinances(null));
    }

    @Test
    public void test_addApplicationFinance_forApplicationIdAndOrganisationId() {
        ApplicationFinanceResource returnedResponse = new ApplicationFinanceResource();

        setupPostWithRestResultExpectations(applicationFinanceRestURL + "/add/123/456", ApplicationFinanceResource.class, null, returnedResponse, OK);

        ApplicationFinanceResource finance = service.addApplicationFinanceForOrganisation(123L, 456L).getSuccessObject();
        Assert.assertEquals(returnedResponse, finance);
    }

    @Test
    public void test_getFileDetails() {

        FileEntryResource returnedResponse = new FileEntryResource();

        setupGetWithRestResultExpectations(applicationFinanceRestURL + "/financeDocument/fileentry?applicationFinanceId=123", FileEntryResource.class, returnedResponse);

        FileEntryResource fileDetails = service.getFileDetails(123L).getSuccessObject();
        Assert.assertEquals(returnedResponse, fileDetails);
    }

    @Test
    public void test_addApplicationFinance_nullSafe() {
        assertNull(service.addApplicationFinanceForOrganisation(123L, null));
        assertNull(service.addApplicationFinanceForOrganisation(null, 456L));
        assertNull(service.addApplicationFinanceForOrganisation(null, null));
    }

    @Test
    public void test_getApplicationFinanceDetails_forApplicationId() {
        List<ApplicationFinanceResource> returnedResponse = newApplicationFinanceResource().withApplication(1L).build(3);
        setupGetWithRestResultExpectations(applicationFinanceRestURL + "/financeDetails/123", applicationFinanceResourceListType(), returnedResponse);
        List<ApplicationFinanceResource> finances = service.getFinanceDetails(123L).getSuccessObject();
        assertEquals(returnedResponse, finances);
    }
}
