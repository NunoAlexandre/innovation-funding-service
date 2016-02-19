package com.worth.ifs.commons.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.commons.error.CommonFailureKeys;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.BaseEitherBackedResult;
import com.worth.ifs.commons.service.ExceptionThrowingFunction;
import com.worth.ifs.commons.service.FailingOrSucceedingResult;
import com.worth.ifs.util.Either;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.List;

import static com.worth.ifs.commons.error.CommonErrors.internalServerErrorError;
import static com.worth.ifs.util.CollectionFunctions.combineLists;
import static com.worth.ifs.util.Either.left;
import static com.worth.ifs.util.Either.right;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

/**
 * Represents the result of a Rest Controller action, that will be either a failure or a success.  A failure will result in a RestFailure, and a
 * success will result in a T.  Additionally, these can be mapped to produce new RestResults that either fail or succeed.
 */
public class RestResult<T> extends BaseEitherBackedResult<T, RestFailure> {

    private HttpStatus successfulStatusCode;

    public RestResult(RestResult<T> original) {
        super(original);
        this.successfulStatusCode = original.successfulStatusCode;
    }

    public RestResult(Either<RestFailure, T> result, HttpStatus successfulStatusCode) {
        super(result);
        this.successfulStatusCode = successfulStatusCode;
    }

    @Override
    public <R> RestResult<R> andOnSuccess(ExceptionThrowingFunction<? super T, FailingOrSucceedingResult<R, RestFailure>> successHandler) {
        return (RestResult<R>) super.andOnSuccess(successHandler);
    }

    @Override
    public <R> RestResult<R> andOnSuccessReturn(ExceptionThrowingFunction<? super T, R> successHandler) {
        return (RestResult<R>) super.andOnSuccessReturn(successHandler);
    }

    /**
     *
     * @param restFailure - Failure object with details about the failure
     * @return Always returns null
     */
    @Override
    public T findAndThrowException(RestFailure restFailure) {
        if(restFailure.is(CommonFailureKeys.GENERAL_FORBIDDEN)){
            throw new AccessDeniedException(restFailure.getStatusCode().getReasonPhrase());
        }

        if(restFailure.getStatusCode() == HttpStatus.NOT_FOUND){
            throw new ResourceNotFoundException();
        }

        if(restFailure.getStatusCode() == HttpStatus.BAD_REQUEST){
            throw new IllegalArgumentException();
        }

        return null;
    }

    @Override
    protected <R> RestResult<R> createSuccess(FailingOrSucceedingResult<R, RestFailure> success) {

        if (success instanceof RestResult) {
            return new RestResult<>((RestResult<R>) success);
        }

        return restSuccess(success.getSuccessObject());
    }

    @Override
    protected <R> RestResult<R> createSuccess(R success) {
        return restSuccess(success);
    }

