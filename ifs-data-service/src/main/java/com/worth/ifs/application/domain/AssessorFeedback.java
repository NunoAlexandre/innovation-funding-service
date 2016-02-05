package com.worth.ifs.application.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.user.domain.ProcessRole;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * An entity representing an Assessor's assessment of a given Response to an assessable question.
 *
 * The idea is that multiple Assessors can score a single Response.
 *
 * Created by dwatson on 05/10/15.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"responseId", "assessorId"}))
public class AssessorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="responseId", referencedColumnName="id")
    private Response response;

    @ManyToOne
    @JoinColumn(name="assessorId", referencedColumnName="id")
    private ProcessRole assessor;

    @Column(name = "assessorId", insertable = false, updatable = false)
    private Long assessorId;

    private String assessmentValue;

    @Column(length=5000)
    private String assessmentFeedback;

    private AssessorFeedback(Response response, ProcessRole assessor) {
        this.response = response;
        this.assessor = assessor;
    }

    public static AssessorFeedback createForResponseAndAssessor(Response response, ProcessRole assessor) {
        return new AssessorFeedback(response, assessor);
    }

    public AssessorFeedback() {
    }

    public Long getId() {
        return id;
    }

    public String getAssessmentValue() { return assessmentValue; }

    public String getAssessmentFeedback() {
        return assessmentFeedback;
    }

    public void setAssessmentValue(String assessmentValue) {
        this.assessmentValue = assessmentValue;
    }

    public void setAssessmentFeedback(String assessmentFeedback) {
        this.assessmentFeedback = assessmentFeedback;
    }

    @JsonIgnore
    public Response getResponse() {
        return response;
    }

    public Long getAssessorId() {
        return assessor != null ? assessor.getId() : assessorId;
    }

    void setAssessor(ProcessRole assessor) {
        this.assessor = assessor;
    }

    @JsonIgnore
    public Integer getWordCount(){
        return assessmentFeedback != null ? assessmentFeedback.split("\\s+").length : 0;
    }

    @JsonIgnore
    public Integer getWordCountLeft(){
        return 350 - this.getWordCount();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public ProcessRole getAssessor() {
        return this.assessor;
    }

    public void setAssessorId(Long assessorId) {
        this.assessorId = assessorId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        AssessorFeedback rhs = (AssessorFeedback) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id)
            .append(this.response, rhs.response)
            .append(this.assessor, rhs.assessor)
            .append(this.assessorId, rhs.assessorId)
            .append(this.assessmentValue, rhs.assessmentValue)
            .append(this.assessmentFeedback, rhs.assessmentFeedback)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(response)
            .append(assessor)
            .append(assessorId)
            .append(assessmentValue)
            .append(assessmentFeedback)
            .toHashCode();
    }
}
