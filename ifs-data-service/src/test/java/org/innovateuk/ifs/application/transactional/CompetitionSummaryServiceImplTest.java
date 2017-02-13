package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.innovateuk.ifs.application.builder.CompletedPercentageResourceBuilder.newCompletedPercentageResource;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.CREATED_AND_OPEN_STATUS_IDS;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.SUBMITTED_STATUS_IDS;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.innovateuk.ifs.invite.domain.CompetitionParticipantRole.ASSESSOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CompetitionSummaryServiceImplTest extends BaseUnitTestMocksTest {

    private static final long COMP_ID = 123L;

    @InjectMocks
    private CompetitionSummaryService competitionSummaryService = new CompetitionSummaryServiceImpl();

    private Competition competition;

    @Before
    public void setUp() {
        competition = newCompetition()
                .withId(COMP_ID)
                .withName("compname")
                .withCompetitionStatus(IN_ASSESSMENT)
                .withEndDate(LocalDateTime.of(2016, 5, 23, 8, 30))
                .build();

        when(competitionRepositoryMock.findById(COMP_ID)).thenReturn(competition);
        when(applicationRepositoryMock.countByCompetitionId(COMP_ID)).thenReturn(83);
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdInAndCompletionLessThanEqual(
                COMP_ID, CREATED_AND_OPEN_STATUS_IDS, new BigDecimal(50L))
        )
                .thenReturn(1);
        when(applicationServiceMock.getProgressPercentageByApplicationId(1L))
                .thenReturn(serviceSuccess(
                        newCompletedPercentageResource().withCompletedPercentage(new BigDecimal("20")).build()
                ));
        when(applicationServiceMock.getProgressPercentageByApplicationId(2L))
                .thenReturn(serviceSuccess(
                        newCompletedPercentageResource().withCompletedPercentage(new BigDecimal("80")).build()
                ));
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdNotInAndCompletionGreaterThan(
                COMP_ID, SUBMITTED_STATUS_IDS, new BigDecimal(50L))
        )
                .thenReturn(1);
        when(applicationServiceMock.getProgressPercentageByApplicationId(3L))
                .thenReturn(serviceSuccess(
                        newCompletedPercentageResource().withCompletedPercentage(new BigDecimal("20")).build()
                ));
        when(applicationServiceMock.getProgressPercentageByApplicationId(4L))
                .thenReturn(serviceSuccess(
                        newCompletedPercentageResource().withCompletedPercentage(new BigDecimal("80")).build()
                ));
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdIn(COMP_ID, SUBMITTED_STATUS_IDS)).thenReturn(5);
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusId(COMP_ID, 3L)).thenReturn(8);
        when(competitionParticipantRepositoryMock.countByCompetitionIdAndRole(COMP_ID, ASSESSOR)).thenReturn(10);
    }

    @Test
    public void getCompetitionSummaryByCompetitionId() {
        ServiceResult<CompetitionSummaryResource> result = competitionSummaryService.getCompetitionSummaryByCompetitionId(COMP_ID);

        assertTrue(result.isSuccess());

        CompetitionSummaryResource summaryResource = result.getSuccessObject();

        assertEquals(COMP_ID, summaryResource.getCompetitionId());
        assertEquals(competition.getName(), summaryResource.getCompetitionName());
        assertEquals(competition.getCompetitionStatus(), summaryResource.getCompetitionStatus());
        assertEquals(83, summaryResource.getTotalNumberOfApplications());
        assertEquals(1, summaryResource.getApplicationsStarted());
        assertEquals(1, summaryResource.getApplicationsInProgress());
        assertEquals(5, summaryResource.getApplicationsSubmitted());
        assertEquals(78, summaryResource.getApplicationsNotSubmitted());
        assertEquals(LocalDateTime.of(2016, 5, 23, 8, 30), summaryResource.getApplicationDeadline());
        assertEquals(10, summaryResource.getAssessorsInvited());
        assertEquals(8, summaryResource.getApplicationsFunded());
    }

}
