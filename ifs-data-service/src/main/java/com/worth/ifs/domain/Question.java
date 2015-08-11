package com.worth.ifs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

/**
 * Question defines database relations and a model to use client side and server side.
 */

@Entity
public class Question {
    public Question(String optionValues, Long id, Competition competition, Section section, QuestionType questionType, List<Response> responses, String name, String description, String guidanceQuestion, String guidanceAnswer, Long characterCount) {
        this.optionValues = optionValues;
        this.id = id;
        this.competition = competition;
        this.section = section;
        this.questionType = questionType;
        this.responses = responses;
        this.name = name;
        this.description = description;
        this.guidanceQuestion = guidanceQuestion;
        this.guidanceAnswer = guidanceAnswer;
        this.characterCount = characterCount;
    }

    public Question(Long id, Competition competition, Section section, String name) {
        this.id = id;
        this.competition = competition;
        this.section = section;
        this.name = name;
    }

    public Question() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="competitionId", referencedColumnName="id")
    private Competition competition;

    @ManyToOne
    @JoinColumn(name="sectionId", referencedColumnName="id")
    private Section section;

    @ManyToOne
    @JoinColumn(name="questionTypeId", referencedColumnName="id")
    private QuestionType questionType;

    @OneToMany(mappedBy="question", fetch = FetchType.LAZY)
    private List<Response> responses;

    private String name;
    @Column(length=5000)
    private String description;
    @Column(length=5000)
    private String guidanceQuestion;
    @Column(length=5000)
    private String guidanceAnswer;
    @Column(length=5000)
    private Long characterCount;
    @Column(length=5000)
    private String optionValues;

    public String getName() {
        return name;
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


    public String getOptionValues() {
        return optionValues;
    }

    public Long getCharacterCount() {
        return characterCount;
    }

    public String getGuidanceAnswer() {
        return guidanceAnswer;
    }

    public String getGuidanceQuestion() {
        return guidanceQuestion;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public List<Response> getResponses() {
        return responses;
    }

    @JsonIgnore
    public Competition getCompetition() {
        return competition;
    }

    @JsonIgnore
    public Section getSection() {
        return section;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }
}
