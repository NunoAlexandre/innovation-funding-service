package org.innovateuk.ifs.applicant.resource;

import org.innovateuk.ifs.application.resource.QuestionStatusResource;

import java.util.Optional;

/**
 * Created by luke.harper on 25/04/2017.
 */
public class ApplicantQuestionStatusResource {

    private QuestionStatusResource status;

    private ApplicantResource markedAsCompleteBy;

    private ApplicantResource assignee;

    public QuestionStatusResource getStatus() {
        return status;
    }

    public void setStatus(QuestionStatusResource status) {
        this.status = status;
    }

    public ApplicantResource getMarkedAsCompleteBy() {
        return markedAsCompleteBy;
    }

    public void setMarkedAsCompleteBy(ApplicantResource markedAsCompleteBy) {
        this.markedAsCompleteBy = markedAsCompleteBy;
    }

    public ApplicantResource getAssignee() {
        return assignee;
    }

    public void setAssignee(ApplicantResource assignee) {
        this.assignee = assignee;
    }
}
