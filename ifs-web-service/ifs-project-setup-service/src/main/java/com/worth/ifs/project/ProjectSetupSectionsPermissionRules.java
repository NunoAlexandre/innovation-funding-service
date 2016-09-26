package com.worth.ifs.project;

import com.worth.ifs.commons.error.exception.ForbiddenActionException;
import com.worth.ifs.commons.security.PermissionRule;
import com.worth.ifs.commons.security.PermissionRules;
import com.worth.ifs.project.resource.ProjectPartnerStatusResource;
import com.worth.ifs.project.resource.ProjectTeamStatusResource;
import com.worth.ifs.project.sections.ProjectSetupSectionPartnerAccessor;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.UserResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Permission checker around the access to various sections within the Project Setup process
 */
@PermissionRules
@Component
public class ProjectSetupSectionsPermissionRules {

    private static final Log LOG = LogFactory.getLog(ProjectSetupSectionsPermissionRules.class);

    @Autowired
    private ProjectService projectService;

    @PermissionRule(value = "ACCESS_PROJECT_DETAILS_SECTION", description = "A partner can access the Project Details section when their Companies House data is complete or not required")
    public boolean partnerCanAccessProjectDetailsSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessProjectDetailsSection);
    }

    @PermissionRule(value = "ACCESS_MONITORING_OFFICER_SECTION", description = "A partner can access the Monitoring Officer " +
            "section when their Companies House details are complete or not required, and the Project Details have been submitted")
    public boolean partnerCanAccessMonitoringOfficerSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessMonitoringOfficerSection);
    }

    @PermissionRule(value = "ACCESS_BANK_DETAILS_SECTION", description = "A partner can access the Bank Details " +
            "section when their Companies House details are complete or not required, and they have a Finance Contact " +
            "available for their Organisation")
    public boolean partnerCanAccessBankDetailsSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessBankDetailsSection);
    }

    @PermissionRule(value = "ACCESS_FINANCE_CHECKS_SECTION", description = "A partner can access the Bank Details " +
            "section when their Companies House details are complete or not required, and the Project Details have been submitted")
    public boolean partnerCanAccessFinanceChecksSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessFinanceChecksSection);
    }

    @PermissionRule(value = "ACCESS_SPEND_PROFILE_SECTION", description = "A partner can access the Spend Profile " +
            "section when their Companies House details are complete or not required, the Project Details have been submitted, " +
            "and the Organisation's Bank Details have been approved or queried")
    public boolean partnerCanAccessSpendProfileSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessSpendProfileSection);
    }

    @PermissionRule(value = "ACCESS_COMPANIES_HOUSE_SECTION", description = "A partner can access the Companies House " +
            "section if their Organisation is a business type (i.e. if Companies House details are required)")
    public boolean partnerCanAccessCompaniesHouseSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessCompaniesHouseSection);
    }

    @PermissionRule(value = "ACCESS_OTHER_DOCUMENTS_SECTION", description = "A partner can access the Other Documents " +
            "section if their Organisation is a business type (i.e. if Companies House details are required)")
    public boolean partnerCanAccessOtherDocumentsSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessOtherDocumentsSection);
    }

    private boolean doSectionCheck(Long projectId, UserResource user, BiFunction<ProjectSetupSectionPartnerAccessor, OrganisationResource, Boolean> sectionCheckFn) {
        ProjectTeamStatusResource teamStatus;

        try {
            teamStatus = projectService.getProjectTeamStatus(projectId, Optional.of(user.getId()));
        } catch (ForbiddenActionException e) {
            LOG.error("User " + user.getId() + " is not a Partner on an Organisation for Project " + projectId);
            return false;
        }

        ProjectPartnerStatusResource partnerStatusForUser =
                !teamStatus.getOtherPartnersStatuses().isEmpty() ?
                        teamStatus.getOtherPartnersStatuses().get(0) :
                        teamStatus.getLeadPartnerStatus();

        ProjectSetupSectionPartnerAccessor sectionAccessor = new ProjectSetupSectionPartnerAccessor(teamStatus);
        OrganisationResource organisation = new OrganisationResource();
        organisation.setId(partnerStatusForUser.getOrganisationId());
        organisation.setOrganisationType(partnerStatusForUser.getOrganisationType().getOrganisationTypeId());

        return sectionCheckFn.apply(sectionAccessor, organisation);
    }
}
