package com.worth.ifs.application.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.competition.domain.Competition;

import static com.worth.ifs.BuilderAmendFunctions.idBasedNames;
import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class QuestionResourceBuilder extends BaseBuilder<QuestionResource, QuestionResourceBuilder> {

    private QuestionResourceBuilder(List<BiConsumer<Integer, QuestionResource>> newMultiActions) {
        super(newMultiActions);
    }

    @Override
    protected QuestionResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, QuestionResource>> actions) {
        return new QuestionResourceBuilder(actions);
    }

    public static QuestionResourceBuilder newQuestionResource() {
        return new QuestionResourceBuilder(emptyList())
                .with(uniqueIds())
                .with(idBasedNames("Section "))
                .withNeedingAssessorScore(true)
                .withPriority(0)
                .withQuestionNumber("1");
    }

    public QuestionResourceBuilder withId(Long... ids) {
        return withArray((id, address) -> setField("id", id, address), ids);
    }

    public QuestionResourceBuilder withName(String... names) {
        return withArray((name, object) -> setField("name", name, object), names);
    }

    public QuestionResourceBuilder withShortName(String... shortNames) {
        return withArray((shortName, object) -> setField("shortName", shortName, object), shortNames);
    }

    public QuestionResourceBuilder withDescription(String... descriptions) {
        return withArray((description, object) -> setField("description", description, object), descriptions);
    }

    public QuestionResourceBuilder withAssessorConfirmationQuestion(String... assessorConfirmationQuestions) {
        return withArray((assessorConfirmationQuestion, object) -> setField("assessorConfirmationQuestion", assessorConfirmationQuestion, object), assessorConfirmationQuestions);
    }

    public QuestionResourceBuilder withCompetition(Long... competitions) {
        return withArray((competition, object) -> setField("competition", competition, object), competitions);
    }

    public QuestionResourceBuilder withSection(Long... sections) {
        return withArray((section, object) -> setField("section", section, object), sections);
    }

    public QuestionResourceBuilder withResponses(List<Long>... responses) {
        return withArray((response, object) -> setField("responses", response, object), responses);
    }

    public QuestionResourceBuilder withQuestionStatuses(List<Long>... questionStatuses) {
        return withArray((questionStatus, object) -> setField("questionStatuses", questionStatus, object), questionStatuses);
    }

    public QuestionResourceBuilder withCosts(List<Long>... costs) {
        return withArray((cost, object) -> setField("costs", cost, object), costs);
    }

    public QuestionResourceBuilder withNeedingAssessorScore(boolean needingAssessorScore) {
        return with(question -> setField("needingAssessorScore", needingAssessorScore, question));
    }

    public QuestionResourceBuilder withQuestionNumber(String value) {
        return with(question -> setField("questionNumber", value, question));
    }

    public QuestionResourceBuilder withPriority(int priority) {
        return with(question -> setField("priority", priority, question));
    }

    public QuestionResourceBuilder withPriority(Function<Integer, Integer> prioritySetter) {
        return with((i, question) -> setField("priority", prioritySetter.apply(i), question));
    }

    public QuestionResourceBuilder withFormInputs(List<Long> formInputs) {
        return with(question -> setField("formInputs", new ArrayList<>(formInputs), question));
    }

    public QuestionResourceBuilder withCompetitionAndSectionAndPriority(Competition competition, Section section, Integer priority) {
        return with(question -> {
            question.setCompetition(competition.getId());
            question.setSection(section.getId());
            setField("priority", priority, question);
        });
    }

    @Override
    protected QuestionResource createInitial() {
        return new QuestionResource();
    }
}
