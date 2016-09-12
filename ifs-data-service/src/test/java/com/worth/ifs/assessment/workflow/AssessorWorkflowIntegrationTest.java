package com.worth.ifs.assessment.workflow;

import com.worth.ifs.BaseIntegrationTest;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.repository.AssessmentRepository;
import com.worth.ifs.assessment.resource.AssessmentOutcomes;
import com.worth.ifs.assessment.resource.AssessmentStates;
import com.worth.ifs.workflow.domain.ProcessOutcome;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

@Transactional
public class AssessorWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    AssessmentWorkflowEventHandler assessmentWorkflowEventHandler;

    @Autowired
    AssessmentRepository assessmentRepository;

    private static final long PENDING_PROCESS_ROLE = 17L;
    private static final long OPEN_PROCESS_ROLE = 8L;
    private static final long ASSESSED_PROCESS_ROLE = 16L;

    private static final String COMMENT = "comment";
    private static final String DESCRIPTION = "description";

    @Test
    public void testStateChangePendingToOpen() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(PENDING_PROCESS_ROLE);
        assertEquals(AssessmentStates.PENDING.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.acceptInvitation(PENDING_PROCESS_ROLE,assessment);
        Assessment update = assessmentRepository.findOneByParticipantId(PENDING_PROCESS_ROLE);
        assertEquals(AssessmentStates.OPEN.getState(),update.getProcessStatus());
    }

    @Test
    public void testStateChangePendingToRejected() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(PENDING_PROCESS_ROLE);
        assertEquals(AssessmentStates.PENDING.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.rejectInvitation(PENDING_PROCESS_ROLE,assessment,createProcessOutcome());
        Assessment update = assessmentRepository.findOneByParticipantId(PENDING_PROCESS_ROLE);
        assertEquals(AssessmentStates.REJECTED.getState(),update.getProcessStatus());
        assertEquals(COMMENT,update.getLastOutcome().getComment());
        assertEquals(DESCRIPTION,update.getLastOutcome().getDescription());
    }

    @Test
    public void testStateChangeOpenToRejected() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(OPEN_PROCESS_ROLE);
        assertEquals(AssessmentStates.OPEN.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.rejectInvitation(OPEN_PROCESS_ROLE,assessment,createProcessOutcome());
        Assessment update = assessmentRepository.findOneByParticipantId(OPEN_PROCESS_ROLE);
        assertEquals(AssessmentStates.REJECTED.getState(),update.getProcessStatus());
        assertEquals(COMMENT,update.getLastOutcome().getComment());
        assertEquals(DESCRIPTION,update.getLastOutcome().getDescription());
    }

    @Test
    public void testStateChangeOpenToAssessed() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(OPEN_PROCESS_ROLE);
        assertEquals(AssessmentStates.OPEN.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.recommend(OPEN_PROCESS_ROLE,assessment,new ProcessOutcome());
        Assessment update = assessmentRepository.findOneByParticipantId(OPEN_PROCESS_ROLE);
        assertEquals(AssessmentStates.ASSESSED.getState(),update.getProcessStatus());
    }

    @Test
    public void testStateChangeAssessedToRejected() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(ASSESSED_PROCESS_ROLE);
        assertEquals(AssessmentStates.ASSESSED.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.rejectInvitation(ASSESSED_PROCESS_ROLE,assessment,createProcessOutcome());
        Assessment update = assessmentRepository.findOneByParticipantId(ASSESSED_PROCESS_ROLE);
        assertEquals(AssessmentStates.REJECTED.getState(),update.getProcessStatus());
        assertEquals(COMMENT,update.getLastOutcome(AssessmentOutcomes.REJECT).getComment());
        assertEquals(DESCRIPTION,update.getLastOutcome(AssessmentOutcomes.REJECT).getDescription());
    }

    @Test
    public void testStateChangeAssessedToSubmitted() throws Exception {
        Assessment assessment = assessmentRepository.findOneByParticipantId(ASSESSED_PROCESS_ROLE);
        assertEquals(AssessmentStates.ASSESSED.getState(),assessment.getProcessStatus());
        assessmentWorkflowEventHandler.submit(assessment);
        Assessment update = assessmentRepository.findOneByParticipantId(ASSESSED_PROCESS_ROLE);
        assertEquals(AssessmentStates.SUBMITTED.getState(),update.getProcessStatus());
    }

    private ProcessOutcome createProcessOutcome() {
        ProcessOutcome processOutcome = new ProcessOutcome();
        processOutcome.setComment(COMMENT);
        processOutcome.setDescription(DESCRIPTION);
        return processOutcome;
    }
}
