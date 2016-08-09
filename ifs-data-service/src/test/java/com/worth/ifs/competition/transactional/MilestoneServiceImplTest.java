package com.worth.ifs.competition.transactional;

import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.domain.Milestone;
import com.worth.ifs.competition.mapper.MilestoneMapper;
import com.worth.ifs.competition.repository.CompetitionRepository;
import com.worth.ifs.competition.repository.MilestoneRepository;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.resource.MilestoneResource.MilestoneName;

@RunWith(MockitoJUnitRunner.class)
public class MilestoneServiceImplTest {
	@InjectMocks
	private MilestoneServiceImpl service;
	@Mock
    private CompetitionRepository competitionRepository;
	@Mock
    private MilestoneRepository milestoneRepository;
	@Mock
    private MilestoneMapper milestoneMapper;
	
	@Before
	public void setUp() {
		when(milestoneMapper.mapToDomain(any(MilestoneResource.class))).thenAnswer(new Answer<Milestone>(){
			@Override
			public Milestone answer(InvocationOnMock invocation) throws Throwable {
				MilestoneResource arg = invocation.getArgumentAt(0, MilestoneResource.class);
				Milestone milestone = new Milestone();
				milestone.setName(arg.getName());
				milestone.setDate(arg.getDate());
				return milestone;
			}
		});
	}
	
	@Test
	public void testUpdateMilestones() {
		Competition competition = newCompetition().build();
		when(competitionRepository.findById(1L)).thenReturn(competition);
		
		List<MilestoneResource> milestones = asList(
                milestone(MilestoneName.FUNDERS_PANEL, LocalDateTime.of(2050, 3, 11, 0, 0)),
                milestone(MilestoneName.ASSESSMENT_PANEL, LocalDateTime.of(2050, 3, 10, 0, 0))
			);
		
		ServiceResult<Void> result = service.update(1L, milestones);
		
		assertTrue(result.isSuccess());
		assertEquals(MilestoneName.ASSESSMENT_PANEL, competition.getMilestones().get(0).getName());
		assertEquals(LocalDateTime.of(2050, 3, 10, 0, 0), competition.getMilestones().get(0).getDate());
		assertEquals(MilestoneName.FUNDERS_PANEL, competition.getMilestones().get(1).getName());
		assertEquals(LocalDateTime.of(2050, 3, 11, 0, 0), competition.getMilestones().get(1).getDate());
	}
	
	@Test
	public void testUpdateNotSequential() {
		Competition competition = newCompetition().build();
		when(competitionRepository.findById(1L)).thenReturn(competition);
		
		List<MilestoneResource> milestones = asList(
                milestone(MilestoneName.FUNDERS_PANEL, LocalDateTime.of(2050, 3, 10, 0, 0)),
                milestone(MilestoneName.ASSESSMENT_PANEL, LocalDateTime.of(2050, 3, 11, 0, 0))
			);
		
		ServiceResult<Void> result = service.update(1L, milestones);
		
		assertFalse(result.isSuccess());
		assertEquals(1, result.getFailure().getErrors().size());
		assertEquals("Milestones must be in date order", result.getFailure().getErrors().get(0).getErrorMessage());
		assertEquals(0, competition.getMilestones().size());
	}
	
	@Test
	public void testUpdateMilestonesNullDate() {
		Competition competition = newCompetition().build();
		when(competitionRepository.findById(1L)).thenReturn(competition);
		
		List<MilestoneResource> milestones = asList(
                milestone(MilestoneName.FUNDERS_PANEL, LocalDateTime.of(2050, 3, 11, 0, 0)),
                milestone(MilestoneName.ASSESSMENT_PANEL, null)
			);
		
		ServiceResult<Void> result = service.update(1L, milestones);
		
		assertFalse(result.isSuccess());
		assertEquals(1, result.getFailure().getErrors().size());
		assertEquals("Milestones must have dates specified", result.getFailure().getErrors().get(0).getErrorMessage());		
		assertEquals(0, competition.getMilestones().size());
	}
	
	@Test
	public void testUpdateMilestonesDateInPast() {
		Competition competition = newCompetition().build();
		when(competitionRepository.findById(1L)).thenReturn(competition);
		
		List<MilestoneResource> milestones = asList(
                milestone(MilestoneName.FUNDERS_PANEL, LocalDateTime.of(2050, 3, 11, 0, 0)),
                milestone(MilestoneName.ASSESSMENT_PANEL, LocalDateTime.of(1985, 3, 10, 0, 0))
			);
		
		ServiceResult<Void> result = service.update(1L, milestones);
		
		assertFalse(result.isSuccess());
		assertEquals(1, result.getFailure().getErrors().size());
		assertEquals("Milestones cannot be in the past", result.getFailure().getErrors().get(0).getErrorMessage());
		assertEquals(0, competition.getMilestones().size());
	}
	
	@Test
	public void testUpdateMilestonesErrorsNotRepeated() {
		Competition competition = newCompetition().build();
		when(competitionRepository.findById(1L)).thenReturn(competition);
		
		List<MilestoneResource> milestones = asList(
                milestone(MilestoneName.FUNDERS_PANEL, null),
                milestone(MilestoneName.ASSESSMENT_PANEL, null),
                milestone(MilestoneName.ALLOCATE_ASSESSORS, null),
                milestone(MilestoneName.ASSESSOR_ACCEPTS, null)
			);
		
		ServiceResult<Void> result = service.update(1L, milestones);
		
		assertFalse(result.isSuccess());
		assertEquals(1, result.getFailure().getErrors().size());
		assertEquals("Milestones must have dates specified", result.getFailure().getErrors().get(0).getErrorMessage());		
		assertEquals(0, competition.getMilestones().size());
	}

	private MilestoneResource milestone(MilestoneName name, LocalDateTime date) {
		MilestoneResource resource = new MilestoneResource();
		resource.setName(name);
		resource.setDate(date);
		return resource;
	}
}
