package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.assessment.form.AssessmentSummaryForm;
import org.innovateuk.ifs.assessment.model.AssessmentSummaryModelPopulator;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.service.AssessmentService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.workflow.ProcessOutcomeService;
import org.innovateuk.ifs.workflow.resource.ProcessOutcomeResource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.Optional;
import java.util.function.Supplier;

import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

@Controller
@PreAuthorize("hasAuthority('assessor')")
public class AssessmentSummaryController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private ProcessOutcomeService processOutcomeService;

    @Autowired
    private AssessmentSummaryModelPopulator assessmentSummaryModelPopulator;

    @RequestMapping(value = "/{assessmentId}/summary", method = RequestMethod.GET)
    public String getSummary(Model model,
                             @ModelAttribute(FORM_ATTR_NAME) AssessmentSummaryForm form,
                             BindingResult bindingResult,
                             @PathVariable("assessmentId") Long assessmentId) {
        if (!bindingResult.hasErrors()) {
            populateFormWithExistingValues(form, assessmentId);
        }
        model.addAttribute("model", assessmentSummaryModelPopulator.populateModel(assessmentId));
        return "assessment/application-summary";
    }

    @RequestMapping(value = "/{assessmentId}/summary", method = RequestMethod.POST)
    public String save(Model model,
                       @Valid @ModelAttribute(FORM_ATTR_NAME) AssessmentSummaryForm form,
                       BindingResult bindingResult,
                       ValidationHandler validationHandler,
                       @PathVariable("assessmentId") Long assessmentId) {

        Supplier<String> failureView = () -> getSummary(model, form, bindingResult, assessmentId);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<Void> updateResult = assessmentService.recommend(assessmentId, form.getFundingConfirmation(), form.getFeedback(), form.getComment());
            return validationHandler.addAnyErrors(updateResult, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(failureView, () -> redirectToCompetitionOfAssessment(assessmentId));
        });
    }

    private String redirectToCompetitionOfAssessment(Long assessmentId) {
        return "redirect:/assessor/dashboard/competition/" + getAssessment(assessmentId).getCompetition();
    }

    private void populateFormWithExistingValues(AssessmentSummaryForm form, Long assessmentId) {
        getOutcome(assessmentId).ifPresent(outcome -> {
            form.setFundingConfirmation(Optional.ofNullable(outcome.getOutcome()).map(BooleanUtils::toBoolean).orElse(null));
            form.setFeedback(outcome.getDescription());
            form.setComment(outcome.getComment());
        });
    }

    private Optional<ProcessOutcomeResource> getOutcome(Long assessmentId) {
        return getAssessment(assessmentId).getProcessOutcomes().stream().reduce((id1, id2) -> id2).map(id -> processOutcomeService.getById(id));
    }

    private AssessmentResource getAssessment(Long assessmentId) {
        return assessmentService.getById(assessmentId);
    }
}
