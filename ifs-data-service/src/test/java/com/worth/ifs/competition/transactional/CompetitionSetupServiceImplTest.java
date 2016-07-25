package com.worth.ifs.competition.transactional;

import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.competitiontemplate.builder.CompetitionTemplateBuilder.newCompetitionTemplate;
import static com.worth.ifs.competitiontemplate.builder.FormInputTemplateBuilder.newFormInputTemplate;
import static com.worth.ifs.competitiontemplate.builder.QuestionTemplateBuilder.newQuestionTemplate;
import static com.worth.ifs.competitiontemplate.builder.SectionTemplateBuilder.newSectionTemplate;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.resource.SectionType;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.domain.CompetitionType;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.competition.repository.CompetitionTypeRepository;
import com.worth.ifs.competitiontemplate.domain.CompetitionTemplate;
import com.worth.ifs.competitiontemplate.domain.SectionTemplate;
import com.worth.ifs.competitiontemplate.repository.CompetitionTemplateRepository;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputType;

@RunWith(MockitoJUnitRunner.class)
public class CompetitionSetupServiceImplTest {

	@InjectMocks
	private CompetitionSetupServiceImpl service;
    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private CompetitionTypeRepository competitionTypeRepository;
    @Mock
    private CompetitionTemplateRepository competitionTemplateRepository;
    
    @Test
    public void testInitialiseForm() {
    	CompetitionType competitionType = new CompetitionType();
    	FormInputType formInputType = new FormInputType();
    	
    	Competition competition = newCompetition().build();
    	CompetitionTemplate competitionTemplate = newCompetitionTemplate()
    			.withCompetitionType(competitionType)
    			.withSectionTemplates(asList(
    						newSectionTemplate()
    						.withName("section1")
    						.withSectionType(SectionType.GENERAL)
    						.withAssessorGuidanceDescription("assessorGuidanceDescription")
    						.withDescription("description")
    						.withQuestionTemplates(asList(
    								newQuestionTemplate()
    								.withAssessorGuidanceAnswer("assessorGuidanceAnswer")
    								.withAssessorGuidanceQuestion("assessorGuidanceQuestion")
    								.withDescription("description")
    								.withName("name")
    								.withShortName("shortName")
    								.withFormInputTemplates(asList(
    										newFormInputTemplate()
    										.withDescription("description")
    										.withFormInputType(formInputType)
    										.withGuidanceAnswer("guidanceAnswer")
    										.withGuidanceQuestion("guidanceQuestion")
    										.withIncludedInApplicationSummary(true)
    										.withInputValidators(new HashSet<>())
    										.build(),
    										newFormInputTemplate()
    										.build()
									))
    								.build(),
    								newQuestionTemplate()
    								.build()
    						))
    						.build(),
    						newSectionTemplate()
    						.build()
    					))
    			.build();
    	
    	when(competitionRepository.findById(123L)).thenReturn(competition);
    	when(competitionTemplateRepository.findByCompetitionTypeId(4L)).thenReturn(competitionTemplate);
    	
    	ServiceResult<Void> result = service.initialiseFormForCompetitionType(123L, 4L);
    	
    	assertTrue(result.isSuccess());
    	assertEquals(competitionType, competition.getCompetitionType());
    	assertEquals(2, competition.getSections().size());
    	Section section = competition.getSections().get(0);
    	assertEquals("section1", section.getName());
    	assertEquals(SectionType.GENERAL, section.getType());
    	assertEquals("assessorGuidanceDescription", section.getAssessorGuidanceDescription());
    	assertEquals("description", section.getDescription());
    	assertEquals(2, section.getQuestions().size());
    	Question question = section.getQuestions().get(0);
    	//assertEquals("assessorGuidanceAnswer", question.getAssessorGuidanceAnswer());
    	//assertEquals("assessorGuidanceQuestion", question.getAssessorGuidanceQuestion());
    	assertEquals("description", question.getDescription());
    	assertEquals("name", question.getName());
    	assertEquals("shortName", question.getShortName());
    	assertEquals(2, question.getFormInputs().size());
    	FormInput formInput = question.getFormInputs().get(0);
    	assertEquals("description", formInput.getDescription());
    	assertEquals(formInputType, formInput.getFormInputType());
    	assertEquals("guidanceAnswer", formInput.getGuidanceAnswer());
    	assertEquals("guidanceQuestion", formInput.getGuidanceQuestion());
    	assertTrue(formInput.getIncludedInApplicationSummary());
    	assertEquals(new HashSet<>(), formInput.getInputValidators());
    }
    
    @Test
    public void testInitialiseFormWithSectionHierarchy() {
    	
    	SectionTemplate parent = newSectionTemplate()
				.withName("parent")
				.build();
    	
    	SectionTemplate child1 = newSectionTemplate()
				.withName("child1")
				.withParentSectionTemplate(parent)
				.build();
    	SectionTemplate child2 = newSectionTemplate()
				.withName("child2")
				.withParentSectionTemplate(parent)
				.build();
    	parent.setChildSectionTemplates(asList(child1, child2));
    	
    	Competition competition = newCompetition().build();
    	CompetitionTemplate competitionTemplate = newCompetitionTemplate()
    			.withSectionTemplates(asList(
    					parent, child1, child2
				))
    			.build();
    	
    	when(competitionRepository.findById(123L)).thenReturn(competition);
    	when(competitionTemplateRepository.findByCompetitionTypeId(4L)).thenReturn(competitionTemplate);
    	
    	ServiceResult<Void> result = service.initialiseFormForCompetitionType(123L, 4L);
    	
    	assertTrue(result.isSuccess());
    	assertEquals(3, competition.getSections().size());
    	Section parentSection = competition.getSections().get(0);
    	Section child1Section = competition.getSections().get(1);
    	Section child2Section = competition.getSections().get(2);
    	assertEquals("parent", parentSection.getName());
    	assertEquals("child1", child1Section.getName());
    	assertEquals("child2", child2Section.getName());
    	assertNull(parentSection.getParentSection());
    	assertEquals(2, parentSection.getChildSections().size());
    	assertTrue(parentSection.getChildSections().contains(child1Section));
    	assertTrue(parentSection.getChildSections().contains(child2Section));
    	assertEquals(parentSection, child1Section.getParentSection());
    	assertNull(child1Section.getChildSections());
    	assertEquals(parentSection, child2Section.getParentSection());
    	assertNull(child2Section.getChildSections());
    }
}
