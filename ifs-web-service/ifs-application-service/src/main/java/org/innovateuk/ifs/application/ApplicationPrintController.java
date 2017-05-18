package org.innovateuk.ifs.application;

import org.innovateuk.ifs.application.populator.ApplicationPrintPopulator;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller will handle the print requests that are related to the application overview.
 */

@Controller
@RequestMapping("/application")
@PreAuthorize("hasAuthority('applicant')")
public class ApplicationPrintController {
    public static final String ASSIGN_QUESTION_PARAM = "assign_question";
    public static final String MARK_AS_COMPLETE = "mark_as_complete";

    @Autowired
    private ApplicationPrintPopulator applicationPrintPopulator;

    /**
     * Printable version of the application
     */
    @GetMapping(value = "/{applicationId}/print")
    public String printApplication(@PathVariable("applicationId") long applicationId,
                                   Model model,
                                   UserResource user) {
        return applicationPrintPopulator.print(applicationId, model, user);
    }
}
