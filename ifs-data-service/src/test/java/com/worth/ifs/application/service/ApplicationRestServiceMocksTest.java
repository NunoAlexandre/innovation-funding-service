package com.worth.ifs.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.BaseRestServiceMocksTest;
import com.worth.ifs.application.domain.Application;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.user.domain.UserRoleType.APPLICANT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

/**
 * Tests to check the ApplicationRestService's interaction with the RestTemplate and the processing of its results
 */
public class ApplicationRestServiceMocksTest extends BaseRestServiceMocksTest<ApplicationRestServiceImpl> {

    private static final String applicationRestURL = "/applications";

    @Override
    protected ApplicationRestServiceImpl registerRestServiceUnderTest() {
        ApplicationRestServiceImpl applicationRestService = new ApplicationRestServiceImpl();
        applicationRestService.applicationRestURL = applicationRestURL;
        return applicationRestService;
    }

    @Test
    public void test_getApplicationById() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/id/" + 123;
        ResponseEntity<Application> response = new ResponseEntity<>(newApplication().build(), OK);
        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), Application.class)).thenReturn(response);

        // now run the method under test
        Application application = service.getApplicationById(123L);
        assertNotNull(application);
        assertTrue(application == response.getBody());
    }

    @Test
    public void test_getApplicationsByCompetitionIdAndUserId() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/getApplicationsByCompetitionIdAndUserId/123/456/APPLICANT";
        Application[] returnedApplications = newApplication().buildArray(3, Application.class);
        ResponseEntity<Application[]> response = new ResponseEntity<>(returnedApplications, OK);
        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), Application[].class)).thenReturn(response);

        // now run the method under test
        List<Application> applications = service.getApplicationsByCompetitionIdAndUserId(123L, 456L, APPLICANT);
        assertNotNull(applications);
        assertEquals(3, applications.size());
        assertEquals(returnedApplications[0], applications.get(0));
        assertEquals(returnedApplications[1], applications.get(1));
        assertEquals(returnedApplications[2], applications.get(2));
    }

    @Test
    public void test_getApplicationsByUserId() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/findByUser/123";
        Application[] returnedApplications = newApplication().buildArray(3, Application.class);
        ResponseEntity<Application[]> response = new ResponseEntity<>(returnedApplications, OK);
        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), Application[].class)).thenReturn(response);

        // now run the method under test
        List<Application> applications = service.getApplicationsByUserId(123L);

        assertNotNull(applications);
        assertEquals(3, applications.size());
        assertEquals(returnedApplications[0], applications.get(0));
        assertEquals(returnedApplications[1], applications.get(1));
        assertEquals(returnedApplications[2], applications.get(2));
    }

    @Test
    public void test_getCompleteQuestionsPercentage() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/getProgressPercentageByApplicationId/123";
        ObjectNode returnedDetails = new ObjectMapper().createObjectNode().put("completedPercentage", "60.5");

        ResponseEntity<ObjectNode> response = new ResponseEntity<>(returnedDetails, OK);
        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), ObjectNode.class)).thenReturn(response);

        // now run the method under test
        Double percentage = service.getCompleteQuestionsPercentage(123L);

        assertNotNull(percentage);
        assertEquals(Double.valueOf(60.5), percentage);
    }

    @Test
    public void test_saveApplication() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/saveApplicationDetails/123";
        Application applicationToUpdate = newApplication().withId(123L).build();

        ResponseEntity<String> response = new ResponseEntity<>("", OK);
        when(mockRestTemplate.postForEntity(expectedUrl, httpEntityForRestCall(applicationToUpdate), String.class)).thenReturn(response);

        // now run the method under test
        service.saveApplication(applicationToUpdate);
    }

    @Test
    public void test_updateApplicationStatus() {

        String expectedUrl = dataServicesUrl + applicationRestURL + "/updateApplicationStatus?applicationId=123&statusId=456";

        ResponseEntity<String> response = new ResponseEntity<>("", OK);
        when(mockRestTemplate.exchange(expectedUrl, GET, httpEntityForRestCall(), String.class)).thenReturn(response);

        // now run the method under test
        service.updateApplicationStatus(123L, 456L);
    }
}
