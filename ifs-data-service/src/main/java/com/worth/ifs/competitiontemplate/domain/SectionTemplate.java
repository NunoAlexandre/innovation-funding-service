package com.worth.ifs.competitiontemplate.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.worth.ifs.application.resource.SectionType;

@Entity
public class SectionTemplate {

    @Id
    private Long id;
    
    @OneToMany(mappedBy="sectionTemplate")
    @OrderBy("priority ASC")
    private List<QuestionTemplate> questionTemplates = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name="section_type")
    private SectionType type = SectionType.GENERAL;
    
    private String name;

    @Column( length = 5000 )
    private String description;
    
    @Column( length = 5000 )
    private String assessorGuidanceDescription;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parentSectionTemplateId", referencedColumnName="id")
    private SectionTemplate parentSectionTemplate;

    @OneToMany(mappedBy="parentSectionTemplate",fetch=FetchType.LAZY)
    @OrderBy("priority ASC")
    private List<SectionTemplate> childSectionTemplates;
    
    @ManyToOne
    @JoinColumn(name="competitionTemplateId", referencedColumnName="id")
    private CompetitionTemplate competitionTemplate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<QuestionTemplate> getQuestionTemplates() {
		return questionTemplates;
	}

	public void setQuestionTemplates(List<QuestionTemplate> questionTemplates) {
		this.questionTemplates = questionTemplates;
	}

	public SectionType getType() {
		return type;
	}

	public void setType(SectionType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAssessorGuidanceDescription() {
		return assessorGuidanceDescription;
	}
	
	public void setAssessorGuidanceDescription(String assessorGuidanceDescription) {
		this.assessorGuidanceDescription = assessorGuidanceDescription;
	}

	public SectionTemplate getParentSectionTemplate() {
		return parentSectionTemplate;
	}

	public void setParentSectionTemplate(SectionTemplate parentSectionTemplate) {
		this.parentSectionTemplate = parentSectionTemplate;
	}

	public List<SectionTemplate> getChildSectionTemplates() {
		return childSectionTemplates;
	}

	public void setChildSectionTemplates(List<SectionTemplate> childSectionTemplates) {
		this.childSectionTemplates = childSectionTemplates;
	}
	
	public CompetitionTemplate getCompetitionTemplate() {
		return competitionTemplate;
	}
	
	public void setCompetitionTemplate(CompetitionTemplate competitionTemplate) {
		this.competitionTemplate = competitionTemplate;
	}
    
}
