package org.innovateuk.ifs.application.areas.controller;

import org.innovateuk.ifs.application.areas.form.ResearchCategoryForm;
import org.innovateuk.ifs.application.areas.populator.ApplicationResearchCategoryPopulator;
import org.innovateuk.ifs.application.areas.viewmodel.ResearchCategoryViewModel;
import org.innovateuk.ifs.application.forms.validator.ApplicationDetailsEditableValidator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationResearchCategoryRestService;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.error.exception.ForbiddenActionException;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;

/**
 * This controller handles requests by Applicants to change the research category choice for an Application.
 */
@Controller
@RequestMapping(APPLICATION_BASE_URL+"{applicationId}/form/question/{questionId}/research-category")
@PreAuthorize("hasAuthority('applicant')")
public class ResearchCategoryController {
    private static String APPLICATION_SAVED_MESSAGE = "applicationSaved";

    @Autowired
    private ApplicationResearchCategoryPopulator researchCategoryPopulator;

    @Autowired
    private ApplicationResearchCategoryRestService applicationResearchCategoryRestService;

    @Autowired
    private ApplicationDetailsEditableValidator applicationDetailsEditableValidator;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @GetMapping
    public String getResearchCategories(Model model, @PathVariable Long applicationId, @PathVariable Long questionId,
                                        HttpServletRequest request) {
        ApplicationResource applicationResource = applicationService.getById(applicationId);

        checkIfAllowed(questionId, applicationResource);

        if (!applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)) {
            throw new ForbiddenActionException();
        }

        ResearchCategoryViewModel researchCategoryViewModel = researchCategoryPopulator.populate(applicationResource, questionId);

        model.addAttribute("model", researchCategoryViewModel);
        model.addAttribute("form", new ResearchCategoryForm());

        return "application/research-categories";
    }

    @PostMapping
    public String submitResearchCategoryChoice(@ModelAttribute("form") @Valid ResearchCategoryForm researchCategoryForm,
                                               BindingResult bindingResult,
                                               HttpServletResponse response,
                                               ValidationHandler validationHandler,
                                               Model model, @PathVariable Long applicationId, @PathVariable Long questionId) {
        ApplicationResource applicationResource = applicationService.getById(applicationId);

        checkIfAllowed(questionId, applicationResource);

        ResearchCategoryViewModel researchCategoryViewModel = researchCategoryPopulator.populate(applicationResource, questionId);

        model.addAttribute("model", researchCategoryViewModel);

        Supplier<String> failureView = () -> "application/research-categories";

        return validationHandler.addAnyErrors(saveResearchCategoryChoice(applicationId, researchCategoryForm))
            .failNowOrSucceedWith(failureView, () -> {
                cookieFlashMessageFilter.setFlashMessage(response, APPLICATION_SAVED_MESSAGE);
                return "redirect:/application/"+applicationId+"/form/question/"+questionId;
        });
    }

    private ServiceResult<ApplicationResource> saveResearchCategoryChoice(Long applicationId, ResearchCategoryForm researchCategoryForm) {
        if(null != researchCategoryForm.getResearchCategoryChoice()) {
            Long researchCategoryId = Long.valueOf(researchCategoryForm.getResearchCategoryChoice());
            return applicationResearchCategoryRestService.saveApplicationResearchCategoryChoice(applicationId, researchCategoryId).toServiceResult();
        }

        return ServiceResult.serviceFailure(emptyList());
    }

    private void checkIfAllowed(Long questionId, ApplicationResource applicationResource) throws ForbiddenActionException {
        if(!applicationDetailsEditableValidator.questionAndApplicationHaveAllowedState(questionId, applicationResource)) {
            throw new ForbiddenActionException();
        }
    }
}
