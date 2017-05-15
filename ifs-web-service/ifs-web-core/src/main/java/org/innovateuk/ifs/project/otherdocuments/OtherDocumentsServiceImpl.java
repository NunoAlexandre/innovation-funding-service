package org.innovateuk.ifs.project.otherdocuments;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.otherdocuments.service.ProjectOtherDocumentsRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A service for dealing with Project Other Documents via the appropriate Rest services
 */
@Service
public class OtherDocumentsServiceImpl implements OtherDocumentsService {

    @Autowired
    private ProjectOtherDocumentsRestService projectOtherDocumentsRestService;

    @Override
    public Optional<ByteArrayResource> getCollaborationAgreementFile(Long projectId) {
        return projectOtherDocumentsRestService.getCollaborationAgreementFile(projectId).getSuccessObjectOrThrowException();
    }

    @Override
    public Optional<FileEntryResource> getCollaborationAgreementFileDetails(Long projectId) {
        return projectOtherDocumentsRestService.getCollaborationAgreementFileDetails(projectId).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<FileEntryResource> addCollaborationAgreementDocument(Long projectId, String contentType, long fileSize, String originalFilename, byte[] bytes) {
        return projectOtherDocumentsRestService.addCollaborationAgreementDocument(projectId, contentType, fileSize, originalFilename, bytes).toServiceResult();
    }

    @Override
    public ServiceResult<Void> removeCollaborationAgreementDocument(Long projectId) {
        return projectOtherDocumentsRestService.removeCollaborationAgreementDocument(projectId).toServiceResult();
    }

    @Override
    public Optional<ByteArrayResource> getExploitationPlanFile(Long projectId) {
        return projectOtherDocumentsRestService.getExploitationPlanFile(projectId).getSuccessObjectOrThrowException();
    }

    @Override
    public Optional<FileEntryResource> getExploitationPlanFileDetails(Long projectId) {
        return projectOtherDocumentsRestService.getExploitationPlanFileDetails(projectId).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<FileEntryResource> addExploitationPlanDocument(Long projectId, String contentType, long fileSize, String originalFilename, byte[] bytes) {
        return projectOtherDocumentsRestService.addExploitationPlanDocument(projectId, contentType, fileSize, originalFilename, bytes).toServiceResult();
    }

    @Override
    public ServiceResult<Void> removeExploitationPlanDocument(Long projectId) {
        return projectOtherDocumentsRestService.removeExploitationPlanDocument(projectId).toServiceResult();
    }

    @Override
    public ServiceResult<Void> acceptOrRejectOtherDocuments(Long projectId, Boolean approved) {
        return projectOtherDocumentsRestService.acceptOrRejectOtherDocuments(projectId, approved).toServiceResult();
    }

    @Override
    public Boolean isOtherDocumentSubmitAllowed(Long projectId) {
        return projectOtherDocumentsRestService.isOtherDocumentsSubmitAllowed(projectId).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<Void> setPartnerDocumentsSubmitted(Long projectId) {
        return projectOtherDocumentsRestService.setPartnerDocumentsSubmitted(projectId).toServiceResult();
    }
}
