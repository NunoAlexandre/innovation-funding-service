package org.innovateuk.ifs.application.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.assessment.resource.AssessmentFeedbackAggregateResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;

import java.util.List;

public class AssessQuestionFeedbackViewModel {

    ApplicationResource application;

    QuestionResource question;

    List<FormInputResponseResource> responses;

    AssessmentFeedbackAggregateResource aggregateResource;

    NavigationViewModel navigation;

    public AssessQuestionFeedbackViewModel(ApplicationResource application,
                                           QuestionResource question,
                                           List<FormInputResponseResource> responses,
                                           AssessmentFeedbackAggregateResource aggregateResource,
                                           NavigationViewModel navigationViewModel) {
        this.application = application;
        this.question = question;
        this.responses = responses;
        this.aggregateResource = aggregateResource;
        this.navigation = navigationViewModel;
    }

    public ApplicationResource getApplication() {
        return application;
    }

    public QuestionResource getQuestion() {
        return question;
    }

    public List<FormInputResponseResource> getResponses() {
        return responses;
    }

    public AssessmentFeedbackAggregateResource getAggregateResource() {
        return aggregateResource;
    }

    public NavigationViewModel getNavigation() {
        return navigation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AssessQuestionFeedbackViewModel that = (AssessQuestionFeedbackViewModel) o;

        return new EqualsBuilder()
                .append(application, that.application)
                .append(question, that.question)
                .append(responses, that.responses)
                .append(aggregateResource, that.aggregateResource)
                .append(navigation, that.navigation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(application)
                .append(question)
                .append(responses)
                .append(aggregateResource)
                .append(navigation)
                .toHashCode();
    }
}
