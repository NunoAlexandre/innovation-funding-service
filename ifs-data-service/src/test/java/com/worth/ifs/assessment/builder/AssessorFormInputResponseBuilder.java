package com.worth.ifs.assessment.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.BuilderAmendFunctions;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.domain.AssessorFormInputResponse;
import com.worth.ifs.form.domain.FormInput;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BaseBuilderAmendFunctions.setField;
import static com.worth.ifs.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for {@link com.worth.ifs.assessment.domain.AssessorFormInputResponse}
 */
public class AssessorFormInputResponseBuilder extends BaseBuilder<AssessorFormInputResponse, AssessorFormInputResponseBuilder> {

    private AssessorFormInputResponseBuilder(List<BiConsumer<Integer, AssessorFormInputResponse>> newActions) {
        super(newActions);
    }

    public static AssessorFormInputResponseBuilder newAssessorFormInputResponse() {
        return new AssessorFormInputResponseBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected AssessorFormInputResponseBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorFormInputResponse>> actions) {
        return new AssessorFormInputResponseBuilder(actions);
    }

    @Override
    protected AssessorFormInputResponse createInitial() {
        return new AssessorFormInputResponse();
    }

    public AssessorFormInputResponseBuilder withId(Long... ids) {
        return withArray(BuilderAmendFunctions::setId, ids);
    }

    public AssessorFormInputResponseBuilder withAssessment(Assessment... assessments) {
        return withArray((assessment, assessorFormInputResponse) -> setField("assessment", assessment, assessorFormInputResponse), assessments);
    }

    public AssessorFormInputResponseBuilder withFormInput(FormInput... formInputs) {
        return withArray((formInput, assessorFormInputResponse) -> setField("formInput", formInput, assessorFormInputResponse), formInputs);
    }

    public AssessorFormInputResponseBuilder withValue(String... values) {
        return withArray((value, assessorFormInputResponse) -> setField("value", value, assessorFormInputResponse), values);
    }

    public AssessorFormInputResponseBuilder withUpdatedDate(LocalDateTime... updatedDates) {
        return withArray((updatedDate, assessorFormInputResponse) -> setField("updatedDate", updatedDate, assessorFormInputResponse), updatedDates);
    }
}