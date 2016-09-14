package com.worth.ifs.project.workflow.projectdetails.actions;

import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.workflow.domain.ActivityState;
import com.worth.ifs.workflow.domain.ProcessOutcome;

import java.util.Optional;

/**
 * The {@code SubmitAction} is used by the assessor. It handles the submit event
 * for an application during assessment.
 * For more info see {@link com.worth.ifs.assessment.workflow.AssessorWorkflowConfig}
 */
public class ProjectCreatedAction extends BaseProjectDetailsAction {

    @Override
    protected void doExecute(Assessment assessment, ActivityState newState, Optional<ProcessOutcome> updatedProcessOutcome) {
        assessment.setActivityState(newState);
        projectRepository.save(assessment);
    }
}
