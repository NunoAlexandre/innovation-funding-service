package com.worth.ifs.competition.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.category.domain.Category;
import com.worth.ifs.competition.resource.*;
import com.worth.ifs.invite.domain.ProcessActivity;
import com.worth.ifs.user.domain.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Competition defines database relations and a model to use client side and server side.
 */
@Entity
public class Competition implements ProcessActivity {

	@Transient
	private DateProvider dateProvider = new DateProvider();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy="competition")
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy="competition")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy="competition")
    private List<CompetitionCoFunder> coFunders = new ArrayList<>();

    @OneToMany(mappedBy="competition")
    @OrderBy("priority ASC")
    private List<Section> sections = new ArrayList<>();

    private String name;

    @Column( length = 5000 )
    private String description;

    @ManyToOne
    @JoinColumn(name="competitionTypeId", referencedColumnName="id")
    private CompetitionType competitionType;

    @OneToMany(mappedBy = "competition")
    private List<Milestone> milestones = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="executiveUserId", referencedColumnName="id")
    private User executive;

    @ManyToOne
    @JoinColumn(name="leadTechnologistUserId", referencedColumnName="id")
    private User leadTechnologist;

    private String pafCode;
    private String budgetCode;
    private String code;

    private Integer maxResearchRatio;
    private Integer academicGrantPercentage;

    @Transient
    private Category innovationSector;
    @Transient
    private Category innovationArea;
    @Transient
    private Set<Category> researchCategories;

    private String activityCode;
    private String innovateBudget;
    private String funder;
    private BigDecimal funderBudget;

    private boolean multiStream;
    private boolean resubmission;
    private String streamName;
    @Enumerated(EnumType.STRING)
    private CollaborationLevel collaborationLevel;
    @Enumerated(EnumType.STRING)
    private LeadApplicantType leadApplicantType;
    
    @ElementCollection
    @JoinTable(name="competition_setup_status", joinColumns=@JoinColumn(name="competition_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn (name="section")
    @Column(name="status")
    private Map<CompetitionSetupSection, Boolean> sectionSetupStatus = new HashMap<>();

    private Boolean setupComplete;

    public Competition() {
        setupComplete = false;
    }

    public Competition(Long id, List<Application> applications, List<Question> questions, List<Section> sections, String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.applications = applications;
        this.questions = questions;
        this.sections = sections;
        this.name = name;
        this.description = description;
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        this.setupComplete = true;
    }

    public Competition(long id, String name, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        this.setupComplete = true;
    }

    public CompetitionResource.Status getCompetitionStatus() {
        LocalDateTime today = dateProvider.provideDate();
        if (setupComplete) {
            if (getStartDate() == null || getStartDate().isAfter(today)) {
                return CompetitionResource.Status.READY_TO_OPEN;
            } else if (getEndDate() != null && getEndDate().isAfter(today)) {
                return CompetitionResource.Status.OPEN;
            } else if (getEndDate() != null && getEndDate().isBefore(today)
                    && getAssessmentStartDate() != null && getAssessmentStartDate().isAfter(today)) {
                return CompetitionResource.Status.CLOSED;
            } else if (getAssessmentEndDate() != null && getAssessmentEndDate().isAfter(today)) {
                return CompetitionResource.Status.IN_ASSESSMENT;
            } else if (getFundersPanelEndDate() == null || getFundersPanelEndDate().isAfter(today)) {
                return CompetitionResource.Status.FUNDERS_PANEL;
            } else if (getAssessorFeedbackDate() == null || getAssessorFeedbackDate().isAfter(today)) {
                return CompetitionResource.Status.ASSESSOR_FEEDBACK;
            } else {
                return CompetitionResource.Status.PROJECT_SETUP;
            }
        } else {
            return CompetitionResource.Status.COMPETITION_SETUP;
        }
    }

    public List<Section> getSections() {
        return sections;
    }

    public String getDescription() {
        return description;
    }


    public void addApplication(Application... apps){
        if(applications == null){
            applications = new ArrayList<>();
        }
        this.applications.addAll(Arrays.asList(apps));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getSetupComplete() {
        return setupComplete;
    }

    public void setSetupComplete(Boolean setupComplete) {
        this.setupComplete = setupComplete;
    }

	public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @JsonIgnore
    public long getDaysLeft(){
        return getDaysBetween(LocalDateTime.now(), this.getEndDate());
    }
    @JsonIgnore
    public long getAssessmentDaysLeft(){
        return getDaysBetween(LocalDateTime.now(), this.getAssessmentEndDate());
    }
    @JsonIgnore
    public long getTotalDays(){
        return getDaysBetween(this.getStartDate(), this.getEndDate());
    }
    @JsonIgnore
    public long getAssessmentTotalDays() {
        return getDaysBetween(this.getAssessmentStartDate(), this.getAssessmentEndDate());
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
    public List<Application> getApplications() {
        return applications;
    }
    @JsonIgnore
    public List<Question> getQuestions(){return questions;}

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEndDate() {
        return getMilestoneValue(MilestoneType.SUBMISSION_DATE);
    }

    public void setEndDate(LocalDateTime endDate) {
    	setMilestoneValue(MilestoneType.SUBMISSION_DATE, endDate);
    }

    public LocalDateTime getStartDate() {
    	return getMilestoneValue(MilestoneType.OPEN_DATE);
    }

    public void setStartDate(LocalDateTime startDate) {
        setMilestoneValue(MilestoneType.OPEN_DATE, startDate);
    }

    public LocalDateTime getAssessmentEndDate() {
    	return getMilestoneValue(MilestoneType.FUNDERS_PANEL);
    }

    public void setAssessmentEndDate(LocalDateTime assessmentEndDate) {
    	setMilestoneValue(MilestoneType.FUNDERS_PANEL, assessmentEndDate);
    }

    public LocalDateTime getAssessmentStartDate() {
    	return getMilestoneValue(MilestoneType.ASSESSOR_ACCEPTS);
    }

    public void setAssessmentStartDate(LocalDateTime assessmentStartDate){
    	setMilestoneValue(MilestoneType.ASSESSOR_ACCEPTS, assessmentStartDate);
    }

    public LocalDateTime getAssessorFeedbackDate() {
    	return getMilestoneValue(MilestoneType.ASSESSOR_DEADLINE);
    }

    public void setAssessorFeedbackDate(LocalDateTime assessorFeedbackDate) {
    	setMilestoneValue(MilestoneType.ASSESSOR_DEADLINE, assessorFeedbackDate);
    }

    public LocalDateTime getFundersPanelEndDate() {
    	return getMilestoneValue(MilestoneType.NOTIFICATIONS);
	}

    public void setFundersPanelEndDate(LocalDateTime fundersPanelEndDate) {
    	setMilestoneValue(MilestoneType.NOTIFICATIONS, fundersPanelEndDate);
	}

    private void setMilestoneValue(MilestoneType MilestoneType, LocalDateTime dateTime) {
		Milestone milestone = milestones.stream().filter(m -> MilestoneType.equals(m.getType())).findAny().orElseGet(() -> {
			Milestone m = new Milestone();
			m.setCompetition(this);
			m.setType(MilestoneType);
			milestones.add(m);
			return m;
		});

		milestone.setDate(dateTime);
	}

	private LocalDateTime getMilestoneValue(MilestoneType milestoneType) {
		Optional<Milestone> milestone = milestones.stream().filter(m -> milestoneType.equals(m.getType())).findAny();

		if(milestone.isPresent()) {
			return milestone.get().getDate();
		}

		return null;
	}

    private long getDaysBetween(LocalDateTime dateA, LocalDateTime dateB) {
        return ChronoUnit.DAYS.between(dateA, dateB);
    }

    private long getDaysLeftPercentage(long daysLeft, long totalDays ) {
        if(daysLeft <= 0){
            return 100;
        }
        double deadlineProgress = 100-( ( (double)daysLeft/(double)totalDays )* 100);
        return (long) deadlineProgress;
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

    public void setId(Long id) {
        this.id = id;
    }

	protected void setDateProvider(DateProvider dateProvider) {
		this.dateProvider = dateProvider;
	}

    protected static class DateProvider {
    	public LocalDateTime provideDate() {
    		return LocalDateTime.now();
    	}
    }

    public User getExecutive() {
        return executive;
    }

    public void setExecutive(User executive) {
        this.executive = executive;
    }

    public User getLeadTechnologist() {
        return leadTechnologist;
    }

    public void setLeadTechnologist(User leadTechnologist) {
        this.leadTechnologist = leadTechnologist;
    }

    public String getPafCode() {
        return pafCode;
    }

    public void setPafCode(String pafCode) {
        this.pafCode = pafCode;
    }

    public String getBudgetCode() {
        return budgetCode;
    }

    public void setBudgetCode(String budgetCode) {
        this.budgetCode = budgetCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public Category getInnovationSector() {
        return innovationSector;
    }

    public void setInnovationSector(Category innovationSector) {
        this.innovationSector = innovationSector;
    }

    public Category getInnovationArea() {
        return innovationArea;
    }

    public void setInnovationArea(Category innovationArea) {
        this.innovationArea = innovationArea;
    }
    
    public Set<Category> getResearchCategories() {
		return researchCategories;
	}
    
    public void setResearchCategories(Set<Category> researchCategories) {
		this.researchCategories = researchCategories;
	}

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }
    
    public boolean isMultiStream() {
		return multiStream;
	}
    
    public void setMultiStream(boolean multiStream) {
		this.multiStream = multiStream;
	}

    public boolean isResubmission() {
        return resubmission;
    }

    public void setResubmission(boolean resubmission) {
        this.resubmission = resubmission;
    }

    public String getStreamName() {
		return streamName;
	}
    
    public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
    
    public CollaborationLevel getCollaborationLevel() {
		return collaborationLevel;
	}
    
    public void setCollaborationLevel(CollaborationLevel collaborationLevel) {
		this.collaborationLevel = collaborationLevel;
	}
    
    public LeadApplicantType getLeadApplicantType() {
		return leadApplicantType;
	}
    
    public void setLeadApplicantType(LeadApplicantType leadApplicantType) {
		this.leadApplicantType = leadApplicantType;
	}
    
    public Map<CompetitionSetupSection, Boolean> getSectionSetupStatus() {
		return sectionSetupStatus;
	}

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String getInnovateBudget() {
        return innovateBudget;
    }

    public void setInnovateBudget(String innovateBudget) {
        this.innovateBudget = innovateBudget;
    }

    public String getFunder() {
        return funder;
    }

    public void setFunder(String funder) {
        this.funder = funder;
    }

    public BigDecimal getFunderBudget() {
        return funderBudget;
    }

    public void setFunderBudget(BigDecimal funderBudget) {
        this.funderBudget = funderBudget;
    }

    public List<CompetitionCoFunder> getCoFunders() {
        return coFunders;
    }

    public void setCoFunders(List<CompetitionCoFunder> coFunders) {
        this.coFunders = coFunders;
    }
}

