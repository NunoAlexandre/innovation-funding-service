package org.innovateuk.ifs.project.projectdetails.transactional;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transactional and secure service for Project Details processing work
 */
public interface ProjectDetailsService {

/*    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<ProjectResource> getProjectById(@P("projectId") Long projectId);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<ProjectResource> getByApplicationId(@P("applicationId") Long applicationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ProjectResource>> findAll();*/

/*    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a project (by making decision)" )
    ServiceResult<ProjectResource> createProjectFromApplication(Long applicationId);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a projects (by making decisions)" )
    ServiceResult<Void> createProjectsFromFundingDecisions(Map<Long, FundingDecision> applicationFundingDecisions);*/

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> setProjectManager(Long projectId, Long projectManagerId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> updateProjectStartDate(Long projectId, LocalDate projectStartDate);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> updateProjectAddress(Long leadOrganisationId, Long projectId, OrganisationAddressType addressType, AddressResource addressResource);

/*    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ProjectResource>> findByUserId(Long userId);*/

    @PreAuthorize("hasPermission(#composite, 'UPDATE_FINANCE_CONTACT')")
    ServiceResult<Void> updateFinanceContact(ProjectOrganisationCompositeId composite, Long financeContactUserId);

    @PreAuthorize("hasPermission(#inviteResource, 'SEND_PROJECT_INVITE')")
    ServiceResult<Void> inviteFinanceContact(Long projectId, InviteProjectResource inviteResource);

    @PreAuthorize("hasPermission(#inviteResource, 'SEND_PROJECT_INVITE')")
    ServiceResult<Void> inviteProjectManager(Long projectId, InviteProjectResource inviteResource);

/*    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'READ')")
    ServiceResult<List<ProjectUserResource>> getProjectUsers(Long projectId);*/

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> submitProjectDetails(Long projectId, ZonedDateTime date);

    @PreAuthorize("hasPermission(#projectId, 'UPDATE_FINANCE_CONTACT')")
    ServiceResult<Boolean> isSubmitAllowed(Long projectId);

/*    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<OrganisationResource> getOrganisationByProjectAndUser(Long projectId, Long userId);

    @PreAuthorize("hasAuthority('system_registrar')")
    @SecuredBySpring(value = "ADD_PARTNER",
            description = "The System Registration user can add a partner to a project")
    ServiceResult<ProjectUser> addPartner(Long projectId, Long userId, Long organisationId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_TEAM_STATUS')")
    ServiceResult<ProjectTeamStatusResource> getProjectTeamStatus(Long projectId, Optional<Long> filterByUserId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'READ')")
    ServiceResult<ProjectUserResource> getProjectManager(Long projectId);*/
}
