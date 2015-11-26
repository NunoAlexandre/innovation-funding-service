package com.worth.ifs.assessment.workflow.actions;

import com.worth.ifs.assessment.domain.Recommendation;
import com.worth.ifs.assessment.repository.AssessmentRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * The AcceptAction is used by the assessor. It handles the accepting event
 * for an application during assessment.
 * For more info see {@link com.worth.ifs.assessment.workflow.AssessorWorkflowConfig}
 */
@Component
public class AcceptAction implements Action<String, String> {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    AssessmentRepository assessmentRepository;

    @Override
    public void execute(StateContext<String, String> context) {
        Recommendation updatedRecommendation = (Recommendation) context.getMessageHeader("recommendation");
        Long applicationId = (Long) context.getMessageHeader("applicationId");
        Long assessorId = (Long) context.getMessageHeader("assessorId");
        Recommendation recommendation = assessmentRepository.findOneByAssessorIdAndApplicationId(assessorId, applicationId);

        if(recommendation !=null) {
            recommendation.setProcessStatus(context.getTransition().getTarget().getId());
            assessmentRepository.save(recommendation);
        }
    }
}
