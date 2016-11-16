package com.worth.ifs.login;

import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.user.resource.RoleResource;
import com.worth.ifs.user.resource.UserResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static java.lang.String.format;
import static org.springframework.util.StringUtils.hasText;

/**
 * This Controller redirects the request from http://<domain>/ to http://<domain>/login
 * So we don't have a public homepage, the login page is the homepage.
 */
@Controller
public class HomeController {

    @Autowired
    UserAuthenticationService userAuthenticationService;

    public static String getRedirectUrlForUser(UserResource user) {

        String roleUrl = "";

        if (!user.getRoles().isEmpty()) {
            roleUrl = user.getRoles().get(0).getUrl();
        }

        return format("redirect:/%s", hasText(roleUrl) ? roleUrl : "dashboard");
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String login() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (unauthenticated(authentication)) {
            return "redirect:/";
        }

        UserResource user = (UserResource) authentication.getDetails();
        if (user.getRoles().size() > 0) {
            redirectToRoleSelection();
        }

        return getRedirectUrlForUser(user);
    }

    @RequestMapping(value = "/role", method = RequestMethod.GET)
    public String chooseRole() {

        return "/roleSelection";
    }

    @RequestMapping(value = "/role", method = RequestMethod.POST)
    public String processRole(Model model,
                              @ModelAttribute("loggedInUser") UserResource user,
                              @Valid @ModelAttribute("form") RoleSelectionForm form,
                              BindingResult bindingResult,
                              ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> redirectToRoleSelection();

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ValidationMessages validationMessages = new ValidationMessages(bindingResult);
            return validationHandler.addAnyErrors(validationMessages, fieldErrorsToFieldErrors(), asGlobalErrors()).
                    failNowOrSucceedWith(failureView, () -> redirectToChosenDashboard(user, form.getSelectedRole().getName()));
        });
    }

    private String redirectToRoleSelection() {
        return "redirect:/role";
    }

    private String redirectToChosenDashboard(UserResource user, String role) {
        List<RoleResource> roles = user.getRoles();
        String url = roles.stream().filter(roleResource -> roleResource.getName().equals(role)).findFirst().get().getUrl();

        return format("redirect:/%s", url);
    }

    private static boolean unauthenticated(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication.getDetails() == null;
    }
}
