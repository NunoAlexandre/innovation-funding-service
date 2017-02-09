package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.assessment.service.CompetitionInviteRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.invite.resource.CompetitionInviteResource;
import org.innovateuk.ifs.invite.resource.ExistingUserStagedInviteResource;
import org.innovateuk.ifs.invite.resource.NewUserStagedInviteListResource;
import org.innovateuk.ifs.invite.resource.NewUserStagedInviteResource;
import org.innovateuk.ifs.management.form.InviteNewAssessorsForm;
import org.innovateuk.ifs.management.form.InviteNewAssessorsRowForm;
import org.innovateuk.ifs.management.model.AssessorProfileModelPopulator;
import org.innovateuk.ifs.management.model.InviteAssessorsFindModelPopulator;
import org.innovateuk.ifs.management.model.InviteAssessorsInviteModelPopulator;
import org.innovateuk.ifs.management.model.InviteAssessorsOverviewModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.innovateuk.ifs.management.controller.CompetitionManagementAssessorProfileController.buildOriginQueryString;

/**
 * This controller will handle all Competition Management requests related to inviting assessors to a Competition.
 */
@Controller
@RequestMapping("/competition/{competitionId}/assessors")
@PreAuthorize("hasAuthority('comp_admin')")
public class CompetitionManagementInviteAssessorsController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionInviteRestService competitionInviteRestService;

    @Autowired
    private InviteAssessorsFindModelPopulator inviteAssessorsFindModelPopulator;

    @Autowired
    private InviteAssessorsInviteModelPopulator inviteAssessorsInviteModelPopulator;

    @Autowired
    private InviteAssessorsOverviewModelPopulator inviteAssessorsOverviewModelPopulator;

    @Autowired
    private AssessorProfileModelPopulator assessorProfileModelPopulator;

    @RequestMapping(method = RequestMethod.GET)
    public String assessors(@PathVariable("competitionId") Long competitionId) {
        return format("redirect:/competition/%s/assessors/find", competitionId);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(Model model,
                       @PathVariable("competitionId") Long competitionId,
                       @RequestParam MultiValueMap<String, String> queryParams) {

        return doViewFind(model, competitionId, queryParams);
    }

    @RequestMapping(value = "/find", params = {"add"}, method = RequestMethod.POST)
    public String addInviteFromFindView(Model model,
                                        @PathVariable("competitionId") Long competitionId,
                                        @RequestParam(name = "add") String email,
                                        @RequestParam MultiValueMap<String, String> queryParams) {
        inviteUser(email, competitionId).getSuccessObjectOrThrowException();
        return doViewFind(model, competitionId, queryParams);
    }

    @RequestMapping(value = "/find", params = {"remove"}, method = RequestMethod.POST)
    public String removeInviteFromFindView(Model model,
                                           @PathVariable("competitionId") Long competitionId,
                                           @RequestParam(name = "remove") String email,
                                           @RequestParam MultiValueMap<String, String> queryParams) {
        deleteInvite(email, competitionId).getSuccessObjectOrThrowException();
        return doViewFind(model, competitionId, queryParams);
    }

    @RequestMapping(value = "/invite", method = RequestMethod.GET)
    public String invite(Model model,
                         @PathVariable("competitionId") Long competitionId,
                         @SuppressWarnings("unused") @ModelAttribute(FORM_ATTR_NAME) InviteNewAssessorsForm form,
                         @RequestParam MultiValueMap<String, String> queryParams
                         ) {
        if (form.getInvites().isEmpty()) {
            form.getInvites().add(new InviteNewAssessorsRowForm());
        }

        return doViewInvite(model, competitionId, queryParams);
    }

    @RequestMapping(value = "/invite", params = {"remove"}, method = RequestMethod.POST)
    public String removeInviteFromInviteView(Model model,
                                             @PathVariable("competitionId") Long competitionId,
                                             @RequestParam(name = "remove") String email,
                                             @RequestParam MultiValueMap<String, String> queryParams,
                                             @SuppressWarnings("unused") @ModelAttribute(FORM_ATTR_NAME) InviteNewAssessorsForm form) {
        deleteInvite(email, competitionId);
        return invite(model, competitionId, form, queryParams);
    }

    @RequestMapping(value = "/invite", params = {"addNewUser"}, method = RequestMethod.POST)
    public String addNewUserToInviteView(Model model,
                                         @PathVariable("competitionId") Long competitionId,
                                         @RequestParam MultiValueMap<String, String> queryParams,
                                         @ModelAttribute(FORM_ATTR_NAME) InviteNewAssessorsForm form) {
        form.getInvites().add(new InviteNewAssessorsRowForm());
        form.setVisible(true);

        return invite(model, competitionId, form, queryParams);
    }

    @RequestMapping(value = "/invite", params = {"removeNewUser"}, method = RequestMethod.POST)
    public String removeNewUserFromInviteView(Model model,
                                              @PathVariable("competitionId") Long competitionId,
                                              @ModelAttribute(FORM_ATTR_NAME) InviteNewAssessorsForm form,
                                              @RequestParam(name = "removeNewUser") int position,
                                              @RequestParam MultiValueMap<String, String> queryParams) {
        form.getInvites().remove(position);
        form.setVisible(true);

        return invite(model, competitionId, form, queryParams);
    }

    @RequestMapping(value = "/invite", params = {"inviteNewUsers"}, method = RequestMethod.POST)
    public String inviteNewsUsersFromInviteView(Model model,
                                                @PathVariable("competitionId") Long competitionId,
                                                @RequestParam MultiValueMap<String, String> queryParams,
                                                @Valid @ModelAttribute(FORM_ATTR_NAME) InviteNewAssessorsForm form,
                                                @SuppressWarnings("unused") BindingResult bindingResult,
                                                ValidationHandler validationHandler) {
        form.setVisible(true);

        return validationHandler.failNowOrSucceedWith(
                () -> invite(model, competitionId, form, queryParams),
                () -> {
                    RestResult<Void> restResult = competitionInviteRestService.inviteNewUsers(
                            newInviteFormToResource(form, competitionId), competitionId
                    );

                    return validationHandler.addAnyErrors(restResult)
                            .failNowOrSucceedWith(
                                    () -> invite(model, competitionId, form, queryParams),
                                    () -> format("redirect:/competition/%s/assessors/invite", competitionId)
                            );
                }
        );
    }

    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public String overview(Model model,
                           @PathVariable("competitionId") Long competitionId,
                           @RequestParam MultiValueMap<String, String> queryParams) {
        CompetitionResource competition = competitionService.getById(competitionId);
        model.addAttribute("model", inviteAssessorsOverviewModelPopulator.populateModel(competition));
        model.addAttribute("originQuery", buildOriginQueryString(CompetitionManagementAssessorProfileController.AssessorProfileOrigin.ASSESSOR_OVERVIEW, queryParams));

        return "assessors/overview";
    }

    private ServiceResult<CompetitionInviteResource> inviteUser(String email, Long competitionId) {
        return competitionInviteRestService.inviteUser(new ExistingUserStagedInviteResource(email, competitionId)).toServiceResult();
    }

    private ServiceResult<Void> deleteInvite(String email, Long competitionId) {
        return competitionInviteRestService.deleteInvite(email, competitionId).toServiceResult();
    }

    private String doViewFind(Model model, Long competitionId, MultiValueMap<String, String> queryParams) {
        CompetitionResource competition = competitionService.getById(competitionId);
        model.addAttribute("model", inviteAssessorsFindModelPopulator.populateModel(competition));
        model.addAttribute("originQuery", buildOriginQueryString(CompetitionManagementAssessorProfileController.AssessorProfileOrigin.ASSESSOR_FIND, queryParams));

        return "assessors/find";
    }

    private String doViewInvite(Model model, Long competitionId, MultiValueMap<String, String> queryParams) {
        CompetitionResource competition = competitionService.getById(competitionId);
        model.addAttribute("model", inviteAssessorsInviteModelPopulator.populateModel(competition));
        model.addAttribute("originQuery", buildOriginQueryString(CompetitionManagementAssessorProfileController.AssessorProfileOrigin.ASSESSOR_INVITE, queryParams));

        return "assessors/invite";
    }

    private NewUserStagedInviteListResource newInviteFormToResource(InviteNewAssessorsForm form, Long competitionId) {
        List<NewUserStagedInviteResource> invites = form.getInvites().stream()
                .map(newUserInvite -> new NewUserStagedInviteResource(
                        newUserInvite.getEmail(),
                        competitionId,
                        newUserInvite.getName(),
                        form.getSelectedInnovationArea()
                ))
                .collect(Collectors.toList());

        return new NewUserStagedInviteListResource(invites);
    }
}
