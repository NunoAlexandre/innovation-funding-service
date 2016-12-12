package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.springframework.core.io.ByteArrayResource;

public interface AssessorFeedbackRestService {

    RestResult<FileEntryResource> addAssessorFeedbackDocument(Long applicationId, String contentType, long contentLength, String originalFilename, byte[] file);

    RestResult<Void> removeAssessorFeedbackDocument(Long applicationId);

    RestResult<ByteArrayResource> getAssessorFeedbackFile(Long applicationId);

    RestResult<FileEntryResource> getAssessorFeedbackFileDetails(Long applicationId);
    
    RestResult<Boolean> feedbackUploaded(Long competitionId);
    
    RestResult<Void> submitAssessorFeedback(Long competitionId);
   
}
