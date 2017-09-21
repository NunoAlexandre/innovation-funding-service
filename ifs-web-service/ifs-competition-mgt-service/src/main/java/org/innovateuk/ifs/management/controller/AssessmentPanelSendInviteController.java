package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.assessment.service.AssessmentPanelInviteRestService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.invite.resource.AssessorInviteSendResource;
import org.innovateuk.ifs.invite.resource.AssessorInvitesToSendResource;
import org.innovateuk.ifs.management.form.SendInviteForm;
import org.innovateuk.ifs.management.viewmodel.SendInvitesViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

/**
 * This controller will handle all Competition Management requests related to sending assessment panel invites to assessors
 */

@Controller
@RequestMapping("/panel/competition/{competitionId}/assessors/invite")
@PreAuthorize("hasAnyAuthority('comp_admin','project_finance')")
public class AssessmentPanelSendInviteController {

    @Autowired
    AssessmentPanelInviteRestService assessmentPanelInviteRestService;
    @GetMapping("/send")
    public String getInvitesToSend(Model model,
                                   @PathVariable("competitionId") long competitionId,
                                   @ModelAttribute(name = "form", binding = false) SendInviteForm form,
                                   BindingResult bindingResult) {
        AssessorInvitesToSendResource invites = assessmentPanelInviteRestService.getAllInvitesToSend(competitionId).getSuccessObjectOrThrowException();

        if (invites.getRecipients().isEmpty()) {
            return redirectToPanelInviteListView(competitionId);
        }

        model.addAttribute("model", new SendInvitesViewModel(
                invites.getCompetitionId(),
                invites.getCompetitionName(),
                invites.getRecipients(),
                invites.getContent()
        ));

        if (!bindingResult.hasErrors()) {
            populateGroupInviteFormWithExistingValues(form, invites);
        }

        return "assessors/send-invites";
    }

    @PostMapping("/send")
    public String sendInvites(Model model,
                              @PathVariable("competitionId") long competitionId,
                              @ModelAttribute("form") @Valid SendInviteForm form,
                              BindingResult bindingResult,
                              ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> getInvitesToSend(model, competitionId, form, bindingResult);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<Void> sendResult = assessmentPanelInviteRestService.sendAllInvites(
                    competitionId,
                    new AssessorInviteSendResource(form.getSubject(), form.getContent())
            )
                    .toServiceResult();

            return validationHandler.addAnyErrors(sendResult, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(failureView, () -> redirectToPanelInviteListView(competitionId));
        });
    }

    private void populateGroupInviteFormWithExistingValues(SendInviteForm form, AssessorInvitesToSendResource assessorInviteToSendResource) {
        form.setSubject(format("Invitation to assess '%s'", assessorInviteToSendResource.getCompetitionName()));
    }


    private String redirectToPanelInviteListView(long competitionId) {
        return format("redirect:/panel/competition/%s/assessors/invite", competitionId);
    }
}
