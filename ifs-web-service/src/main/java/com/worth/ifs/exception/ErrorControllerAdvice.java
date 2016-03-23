package com.worth.ifs.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.commons.error.exception.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * This controller can handle all Exceptions, so the user should always gets a
 * nice looking error page, or a json error message is returned.
 * NOTE: Make sure every (non json) response uses createExceptionModelAndView as it also sets login and dashboard links
 */
@ControllerAdvice
public class ErrorControllerAdvice extends BaseErrorControllerAdvice {
    private static final Log LOG = LogFactory.getLog(ErrorControllerAdvice.class);
    public static final String URL_HASH_INVALID_TEMPLATE = "url-hash-invalid";

    public ErrorControllerAdvice() {
        super();
    }

    public ErrorControllerAdvice(Environment env, MessageSource messageSource) {
        super();
        this.env = env;
        this.messageSource = messageSource;
    }

    @ResponseStatus(HttpStatus.ALREADY_REPORTED)     // 208
    @ExceptionHandler(value = InvalidURLException.class)
    public ModelAndView invalidUrlErrorHandler(HttpServletRequest req, InvalidURLException e) {
        LOG.debug("ErrorController invalidUrlErrorHandler", e);
        return createExceptionModelAndView(e, "url-hash-invalid", req, e.getArguments(), HttpStatus.ALREADY_REPORTED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)     // 400
    @ExceptionHandler(value = {AutosaveElementException.class})
    public @ResponseBody ObjectNode jsonAutosaveResponseHandler(AutosaveElementException e) throws AutosaveElementException {
        LOG.debug("ErrorController jsonAutosaveResponseHandler", e);
        return e.createJsonResponse();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)     // 400
    @ExceptionHandler(value = IncorrectArgumentTypeException.class)
    public ModelAndView incorrectArgumentTypeErrorHandler(HttpServletRequest req, IncorrectArgumentTypeException e) {
        LOG.debug("ErrorController incorrectArgumentTypeErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value= HttpStatus.FORBIDDEN)  // 403
    @ExceptionHandler(value = ForbiddenActionException.class)
    public ModelAndView accessDeniedException(HttpServletRequest req, ForbiddenActionException e) {
        LOG.debug("ErrorController  actionNotAllowed", e);
        return createExceptionModelAndView(e, "forbidden", req, e.getArguments(), HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(value = ObjectNotFoundException.class)
    public ModelAndView objectNotFoundHandler(HttpServletRequest req, ObjectNotFoundException e) {
        LOG.debug("ErrorController  objectNotFoundHandler", e);
        return createExceptionModelAndView(e, "404", req, e.getArguments(), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)    // 409
    @ExceptionHandler(value = FileAlreadyLinkedToFormInputResponseException.class)
    public ModelAndView fileAlreadyLinkedToFormInputResponse(HttpServletRequest req, FileAlreadyLinkedToFormInputResponseException e){
        LOG.debug("ErrorController fileAlreadyLinkedToFormInputResponse", e );
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.CONFLICT);
    }

    @ResponseStatus(value= HttpStatus.LENGTH_REQUIRED)  // 411
    @ExceptionHandler(value = LengthRequiredException.class)
    public ModelAndView lengthRequiredErrorHandler(HttpServletRequest req, LengthRequiredException e){
        LOG.debug("ErrorController lengthRequired", e );
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.LENGTH_REQUIRED);
    }

    @ResponseStatus(value= HttpStatus.PAYLOAD_TOO_LARGE)  // 413
    @ExceptionHandler(value = {MaxUploadSizeExceededException.class, MultipartException.class, PayloadTooLargeException.class})
    public ModelAndView payloadTooLargeErrorHandler(HttpServletRequest req, MultipartException e){
        LOG.debug("ErrorController payloadTooLarge", e );
        // TODO: Check if we can include more information by checking root cause as follows:
        // if(e.getRootCause() != null && e.getRootCause() instanceof FileUploadBase.FileSizeLimitExceededException)
        return createExceptionModelAndView(e, "413", req, Collections.emptyList(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ResponseStatus(value= HttpStatus.UNSUPPORTED_MEDIA_TYPE)  // 415
    @ExceptionHandler(value = UnsupportedMediaTypeException.class)
    public ModelAndView unsupportedMediaTypeErrorHandler(HttpServletRequest req, UnsupportedMediaTypeException e){
        LOG.debug("ErrorController unsupportedMediaType", e );
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToCreateFileException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToCreateFileException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToCreateFoldersException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToCreateFoldersException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToSendNotificationException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToSendNotificationException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToUpdateFileException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToUpdateFileException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToDeleteFileException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToDeleteFileException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = UnableToRenderNotificationTemplateException.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, UnableToRenderNotificationTemplateException e) {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, e.getArguments(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)    //500
    @ExceptionHandler(value = {GeneralUnexpectedErrorException.class, Exception.class})
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        LOG.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req, Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}