package com.worth.ifs.commons.error.exception;

import java.util.List;

/**
 * Created by rav on 18/02/2016.
 *
 */
public class IncorrectlyReportedMediaTypeException extends IFSRuntimeException {
    public IncorrectlyReportedMediaTypeException() {
    }

    public IncorrectlyReportedMediaTypeException(List<Object> arguments) {
        super(arguments);
    }

    public IncorrectlyReportedMediaTypeException(String message, List<Object> arguments) {
        super(message, arguments);
    }

    public IncorrectlyReportedMediaTypeException(String message, Throwable cause, List<Object> arguments) {
        super(message, cause, arguments);
    }

    public IncorrectlyReportedMediaTypeException(Throwable cause, List<Object> arguments) {
        super(cause, arguments);
    }

    public IncorrectlyReportedMediaTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<Object> arguments) {
        super(message, cause, enableSuppression, writableStackTrace, arguments);
    }
}
