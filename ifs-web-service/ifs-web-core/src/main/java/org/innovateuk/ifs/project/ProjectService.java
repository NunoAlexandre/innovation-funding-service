package org.innovateuk.ifs.project;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.status.resource.ProjectStatusResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * A service for dealing with ProjectResources via the appropriate Rest services
 */
public interface ProjectService {

    List<ProjectUserResource> getProjectUsersForProject(Long projectId);

    List<OrganisationResource> getPartnerOrganisationsForProject(Long projectId);

    ProjectResource getById(Long projectId);

    ProjectResource getByApplicationId(Long applicationId);

    ServiceResult<Void> updateFinanceContact(ProjectOrganisationCompositeId composite, Long financeContactUserId);

    ServiceResult<Void> updateProjectManager(Long projectId, Long projectManagerUserId);

    ServiceResult<List<ProjectResource>> findByUser(Long userId);

    ServiceResult<Void> updateProjectStartDate(Long projectId, LocalDate projectStartDate);

    ServiceResult<Void> updateAddress(Long leadOrganisationId, Long projectId, OrganisationAddressType addressType, AddressResource address);

    ServiceResult<Void> setApplicationDetailsSubmitted(Long projectId);

    ServiceResult<Boolean> isSubmitAllowed(Long projectId);

    OrganisationResource getLeadOrganisation(Long projectId);

    OrganisationResource getOrganisationByProjectAndUser(Long projectId, Long userId);

    boolean isUserLeadPartner(Long projectId, Long userId);

    List<ProjectUserResource> getLeadPartners(Long projectId);

    List<ProjectUserResource> getPartners(Long projectId);

    ProjectTeamStatusResource getProjectTeamStatus(Long projectId, Optional<Long> filterByUserId);

    ProjectStatusResource getProjectStatus(Long projectId);

    ServiceResult<Void> inviteFinanceContact (Long projectId, InviteProjectResource inviteProjectResource);

    ServiceResult<Void> inviteProjectManager (Long projectId, InviteProjectResource inviteProjectResource);

    List<ProjectUserResource> getProjectUsersWithPartnerRole(Long projectId);

    ServiceResult<Void> saveProjectInvite(InviteProjectResource inviteProjectResource);

    ServiceResult<List<InviteProjectResource>> getInvitesByProject(Long projectId);

    Optional<ProjectUserResource> getProjectManager(Long projectId);

    Boolean isProjectManager(Long userId, Long projectId);
}
