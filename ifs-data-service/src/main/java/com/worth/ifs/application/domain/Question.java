package com.worth.ifs.application.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.form.domain.FormInput;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Question defines database relations and a model to use client side and server side.
 */
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String shortName;

    @Column(length = 5000)
    private String description;

    private Boolean markAsCompletedEnabled = false;

    private Boolean assignEnabled = true;

    private Boolean multipleStatuses = false;

    private Integer priority;

    @OneToMany(mappedBy = "question")
    @OrderColumn(name = "priority", nullable = false)
    private List<FormInput> formInputs = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "competitionId", referencedColumnName = "id")
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sectionId", referencedColumnName = "id")
    private Section section;

    @OneToMany(mappedBy = "question")
    private List<QuestionStatus> questionStatuses;

    @OneToMany(mappedBy = "question")
    private List<Cost> costs;

    private String questionNumber;

    private Integer assessorMaximumScore;

    public Question() {
        //default constructor
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<QuestionStatus> getQuestionStatuses() {
        return questionStatuses;
    }

    @JsonIgnore
    public Competition getCompetition() {
        return competition;
    }

    @JsonIgnore
    public Section getSection() {
        return section;
    }

    public void setQuestionStatuses(List<QuestionStatus> questionStatuses) {
        this.questionStatuses = questionStatuses;
    }

    public Boolean isMarkAsCompletedEnabled() {
        return markAsCompletedEnabled == null ? false : markAsCompletedEnabled;
    }

    public Boolean hasMultipleStatuses() {
        return multipleStatuses;
    }

    public Boolean getMultipleStatuses() {
        return multipleStatuses;
    }

    public Boolean getMarkAsCompletedEnabled() {
        return markAsCompletedEnabled;
    }

    public Boolean isAssignEnabled() {
        // never return a null value.. it is enabled or disabled.
        return assignEnabled == null ? true : assignEnabled;
    }

    public void setAssignEnabled(Boolean assignEnabled) {
        this.assignEnabled = assignEnabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public List<FormInput> getFormInputs() {
        return formInputs;
    }

    @JsonIgnore
    public List<Cost> getCosts() {
        return costs;
    }

    public Boolean getAssignEnabled() {
        return this.assignEnabled;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMarkAsCompletedEnabled(Boolean markAsCompletedEnabled) {
        this.markAsCompletedEnabled = markAsCompletedEnabled;
    }

    public void setMultipleStatuses(Boolean multipleStatuses) {
        this.multipleStatuses = multipleStatuses;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setFormInputs(List<FormInput> formInputs) {
        this.formInputs = formInputs;
    }

    public void setCosts(List<Cost> costs) {
        this.costs = costs;
    }

    public void setQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
    }

    public Integer getAssessorMaximumScore() {
        return assessorMaximumScore;
    }

    public void setAssessorMaximumScore(Integer assessorMaximumScore) {
        this.assessorMaximumScore = assessorMaximumScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Question question = (Question) o;

        return new EqualsBuilder()
                .append(id, question.id)
                .append(name, question.name)
                .append(shortName, question.shortName)
                .append(description, question.description)
                .append(markAsCompletedEnabled, question.markAsCompletedEnabled)
                .append(assignEnabled, question.assignEnabled)
                .append(multipleStatuses, question.multipleStatuses)
                .append(priority, question.priority)
                .append(formInputs, question.formInputs)
                .append(competition, question.competition)
                .append(section, question.section)
                .append(questionStatuses, question.questionStatuses)
                .append(costs, question.costs)
                .append(questionNumber, question.questionNumber)
                .append(assessorMaximumScore, question.assessorMaximumScore)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(shortName)
                .append(description)
                .append(markAsCompletedEnabled)
                .append(assignEnabled)
                .append(multipleStatuses)
                .append(priority)
                .append(formInputs)
                .append(competition)
                .append(section)
                .append(questionStatuses)
                .append(costs)
                .append(questionNumber)
                .append(assessorMaximumScore)
                .toHashCode();
    }
}
