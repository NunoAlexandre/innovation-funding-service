package com.worth.ifs.assessment.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.assessment.controller.AssessmentController;
import com.worth.ifs.assessment.resource.ApplicationRejectionResource;
import com.worth.ifs.assessment.resource.AssessmentFundingDecisionResource;
import com.worth.ifs.assessment.resource.AssessmentResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.List;

import static com.worth.ifs.assessment.documentation.ApplicationRejectionDocs.applicationRejectionResourceBuilder;
import static com.worth.ifs.assessment.documentation.ApplicationRejectionDocs.applicationRejectionResourceFields;
import static com.worth.ifs.assessment.documentation.AssessmentFundingDecisionDocs.assessmentFundingDecisionResourceBuilder;
import static com.worth.ifs.assessment.documentation.AssessmentFundingDecisionDocs.assessmentFundingDecisionResourceFields;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.documentation.AssessmentDocs.assessmentFields;
import static com.worth.ifs.documentation.AssessmentDocs.assessmentResourceBuilder;
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

public class AssessmentControllerDocumentation extends BaseControllerMockMVCTest<AssessmentController> {

    private RestDocumentationResultHandler document;

    @Override
    protected AssessmentController supplyControllerUnderTest() {
        return new AssessmentController();
    }

    @Before
    public void setup() {
        this.document = document("assessment/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Test
    public void findById() throws Exception {
        long assessmentId = 1L;
        AssessmentResource assessmentResource = assessmentResourceBuilder.build();

        when(assessmentServiceMock.findById(assessmentId)).thenReturn(serviceSuccess(assessmentResource));

        mockMvc.perform(get("/assessment/{id}", assessmentId))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("Id of the assessment that is being requested")
                        ),
                        responseFields(assessmentFields)
                ));
    }

    @Test
    public void findByUserAndCompetition() throws Exception {
        long userId = 1L;
        long competitionId = 2L;
        List<AssessmentResource> assessmentResources = assessmentResourceBuilder.build(2);

        when(assessmentServiceMock.findByUserAndCompetition(userId, competitionId)).thenReturn(serviceSuccess(assessmentResources));

        mockMvc.perform(get("/assessment/user/{userId}/competition/{competitionId}", userId, competitionId))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("userId").description("Id of the user whose assessments are being requested"),
                                parameterWithName("competitionId").description("Id of the competition associated with the user's assessments")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of assessments the user is allowed to see")
                        )
                ));
    }

    @Test
    public void recommend() throws Exception {
        Long assessmentId = 1L;
        AssessmentFundingDecisionResource assessmentFundingDecision = assessmentFundingDecisionResourceBuilder.build();

        when(assessmentServiceMock.recommend(assessmentId, assessmentFundingDecision)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/assessment/{id}/recommend", assessmentId)
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(assessmentFundingDecision)))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("id of the assessment for which to recommend")
                        ),
                        requestFields(assessmentFundingDecisionResourceFields)
                ));
    }

    @Test
    public void reject() throws Exception {
        Long assessmentId = 1L;
        ApplicationRejectionResource applicationRejection = applicationRejectionResourceBuilder.build();

        when(assessmentServiceMock.rejectInvitation(assessmentId, applicationRejection)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/assessment/{id}/rejectInvitation", assessmentId)
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(applicationRejection)))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("id of the assessment for which to reject")
                        ),
                        requestFields(applicationRejectionResourceFields)
                ));
    }

}
