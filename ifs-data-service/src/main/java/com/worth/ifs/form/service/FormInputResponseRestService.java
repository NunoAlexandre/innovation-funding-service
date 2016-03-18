package com.worth.ifs.form.service;

import com.worth.ifs.application.domain.Response;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.form.domain.FormInputResponse;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

/**
 * Interface for CRUD operations on {@link Response} related data.
 */
public interface FormInputResponseRestService {
    RestResult<List<FormInputResponse>> getResponsesByApplicationId(Long applicationId);
    RestResult<List<String>> saveQuestionResponse(Long userId, Long applicationId, Long formInputId, String value, boolean ignoreEmpty);
    RestResult<FileEntryResource> createFileEntry(long formInputId, long applicationId, long processRoleId, String contentType, long contentLength, String originalFilename, byte[] file);
    RestResult<Void> removeFileEntry(long formInputId, long applicationId, long processRoleId);
    RestResult<ByteArrayResource> getFile(long formInputId, long applicationId, long processRoleId);
}
