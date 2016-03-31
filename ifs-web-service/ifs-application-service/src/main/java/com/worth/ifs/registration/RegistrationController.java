package com.worth.ifs.registration;

import com.worth.ifs.application.AcceptInviteController;
import com.worth.ifs.application.ApplicationCreationController;
import com.worth.ifs.application.service.OrganisationService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.error.exception.InvalidURLException;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.filter.CookieFlashMessageFilter;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.registration.form.RegistrationForm;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.CompAdminEmailService;
import com.worth.ifs.user.service.UserService;
import com.worth.ifs.util.CookieUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.login.HomeController.getRedirectUrlForUser;

@Controller
@RequestMapping("/registration")
public class RegistrationController {
    public static final String BASE_URL = "/registration/register";

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired
    Validator validator;
    @Autowired
    private UserService userService;

    @Autowired
    private CompAdminEmailService compAdminEmailService;

    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private InviteRestService inviteRestService;

    @Autowired
    protected UserAuthenticationService userAuthenticationService;

    @Autowired
    protected CookieFlashMessageFilter cookieFlashMessageFilter;

    private static final Log LOG = LogFactory.getLog(RegistrationController.class);

    public final static String ORGANISATION_ID_PARAMETER_NAME = "organisationId";
    public final static String EMAIL_FIELD_NAME = "email";

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String registrationSuccessful(
            @RequestHeader(value = "referer", required = false) final String referer,
            final HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.removeCookie(response, OrganisationCreationController.ORGANISATION_ID);
        if(referer == null || !referer.contains(request.getServerName() + "/registration/register")){
            throw new ObjectNotFoundException("Attempt to access registration page directly...", Collections.emptyList());
        }
        return "registration/successful";
    }

    @RequestMapping(value = "/verified", method = RequestMethod.GET)
    public String verificationSuccessful(final HttpServletRequest request, final HttpServletResponse response) {
        if(!hasVerifiedCookieSet(request)){
            throw new ObjectNotFoundException("Attempt to access registration page directly...", Collections.emptyList());
        } else {
            cookieFlashMessageFilter.removeFlashMessage(response);
            return "registration/verified";
        }
    }

