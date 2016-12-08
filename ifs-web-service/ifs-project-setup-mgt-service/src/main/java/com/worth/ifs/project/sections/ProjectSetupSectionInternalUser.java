package com.worth.ifs.project.sections;

import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.project.resource.ApprovalType;
import com.worth.ifs.project.status.resource.ProjectStatusResource;
import com.worth.ifs.user.resource.UserResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.worth.ifs.project.constant.ProjectActivityStates.*;
import static com.worth.ifs.project.sections.SectionAccess.ACCESSIBLE;
import static com.worth.ifs.project.sections.SectionAccess.NOT_ACCESSIBLE;
import static com.worth.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static com.worth.ifs.user.resource.UserRoleType.PROJECT_FINANCE;

/**
 * This is a helper class for determining whether or not a given Project Setup section is available to access
 */
public class ProjectSetupSectionInternalUser {

    private static final Log LOG = LogFactory.getLog(ProjectSetupSectionInternalUser.class);

    private ProjectSetupProgressChecker projectSetupProgressChecker;

    public ProjectSetupSectionInternalUser(ProjectStatusResource projectStatusResource) {
        this.projectSetupProgressChecker = new ProjectSetupProgressChecker(projectStatusResource);
    }

    public SectionAccess canAccessCompaniesHouseSection(UserResource userResource) {
        return NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessProjectDetailsSection(UserResource userResource) {
        if (!projectSetupProgressChecker.isProjectDetailsSubmitted()) {
            return fail("Unable to access Project Details section until Project Details are submitted");
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessMonitoringOfficerSection(UserResource userResource) {
        if (!projectSetupProgressChecker.isProjectDetailsSubmitted()) {
            return fail("Unable to access Monitoring Officer section until Project Details are submitted");
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessBankDetailsSection(UserResource userResource) {

        if (!userResource.hasRole(PROJECT_FINANCE)) {
            return NOT_ACCESSIBLE;
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessFinanceChecksSection(UserResource userResource) {
        return userResource.hasRole(PROJECT_FINANCE) ? ACCESSIBLE : NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessSpendProfileSection(UserResource userResource) {

        if (projectSetupProgressChecker.isSpendProfileApproved() || projectSetupProgressChecker.isSpendProfileSubmitted()) {
            return ACCESSIBLE;
        }

        return NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessOtherDocumentsSection(UserResource userResource) {
        if(!projectSetupProgressChecker.isOtherDocumentsSubmitted() && !(projectSetupProgressChecker.isOtherDocumentsApproved() || projectSetupProgressChecker.isOtherDocumentsRejected())) {
            return NOT_ACCESSIBLE;
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessGrantOfferLetterSection(UserResource userResource) {
        if(!projectSetupProgressChecker.isGrantOfferLetterSent()) {
            return NOT_ACCESSIBLE;
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessGrantOfferLetterSendSection(UserResource userResource) {
        if((userResource.hasRole(COMP_ADMIN) || userResource.hasRole(PROJECT_FINANCE)) && projectSetupProgressChecker.isOtherDocumentsApproved() && projectSetupProgressChecker.isSpendProfileApproved()) {
            return ACCESSIBLE;
        }

        return NOT_ACCESSIBLE;
    }
    private SectionAccess fail(String message) {
        LOG.info(message);
        return NOT_ACCESSIBLE;
    }

    public ProjectActivityStates grantOfferLetterActivityStatus(UserResource userResource) {
        if(userResource.hasRole(COMP_ADMIN) || userResource.hasRole(PROJECT_FINANCE)) {
            return projectSetupProgressChecker.getRoleSpecificActivityState().get(COMP_ADMIN);
        } else {
            return projectSetupProgressChecker.getGrantOfferLetterState();
        }

    }


}
