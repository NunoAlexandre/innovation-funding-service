package com.worth.ifs.competition.transactional;

import com.google.common.collect.Lists;
import com.worth.ifs.application.domain.*;
import com.worth.ifs.application.repository.*;
import com.worth.ifs.assessment.resource.*;
import com.worth.ifs.commons.service.*;
import com.worth.ifs.competition.resource.*;
import com.worth.ifs.form.domain.*;
import com.worth.ifs.form.mapper.*;
import com.worth.ifs.form.repository.*;
import com.worth.ifs.form.resource.*;
import com.worth.ifs.transactional.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for operations around the usage and processing of Competitions questions in setup.
 */
@Service
public class CompetitionSetupQuestionServiceImpl extends BaseTransactionalService implements CompetitionSetupQuestionService {
    
	private static final Log LOG = LogFactory.getLog(CompetitionSetupQuestionServiceImpl.class);

    //TODO INFUND-6283 Remove this hard coded strings and expose to UI.
    private static String APPENDIX_GUIDANCE_QUESTION = "What should I include in the appendix?";
    private static String APPENDIX_GUIDANCE_ANSWER = "<p>You may include an appendix of additional information to support the question.</p>" +
                                                     "<p>You may include, for example, a Gantt chart or project management structure.</p>" +
                                                     "<p>The appendix should:</p>" +
                                                     "<ul class=\"list-bullet\"><li>be in a portable document format (.pdf)</li>" +
                                                     "<li>be readable with 100% magnification</li>" +
                                                     "<li>contain your application number and project title at the top</li>" +
                                                     "<li>not be any longer than 6 sides of A4. Longer appendices will only have the first 6 pages assessed</li><" +
                                                     "li>be less than 1mb in size</li>" +
                                                     "</ul>";
    private static String APPENDIX_DESCRIPTION = "Appendix";

    public static String SCOPE_IDENTIFIER = "Scope";
    private static String PROJECT_SUMMARY_IDENTIFIER = "Project summary";
    private static String PUBLIC_DESCRIPTION_IDENTIFIER = "Public description";

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private FormInputTypeRepository formInputTypeRepository;

    @Autowired
    private GuidanceRowMapper guidanceRowMapper;

    @Override
    public ServiceResult<CompetitionSetupQuestionResource> getByQuestionId(Long questionId) {
        Question question = questionRepository.findOne(questionId);
        CompetitionSetupQuestionResource setupResource = new CompetitionSetupQuestionResource();

        //Set form input toggles to false. They will be set to true if form inputs are found in db.
        setupResource.setWrittenFeedback(false);
        setupResource.setAppendix(false);
        setupResource.setScored(false);
        setupResource.setResearchCategoryQuestion(false);
        setupResource.setScope(false);

        question.getFormInputs().forEach(formInput -> {
            if(FormInputScope.ASSESSMENT.equals(formInput.getScope())) {
                mapAssessmentFormInput(formInput, setupResource);
            } else {
                mapApplicationFormInput(formInput, setupResource);
            }
        });

        setupResource.setScoreTotal(question.getAssessorMaximumScore());
        setupResource.setNumber(question.getQuestionNumber());
        setupResource.setShortTitle(question.getShortName());
        setupResource.setTitle(question.getName());
        setupResource.setSubTitle(question.getDescription());
        setupResource.setQuestionId(question.getId());
        setupResource.setType(typeFromQuestion(question));

        return ServiceResult.serviceSuccess(setupResource);
    }

    //TODO INFUND-6282 Remove this type and replace with an active, inactive, null checks on UI.
    private CompetitionSetupQuestionType typeFromQuestion(Question question) {
        if (question.getShortName().equals(SCOPE_IDENTIFIER)) {
            return CompetitionSetupQuestionType.SCOPE;
        } else if (question.getShortName().equals(PROJECT_SUMMARY_IDENTIFIER)) {
            return CompetitionSetupQuestionType.PROJECT_SUMMARY;
        } else if (question.getShortName().equals(PUBLIC_DESCRIPTION_IDENTIFIER)) {
            return CompetitionSetupQuestionType.PUBLIC_DESCRIPTION;
        } else {
            return CompetitionSetupQuestionType.ASSESSED_QUESTION;
        }
    }

