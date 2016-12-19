package org.innovateuk.ifs.project.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileAndContents;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Transactional and secure service for Project processing work - grant offer service.
 **/
public interface ProjectGrantOfferService {

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_PARTNER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileAndContents> getSignedGrantOfferLetterFileAndContents(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_PARTNER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileAndContents> getGrantOfferLetterFileAndContents(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_PARTNER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'DOWNLOAD_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileAndContents> getAdditionalContractFileAndContents(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileEntryResource> getSignedGrantOfferLetterFileEntryDetails(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileEntryResource> getGrantOfferLetterFileEntryDetails(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER') || hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'VIEW_GRANT_OFFER_INTERNAL_USER')")
    public ServiceResult<FileEntryResource> getAdditionalContractFileEntryDetails(Long projectId);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPLOAD_SIGNED_GRANT_OFFER')")
    ServiceResult<FileEntryResource> createSignedGrantOfferLetterFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a grant offer letter" )
    ServiceResult<FileEntryResource> createGrantOfferLetterFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a grant offer letter" )
    ServiceResult<FileEntryResource> generateGrantOfferLetter(Long projectId, FileEntryResource fileEntryResource);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a grant offer letter" )
    ServiceResult<Void> generateGrantOfferLetterIfReady(Long projectId);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "DELETE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to delete a grant offer letter" )
    ServiceResult<Void> removeGrantOfferLetterFileEntry(Long projectId);

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    @SecuredBySpring(value = "UPDATE", securedType = ProjectResource.class, description = "Only comp admin and project finance user are able to create a additional contract for Appendix 2 if any")
    ServiceResult<FileEntryResource> createAdditionalContractFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectResource', 'UPLOAD_SIGNED_GRANT_OFFER')")
    ServiceResult<Void> updateSignedGrantOfferLetterFile(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#projectId, 'SUBMIT_GRANT_OFFER_LETTER')")
    ServiceResult<Void> submitGrantOfferLetter(Long projectId);
}
