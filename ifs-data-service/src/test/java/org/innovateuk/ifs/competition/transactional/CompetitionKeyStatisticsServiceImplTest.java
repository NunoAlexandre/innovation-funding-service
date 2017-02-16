package org.innovateuk.ifs.competition.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.domain.FundingDecisionStatus;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.EnumSet.of;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.builder.ApplicationStatisticsBuilder.newApplicationStatistics;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.CREATED_AND_OPEN_STATUS_IDS;
import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.SUBMITTED_STATUS_IDS;
import static org.innovateuk.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static org.innovateuk.ifs.assessment.builder.CompetitionParticipantBuilder.newCompetitionParticipant;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.builder.CompetitionClosedKeyStatisticsResourceBuilder.newCompetitionClosedKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionFundedKeyStatisticsResourceBuilder.newCompetitionFundedKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionInAssessmentKeyStatisticsResourceBuilder.newCompetitionInAssessmentKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionOpenKeyStatisticsResourceBuilder.newCompetitionOpenKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionReadyToOpenKeyStatisticsResourceBuilder.newCompetitionReadyToOpenKeyStatisticsResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.invite.domain.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.workflow.domain.ActivityType.APPLICATION_ASSESSMENT;
import static org.innovateuk.ifs.workflow.resource.State.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CompetitionKeyStatisticsServiceImplTest extends BaseServiceUnitTest<CompetitionKeyStatisticsServiceImpl> {


    @Override
    protected CompetitionKeyStatisticsServiceImpl supplyServiceUnderTest() {
        return new CompetitionKeyStatisticsServiceImpl();
    }


    @Test
    public void getReadyToOpenKeyStatisticsByCompetition() throws Exception {
        Long competitionId = 1L;
        CompetitionReadyToOpenKeyStatisticsResource keyStatisticsResource = newCompetitionReadyToOpenKeyStatisticsResource()
                .withAssessorsAccepted(1)
                .withAssessorsInvited(2)
                .build();

        when(competitionInviteRepositoryMock.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(OPENED, SENT))).thenReturn(keyStatisticsResource.getAssessorsInvited());
        when(competitionParticipantRepositoryMock.countByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, ParticipantStatus.ACCEPTED)).thenReturn(keyStatisticsResource.getAssessorsAccepted());

        CompetitionReadyToOpenKeyStatisticsResource response = service.getReadyToOpenKeyStatisticsByCompetition(competitionId).getSuccessObjectOrThrowException();
        assertEquals(keyStatisticsResource, response);
    }

    @Test
    public void getOpenKeyStatisticsByCompetition() throws Exception {
        Long competitionId = 1L;
        CompetitionOpenKeyStatisticsResource keyStatisticsResource = newCompetitionOpenKeyStatisticsResource()
                .withAssessorsAccepted(1)
                .withAssessorsInvited(2)
                .withApplicationsPastHalf(3)
                .withApplicationsPerAssessor(4)
                .withApplicationsStarted(5)
                .withApplicationsSubmitted(6)
                .build();

        Competition competition = newCompetition()
                .withAssessorCount(4)
                .build();

        BigDecimal limit = new BigDecimal(50L);

        when(competitionInviteRepositoryMock.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(OPENED, SENT))).thenReturn(keyStatisticsResource.getAssessorsInvited());
        when(competitionParticipantRepositoryMock.countByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, ParticipantStatus.ACCEPTED)).thenReturn(keyStatisticsResource.getAssessorsAccepted());
        when(competitionRepositoryMock.findById(competitionId)).thenReturn(competition);
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdInAndCompletionLessThanEqual(competitionId, CREATED_AND_OPEN_STATUS_IDS, limit)).thenReturn(keyStatisticsResource.getApplicationsStarted());
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdNotInAndCompletionGreaterThan(competitionId, SUBMITTED_STATUS_IDS, limit)).thenReturn(keyStatisticsResource.getApplicationsPastHalf());
        when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS)).thenReturn(keyStatisticsResource.getApplicationsSubmitted());

        CompetitionOpenKeyStatisticsResource response = service.getOpenKeyStatisticsByCompetition(competitionId).getSuccessObjectOrThrowException();
        assertEquals(keyStatisticsResource, response);
    }

    @Test
    public void getClosedKeyStatisticsByCompetition() throws Exception {
        long competitionId = 1L;

        CompetitionClosedKeyStatisticsResource keyStatisticsResource = newCompetitionClosedKeyStatisticsResource()
                .withAssessorsInvited(5)
                .withAssessorsAccepted(6)
                .withApplicationsPerAssessor(2)
                .withApplicationsRequiringAssessors(2)
                .withAssessorsWithoutApplications(2)
                .withAssignmentCount(3)
                .build();

        Competition competition = newCompetition()
                .withAssessorCount(2)
                .build();

        List<Assessment> assessments = newAssessment()
                .withActivityState(
                        new ActivityState(APPLICATION_ASSESSMENT, PENDING),
                        new ActivityState(APPLICATION_ASSESSMENT, REJECTED),
                        new ActivityState(APPLICATION_ASSESSMENT, OPEN))
                .build(3);

        List<Assessment> assessmentList = newAssessment()
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, SUBMITTED))
                .build(1);

        List<ApplicationStatistics> applicationStatistics = newApplicationStatistics()
                .withAssessments(assessments, assessmentList, emptyList())
                .build(3);

        List<CompetitionParticipant> competitionParticipants = newCompetitionParticipant()
                .withId(1L, 2L, 3L)
                .build(3);
        when(assessmentRepositoryMock.countByParticipantUserIdAndActivityStateStateNotIn(1L, of(REJECTED, WITHDRAWN))).thenReturn(0L);
        when(assessmentRepositoryMock.countByParticipantUserIdAndActivityStateStateNotIn(2L, of(REJECTED, WITHDRAWN))).thenReturn(2L);
        when(assessmentRepositoryMock.countByParticipantUserIdAndActivityStateStateNotIn(3L, of(REJECTED, WITHDRAWN))).thenReturn(0L);

        when(competitionRepositoryMock.findById(competitionId)).thenReturn(competition);
        when(applicationStatisticsRepositoryMock.findByCompetition(competitionId)).thenReturn(applicationStatistics);
        when(competitionParticipantRepositoryMock.getByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, ParticipantStatus.ACCEPTED)).thenReturn(competitionParticipants);
        when(applicationStatisticsRepositoryMock.findByCompetition(competitionId)).thenReturn(applicationStatistics);
        when(competitionInviteRepositoryMock.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(OPENED, SENT))).thenReturn(keyStatisticsResource.getAssessorsInvited());
        when(competitionParticipantRepositoryMock.countByCompetitionIdAndRoleAndStatus(competitionId, ASSESSOR, ParticipantStatus.ACCEPTED)).thenReturn(keyStatisticsResource.getAssessorsAccepted());

        CompetitionClosedKeyStatisticsResource response = service.getClosedKeyStatisticsByCompetition(competitionId).getSuccessObjectOrThrowException();
        assertEquals(keyStatisticsResource, response);

    }

    @Test
    public void getInAssessmentKeyStatisticsByCompetition() throws Exception {
        long competitionId = 1L;

        CompetitionInAssessmentKeyStatisticsResource keyStatisticsResource = newCompetitionInAssessmentKeyStatisticsResource()
                .withAssessmentsStarted(1)
                .withAssessmentsSubmitted(2)
                .withAssignmentCount(3)
                .withAssignmentsAccepted(4)
                .withAssignmentsWaiting(5)
                .build();

        List<Assessment> assessments = newAssessment()
                .withActivityState(
                        new ActivityState(APPLICATION_ASSESSMENT, PENDING),
                        new ActivityState(APPLICATION_ASSESSMENT, REJECTED),
                        new ActivityState(APPLICATION_ASSESSMENT, OPEN))
                .build(3);

        List<Assessment> assessmentList = newAssessment()
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, SUBMITTED))
                .build(1);

        List<ApplicationStatistics> applicationStatistics = newApplicationStatistics()
                .withAssessments(assessments, assessmentList)
                .build(2);

        when(applicationStatisticsRepositoryMock.findByCompetition(competitionId)).thenReturn(applicationStatistics);
        when(assessmentRepositoryMock.countByActivityStateStateAndTargetCompetitionId(PENDING, competitionId)).thenReturn(keyStatisticsResource.getAssignmentsWaiting());
        when(assessmentRepositoryMock.countByActivityStateStateAndTargetCompetitionId(ACCEPTED, competitionId)).thenReturn(keyStatisticsResource.getAssignmentsAccepted());
        when(assessmentRepositoryMock.countByActivityStateStateInAndTargetCompetitionId(of(OPEN, DECIDE_IF_READY_TO_SUBMIT, READY_TO_SUBMIT), competitionId)).thenReturn(keyStatisticsResource.getAssessmentsStarted());
        when(assessmentRepositoryMock.countByActivityStateStateAndTargetCompetitionId(SUBMITTED, competitionId)).thenReturn(keyStatisticsResource.getAssessmentsSubmitted());

        CompetitionInAssessmentKeyStatisticsResource response = service.getInAssessmentKeyStatisticsByCompetition(competitionId).getSuccessObjectOrThrowException();
        assertEquals(keyStatisticsResource, response);
    }

    @Test
    public void getFundedKeyStatisticsByCompetition() throws Exception {
        long competitionId = 1L;

        List<Application> applications = newApplication().withApplicationStatus(ApplicationStatusConstants.SUBMITTED).withFundingDecision(FundingDecisionStatus.FUNDED).build(2);

        when(applicationRepositoryMock.findByCompetitionIdAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS)).thenReturn(applications);

        CompetitionFundedKeyStatisticsResource response = service.getFundedKeyStatisticsByCompetition(competitionId).getSuccessObjectOrThrowException();
        assertEquals(2, response.getApplicationsSubmitted());
        assertEquals(2, response.getApplicationsFunded());
        assertEquals(0, response.getApplicationsNotFunded());
        assertEquals(0, response.getApplicationsOnHold());
        assertEquals(0, response.getApplicationsNotifiedOfDecision());
        assertEquals(0, response.getApplicationsAwaitingDecision());
    }
}