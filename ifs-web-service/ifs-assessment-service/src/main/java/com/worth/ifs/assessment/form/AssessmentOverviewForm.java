package com.worth.ifs.assessment.form;


import com.worth.ifs.controller.BaseBindingResultTarget;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Form field model for the assessment rejection content
 */
public class AssessmentOverviewForm extends BaseBindingResultTarget {

    @NotEmpty(message = "{validation.assessmentoverviewform.rejectReason.required}")
    private String rejectReason;
    private String rejectComment;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssessmentOverviewForm that = (AssessmentOverviewForm) o;

        return new EqualsBuilder()
                .append(rejectReason, that.rejectReason)
                .append(rejectComment, that.rejectComment)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(rejectReason)
                .append(rejectComment)
                .toHashCode();
    }
}
