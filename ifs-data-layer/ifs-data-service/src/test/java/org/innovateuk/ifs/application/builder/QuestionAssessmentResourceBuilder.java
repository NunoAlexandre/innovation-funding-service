package org.innovateuk.ifs.application.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.competition.resource.GuidanceRowResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionResource;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static java.util.Collections.emptyList;

public class QuestionAssessmentResourceBuilder extends BaseBuilder<CompetitionSetupQuestionResource, QuestionAssessmentResourceBuilder> {

    private QuestionAssessmentResourceBuilder(List<BiConsumer<Integer, CompetitionSetupQuestionResource>> newMultiActions) {
        super(newMultiActions);
    }

    @Override
    protected QuestionAssessmentResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionSetupQuestionResource>> actions) {
        return new QuestionAssessmentResourceBuilder(actions);
    }

    public static QuestionAssessmentResourceBuilder newQuestionAssessment() {
        return new QuestionAssessmentResourceBuilder(emptyList())
                .with(uniqueIds());
    }

    public QuestionAssessmentResourceBuilder withId(Long... ids) {
        return withArray((id, address) -> setField("id", id, address), ids);
    }


    public QuestionAssessmentResourceBuilder withScored(Boolean scored) {
        return with(questionAssessment -> setField("scored", scored, questionAssessment));
    }

    public QuestionAssessmentResourceBuilder withScoreTotal(Integer scoreTotal) {
        return with(questionAssessment -> setField("scoreTotal", scoreTotal, questionAssessment));
    }

    public QuestionAssessmentResourceBuilder withWrittenFeedback(Boolean writtenFeedback) {
        return with(questionAssessment -> setField("writtenFeedback", writtenFeedback, questionAssessment));
    }

    public QuestionAssessmentResourceBuilder withGuidance(String guidance) {
        return with(questionAssessment -> setField("guidance", guidance, questionAssessment));
    }

    public QuestionAssessmentResourceBuilder withWordCount(Integer wordCount) {
        return with(questionAssessment -> setField("wordCount", wordCount, questionAssessment));
    }

    public QuestionAssessmentResourceBuilder withScoreRows(List<GuidanceRowResource> scoreRows) {
        return with(questionAssessment -> setField("scoreRows", scoreRows, questionAssessment));
    }

    @Override
    protected CompetitionSetupQuestionResource createInitial() {
        return new CompetitionSetupQuestionResource();
    }
}
