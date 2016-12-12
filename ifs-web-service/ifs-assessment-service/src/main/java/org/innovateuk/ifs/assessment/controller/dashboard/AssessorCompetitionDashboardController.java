package org.innovateuk.ifs.assessment.controller.dashboard;

import org.innovateuk.ifs.assessment.form.dashboard.AssessorCompetitionDashboardAssessmentForm;
import org.innovateuk.ifs.assessment.model.AssessorCompetitionDashboardModelPopulator;
import org.innovateuk.ifs.assessment.service.AssessmentService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.function.Supplier;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;

/**
 * This controller will handle all requests that are related to the assessor competition dashboard.
 */
@Controller
@RequestMapping(value = "/assessor")
public class AssessorCompetitionDashboardController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private AssessorCompetitionDashboardModelPopulator assessorCompetitionDashboardModelPopulator;

    @Autowired
    private AssessmentService assessmentService;

    @RequestMapping(value = "/dashboard/competition/{competitionId}", method = RequestMethod.GET)
    public String competitionDashboard(final Model model,
                                       @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                       @PathVariable("competitionId") final Long competitionId,
                                       @ModelAttribute(FORM_ATTR_NAME) AssessorCompetitionDashboardAssessmentForm form) {

        model.addAttribute("model", assessorCompetitionDashboardModelPopulator.populateModel(competitionId, loggedInUser.getId()));
        return "assessor-competition-dashboard";
    }

    @RequestMapping(value = "/dashboard/competition/{competitionId}", method = RequestMethod.POST)
    public String submitAssessments(Model model,
                                    @PathVariable("competitionId") Long competitionId,
                                    @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                    @ModelAttribute(FORM_ATTR_NAME) @Valid AssessorCompetitionDashboardAssessmentForm form,
                                    BindingResult bindingResult,
                                    ValidationHandler validationHandler) {

        Supplier<String> renderDashboard = () -> competitionDashboard(model, loggedInUser, competitionId, form);

        return validationHandler.failNowOrSucceedWith(
                renderDashboard,
                () -> {
                    ServiceResult<Void> serviceResult = assessmentService.submitAssessments(form.getAssessmentIds());

                    return validationHandler.addAnyErrors(serviceResult, asGlobalErrors())
                            .failNowOrSucceedWith(renderDashboard, renderDashboard);
                }
        );
    }

    @RequestMapping(value = "/dashboard/confirm-competition/{competitionId}", method = RequestMethod.POST)
    public String confirmSubmitAssessments(Model model,
                                           @PathVariable("competitionId") final Long competitionId,
                                           @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                           @ModelAttribute(FORM_ATTR_NAME) @Valid AssessorCompetitionDashboardAssessmentForm form,
                                           BindingResult bindingResult,
                                           ValidationHandler validationHandler) {


        Supplier<String> renderDashboard = () -> competitionDashboard(model, loggedInUser, competitionId, form);

        return validationHandler.failNowOrSucceedWith(
                renderDashboard,
                () -> {
                    model.addAttribute("competitionId", competitionId);

                    return "assessment/assessment-submit-confirm";
                }
        );

    }
}
