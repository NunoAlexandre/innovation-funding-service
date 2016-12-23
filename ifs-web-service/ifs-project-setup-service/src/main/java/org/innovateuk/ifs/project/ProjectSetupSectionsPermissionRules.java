package org.innovateuk.ifs.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.exception.ForbiddenActionException;
import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.project.resource.ProjectPartnerStatusResource;
import org.innovateuk.ifs.project.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.sections.ProjectSetupSectionPartnerAccessor;
import org.innovateuk.ifs.project.sections.SectionAccess;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.innovateuk.ifs.project.sections.SectionAccess.ACCESSIBLE;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * Permission checker around the access to various sections within the Project Setup process
 */
@PermissionRules
@Component
public class ProjectSetupSectionsPermissionRules {

    private static final Log LOG = LogFactory.getLog(ProjectSetupSectionsPermissionRules.class);

    @Autowired
    private ProjectService projectService;

    private ProjectSetupSectionPartnerAccessorSupplier accessorSupplier = new ProjectSetupSectionPartnerAccessorSupplier();

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

    @PermissionRule(value = "ACCESS_GRANT_OFFER_LETTER_SECTION", description = "A lead partner can access the Grant Offer Letter " +
            "section when all other sections are complete and a Grant Offer Letter has been generated by the internal team")
    public boolean partnerCanAccessGrantOfferLetterSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionPartnerAccessor::canAccessGrantOfferLetterSection);
    }

    @PermissionRule(value = "MARK_SPEND_PROFILE_INCOMPLETE", description = "A project manager can access certain methods which are unavailable to others")
    public boolean userIsProjectManager(Long projectId, UserResource user) {
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pu -> user.getId().equals(pu.getUser()) && UserRoleType.PROJECT_MANAGER.getName().equals(pu.getRoleName())) != null;
    }

    private boolean doSectionCheck(Long projectId, UserResource user, BiFunction<ProjectSetupSectionPartnerAccessor, OrganisationResource, SectionAccess> sectionCheckFn) {

        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);

        Optional<ProjectUserResource> projectUser = simpleFindFirst(projectUsers, pu ->
                user.getId().equals(pu.getUser()) && UserRoleType.PARTNER.getName().equals(pu.getRoleName()));

        return projectUser.map(pu -> {

            ProjectTeamStatusResource teamStatus;

            try {
                teamStatus = projectService.getProjectTeamStatus(projectId, Optional.of(user.getId()));
            } catch (ForbiddenActionException e) {
                LOG.error("User " + user.getId() + " is not a Partner on an Organisation for Project " + projectId + ".  Denying access to Project Setup");
                return false;
            }

            ProjectPartnerStatusResource partnerStatusForUser = teamStatus.getPartnerStatusForOrganisation(pu.getOrganisation()).get();

            ProjectSetupSectionPartnerAccessor sectionAccessor = accessorSupplier.apply(teamStatus);
            OrganisationResource organisation = new OrganisationResource();
            organisation.setId(partnerStatusForUser.getOrganisationId());
            organisation.setOrganisationType(partnerStatusForUser.getOrganisationType().getOrganisationTypeId());

            return sectionCheckFn.apply(sectionAccessor, organisation) == ACCESSIBLE;

        }).orElseGet(() -> {
            LOG.error("User " + user.getId() + " is not a Partner on an Organisation for Project " + projectId + ".  Denying access to Project Setup");
            return false;
        });
    }

    class ProjectSetupSectionPartnerAccessorSupplier implements Function<ProjectTeamStatusResource, ProjectSetupSectionPartnerAccessor> {

        @Override
        public ProjectSetupSectionPartnerAccessor apply(ProjectTeamStatusResource teamStatus) {
            return new ProjectSetupSectionPartnerAccessor(teamStatus);
        }
    }
}
