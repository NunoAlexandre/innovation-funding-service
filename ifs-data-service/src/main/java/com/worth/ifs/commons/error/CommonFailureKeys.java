package com.worth.ifs.commons.error;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

/**
 * A set of failure cases for Service code, including general catch-all errors and more specific use-case errors that potentially
 * span different services
 */
public enum CommonFailureKeys implements ErrorTemplate {

    /**
     * General
     */
    GENERAL_UNEXPECTED_ERROR("An unexpected error occurred", INTERNAL_SERVER_ERROR),
    GENERAL_NOT_FOUND("Unable to find entity", NOT_FOUND),
    GENERAL_INCORRECT_TYPE("Argument was of an incorrect type", BAD_REQUEST),
    GENERAL_FORBIDDEN("User is forbidden from performing requested action", FORBIDDEN),

    /**
     * Files
     */
    FILES_UNABLE_TO_CREATE_FILE("The file could not be created", INTERNAL_SERVER_ERROR),
    FILES_FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE("A file is already linked to this Form Input Response", CONFLICT),
    FILES_UNABLE_TO_UPDATE_FILE("The file could not be updated", INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_DELETE_FILE("The file could not be deleted", INTERNAL_SERVER_ERROR),
    FILES_UNABLE_TO_CREATE_FOLDERS("Unable to create folders in order to store files", INTERNAL_SERVER_ERROR),
    FILES_DUPLICATE_FILE_CREATED("A matching file already exists", CONFLICT),
    FILES_INCORRECTLY_REPORTED_MEDIA_TYPE("The actual file media type didn't match the reported media type", UNSUPPORTED_MEDIA_TYPE),
    FILES_INCORRECTLY_REPORTED_FILESIZE("The actual file size didn't match the reported file size", BAD_REQUEST),

    /**
     * Notifications
     */
    NOTIFICATIONS_UNABLE_TO_SEND_SINGLE("The notification could not be sent", INTERNAL_SERVER_ERROR),
    NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE("Unable to send the Notifications", INTERNAL_SERVER_ERROR),
    NOTIFICATIONS_UNABLE_TO_RENDER_TEMPLATE("Could not render Notification template", INTERNAL_SERVER_ERROR),

    /**
     * Emails
     */
    EMAILS_NOT_SENT_MULTIPLE("The emails could not be sent", INTERNAL_SERVER_ERROR),

    /**
     * Users
     */
    USERS_DUPLICATE_EMAIL_ADDRESS("This email address is already taken", CONFLICT)
    ;

    private ErrorTemplate errorTemplate;

    CommonFailureKeys(String errorMessage, HttpStatus category) {
        this.errorTemplate = new ErrorTemplateImpl(name(), errorMessage, category);
    }

    @Override
    public String getErrorKey() {
        return errorTemplate.getErrorKey();
    }

    @Override
    public String getErrorMessage() {
        return errorTemplate.getErrorMessage();
    }

    @Override
    public HttpStatus getCategory() {
        return errorTemplate.getCategory();
    }
}
