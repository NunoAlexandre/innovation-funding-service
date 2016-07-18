package com.worth.ifs.project.transactional;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.resource.OrganisationAddressType;
import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.project.resource.MonitoringOfficerResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.security.SecuredBySpring;
import com.worth.ifs.user.resource.OrganisationResource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Transactional and secure service for Project processing work
 */
public interface ProjectService {

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<ProjectResource> getProjectById(@P("projectId") final Long projectId);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<ProjectResource> getByApplicationId(@P("applicationId") final Long applicationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ProjectResource>> findAll();

    @PreAuthorize("hasAuthority('comp_admin')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin is able to create a project (by making decision)" )
    ServiceResult<ProjectResource> createProjectFromApplication(final Long applicationId);

    @PreAuthorize("hasAuthority('comp_admin')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin is able to create a projects (by making decisions)" )
    ServiceResult<Void> createProjectsFromFundingDecisions(Map<Long, FundingDecision> applicationFundingDecisions);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
	ServiceResult<Void> setProjectManager(Long projectId, Long projectManagerId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> updateProjectStartDate(Long projectId, LocalDate projectStartDate);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> updateProjectAddress(Long leadOrganisationId, Long projectId, OrganisationAddressType addressType, AddressResource addressResource);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<ProjectResource>> findByUserId(final Long userId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_FINANCE_CONTACT')")
    ServiceResult<Void> updateFinanceContact(Long projectId, Long organisationId, Long financeContactUserId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'READ')")
    ServiceResult<List<ProjectUserResource>> getProjectUsers(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_BASIC_PROJECT_SETUP_DETAILS')")
    ServiceResult<Void> saveProjectSubmitDateTime(final Long projectId, LocalDateTime date);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPDATE_FINANCE_CONTACT')")
    ServiceResult<Boolean> isSubmitAllowed(final Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'VIEW_MONITORING_OFFICER')")
    ServiceResult<MonitoringOfficerResource> getMonitoringOfficer(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'ASSIGN_MONITORING_OFFICER')")
    ServiceResult<Void> saveMonitoringOfficer(final Long projectId, final MonitoringOfficerResource monitoringOfficerResource);

 	@PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<OrganisationResource> getOrganisationByProjectAndUser(final Long projectId, final Long userId);

    @PreAuthorize("hasPermission(#monitoringOfficer.project, 'com.worth.ifs.project.resource.ProjectResource', 'ASSIGN_MONITORING_OFFICER')")
    ServiceResult<Void> notifyMonitoringOfficer(MonitoringOfficerResource monitoringOfficer);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPLOAD_OTHER_DOCUMENTS')")
    ServiceResult<FileEntryResource> createCollaborationAgreementFileEntry(long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'DOWNLOAD_OTHER_DOCUMENTS')")
    ServiceResult<Pair<FileEntryResource,Supplier<InputStream>>> getCollaborationAgreementFileEntryContents(long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'VIEW_OTHER_DOCUMENTS_DETAILS')")
    ServiceResult<FileEntryResource> getCollaborationAgreementFileEntryDetails(long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPLOAD_OTHER_DOCUMENTS')")
    ServiceResult<FileEntryResource> updateCollaborationAgreementFileEntry(long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'DELETE_OTHER_DOCUMENTS')")
    ServiceResult<Void> deleteCollaborationAgreementFileEntry(long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPLOAD_OTHER_DOCUMENTS')")
    ServiceResult<FileEntryResource> createExploitationPlanFileEntry(long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'DOWNLOAD_OTHER_DOCUMENTS')")
    ServiceResult<Pair<FileEntryResource,Supplier<InputStream>>> getExploitationPlanFileEntryContents(long projectId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'VIEW_OTHER_DOCUMENTS_DETAILS')")
    ServiceResult<FileEntryResource> getExploitationPlanFileEntryDetails(long applicationId);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'UPLOAD_OTHER_DOCUMENTS')")
    ServiceResult<FileEntryResource> updateExploitationPlanFileEntry(long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'com.worth.ifs.project.resource.ProjectResource', 'DELETE_OTHER_DOCUMENTS')")
    ServiceResult<Void> deleteExploitationPlanFileEntry(long applicationId);
}
