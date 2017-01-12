package org.innovateuk.ifs.assessment.workflow.actions;

import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.workflow.configuration.AssessmentWorkflow;
import org.innovateuk.ifs.workflow.domain.ProcessOutcome;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.innovateuk.ifs.assessment.resource.AssessmentOutcomes.FUNDING_DECISION;
import static java.util.Optional.ofNullable;

/**
 * The {@code FundingDecisionAction} is used when an assessment has been accepted and a funding decision is added by the assessor.
 * For more info see {@link AssessmentWorkflow}
 */
@Component
public class FundingDecisionAction extends BaseAssessmentAction {

    @Override
    protected void doExecute(Assessment assessment, Optional<ProcessOutcome> processOutcome) {
        ProcessOutcome processOutcomeValue = processOutcome.get();
        Optional<ProcessOutcome> existingOutcome = assessment.getLastOutcome(FUNDING_DECISION);

        // Update the existing outcome if it exists
        existingOutcome.ifPresent(existingOutcomeValue -> copyOutcome(processOutcomeValue, existingOutcomeValue));

        // Otherwise use the new outcome
        if (!existingOutcome.isPresent()) {
            processOutcomeValue.setProcess(assessment);
            processOutcomeValue.setOutcomeType(FUNDING_DECISION.getType());
            assessment.getProcessOutcomes().add(processOutcomeValue);
        }
    }

    private void copyOutcome(ProcessOutcome source, ProcessOutcome target) {
        target.setOutcome(source.getOutcome());
        target.setDescription(source.getDescription());
        target.setComment(source.getComment());
    }
}
