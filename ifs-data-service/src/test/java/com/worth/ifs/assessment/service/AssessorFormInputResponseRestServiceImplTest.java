package com.worth.ifs.assessment.service;

import com.worth.ifs.BaseRestServiceUnitTest;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.junit.Test;

import java.util.List;

import static com.worth.ifs.assessment.builder.AssessorFormInputResponseResourceBuilder.newAssessorFormInputResponseResource;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.assessorFormInputResponseResourceListType;
import static java.lang.String.format;
import static org.junit.Assert.assertSame;
import static org.springframework.http.HttpStatus.OK;

public class AssessorFormInputResponseRestServiceImplTest extends BaseRestServiceUnitTest<AssessorFormInputResponseRestServiceImpl> {

    private static String assessorFormInputResponseRestUrl = "/assessment";

    @Override
    protected AssessorFormInputResponseRestServiceImpl registerRestServiceUnderTest() {
        AssessorFormInputResponseRestServiceImpl assessorFormInputResponseRestService = new AssessorFormInputResponseRestServiceImpl();
        assessorFormInputResponseRestService.setAssessorFormInputResponseRestUrl(assessorFormInputResponseRestUrl);
        return assessorFormInputResponseRestService;
    }

    @Test
    public void getAllAssessorFormInputResponses() throws Exception {
        List<AssessorFormInputResponseResource> expected = newAssessorFormInputResponseResource()
                .build(2);

        Long assessmentId = 1L;

        setupGetWithRestResultExpectations(format("%s/assessment/%s", assessorFormInputResponseRestUrl, assessmentId), assessorFormInputResponseResourceListType(), expected, OK);
        List<AssessorFormInputResponseResource> response = service.getAllAssessorFormInputResponses(assessmentId).getSuccessObject();
        assertSame(expected, response);
    }

    @Test
    public void getAllAssessorFormInputResponsesByAssessmentAndQuestion() throws Exception {
        List<AssessorFormInputResponseResource> expected = newAssessorFormInputResponseResource()
                .build(2);

        Long assessmentId = 1L;
        Long questionId = 2L;

        setupGetWithRestResultExpectations(format("%s/assessment/%s/question/%s", assessorFormInputResponseRestUrl, assessmentId, questionId), assessorFormInputResponseResourceListType(), expected, OK);
        List<AssessorFormInputResponseResource> response = service.getAllAssessorFormInputResponsesByAssessmentAndQuestion(assessmentId, questionId).getSuccessObject();
        assertSame(expected, response);
    }
}