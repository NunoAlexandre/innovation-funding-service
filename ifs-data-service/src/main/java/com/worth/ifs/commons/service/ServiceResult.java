package com.worth.ifs.commons.service;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.error.ErrorTemplate;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.util.Either;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.Errors.internalServerErrorError;
import static com.worth.ifs.commons.rest.RestResult.restFailure;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.util.Either.left;
import static com.worth.ifs.util.Either.right;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

/**
 * Represents the result of an action, that will be either a failure or a success.  A failure will result in a ServiceFailure, and a
 * success will result in a T.  Additionally, these can be mapped to produce new ServiceResults that either fail or succeed.
 */
public class ServiceResult<T> extends BaseEitherBackedResult<T, ServiceFailure> {

    private static final Log LOG = LogFactory.getLog(ServiceResult.class);

    private ServiceResult(Either<ServiceFailure, T> result) {
        super(result);
    }

    @Override
    public <R> ServiceResult<R> andOnSuccess(ExceptionThrowingFunction<? super T, FailingOrSucceedingResult<R, ServiceFailure>> successHandler) {
        return (ServiceResult<R>) super.andOnSuccess(successHandler);
    }

    public ServiceResult<Void> andOnSuccessReturnVoid() {
        return andOnSuccess(success -> serviceSuccess());
    }

    public ServiceResult<Void> andOnSuccessReturnVoid(Consumer<? super T> successHandler) {
        return andOnSuccess(success -> {
            successHandler.accept(success);
            return serviceSuccess();
        });
    }

    @Override
    public <R> ServiceResult<R> andOnSuccessReturn(ExceptionThrowingFunction<? super T, R> successHandler) {
        return (ServiceResult<R>) super.andOnSuccessReturn(successHandler);
    }

    @Override
    protected <R> BaseEitherBackedResult<R, ServiceFailure> createSuccess(FailingOrSucceedingResult<R, ServiceFailure> success) {
        return serviceSuccess(success.getSuccessObject());
    }

    @Override
    protected <R> BaseEitherBackedResult<R, ServiceFailure> createSuccess(R success) {
        return serviceSuccess(success);
    }

    @Override
    protected <R> BaseEitherBackedResult<R, ServiceFailure> createFailure(FailingOrSucceedingResult<R, ServiceFailure> failure) {
        return failure != null ? serviceFailure(failure.getFailure()) : serviceFailure(internalServerErrorError("Unexpected error"));
    }

    private RestResult<T> toRestResult() {
        return toRestResult(OK);
    }

    private RestResult<Void> toEmptyRestResult() {
        return toEmptyRestResult(OK);
    }

    private RestResult<T> toRestResult(HttpStatus statusCode) {
        return handleSuccessOrFailure(
                failure -> handleServiceFailure(failure),
                success -> restSuccess(success, statusCode)
        );
    }

    private RestResult<Void> toEmptyRestResult(HttpStatus statusCode) {
        return handleSuccessOrFailure(
                failure -> handleServiceFailure(failure),
                success -> restSuccess(statusCode)
        );
    }

    private RestResult<Void> toRestResultNoContent() {
        return toEmptyRestResult(NO_CONTENT);
    }

    public RestResult<T> toGetResponse() {
        return toRestResult();
    }

    public RestResult<T> toPostCreateResponse() {
        return toRestResult(CREATED);
    }

    /**
     * @deprecated should use POSTs for create, and PUTs for update
     */
    public RestResult<Void> toPostUpdateResponse() {
        return toEmptyRestResult();
    }

    public RestResult<Void> toPutResponse() {
        return toEmptyRestResult();
    }

    /**
     * @deprecated PUTs shouldn't generally return results - an HTTP status is generally ok
     */
    public RestResult<T> toPutWithBodyResponse() {
        return toRestResult();
    }

    public RestResult<Void> toDeleteResponse() {
        return toRestResultNoContent();
    }

