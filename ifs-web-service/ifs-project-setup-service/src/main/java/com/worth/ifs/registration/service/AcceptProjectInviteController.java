package com.worth.ifs.registration.service;

import com.worth.ifs.BaseController;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.filter.CookieFlashMessageFilter;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.project.service.ProjectRestService;
import com.worth.ifs.project.viewmodel.JoinAProjectViewModel;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.OrganisationRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.Error.globalError;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.invite.constant.InviteStatusConstants.SEND;
import static com.worth.ifs.util.CookieUtil.getCookieValue;
import static com.worth.ifs.util.CookieUtil.saveToCookie;
import static com.worth.ifs.util.RestLookupCallbacks.find;

/**
 * This class is use as an entry point to accept a invite to a project, to a application.
 */
@Controller
public class AcceptProjectInviteController extends BaseController {

    public static final String INVITE_HASH = "project_invite_hash";
    @Autowired
    private InviteRestService inviteRestService;
    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;
    @Autowired
    private ProjectRestService projectRestService;
    @Autowired
    private OrganisationRestService organisationRestService;

    private static final String ACCEPT_INVITE_MAPPING = "/accept-invite/";
    private static final String ACCEPT_INVITE_USER_DOES_NOT_YET_EXIST_SHOW_PROJECT_MAPPING = "/registration/accept-invite-user-does-not-yet-exist-show-project";
    private static final String ACCEPT_INVITE_USER_EXIST_SHOW_PROJECT_MAPPING = "/registration/accept-invite-user-exist-show-project";
    private static final String ACCEPT_INVITE_USER_EXIST_CONFIRM_MAPPING = "/registration/accept-invite-user-exist-confirm";

    private static final String ACCEPT_INVITE_USER_EXISTS_BUT_NOT_LOGGED_IN_VIEW = "project/registration/accept-invite-user-exists-but-not-logged-in";
    private static final String ACCEPT_INVITE_SHOW_PROJECT = "project/registration/accept-invite-show-project";
    private static final String ACCEPT_INVITE_FAILURE = "project/registration/accept-invite-failure";


    //===================================
    // Initial landing of the invite link
    //===================================

    @RequestMapping(value = ACCEPT_INVITE_MAPPING + "{hash}", method = RequestMethod.GET)
    public String inviteEntryPage(
            @PathVariable("hash") final String hash,
            HttpServletResponse response,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        return find(inviteByHash(hash), inviteOrganisationByHash(hash), checkUserExistsByHash(hash)).andOnSuccess((invite, inviteOrganisation, userExists) -> {
            ValidationMessages errors = errorMessages(loggedInUser, invite);
            if (errors.hasErrors()) {
                return populateModelWithErrorsAndReturnErrorView(errors, model);
            }
            saveToCookie(response, INVITE_HASH, hash);
            if (userExists && loggedInUser == null) {
                return restSuccess(ACCEPT_INVITE_USER_EXISTS_BUT_NOT_LOGGED_IN_VIEW);
            } else if (userExists) {
                return restSuccess("redirect:" + ACCEPT_INVITE_USER_EXIST_SHOW_PROJECT_MAPPING);
            } else {
                return restSuccess("redirect:" + ACCEPT_INVITE_USER_DOES_NOT_YET_EXIST_SHOW_PROJECT_MAPPING);
            }
        }).getSuccessObject();
    }

    //==================================
    // Show the user the confirm project
    //==================================

    @RequestMapping(value = ACCEPT_INVITE_USER_DOES_NOT_YET_EXIST_SHOW_PROJECT_MAPPING, method = RequestMethod.GET)
    public String acceptInviteUserDoesNotYetExistShowProject(HttpServletRequest request, Model model, @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        model.addAttribute("userExists", false);
        return acceptInviteShowProject(request, model, loggedInUser);
    }

    @RequestMapping(value = ACCEPT_INVITE_USER_EXIST_SHOW_PROJECT_MAPPING, method = RequestMethod.GET)
    public String acceptInviteUserDoesExistShowProject(HttpServletRequest request, Model model, @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        model.addAttribute("userExists", true);
        return acceptInviteShowProject(request, model, loggedInUser);
    }

    private String acceptInviteShowProject(HttpServletRequest request, Model model, UserResource loggedInUser) {
        String hash = getCookieValue(request, INVITE_HASH);
        return find(inviteByHash(hash), inviteOrganisationByHash(hash))
                .andOnSuccess((invite, inviteOrganisation) -> {
                    ValidationMessages errors = errorMessages(loggedInUser, invite);
                    if (errors.hasErrors()) {
                        return populateModelWithErrorsAndReturnErrorView(errors, model);
                    }
                    return organisationRestService.getOrganisationByIdForAnonymousUserFlow(inviteOrganisation.getOrganisation())
                            .andOnSuccessReturn(organisation -> {
                                JoinAProjectViewModel pdavm = new JoinAProjectViewModel();
                                model.addAttribute("model", pdavm);
                                return ACCEPT_INVITE_SHOW_PROJECT;
                            });
                }).getSuccessObject();


    }

    //======================================================
    // Accept a project invite for a user who already exists
    //======================================================

    @RequestMapping(value = ACCEPT_INVITE_USER_EXIST_CONFIRM_MAPPING, method = RequestMethod.GET)
    public String acceptInviteUserDoesExistConfirm(HttpServletRequest request, @ModelAttribute("loggedInUser") UserResource loggedInUser, Model model) {
        String hash = getCookieValue(request, INVITE_HASH);
        return find(inviteByHash(hash), inviteOrganisationByHash(hash), userByHash(hash)).andOnSuccess((invite, inviteOrganisation, userExists) -> {
                    ValidationMessages errors = errorMessages(loggedInUser, invite);
                    if (errors.hasErrors()) {
                        return populateModelWithErrorsAndReturnErrorView(errors, model);
                    }
                    // Add the user to the project
                    return projectRestService.addPartner(1L, userExists.getId(), inviteOrganisation.getOrganisation())
                            .andOnSuccess(() -> inviteRestService.acceptInvite(hash, userExists.getId()))
                            .andOnSuccessReturn(() -> "redirect:/");

                }
        ).getSuccessObject();
    }


    //======================================================
    // Code to validate fundamental problems with the invite
    //======================================================

    private RestResult<String> populateModelWithErrorsAndReturnErrorView(ValidationMessages errors, Model model) {
        model.addAttribute("failureMessageKeys", errors.getErrors());
        return restSuccess(ACCEPT_INVITE_FAILURE);
    }

    private ValidationMessages errorMessages(UserResource loggedInUser, InviteResource invite) {
        ValidationMessages errors = new ValidationMessages();
        if (!invite.getStatus().equals(SEND)) {
            errors.addError(globalError("registration.INVITE_ALREADY_ACCEPTED"));
        } else if (loggedInUser != null && !invite.getEmail().equalsIgnoreCase(loggedInUser.getEmail())) {
            errors.addError(globalError("registration.LOGGED_IN_WITH_OTHER_ACCOUNT"));
        }
        return errors;
    }

    private Supplier<RestResult<InviteResource>> inviteByHash(String hash) {
        return () -> inviteRestService.getInviteByHash(hash);
    }

    private Supplier<RestResult<InviteOrganisationResource>> inviteOrganisationByHash(String hash) {
        return () -> inviteRestService.getInviteOrganisationByHash(hash);
    }

    private Supplier<RestResult<Boolean>> checkUserExistsByHash(String hash) {
        return () -> inviteRestService.checkExistingUser(hash);
    }

    private Supplier<RestResult<UserResource>> userByHash(String hash) {
        return () -> inviteRestService.getUser(hash);
    }

}