    private void mapApplicationFormInput(FormInput formInput, CompetitionSetupQuestionResource setupResource) {
        if (ApplicantFormInputType.FILE_UPLOAD.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setAppendix(true);
        } else if (ApplicantFormInputType.QUESTION.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setGuidanceTitle(formInput.getGuidanceQuestion());
            setupResource.setGuidance(formInput.getGuidanceAnswer());
            setupResource.setMaxWords(wordCountWithDefault(formInput.getWordCount()));
        }
    }

    private void mapAssessmentFormInput(FormInput formInput, CompetitionSetupQuestionResource setupResource) {
        if (AssessorFormInputType.FEEDBACK.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setWrittenFeedback(true);
            setupResource.setAssessmentMaxWords(wordCountWithDefault(formInput.getWordCount()));
            setupResource.setAssessmentGuidance(formInput.getGuidanceQuestion());
            setupResource.setGuidanceRows(Lists.newArrayList(guidanceRowMapper.mapToResource(formInput.getGuidanceRows())));
        } else if (AssessorFormInputType.SCORE.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setScored(true);
        } else if (AssessorFormInputType.APPLICATION_IN_SCOPE.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setScope(true);
        } else if (AssessorFormInputType.RESEARCH_CATEGORY.getTitle().equals(formInput.getFormInputType().getTitle())) {
            setupResource.setResearchCategoryQuestion(true);
        }
    }

    @Override
    public ServiceResult<CompetitionSetupQuestionResource> save(CompetitionSetupQuestionResource competitionSetupQuestionResource) {
        Long questionId = competitionSetupQuestionResource.getQuestionId();
        Question question = questionRepository.findOne(questionId);

        question.setShortName(competitionSetupQuestionResource.getShortTitle());
        question.setName(competitionSetupQuestionResource.getTitle());
        question.setDescription(competitionSetupQuestionResource.getSubTitle());
        question.setAssessorMaximumScore(competitionSetupQuestionResource.getScoreTotal());

        FormInput questionFormInput = formInputRepository.findByQuestionIdAndScopeAndFormInputTypeTitle(questionId, FormInputScope.APPLICATION, ApplicantFormInputType.QUESTION.getTitle());
        questionFormInput.setGuidanceQuestion(competitionSetupQuestionResource.getGuidanceTitle());
        questionFormInput.setGuidanceAnswer(competitionSetupQuestionResource.getGuidance());
        questionFormInput.setWordCount(competitionSetupQuestionResource.getMaxWords());

        createOrDeleteAppendixFormInput(questionId, competitionSetupQuestionResource, question, questionFormInput);
        createOrDeleteScoredFormInput(questionId, competitionSetupQuestionResource, question, questionFormInput);
        createOrDeleteWrittenFeedbackFormInput(questionId, competitionSetupQuestionResource, question, questionFormInput);

        //TODO INFUND-5685 and INFUND-5631 Save assessor form inputs for AssessorFormInputTypes

        return ServiceResult.serviceSuccess(competitionSetupQuestionResource);
    }

