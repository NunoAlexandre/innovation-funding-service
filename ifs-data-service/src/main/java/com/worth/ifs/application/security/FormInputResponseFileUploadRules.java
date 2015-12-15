package com.worth.ifs.application.security;

import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.worth.ifs.user.domain.UserRoleType.LEADAPPLICANT;

/**
 * Rules defining who is allowed to upload files as part of an Application Form response to a Question
 */
@Component
@PermissionRules
public class FormInputResponseFileUploadRules {

    private static final Log LOG = LogFactory.getLog(FormInputResponseFileUploadRules.class);

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @PermissionRule(value = "UPDATE", description = "An Applicant can upload a file for an answer to one of their own Applications")
    public boolean applicantCanUploadFilesInResponsesForOwnApplication(FormInputResponseFileEntryResource fileEntry, User user) {
        return userIsApplicantOnThisApplication(fileEntry, user);
    }

    @PermissionRule(value = "READ", description = "An Applicant can download a file for an answer to one of their own Applications")
    public boolean applicantCanDownloadFilesInResponsesForOwnApplication(FormInputResponse formInputResponse, User user) {
        return userIsApplicantOnThisApplication(formInputResponse, user);
    }

    private boolean userIsApplicantOnThisApplication(FormInputResponseFileEntryResource fileEntry, User user) {

        FormInputResponse response = formInputResponseRepository.findOne(fileEntry.getFormInputResponseId());

        if (response == null) {
            LOG.warn("Unable to locate FormInputResponse with id " + fileEntry.getFormInputResponseId());
            return false;
        }

        return userIsApplicantOnThisApplication(response, user);
    }

    private boolean userIsApplicantOnThisApplication(FormInputResponse response, User user) {

        Long applicationId = response.getApplication().getId();
        List<ProcessRole> applicantProcessRoles = processRoleRepository.findByUserId(user.getId());

        boolean userIsApplicantOnThisApplication =
            applicantProcessRoles.stream().anyMatch(processRole -> {
                return processRole.getRole().getName().equals(LEADAPPLICANT.getName()) &&
                       processRole.getApplication().getId().equals(applicationId);
            });

        return userIsApplicantOnThisApplication;
    }
}
