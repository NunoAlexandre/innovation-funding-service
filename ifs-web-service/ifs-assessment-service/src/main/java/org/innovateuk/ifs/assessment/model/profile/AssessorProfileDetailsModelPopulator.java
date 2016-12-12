package org.innovateuk.ifs.assessment.model.profile;

import org.innovateuk.ifs.assessment.viewmodel.profile.AssessorProfileDetailsViewModel;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Build the model for the Assessor Details view.
 */
@Component
public class AssessorProfileDetailsModelPopulator {

    @Autowired
    UserService userService;

    public AssessorProfileDetailsViewModel populateModel(UserResource user) {
        return new AssessorProfileDetailsViewModel(userService.getUserProfile(user.getId()));
    }
}
