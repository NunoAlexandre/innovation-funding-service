package com.worth.ifs.assessment.service;

import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.assessorFormInputResponseResourceListType;
import static java.lang.String.format;

/**
 * AssessorFormInputResponseRestServiceImpl is a utility for CRUD operations on {@link com.worth.ifs.assessment.domain.AssessorFormInputResponse}.
 * This class connects to the {@link com.worth.ifs.assessment.controller.AssessorFormInputResponseController}
 * through a REST call.
 */
@Service
public class AssessorFormInputResponseRestServiceImpl extends BaseRestService implements AssessorFormInputResponseRestService {

    private String assessorFormInputResponseRestUrl = "/assessorFormInputResponse";

    protected void setAssessorFormInputResponseRestUrl(String assessorFormInputResponseRestUrl) {
        this.assessorFormInputResponseRestUrl = assessorFormInputResponseRestUrl;
    }

    @Override
    public RestResult<List<AssessorFormInputResponseResource>> getAllAssessorFormInputResponses(Long assessmentId) {
        return getWithRestResult(format("%s/assessment/%s", assessorFormInputResponseRestUrl, assessmentId), assessorFormInputResponseResourceListType());
    }

    @Override
    public RestResult<List<AssessorFormInputResponseResource>> getAllAssessorFormInputResponsesByAssessmentAndQuestion(Long assessmentId, Long questionId) {
        return getWithRestResult(format("%s/assessment/%s/question/%s", assessorFormInputResponseRestUrl, assessmentId, questionId), assessorFormInputResponseResourceListType());
    }

    @Override
    public RestResult<Void> updateFormInputResponse(Long assessmentId, Long formInputId, String value) {
        return putWithRestResult(format("%s/%s/%s", assessorFormInputResponseRestUrl, formInputId, assessmentId), value, Void.class);
    }
}
