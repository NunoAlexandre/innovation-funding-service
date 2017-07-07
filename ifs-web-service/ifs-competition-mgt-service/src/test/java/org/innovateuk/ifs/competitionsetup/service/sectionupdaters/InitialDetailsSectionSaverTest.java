package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.TimeZoneUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.UserRoleType.INNOVATION_LEAD;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InitialDetailsSectionSaverTest {

    @InjectMocks
    private InitialDetailsSectionSaver service;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private MilestoneRestService milestoneRestService;

    @Mock
    private CategoryRestService categoryRestService;

    @Mock
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;

    @Mock
    private UserService userService;

    //TODO INFUND-9493: Create tests for situations surrounding Milestone saving
    //TODO INFUND-9493: Create test for invalid date handling
    //TODO INFUND-9493: Create test for situations surrounding retrieval of innovation sector

    @Test
    public void saveCompetitionSetupSection() {
        Long executiveUserId = 1L;
        Long competitionTypeId = 2L;
        Long leadTechnologistId = 3L;
        Long innovationAreaId = 4L;
        Long innovationSectorId = 5L;

        ZonedDateTime openingDate = ZonedDateTime.of(2020, 12, 1, 0, 0, 0, 0, TimeZoneUtil.UK_TIME_ZONE);

        InitialDetailsForm competitionSetupForm = new InitialDetailsForm();
        competitionSetupForm.setTitle("title");
        competitionSetupForm.setExecutiveUserId(executiveUserId);
        competitionSetupForm.setOpeningDateDay(openingDate.getDayOfMonth());
        competitionSetupForm.setOpeningDateMonth(openingDate.getMonthValue());
        competitionSetupForm.setOpeningDateYear(openingDate.getYear());
        competitionSetupForm.setLeadTechnologistUserId(leadTechnologistId);
        competitionSetupForm.setCompetitionTypeId(competitionTypeId);
        competitionSetupForm.setInnovationSectorCategoryId(innovationSectorId);

        InnovationAreaResource innovationArea = newInnovationAreaResource().withId(innovationAreaId).build();
        competitionSetupForm.setInnovationAreaCategoryIds(asList(innovationAreaId));

        List<MilestoneResource> milestones = new ArrayList<>();
        milestones.add(getMilestone());

        List<Long> milestonesIds = new ArrayList<>();
        milestonesIds.add(10L);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("compcode").build();
        competition.setMilestones(milestonesIds);
        competition.setSetupComplete(false);

        when(milestoneRestService.getAllMilestonesByCompetitionId(competition.getId())).thenReturn(restSuccess(milestones));
        when(categoryRestService.getInnovationAreas()).thenReturn(restSuccess(asList(innovationArea)));
        when(categoryRestService.getInnovationAreasBySector(innovationSectorId)).thenReturn(restSuccess(singletonList(innovationArea)));
        when(competitionService.initApplicationFormByCompetitionType(competition.getId(), competitionSetupForm.getCompetitionTypeId())).thenReturn(serviceSuccess());
        when(competitionService.update(competition)).thenReturn(serviceSuccess());
        when(competitionSetupMilestoneService.createMilestonesForCompetition(anyLong())).thenReturn(serviceSuccess(milestones));
        when(competitionSetupMilestoneService.updateMilestonesForCompetition(anyList(), anyMap(), anyLong())).thenReturn(serviceSuccess());
        when(userService.existsAndHasRole(executiveUserId, COMP_ADMIN)).thenReturn(true);
        when(userService.existsAndHasRole(leadTechnologistId, INNOVATION_LEAD)).thenReturn(true);

        service.saveSection(competition, competitionSetupForm);

        assertEquals("title", competition.getName());
        assertEquals(competition.getExecutive(), executiveUserId);
        assertEquals(competition.getCompetitionType(), competitionTypeId);
        assertEquals(competition.getLeadTechnologist(), leadTechnologistId);
        // We don't care about the order of the innovation area ids, so compare as a set
        Set<Long> expectedInnovationAreaIds = asSet(innovationAreaId);
        Set<Long> actualInnovationAreaIds = competition.getInnovationAreas().stream().collect(Collectors.toSet());
        assertEquals(expectedInnovationAreaIds, actualInnovationAreaIds);
        assertEquals(competition.getInnovationSector(), innovationSectorId);
        assertEquals(openingDate, competition.getStartDate());
        assertEquals(competition.getCompetitionType(), competitionTypeId);
        assertEquals(innovationSectorId, competition.getInnovationSector());

        verify(competitionService).update(competition);
        verify(competitionService).initApplicationFormByCompetitionType(competition.getId(), competitionSetupForm.getCompetitionTypeId());
        verify(userService).existsAndHasRole(executiveUserId, COMP_ADMIN);
        verify(userService).existsAndHasRole(leadTechnologistId, INNOVATION_LEAD);
    }

    @Test
    public void autoSaveCompetitionSetupSection() {
        CompetitionResource competition = newCompetitionResource().build();
        competition.setMilestones(singletonList(10L));
        when(milestoneRestService.getAllMilestonesByCompetitionId(competition.getId())).thenReturn(restSuccess(singletonList(getMilestone())));
        when(competitionService.update(competition)).thenReturn(serviceSuccess());
        when(competitionSetupMilestoneService.createMilestonesForCompetition(anyLong())).thenReturn(serviceSuccess(singletonList(getMilestone())));
        when(competitionSetupMilestoneService.updateMilestonesForCompetition(anyList(), anyMap(), anyLong())).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.autoSaveSectionField(competition, null, "openingDate", "20-10-" + (ZonedDateTime.now().getYear() + 1), null);

        assertTrue(result.isSuccess());
        verify(competitionService).update(competition);
    }

    @Test
    public void autoSaveInnovationAreaCategoryIds() {

        CompetitionResource competition = newCompetitionResource().build();
        competition.setInnovationAreas(Collections.singleton(999L));

        when(competitionService.update(competition)).thenReturn(serviceSuccess());

        ServiceResult<Void> errors = service.autoSaveSectionField(competition, null, "autosaveInnovationAreaIds", "1,2, 3", null);

        assertTrue(errors.isSuccess());
        assertThat(competition.getInnovationAreas(), hasItems(1L, 2L, 3L));
        assertThat(competition.getInnovationAreas(), hasSize(3));
        verify(competitionService).update(competition);
    }

    @Test
    public void autoSaveCompetitionSetupSectionUnknown() {
        CompetitionResource competition = newCompetitionResource().build();

        ServiceResult<Void> errors = service.autoSaveSectionField(competition, null, "notExisting", "Strange!@#1Value", null);

        assertTrue(!errors.isSuccess());
        verify(competitionService, never()).update(competition);
    }

    @Test
    public void completedCompetitionCanSetOnlyLeadTechnologistAndExecutive() {
        String newTitle = "New title";
        Long newExec = 1L;
        Long leadTechnologistId = 2L;
        Long competitionTypeId = 3L;
        Long innovationSectorId = 4L;

        ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);

        CompetitionResource competition = newCompetitionResource()
                .withSetupComplete(true)
                .withStartDate(yesterday)
                .withFundersPanelDate(tomorrow)
                .build();

        InitialDetailsForm form = new InitialDetailsForm();
        form.setTitle(newTitle);
        form.setExecutiveUserId(newExec);
        form.setLeadTechnologistUserId(leadTechnologistId);
        form.setCompetitionTypeId(competitionTypeId);
        form.setInnovationSectorCategoryId(innovationSectorId);

        when(userService.existsAndHasRole(newExec, COMP_ADMIN)).thenReturn(true);
        when(userService.existsAndHasRole(leadTechnologistId, INNOVATION_LEAD)).thenReturn(true);
        when(competitionService.update(competition)).thenReturn(serviceSuccess());

        service.saveSection(competition, form);

        assertNull(competition.getName());
        assertEquals(competition.getLeadTechnologist(), leadTechnologistId);
        assertEquals(competition.getExecutive(), newExec);
        assertNull(competition.getCompetitionType());
        assertNull(competition.getInnovationSector());

        verify(userService).existsAndHasRole(newExec, COMP_ADMIN);
        verify(userService).existsAndHasRole(leadTechnologistId, INNOVATION_LEAD);
    }

    private MilestoneResource getMilestone(){
        MilestoneResource milestone = new MilestoneResource();
        milestone.setId(10L);
        milestone.setType(MilestoneType.OPEN_DATE);
        milestone.setDate(ZonedDateTime.of(2020, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
        milestone.setCompetitionId(1L);
        return milestone;
    }

    @Test
    public void supportsForm() {
        assertTrue(service.supportsForm(InitialDetailsForm.class));
        assertFalse(service.supportsForm(CompetitionSetupForm.class));
    }

    @Test
    public void compExecNotValid() {
        Long executiveUserId = 1L;
        Long competitionTypeId = 2L;
        Long leadTechnologistId = 3L;
        Long innovationAreaId = 4L;
        Long innovationSectorId = 5L;

        ZonedDateTime openingDate = ZonedDateTime.of(2020, 12, 1, 0, 0, 0, 0, TimeZoneUtil.UK_TIME_ZONE);

        InitialDetailsForm competitionSetupForm = new InitialDetailsForm();
        competitionSetupForm.setTitle("title");
        competitionSetupForm.setExecutiveUserId(executiveUserId);
        competitionSetupForm.setOpeningDateDay(openingDate.getDayOfMonth());
        competitionSetupForm.setOpeningDateMonth(openingDate.getMonthValue());
        competitionSetupForm.setOpeningDateYear(openingDate.getYear());
        competitionSetupForm.setLeadTechnologistUserId(leadTechnologistId);
        competitionSetupForm.setCompetitionTypeId(competitionTypeId);
        competitionSetupForm.setInnovationSectorCategoryId(innovationSectorId);

        competitionSetupForm.setInnovationAreaCategoryIds(asList(innovationAreaId, 1L, 2L, 3L));

        List<Long> milestonesIds = new ArrayList<>();
        milestonesIds.add(10L);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("compcode").build();
        competition.setMilestones(milestonesIds);
        competition.setSetupComplete(false);

        when(userService.existsAndHasRole(executiveUserId, COMP_ADMIN)).thenReturn(false);

        ServiceResult<Void> result = service.saveSection(competition, competitionSetupForm);

        assertTrue(result.isFailure());
        assertEquals("competition.setup.invalid.comp.exec", result.getFailure().getErrors().get(0).getErrorKey());
        assertEquals("executiveUserId", result.getFailure().getErrors().get(0).getFieldName());

        verify(userService).existsAndHasRole(executiveUserId, COMP_ADMIN);
    }

    @Test
    public void compTechnologistNotValid() {
        Long executiveUserId = 1L;
        Long competitionTypeId = 2L;
        Long leadTechnologistId = 3L;
        Long innovationAreaId = 4L;
        Long innovationSectorId = 5L;

        ZonedDateTime openingDate = ZonedDateTime.of(2020, 12, 1, 0, 0, 0, 0, TimeZoneUtil.UK_TIME_ZONE);

        InitialDetailsForm competitionSetupForm = new InitialDetailsForm();
        competitionSetupForm.setTitle("title");
        competitionSetupForm.setExecutiveUserId(executiveUserId);
        competitionSetupForm.setOpeningDateDay(openingDate.getDayOfMonth());
        competitionSetupForm.setOpeningDateMonth(openingDate.getMonthValue());
        competitionSetupForm.setOpeningDateYear(openingDate.getYear());
        competitionSetupForm.setLeadTechnologistUserId(leadTechnologistId);
        competitionSetupForm.setCompetitionTypeId(competitionTypeId);
        competitionSetupForm.setInnovationSectorCategoryId(innovationSectorId);

        competitionSetupForm.setInnovationAreaCategoryIds(asList(innovationAreaId, 1L, 2L, 3L));

        List<Long> milestonesIds = new ArrayList<>();
        milestonesIds.add(10L);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("compcode").build();
        competition.setMilestones(milestonesIds);
        competition.setSetupComplete(false);

        when(userService.existsAndHasRole(executiveUserId, COMP_ADMIN)).thenReturn(true);
        when(userService.existsAndHasRole(leadTechnologistId, INNOVATION_LEAD)).thenReturn(false);

        ServiceResult<Void> result = service.saveSection(competition, competitionSetupForm);

        assertTrue(result.isFailure());
        assertEquals("competition.setup.invalid.comp.technologist", result.getFailure().getErrors().get(0).getErrorKey());
        assertEquals("innovationLeadUserId", result.getFailure().getErrors().get(0).getFieldName());

        verify(userService).existsAndHasRole(executiveUserId, COMP_ADMIN);
        verify(userService).existsAndHasRole(leadTechnologistId, INNOVATION_LEAD);
    }
}