    private void createOrDeleteAppendixFormInput(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource, Question question, FormInput questionFormInput) {
        FormInput appendixFormInput = formInputRepository.findByQuestionIdAndScopeAndFormInputTypeTitle(questionId, FormInputScope.APPLICATION, ApplicantFormInputType.FILE_UPLOAD.getTitle());
        if (competitionSetupQuestionResource.getAppendix()) {
            if (appendixFormInput == null) {
                appendixFormInput = new FormInput();
                appendixFormInput.setScope(FormInputScope.APPLICATION);
                appendixFormInput.setFormInputType(formInputTypeRepository.findOneByTitle(ApplicantFormInputType.FILE_UPLOAD.getTitle()));
                appendixFormInput.setQuestion(question);
                appendixFormInput.setGuidanceQuestion(APPENDIX_GUIDANCE_QUESTION);
                appendixFormInput.setGuidanceQuestion(APPENDIX_GUIDANCE_ANSWER);
                appendixFormInput.setDescription(APPENDIX_DESCRIPTION);
                appendixFormInput.setIncludedInApplicationSummary(true);
                appendixFormInput.setCompetition(question.getCompetition());
                if (questionFormInput != null) {
                    appendixFormInput.setPriority(questionFormInput.getPriority() + 1);
                } else {
                    appendixFormInput.setPriority(0);
                }
                formInputRepository.save(appendixFormInput);
            }
        } else if (appendixFormInput != null) {
            formInputRepository.delete(appendixFormInput);
        }
    }

    private void createOrDeleteScoredFormInput(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource, Question question, FormInput questionFormInput) {

        FormInput scoredFormInput = formInputRepository.findByQuestionIdAndScopeAndFormInputTypeTitle(questionId, FormInputScope.APPLICATION, AssessorFormInputType.SCORE.getTitle());

        if (competitionSetupQuestionResource.getScored()) {
            if (scoredFormInput == null) {
                scoredFormInput = new FormInput();
            }
            scoredFormInput.setScope(FormInputScope.APPLICATION);
            scoredFormInput.setFormInputType(formInputTypeRepository.findOneByTitle(AssessorFormInputType.SCORE.getTitle()));
            scoredFormInput.setQuestion(question);
            scoredFormInput.setCompetition(question.getCompetition());

            if (questionFormInput != null) {
                scoredFormInput.setPriority(questionFormInput.getPriority() + 1);
            } else {
                scoredFormInput.setPriority(0);
            }
            formInputRepository.save(scoredFormInput);

        } else if (scoredFormInput != null) {
            formInputRepository.delete(scoredFormInput);
        }
    }

    private void createOrDeleteWrittenFeedbackFormInput(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource, Question question, FormInput questionFormInput) {

        FormInput writtenFeedbackFormInput = formInputRepository.findByQuestionIdAndScopeAndFormInputTypeTitle(questionId, FormInputScope.APPLICATION, AssessorFormInputType.FEEDBACK.getTitle());

        if (competitionSetupQuestionResource.getWrittenFeedback()) {
            if (writtenFeedbackFormInput == null) {

                writtenFeedbackFormInput = new FormInput();
            }
            writtenFeedbackFormInput.setScope(FormInputScope.APPLICATION);
            writtenFeedbackFormInput.setFormInputType(formInputTypeRepository.findOneByTitle(AssessorFormInputType.FEEDBACK.getTitle()));
            writtenFeedbackFormInput.setQuestion(question);
            writtenFeedbackFormInput.setGuidanceQuestion(competitionSetupQuestionResource.getAssessmentGuidance());
            writtenFeedbackFormInput.setWordCount(competitionSetupQuestionResource.getAssessmentMaxWords());
            writtenFeedbackFormInput.setGuidanceRows(Lists.newArrayList(guidanceRowMapper.mapToDomain(competitionSetupQuestionResource.getGuidanceRows())));
            writtenFeedbackFormInput.setCompetition(question.getCompetition());
            if (questionFormInput != null) {
                writtenFeedbackFormInput.setPriority(questionFormInput.getPriority() + 1);
            } else {
                writtenFeedbackFormInput.setPriority(0);
            }
            formInputRepository.save(writtenFeedbackFormInput);
        } else if (writtenFeedbackFormInput != null) {
            formInputRepository.delete(writtenFeedbackFormInput);
        }
    }

    private int wordCountWithDefault(Integer wordCount) {
        if (wordCount != null && wordCount > 0) {
            return wordCount;
        } else {
            return 400;
        }
    }
}
