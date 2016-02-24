package com.worth.ifs.commons.error.exception;

import java.util.List;

/**
 * Created by rav on 18/02/2016.
 */
public class UnableToCreateFileException extends IFSRuntimeException {
    public UnableToCreateFileException() {
    }

    public UnableToCreateFileException(List<Object> arguments) {
        super(arguments);
    }

    public UnableToCreateFileException(String message, List<Object> arguments) {
        super(message, arguments);
    }

    public UnableToCreateFileException(String message, Throwable cause, List<Object> arguments) {
        super(message, cause, arguments);
    }

    public UnableToCreateFileException(Throwable cause, List<Object> arguments) {
        super(cause, arguments);
    }

    public UnableToCreateFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, List<Object> arguments) {
        super(message, cause, enableSuppression, writableStackTrace, arguments);
    }
}
