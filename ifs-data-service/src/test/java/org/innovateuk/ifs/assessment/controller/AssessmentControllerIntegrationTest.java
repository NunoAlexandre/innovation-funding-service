package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.BaseControllerIntegrationTest;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.resource.*;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.assessment.builder.ApplicationRejectionResourceBuilder.newApplicationRejectionResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentFundingDecisionResourceBuilder.newAssessmentFundingDecisionResource;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.*;
import static org.innovateuk.ifs.commons.error.CommonErrors.forbiddenError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("Ignored to investigate failures on Bamboo")
public class AssessmentControllerIntegrationTest extends BaseControllerIntegrationTest<AssessmentController> {

    @Before
    public void setUp() throws Exception {
    }

    @Autowired
    @Override
    protected void setControllerUnderTest(final AssessmentController controller) {
        this.controller = controller;
    }

    @Test
    public void findById() throws Exception {
        Long assessmentId = 6L;

        loginFelixWilson();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(assessmentId, assessmentResource.getId());
        assertEquals(Long.valueOf(21L), assessmentResource.getProcessRole());
        assertEquals(Long.valueOf(4L), assessmentResource.getApplication());
        assertEquals(Long.valueOf(1L), assessmentResource.getCompetition());
        assertEquals(emptyList(), assessmentResource.getProcessOutcomes());
    }

