package org.innovateuk.ifs.assessment.viewmodel;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.form.resource.FormInputResource;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.lowerCase;

/**
 * Holder of model attributes for the feedback given as part of the assessment journey to a question for an application.
 */
public class AssessmentFeedbackViewModel {

    private Long assessmentId;
    private long daysLeft;
    private long daysLeftPercentage;
    private CompetitionResource competition;
    private ApplicationResource application;
    private Long questionId;
    private String questionNumber;
    private String questionShortName;
    private String questionName;
    private Integer maximumScore;
    private String applicantResponse;
    private List<FormInputResource> assessmentFormInputs;
    private boolean scoreFormInputExists;
    private boolean scopeFormInputExists;
    private boolean appendixExists;
    private FileDetailsViewModel appendixDetails;
    private List<ResearchCategoryResource> researchCategories;

    public AssessmentFeedbackViewModel(Long assessmentId, long daysLeft, long daysLeftPercentage, CompetitionResource competition, ApplicationResource application, Long questionId, String questionNumber, String questionShortName, String questionName, Integer maximumScore, String applicantResponse, List<FormInputResource> assessmentFormInputs, boolean scoreFormInputExists, boolean scopeFormInputExists, List<ResearchCategoryResource> researchCategories) {
        this(assessmentId, daysLeft, daysLeftPercentage, competition, application, questionId, questionNumber, questionShortName, questionName, maximumScore, applicantResponse, assessmentFormInputs, scoreFormInputExists, scopeFormInputExists, false, null, researchCategories);
    }

    public AssessmentFeedbackViewModel(Long assessmentId, long daysLeft, long daysLeftPercentage, CompetitionResource competition, ApplicationResource application, Long questionId, String questionNumber, String questionShortName, String questionName, Integer maximumScore, String applicantResponse, List<FormInputResource> assessmentFormInputs, boolean scoreFormInputExists, boolean scopeFormInputExists, boolean appendixExists, FileDetailsViewModel appendixDetails, List<ResearchCategoryResource> researchCategories) {
        this.assessmentId = assessmentId;
        this.daysLeft = daysLeft;
        this.daysLeftPercentage = daysLeftPercentage;
        this.competition = competition;
        this.application = application;
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.questionShortName = questionShortName;
        this.questionName = questionName;
        this.maximumScore = maximumScore;
        this.applicantResponse = applicantResponse;
        this.assessmentFormInputs = assessmentFormInputs;
        this.scoreFormInputExists = scoreFormInputExists;
        this.scopeFormInputExists = scopeFormInputExists;
        this.appendixExists = appendixExists;
        this.appendixDetails = appendixDetails;
        this.researchCategories = researchCategories;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public long getDaysLeft() {
        return daysLeft;
    }

    public long getDaysLeftPercentage() {
        return daysLeftPercentage;
    }

    public CompetitionResource getCompetition() {
        return competition;
    }

    public ApplicationResource getApplication() {
        return application;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionShortName() {
        return questionShortName;
    }

    public String getQuestionName() {
        return questionName;
    }

    public Integer getMaximumScore() {
        return maximumScore;
    }

    public String getApplicantResponse() {
        return applicantResponse;
    }

    public List<FormInputResource> getAssessmentFormInputs() {
        return assessmentFormInputs;
    }

    public boolean isScoreFormInputExists() {
        return scoreFormInputExists;
    }

    public boolean isScopeFormInputExists() {
        return scopeFormInputExists;
    }

    public boolean isAppendixExists() {
        return appendixExists;
    }

    public FileDetailsViewModel getAppendixDetails() {
        return appendixDetails;
    }

    public String getAppendixFileDescription() {
        return format("View %s appendix", lowerCase(getQuestionShortName()));
    }

    public List<ResearchCategoryResource> getResearchCategories() {
        return researchCategories;
    }
}
