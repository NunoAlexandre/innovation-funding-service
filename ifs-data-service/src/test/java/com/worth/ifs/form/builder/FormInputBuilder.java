package com.worth.ifs.form.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.application.domain.GuidanceRow;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.domain.FormValidator;
import com.worth.ifs.form.resource.FormInputScope;
import com.worth.ifs.form.resource.FormInputType;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.idBasedDescriptions;
import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * A Builder for Form Inputs.  By default this builder will assign unique ids and descriptions based upon the ids.
 * It will also assign priorities.
 */
public class FormInputBuilder extends BaseBuilder<FormInput, FormInputBuilder> {

    private FormInputBuilder(List<BiConsumer<Integer, FormInput>> newMultiActions) {
        super(newMultiActions);
    }

    @Override
    protected FormInputBuilder createNewBuilderWithActions(List<BiConsumer<Integer, FormInput>> actions) {
        return new FormInputBuilder(actions);
    }

    @Override
    protected FormInput createInitial() {
        return new FormInput();
    }

    public static FormInputBuilder newFormInput() {
        return new FormInputBuilder(emptyList())
                .with(uniqueIds())
                .with(idBasedDescriptions("Description "));
    }

    public FormInputBuilder withId(Long... value) {
        return withArraySetFieldByReflection("id", value);
    }

    public FormInputBuilder withWordCount(Integer... value) {
        return withArraySetFieldByReflection("wordCount", value);
    }

    public FormInputBuilder withType(FormInputType... value) {
        return withArraySetFieldByReflection("type", value);
    }

    public FormInputBuilder withResponses(List<FormInputResponse>... value) {
        return withArraySetFieldByReflection("responses", value);
    }

    public FormInputBuilder withQuestion(Question... value) {
        return withArraySetFieldByReflection("question", value);
    }

    public FormInputBuilder withCompetition(Competition... value) {
        return withArraySetFieldByReflection("competition", value);
    }

    public FormInputBuilder withInputValidators(Set<FormValidator>... value) {
        return withArraySetFieldByReflection("inputValidators", value);
    }

    public FormInputBuilder withGuidanceTitle(String... value) {
        return withArraySetFieldByReflection("guidanceTitle", value);
    }

    public FormInputBuilder withGuidanceAnswer(String... value) {
        return withArraySetFieldByReflection("guidanceAnswer", value);
    }

    public FormInputBuilder withDescription(String... value) {
        return withArraySetFieldByReflection("description", value);
    }

    public FormInputBuilder withIncludedInApplicationSummary(Boolean... value) {
        return withArraySetFieldByReflection("includedInApplicationSummary", value);
    }

    public FormInputBuilder withPriority(Integer... value) {
        return withArraySetFieldByReflection("priority", value);
    }

    public FormInputBuilder withScope(FormInputScope... value) {
        return withArraySetFieldByReflection("scope", value);
    }

    public FormInputBuilder withGuidanceRows(List<GuidanceRow>... value) {
        return withArraySetFieldByReflection("guidanceRows", value);
    }

    public FormInputBuilder withActive(Boolean... active) {
        return withArraySetFieldByReflection("active", active);
    }
}
