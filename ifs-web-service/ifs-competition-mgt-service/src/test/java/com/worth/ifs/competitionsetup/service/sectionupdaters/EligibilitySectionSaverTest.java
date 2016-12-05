package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.MilestoneService;
import com.worth.ifs.competition.resource.*;
import com.worth.ifs.competitionsetup.form.EligibilityForm;
import com.worth.ifs.util.CollectionFunctions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EligibilitySectionSaverTest {
	
	@InjectMocks
	private EligibilitySectionSaver service;

	@Mock
	private MilestoneService milestoneService;
	
	@Mock
	private CompetitionService competitionService;
	
	@Test
	public void testSaveCompetitionSetupSection() {
		EligibilityForm competitionSetupForm = new EligibilityForm();
		competitionSetupForm.setLeadApplicantType("business");
		competitionSetupForm.setMultipleStream("yes");
		competitionSetupForm.setStreamName("streamname");
		competitionSetupForm.setResubmission("yes");
		competitionSetupForm.setResearchCategoryId(CollectionFunctions.asLinkedSet(1L, 2L, 3L));
		competitionSetupForm.setResearchParticipationAmountId(1);
		competitionSetupForm.setSingleOrCollaborative("collaborative");
		
		CompetitionResource competition = newCompetitionResource().build();

		service.saveSection(competition, competitionSetupForm, false);
		
		assertEquals(LeadApplicantType.BUSINESS, competition.getLeadApplicantType());
		assertTrue(competition.isMultiStream());
		assertEquals("streamname", competition.getStreamName());
		assertEquals(CollectionFunctions.asLinkedSet(1L, 2L, 3L), competition.getResearchCategories());
		assertEquals(Integer.valueOf(30), competition.getMaxResearchRatio());
		assertEquals(CollaborationLevel.COLLABORATIVE, competition.getCollaborationLevel());

		verify(competitionService).update(competition);
	}

//	@Test
//	public void testAutoSaveResearchCategoryCheck() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//
//		List<Error> errors = service.autoSaveSectionField(competition, "researchCategoryId", "35", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertTrue(competition.getResearchCategories().contains(35L));
//	}
//
//	@Test
//	public void testAutoSaveResearchCategoryUncheck() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//		researchCategories.add(35L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//
//		List<Error> errors = service.autoSaveSectionField(competition, "researchCategoryId", "35", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertTrue(!competition.getResearchCategories().contains(35L));
//	}

//	@Test
//	public void testAutoSaveMultipleStreamYes() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//		researchCategories.add(35L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//		competition.setMultiStream(false);
//
//		List<Error> errors = service.autoSaveSectionField(competition, "multipleStream", "yes", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertTrue(competition.isMultiStream());
//	}

//	@Test
//	public void testAutoSaveMultipleStreamNo() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//		researchCategories.add(35L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//		competition.setMultiStream(true);
//
//		List<Error> errors = service.autoSaveSectionField(competition, "multipleStream", "no", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertTrue(!competition.isMultiStream());
//	}

//	@Test
//	public void testAutoSaveSingleOrCollaborative() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//		researchCategories.add(35L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//		competition.setMultiStream(true);
//		competition.setCollaborationLevel(CollaborationLevel.COLLABORATIVE);
//
//		List<Error> errors = service.autoSaveSectionField(competition, "singleOrCollaborative", "single", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertEquals(competition.getCollaborationLevel(), CollaborationLevel.SINGLE);
//	}

//	@Test
//	public void testAutoSaveResearchParticipationAmountId() {
//		when(milestoneService.getAllMilestonesByCompetitionId(1L)).thenReturn(asList(getMilestone()));
//
//		Set<Long> researchCategories = new HashSet<>();
//		researchCategories.add(33L);
//		researchCategories.add(34L);
//		researchCategories.add(35L);
//
//		CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
//		competition.setMilestones(asList(10L));
//		competition.setMultiStream(true);
//		competition.setCollaborationLevel(CollaborationLevel.COLLABORATIVE);
//		competition.setMaxResearchRatio(Integer.valueOf(50));
//
//		List<Error> errors = service.autoSaveSectionField(competition, "researchParticipationAmountId", "1", null);
//
//		assertTrue(errors.isEmpty());
//		verify(competitionService).update(competition);
//
//		assertEquals(competition.getMaxResearchRatio(), Integer.valueOf(30));
//	}

	private MilestoneResource getMilestone(){
		MilestoneResource milestone = new MilestoneResource();
		milestone.setId(10L);
		milestone.setType(MilestoneType.OPEN_DATE);
		milestone.setDate(LocalDateTime.of(2020, 12, 1, 0, 0));
		milestone.setCompetition(1L);
		return milestone;
	}

}
