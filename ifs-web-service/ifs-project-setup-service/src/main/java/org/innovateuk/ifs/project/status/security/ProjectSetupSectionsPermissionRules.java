package org.innovateuk.ifs.project.status.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.exception.ForbiddenActionException;
import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectPartnerStatusResource;
import org.innovateuk.ifs.project.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.sections.ProjectSetupSectionsAccessibilityHelper;
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
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessProjectDetailsSection);
    }

    @PermissionRule(value = "ACCESS_MONITORING_OFFICER_SECTION", description = "A partner can access the Monitoring Officer " +
            "section when their Companies House details are complete or not required, and the Project Details have been submitted")
    public boolean partnerCanAccessMonitoringOfficerSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessMonitoringOfficerSection);
    }

    @PermissionRule(value = "ACCESS_BANK_DETAILS_SECTION", description = "A partner can access the Bank Details " +
            "section when their Companies House details are complete or not required, and they have a Finance Contact " +
            "available for their Organisation")
    public boolean partnerCanAccessBankDetailsSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessBankDetailsSection);
    }

    @PermissionRule(value = "ACCESS_FINANCE_CHECKS_SECTION_EXTERNAL", description = "A partner can access the Bank Details " +
            "section when their Companies House details are complete or not required, and the Project Details have been submitted")
    public boolean partnerCanAccessFinanceChecksSection(Long projectId, UserResource user) {
            return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessFinanceChecksSection);
    }

    @PermissionRule(value = "ACCESS_SPEND_PROFILE_SECTION", description = "A partner can access the Spend Profile " +
            "section when their Companies House details are complete or not required, the Project Details have been submitted, " +
            "and the Organisation's Bank Details have been approved or queried")
    public boolean partnerCanAccessSpendProfileSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessSpendProfileSection);
    }

    @PermissionRule(value = "ACCESS_COMPANIES_HOUSE_SECTION", description = "A partner can access the Companies House " +
            "section if their Organisation is a business type (i.e. if Companies House details are required)")
    public boolean partnerCanAccessCompaniesHouseSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessCompaniesHouseSection);
    }

    @PermissionRule(value = "ACCESS_OTHER_DOCUMENTS_SECTION", description = "A partner can access the Other Documents " +
            "section if their Organisation is a business type (i.e. if Companies House details are required)")
    public boolean partnerCanAccessOtherDocumentsSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessOtherDocumentsSection);
    }

    @PermissionRule(value = "ACCESS_GRANT_OFFER_LETTER_SECTION", description = "A lead partner can access the Grant Offer Letter " +
            "section when all other sections are complete and a Grant Offer Letter has been generated by the internal team")
    public boolean partnerCanAccessGrantOfferLetterSection(Long projectId, UserResource user) {
        return doSectionCheck(projectId, user, ProjectSetupSectionsAccessibilityHelper::canAccessGrantOfferLetterSection);
    }

    @PermissionRule(value = "ACCESS_SIGNED_GRANT_OFFER_LETTER", description = "A lead partner can view and download signed grant offer letter document")
    public boolean leadPartnerAccess(Long projectId, UserResource user) {
        return projectService.isUserLeadPartner(projectId, user.getId());
    }

    @PermissionRule(value = "MARK_SPEND_PROFILE_INCOMPLETE", description = "All lead partners can mark partners spend profiles as incomplete")
    public boolean userCanMarkSpendProfileIncomplete(Long projectId, UserResource user) {
        List<ProjectUserResource> projectLeadPartners = projectService.getLeadPartners(projectId);
        Optional<ProjectUserResource> returnedProjectUser = simpleFindFirst(projectLeadPartners, projectUserResource -> projectUserResource.getUser().equals(user.getId()));

        return returnedProjectUser.isPresent();
    }

    private boolean doSectionCheck(Long projectId, UserResource user, BiFunction<ProjectSetupSectionsAccessibilityHelper, OrganisationResource, SectionAccess> sectionCheckFn) {

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

            ProjectSetupSectionsAccessibilityHelper sectionAccessor = accessorSupplier.apply(teamStatus);
            OrganisationResource organisation = new OrganisationResource();
            organisation.setId(partnerStatusForUser.getOrganisationId());
            organisation.setOrganisationType(partnerStatusForUser.getOrganisationType().getOrganisationTypeId());

            return sectionCheckFn.apply(sectionAccessor, organisation) == ACCESSIBLE;

        }).orElseGet(() -> {
            LOG.error("User " + user.getId() + " is not a Partner on an Organisation for Project " + projectId + ".  Denying access to Project Setup");
            return false;
        });
    }

    public class ProjectSetupSectionPartnerAccessorSupplier implements Function<ProjectTeamStatusResource, ProjectSetupSectionsAccessibilityHelper> {

        @Override
        public ProjectSetupSectionsAccessibilityHelper apply(ProjectTeamStatusResource teamStatus) {
            return new ProjectSetupSectionsAccessibilityHelper(teamStatus);
        }
    }
}
