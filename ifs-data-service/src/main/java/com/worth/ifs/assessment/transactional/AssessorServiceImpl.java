package com.worth.ifs.assessment.transactional;

import com.worth.ifs.application.domain.AssessorFeedback;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.assessment.dto.Feedback;
import com.worth.ifs.assessment.security.FeedbackLookup;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.UserRoleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static com.worth.ifs.application.transactional.ServiceFailureKeys.GENERAL_INCORRECT_TYPE;
import static com.worth.ifs.commons.service.ServiceResult.*;

/**
 * Service to handle crosscutting business processes related to Assessors and their role within the system.
 *
 * Created by dwatson on 06/10/15.
 */
@Service
public class AssessorServiceImpl extends BaseTransactionalService implements AssessorService {

    @Autowired
    FeedbackLookup feedbackLookup;

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(AssessorServiceImpl.class);

    @Override
    public ServiceResult<Feedback> updateAssessorFeedback(Feedback feedback) {

        BiFunction<ProcessRole, Response, ServiceResult<Feedback>> updateFeedback = (role, response) -> {
            AssessorFeedback responseFeedback = response.getOrCreateResponseAssessorFeedback(role);
            responseFeedback.setAssessmentValue(feedback.getValue().orElse(null));
            responseFeedback.setAssessmentFeedback(feedback.getText().orElse(null));
            responseRepository.save(response);
            return serviceSuccess(feedback);
        };

        return handlingErrors(() -> getResponse(feedback.getResponseId()).
            map(response -> getProcessRole(feedback.getAssessorProcessRoleId()).
            map(processRole -> validateProcessRoleCorrectType(processRole, UserRoleType.ASSESSOR).
            map(assessorRole -> validateProcessRoleInApplication(response, processRole).
            map(roleInApplication -> updateFeedback.apply(assessorRole, response))
        ))));
    }

    @Override
    public ServiceResult<Feedback> getFeedback(Feedback.Id id) {
        return handlingErrors(() -> {
            Feedback feedback = feedbackLookup.getFeedback(id);
            return serviceSuccess(feedback);
        });
    }

    /**
     * Validate that the given ProcessRole is correctly related to the given Application.
     *
     * @param response
     * @param processRole
     * @return
     */
    private ServiceResult<ProcessRole> validateProcessRoleInApplication(Response response, ProcessRole processRole) {
        return response.getApplication().getId().equals(processRole.getApplication().getId()) ? serviceSuccess(processRole) : serviceFailure(new Error(GENERAL_INCORRECT_TYPE, ProcessRole.class, processRole.getId()));
    }

    /**
     * Validate that the given ProcessRole is of the expected type.
     *
     * @param processRole
     * @param type
     * @return
     */
    private ServiceResult<ProcessRole> validateProcessRoleCorrectType(ProcessRole processRole, UserRoleType type) {
        return processRole.getRole().getName().equals(type.getName()) ? serviceSuccess(processRole) : serviceFailure(new Error(GENERAL_INCORRECT_TYPE, ProcessRole.class, processRole.getId()));
    }
}
