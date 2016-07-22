package com.worth.ifs.assessment.model;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import com.worth.ifs.assessment.service.AssessorFormInputResponseService;
import com.worth.ifs.assessment.viewmodel.AssessmentFeedbackViewModel;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.file.controller.viewmodel.FileDetailsViewModel;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.form.service.FormInputResponseService;
import com.worth.ifs.form.service.FormInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.worth.ifs.commons.rest.RestResult.aggregate;
import static com.worth.ifs.util.CollectionFunctions.flattenLists;
import static com.worth.ifs.util.CollectionFunctions.simpleToMap;
import static java.util.stream.Collectors.toList;

/**
 * Build the model for Assessment Feedback view.
 */
@Component
public class AssessmentFeedbackModelPopulator {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FormInputService formInputService;

    @Autowired
    private FormInputResponseService formInputResponseService;

    @Autowired
    private AssessorFormInputResponseService assessorFormInputResponseService;

    public AssessmentFeedbackViewModel populateModel(Long assessmentId, Long questionId, ApplicationResource application) {
        CompetitionResource competition = getCompetition(application.getCompetition());
        QuestionResource question = getQuestion(questionId);
        List<FormInputResource> applicationFormInputs = getApplicationFormInputs(questionId);
        boolean appendixFormInputExists = applicationFormInputs.size() > 1;
        FormInputResource applicationFormInput = applicationFormInputs.get(0);
        Map<Long, FormInputResponseResource> applicantResponses = getApplicantResponses(application.getId(), applicationFormInputs);
        FormInputResponseResource applicantResponse = applicantResponses.get(applicationFormInput.getId());
        String applicantResponseValue = applicantResponse != null ? applicantResponse.getValue() : null;
        List<FormInputResource> assessmentFormInputs = getAssessmentFormInputs(questionId);
        Map<Long, AssessorFormInputResponseResource> assessorResponses = getAssessorResponses(assessmentId, questionId);

        if (appendixFormInputExists) {
            FormInputResource appendixFormInput = applicationFormInputs.get(1);
            FormInputResponseResource applicantAppendixResponse = applicantResponses.get(appendixFormInput.getId());
            boolean applicantAppendixResponseExists = applicantAppendixResponse != null;
            if (applicantAppendixResponseExists) {
                FileDetailsViewModel appendixDetails = new FileDetailsViewModel(applicantAppendixResponse.getFilename(), applicantAppendixResponse.getFilesizeBytes());
                return new AssessmentFeedbackViewModel(competition.getAssessmentDaysLeft(), competition.getAssessmentDaysLeftPercentage(), competition, application, question.getId(), question.getQuestionNumber(), question.getShortName(), question.getName(), question.getAssessorMaximumScore(), applicantResponseValue, assessmentFormInputs, assessorResponses, true, appendixDetails);
            }
        }

        return new AssessmentFeedbackViewModel(competition.getAssessmentDaysLeft(), competition.getAssessmentDaysLeftPercentage(), competition, application, question.getId(), question.getQuestionNumber(), question.getShortName(), question.getName(), question.getAssessorMaximumScore(), applicantResponseValue, assessmentFormInputs, assessorResponses);
    }

    private CompetitionResource getCompetition(Long competitionId) {
        return competitionService.getById(competitionId);
    }

    private QuestionResource getQuestion(Long questionId) {
        return questionService.getById(questionId);
    }

    private List<FormInputResource> getApplicationFormInputs(Long questionId) {
        return formInputService.findApplicationInputsByQuestion(questionId);
    }

    private List<FormInputResource> getAssessmentFormInputs(Long questionId) {
        return formInputService.findAssessmentInputsByQuestion(questionId);
    }

    private Map<Long, FormInputResponseResource> getApplicantResponses(Long applicationId, List<FormInputResource> applicationFormInputs) {
        RestResult<List<List<FormInputResponseResource>>> applicantResponses = aggregate(applicationFormInputs
                .stream()
                .map(formInput -> formInputResponseService.getByFormInputIdAndApplication(formInput.getId(), applicationId))
                .collect(toList()));
        return simpleToMap(
                flattenLists(applicantResponses.getSuccessObjectOrThrowException()),
                FormInputResponseResource::getFormInput
        );
    }

    private Map<Long, AssessorFormInputResponseResource> getAssessorResponses(Long assessmentId, Long questionId) {
        return simpleToMap(assessorFormInputResponseService.getAllAssessorFormInputResponsesByAssessmentAndQuestion(assessmentId, questionId),
                AssessorFormInputResponseResource::getFormInput);
    }
}
