package org.innovateuk.ifs.workflow.controller;

import org.innovateuk.ifs.BaseControllerIntegrationTest;
import org.innovateuk.ifs.workflow.resource.ProcessOutcomeResource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.innovateuk.ifs.assessment.resource.AssessmentOutcomes.FUNDING_DECISION;
import static org.junit.Assert.assertEquals;

public class ProcessOutcomeControllerIntegrationTest extends BaseControllerIntegrationTest<ProcessOutcomeController>  {

    @Override
    @Autowired
    protected void setControllerUnderTest(ProcessOutcomeController controller) {
        this.controller = controller;
    }

    @Test
    public void testGetProcessOutcomeById() {
        Long processOutcomeId = 1L;

        ProcessOutcomeResource processOutcome = controller.findById(processOutcomeId).getSuccessObjectOrThrowException();

        assertEquals("YES",processOutcome.getOutcome());
        assertEquals(FUNDING_DECISION.getType(),processOutcome.getOutcomeType());
    }

    @Test
    public void testGetByProcessId() {
        Long processId = 7L;

        ProcessOutcomeResource processOutcome = controller.findLatestByProcess(processId).getSuccessObjectOrThrowException();

        assertEquals("YES",processOutcome.getOutcome());
        assertEquals(FUNDING_DECISION.getType(),processOutcome.getOutcomeType());

    }

    @Test
    public void testGetByProcessIdAndOutcomeType() {
        Long processId = 5L;

        ProcessOutcomeResource processOutcome = controller.findLatestByProcessAndOutcomeType(processId, FUNDING_DECISION.getType()).getSuccessObjectOrThrowException();

        assertEquals("YES",processOutcome.getOutcome());
        assertEquals(FUNDING_DECISION.getType(),processOutcome.getOutcomeType());
    }
}
