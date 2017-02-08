package org.innovateuk.ifs.application.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource;
import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.repository.RoleRepository;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.innovateuk.ifs.security.SecurityRuleUtil.isInternal;
import static org.innovateuk.ifs.user.resource.UserRoleType.*;

/**
 * Rules defining who is allowed to upload files as part of an Application Form response to a Question
 */
@Component
@PermissionRules
public class FormInputResponseFileUploadRules {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(FormInputResponseFileUploadRules.class);

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @PermissionRule(value = "UPDATE", description = "An Applicant can upload a file for an answer to one of their own Applications")
    public boolean applicantCanUploadFilesInResponsesForOwnApplication(FormInputResponseFileEntryResource fileEntry, UserResource user) {
        Application application = applicationRepository.findOne(fileEntry.getCompoundId().getApplicationId());
        return userIsApplicantOnThisApplication(fileEntry, user) && application.isOpen();
    }

    @PermissionRule(value = "READ", description = "An Applicant can download a file for an answer to one of their own Applications")
    public boolean applicantCanDownloadFilesInResponsesForOwnApplication(FormInputResponseFileEntryResource fileEntry, UserResource user) {
        return userIsApplicantOnThisApplication(fileEntry, user);
    }

    @PermissionRule(value = "READ", description = "An internal user can download a file for an answer")
    public boolean internalUserCanDownloadFilesInResponses(FormInputResponseFileEntryResource fileEntry, UserResource user) {
        return isInternal(user);
    }


    private boolean userIsApplicantOnThisApplication(FormInputResponseFileEntryResource fileEntry, UserResource user) {
        return userIsApplicantOnThisApplication(fileEntry.getCompoundId().getApplicationId(), user);
    }

    private boolean userIsApplicantOnThisApplication(long applicationId, UserResource user) {
        List<Role> allApplicantRoles = roleRepository.findByNameIn(Arrays.asList(APPLICANT.getName(), LEADAPPLICANT.getName(), COLLABORATOR.getName()));
        List<ProcessRole> applicantProcessRoles = processRoleRepository.findByUserIdAndRoleInAndApplicationId(user.getId(), allApplicantRoles, applicationId);
        return !applicantProcessRoles.isEmpty();
    }
}
