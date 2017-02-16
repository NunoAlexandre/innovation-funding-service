package org.innovateuk.ifs.assessment.controller.profile;

import org.innovateuk.ifs.assessment.form.profile.AssessorProfileSkillsForm;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileEditSkillsModelPopulator;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileSkillsModelPopulator;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.user.resource.ProfileSkillsResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.function.Supplier;

import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

/**
 * Controller to manage the Assessor Profile Skills page
 */
@Controller
@RequestMapping("/profile/skills")
@PreAuthorize("hasAuthority('assessor')")
public class AssessorProfileSkillsController {

    @Autowired
    private UserService userService;

    @Autowired
    private AssessorProfileSkillsModelPopulator assessorProfileSkillsModelPopulator;

    @Autowired
    private AssessorProfileEditSkillsModelPopulator assessorProfileEditSkillsModelPopulator;

    private static final String FORM_ATTR_NAME = "form";

    @RequestMapping(method = RequestMethod.GET)
    public String getReadonlySkills(Model model,
                                    @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        model.addAttribute("model", assessorProfileSkillsModelPopulator.populateModel(loggedInUser.getId()));
        return "profile/skills";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit")
    public String getSkills(Model model,
                            @ModelAttribute("loggedInUser") UserResource loggedInUser,
                            @ModelAttribute(FORM_ATTR_NAME) AssessorProfileSkillsForm form,
                            BindingResult bindingResult) {
        return doViewEditSkills(model, loggedInUser, form, bindingResult);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit")
    public String submitSkills(Model model,
                               @ModelAttribute("loggedInUser") UserResource loggedInUser,
                               @Valid @ModelAttribute(FORM_ATTR_NAME) AssessorProfileSkillsForm form,
                               BindingResult bindingResult,
                               ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> doViewEditSkills(model, loggedInUser, form, bindingResult);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<Void> result = userService.updateProfileSkills(loggedInUser.getId(), form.getAssessorType(), form.getSkillAreas());

            return validationHandler.addAnyErrors(result, fieldErrorsToFieldErrors(), asGlobalErrors())
                    .failNowOrSucceedWith(failureView, () -> "redirect:/profile/skills");
        });
    }

    private String doViewEditSkills(Model model, UserResource loggedInUser,
                                    AssessorProfileSkillsForm form, BindingResult bindingResult) {
        ProfileSkillsResource profileSkillsResource = userService.getProfileSkills(loggedInUser.getId());
        if (!bindingResult.hasErrors()) {
            populateFormWithExistingValues(profileSkillsResource, form);
        }
        model.addAttribute("model", assessorProfileEditSkillsModelPopulator.populateModel(profileSkillsResource));
        return "profile/skills-edit";
    }

    private void populateFormWithExistingValues(ProfileSkillsResource profileSkillsResource, AssessorProfileSkillsForm form) {
        form.setAssessorType(profileSkillsResource.getBusinessType());
        form.setSkillAreas(profileSkillsResource.getSkillsAreas());
    }
}
