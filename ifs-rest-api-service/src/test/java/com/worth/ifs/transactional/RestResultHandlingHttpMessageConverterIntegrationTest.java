package com.worth.ifs.transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationRestService;
import com.worth.ifs.commons.BaseWebIntegrationTest;
import com.worth.ifs.commons.error.CommonFailureKeys;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestErrorResponse;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.SecuritySetter;
import com.worth.ifs.commons.security.UidAuthenticationService;
import com.worth.ifs.commons.service.HttpHeadersUtils;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.UserRestService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.Future;

import static com.worth.ifs.commons.error.CommonFailureKeys.GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION;
import static com.worth.ifs.commons.security.UidAuthenticationService.AUTH_TOKEN;
import static com.worth.ifs.commons.service.HttpHeadersUtils.getJSONHeaders;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

/**
 * Tests for the {@link com.worth.ifs.rest.RestResultHandlingHttpMessageConverter}, to assert that it can take successful
 * RestResults from Controllers and convert them into the "body" of the RestResult, and that it can take failing RestResults
 * and convert them into {@link RestErrorResponse} objects.
 */
public class RestResultHandlingHttpMessageConverterIntegrationTest extends BaseWebIntegrationTest {

    @Value("${ifs.data.service.rest.baseURL}")
    private String dataUrl;

    @Autowired
    public ApplicationRestService applicationRestService;

    @Autowired
    public UserRestService userRestService;

    @Test
    public void testSuccessRestResultHandledAsTheBodyOfTheRestResult() {

        RestTemplate restTemplate = new RestTemplate();

        final long applicationId = 1L;

        try {
            final String url = String.format("%s/application/%s", dataUrl, applicationId);
            ResponseEntity<ApplicationResource> response = restTemplate.exchange(url, GET, leadApplicantHeadersEntity(), ApplicationResource.class);
            assertEquals(OK, response.getStatusCode());
            assertNotNull(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            fail("Should have handled the request and response ok, but got exception - " + e);
        }
    }

    @Test
    public void testFailureRestResultHandledAsARestErrorResponse() throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        try {

            String url = dataUrl + "/application/9999";
            restTemplate.exchange(url, GET, leadApplicantHeadersEntity(), String.class);
            fail("Should have had a Not Found on the server side, as a non-existent id was specified");

        } catch (HttpClientErrorException | HttpServerErrorException e) {

            assertEquals(FORBIDDEN, e.getStatusCode());
            RestErrorResponse restErrorResponse = new ObjectMapper().readValue(e.getResponseBodyAsString(), RestErrorResponse.class);
            Error expectedError = new Error(CommonFailureKeys.GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION.name(), null);
            RestErrorResponse expectedResponse = new RestErrorResponse(expectedError);
            Assert.assertEquals(expectedResponse, restErrorResponse);
        }
    }

    @Test
    public void testFailureRestResultHandledAsync() throws Exception {
        final UserResource initial = SecuritySetter.swapOutForUser(leadApplicantUser());
        try {
            final long applicationIdThatDoesNotExist = -1L;
            final Future<RestResult<Double>> completeQuestionsPercentage = applicationRestService.getCompleteQuestionsPercentage(applicationIdThatDoesNotExist);
            // We have set the future going but now we need to call it. This call should not throw
            final RestResult<Double> doubleRestResult = completeQuestionsPercentage.get();
            assertTrue(doubleRestResult.isFailure());
            Assert.assertEquals(FORBIDDEN, doubleRestResult.getStatusCode());
        }
        finally {
            SecuritySetter.swapOutForUser(initial);
        }
    }


    private <T> HttpEntity<T> leadApplicantHeadersEntity(){
        return getUserJSONHeaders(leadApplicantUser());
    }

    private <T> HttpEntity<T> getUserJSONHeaders(UserResource user) {
        HttpHeaders headers = HttpHeadersUtils.getJSONHeaders();
        headers.set(UidAuthenticationService.AUTH_TOKEN, user.getUid());
        return new HttpEntity<>(headers);
    }

    private UserResource leadApplicantUser(){
        return getUserResourceForSecurity("steve.smith@empire.com");
    }

    private UserResource getUserResourceForSecurity(String email) {
        UserResource user = userRestService.findUserByEmail(email).getSuccessObject();
        UserResource resource = new UserResource();
        resource.setId(user.getId());
        resource.setUid(user.getUid());
        return resource;
    }
}