    @Test
    public void findById_notFound() throws Exception {
        Long assessmentId = 999L;

        loginPaulPlum();
        RestResult<AssessmentResource> result = controller.findById(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Assessment.class, 999L)));
    }

    @Test
    public void findById_notTheAssessmentOwner() throws Exception {
        Long assessmentId = 5L;

        loginSteveSmith();
        RestResult<AssessmentResource> result = controller.findById(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void findAssignableById() throws Exception {
        Long assessmentId = 4L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findAssignableById(assessmentId).getSuccessObject();
        assertEquals(Long.valueOf(17L), assessmentResource.getProcessRole());
        assertEquals(Long.valueOf(6L), assessmentResource.getApplication());
        assertEquals(Long.valueOf(1L), assessmentResource.getCompetition());
        assertEquals(emptyList(), assessmentResource.getProcessOutcomes());
    }

    @Test
    public void findAssignableById_notFound() throws Exception {
        Long assessmentId = 999L;

        loginPaulPlum();
        RestResult<AssessmentResource> result = controller.findAssignableById(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Assessment.class, 999L)));
    }

    @Test
    public void findAssignableById_notTheAssessmentOwner() throws Exception {
        Long assessmentId = 5L;

        loginSteveSmith();
        RestResult<AssessmentResource> result = controller.findAssignableById(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void findAssignableById_notAssignable() throws Exception {
        Long assessmentId = 6L;

        loginFelixWilson();
        RestResult<AssessmentResource> result = controller.findAssignableById(assessmentId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void findByUserAndCompetition() throws Exception {
        Long userId = 3L;
        Long competitionId = 1L;

        loginPaulPlum();
        RestResult<List<AssessmentResource>> result = controller.findByUserAndCompetition(userId, competitionId);
        assertTrue(result.isSuccess());
        List<AssessmentResource> assessmentResources = result.getSuccessObjectOrThrowException();
        assertEquals(4, assessmentResources.size());
    }

    @Test
    public void countByStateAndCompetition() throws Exception {
        AssessmentStates state = CREATED;
        Long competitionId = 1L;

        loginCompAdmin();
        RestResult<Long> result = controller.countByStateAndCompetition(state, competitionId);
        assertTrue(result.isSuccess());
        long count = result.getSuccessObject();
        assertEquals(1L, count);
    }

    @Test
    public void getTotalScore() throws Exception {
        loginPaulPlum();

        AssessmentTotalScoreResource result = controller.getTotalScore(1L).getSuccessObjectOrThrowException();
        assertEquals(72, result.getTotalScoreGiven());
        assertEquals(100, result.getTotalScorePossible());
    }

    @Test
    public void recommend() throws Exception {
        Long assessmentId = 2L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(OPEN, assessmentResource.getAssessmentState());

        AssessmentFundingDecisionResource assessmentFundingDecision = newAssessmentFundingDecisionResource().build();

        RestResult<Void> result = controller.recommend(assessmentResource.getId(), assessmentFundingDecision);
        assertTrue(result.isSuccess());

        AssessmentResource assessmentResult = controller.findById(assessmentId).getSuccessObject();
        assertEquals(OPEN, assessmentResult.getAssessmentState());
    }

    @Test
    public void rejectInvitation() {
        Long assessmentId = 2L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(OPEN, assessmentResource.getAssessmentState());

        ApplicationRejectionResource applicationRejection = newApplicationRejectionResource().build();

        RestResult<Void> result = controller.rejectInvitation(assessmentResource.getId(), applicationRejection);
        assertTrue(result.isSuccess());

        RestResult<AssessmentResource> assessmentResult = controller.findById(assessmentId);
        assertTrue(assessmentResult.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void rejectInvitation_eventNotAccepted() {
        Long assessmentId = 2L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(OPEN, assessmentResource.getAssessmentState());

        ApplicationRejectionResource applicationRejection = newApplicationRejectionResource().build();

        RestResult<Void> result = controller.rejectInvitation(assessmentResource.getId(), applicationRejection);
        assertTrue(result.isSuccess());

        RestResult<AssessmentResource> assessmentResult = controller.findById(assessmentId);

        assertEquals(assessmentResult.getErrors().get(0).getErrorKey(), GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION.getErrorKey());

        // Now reject the assessment again
        assertTrue(controller.rejectInvitation(assessmentId, applicationRejection).isFailure());
    }

    @Test
    public void accept() throws Exception {
        Long assessmentId = 4L;
        Long processRole = 17L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(AssessmentStates.PENDING, assessmentResource.getAssessmentState());
        assertEquals(processRole, assessmentResource.getProcessRole());

        RestResult<Void> result = controller.acceptInvitation(assessmentResource.getId());
        assertTrue(result.isSuccess());

        AssessmentResource assessmentResult = controller.findById(assessmentId).getSuccessObject();
        assertEquals(ACCEPTED, assessmentResult.getAssessmentState());
    }

    @Test
    public void withdrawAssessment() throws Exception {
        Long assessmentId = 4L;

        loginPaulPlum();
        AssessmentResource assessmentResource = controller.findById(assessmentId).getSuccessObject();
        assertEquals(PENDING, assessmentResource.getAssessmentState());

        loginCompAdmin();
        RestResult<Void> result = controller.withdrawAssessment(assessmentResource.getId());
        assertTrue(result.isSuccess());

        loginPaulPlum();
        RestResult<AssessmentResource> assessmentResult = controller.findById(assessmentId);
        assertTrue(assessmentResult.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void withdrawCreatedAssessment() throws Exception {
        Long assessmentId = 9L;

        loginCompAdmin();
        RestResult<AssessmentResource> assessmentResource = controller.findById(assessmentId);
        assertTrue(assessmentResource.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));

        RestResult<Void> result = controller.withdrawAssessment(assessmentId);
        assertTrue(result.isSuccess());

        RestResult<AssessmentResource> assessmentResult = controller.findById(assessmentId);
        assertTrue(assessmentResult.getFailure().is(notFoundError(Assessment.class, assessmentId)));

    }

    @Test
    public void notifyAssessor() throws Exception {
        Long assessmentId = 9L;

        loginFelixWilson();
        RestResult<AssessmentResource> assessmentResource = controller.findById(assessmentId);
        assertTrue(assessmentResource.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));

        loginCompAdmin();
        RestResult<Void> result = controller.notify(assessmentId);
        assertTrue(result.isSuccess());

        loginFelixWilson();
        AssessmentResource assessmentResult = controller.findById(assessmentId).getSuccessObject();
        assertEquals(PENDING, assessmentResult.getAssessmentState());
    }


}
