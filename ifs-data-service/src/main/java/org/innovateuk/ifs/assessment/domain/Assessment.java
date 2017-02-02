package org.innovateuk.ifs.assessment.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.workflow.domain.Process;

import javax.persistence.*;
import java.util.List;

@Entity
public class Assessment extends Process<ProcessRole, Application, AssessmentStates> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", referencedColumnName = "id")
    private ProcessRole participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    private Application target;

    @OneToMany(mappedBy = "assessment")
    private List<AssessorFormInputResponse> responses;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "process")
    private AssessmentFundingDecisionOutcome fundingDecision;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "process")
    private AssessmentRejectOutcome rejection;

    public Assessment() {
        super();
    }

    public Assessment(Application application, ProcessRole processRole) {
        this.participant = processRole;
        this.target = application;
    }

    public AssessmentFundingDecisionOutcome getFundingDecision() {
        return fundingDecision;
    }

    public void setFundingDecision(AssessmentFundingDecisionOutcome fundingDecision) {
        if (fundingDecision != null) {
            fundingDecision.setAssessment(this);
        }
        this.fundingDecision = fundingDecision;
    }

    public AssessmentRejectOutcome getRejection() {
        return rejection;
    }

    public void setRejection(AssessmentRejectOutcome rejection) {
        if (rejection != null) {
            rejection.setAssessment(this);
        }
        this.rejection = rejection;
    }

    @Override
    public ProcessRole getParticipant() {
        return participant;
    }

    @Override
    public void setParticipant(ProcessRole participant) {
        this.participant = participant;
    }

    public List<AssessorFormInputResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<AssessorFormInputResponse> responses) {
        this.responses = responses;
    }

    @Override
    public Application getTarget() {
        return target;
    }

    @Override
    public void setTarget(Application target) {
        this.target = target;
    }

    public AssessmentStates getActivityState() {
        return AssessmentStates.fromState(activityState.getState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Assessment that = (Assessment) o;

        return new EqualsBuilder()
                .append(participant, that.participant)
                .append(target, that.target)
                .append(responses, that.responses)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(participant)
                .append(target)
                .append(responses)
                .toHashCode();
    }
}
