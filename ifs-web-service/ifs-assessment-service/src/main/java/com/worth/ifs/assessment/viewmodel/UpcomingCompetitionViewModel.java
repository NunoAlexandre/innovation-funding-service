package com.worth.ifs.assessment.viewmodel;

import com.worth.ifs.competition.resource.CompetitionResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ViewModel of an UpcomingCompetition.
 */
public class UpcomingCompetitionViewModel {

    private String competitionName;
    private String competitionDescription;
    private LocalDateTime assessmentPeriodDateFrom;
    private LocalDateTime assessmentPeriodDateTo;
    private LocalDateTime assessorBriefingDate;
    private BigDecimal assessorPay;


    public UpcomingCompetitionViewModel(CompetitionResource competitionResource) {
        this.competitionName = competitionResource.getName();
        this.competitionDescription = competitionResource.getDescription();
        this.assessmentPeriodDateFrom = competitionResource.getAssessorAcceptsDate();
        this.assessmentPeriodDateTo = competitionResource.getAssessorDeadlineDate();
        this.assessorPay = competitionResource.getAssessorPay();
        this.assessorBriefingDate = competitionResource.getAssessorBriefingDate();
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionDescription() {
        return competitionDescription;
    }

    public void setCompetitionDescription(String competitionDescription) {
        this.competitionDescription = competitionDescription;
    }

    public LocalDateTime getAssessmentPeriodDateFrom() {
        return assessmentPeriodDateFrom;
    }

    public void setAssessmentPeriodDateFrom(LocalDateTime assessmentPeriodDateFrom) {
        this.assessmentPeriodDateFrom = assessmentPeriodDateFrom;
    }

    public LocalDateTime getAssessmentPeriodDateTo() {
        return assessmentPeriodDateTo;
    }

    public void setAssessmentPeriodDateTo(LocalDateTime assessmentPeriodDateTo) {
        this.assessmentPeriodDateTo = assessmentPeriodDateTo;
    }

    public LocalDateTime getAssessorBriefingDate() {
        return assessorBriefingDate;
    }

    public void setAssessorBriefingDate(LocalDateTime assessorBriefingDate) {
        this.assessorBriefingDate = assessorBriefingDate;
    }

    public BigDecimal getAssessorPay() {
        return assessorPay;
    }

    public void setAssessorPay(BigDecimal assessorPay) {
        this.assessorPay = assessorPay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UpcomingCompetitionViewModel that = (UpcomingCompetitionViewModel) o;

        return new EqualsBuilder()
                .append(competitionName, that.competitionName)
                .append(competitionDescription, that.competitionDescription)
                .append(assessmentPeriodDateFrom, that.assessmentPeriodDateFrom)
                .append(assessmentPeriodDateTo, that.assessmentPeriodDateTo)
                .append(assessorBriefingDate, that.assessorBriefingDate)
                .append(assessorPay, that.assessorPay)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(competitionName)
                .append(competitionDescription)
                .append(assessmentPeriodDateFrom)
                .append(assessmentPeriodDateTo)
                .append(assessorBriefingDate)
                .append(assessorPay)
                .toHashCode();
    }
}
