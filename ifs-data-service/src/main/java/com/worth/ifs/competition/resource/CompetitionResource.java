package com.worth.ifs.competition.resource;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.application.domain.Application;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompetitionResource {
    private Long id;
    private List<Long> applications = new ArrayList<>();
    private List<Long> questions = new ArrayList<>();
    private List<Long> sections = new ArrayList<>();
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate assessmentStartDate;
    private LocalDate assessmentEndDate;
    private Status competitionStatus;
    @Min(0)
    @Max(100)
    private Integer maxResearchRatio;
    @Min(0)
    @Max(100)
    private Integer academicGrantPercentage;
    public CompetitionResource() {
        // no-arg constructor
    }

    public CompetitionResource(Long id, List<Long> applications, List<Long> questions, List<Long> sections, String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.applications = applications;
        this.questions = questions;
        this.sections = sections;
        this.name = name;
        this.description = description;
        this.startDate = startDate.toLocalDate();
        this.endDate = endDate.toLocalDate();
    }

    public CompetitionResource(long id, String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate.toLocalDate(); //TO DO change back
        this.endDate = endDate.toLocalDate();
    }

    public Status getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(Status competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public List<Long> getSections() {
        return sections;
    }

    public void setSections(List<Long> sections) {
        this.sections = sections;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addApplication(Application... apps) {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        this.applications.addAll(Arrays.asList(apps).stream().map(Application::getId).collect(Collectors.toList()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getAssessmentEndDate() {
        return assessmentEndDate;
    }

    public void setAssessmentEndDate(LocalDate assessmentEndDate) {
        this.assessmentEndDate = assessmentEndDate;
    }

    public LocalDate getAssessmentStartDate() {
        return assessmentStartDate;
    }

    public void setAssessmentStartDate(LocalDate assessmentStartDate) {
        this.assessmentStartDate = assessmentStartDate;
    }

    @JsonIgnore
    public long getDaysLeft() {
        return getDaysBetween(LocalDate.now(), this.endDate);
    }

    @JsonIgnore
    public long getAssessmentDaysLeft() {
        return getDaysBetween(LocalDate.now(), this.assessmentEndDate);
    }

    @JsonIgnore
    public long getTotalDays() {
        return getDaysBetween(this.startDate, this.endDate);
    }



    /* Keep it D.R.Y */

    @JsonIgnore
    public long getAssessmentTotalDays() {
        return getDaysBetween(this.assessmentStartDate, this.assessmentEndDate);
    }

    @JsonIgnore
    public long getStartDateToEndDatePercentage() {
        return getDaysLeftPercentage(getDaysLeft(), getTotalDays());
    }

    @JsonIgnore
    public long getAssessmentDaysLeftPercentage() {
        return getDaysLeftPercentage(getAssessmentDaysLeft(), getAssessmentTotalDays());
    }

    @JsonIgnore
    public List<Long> getApplications() {
        return applications;
    }

    public void setApplications(List<Long> applications) {
        this.applications = applications;
    }

    @JsonIgnore
    public List<Long> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Long> questions) {
        this.questions = questions;
    }

    private long getDaysBetween(LocalDate dateA, LocalDate dateB) {
        return ChronoUnit.DAYS.between(dateA, dateB);

    }

    private long getDaysLeftPercentage(long daysLeft, long totalDays) {
        if (daysLeft <= 0) {
            return 100;
        }
        double deadlineProgress = 100 - (((double) daysLeft / (double) totalDays) * 100);
        long startDateToEndDatePercentage = (long) deadlineProgress;
        return startDateToEndDatePercentage;
    }

    public Integer getMaxResearchRatio() {
        return maxResearchRatio;
    }

    public void setMaxResearchRatio(Integer maxResearchRatio) {
        this.maxResearchRatio = maxResearchRatio;
    }

    public Integer getAcademicGrantPercentage() {
        return academicGrantPercentage;
    }

    public void setAcademicGrantPercentage(Integer academicGrantPercentage) {
        this.academicGrantPercentage = academicGrantPercentage;
    }

    public enum Status {
        NOT_STARTED, OPEN, IN_ASSESSMENT, FUNDERS_PANEL, PROJECT_SETUP
    }
}
