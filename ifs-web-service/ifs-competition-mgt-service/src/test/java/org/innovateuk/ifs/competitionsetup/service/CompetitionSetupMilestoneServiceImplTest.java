package org.innovateuk.ifs.competitionsetup.service;

import org.apache.commons.collections4.map.LinkedMap;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.MilestoneResourceBuilder.newMilestoneResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompetitionSetupMilestoneServiceImplTest {

	@InjectMocks
	private CompetitionSetupMilestoneServiceImpl service;
	
	@Mock
	private MilestoneService milestoneService;

	@Test
	public void testCreateMilestonesForCompetition() {
        when(milestoneService.create(any(MilestoneType.class), anyLong())).thenReturn(newMilestoneResource().with(
				(integer, milestoneResource) -> {
					milestoneResource.setType(MilestoneType.OPEN_DATE);
				}
		).build());

		List<MilestoneResource> result = service.createMilestonesForCompetition(123L);

        result.forEach(milestoneResource -> assertEquals(MilestoneType.OPEN_DATE, milestoneResource.getType()));
		assertEquals(MilestoneType.presetValues().length, result.size());
		verify(milestoneService, times(MilestoneType.presetValues().length)).create(any(MilestoneType.class), anyLong());
	}

	@Test
	public void testUpdateMilestonesForCompetition() {
        List<MilestoneResource> oldMilestones = asList(
                newMilestoneResource()
                .with(milestoneResource -> milestoneResource.setType(MilestoneType.SUBMISSION_DATE))
                .withDate(LocalDateTime.MAX)
                .build());

        LinkedMap<String, MilestoneRowForm> newMilestones = new LinkedMap<>();
        MilestoneRowForm milestoneRowForm = new MilestoneRowForm(MilestoneType.SUBMISSION_DATE, LocalDateTime.MIN);
        newMilestones.put(MilestoneType.SUBMISSION_DATE.name(), milestoneRowForm);

        when(milestoneService.updateMilestones(anyListOf(MilestoneResource.class))).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.updateMilestonesForCompetition(oldMilestones, newMilestones, 123L);

        assertTrue(result.isSuccess());
        MilestoneRowForm newMilestone = newMilestones.get(MilestoneType.SUBMISSION_DATE.name());
        assertEquals(Integer.valueOf(LocalDate.MIN.getDayOfMonth()), newMilestone.getDay());
        assertEquals(Integer.valueOf(LocalDate.MIN.getMonthValue()), newMilestone.getMonth());
        assertEquals(Integer.valueOf(LocalDate.MIN.getYear()), newMilestone.getYear());
	}

    @Test
    public void validateMilestoneDatesTrue() {
        LinkedMap<String, MilestoneRowForm> milestones = new LinkedMap<>();
        MilestoneRowForm milestoneRowForm = new MilestoneRowForm(MilestoneType.SUBMISSION_DATE, LocalDateTime.MIN);
        milestones.put(MilestoneType.SUBMISSION_DATE.name(), milestoneRowForm);

        List<Error> result = service.validateMilestoneDates(milestones);

        assertTrue(result.isEmpty());
    }

    @Test
    public void validateMilestoneDatesFalse() {
        LinkedMap<String, MilestoneRowForm> milestones = new LinkedMap<>();
        MilestoneRowForm milestoneRowForm = new MilestoneRowForm(MilestoneType.SUBMISSION_DATE, LocalDateTime.MAX);
        milestones.put(MilestoneType.SUBMISSION_DATE.name(), milestoneRowForm);

        List<Error> result = service.validateMilestoneDates(milestones);

        assertTrue(!result.isEmpty());
    }

    @Test
    public void testisMilestoneDateValidTrue() {
        Boolean resultOne = service.isMilestoneDateValid(1, 1, 1);
        Boolean resultTwo = service.isMilestoneDateValid(1, 1, 2000);
        Boolean resultThree = service.isMilestoneDateValid(31, 12, 9999);

        assertTrue(resultOne);
        assertTrue(resultTwo);
        assertTrue(resultThree);
    }

    @Test
    public void testisMilestoneDateValidFalse() {
        Boolean resultOne = service.isMilestoneDateValid(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Boolean resultTwo = service.isMilestoneDateValid(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        Boolean resultThree = service.isMilestoneDateValid(2019, 12, 31);

        assertFalse(resultOne);
        assertFalse(resultTwo);
        assertFalse(resultThree);
    }
}
