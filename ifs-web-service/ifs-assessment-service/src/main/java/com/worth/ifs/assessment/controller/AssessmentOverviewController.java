package com.worth.ifs.assessment.controller;

import com.worth.ifs.application.AbstractApplicationController;
import com.worth.ifs.assessment.form.AssessmentOverviewForm;
import com.worth.ifs.assessment.model.AssessmentFinancesSummaryModelPopulator;
import com.worth.ifs.assessment.model.AssessmentOverviewModelPopulator;
import com.worth.ifs.assessment.model.RejectAssessmentModelPopulator;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.controller.ValidationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

@Controller
@RequestMapping(value = "/{assessmentId}")
public class AssessmentOverviewController extends AbstractApplicationController {

    @Autowired
    private AssessmentOverviewModelPopulator assessmentOverviewModelPopulator;

    @Autowired
    private AssessmentFinancesSummaryModelPopulator assessmentFinancesSummaryModelPopulator;

    @Autowired
    private RejectAssessmentModelPopulator rejectAssessmentModelPopulator;

    @Autowired
    private AssessmentService assessmentService;

    @RequestMapping(method = RequestMethod.GET)
    public String getOverview(Model model, AssessmentOverviewForm form, @PathVariable("assessmentId") Long assessmentId,
                              HttpServletRequest request) throws InterruptedException, ExecutionException {

        Long userId = userAuthenticationService.getAuthenticatedUser(request).getId();
        assessmentOverviewModelPopulator.populateModel(assessmentId, userId, form, model);

        return "assessment/application-overview";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/finances")
    public String getFinancesSummary(Model model, @PathVariable("assessmentId") Long assessmentId) throws InterruptedException, ExecutionException {

        assessmentFinancesSummaryModelPopulator.populateModel(assessmentId, model);

        return "assessment/application-finances-summary";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reject")
    public String rejectInvitation(
            @Valid @ModelAttribute(MODEL_ATTRIBUTE_FORM) AssessmentOverviewForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            @PathVariable("assessmentId") Long assessmentId) {
        AssessmentResource assessment = assessmentService.getById(assessmentId);
        ServiceResult<Void> result = assessmentService.rejectInvitation(assessment.getId(), form.getRejectReason(), form.getRejectComment());
        return redirectToAssessorCompetitionDashboard(assessment.getCompetition());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reject/confirm")
    public String rejectInvitationConfirm(
            Model model,
            @ModelAttribute(MODEL_ATTRIBUTE_FORM) AssessmentOverviewForm form,
            @PathVariable("assessmentId") Long assessmentId) {

        model.addAttribute("model", rejectAssessmentModelPopulator.populateModel(assessmentId));
        return "assessment/reject-invitation-confirm";
    }

    private String redirectToAssessorCompetitionDashboard(Long competitionId) {
        return format("redirect:/assessor/dashboard/competition/%s", competitionId);
    }
}
