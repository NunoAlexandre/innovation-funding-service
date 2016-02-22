package com.worth.ifs.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller can handle all Exceptions, so the user should always gets a
 * nice looking error page, or a json error message is returned.
 */
@ControllerAdvice
public class ErrorController {
    private final Log log = LogFactory.getLog(getClass());

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = AutosaveElementException.class)
    public @ResponseBody ObjectNode jsonAutosaveResponseHandler(AutosaveElementException e) throws AutosaveElementException {
        log.debug("ErrorController jsonAutosaveResponseHandler", e);
        return e.createJsonResponse();
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(value = ObjectNotFoundException.class)
    public ModelAndView objectNotFoundHandler(HttpServletRequest req, Exception e) {
        log.debug("ErrorController  objectNotFoundHandler", e);
        return createExceptionModelAndView(e, "404", req);
    }

    @ResponseStatus(value= HttpStatus.FORBIDDEN, reason="Requested operation not allowed")  // 403
    @ExceptionHandler(value = AccessDeniedException.class)
    public ModelAndView accessDeniedException(HttpServletRequest req, Exception e) {
        log.debug("ErrorController  actionNotAllowed", e);
        return createExceptionModelAndView(e, "forbidden", req);
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.debug("ErrorController  defaultErrorHandler", e);
        return createExceptionModelAndView(e, "error", req);
    }

    private static ModelAndView createExceptionModelAndView(Exception e, String message, HttpServletRequest req){
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("stacktrace", ExceptionUtils.getStackTrace(e));
        mav.addObject("message", e.getMessage());
        mav.addObject("url", req.getRequestURL().toString());
        mav.setViewName(message);
        return mav;
    }
}