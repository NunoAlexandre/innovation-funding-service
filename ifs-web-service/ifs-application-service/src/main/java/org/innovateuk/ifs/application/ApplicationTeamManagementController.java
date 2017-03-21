package org.innovateuk.ifs.application;

import org.innovateuk.ifs.application.form.ApplicantInviteForm;
import org.innovateuk.ifs.application.form.ApplicationTeamUpdateForm;
import org.innovateuk.ifs.application.populator.ApplicationTeamManagementModelPopulator;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteResultsResource;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.commons.service.ServiceResult.processAnyFailuresOrSucceed;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * This controller will handle all requests that are related to the management of application participants
 */
@Controller
@RequestMapping("/application/{applicationId}/team/update")
@PreAuthorize("hasAuthority('applicant')")
public class ApplicationTeamManagementController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private InviteRestService inviteRestService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationTeamManagementModelPopulator applicationTeamManagementModelPopulator;

    @GetMapping(params = {"organisation"})
    public String getUpdateOrganisation(Model model,
                                        @PathVariable("applicationId") long applicationId,
                                        @RequestParam(name = "organisation") long organisationId,
                                        @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                        @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form) {
        model.addAttribute("model", applicationTeamManagementModelPopulator.populateModelByOrganisationId(applicationId, organisationId,
                loggedInUser.getId()));
        return "application-team/edit-org";
    }

    @GetMapping(params = {"inviteOrganisation"})
    public String getUpdateOrganisationByInviteOrganisation(Model model,
                                                            @PathVariable("applicationId") long applicationId,
                                                            @RequestParam(name = "inviteOrganisation") long inviteOrganisationId,
                                                            @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                            @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form) {
        model.addAttribute("model", applicationTeamManagementModelPopulator.populateModelByInviteOrganisationId(applicationId, inviteOrganisationId,
                loggedInUser.getId()));
        return "application-team/edit-org";
    }

    @PostMapping(params = {"organisation"})
    public String submitUpdateOrganisation(Model model,
                                           @PathVariable("applicationId") Long applicationId,
                                           @RequestParam(name = "organisation") long organisationId,
                                           @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                           @Valid @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                           @SuppressWarnings("unused") BindingResult bindingResult,
                                           ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<InviteResultsResource> updateResult = updateInvitesByOrganisation(organisationId, form, applicationId);

            return validationHandler.addAnyErrors(updateResult, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(failureView, () -> format("redirect:/application/%s/team", applicationId));
        });
    }

    @PostMapping(params = {"inviteOrganisation"})
    public String submitUpdateOrganisationByInviteOrganisation(Model model,
                                                               @PathVariable("applicationId") Long applicationId,
                                                               @RequestParam(name = "inviteOrganisation") long inviteOrganisationId,
                                                               @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                               @Valid @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                                               @SuppressWarnings("unused") BindingResult bindingResult,
                                                               ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> getUpdateOrganisationByInviteOrganisation(model, applicationId, inviteOrganisationId, loggedInUser, form);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<InviteResultsResource> updateResult = updateInvitesByInviteOrganisation(inviteOrganisationId, form, applicationId);

            return validationHandler.addAnyErrors(updateResult, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(failureView, () -> format("redirect:/application/%s/team", applicationId));
        });
    }

    @PostMapping(params = {"addApplicant", "organisation"})
    public String addApplicant(Model model,
                               @PathVariable("applicationId") long applicationId,
                               @RequestParam(name = "organisation") long organisationId,
                               @ModelAttribute("loggedInUser") UserResource loggedInUser,
                               @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form) {
        form.getApplicants().add(new ApplicantInviteForm());
        return getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form);
    }

    @PostMapping(params = {"addApplicant", "inviteOrganisation"})
    public String addApplicantByInviteOrganisation(Model model,
                                                   @PathVariable("applicationId") long applicationId,
                                                   @RequestParam(name = "inviteOrganisation") long inviteOrganisationId,
                                                   @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                   @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form) {
        form.getApplicants().add(new ApplicantInviteForm());
        return getUpdateOrganisationByInviteOrganisation(model, applicationId, inviteOrganisationId, loggedInUser, form);
    }

    @PostMapping(params = {"removeApplicant", "organisation"})
    public String removeApplicant(Model model,
                                  @PathVariable("applicationId") long applicationId,
                                  @RequestParam(name = "organisation") long organisationId,
                                  @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                  @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                  @RequestParam(name = "removeApplicant") Integer position) {
        form.getApplicants().remove(position.intValue());
        return getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form);
    }

    @PostMapping(params = {"removeApplicant", "inviteOrganisation"})
    public String removeApplicantByInviteOrganisation(Model model,
                                                      @PathVariable("applicationId") long applicationId,
                                                      @RequestParam(name = "inviteOrganisation") long inviteOrganisationId,
                                                      @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                      @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                                      @RequestParam(name = "removeApplicant") Integer position) {
        form.getApplicants().remove(position.intValue());
        return getUpdateOrganisationByInviteOrganisation(model, applicationId, inviteOrganisationId, loggedInUser, form);
    }

    @PostMapping(params = {"markForRemoval", "organisation"})
    public String markForRemoval(Model model,
                                 @PathVariable("applicationId") long applicationId,
                                 @RequestParam(name = "organisation") long organisationId,
                                 @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                 @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                 @RequestParam(name = "markForRemoval") long applicationInviteId) {
        form.getMarkedForRemoval().add(applicationInviteId);
        return getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form);
    }

    @PostMapping(params = {"markForRemoval", "inviteOrganisation"})
    public String markForRemovalByInviteOrganisation(Model model,
                                                     @PathVariable("applicationId") long applicationId,
                                                     @RequestParam(name = "inviteOrganisation") long inviteOrganisationId,
                                                     @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                     @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                                     @RequestParam(name = "markForRemoval") long applicationInviteId) {
        form.getMarkedForRemoval().add(applicationInviteId);
        return getUpdateOrganisationByInviteOrganisation(model, applicationId, inviteOrganisationId, loggedInUser, form);
    }

    @PostMapping(params = {"deleteLastApplicant", "organisation"})
    public String deleteLastApplicantForOrganisation(Model model,
                                                     @PathVariable("applicationId") Long applicationId,
                                                     @RequestParam("organisation") long organisationId,
                                                     @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                     @Valid @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                                     @RequestParam("deleteLastApplicant") long lastApplicantId) {
        form.getMarkedForRemoval().add(lastApplicantId);

        return updateInvitesByOrganisation(organisationId, form, applicationId).handleSuccessOrFailure(
                failure -> getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form),
                success -> format("redirect:/application/%s/team", applicationId)
        );
    }

    @PostMapping(params = {"deleteLastApplicant", "inviteOrganisation"})
    public String deleteLastApplicantForInviteOrganisation(Model model,
                                                           @PathVariable("applicationId") Long applicationId,
                                                           @RequestParam("inviteOrganisation") long organisationId,
                                                           @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                                           @Valid @ModelAttribute(FORM_ATTR_NAME) ApplicationTeamUpdateForm form,
                                                           @RequestParam("deleteLastApplicant") long lastApplicantId) {
        form.getMarkedForRemoval().add(lastApplicantId);

        return updateInvitesByInviteOrganisation(organisationId, form, applicationId).handleSuccessOrFailure(
                failure -> getUpdateOrganisation(model, applicationId, organisationId, loggedInUser, form),
                success -> format("redirect:/application/%s/team", applicationId)
        );
    }

    private ServiceResult<InviteResultsResource> updateInvitesByOrganisation(long organisationId,
                                                                             ApplicationTeamUpdateForm form,
                                                                             long applicationId) {
        List<ApplicationInviteResource> invites = createInvites(form, applicationId);

        return processAnyFailuresOrSucceed(simpleMap(form.getMarkedForRemoval(), applicationService::removeCollaborator))
                .andOnSuccess(() -> invites.isEmpty() ?
                        serviceSuccess(new InviteResultsResource()) :
                        inviteRestService.createInvitesByOrganisation(organisationId, invites).toServiceResult());
    }

    private ServiceResult<InviteResultsResource> updateInvitesByInviteOrganisation(long inviteOrganisationId,
                                                                                   ApplicationTeamUpdateForm form,
                                                                                   long applicationId) {
        return processAnyFailuresOrSucceed(simpleMap(form.getMarkedForRemoval(), applicationService::removeCollaborator))
                .andOnSuccess(() -> {
                    List<ApplicationInviteResource> invites = createInvites(form, applicationId, inviteOrganisationId);

                    return inviteRestService.saveInvites(invites).toServiceResult();
                });
    }

    private List<ApplicationInviteResource> createInvites(ApplicationTeamUpdateForm applicationTeamUpdateForm,
                                                          long applicationId) {
        return createInvites(applicationTeamUpdateForm, applicationId, null);
    }

    private List<ApplicationInviteResource> createInvites(ApplicationTeamUpdateForm applicationTeamUpdateForm,
                                                          long applicationId,
                                                          Long inviteOrganisationId) {
        return simpleMap(
                applicationTeamUpdateForm.getApplicants(),
                applicantInviteForm -> createInvite(applicantInviteForm, applicationId, inviteOrganisationId)
        );
    }

    private ApplicationInviteResource createInvite(ApplicantInviteForm applicantInviteForm,
                                                   long applicationId,
                                                   Long inviteOrganisationId) {
        ApplicationInviteResource applicationInviteResource = new ApplicationInviteResource(
                applicantInviteForm.getName(),
                applicantInviteForm.getEmail(),
                applicationId
        );
        applicationInviteResource.setInviteOrganisation(inviteOrganisationId);

        return applicationInviteResource;
    }
}
