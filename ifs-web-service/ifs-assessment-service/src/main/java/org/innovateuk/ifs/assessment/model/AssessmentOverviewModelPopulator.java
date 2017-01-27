package org.innovateuk.ifs.assessment.model;

import org.apache.commons.io.FileUtils;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.innovateuk.ifs.assessment.service.AssessmentService;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseService;
import org.innovateuk.ifs.assessment.viewmodel.AssessmentOverviewAppendixViewModel;
import org.innovateuk.ifs.assessment.viewmodel.AssessmentOverviewQuestionViewModel;
import org.innovateuk.ifs.assessment.viewmodel.AssessmentOverviewSectionViewModel;
import org.innovateuk.ifs.assessment.viewmodel.AssessmentOverviewViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.form.service.FormInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.innovateuk.ifs.form.resource.FormInputType.ASSESSOR_APPLICATION_IN_SCOPE;
import static org.innovateuk.ifs.form.resource.FormInputType.ASSESSOR_SCORE;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

/**
 * Build the model for Assessment Overview view.
 */
@Component
public class AssessmentOverviewModelPopulator {

    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private AssessmentService assessmentService;
    @Autowired
    private AssessorFormInputResponseService assessorFormInputResponseService;
    @Autowired
    private FormInputService formInputService;
    @Autowired
    private FormInputResponseService formInputResponseService;

    public AssessmentOverviewViewModel populateModel(long assessmentId) {
        AssessmentResource assessment = assessmentService.getById(assessmentId);
        CompetitionResource competition = competitionService.getById(assessment.getCompetition());
        List<SectionResource> sections = sectionService.filterParentSections(
                sectionService.getAllByCompetitionId(assessment.getCompetition())
        );
        List<QuestionResource> questions = questionService.findByCompetition(assessment.getCompetition());
        Map<Long, List<FormInputResource>> formInputs = getFormInputsByQuestion(assessment.getCompetition());
        Map<Long, AssessorFormInputResponseResource> responses = getResponsesByFormInput(assessmentId);

        return new AssessmentOverviewViewModel(assessmentId,
                assessment.getApplication(),
                assessment.getApplicationName(),
                assessment.getCompetition(),
                competition.getAssessmentDaysLeftPercentage(),
                competition.getAssessmentDaysLeft(),
                getSections(sections, questions, formInputs, responses),
                getAppendices(assessment.getApplication(), questions)
        );
    }

    private List<AssessmentOverviewSectionViewModel> getSections(List<SectionResource> sections,
                                                                 List<QuestionResource> questions,
                                                                 Map<Long, List<FormInputResource>> formInputs,
                                                                 Map<Long, AssessorFormInputResponseResource> responses) {
        Map<Long, QuestionResource> questionsMap = simpleToMap(questions, QuestionResource::getId, identity());
        return sections.stream()
                .map(sectionResource -> {

                    List<QuestionResource> sectionQuestions = sectionResource.getQuestions().stream()
                            .map(questionsMap::get)
                            .collect(toList());

                    return new AssessmentOverviewSectionViewModel(sectionResource.getId(),
                            sectionResource.getName(),
                            sectionResource.getAssessorGuidanceDescription(),
                            getQuestions(sectionQuestions, formInputs, responses)
                    );
                })
                .collect(toList());
    }

    private List<AssessmentOverviewQuestionViewModel> getQuestions(List<QuestionResource> questions,
                                                                   Map<Long, List<FormInputResource>> formInputs,
                                                                   Map<Long, AssessorFormInputResponseResource> responses) {
        return simpleMap(questions, question -> {
            List<FormInputResource> questionFormInputs = formInputs.getOrDefault(question.getId(), emptyList());
            Optional<FormInputResource> scopeInput = findScopeInput(questionFormInputs);
            Boolean scopeResponse = getResponseValue(scopeInput, responses).map(Boolean::valueOf).orElse(null);
            Optional<FormInputResource> scoreInput = findScoreInput(questionFormInputs);
            String scoreResponse = getResponseValue(scoreInput, responses).orElse(null);
            return new AssessmentOverviewQuestionViewModel(question.getId(),
                    question.getShortName(),
                    question.getQuestionNumber(),
                    question.getAssessorMaximumScore(),
                    !questionFormInputs.isEmpty(),
                    isAssessed(questionFormInputs, responses),
                    scopeResponse,
                    scoreResponse
            );
        });
    }

    private List<AssessmentOverviewAppendixViewModel> getAppendices(long applicationId, List<QuestionResource> questions) {
        List<FormInputResponseResource> applicantResponses = formInputResponseService.getByApplication(applicationId);
        Map<Long, QuestionResource> questionsMap = simpleToMap(questions, QuestionResource::getId, identity());
        return applicantResponses.stream()
                .filter(formInputResponseResource -> formInputResponseResource.getFileEntry() != null)
                .map(formInputResponseResource -> getAppendix(formInputResponseResource, questionsMap))
                .collect(toList());
    }

    private Map<Long, List<FormInputResource>> getFormInputsByQuestion(long competitionId) {
        return formInputService.
                findAssessmentInputsByCompetition(competitionId).stream().collect(groupingBy(FormInputResource::getQuestion));
    }

    private Map<Long, AssessorFormInputResponseResource> getResponsesByFormInput(long assessmentId) {
        return simpleToMap(assessorFormInputResponseService.getAllAssessorFormInputResponses(assessmentId),
                AssessorFormInputResponseResource::getFormInput);
    }

    private boolean isAssessed(List<FormInputResource> formInputs,
                               Map<Long, AssessorFormInputResponseResource> responses) {
        if (formInputs.isEmpty()) {
            return false;
        }
        return formInputs.stream().allMatch(formInputResource ->
                isNotBlank(getResponseValue(formInputResource, responses).orElse(null)));
    }

    private Optional<FormInputResource> findScopeInput(List<FormInputResource> formInputs) {
        return findInputByType(formInputs, ASSESSOR_APPLICATION_IN_SCOPE);
    }

    private Optional<FormInputResource> findScoreInput(List<FormInputResource> formInputs) {
        return findInputByType(formInputs, ASSESSOR_SCORE);
    }

    private Optional<FormInputResource> findInputByType(List<FormInputResource> formInputs, FormInputType formInputType) {
        return simpleFindFirst(formInputs, formInputResource -> formInputType == formInputResource.getType());
    }

    private Optional<String> getResponseValue(Optional<FormInputResource> formInput,
                                              Map<Long, AssessorFormInputResponseResource> responses) {
        return formInput.flatMap(formInputResource -> getResponseValue(formInputResource, responses));
    }

    private Optional<String> getResponseValue(FormInputResource formInput,
                                              Map<Long, AssessorFormInputResponseResource> responses) {
        return ofNullable(responses.get(formInput.getId())).map(AssessorFormInputResponseResource::getValue);
    }

    private AssessmentOverviewAppendixViewModel getAppendix(FormInputResponseResource formInputResponse,
                                                            Map<Long, QuestionResource> questions) {
        QuestionResource question = questions.get(formInputResponse.getQuestion());

        return new AssessmentOverviewAppendixViewModel(
                formInputResponse.getFormInput(),
                ofNullable(question.getShortName()).orElse(question.getName()),
                formInputResponse.getFilename(),
                FileUtils.byteCountToDisplaySize(formInputResponse.getFilesizeBytes())
        );
    }
}
