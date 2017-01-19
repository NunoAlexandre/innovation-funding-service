package org.innovateuk.ifs.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.OverheadFileRestService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

/**
 * A saver class intended to use for adding / removing files attached to overhead finance row
 */

@Component
public class OverheadFileSaver {

    @Autowired
    private OverheadFileRestService overheadFileRestService;

    private static final Log LOG = LogFactory.getLog(OverheadFileSaver.class);

    private ValidationMessages uploadOverheadFile(HttpServletRequest request) {
        ValidationMessages messages = new ValidationMessages();

        final Map<String, MultipartFile> fileMap = ((StandardMultipartHttpServletRequest) request).getFileMap();
        final MultipartFile file = fileMap.get("overheadfile");
        try {
            Long overheadId = Long.valueOf(request.getParameter("fileoverheadid"));
            RestResult<FileEntryResource> fileEntryResult = overheadFileRestService.updateOverheadCalculationFile(overheadId, file.getContentType(), file.getSize(), file.getOriginalFilename(), file.getBytes());

            handleRestResultUpload(fileEntryResult, messages);
        } catch(NumberFormatException | IOException e) {
            LOG.error("Overheadfile cannot be saved :"  + e.getMessage());
        }

        return messages;
    }

    private void handleRestResultUpload(RestResult<FileEntryResource> fileEntryResult, ValidationMessages messages) {
        if(fileEntryResult.isFailure()) {
            if(fileEntryResult.getErrors().stream().anyMatch(error -> error.getErrorKey().equals("UNSUPPORTED_MEDIA_TYPE"))) {
                Error error = new Error("validation.finance.overhead.file.type",UNSUPPORTED_MEDIA_TYPE);
                messages.addError(error);
            }
            else {
                messages.addAll(fileEntryResult);
            }
        }
    }

    private ValidationMessages deleteOverheadFile(HttpServletRequest request) {
        ValidationMessages messages = new ValidationMessages();
        try {
            Long overheadId = Long.valueOf(request.getParameter("fileoverheadid"));

            RestResult<Void> fileEntryResult = overheadFileRestService.removeOverheadCalculationFile(overheadId);

            if (fileEntryResult.isFailure()) {
                messages.addAll(fileEntryResult);
            }
        } catch (NumberFormatException e) {
            LOG.error("Overheadfile cannot be deleted :"  + e.getMessage());
        }

        return messages;
    }

    public ValidationMessages handleOverheadFileRequest(HttpServletRequest request) {
        if(isOverheadFileUploadRequest(request)) {
            return uploadOverheadFile(request);
        }
        else if (isOverheadFileDeleteRequest(request)) {
            return deleteOverheadFile(request);
        }
        else {
            return new ValidationMessages();
        }
    }

    private boolean isOverheadFileUploadRequest(HttpServletRequest request) {
        return request instanceof StandardMultipartHttpServletRequest && request.getParameter("overheadfilesubmit") != null;
    }

    public boolean isOverheadFileDeleteRequest(HttpServletRequest request) {
        return request.getParameter("overheadfiledelete") != null;
    }
}
