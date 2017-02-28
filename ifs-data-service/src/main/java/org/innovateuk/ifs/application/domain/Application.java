package org.innovateuk.ifs.application.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.category.domain.ApplicationResearchCategoryLink;
import org.innovateuk.ifs.category.domain.ResearchCategory;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.invite.domain.ApplicationInvite;
import org.innovateuk.ifs.invite.domain.ProcessActivity;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserRoleType;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application defines database relations and a model to use client side and server side.
 */
@Entity
public class Application implements ProcessActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private LocalDate startDate;
    private LocalDateTime submittedDate;
    private Boolean resubmission;
    private String previousApplicationNumber;
    private String previousApplicationTitle;
    private LocalDateTime manageFundingEmailDate;

    @Min(0)
    private Long durationInMonths; // in months
    @Min(0)
    @Max(100)
    private BigDecimal completion = BigDecimal.ZERO;

    @OneToMany(mappedBy = "applicationId")
    private List<ProcessRole> processRoles = new ArrayList<>();

    @OneToMany(mappedBy = "application")
    private List<ApplicationFinance> applicationFinances = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationStatusId", referencedColumnName = "id")
    private ApplicationStatus applicationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition", referencedColumnName = "id")
    private Competition competition;

    @OneToMany(mappedBy = "application")
    private List<ApplicationInvite> invites;

    @Enumerated(EnumType.STRING)
    private FundingDecisionStatus fundingDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessorFeedbackFileEntryId", referencedColumnName = "id")
    private FileEntry assessorFeedbackFileEntry;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<FormInputResponse> formInputResponses = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApplicationResearchCategoryLink> researchCategories = new HashSet<>();

    private Boolean stateAidAgreed;

    public Application() {
        /*default constructor*/
    }

    public Application(Long id, String name, ApplicationStatus applicationStatus) {
        this.id = id;
        this.name = name;
        this.applicationStatus = applicationStatus;
    }

    public Application(Competition competition, String name, List<ProcessRole> processRoles, ApplicationStatus applicationStatus, Long id) {
        this.competition = competition;
        this.name = name;
        this.processRoles = processRoles;
        this.applicationStatus = applicationStatus;
        this.id = id;
    }

    protected boolean canEqual(Object other) {
        return other instanceof Application;
    }

    public String getFormattedId() {
        return ApplicationResource.formatter.format(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getResubmission() {
        return resubmission;
    }

    public void setResubmission(Boolean resubmission) {
        this.resubmission = resubmission;
    }

    public String getPreviousApplicationNumber() {
        return previousApplicationNumber;
    }

    public void setPreviousApplicationNumber(String previousApplicationNumber) {
        this.previousApplicationNumber = previousApplicationNumber;
    }

    public String getPreviousApplicationTitle() {
        return previousApplicationTitle;
    }

    public void setPreviousApplicationTitle(String previousApplicationTitle) {
        this.previousApplicationTitle = previousApplicationTitle;
    }


    public void setName(String name) {
        this.name = name;
    }

    public List<ProcessRole> getProcessRoles() {
        return processRoles;
    }

    public void setProcessRoles(List<ProcessRole> processRoles) {
        this.processRoles = processRoles;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public void addUserApplicationRole(ProcessRole... processRoles) {
        if (this.processRoles == null) {
            this.processRoles = new ArrayList<>();
        }
        for (ProcessRole processRole : processRoles) {
            if (!this.processRoles.contains(processRole)) {
                this.processRoles.add(processRole);
            }
        }
    }

    public LocalDateTime getManageFundingEmailDate() {
        return manageFundingEmailDate;
    }

    public void setManageFundingEmailDate(LocalDateTime manageFundingEmailDate) {
        this.manageFundingEmailDate = manageFundingEmailDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
    public List<ApplicationFinance> getApplicationFinances() {
        return applicationFinances;
    }

    public Long getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Long durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public void setApplicationFinances(List<ApplicationFinance> applicationFinances) {
        this.applicationFinances = applicationFinances;
    }

    @JsonIgnore
    public ProcessRole getLeadApplicantProcessRole() {
        return getLeadProcessRole().orElse(null);
    }

    @JsonIgnore
    private Optional<ProcessRole> getLeadProcessRole() {
        return this.processRoles.stream().filter(p -> UserRoleType.LEADAPPLICANT.getName().equals(p.getRole().getName())).findAny();
    }

    @JsonIgnore
    public User getLeadApplicant() {
        return getLeadProcessRole().map(role -> role.getUser()).orElse(null);
    }

    @JsonIgnore
    public Long getLeadOrganisationId() {
        return getLeadProcessRole().map(role -> role.getOrganisationId()).orElse(null);
    }

    @JsonIgnore
    public List<ApplicationInvite> getInvites() {
        return this.invites;
    }

    @JsonIgnore
    public boolean isOpen() {
        return Objects.equals(applicationStatus.getId(), ApplicationStatusConstants.OPEN.getId());
    }


    public void setInvites(List<ApplicationInvite> invites) {
        this.invites = invites;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public void setFundingDecision(FundingDecisionStatus fundingDecision) {
        this.fundingDecision = fundingDecision;
    }

    public FundingDecisionStatus getFundingDecision() {
        return fundingDecision;
    }

    public FileEntry getAssessorFeedbackFileEntry() {
        return assessorFeedbackFileEntry;
    }

    public void setAssessorFeedbackFileEntry(FileEntry assessorFeedbackFileEntry) {
        this.assessorFeedbackFileEntry = assessorFeedbackFileEntry;
    }

    public List<FormInputResponse> getFormInputResponses() {
        return formInputResponses;
    }

    public void setFormInputResponses(List<FormInputResponse> formInputResponses) {
        this.formInputResponses = formInputResponses;
    }

    public void addFormInputResponse(FormInputResponse formInputResponse) {
        Optional<FormInputResponse> existing = getFormInputResponseByFormInput(formInputResponse.getFormInput());
        if (existing.isPresent()) {
            existing.get().setFileEntry(formInputResponse.getFileEntry());
            existing.get().setUpdateDate(formInputResponse.getUpdateDate());
            existing.get().setUpdatedBy(formInputResponse.getUpdatedBy());
            existing.get().setValue(formInputResponse.getValue());
        } else {
            formInputResponses.add(formInputResponse);
        }
    }

    public Optional<FormInputResponse> getFormInputResponseByFormInput(FormInput formInput) {
        return formInputResponses.stream().filter(fir -> formInput.equals(fir.getFormInput())).findFirst();
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

    public Set<ResearchCategory> getResearchCategories() {
        return researchCategories.stream().map(ApplicationResearchCategoryLink::getCategory).collect(Collectors.toSet());
    }

    public void addResearchCategory(ResearchCategory researchCategory) {
        researchCategories.clear();
        researchCategories.add(new ApplicationResearchCategoryLink(this, researchCategory));
    }
}
