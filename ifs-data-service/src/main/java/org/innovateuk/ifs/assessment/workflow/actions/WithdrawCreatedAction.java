package org.innovateuk.ifs.assessment.workflow.actions;

import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.workflow.configuration.AssessmentWorkflow;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.workflow.domain.ProcessOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The {@code WithdrawAction} is used by the assessor. If handles the withdrawal event
 * for an application during assessment
 * For more info see {@link AssessmentWorkflow}
 */
@Component
public class WithdrawCreatedAction extends BaseAssessmentAction {

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Override
    protected void doExecute(Assessment assessment, Optional<ProcessOutcome> processOutcome) {

        assessmentRepository.delete(assessment);
        processRoleRepository.delete(assessment.getParticipant());

    }
}
