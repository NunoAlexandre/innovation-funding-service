package com.worth.ifs.application;

import com.worth.ifs.application.form.ContributorsForm;
import com.worth.ifs.application.form.InviteeForm;
import com.worth.ifs.application.form.OrganisationInviteForm;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.OrganisationService;
import com.worth.ifs.application.service.UserService;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.security.CookieFlashMessageFilter;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.util.CookieUtil;
import com.worth.ifs.util.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO DW - INFUND-1555 - handle rest results
@Controller
@RequestMapping("/application/{applicationId}/contributors")
public class ApplicationContributorController{
    public static final String APPLICATION_CONTRIBUTORS_DISPLAY = "application-contributors/display";
    public static final String APPLICATION_CONTRIBUTORS_INVITE = "application-contributors/invite";
    private static final String CONTRIBUTORS_COOKIE = "contributor_invite_state";
    private final Log log = LogFactory.getLog(getClass());
    @Autowired
    private InviteRestService inviteRestService;
    @Autowired
    private UserAuthenticationService userAuthenticationService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Autowired
    private Validator validator;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String displayContributors(@PathVariable("applicationId") final Long applicationId, HttpServletRequest request, Model model) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        ProcessRole leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application);
        Organisation leadOrganisation = leadApplicantProcessRole.getOrganisation();
        User leadApplicant = leadApplicantProcessRole.getUser();

        List<InviteOrganisationResource> savedInvites = getSavedInviteOrganisations(application);
        if(savedInvites.stream().noneMatch(i -> i.getOrganisation() != null && i.getOrganisation().equals(leadOrganisation.getId()))){
            // Lead organisation has no invites, add it to the list
            savedInvites.add(0, new InviteOrganisationResource(0L, leadOrganisation.getName(), leadOrganisation, new ArrayList())); // make sure the lead organisation is also part of this list.
        }else{
            // lead organisation has invites, make sure its the first in the list.
            Optional<InviteOrganisationResource> leadOrg = savedInvites.stream().filter(i -> i.getOrganisation() != null && i.getOrganisation().equals(leadOrganisation.getId())).findAny();
            leadOrg.get().setId(0L);
        }
        Map<Long, InviteOrganisationResource> organisationInvites = savedInvites.stream().collect(Collectors.toMap(InviteOrganisationResource::getId, Function.identity()));

        model.addAttribute("authenticatedUser", user);
        model.addAttribute("currentApplication", application);
        model.addAttribute("currentCompetition", competition);
        model.addAttribute("leadApplicant", leadApplicant);
        model.addAttribute("leadOrganisation", leadOrganisation);
        model.addAttribute("organisationInvites", organisationInvites.values());
        return APPLICATION_CONTRIBUTORS_DISPLAY;
    }

    @RequestMapping(value = "/invite", method = RequestMethod.GET)
    public String inviteContributors(@PathVariable("applicationId") final Long applicationId,
                                     @ModelAttribute ContributorsForm contributorsForm,
                                     BindingResult bindingResult,
                                     HttpServletResponse response,
                                     HttpServletRequest request,
                                     Model model) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        ProcessRole leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application);
        Organisation leadOrganisation = leadApplicantProcessRole.getOrganisation();
        User leadApplicant = leadApplicantProcessRole.getUser();

        List<InviteOrganisationResource> savedInvites = getSavedInviteOrganisations(application);
        Map<Long, InviteOrganisationResource> organisationInvites = savedInvites.stream().collect(Collectors.toMap(InviteOrganisationResource::getId, Function.identity()));

        addSavedInvitesToForm(contributorsForm, leadOrganisation, savedInvites);
        mergeAndValidateCookieData(request, response, bindingResult, contributorsForm, applicationId, application, leadApplicant);

        model.addAttribute("authenticatedUser", user);
        model.addAttribute("currentApplication", application);
        model.addAttribute("currentCompetition", competition);
        model.addAttribute("leadApplicant", leadApplicant);
        model.addAttribute("leadOrganisation", leadOrganisation);
        model.addAttribute("organisationInvites", organisationInvites);
        return APPLICATION_CONTRIBUTORS_INVITE;
    }

    /**
     * Add the invites from the database, to the ContributorsForm object.
     */
    private void addSavedInvitesToForm(ContributorsForm contributorsForm, Organisation leadOrganisation, List<InviteOrganisationResource> savedInvites) {
        OrganisationInviteForm leadOrganisationInviteForm = new OrganisationInviteForm();
        leadOrganisationInviteForm.setOrganisationName(leadOrganisation.getName());
        leadOrganisationInviteForm.setOrganisationId(leadOrganisation.getId());
        Optional<InviteOrganisationResource> hasInvites = savedInvites.stream()
                .filter(i -> leadOrganisation.getId().equals(i.getOrganisation()))
                .findAny();
        if (hasInvites.isPresent()) {
            leadOrganisationInviteForm.setOrganisationInviteId(hasInvites.get().getId());
        }
        contributorsForm.getOrganisations().add(leadOrganisationInviteForm);

        savedInvites.stream()
                .filter(inviteOrg -> inviteOrg.getOrganisation()==null || !inviteOrg.getOrganisation().equals(leadOrganisation.getId()))
                .forEach(inviteOrg -> {
                    OrganisationInviteForm invitedOrgForm = new OrganisationInviteForm();
                    invitedOrgForm.setOrganisationName(inviteOrg.getOrganisationName());
                    invitedOrgForm.setOrganisationId(inviteOrg.getOrganisation());
                    invitedOrgForm.setOrganisationInviteId(inviteOrg.getId());
                    contributorsForm.getOrganisations().add(invitedOrgForm);
                });
    }

    private void mergeAndValidateCookieData(HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult, ContributorsForm contributorsForm, Long applicationId, ApplicationResource application, User leadApplicant) {

        String json = CookieUtil.getCookieValue(request, CONTRIBUTORS_COOKIE);

        if (json != null && !json.equals("")) {
            ContributorsForm contributorsFormCookie = JsonUtil.getObjectFromJson(json, ContributorsForm.class);
            if (contributorsFormCookie.getApplicationId().equals(applicationId)) {
                if (contributorsFormCookie.isTriedToSave()) {
                    // if the form was saved, validate and update cookie.
                    contributorsFormCookie.setTriedToSave(false);
                    String jsonState = JsonUtil.getSerializedObject(contributorsFormCookie);
                    CookieUtil.saveToCookie(response, CONTRIBUTORS_COOKIE, jsonState);

                    contributorsForm.merge(contributorsFormCookie);
                    validator.validate(contributorsForm, bindingResult);
                    validateUniqueEmails(contributorsForm, bindingResult, application, leadApplicant);
                } else {
                    contributorsForm.merge(contributorsFormCookie);
                }
            }
        }
    }

    /**
     * Handle form POST, manage ContributorsForm object, save to cookie, and redirect to the GET handler.
     */
    @RequestMapping(value = "/invite", method = RequestMethod.POST)
    public String inviteContributors(@PathVariable("applicationId") final Long applicationId,
                                     @RequestParam(name = "add_person", required = false) String organisationIndex,
                                     @RequestParam(name = "add_partner", required = false) String addPartner,
                                     @RequestParam(name = "remove_person", required = false) String organisationAndPerson,
                                     @RequestParam(name = "save_contributors", required = false) String saveContributors,
                                     @RequestParam(name = "newApplication", required = false) String newApplication,
                                     @ModelAttribute ContributorsForm contributorsForm,
                                     BindingResult bindingResult,
                                     HttpServletResponse response,
                                     HttpServletRequest request) {
        ApplicationResource application = applicationService.getById(applicationId);
        ProcessRole leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application);
        // User should never be able to set the organisation name or id of the lead-organisation.
        contributorsForm.getOrganisations().get(0).setOrganisationName(leadApplicantProcessRole.getOrganisation().getName());
        contributorsForm.getOrganisations().get(0).setOrganisationId(leadApplicantProcessRole.getOrganisation().getId());
        contributorsForm.setTriedToSave(false);

        if (organisationIndex != null) {
            addPersonRow(contributorsForm, organisationIndex);
            saveFormValuesToCookie(response, contributorsForm, applicationId);
        } else if (organisationAndPerson != null) {
            removePersonRow(contributorsForm, organisationAndPerson);
            saveFormValuesToCookie(response, contributorsForm, applicationId);
        } else if (addPartner != null) {
            addPartnerRow(contributorsForm);
            saveFormValuesToCookie(response, contributorsForm, applicationId);
        } else if (saveContributors != null) {
            contributorsForm.setTriedToSave(true);
            validator.validate(contributorsForm, bindingResult);
            User leadApplicant = leadApplicantProcessRole.getUser();
            validateUniqueEmails(contributorsForm, bindingResult, application, leadApplicant);

            if (!bindingResult.hasErrors()) {
                saveContributors(applicationId, contributorsForm, response);
                // empty cookie, since the invites are saved.
                CookieUtil.saveToCookie(response, CONTRIBUTORS_COOKIE, "");

                if (newApplication != null) {
                    return ApplicationController.redirectToApplication(application);
                }
                return String.format("redirect:/application/%d/contributors", applicationId);
            } else {
                saveFormValuesToCookie(response, contributorsForm, applicationId);
            }
        } else {
            // no specific submit action, just save the data to the cookie.
            saveFormValuesToCookie(response, contributorsForm, applicationId);
        }

        if (newApplication != null) {
            return String.format("redirect:/application/%d/contributors/invite/?newApplication", applicationId);
        }
        return String.format("redirect:/application/%d/contributors/invite", applicationId);
    }

    private void saveContributors(@PathVariable("applicationId") Long applicationId, @ModelAttribute ContributorsForm contributorsForm, HttpServletResponse response) {
        contributorsForm.getOrganisations().forEach((organisationInvite) -> {
            List<InviteResource> invites = new ArrayList<>();
            Organisation existingOrganisation = null;
            if (organisationInvite.getOrganisationId() != null) {
                // check if there is a organisation with this ID, just to make sure the user has not entered a non-existing organisation id.
                existingOrganisation = organisationService.getOrganisationById(organisationInvite.getOrganisationId());
            }

            organisationInvite.getInvites().stream().forEach(invite -> {
                InviteResource inviteResource = new InviteResource(invite.getPersonName(), invite.getEmail(), applicationId);
                if (organisationInvite.getOrganisationInviteId() != null && !organisationInvite.getOrganisationInviteId().equals(Long.valueOf(0))) {
                    inviteResource.setInviteOrganisation(organisationInvite.getOrganisationInviteId());
                }
                invites.add(inviteResource);
            });

            if (organisationInvite.getOrganisationInviteId() != null && !organisationInvite.getOrganisationInviteId().equals(Long.valueOf(0))) {
                // save new invites, to InviteOrganisation that already is saved.
                inviteRestService.saveInvites(invites);
                cookieFlashMessageFilter.setFlashMessage(response, "invitesSend");
            } else if (existingOrganisation != null) {
                // Save invites, and link to existing organisation.
                inviteRestService.createInvitesByOrganisation(existingOrganisation.getId(), invites);
                cookieFlashMessageFilter.setFlashMessage(response, "invitesSend");
            } else {
                // Save invites, and create new InviteOrganisation
                inviteRestService.createInvitesByInviteOrganisation(organisationInvite.getOrganisationName(), invites);
                cookieFlashMessageFilter.setFlashMessage(response, "invitesSend");
            }
        });
    }

    /**
     * Check if e-mail addresses entered, are unique within this application's invites.
     */
    private void validateUniqueEmails(@ModelAttribute ContributorsForm contributorsForm, BindingResult bindingResult, ApplicationResource application, User leadApplicant) {
        Set<String> savedEmails = getSavedEmailAddresses(application);
        savedEmails.add(leadApplicant.getEmail());
        contributorsForm.getOrganisations().forEach(o -> o.getInvites().forEach(i -> {
            if(!savedEmails.add(i.getEmail())){
                // Could not add the element, so its a duplicate.
                FieldError fieldError = new FieldError("contributorsForm", String.format("organisations[%d].invites[%d].email", contributorsForm.getOrganisations().indexOf(o), o.getInvites().indexOf(i)), i.getEmail(), false, new String[]{"NotUnique"}, null, "may not be duplicate");
                bindingResult.addError(fieldError);
            }
        }));
    }

    private Set<String> getSavedEmailAddresses(ApplicationResource application) {
        Set<String> savedEmails = new TreeSet<>();
        List<InviteOrganisationResource> savedInvites = getSavedInviteOrganisations(application);
        savedInvites.forEach(s -> {
                    s.getInviteResources().stream().forEach(i -> savedEmails.add(i.getEmail()));
                }
        );
        return savedEmails;
    }

    private void saveFormValuesToCookie(HttpServletResponse response, ContributorsForm contributorsForm, Long applicationId) {
        contributorsForm.setApplicationId(applicationId);
        String jsonState = JsonUtil.getSerializedObject(contributorsForm);
        CookieUtil.saveToCookie(response, CONTRIBUTORS_COOKIE, jsonState);
    }

    private void removePersonRow(ContributorsForm contributorsForm, String organisationAndPerson) {
        int organisationIndex = Integer.parseInt(organisationAndPerson.substring(0, organisationAndPerson.indexOf("_")));
        int personIndex = Integer.parseInt(organisationAndPerson.substring(organisationAndPerson.indexOf("_") + 1, organisationAndPerson.length()));

        // Removing the last person from a organisation will also remove the organisation itself.
        contributorsForm.getOrganisations().get(organisationIndex).getInvites().remove(personIndex);
        if (organisationIndex != 0 && contributorsForm.getOrganisations().get(organisationIndex).getInvites().size() == 0) {
            contributorsForm.getOrganisations().remove(organisationIndex);
        }
    }

    private void addPersonRow(ContributorsForm contributorsForm, String organisationIndex) {
        OrganisationInviteForm organisationInviteForm = contributorsForm.getOrganisations().get(Integer.parseInt(organisationIndex));
        List<InviteeForm> invites = organisationInviteForm.getInvites();
        invites.add(new InviteeForm());
    }

    private void addPartnerRow(ContributorsForm contributorsForm) {
        OrganisationInviteForm organisationInviteForm = new OrganisationInviteForm();
        InviteeForm invite = new InviteeForm();
        List<InviteeForm> invites = organisationInviteForm.getInvites();
        invites.add(invite);
        organisationInviteForm.setInvites(invites);
        contributorsForm.getOrganisations().add(organisationInviteForm);
    }

    private List<InviteOrganisationResource> getSavedInviteOrganisations(ApplicationResource application) {
        return inviteRestService.getInvitesByApplication(application.getId()).handleSuccessOrFailure(
                failure -> Collections.<InviteOrganisationResource>emptyList(),
                success -> success);
    }
}
