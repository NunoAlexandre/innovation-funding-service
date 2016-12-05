package com.worth.ifs.competition.transactional;

import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.repository.GuidanceRowRepository;
import com.worth.ifs.application.repository.QuestionRepository;
import com.worth.ifs.application.repository.SectionRepository;
import com.worth.ifs.application.resource.SectionType;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.AssessorCountOption;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.domain.CompetitionType;
import com.worth.ifs.competition.domain.Milestone;
import com.worth.ifs.competition.repository.AssessorCountOptionRepository;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.competition.repository.CompetitionTypeRepository;
import com.worth.ifs.competition.resource.CompetitionStatus;
import com.worth.ifs.form.repository.FormInputRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.application.builder.GuidanceRowBuilder.newFormInputGuidanceRow;
import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.SectionBuilder.newSection;
import static com.worth.ifs.competition.builder.AssessorCountOptionBuilder.newAssessorCountOption;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.competition.builder.CompetitionTypeBuilder.newCompetitionType;
import static com.worth.ifs.competition.builder.MilestoneBuilder.newMilestone;
import static com.worth.ifs.competition.resource.MilestoneType.*;
import static com.worth.ifs.form.builder.FormInputBuilder.newFormInput;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompetitionSetupServiceImplTest {

	@InjectMocks
	private CompetitionSetupServiceImpl service;
    @Mock
    private CompetitionRepository competitionRepository;
    @Mock
    private CompetitionTypeRepository competitionTypeRepository;
    @Mock
    private FormInputRepository formInputRepository;
    @Mock
    private QuestionRepository questionRepository;
	@Mock
	private SectionRepository sectionRepository;
	@Mock
	private GuidanceRowRepository assessmentScoreRowRepository;
	@Mock
	private AssessorCountOptionRepository competitionTypeAssessorOptionRepository;
	@Mock
	private EntityManager entityManager;

    @Before
	public void setup() {
        when(formInputRepository.findByCompetitionId(anyLong())).thenReturn(new ArrayList());
        when(questionRepository.findByCompetitionId(anyLong())).thenReturn(new ArrayList());
        when(sectionRepository.findByCompetitionIdOrderByParentSectionIdAscPriorityAsc(anyLong())).thenReturn(new ArrayList());
    }

    @Test
    public void copyFromCompetitionTypeTemplate() {
		long typeId = 4L;
		long competitionId = 2L;
    	CompetitionType competitionType = newCompetitionType().withId(typeId).build();
    	Competition competition = newCompetition().build();
    	Competition competitionTemplate = newCompetition()
    			.withCompetitionType(competitionType)
    			.withSections(newSection()
						.withSectionType(SectionType.GENERAL)
						.withQuestions(newQuestion()
								.withFormInputs(newFormInput()
										.withGuidanceRows(newFormInputGuidanceRow().build(2)
										).build(2)
							).build(2)
					).build(2)
			).build();

		competitionType.setTemplate(competitionTemplate);

    	when(competitionRepository.findById(competitionId)).thenReturn(competition);
		when(competitionTypeRepository.findOne(typeId)).thenReturn(competitionType);
		when(competitionTypeAssessorOptionRepository.findByCompetitionTypeIdAndDefaultOptionTrue(typeId)).thenReturn(Optional.empty());

		ServiceResult<Void> result = service.copyFromCompetitionTypeTemplate(competitionId, typeId);

    	assertTrue(result.isSuccess());
		assertEquals(competition.getCompetitionType(), competitionType);
		assertEquals(competition.getSections(), competitionTemplate.getSections());
    }

    @Test
    public void testInitialiseFormWithSectionHierarchy() {
		Long typeId = 4L;
    	Section parent = newSection()
				.withName("parent")
				.build();

    	Section child1 = newSection()
				.withName("child1")
				.withParentSection(parent)
				.build();
		Section child2 = newSection()
				.withName("child2")
				.withParentSection(parent)
				.build();
    	parent.setChildSections(new ArrayList<>(asList(child1, child2)));

		CompetitionType competitionType = newCompetitionType().withId(typeId).build();
		Competition competition = newCompetition().build();
    	Competition competitionTemplate = newCompetition()
    			.withSections(asList(
    					parent, child1, child2
				))
    			.build();

		competitionType.setTemplate(competitionTemplate);
    	when(competitionRepository.findById(123L)).thenReturn(competition);
    	when(competitionTypeRepository.findOne(typeId)).thenReturn(competitionType);
		when(competitionTypeAssessorOptionRepository.findByCompetitionTypeIdAndDefaultOptionTrue(typeId)).thenReturn(Optional.empty());

		ServiceResult<Void> result = service.copyFromCompetitionTypeTemplate(123L, 4L);

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

	@Test
	public void testMarkAsSetup() {
		Long competitionId = 1L;
		Competition comp = new Competition();
		when(competitionRepository.findById(competitionId)).thenReturn(comp);

		service.markAsSetup(competitionId);

		assertTrue(comp.getSetupComplete());
	}

	@Test
	public void testReturnToSetup() {
		Long competitionId = 1L;
		Competition comp = new Competition();
		when(competitionRepository.findById(competitionId)).thenReturn(comp);

		service.returnToSetup(competitionId);

		assertFalse(comp.getSetupComplete());
	}


	@Test
	public void copyFromCompetitionTypeTemplateAssessorCountAndPay() {
		long typeId = 4L;
		long competitionId = 2L;
		CompetitionType competitionType = newCompetitionType().withId(typeId).build();
		Competition competition = newCompetition().build();
		Competition competitionTemplate = newCompetition()
				.withCompetitionType(competitionType)
				.withSections(newSection()
						.withSectionType(SectionType.GENERAL)
						.withQuestions(newQuestion()
								.withFormInputs(newFormInput()
										.build(2)
								).build(2)
						).build(2)
				).build();

		competitionType.setTemplate(competitionTemplate);

		AssessorCountOption assessorOption = newAssessorCountOption().withId(1L)
				.withAssessorOptionName("1").withAssessorOptionValue(1).withDefaultOption(Boolean.TRUE).build();

		when(competitionRepository.findById(competitionId)).thenReturn(competition);
		when(competitionTypeRepository.findOne(typeId)).thenReturn(competitionType);
		when(competitionTypeAssessorOptionRepository.findByCompetitionTypeIdAndDefaultOptionTrue(typeId)).thenReturn(Optional.of(assessorOption));
		ServiceResult<Void> result = service.copyFromCompetitionTypeTemplate(competitionId, typeId);

		assertTrue(result.isSuccess());
		assertEquals(competition.getCompetitionType(), competitionType);
		assertEquals(Integer.valueOf(1), competition.getAssessorCount());
		assertEquals(CompetitionSetupServiceImpl.DEFAULT_ASSESSOR_PAY, competition.getAssessorPay());
	}

	@Test
	public void copyFromCompetitionTypeTemplateAssessorCountAndPayWithNoDefault() {
		long typeId = 4L;
		long competitionId = 2L;
		CompetitionType competitionType = newCompetitionType().withId(typeId).build();
		Competition competition = newCompetition().build();
		Competition competitionTemplate = newCompetition()
				.withCompetitionType(competitionType)
				.withSections(newSection()
						.withSectionType(SectionType.GENERAL)
						.withQuestions(newQuestion()
								.withFormInputs(newFormInput()
										.build(2)
								).build(2)
						).build(2)
				).build();

		competitionType.setTemplate(competitionTemplate);

		when(competitionRepository.findById(competitionId)).thenReturn(competition);
		when(competitionTypeRepository.findOne(typeId)).thenReturn(competitionType);
		when(competitionTypeAssessorOptionRepository.findByCompetitionTypeIdAndDefaultOptionTrue(typeId)).thenReturn(Optional.empty());
		ServiceResult<Void> result = service.copyFromCompetitionTypeTemplate(competitionId, typeId);

		assertTrue(result.isSuccess());
		assertNull(competition.getAssessorCount());
		assertEquals(CompetitionSetupServiceImpl.DEFAULT_ASSESSOR_PAY, competition.getAssessorPay());
	}
}
