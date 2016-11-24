package com.worth.ifs.assessment.transactional;

import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.resource.ApplicationRejectionResource;
import com.worth.ifs.assessment.resource.AssessmentFundingDecisionResource;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.workflow.configuration.AssessmentWorkflowHandler;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.workflow.domain.ActivityState;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static com.worth.ifs.assessment.builder.ApplicationRejectionResourceBuilder.newApplicationRejectionResource;
import static com.worth.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static com.worth.ifs.assessment.builder.AssessmentFundingDecisionResourceBuilder.newAssessmentFundingDecisionResource;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.AssessmentTotalScoreResourceBuilder.newAssessmentTotalScoreResource;
import static com.worth.ifs.assessment.resource.AssessmentStates.OPEN;
import static com.worth.ifs.assessment.resource.AssessmentStates.PENDING;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.workflow.domain.ActivityType.APPLICATION_ASSESSMENT;
import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class AssessmentServiceImplTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private AssessmentService assessmentService = new AssessmentServiceImpl();

    @Mock
    private AssessmentWorkflowHandler assessmentWorkflowHandler;

    @Test
    public void findById() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment().build();
        AssessmentResource expected = newAssessmentResource().build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentMapperMock.mapToResource(same(assessment))).thenReturn(expected);

        AssessmentResource found = assessmentService.findById(assessmentId).getSuccessObject();

        assertSame(expected, found);
        verify(assessmentRepositoryMock, only()).findOne(assessmentId);
    }

    @Test
    public void findByUserAndCompetition() throws Exception {
        Long userId = 2L;
        Long competitionId = 1L;

        List<Assessment> assessments = newAssessment().build(2);
        List<AssessmentResource> expected = newAssessmentResource().build(2);

        when(assessmentRepositoryMock.findByParticipantUserIdAndParticipantApplicationCompetitionIdOrderByActivityStateStateAscIdAsc(userId, competitionId)).thenReturn(assessments);
        when(assessmentMapperMock.mapToResource(same(assessments.get(0)))).thenReturn(expected.get(0));
        when(assessmentMapperMock.mapToResource(same(assessments.get(1)))).thenReturn(expected.get(1));

        List<AssessmentResource> found = assessmentService.findByUserAndCompetition(userId, competitionId).getSuccessObject();

        assertEquals(expected, found);
        verify(assessmentRepositoryMock, only()).findByParticipantUserIdAndParticipantApplicationCompetitionIdOrderByActivityStateStateAscIdAsc(userId, competitionId);
    }

    @Test
    public void getTotalScore() throws Exception {
        Long assessmentId = 1L;

        AssessmentTotalScoreResource expected = newAssessmentTotalScoreResource()
                .withTotalScoreGiven(55)
                .withTotalScorePossible(100)
                .build();

        when(assessmentRepositoryMock.getTotalScore(assessmentId)).thenReturn(new AssessmentTotalScoreResource(55, 100));

        assertEquals(expected, assessmentService.getTotalScore(assessmentId).getSuccessObject());

        verify(assessmentRepositoryMock, only()).getTotalScore(assessmentId);
    }

    @Test
    public void recommend() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, OPEN.getBackingState()))
                .build();

        AssessmentFundingDecisionResource assessmentFundingDecision = newAssessmentFundingDecisionResource().build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.fundingDecision(assessment, assessmentFundingDecision)).thenReturn(true);

        ServiceResult<Void> result = assessmentService.recommend(assessmentId, assessmentFundingDecision);
        assertTrue(result.isSuccess());

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler, calls(1)).fundingDecision(assessment, assessmentFundingDecision);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void recommend_eventNotAccepted() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, OPEN.getBackingState()))
                .build();

        AssessmentFundingDecisionResource assessmentFundingDecision = newAssessmentFundingDecisionResource().build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.fundingDecision(assessment, assessmentFundingDecision)).thenReturn(false);

        ServiceResult<Void> result = assessmentService.recommend(assessmentId, assessmentFundingDecision);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(ASSESSMENT_RECOMMENDATION_FAILED));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler).fundingDecision(assessment, assessmentFundingDecision);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, OPEN.getBackingState()))
                .build();

        ApplicationRejectionResource applicationRejectionResource = newApplicationRejectionResource().build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.rejectInvitation(assessment, applicationRejectionResource)).thenReturn(true);

        ServiceResult<Void> result = assessmentService.rejectInvitation(assessmentId, applicationRejectionResource);
        assertTrue(result.isSuccess());

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler).rejectInvitation(assessment, applicationRejectionResource);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation_eventNotAccepted() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, OPEN.getBackingState()))
                .build();

        ApplicationRejectionResource applicationRejectionResource = newApplicationRejectionResource().build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.rejectInvitation(assessment, applicationRejectionResource)).thenReturn(false);

        ServiceResult<Void> result = assessmentService.rejectInvitation(assessmentId, applicationRejectionResource);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(ASSESSMENT_REJECTION_FAILED));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler).rejectInvitation(assessment, applicationRejectionResource);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvitation() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, PENDING.getBackingState()))
                .build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.acceptInvitation(assessment)).thenReturn(true);

        ServiceResult<Void> result = assessmentService.acceptInvitation(assessmentId);
        assertTrue(result.isSuccess());

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler).acceptInvitation(assessment);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvitation_eventNotAccepted() throws Exception {
        Long assessmentId = 1L;

        Assessment assessment = newAssessment()
                .withId(assessmentId)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, PENDING.getBackingState()))
                .build();

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(assessmentWorkflowHandler.acceptInvitation(assessment)).thenReturn(false);

        ServiceResult<Void> result = assessmentService.acceptInvitation(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(ASSESSMENT_ACCEPT_FAILED));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findOne(assessmentId);
        inOrder.verify(assessmentWorkflowHandler, calls(1)).acceptInvitation(assessment);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void submitAssessments() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissions = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();
        List<Assessment> assessments = newAssessment()
                .withId(1L, 2L)
                .withActivityState(
                        new ActivityState(APPLICATION_ASSESSMENT, READY_TO_SUBMIT.getBackingState()),
                        new ActivityState(APPLICATION_ASSESSMENT, READY_TO_SUBMIT.getBackingState())
                )
                .build(2);

        assertEquals(2, assessmentSubmissions.getAssessmentIds().size());

        when(assessmentRepositoryMock.findAll(assessmentSubmissions.getAssessmentIds())).thenReturn(assessments);

        when(assessmentWorkflowHandler.submit(assessments.get(0))).thenAnswer(invocation -> {
            assessments.get(0).setActivityState(new ActivityState(APPLICATION_ASSESSMENT, SUBMITTED.getBackingState()));
            return Boolean.TRUE;
        });
        when(assessmentWorkflowHandler.submit(assessments.get(1))).thenAnswer(invocation -> {
            assessments.get(1).setActivityState(new ActivityState(APPLICATION_ASSESSMENT, SUBMITTED.getBackingState()));
            return Boolean.TRUE;
        });

        ServiceResult<Void> result = assessmentService.submitAssessments(assessmentSubmissions);
        assertTrue(result.isSuccess());

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findAll(assessmentSubmissions.getAssessmentIds());
        inOrder.verify(assessmentWorkflowHandler, calls(1)).submit(assessments.get(0));
        inOrder.verify(assessmentWorkflowHandler, calls(1)).submit(assessments.get(1));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void submitAssessments_eventNotAccepted() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissions = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L))
                .build();

        Application application = new Application();
        application.setName("Test Application");

        Assessment assessment = newAssessment()
                .withId(1L)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, PENDING.getBackingState()))
                .with((resource) -> resource.setTarget(application))
                .build();

        assertEquals(1, assessmentSubmissions.getAssessmentIds().size());

        when(assessmentRepositoryMock.findAll(assessmentSubmissions.getAssessmentIds())).thenReturn(singletonList(assessment));
        when(assessmentWorkflowHandler.submit(assessment)).thenReturn(false);

        ServiceResult<Void> result = assessmentService.submitAssessments(assessmentSubmissions);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0), new Error(ASSESSMENT_SUBMIT_FAILED, 1L, "Test Application"));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findAll(assessmentSubmissions.getAssessmentIds());
        inOrder.verify(assessmentWorkflowHandler, calls(1)).submit(assessment);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void submitAssessments_notFound() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissions = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();

        when(assessmentRepositoryMock.findAll(assessmentSubmissions.getAssessmentIds())).thenReturn(emptyList());

        ServiceResult<Void> result = assessmentService.submitAssessments(assessmentSubmissions);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Assessment.class, 1L), notFoundError(Assessment.class, 2L)));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findAll(assessmentSubmissions.getAssessmentIds());
        inOrder.verify(assessmentWorkflowHandler, never()).submit(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void submitAssessments_notFoundAndEventNotAccepted() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissions = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();

        Application application = newApplication()
                .withName("Test Application")
                .build();

        Assessment assessment = newAssessment()
                .withId(1L)
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, PENDING.getBackingState()))
                .with((resource) -> resource.setTarget(application))
                .build();

        assertEquals(2, assessmentSubmissions.getAssessmentIds().size());

        when(assessmentRepositoryMock.findAll(assessmentSubmissions.getAssessmentIds())).thenReturn(asList(assessment));
        when(assessmentWorkflowHandler.submit(assessment)).thenReturn(false);

        ServiceResult<Void> result = assessmentService.submitAssessments(assessmentSubmissions);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(new Error(ASSESSMENT_SUBMIT_FAILED, 1L, "Test Application"), notFoundError(Assessment.class, 2L)));

        InOrder inOrder = inOrder(assessmentRepositoryMock, assessmentWorkflowHandler);
        inOrder.verify(assessmentRepositoryMock, calls(1)).findAll(assessmentSubmissions.getAssessmentIds());
        inOrder.verify(assessmentWorkflowHandler, calls(1)).submit(assessment);
        inOrder.verifyNoMoreInteractions();
    }
}