package org.innovateuk.ifs.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.commons.rest.RestResult;

public class ApplicationSummaryRestServiceMocksTest extends BaseRestServiceUnitTest<ApplicationSummaryRestServiceImpl> {

    private static final String APPLICATION_SUMMARY_REST_URL = "/appsummary";

    @Override
    protected ApplicationSummaryRestServiceImpl registerRestServiceUnderTest() {
    	ApplicationSummaryRestServiceImpl applicationSummaryRestService = new ApplicationSummaryRestServiceImpl();
    	applicationSummaryRestService.setApplicationSummaryRestUrl(APPLICATION_SUMMARY_REST_URL);
    	return applicationSummaryRestService;
    }
    
    @Test
    public void testFindByCompetitionWithoutSortFieldOrPageOrPageSize() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(123L, null, null, null, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }

    @Test
    public void testFindByCompetitionWithoutPage() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?size=20&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(Long.valueOf(123L), "id", null, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindByCompetitionWithoutPageSize() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?page=6&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(Long.valueOf(123L), "id", 6, null, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindByCompetitionWithoutSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?page=6&size=20", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(Long.valueOf(123L), null, 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindByCompetitionWithSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?page=6&size=20&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(Long.valueOf(123L), "id", 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }

    @Test
    public void testFindByCompetitionWithFilter() {
        ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?page=6&size=20&filter=10", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(123L, null, 6, 20, "10");

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    @Test
    public void testFindByCompetitionWithFilterAndSort() {
        ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123?page=6&size=20&sort=id&filter=10", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getAllApplications(123L, "id", 6, 20, "10");

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindSubmittedApplicationsByCompetitionWithoutSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/submitted?page=6&size=20", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getSubmittedApplications(Long.valueOf(123L), null, 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindSubmittedApplicationsByCompetitionWithSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/submitted?page=6&size=20&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getSubmittedApplications(Long.valueOf(123L), "id", 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }

    @Test
    public void testFindSubmittedApplicationsByCompetitionWithFilter() {
        ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/submitted?page=6&size=20&filter=10", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getSubmittedApplications(Long.valueOf(123L), null, 6, 20, "10");

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }

    @Test
    public void testFindSubmittedApplicationsByCompetitionWithFilterAndSortField() {
        ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/submitted?page=6&size=20&sort=id&filter=10", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getSubmittedApplications(Long.valueOf(123L), "id", 6, 20, "10");

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }

    @Test
    public void testFindNotSubmittedApplicationsByCompetitionWithoutSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/not-submitted?page=6&size=20", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getNonSubmittedApplications(Long.valueOf(123L), null, 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindNotSubmittedApplicationsByCompetitionWithSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/not-submitted?page=6&size=20&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getNonSubmittedApplications(Long.valueOf(123L), "id", 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindFeedbackRequiredpplicationsByCompetitionWithoutSortField() {
    	ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/feedback-required?page=6&size=20", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getFeedbackRequiredApplications(Long.valueOf(123L), null, 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
    
    @Test
    public void testFindFeedbackRequiredApplicationsByCompetitionWithSortField() {
        ApplicationSummaryPageResource responseBody = new ApplicationSummaryPageResource();
        setupGetWithRestResultExpectations(APPLICATION_SUMMARY_REST_URL + "/findByCompetition/123/feedback-required?page=6&size=20&sort=id", ApplicationSummaryPageResource.class, responseBody);

        RestResult<ApplicationSummaryPageResource> result = service.getFeedbackRequiredApplications(Long.valueOf(123L), "id", 6, 20, null);

        assertTrue(result.isSuccess());
        Assert.assertEquals(responseBody, result.getSuccessObject());
    }
}
