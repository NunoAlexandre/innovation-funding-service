package com.worth.ifs.application.security;

import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.worth.ifs.user.domain.UserRoleType.APPLICANT;
import static com.worth.ifs.user.domain.UserRoleType.COLLABORATOR;
import static com.worth.ifs.user.domain.UserRoleType.LEADAPPLICANT;

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

    @PermissionRule(value = "UPDATE", description = "An Applicant can upload a file for an answer to one of their own Applications")
    public boolean applicantCanUploadFilesInResponsesForOwnApplication(FormInputResponseFileEntryResource fileEntry, User user) {
        return userIsApplicantOnThisApplication(fileEntry, user);
    }

    @PermissionRule(value = "READ", description = "An Applicant can download a file for an answer to one of their own Applications")
    public boolean applicantCanDownloadFilesInResponsesForOwnApplication(FormInputResponseFileEntryResource fileEntry, User user) {
        return userIsApplicantOnThisApplication(fileEntry, user);
    }

    private boolean userIsApplicantOnThisApplication(FormInputResponseFileEntryResource fileEntry, User user) {
        return userIsApplicantOnThisApplication(fileEntry.getCompoundId().getApplicationId(), user);
    }

    private boolean userIsApplicantOnThisApplication(long applicationId, User user) {
        List<Role> allApplicantRoles = roleRepository.findByNameIn(Arrays.asList(APPLICANT.getName(), LEADAPPLICANT.getName(), COLLABORATOR.getName()));
        List<ProcessRole> applicantProcessRoles = processRoleRepository.findByUserIdAndRoleInAndApplicationId(user.getId(), allApplicantRoles, applicationId);
        return !applicantProcessRoles.isEmpty();
    }
}