    @Override
    protected <R> RestResult<R> createFailure(FailingOrSucceedingResult<R, RestFailure> failure) {

        if (failure instanceof RestResult) {
            return new RestResult<>((RestResult<R>) failure);
        }

        return (RestResult<R>) restFailure(INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getStatusCode() {
        return isFailure() ? result.getLeft().getStatusCode() : successfulStatusCode;
    }

    public static <T1> T1 getLeftOrRight(Either<T1, T1> either) {
        return Either.getLeftOrRight(either);
    }

    public static RestResult<Void> restSuccess() {
        return restSuccess(OK);
    }

    public static RestResult<Void> restSuccess(HttpStatus statusCode) {
        return restSuccess(null, statusCode);
    }

    public static <T> RestResult<T> restSuccess(T successfulResult) {
        return restSuccess(successfulResult, OK);
    }

    public static <T> RestResult<T> restSuccess(T result, HttpStatus statusCode) {
        return new RestResult<>(right(result), statusCode);
    }

    public static <T> RestResult<T> restFailure(RestFailure failure) {
        return new RestResult<>(left(failure), null);
    }

    public static RestResult<Void> restFailure(HttpStatus statusCode) {
        return restFailure(null, statusCode);
    }

    public static <T> RestResult<T> restFailure(Error error) {
        return restFailure(singletonList(error));
    }

    public static <T> RestResult<T> restFailure(List<Error> errors) {
        return new RestResult<>(left(RestFailure.error(errors)), null);
    }

    public static <T> RestResult<T> restFailure(List<Error> errors, HttpStatus statusCode) {
        return new RestResult<>(left(RestFailure.error(errors, statusCode)), null);
    }

    public static <T> Either<Void, T> fromJson(String json, Class<T> clazz) {
        if (Void.class.equals(clazz)) {
            return right(null);
        }
        if (String.class.equals(clazz)) {
            return Either.<Void, T>right((T) json);
        }
        try {
            return right(new ObjectMapper().readValue(json, clazz));
        } catch (IOException e) {
            return left();
        }
    }

    public static <T> RestResult<T> fromException(HttpStatusCodeException e) {
        return fromJson(e.getResponseBodyAsString(), RestErrorResponse.class).mapLeftOrRight(
                failure -> RestResult.<T>restFailure(internalServerErrorError("Unable to process JSON response as type " + RestErrorResponse.class.getSimpleName())),
                success -> RestResult.<T>restFailure(success.getErrors(), e.getStatusCode())
        );
    }

    public static <T> RestResult<T> fromResponse(final ResponseEntity<T> response, HttpStatus expectedSuccessCode, HttpStatus... otherExpectedStatusCodes) {
        List<HttpStatus> allExpectedSuccessStatusCodes = combineLists(asList(otherExpectedStatusCodes), expectedSuccessCode);
        if (allExpectedSuccessStatusCodes.contains(response.getStatusCode())) {
            return RestResult.<T>restSuccess(response.getBody(), response.getStatusCode());
        } else {
            return RestResult.<T>restFailure(new com.worth.ifs.commons.error.Error(INTERNAL_SERVER_ERROR, "Unexpected status code " + response.getStatusCode(), INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a GET request that is requesting
     * data.
     *
     * This will be a RestResult containing the body of the ServiceResult and a "200 - OK" response.
     */
    public static <T> RestResult<T> toGetResponse(T body) {
        return restSuccess(body, OK);
    }

    /**
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a POST request that is
     * creating data.
     *
     * This will be a RestResult containing the body of the ServiceResult and a "201 - Created" response.
     *
     * This is an appropriate response for a POST that is creating data.  To update data, consider using a PUT.
     */
    public static <T> RestResult<T> toPostCreateResponse(T body) {
        return restSuccess(body, CREATED);
    }

    /**
     * @deprecated should use POSTs to create new data, and PUTs to update data.
     *
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a POST request that is
     * updating data (although PUTs should really be used).
     *
     * This will be a bodiless RestResult with a "200 - OK" response.
     */
    public static <T> RestResult<Void> toPostUpdateResponse() {
        return restSuccess();
    }

    /**
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a PUT request that is
     * updating data.
     *
     * This will be a bodiless RestResult with a "200 - OK" response.
     */
    public static RestResult<Void> toPutResponse() {
        return restSuccess();
    }

    /**
     * @deprecated PUTs shouldn't generally return results in their bodies
     *
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a PUT request that is
     * updating data.
     *
     * This will be a RestResult containing the body of the ServiceResult with a "200 - OK" response, although ideally
     * PUT responses shouldn't need to inculde bodies.
     */
    public static <T> RestResult<T> toPutWithBodyResponse(T body) {
        return restSuccess(body, OK);
    }

    /**
     * Convenience method to convert a ServiceResult into an appropriate RestResult for a DELETE request that is
     * deleting data.
     *
     * This will be a bodiless RestResult with a "204 - No content" response.
     */
    public static RestResult<Void> toDeleteResponse() {
        return restSuccess(NO_CONTENT);
    }

}