    @RequestMapping(value = "/verify-email/{hash}", method = RequestMethod.GET)
    public String verifyEmailAddress(@PathVariable("hash") final String hash,
                                     final HttpServletResponse response){
        if(userService.verifyEmail(hash).isSuccess()){
            cookieFlashMessageFilter.setFlashMessage(response, "verificationSuccessful");
            return "redirect:/registration/verified";
        }else{
            throw new InvalidURLException();
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerForm(Model model, HttpServletRequest request) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        if(user != null){
            return getRedirectUrlForUser(user);
        }

        String destination = "registration-register";

        if (!processOrganisation(request, model)) {
            destination = "redirect:/";
        }

        addRegistrationFormToModel(model, request);
        return destination;
    }

    private boolean processOrganisation(HttpServletRequest request, Model model) {
        boolean success = true;

        Organisation organisation = getOrganisation(request);
        if (organisation != null) {
            addOrganisationNameToModel(model, organisation);
        } else {
            success = false;
        }

        return success;
    }

    private void addRegistrationFormToModel(Model model, HttpServletRequest request) {
        RegistrationForm registrationForm = new RegistrationForm();
        setFormActionURL(registrationForm, request);
        setInviteeEmailAddress(registrationForm, request, model);
        model.addAttribute("registrationForm", registrationForm);
    }

    /**
     * When the current user is a invitee, user the invite email-address in the registration flow.
     */
    private boolean setInviteeEmailAddress(RegistrationForm registrationForm, HttpServletRequest request, Model model) {
        String inviteHash = CookieUtil.getCookieValue(request, AcceptInviteController.INVITE_HASH);
        if(StringUtils.hasText(inviteHash)){
            RestResult<InviteResource> invite = inviteRestService.getInviteByHash(inviteHash);
            if(invite.isSuccess() && InviteStatusConstants.SEND.equals(invite.getSuccessObject().getStatus())){
                InviteResource inviteResource = invite.getSuccessObject();
                registrationForm.setEmail(inviteResource.getEmail());
                model.addAttribute("invitee", true);
                return true;
            }else{
                LOG.debug("Invite already accepted.");
            }
        }
        return false;
    }

    private Organisation getOrganisation(HttpServletRequest request) {
        return organisationService.getOrganisationById(getOrganisationId(request));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerFormSubmit(@Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
                                     BindingResult bindingResult,
                                     HttpServletResponse response,
                                     HttpServletRequest request,
                                     Model model) {

        LOG.warn("registerFormSubmit");
        if(setInviteeEmailAddress(registrationForm, request, model)){
            LOG.warn("setInviteeEmailAddress"+ registrationForm.getEmail());
            // re-validate since we did set the emailaddress in the meantime. @Valid annotation is needed for unit tests.
            bindingResult = new BeanPropertyBindingResult(registrationForm, "registrationForm");
            validator.validate(registrationForm, bindingResult);
        }

        User user = userAuthenticationService.getAuthenticatedUser(request);
        if(user != null){
            return getRedirectUrlForUser(user);
        }

        String destination = "registration-register";

        checkForExistingEmail(registrationForm.getEmail(), bindingResult);

        if(isUserCompAdmin(registrationForm.getEmail())){
            LOG.info("User is comp admin");
        } else {
            LOG.info("User is not comp admin");
        }

        if(!bindingResult.hasErrors()) {
            RestResult<UserResource> createUserResult = createUser(registrationForm, getOrganisationId(request), getCompetitionId(request));

            if (createUserResult.isSuccess()) {
                removeCompetitionIdCookie(response);
                acceptInvite(request, response, createUserResult.getSuccessObject()); // might want to move this, to after email verifications.
                destination = "redirect:/registration/success";
            } else {
                if (!processOrganisation(request, model)) {
                    destination = "redirect:/";
                }
                addEnvelopeErrorsToBindingResultErrors(createUserResult.getFailure().getErrors(), bindingResult);
            }
        } else {
            if (!processOrganisation(request, model)) {
                destination = "redirect:/";
            }
        }

        return destination;
    }

    private void removeCompetitionIdCookie(HttpServletResponse response) {
        CookieUtil.removeCookie(response, ApplicationCreationController.COMPETITION_ID);
    }

    private Long getCompetitionId(HttpServletRequest request) {
        Long competitionId = null;
        if(StringUtils.hasText(CookieUtil.getCookieValue(request, ApplicationCreationController.COMPETITION_ID))){
            competitionId = Long.valueOf(CookieUtil.getCookieValue(request, ApplicationCreationController.COMPETITION_ID));
        }
        return competitionId;
    }

    private boolean acceptInvite(HttpServletRequest request, HttpServletResponse response, UserResource userResource) {
        String inviteHash = CookieUtil.getCookieValue(request, AcceptInviteController.INVITE_HASH);
        if(StringUtils.hasText(inviteHash)){
            RestResult<InviteResource> restResult = inviteRestService.getInviteByHash(inviteHash).andOnSuccessReturn(i -> {
                inviteRestService.acceptInvite(inviteHash, userResource.getId()).getStatusCode();
                return i;
            });
            CookieUtil.removeCookie(response, AcceptInviteController.INVITE_HASH);
            return restResult.isSuccess();
        }
        return false;
    }

    private void checkForExistingEmail(String email, BindingResult bindingResult) {
        if(!bindingResult.hasFieldErrors(EMAIL_FIELD_NAME) && StringUtils.hasText(email)) {
            RestResult existingUserSearch = userService.findUserByEmail(email);
            if (!HttpStatus.NOT_FOUND.equals(existingUserSearch.getStatusCode())) {
                bindingResult.addError(new FieldError(EMAIL_FIELD_NAME, EMAIL_FIELD_NAME, email, false, null, null, "Email address is already in use"));
            }
        }
    }

    private boolean isUserCompAdmin(final String email) {
        if(StringUtils.hasText(email)) {
            RestResult existingUserSearch = compAdminEmailService.findByEmail(email);
            if (HttpStatus.FOUND.equals(existingUserSearch.getStatusCode())) {
                return true;
            }
        }
        return false;
    }

    private void addEnvelopeErrorsToBindingResultErrors(List<Error> errors, BindingResult bindingResult) {
        errors.forEach(
                error -> bindingResult.reject("registration."+error.getErrorKey())
        );
    }

    private RestResult<UserResource> createUser(RegistrationForm registrationForm, Long organisationId, Long competitionId) {
        return userService.createLeadApplicantForOrganisationWithCompetitionId(
                registrationForm.getFirstName(),
                registrationForm.getLastName(),
                registrationForm.getPassword(),
                registrationForm.getEmail(),
                registrationForm.getTitle(),
                registrationForm.getPhoneNumber(),
                organisationId,
                competitionId);
    }

    private void addOrganisationNameToModel(Model model, Organisation organisation) {
        model.addAttribute("organisationName", organisation.getName());
    }

    private Long getOrganisationId(HttpServletRequest request) {
        String organisationParameter = request.getParameter(ORGANISATION_ID_PARAMETER_NAME);
        Long organisationId = null;

        try {
            if (Long.parseLong(organisationParameter) >= 0) {
                organisationId = Long.parseLong(organisationParameter);
            }
        } catch (NumberFormatException e) {
            LOG.info("Invalid organisationId number format:" + e);
        }

        return organisationId;
    }

    private void setFormActionURL(RegistrationForm registrationForm, HttpServletRequest request) {
        Long organisationId = getOrganisationId(request);
        registrationForm.setActionUrl(BASE_URL + "?" + ORGANISATION_ID_PARAMETER_NAME + "=" + organisationId);
    }

    private boolean hasVerifiedCookieSet(final HttpServletRequest request) {
        final Optional<Cookie> cookie = CookieUtil.getCookie(request, "flashMessage");
        return cookie.isPresent() && cookie.get().getValue().equals("verificationSuccessful");
    }
}
