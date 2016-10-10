package com.worth.ifs.assessment.controller.profile;

import com.worth.ifs.assessment.form.AssessorRegistrationSkillsForm;
import com.worth.ifs.assessment.model.profile.AssessorProfileSkillsModelPopulator;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.user.resource.ProfileResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.function.Supplier;

import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static com.worth.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

/**
 * Controller to manage the Assessor Profile Skills page
 */
@Controller
@RequestMapping("/profile/skills")
public class AssessorProfileSkillsController {

    @Autowired
    private AssessorProfileSkillsModelPopulator assessorSkillsModelPopulator;

    @Autowired
    private UserService userService;

    private static final String FORM_ATTR_NAME = "form";

    @RequestMapping(method = RequestMethod.GET)
    public String getSkills(Model model, @ModelAttribute(FORM_ATTR_NAME) AssessorRegistrationSkillsForm form) {
        return doViewYourSkills(model);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String submitSkills(Model model,
                               @ModelAttribute("loggedInUser") UserResource loggedInUser,
                               @Valid @ModelAttribute(FORM_ATTR_NAME) AssessorRegistrationSkillsForm form,
                               @SuppressWarnings("unused") BindingResult bindingResult,
                               ValidationHandler validationHandler
    ) {

        Supplier<String> failureView = () -> doViewYourSkills(model);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ProfileResource profile = new ProfileResource();
            profile.setBusinessType(form.getAssessorType());
            profile.setSkillsAreas(form.getSkillAreas());
            ServiceResult<UserResource> result = userService.updateProfile(loggedInUser.getId(), profile);
            return validationHandler.addAnyErrors(result, fieldErrorsToFieldErrors(), asGlobalErrors()).
                    failNowOrSucceedWith(failureView, () -> "redirect:/profile/declaration");
        });

    }

    private String doViewYourSkills(Model model) {
        model.addAttribute("model", assessorSkillsModelPopulator.populateModel());
        return "profile/innovation-areas";
    }
}
