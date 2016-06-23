package com.worth.ifs.assessment.service;

import com.worth.ifs.assessment.resource.AssessmentFeedbackResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.assessmentFeedbackResourceListType;
import static java.lang.String.format;

/**
 * AssessmentFeedbackRestServiceImpl is a utility for CRUD operations on {@link com.worth.ifs.assessment.domain.AssessmentFeedback}.
 * This class connects to the {@link com.worth.ifs.assessment.controller.AssessmentFeedbackController}
 * through a REST call.
 */
@Service
public class AssessmentFeedbackRestServiceImpl extends BaseRestService implements AssessmentFeedbackRestService {

    private String assessmentFeedbackRestURL = "/assessment-feedback";

    protected void setAssessmentFeedbackRestURL(final String assessmentFeedbackRestURL) {
        this.assessmentFeedbackRestURL = assessmentFeedbackRestURL;
    }

    @Override
    public RestResult<List<AssessmentFeedbackResource>> getAllAssessmentFeedback(final Long assessmentId) {
        return getWithRestResult(format("%s/assessment/%s", assessmentFeedbackRestURL, assessmentId), assessmentFeedbackResourceListType());
    }

    @Override
    public RestResult<AssessmentFeedbackResource> getAssessmentFeedbackByAssessmentAndQuestion(final Long assessmentId, final Long questionId) {
        return getWithRestResult(format("%s/assessment/%s/question/%s", assessmentFeedbackRestURL, assessmentId, questionId), AssessmentFeedbackResource.class);
    }
}