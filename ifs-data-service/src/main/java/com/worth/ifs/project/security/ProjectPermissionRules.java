package com.worth.ifs.project.security;

import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.security.BasePermissionRules;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

import static com.worth.ifs.security.SecurityRuleUtil.isCompAdmin;
import static com.worth.ifs.security.SecurityRuleUtil.isProjectFinanceUser;

@PermissionRules
@Component
public class ProjectPermissionRules extends BasePermissionRules {

    @PermissionRule(value = "READ", description = "A user can see projects that they are partners on")
    public boolean partnersOnProjectCanView(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(value = "READ", description = "Comp admins can see project resources")
    public boolean compAdminsCanViewProjects(final ProjectResource project, final UserResource user){
        return isCompAdmin(user);
    }

    @PermissionRule(value = "READ", description = "Project finance users can see project resources")
    public boolean projectFinanceUsersCanViewProjects(final ProjectResource project, final UserResource user){
        return isProjectFinanceUser(user);
    }

    @PermissionRule(
            value = "UPDATE_BASIC_PROJECT_SETUP_DETAILS",
            description = "The lead partners can update the basic project details, like start date, address, project manager")
    public boolean leadPartnersCanUpdateTheBasicProjectDetails(ProjectResource project, UserResource user) {
        return isLeadPartner(project.getId(), user.getId());
    }


    @PermissionRule(
            value = "UPDATE_FINANCE_CONTACT",
            description = "The lead partner can update the basic project details like start date")
    public boolean partnersCanUpdateTheirOwnOrganisationsFinanceContacts(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "INVITE_FINANCE_CONTACT",
            description = "A partner can invite a member of their organisation to become a finance contact")
    public boolean partnersCanInviteTheirOwnOrganisationsFinanceContacts(InviteResource invite, UserResource user) {
        return isSpecificProjectPartnerByApplicationId(invite.getApplication(), invite.getInviteOrganisation(), user.getId());
    }

    @PermissionRule(
            value = "VIEW_MONITORING_OFFICER",
            description = "Comp admins can view Monitoring Officers on any Project")
    public boolean compAdminsCanViewMonitoringOfficersForAnyProject(ProjectResource project, UserResource user) {
        return isCompAdmin(user);
    }

    @PermissionRule(
            value = "VIEW_MONITORING_OFFICER",
            description = "Partners can view monitoring officers on Projects that they are partners on")
    public boolean partnersCanViewMonitoringOfficersOnTheirProjects(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "ASSIGN_MONITORING_OFFICER",
            description = "Comp admins can assign Monitoring Officers on any Project")
    public boolean compAdminsCanAssignMonitoringOfficersForAnyProject(ProjectResource project, UserResource user) {
        return isCompAdmin(user);
    }

    @PermissionRule(
            value = "UPLOAD_OTHER_DOCUMENTS",
            description = "The lead partners can upload Other Documents (Collaboration Agreement, Exploitation Plan) for their Projects")
    public boolean leadPartnersCanUploadOtherDocuments(ProjectResource project, UserResource user) {
        return isLeadPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "DOWNLOAD_OTHER_DOCUMENTS",
            description = "Partners can download Other Documents (Collaboration Agreement, Exploitation Plan) that their lead partners have uploaded")
    public boolean partnersCanDownloadOtherDocuments(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "VIEW_OTHER_DOCUMENTS_DETAILS",
            description = "Partners can view Other Documents (Collaboration Agreement, Exploitation Plan) details that their lead partners have uploaded")
    public boolean partnersCanViewOtherDocumentsDetails(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "DELETE_OTHER_DOCUMENTS",
            description = "The lead partners can delete Other Documents (Collaboration Agreement, Exploitation Plan) for their Projects")
    public boolean leadPartnersCanDeleteOtherDocuments(ProjectResource project, UserResource user) {
        return isLeadPartner(project.getId(), user.getId());
    }
}