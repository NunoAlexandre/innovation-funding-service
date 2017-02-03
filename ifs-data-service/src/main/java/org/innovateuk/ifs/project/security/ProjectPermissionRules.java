package org.innovateuk.ifs.project.security;

import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.security.BasePermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.security.SecurityRuleUtil.*;

@PermissionRules
@Component
public class ProjectPermissionRules extends BasePermissionRules {

    @PermissionRule(value = "READ", description = "A user can see projects that they are partners on")
    public boolean partnersOnProjectCanView(ProjectResource project, UserResource user) {
        return project != null && isPartner(project.getId(), user.getId());
    }

    @PermissionRule(value = "READ", description = "Internal users can see project resources")
    public boolean internalUsersCanViewProjects(final ProjectResource project, final UserResource user) {
        return isInternal(user);
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
            value = "VIEW_MONITORING_OFFICER",
            description = "Internal users can view Monitoring Officers on any Project")
    public boolean internalUsersCanViewMonitoringOfficersForAnyProject(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_MONITORING_OFFICER",
            description = "Partners can view monitoring officers on Projects that they are partners on")
    public boolean partnersCanViewMonitoringOfficersOnTheirProjects(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "ASSIGN_MONITORING_OFFICER",
            description = "Internal users can assign Monitoring Officers on any Project")
    public boolean internalUsersCanAssignMonitoringOfficersForAnyProject(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "UPLOAD_OTHER_DOCUMENTS",
            description = "The lead partners can upload Other Documents (Collaboration Agreement, Exploitation Plan) for their Projects")
    public boolean leadPartnersCanUploadOtherDocuments(ProjectResource project, UserResource user) {
        return isLeadPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "DOWNLOAD_OTHER_DOCUMENTS",
            description = "Partners can download Other Documents (Collaboration Agreement, Exploitation Plan)")
    public boolean partnersCanDownloadOtherDocuments(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "DOWNLOAD_OTHER_DOCUMENTS",
            description = "Internal users can download Other Documents (Collaboration Agreement, Exploitation Plan)")
    public boolean internalUserCanDownloadOtherDocuments(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_OTHER_DOCUMENTS_DETAILS",
            description = "Partners can view Other Documents (Collaboration Agreement, Exploitation Plan) details that their lead partners have uploaded")
    public boolean partnersCanViewOtherDocumentsDetails(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "VIEW_OTHER_DOCUMENTS_DETAILS",
            description = "Internal users can view Other Documents (Collaboration Agreement, Exploitation Plan) details that their lead partners have uploaded")
    public boolean internalUserCanViewOtherDocumentsDetails(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "DELETE_OTHER_DOCUMENTS",
            description = "The lead partners can delete Other Documents (Collaboration Agreement, Exploitation Plan) for their Projects")
    public boolean leadPartnersCanDeleteOtherDocuments(ProjectResource project, UserResource user) {
        return isLeadPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "SUBMIT_OTHER_DOCUMENTS",
            description = "Only a project manager can submit completed partner documents")
    public boolean onlyProjectManagerCanMarkDocumentsAsSubmit(ProjectResource project, UserResource user) {
        return isProjectManager(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "ACCEPT_REJECT_OTHER_DOCUMENTS",
            description = "Internal user can accept or reject Other Documents (Collaboration Agreement, Exploitation Plan)")
    public boolean internalUserCanAcceptOrRejectOtherDocuments(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_TEAM_STATUS",
            description = "All partners can view team status")
    public boolean partnersCanViewTeamStatus(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "VIEW_TEAM_STATUS",
            description = "Internal users can see a team's status")
    public boolean internalUsersCanViewTeamStatus(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_STATUS",
            description = "All partners can view the project status")
    public boolean partnersCanViewStatus(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(
            value = "VIEW_STATUS",
            description = "Internal users can see the project status")
    public boolean internalUsersCanViewStatus(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "SEND_GRANT_OFFER_LETTER",
            description = "Internal users can send the Grant Offer Letter notification")
    public boolean internalUserCanSendGrantOfferLetter(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "APPROVE_SIGNED_GRANT_OFFER_LETTER",
            description = "Internal users can approve the signed Grant Offer Letter")
    public boolean internalUsersCanApproveSignedGrantOfferLetter(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_GRANT_OFFER_LETTER_SEND_STATUS",
            description = "Internal users can view the send status of Grant Offer Letter for a project")
    public boolean internalUserCanViewSendGrantOfferLetterStatus(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(
            value = "VIEW_GRANT_OFFER_LETTER_SEND_STATUS",
            description = "Partners can view the send status of Grant Offer Letter for a project")
    public boolean externalUserCanViewSendGrantOfferLetterStatus(ProjectResource project, UserResource user) {
        return isPartner(project.getId(), user.getId());
    }

    @PermissionRule(value = "VIEW_SIGNED_GRANT_OFFER_LETTER_APPROVED_STATUS", description = "A user can see grant offer approval status that they are partners on")
    public boolean partnersOnProjectCanViewGrantOfferApprovedStatus(ProjectResource project, UserResource user) {
        return project != null && isPartner(project.getId(), user.getId());
    }

    @PermissionRule(value = "VIEW_SIGNED_GRANT_OFFER_LETTER_APPROVED_STATUS", description = "Internal users can see grant offer approval status")
    public boolean internalUsersCanViewGrantOfferApprovedStatus(ProjectResource project, UserResource user) {
        return isInternal(user);
    }

}
