package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.assessment.resource.AssessmentCreateResource;
import org.innovateuk.ifs.assessment.service.AssessmentRestService;
import org.innovateuk.ifs.competition.resource.AvailableAssessorsSortFieldType;
import org.innovateuk.ifs.management.form.AvailableAssessorsForm;
import org.innovateuk.ifs.management.model.ApplicationAssessmentProgressModelPopulator;
import org.innovateuk.ifs.management.viewmodel.ApplicationAssessmentProgressRemoveViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static java.lang.String.format;
import static org.innovateuk.ifs.management.controller.CompetitionManagementAssessorProfileController.buildOriginQueryString;

/**
 * This controller will handle all Competition Management requests related to allocating assessors to an Application.
 */
@Controller
@RequestMapping("/competition/{competitionId}/application/{applicationId}/assessors")
@PreAuthorize("hasAnyAuthority('comp_admin','project_finance')")
public class CompetitionManagementApplicationAssessmentProgressController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private ApplicationAssessmentProgressModelPopulator applicationAssessmentProgressModelPopulator;

    @Autowired
    private AssessmentRestService assessmentRestService;

    @RequestMapping(method = RequestMethod.GET)
    public String applicationProgress(Model model,
                                      @Valid @ModelAttribute(FORM_ATTR_NAME) AvailableAssessorsForm form,
                                      @SuppressWarnings("unused") BindingResult bindingResult,
                                      @PathVariable("applicationId") Long applicationId,
                                      @RequestParam MultiValueMap<String, String> queryParams) {
        return doProgressView(model, applicationId, form.getSortField(), queryParams);
    }

    @RequestMapping(path = "/assign/{assessorId}", method = RequestMethod.POST)
    public String assignAssessor(@PathVariable("competitionId") Long competitionId,
                                 @PathVariable("applicationId") Long applicationId,
                                 @PathVariable("assessorId") Long assessorId,
                                 @RequestParam(value = "sortField", defaultValue = "TITLE") String sortField) {
        assessmentRestService.createAssessment(new AssessmentCreateResource(applicationId, assessorId)).getSuccessObjectOrThrowException();
        return format("redirect:/competition/%s/application/%s/assessors?sortField=%s", competitionId, applicationId, sortField);
    }

    @RequestMapping(value = "/withdraw/{assessmentId}", method = RequestMethod.POST)
    public String withdrawAssessment(@PathVariable("competitionId") Long competitionId,
                                     @PathVariable("applicationId") Long applicationId,
                                     @PathVariable("assessmentId") Long assessmentId,
                                     @RequestParam(value = "sortField", defaultValue = "TITLE") String sortField) {
        assessmentRestService.withdrawAssessment(assessmentId).getSuccessObjectOrThrowException();
        return format("redirect:/competition/%s/application/%s/assessors?sortField=%s", competitionId, applicationId, sortField);
    }

    @RequestMapping(value = "/withdraw/{assessmentId}/confirm", method = RequestMethod.GET)
    public String withdrawAssessmentConfirm(
            Model model,
            @PathVariable("competitionId") Long competitionId,
            @PathVariable("applicationId") Long applicationId,
            @PathVariable("assessmentId") Long assessmentId,
            @RequestParam(value = "sortField", defaultValue = "TITLE") String sortField) {
        model.addAttribute("model", new ApplicationAssessmentProgressRemoveViewModel(
                competitionId,
                applicationId,
                assessmentId,
                AvailableAssessorsSortFieldType.valueOf(sortField)
        ));
        return "competition/application-progress-remove-confirm";
    }

    private String doProgressView(Model model, Long applicationId, AvailableAssessorsSortFieldType sort, MultiValueMap<String, String> queryParams) {
        model.addAttribute("model", applicationAssessmentProgressModelPopulator.populateModel(applicationId, sort));
        queryParams.add("applicationId", applicationId.toString());
        model.addAttribute("originQuery", buildOriginQueryString(CompetitionManagementAssessorProfileController.AssessorProfileOrigin.APPLICATION_PROGRESS, queryParams));
        return "competition/application-progress";
    }
}
