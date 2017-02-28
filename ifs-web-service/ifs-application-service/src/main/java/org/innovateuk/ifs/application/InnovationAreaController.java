package org.innovateuk.ifs.application;

import org.innovateuk.ifs.application.form.InnovationAreaForm;
import org.innovateuk.ifs.application.populator.ApplicationInnovationAreaPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationInnovationAreaRestService;
import org.innovateuk.ifs.application.viewmodel.InnovationAreaViewModel;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * This controller handles requests by Applicants to change the Innovation Area choice for an Application.
 */
@Controller
@RequestMapping(ApplicationFormController.APPLICATION_BASE_URL+"{applicationId}/form/question/{questionId}/innovation-area")
@PreAuthorize("hasAuthority('applicant')")
public class InnovationAreaController {
    private static String APPLICATION_SAVED_MESSAGE = "applicationSaved";

    @Autowired
    private ApplicationInnovationAreaPopulator innovationAreaPopulator;

    @Autowired
    private ApplicationInnovationAreaRestService applicationInnovationAreaRestService;

    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @GetMapping
    public String getInnovationAreas(Model model, @PathVariable Long applicationId, @PathVariable Long questionId) {
        InnovationAreaViewModel innovationAreaViewModel = innovationAreaPopulator.populate(applicationId, questionId);

        model.addAttribute("model", innovationAreaViewModel);

        return "application/innovation-areas";
    }

    @PostMapping
    public String submitInnovationAreaChoice(@ModelAttribute("form") @Valid InnovationAreaForm innovationAreaForm, HttpServletResponse response,
                                             BindingResult bindingResult, Model model, @PathVariable Long applicationId, @PathVariable Long questionId) {

        saveInnovationAreaChoice(applicationId, innovationAreaForm);

        InnovationAreaViewModel innovationAreaViewModel = innovationAreaPopulator.populate(applicationId, questionId);

        model.addAttribute("model", innovationAreaViewModel);
        if(bindingResult.hasErrors()) {
            return "application/innovation-areas";
        }
        else {
            cookieFlashMessageFilter.setFlashMessage(response, APPLICATION_SAVED_MESSAGE);
            return "redirect:/application/"+applicationId+"/form/question/"+questionId;
        }
    }

    private RestResult<ApplicationResource> saveInnovationAreaChoice(Long applicationId, InnovationAreaForm innovationAreaForm) {
        if(innovationAreaForm.getInnovationAreaChoice().equals("NOT_APPLICABLE")) {
            return applicationInnovationAreaRestService.setApplicationInnovationAreaToNotApplicable(applicationId);
        }
        else {
            Long innovationAreaId = Long.valueOf(innovationAreaForm.getInnovationAreaChoice());
            return applicationInnovationAreaRestService.saveApplicationInnovationAreaChoice(applicationId, innovationAreaId);
        }
    }
}
