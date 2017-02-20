package org.innovateuk.ifs.nonifs.saver;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupMilestoneService;
import org.innovateuk.ifs.nonifs.form.NonIfsDetailsForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.MilestoneType.OPEN_DATE;
import static org.innovateuk.ifs.competition.resource.MilestoneType.RELEASE_FEEDBACK;
import static org.innovateuk.ifs.competition.resource.MilestoneType.SUBMISSION_DATE;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NonIfsDetailsFormSaverTest {

    private static final String COMPETITION_NAME = "COMPETITION_NAME";
    private static final Long INNOVATION_SECTOR = 1L;
    private static final Long INNOVATION_AREA = 2L;
    private static final String COMPETITION_URL = "COMPETITION_URL";
    private static final LocalDateTime NOTIFIED = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime OPEN = LocalDateTime.now().plusDays(2);
    private static final LocalDateTime CLOSE = LocalDateTime.now().plusDays(3);

    @InjectMocks
    private NonIfsDetailsFormSaver target;

    @Mock
    private CompetitionService competitionService;
    @Mock
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;
    @Mock
    private MilestoneService milestoneService;
    @Captor
    private ArgumentCaptor<Map<String, MilestoneRowForm>> captor;

    @Test
    public void testSaveSuccess() {
        NonIfsDetailsForm form = createForm();
        CompetitionResource competition = newCompetitionResource().withNonIfs(true).build();
        List<MilestoneResource> allMilestones = new ArrayList<>();
        when(competitionSetupMilestoneService.validateMilestoneDates(any())).thenReturn(emptyList());
        when(competitionService.update(competition)).thenReturn(serviceSuccess());
        when(milestoneService.getAllMilestonesByCompetitionId(competition.getId())).thenReturn(allMilestones);
        when(competitionSetupMilestoneService.updateMilestonesForCompetition(eq(allMilestones), any(), eq(competition.getId()))).thenReturn(emptyList());

        ServiceResult<Void> result = target.save(form, competition);

        assertThat(result.isSuccess(), equalTo(true));
        assertThat(competition.getName(), equalTo(COMPETITION_NAME));
        assertThat(competition.getNonIfsUrl(), equalTo(COMPETITION_URL));
        assertThat(competition.getInnovationAreas(), equalTo(asSet(INNOVATION_AREA)));
        assertThat(competition.getInnovationSector(), equalTo(INNOVATION_SECTOR));

        verify(competitionSetupMilestoneService).updateMilestonesForCompetition(eq(allMilestones), captor.capture(), eq(competition.getId()));

        Map<String, MilestoneRowForm> milestones = captor.getValue();

        assertThat(milestones.get(RELEASE_FEEDBACK.name()).getDate(), equalTo(NOTIFIED));
        assertThat(milestones.get(OPEN_DATE.name()).getDate(), equalTo(OPEN));
        assertThat(milestones.get(SUBMISSION_DATE.name()).getDate(), equalTo(CLOSE));
    }

    @Test
    public void testSaveIfsCompetitionFailure() {
        NonIfsDetailsForm form = createForm();
        CompetitionResource competition = newCompetitionResource().withNonIfs(false).build();

        ServiceResult<Void> result = target.save(form, competition);

        assertThat(result.isFailure(), equalTo(true));
        assertThat(result.getErrors().get(0).getErrorKey(), equalTo(CommonFailureKeys.ONLY_NON_IFS_COMPETITION_VALID.name()));
    }

    private NonIfsDetailsForm createForm() {
        NonIfsDetailsForm form = new NonIfsDetailsForm();
        form.setTitle(COMPETITION_NAME);
        form.setUrl(COMPETITION_URL);
        form.setInnovationSector(INNOVATION_SECTOR);
        form.setInnovationArea(INNOVATION_AREA);
        form.setOpenDate(new MilestoneRowForm(MilestoneType.OPEN_DATE, OPEN));
        form.setCloseDate(new MilestoneRowForm(MilestoneType.SUBMISSION_DATE, CLOSE));
        form.setApplicantNotifiedDate(new MilestoneRowForm(MilestoneType.RELEASE_FEEDBACK, NOTIFIED));
        return form;
    }

}
