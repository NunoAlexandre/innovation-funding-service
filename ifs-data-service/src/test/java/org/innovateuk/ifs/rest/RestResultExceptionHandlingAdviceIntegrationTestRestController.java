package org.innovateuk.ifs.rest;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;

/**
 * A test Controller for tests in {@link RestResultExceptionHandlingAdviceIntegrationTest}
 */
@RestController
public class RestResultExceptionHandlingAdviceIntegrationTestRestController {

    @RequestMapping("/success-test")
    public RestResult<String> successfulMethod() {
        return RestResult.restSuccess("Success");
    }

    @RequestMapping("/failure-test")
    public RestResult<String> failingMethod() {
        return RestResult.restFailure(internalServerErrorError());
    }

    @RequestMapping("/null-test")
    public RestResult<String> nullReturningMethod() {
        return null;
    }

    @RequestMapping("/exception-test")
    public RestResult<String> exceptionThrowingMethod() {
        throw new RuntimeException("Exception");
    }

    @RequestMapping("/package-private-exception-test")
    RestResult<String> packagePrivateMethod() {
        return null;
    }

    public RestResult<String> nonRequestMappingAnnotatedMethod() {
        return null;
    }

    @RequestMapping("/non-rest-result-returning-method")
    public String nonRestResultReturningMethod() {
        return null;
    }
}
