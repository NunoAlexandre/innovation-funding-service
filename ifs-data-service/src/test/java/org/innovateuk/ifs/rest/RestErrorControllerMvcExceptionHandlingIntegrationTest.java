package org.innovateuk.ifs.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.innovateuk.ifs.commons.BaseWebIntegrationTest;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestErrorResponse;
import org.innovateuk.ifs.commons.security.authentication.token.Authentication;
import org.innovateuk.ifs.commons.service.HttpHeadersUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_FORBIDDEN;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_NOT_FOUND;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * This test tests that the {RestErrorController} is able to take low-level errors produced by Spring MVC and Spring Security
 * prior to any Controller code actually being called, and convert them into RestErrorResponses.
 */
public class RestErrorControllerMvcExceptionHandlingIntegrationTest extends BaseWebIntegrationTest {


    @Test
    public void testIncorrectUrl() throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "http://localhost:"+ port + "/non/existent/url";
            restTemplate.exchange(url, GET, headersEntity(), String.class);
            fail("Should have had a Not Found on the server side, as a non-handled URL was specified");

        } catch (HttpClientErrorException | HttpServerErrorException e) {

            assertEquals(NOT_FOUND, e.getStatusCode());
            RestErrorResponse restErrorResponse = new ObjectMapper().readValue(e.getResponseBodyAsString(), RestErrorResponse.class);
            Error expectedError = new Error(GENERAL_NOT_FOUND.getErrorKey(), null);
            RestErrorResponse expectedResponse = new RestErrorResponse(expectedError);
            Assert.assertEquals(expectedResponse, restErrorResponse);
        }
    }

    @Test
    public void testAccessDenied() throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        try {
            String url  = "http://localhost:"+port + "/application/2";
            restTemplate.exchange(url, GET, new HttpEntity<>(new HttpHeaders()), String.class);
            fail("Should have had a Forbidden on the server side, as we are not specifying a user authentication to this restricted resource");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            assertEquals(FORBIDDEN, e.getStatusCode());
            RestErrorResponse restErrorResponse = new ObjectMapper().readValue(e.getResponseBodyAsString(), RestErrorResponse.class);
            Error expectedError = new Error(GENERAL_FORBIDDEN.getErrorKey(), null);
            RestErrorResponse expectedResponse = new RestErrorResponse(expectedError);
            Assert.assertEquals(expectedResponse, restErrorResponse);
        }
    }

    private <T> HttpEntity<T> headersEntity(){
        HttpHeaders headers = HttpHeadersUtils.getJSONHeaders();
        headers.set(Authentication.TOKEN, "847ac08d-5486-3f3a-9e15-06303fb01ffb");
        return new HttpEntity<>(headers);
    }

    //steve "6b50cb4f-7222-33a5-99c5-8c068cd0b03c"
}