    private <R> RestResult<R> handleServiceFailure(ServiceFailure failure) {
        return restFailure(failure.getErrors());
    }

    public static ServiceResult<Void> serviceSuccess() {
        return new ServiceResult<>(right(null));
    }

    public static <T> ServiceResult<T> serviceSuccess(T successfulResult) {
        return new ServiceResult<>(right(successfulResult));
    }

    public static <T> ServiceResult<T> serviceFailure(ServiceFailure failure) {
        return new ServiceResult<>(left(failure));
    }

    public static <T> ServiceResult<T> serviceFailure(Error error) {
        return new ServiceResult<>(left(new ServiceFailure(singletonList(error))));
    }

    public static <T> ServiceResult<T> getNonNullValue(T value, Error error) {

        if (value == null) {
            return serviceFailure(error);
        }

        return serviceSuccess(value);
    }

    public static <T, R> ServiceResult<T> processAnyFailuresOrSucceed(List<ServiceResult<R>> results, ServiceResult<T> failureResponse, ServiceResult<T> successResponse) {
        return results.stream().anyMatch(ServiceResult::isFailure) ? failureResponse : successResponse;
    }

    /**
     * This wrapper wraps the serviceCode function and rolls back transactions upon receiving a ServiceFailure
     * response (an Either with a left of ServiceFailure).
     *
     * It will also catch all exceptions thrown from within serviceCode and convert them into ServiceFailures of
     * type GENERAL_UNEXPECTED_ERROR.
     *
     * @param serviceCode - code that performs some process and returns a successful or failing ServiceResult, the state
     *                      of which then allows this wrapper to perform additional actions
     *
     * @param <T> - the successful return type of the ServiceResult
     * @return the original ServiceResult returned from the serviceCode, or a generic ServiceResult failure if an exception
     * was thrown in serviceCode
     */
    public static <T> ServiceResult<T> handlingErrors(Supplier<ServiceResult<T>> serviceCode) {
        return handlingErrors(internalServerErrorError(), serviceCode);
    }

    /**
     * This wrapper wraps the serviceCode function and rolls back transactions upon receiving a ServiceFailure
     * response (an Either with a left of ServiceFailure).
     *
     * It will also catch all exceptions thrown from within serviceCode and convert them into ServiceFailures of
     * type GENERAL_UNEXPECTED_ERROR.
     *
     * @param serviceCode - code that performs some process and returns a successful or failing ServiceResult, the state
     *                      of which then allows this wrapper to perform additional actions
     *
     * @param <T> - the successful return type of the ServiceResult
     * @return the original ServiceResult returned from the serviceCode, or a generic ServiceResult failure if an exception
     * was thrown in serviceCode
     */
    public static <T> ServiceResult<T> handlingErrors(ErrorTemplate catchAllErrorTemplate, Supplier<ServiceResult<T>> serviceCode) {
        return handlingErrors(new Error(catchAllErrorTemplate), serviceCode);
    }

        /**
         * This wrapper wraps the serviceCode function and rolls back transactions upon receiving a ServiceFailure
         * response (an Either with a left of ServiceFailure).
         *
         * It will also catch all exceptions thrown from within serviceCode and convert them into ServiceFailures of
         * type GENERAL_UNEXPECTED_ERROR.
         *
         * @param <T> - the successful return type of the ServiceResult
         * @param serviceCode - code that performs some process and returns a successful or failing ServiceResult, the state
         *                      of which then allows this wrapper to perform additional actions
         * @return the original ServiceResult returned from the serviceCode, or a generic ServiceResult failure if an exception
         * was thrown in serviceCode
         */
    public static <T> ServiceResult<T> handlingErrors(Error catchAllError, Supplier<ServiceResult<T>> serviceCode) {
        try {
            return serviceCode.get();
        } catch (Exception e) {
            LOG.warn("Uncaught exception encountered while performing service call.  Returning ServiceFailure", e);
            return serviceFailure(catchAllError);
        }
    }
}
