package com.worth.ifs.application.security;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.security.BasePermissionRules;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

@Component
@PermissionRules
public class SectionPermissionRules extends BasePermissionRules {
    @PermissionRule(value = "READ", description = "everyone can read sections")
    public boolean userCanReadSection(SectionResource section, UserResource user) {
        return true;
    }

    @PermissionRule(value = "UPDATE", description = "no one can update sections yet")
    public boolean userCanUpdateSection(SectionResource section, UserResource user) {
        return false;
    }

    @PermissionRule(value = "MARK_SECTION_AS_COMPLETE", description = "Only lead Applicant can mark a section as complete")
    public boolean onlyLeadApplicantCanMarkSectionAsComplete(ApplicationResource applicationResource, UserResource user) {
        return isLeadApplicant(applicationResource.getId(), user);
    }

    @PermissionRule(value = "MARK_SECTION_AS_INCOMPLETE", description = "Only lead Applicant can mark a section as incomplete")
    public boolean onlyLeadApplicantCanMarkSectionAsInComplete(ApplicationResource applicationResource, UserResource user) {
        return isLeadApplicant(applicationResource.getId(), user);
    }

}
