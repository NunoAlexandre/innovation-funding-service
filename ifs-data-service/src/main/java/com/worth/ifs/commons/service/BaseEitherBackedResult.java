package com.worth.ifs.commons.service;

import com.worth.ifs.util.Either;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents the result of an action, that will be either a failure or a success.  A failure will result in a FailureType, and a
 * success will result in a T.  Additionally, these can be mapped to produce new ServiceResults that either fail or succeed.
 */
public abstract class BaseEitherBackedResult<T, FailureType> implements FailingOrSucceedingResult<T, FailureType> {

    private static final Log LOG = LogFactory.getLog(BaseEitherBackedResult.class);

    protected Either<FailureType, T> result;

    protected BaseEitherBackedResult(BaseEitherBackedResult<T, FailureType> original) {
        this.result = original.result;
    }

    protected BaseEitherBackedResult(Either<FailureType, T> result) {
        this.result = result;
    }

    @Override
    public <T1> T1 handleSuccessOrFailure(ExceptionThrowingFunction<? super FailureType, ? extends T1> failureHandler, ExceptionThrowingFunction<? super T, ? extends T1> successHandler) {
        return mapLeftOrRight(failureHandler, successHandler);
    }

    @Override
    public <R> BaseEitherBackedResult<R, FailureType> andOnSuccess(ExceptionThrowingFunction<? super T, FailingOrSucceedingResult<R, FailureType>> successHandler) {
        return map(successHandler);
    }

    @Override
    public <R> BaseEitherBackedResult<R, FailureType> andOnSuccessReturn(ExceptionThrowingFunction<? super T, R> successHandler) {
        return flatMap(successHandler);
    }

    @Override
    public boolean isSuccess() {
        return isRight();
    }

    @Override
    public boolean isFailure() {
        return isLeft();
    }

    @Override
    public FailureType getFailure() {
        return getLeft();
    }

    @Override
    public T getSuccessObject() {
        return getRight();
    }

    // TODO DW - INFUND-1555 - remove "BACKWARDS COMPATIBILITY" method here (for "not found" nulls)

    /**
     * @deprecated Should handled either success or failure case explicitly, usually by using handlesuccessorfailure()
     */
    @Deprecated
    public T getSuccessObjectOrNull() {
        return isRight() ? getRight() : null;
    }

    public T getSuccessObjectOrThrowException() {
        return isRight() ? getRight() :  findAndThrowException(getLeft());
    }

    public abstract T findAndThrowException(FailureType failureType);

    protected <T1> T1 mapLeftOrRight(ExceptionThrowingFunction<? super FailureType, ? extends T1> lFunc, ExceptionThrowingFunction<? super T, ? extends T1> rFunc) {
        return result.mapLeftOrRight(lFunc, rFunc);
    }

    protected <R> BaseEitherBackedResult<R, FailureType> map(ExceptionThrowingFunction<? super T, FailingOrSucceedingResult<R, FailureType>> rFunc) {

        if (result.isLeft()) {
            return createFailure((FailingOrSucceedingResult<R, FailureType>) this);
        }

        try {
            FailingOrSucceedingResult<R, FailureType> successResult = rFunc.apply(result.getRight());
            return successResult.isFailure() ? createFailure(successResult) : createSuccess(successResult);
        } catch (Exception e) {
            LOG.warn("Exception caught while processing success function - throwing as a runtime exception", e);
            throw new RuntimeException(e);
        }
    }

    protected <R> BaseEitherBackedResult<R, FailureType> flatMap(ExceptionThrowingFunction<? super T, R> rFunc) {

        if (result.isLeft()) {
            return createFailure((FailingOrSucceedingResult<R, FailureType>) this);
        }

        try {
            R successResult = rFunc.apply(result.getRight());
            return createSuccess(successResult);
        } catch (Exception e) {
            LOG.warn("Exception caught while processing success function - throwing as a runtime exception", e);
            throw new RuntimeException(e);
        }
    }

    protected abstract <R> BaseEitherBackedResult<R, FailureType> createSuccess(FailingOrSucceedingResult<R, FailureType> success);

    protected abstract <R> BaseEitherBackedResult<R, FailureType> createSuccess(R success);

    protected abstract <R> BaseEitherBackedResult<R, FailureType> createFailure(FailingOrSucceedingResult<R, FailureType> failure);

    private boolean isLeft() {
        return result.isLeft();
    }

    private boolean isRight() {
        return result.isRight();
    }

    private FailureType getLeft() {
        return result.getLeft();
    }

    private T getRight() {
        return result.getRight();
    }
}
