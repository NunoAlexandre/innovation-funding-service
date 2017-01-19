package org.innovateuk.ifs.application.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.commons.validation.constraints.FieldRequiredIf;
import org.innovateuk.ifs.commons.validation.constraints.FutureLocalDate;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.innovateuk.ifs.competition.resource.CompetitionStatus.*;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@FieldRequiredIf(required = "previousApplicationNumber", argument = "resubmission", predicate = true, message = "{validation.application.previous.application.number.required}")
@FieldRequiredIf(required = "previousApplicationTitle", argument = "resubmission", predicate = true, message = "{validation.application.previous.application.title.required}")
public class ApplicationResource {
    private static final String ID_PATTERN = "#00000000";
    public static final DecimalFormat formatter = new DecimalFormat(ID_PATTERN);

    private static final int MIN_DURATION_IN_MONTHS = 1;
    private static final int MAX_DURATION_IN_MONTHS = 36;

    private static final List<CompetitionStatus> PUBLISHED_ASSESSOR_FEEDBACK_COMPETITION_STATES = singletonList(PROJECT_SETUP);
    private static final List<CompetitionStatus> EDITABLE_ASSESSOR_FEEDBACK_COMPETITION_STATES = asList(FUNDERS_PANEL, ASSESSOR_FEEDBACK);
    private static final List<CompetitionStatus> SUBMITABLE_COMPETITION_STATES = asList(OPEN);
    private static final List<Long> SUBMITTED_APPLICATION_STATES =
            simpleMap(asList(ApplicationStatusConstants.SUBMITTED, ApplicationStatusConstants.APPROVED, ApplicationStatusConstants.REJECTED), ApplicationStatusConstants::getId);

    private Long id;

    @NotBlank(message ="{validation.project.name.must.not.be.empty}")
    private String name;

    @FutureLocalDate(message = "{validation.project.start.date.not.in.future}")
    private LocalDate startDate;
    private LocalDateTime submittedDate;

    @Min(value=MIN_DURATION_IN_MONTHS, message ="{validation.application.details.duration.in.months.max.digits}")
    @Max(value=MAX_DURATION_IN_MONTHS, message ="{validation.application.details.duration.in.months.max.digits}")
    @NotNull
    private Long durationInMonths;
    private Long applicationStatus;
    private String applicationStatusName;
    private Long competition;
    private String competitionName;
    private Long assessorFeedbackFileEntry;
    private CompetitionStatus competitionStatus;
    private BigDecimal completion;
    private Boolean stateAidAgreed;

    @NotNull(message="{validation.application.must.indicate.resubmission.or.not}")
    private Boolean resubmission;
    private String previousApplicationNumber;
    private String previousApplicationTitle;
    private Set<ResearchCategoryResource> researchCategories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getFormattedId(){
        return formatter.format(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getApplicationDisplayName() {
        if(StringUtils.isNotEmpty(name)) {
            return name;
        } else {
            return competitionName;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Long getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Long durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public Long getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(Long applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    @JsonIgnore
    public void setApplicationStatusConstant(ApplicationStatusConstants applicationStatus) {
        this.applicationStatus = applicationStatus.getId();
        this.applicationStatusName = applicationStatus.getName();
    }

    public Long getCompetition() {
        return competition;
    }

    public void setCompetition(Long competition) {
        this.competition = competition;
    }

    public Boolean getResubmission() {
        return resubmission;
    }

    public void setResubmission(Boolean resubmission) { this.resubmission = resubmission; }

    public String getPreviousApplicationNumber() {
        return previousApplicationNumber;
    }

    public void setPreviousApplicationNumber(String previousApplicationNumber) { this.previousApplicationNumber = previousApplicationNumber; }

    public String getPreviousApplicationTitle() {
        return previousApplicationTitle;
    }

    public void setPreviousApplicationTitle(String previousApplicationTitle) { this.previousApplicationTitle = previousApplicationTitle; }

    @JsonIgnore
    public boolean isOpen(){
        return ApplicationStatusConstants.OPEN.getId().equals(applicationStatus) || ApplicationStatusConstants.CREATED.getId().equals(applicationStatus);
    }
    @JsonIgnore
    public void enableViewMode(){
        setApplicationStatus(ApplicationStatusConstants.SUBMITTED.getId());
    }

    public Long getAssessorFeedbackFileEntry() {
        return assessorFeedbackFileEntry;
    }

    public void setAssessorFeedbackFileEntry(Long assessorFeedbackFileEntry) {
        this.assessorFeedbackFileEntry = assessorFeedbackFileEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ApplicationResource that = (ApplicationResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(startDate, that.startDate)
                .append(durationInMonths, that.durationInMonths)
                .append(applicationStatus, that.applicationStatus)
                .append(competition, that.competition)
                .append(assessorFeedbackFileEntry, that.assessorFeedbackFileEntry)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(startDate)
                .append(durationInMonths)
                .append(applicationStatus)
                .append(competition)
                .append(assessorFeedbackFileEntry)
                .toHashCode();
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getApplicationStatusName() {
        return applicationStatusName;
    }

    public void setApplicationStatusName(String applicationStatusName) {
        this.applicationStatusName = applicationStatusName;
    }

    public CompetitionStatus getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(CompetitionStatus competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    public boolean hasPublishedAssessorFeedback() {
        return isInPublishedAssessorFeedbackCompetitionState() && getAssessorFeedbackFileEntry() != null;
    }

    @JsonIgnore
    public boolean isInPublishedAssessorFeedbackCompetitionState() {
        return PUBLISHED_ASSESSOR_FEEDBACK_COMPETITION_STATES.contains(competitionStatus);
    }

    @JsonIgnore
    public boolean isInEditableAssessorFeedbackCompetitionState() {
        return EDITABLE_ASSESSOR_FEEDBACK_COMPETITION_STATES.contains(competitionStatus);
    }

    @JsonIgnore
    public boolean isSubmitable() {
        return isInSubmitableCompetitionState() && !hasBeenSubmitted();
    }

    @JsonIgnore
    public boolean hasBeenSubmitted() {
        return SUBMITTED_APPLICATION_STATES.contains(applicationStatus);
    }

    private boolean isInSubmitableCompetitionState() {
        return SUBMITABLE_COMPETITION_STATES.contains(competitionStatus);
    }

    public BigDecimal getCompletion() {
        return completion;
    }

    public void setCompletion(final BigDecimal completion) {
        this.completion = completion;
    }

    public Boolean getStateAidAgreed() {
        return stateAidAgreed;
    }

    public void setStateAidAgreed(Boolean stateAidAgreed) {
        this.stateAidAgreed = stateAidAgreed;
    }

    public Set<ResearchCategoryResource> getResearchCategories() {
        return researchCategories;
    }

    public void setResearchCategories(Set<ResearchCategoryResource> researchCategories) {
        this.researchCategories = researchCategories;
    }
}
