package com.worth.ifs.assessment.service;

import com.worth.ifs.assessment.resource.ApplicationRejectionResource;
import com.worth.ifs.assessment.resource.AssessmentFundingDecisionResource;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.commons.service.ParameterizedTypeReferences;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.assessmentResourceListType;
import static java.lang.String.format;

/**
 * AssessmentFeedbackRestServiceImpl is a utility for CRUD operations on {@link com.worth.ifs.assessment.resource.AssessmentResource}.
 * This class connects to the {com.worth.ifs.assessment.controller.AssessmentController}
 * through a REST call.
 */
@Service
public class AssessmentRestServiceImpl extends BaseRestService implements AssessmentRestService {

    private String assessmentRestURL = "/assessment";

    protected void setAssessmentRestURL(final String assessmentRestURL) {
        this.assessmentRestURL = assessmentRestURL;
    }

    @Override
    public RestResult<AssessmentResource> getById(final Long id) {
        return getWithRestResult(format("%s/%s", assessmentRestURL, id), AssessmentResource.class);
    }

    @Override
    public RestResult<List<AssessmentResource>> getByUserAndCompetition(Long userId, Long competitionId) {
        return getWithRestResult(format("%s/user/%s/competition/%s", assessmentRestURL, userId, competitionId), ParameterizedTypeReferences.assessmentResourceListType());
    }

    @Override
    public RestResult<AssessmentTotalScoreResource> getTotalScore(Long id) {
        return getWithRestResult(format("%s/%s/score", assessmentRestURL, id), AssessmentTotalScoreResource.class);
    }

    @Override
    public RestResult<Void> recommend(Long id, AssessmentFundingDecisionResource assessmentFundingDecision) {
        return putWithRestResult(format("%s/%s/recommend", assessmentRestURL, id), assessmentFundingDecision, Void.class);
    }

    @Override
    public RestResult<Void> rejectInvitation(Long id, ApplicationRejectionResource applicationRejection) {
        return putWithRestResult(format("%s/%s/rejectInvitation", assessmentRestURL, id), applicationRejection, Void.class);
    }

    @Override
    public RestResult<Void> acceptInvitation(Long id) {
        return putWithRestResult(format("%s/%s/acceptInvitation", assessmentRestURL, id), Void.class);
    }

    @Override
    public RestResult<Void> submitAssessments(AssessmentSubmissionsResource assessmentSubmissions) {
        return putWithRestResult(format("%s/submitAssessments", assessmentRestURL), assessmentSubmissions, Void.class);
    }
}