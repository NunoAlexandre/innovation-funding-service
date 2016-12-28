package org.innovateuk.ifs.controller;

import org.innovateuk.ifs.commons.security.UserAuthenticationService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * Advice to make common parameters available to MVC methods e.g. logged in users
 */
@ControllerAdvice
public class ControllerModelAttributeAdvice {

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @ModelAttribute("loggedInUser")
    public UserResource getLoggedInUser(HttpServletRequest request) {
        return userAuthenticationService.getAuthenticatedUser(request);
    }
}
