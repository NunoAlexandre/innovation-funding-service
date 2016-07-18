package com.worth.ifs.application.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.transactional.AssessorFeedbackService;
import com.worth.ifs.commons.rest.RestErrorResponse;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.BasicFileAndContents;
import com.worth.ifs.file.transactional.FileHeaderAttributes;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.worth.ifs.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.JsonTestUtil.toJson;
import static com.worth.ifs.commons.error.CommonErrors.internalServerErrorError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static com.worth.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AssessorFeedbackControllerTest extends BaseControllerMockMVCTest<AssessorFeedbackController> {

    @Test
    public void testCreateAssessorFeedback() throws Exception {

        BiFunction<AssessorFeedbackService, FileEntryResource, ServiceResult<FileEntryResource>> serviceCall =
                (service, fileToUpload) -> service.createAssessorFeedbackFileEntry(eq(123L), eq(fileToUpload), fileUploadInputStreamExpectations());

        assertFileUploadProcess("/assessorfeedback/assessorFeedbackDocument", new Object[] {},
                asMap("applicationId", "123"),
                assessorFeedbackServiceMock,
                serviceCall);
    }

    @Test
    public void testUpdateAssessorFeedback() throws Exception {

        // having to "fake" the request body as JSON because Spring Restdocs does not support other content types other
        // than JSON and XML
        String dummyContent = "{\"description\":\"The request body is the binary content of the file being uploaded - it is NOT JSON as seen here!\"}";

        FileHeaderAttributes fileAttributesAfterValidation = new FileHeaderAttributes(MediaType.valueOf("application/pdf"), 1000L, "updated.pdf");
        when(fileValidatorMock.validateFileHeaders("application/pdf", "1000", "updated.pdf")).thenReturn(serviceSuccess(fileAttributesAfterValidation));

        FileEntryResource updatedResource = newFileEntryResource().with(id(1111L)).build();
        ServiceResult<FileEntryResource> successResponse = serviceSuccess(updatedResource);

        when(assessorFeedbackServiceMock.updateAssessorFeedbackFileEntry(eq(123L),
                createFileEntryResourceExpectations(fileAttributesAfterValidation),
                fileUploadInputStreamExpectations(dummyContent))).thenReturn(successResponse);

        mockMvc.perform(
                        put("/assessorfeedback/assessorFeedbackDocument").
                                param("applicationId", "123").
                                param("filename", "updated.pdf").
                                header("Content-Type", "application/pdf").
                                header("Content-Length", "1000").
                                header("IFS_AUTH_TOKEN", "123abc").
                                content(dummyContent)
                ).
                andExpect(status().isOk()).
                andExpect(content().string("")).
                andDo(documentUpdateAssessorFeedbackDocument());

        verify(fileValidatorMock).validateFileHeaders("application/pdf", "1000", "updated.pdf");
        verify(assessorFeedbackServiceMock).updateAssessorFeedbackFileEntry(eq(123L), createFileEntryResourceExpectations(fileAttributesAfterValidation), fileUploadInputStreamExpectations(dummyContent));
    }

    @Test
    public void testDeleteAssessorFeedback() throws Exception {

        when(assessorFeedbackServiceMock.deleteAssessorFeedbackFileEntry(123L)).thenReturn(serviceSuccess());

        mockMvc.perform(
                delete("/assessorfeedback/assessorFeedbackDocument").
                        param("applicationId", "123").
                        header("IFS_AUTH_TOKEN", "123abc")
        ).
                andExpect(status().isNoContent()).
                andExpect(content().string("")).
                andDo(documentDeleteAssessorFeedbackDocument());

        verify(assessorFeedbackServiceMock).deleteAssessorFeedbackFileEntry(123L);
    }

    @Test
    public void testGetAssessorFeedbackFileContents() throws Exception {

        FileEntryResource returnedFileEntry = newFileEntryResource().build();

        Supplier<InputStream> inputStreamSupplier = () -> new ByteArrayInputStream("The returned binary file data".getBytes());

        when(assessorFeedbackServiceMock.getAssessorFeedbackFileEntryContents(123L)).thenReturn(serviceSuccess(new BasicFileAndContents(returnedFileEntry, inputStreamSupplier)));

        MvcResult response = mockMvc.
                perform(
                        MockMvcRequestBuilders.get("/assessorfeedback/assessorFeedbackDocument").
                                param("applicationId", "123").
                                header("IFS_AUTH_TOKEN", "123abc")
                ).
                andExpect(status().isOk()).
                andDo(documentGetAssessorFeedbackDocumentationContents()).
                andReturn();

        assertEquals("The returned binary file data", response.getResponse().getContentAsString());
    }

    @Test
    public void testGetAssessorFeedbackFileEntry() throws Exception {

        FileEntryResource returnedFileEntry = newFileEntryResource().
                withName("lookedup.pdf").
                withMediaType("application/pdf").
                withFilesizeBytes(1000).build();

        when(assessorFeedbackServiceMock.getAssessorFeedbackFileEntryDetails(123L)).thenReturn(serviceSuccess(returnedFileEntry));

        mockMvc.
                perform(
                        MockMvcRequestBuilders.get("/assessorfeedback/assessorFeedbackDocument/fileentry").
                                param("applicationId", "123").
                                header("IFS_AUTH_TOKEN", "123abc")
                ).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(returnedFileEntry))).
                andDo(documentGetAssessorFeedbackDocumentationFileEntry());

        verify(assessorFeedbackServiceMock).getAssessorFeedbackFileEntryDetails(123L);

    }
    
    @Test
    public void testAssessorFeedbackUploaded() throws Exception {

        when(assessorFeedbackServiceMock.assessorFeedbackUploaded(123L)).thenReturn(serviceSuccess(true));

        mockMvc.
                perform(
                        MockMvcRequestBuilders.get("/assessorfeedback/assessorFeedbackUploaded").
                                param("competitionId", "123").
                                header("IFS_AUTH_TOKEN", "123abc")
                ).
                andExpect(status().isOk()).
                andExpect(content().string("true")).
                andDo(documentAssessorFeedbackUploaded());

        verify(assessorFeedbackServiceMock).assessorFeedbackUploaded(123L);
    }
    
    @Test
    public void testSubmitAssessorFeedback() throws Exception {

        when(assessorFeedbackServiceMock.submitAssessorFeedback(123L)).thenReturn(serviceSuccess());
        when(assessorFeedbackServiceMock.notifyLeadApplicantsOfAssessorFeedback(123L)).thenReturn(serviceSuccess());

        mockMvc.
                perform(
                        MockMvcRequestBuilders.post("/assessorfeedback/submitAssessorFeedback/123").
                                header("IFS_AUTH_TOKEN", "123abc")
                ).
                andExpect(status().isOk()).
                andExpect(content().string("")).
                andDo(documentSubmitAssessorFeedback());

        verify(assessorFeedbackServiceMock).submitAssessorFeedback(123L);
        verify(assessorFeedbackServiceMock).notifyLeadApplicantsOfAssessorFeedback(123L);
    }

    @Test
    public void testSubmitAssessorFeedbackButSubmissionFailsSoNoEmailsSent() throws Exception {

        when(assessorFeedbackServiceMock.submitAssessorFeedback(123L)).thenReturn(serviceFailure(internalServerErrorError("Urgh!")));

        mockMvc.
                perform(
                        MockMvcRequestBuilders.post("/assessorfeedback/submitAssessorFeedback/123").
                                header("IFS_AUTH_TOKEN", "123abc")
                ).
                andExpect(status().isInternalServerError()).
                andExpect(content().json(toJson(new RestErrorResponse(internalServerErrorError("Urgh!")))));

        verify(assessorFeedbackServiceMock).submitAssessorFeedback(123L);
        verify(assessorFeedbackServiceMock, never()).notifyLeadApplicantsOfAssessorFeedback(123L);
    }

    private RestDocumentationResultHandler documentGetAssessorFeedbackDocumentationFileEntry() {

        return document("assessor-feedback/assessorFeedbackDocument_getFileEntry",
                requestParameters(
                        parameterWithName("applicationId").description("Id of the Application that the FormInputResponse is related to")
                ),
                requestHeaders(
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ),
                responseFields(
                        fieldWithPath("id").description("Id of the FileEntry that was looked up"),
                        fieldWithPath("name").description("Name of the FileEntry that was looked up"),
                        fieldWithPath("mediaType").description("Media type of the FileEntry that was looked up"),
                        fieldWithPath("filesizeBytes").description("File size in bytes of the FileEntry that was looked up")
                ));
    }

    private RestDocumentationResultHandler documentGetAssessorFeedbackDocumentationContents() {

        return document("assessor-feedback/assessorFeedbackDocument_getFileContents",
                requestParameters(
                        parameterWithName("applicationId").description("Id of the Application that the FormInputResponse is related to")
                ),
                requestHeaders(
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ));
    }

    private RestDocumentationResultHandler documentCreateAssessorFeedbackDocument() {

        return document("assessor-feedback/assessorFeedbackDocument_create",
                requestParameters(
                        parameterWithName("applicationId").description("Id of the Application that the Assessor Feedback document is being applied to"),
                        parameterWithName("filename").description("The filename of the file being uploaded")
                ),
                requestHeaders(
                        headerWithName("Content-Type").description("The Content Type of the file being uploaded e.g. application/pdf"),
                        headerWithName("Content-Length").description("The Content Length of the binary file data being uploaded in bytes"),
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ),
                requestFields(fieldWithPath("description").description("The body of the request should be the binary data of the file being uploaded (and NOT JSON as shown in example)")),
                responseFields(
                        fieldWithPath("id").description("Id of the FileEntry that was created"),
                        fieldWithPath("name").description("Name of the FileEntry that was created"),
                        fieldWithPath("mediaType").description("Media type of the FileEntry that was created"),
                        fieldWithPath("filesizeBytes").description("File size in bytes of the FileEntry that was created")
                ));
    }

    private RestDocumentationResultHandler documentUpdateAssessorFeedbackDocument() {

        return document("assessor-feedback/assessorFeedbackDocument_update",
                requestParameters(
                        parameterWithName("applicationId").description("Id of the Application that the Assessor Feedback document is being applied to"),
                        parameterWithName("filename").description("The filename of the file being uploaded")
                ),
                requestHeaders(
                        headerWithName("Content-Type").description("The Content Type of the file being uploaded e.g. application/pdf"),
                        headerWithName("Content-Length").description("The Content Length of the binary file data being uploaded in bytes"),
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ),
                requestFields(fieldWithPath("description").description("The body of the request should be the binary data of the file being uploaded (and NOT JSON as shown in example)")));
    }

    private RestDocumentationResultHandler documentDeleteAssessorFeedbackDocument() {

        return document("assessor-feedback/assessorFeedbackDocument_delete",
                requestParameters(
                        parameterWithName("applicationId").description("Id of the Application that the Assessor Feedback document is being deleted from")
                ),
                requestHeaders(
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ));
    }
    
    private RestDocumentationResultHandler documentAssessorFeedbackUploaded() {
    	return document("assessor-feedback/assessorFeedbackUploaded",
    			requestParameters(
                        parameterWithName("competitionId").description("Id of the competition that we are checking if feedback is uploaded for all submitted applications")
                ),
                requestHeaders(
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ));
    }
    
    private RestDocumentationResultHandler documentSubmitAssessorFeedback() {
    	return document("assessor-feedback/submitAssessorFeedback",
                requestHeaders(
                        headerWithName("IFS_AUTH_TOKEN").description("The authentication token for the logged in user")
                ));
    }

    private FileEntryResource createFileEntryResourceExpectations(FileHeaderAttributes expectedAttributes) {
        return eq(expectedAttributes.toFileEntryResource());
    }

    @Override
    protected AssessorFeedbackController supplyControllerUnderTest() {
        return new AssessorFeedbackController();
    }
}
