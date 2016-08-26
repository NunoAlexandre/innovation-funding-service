package com.worth.ifs.project;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.resource.OrganisationAddressType;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.invite.resource.InviteProjectResource;
import com.worth.ifs.project.resource.MonitoringOfficerResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.resource.SpendProfileResource;
import com.worth.ifs.user.resource.OrganisationResource;
import org.springframework.core.io.ByteArrayResource;

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

    SpendProfileResource getSpendProfile(Long projectId, Long organisationId);

    ProjectResource getByApplicationId(Long applicationId);

    ServiceResult<Void> updateFinanceContact(Long projectId, Long organisationId, Long financeContactUserId);

    ServiceResult<Void> updateProjectManager(Long projectId, Long projectManagerUserId);

    ServiceResult<List<ProjectResource>> findByUser(Long userId);

    ServiceResult<Void> updateProjectStartDate(Long projectId, LocalDate projectStartDate);

    ServiceResult<Void> updateAddress(Long leadOrganisationId, Long projectId, OrganisationAddressType addressType, AddressResource address);

    ServiceResult<Void> setApplicationDetailsSubmitted(Long projectId);

    ServiceResult<Boolean> isSubmitAllowed(Long projectId);

    OrganisationResource getLeadOrganisation(Long projectId);

    OrganisationResource getOrganisationByProjectAndUser(Long projectId, Long userId);

    Optional<MonitoringOfficerResource> getMonitoringOfficerForProject(Long projectId);

    ServiceResult<Void> updateMonitoringOfficer(Long projectId, String firstName, String lastName, String emailAddress, String phoneNumber);

    Optional<ByteArrayResource> getCollaborationAgreementFile(Long projectId);

    Optional<FileEntryResource> getCollaborationAgreementFileDetails(Long projectId);

    ServiceResult<FileEntryResource> addCollaborationAgreementDocument(Long projectId, String contentType, long fileSize, String originalFilename, byte[] bytes);

    ServiceResult<Void> removeCollaborationAgreementDocument(Long projectId);

    Optional<ByteArrayResource> getExploitationPlanFile(Long projectId);

    Optional<FileEntryResource> getExploitationPlanFileDetails(Long projectId);

    ServiceResult<FileEntryResource> addExploitationPlanDocument(Long projectId, String contentType, long fileSize, String originalFilename, byte[] bytes);

    ServiceResult<Void> removeExploitationPlanDocument(Long projectId);

    boolean isUserLeadPartner(Long projectId, Long userId);

    List<ProjectUserResource> getLeadPartners(Long projectId);

    boolean isUserPartner(Long projectId, Long userId);

    List<ProjectUserResource> getPartners(Long projectId);

    ServiceResult<Boolean> isOtherDocumentSubmitAllowed(Long projectId);

    ServiceResult<Void> setPartnerDocumentsSubmitted(Long projectId);

    ServiceResult<Void> inviteFinanceContact (Long projectId, InviteProjectResource inviteProjectResource);

    ServiceResult<Void> saveProjectInvite(InviteProjectResource inviteProjectResource);
}
