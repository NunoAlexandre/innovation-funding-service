package org.innovateuk.ifs.assessment.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.controller.AssessorFormInputResponseController;
import org.innovateuk.ifs.assessment.resource.ApplicationAssessmentAggregateResource;
import org.innovateuk.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.List;

import static org.innovateuk.ifs.assessment.documentation.AssessmentAggregateScoreDocs.applicationAssessmentAggregateResourceFields;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.AssessorFormInputResponseDocs.assessorFormInputResponseFields;
import static org.innovateuk.ifs.documentation.AssessorFormInputResponseDocs.assessorFormInputResponseResourceBuilder;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class AssessorFormInputResponseControllerDocumentation extends BaseControllerMockMVCTest<AssessorFormInputResponseController> {

    private RestDocumentationResultHandler document;

    @Override
    protected AssessorFormInputResponseController supplyControllerUnderTest() {
        return new AssessorFormInputResponseController();
    }

    @Before
    public void setup(){
        this.document = document("assessorFormInputResponse/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Test
    public void getAllAssessorFormInputResponses() throws Exception {
        Long assessmentId = 1L;
        List<AssessorFormInputResponseResource> responses = assessorFormInputResponseResourceBuilder.build(2);
        when(assessorFormInputResponseServiceMock.getAllAssessorFormInputResponses(assessmentId)).thenReturn(serviceSuccess(responses));

        mockMvc.perform(get("/assessorFormInputResponse/assessment/{assessmentId}", assessmentId))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("assessmentId").description("Id of the assessment associated with responses being requested")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of responses the user is allowed to see")
                        )
                ));
    }

    @Test
    public void getAllAssessorFormInputResponsesByAssessmentAndQuestion() throws Exception {
        Long assessmentId = 1L;
        Long questionId = 2L;
        List<AssessorFormInputResponseResource> responses = assessorFormInputResponseResourceBuilder.build(2);
        when(assessorFormInputResponseServiceMock.getAllAssessorFormInputResponsesByAssessmentAndQuestion(assessmentId, questionId)).thenReturn(serviceSuccess(responses));

        mockMvc.perform(get("/assessorFormInputResponse/assessment/{assessmentId}/question/{questionId}", assessmentId, questionId))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("assessmentId").description("Id of the assessment associated with responses being requested"),
                                parameterWithName("questionId").description("Id of the question associated with responses being requested")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of responses the user is allowed to see")
                        )
                ));
    }

    @Test
    public void updateAssessorFormInputResponse() throws Exception {
        AssessorFormInputResponseResource response = assessorFormInputResponseResourceBuilder.build();

        when(assessorFormInputResponseServiceMock.updateFormInputResponse(response)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/assessorFormInputResponse")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().is2xxSuccessful())
                .andDo(this.document.snippets(
                        requestFields(assessorFormInputResponseFields)
                ));
    }

    @Test
    public void getApplicationAggregateScores() throws Exception {
        long applicationId = 1;
        ApplicationAssessmentAggregateResource response = new ApplicationAssessmentAggregateResource();

        when(assessorFormInputResponseServiceMock.getApplicationAggregateScores(applicationId)).thenReturn(serviceSuccess(response));

        mockMvc.perform(get("/assessorFormInputResponse/application/{applicationId}/scores", applicationId))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("applicationId").description("Id of the application associated with the aggregate scores being requested")
                        ),
                        responseFields(applicationAssessmentAggregateResourceFields)
                ));
    }
}
