package com.worth.ifs.project.sections;

import com.worth.ifs.project.status.resource.ProjectStatusResource;
import com.worth.ifs.user.resource.UserResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.worth.ifs.project.sections.SectionAccess.ACCESSIBLE;
import static com.worth.ifs.project.sections.SectionAccess.NOT_ACCESSIBLE;

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
        return NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessMonitoringOfficerSection(UserResource userResource) {
        if (!projectSetupProgressChecker.isProjectDetailsSubmitted()) {
            return fail("Unable to access Monitoring Officer section until Project Details are submitted");
        }

        return ACCESSIBLE;
    }

    public SectionAccess canAccessBankDetailsSection(UserResource userResource) {
        if(!projectSetupProgressChecker.isBankDetailsApproved()
                && !projectSetupProgressChecker.isBankDetailsActionRequired()) {
            return NOT_ACCESSIBLE;
        }
        return ACCESSIBLE;
    }

    public SectionAccess canAccessFinanceChecksSection(UserResource userResource) {
        return NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessSpendProfileSection(UserResource userResource) {
        return NOT_ACCESSIBLE;
    }

    public SectionAccess canAccessOtherDocumentsSection(UserResource userResource) {
        return NOT_ACCESSIBLE;
    }

    public boolean isProjectDetailsSubmitted() {
        return projectSetupProgressChecker.isProjectDetailsSubmitted();
    }

    private SectionAccess fail(String message) {
        LOG.info(message);
        return NOT_ACCESSIBLE;
    }
}
