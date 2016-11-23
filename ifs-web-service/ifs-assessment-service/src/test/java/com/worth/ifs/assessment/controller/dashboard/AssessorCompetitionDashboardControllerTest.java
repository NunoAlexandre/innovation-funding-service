package com.worth.ifs.assessment.controller.dashboard;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.assessment.model.AssessorCompetitionDashboardModelPopulator;
import com.worth.ifs.assessment.resource.AssessmentOutcomes;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.AssessmentTotalScoreResource;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.assessment.viewmodel.AssessorCompetitionDashboardApplicationViewModel;
import com.worth.ifs.assessment.viewmodel.AssessorCompetitionDashboardViewModel;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.RoleResource;
import com.worth.ifs.user.resource.UserRoleType;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.AssessmentTotalScoreResourceBuilder.newAssessmentTotalScoreResource;
import static com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder.newProcessOutcomeResource;
import static com.worth.ifs.assessment.resource.AssessmentStates.*;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessorCompetitionDashboardControllerTest extends BaseControllerMockMVCTest<AssessorCompetitionDashboardController> {

    @Spy
    @InjectMocks
    private AssessorCompetitionDashboardModelPopulator assessorCompetitionDashboardModelPopulator;

    @Mock
    private AssessmentService assessmentService;

    @Override
    protected AssessorCompetitionDashboardController supplyControllerUnderTest() {
        return new AssessorCompetitionDashboardController();
    }

    @Test
    public void competitionDashboard() throws Exception {
        Long userId = 1L;

        CompetitionResource competition = buildTestCompetition();
        List<ApplicationResource> applications = buildTestApplications();

        List<AssessmentResource> assessments = newAssessmentResource()
                .withId(1L, 2L, 3L, 4L)
                .withApplication(applications.get(0).getId(), applications.get(1).getId(), applications.get(2).getId(), applications.get(3).getId())
                .withCompetition(competition.getId())
                .withActivityState(PENDING, ACCEPTED, READY_TO_SUBMIT, SUBMITTED)
                .build(4);

        List<ProcessOutcomeResource> processOutcomes = newProcessOutcomeResource()
                .withId(1L, 2L)
                .withOutcome(null, Boolean.toString(false))
                .build(2);

        List<AssessmentTotalScoreResource> totalScores = newAssessmentTotalScoreResource()
                .withTotalScoreGiven(0, 0, 50, 55)
                .withTotalScorePossible(100, 100, 100, 100)
                .build(4);

        RoleResource role = buildLeadApplicantRole();
        List<OrganisationResource> organisations = buildTestOrganisations();
        List<ProcessRoleResource> participants = newProcessRoleResource()
                .withRole(role)
                .withOrganisation(organisations.get(0).getId(), organisations.get(1).getId(), organisations.get(2).getId(), organisations.get(3).getId())
                .build(4);

        when(competitionService.getById(competition.getId())).thenReturn(competition);
        when(assessmentService.getByUserAndCompetition(userId, competition.getId())).thenReturn(assessments);
        applications.forEach(application -> when(applicationService.getById(application.getId())).thenReturn(application));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(0).getId())).thenReturn(asList(participants.get(0)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(1).getId())).thenReturn(asList(participants.get(1)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(2).getId())).thenReturn(asList(participants.get(2)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(3).getId())).thenReturn(asList(participants.get(3)));
        when(processOutcomeService.getByProcessIdAndOutcomeType(assessments.get(2).getId(), AssessmentOutcomes.FUNDING_DECISION.getType())).thenReturn(processOutcomes.get(0));
        when(processOutcomeService.getByProcessIdAndOutcomeType(assessments.get(3).getId(), AssessmentOutcomes.FUNDING_DECISION.getType())).thenReturn(processOutcomes.get(1));
        when(assessmentService.getTotalScore(assessments.get(0).getId())).thenReturn(totalScores.get(0));
        when(assessmentService.getTotalScore(assessments.get(1).getId())).thenReturn(totalScores.get(1));
        when(assessmentService.getTotalScore(assessments.get(2).getId())).thenReturn(totalScores.get(2));
        when(assessmentService.getTotalScore(assessments.get(3).getId())).thenReturn(totalScores.get(3));

        organisations.forEach(organisation -> when(organisationRestService.getOrganisationById(organisation.getId())).thenReturn(restSuccess(organisation)));

        MvcResult result = mockMvc.perform(get("/assessor/dashboard/competition/{competitionId}", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessor-competition-dashboard"))
                .andReturn();

        InOrder inOrder = inOrder(competitionService, assessmentService, applicationService, processRoleService, organisationRestService, processOutcomeService);
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(assessmentService).getByUserAndCompetition(userId, competition.getId());

        assessments.forEach(assessment -> {
            inOrder.verify(applicationService).getById(assessment.getApplication());
            inOrder.verify(processRoleService).findProcessRolesByApplicationId(assessment.getApplication());
            inOrder.verify(organisationRestService).getOrganisationById(isA(Long.class));
            if (assessment.getAssessmentState() == SUBMITTED || assessment.getAssessmentState() == READY_TO_SUBMIT) {
                inOrder.verify(processOutcomeService).getByProcessIdAndOutcomeType(assessment.getId(), AssessmentOutcomes.FUNDING_DECISION.getType());
            } else {
                inOrder.verify(processOutcomeService, never()).getByProcessIdAndOutcomeType(assessment.getId(), AssessmentOutcomes.FUNDING_DECISION.getType());
            }
            inOrder.verify(assessmentService).getTotalScore(assessment.getId());
        });

        inOrder.verifyNoMoreInteractions();

        List<AssessorCompetitionDashboardApplicationViewModel> expectedSubmitted = asList(
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(3).getId(), assessments.get(3).getId(), applications.get(3).getName(), organisations.get(3).getName(), assessments.get(3).getAssessmentState(), totalScores.get(3).getTotalScorePercentage(), false)
        );

        List<AssessorCompetitionDashboardApplicationViewModel> expectedOutstanding = asList(
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(0).getId(), assessments.get(0).getId(), applications.get(0).getName(), organisations.get(0).getName(), assessments.get(0).getAssessmentState(), totalScores.get(0).getTotalScorePercentage(), false),
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(1).getId(), assessments.get(1).getId(), applications.get(1).getName(), organisations.get(1).getName(), assessments.get(1).getAssessmentState(), totalScores.get(1).getTotalScorePercentage(), false),
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(2).getId(), assessments.get(2).getId(), applications.get(2).getName(), organisations.get(2).getName(), assessments.get(2).getAssessmentState(), totalScores.get(2).getTotalScorePercentage(), false)
        );

        AssessorCompetitionDashboardViewModel model = (AssessorCompetitionDashboardViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(competition.getName(), model.getCompetitionTitle());
        assertEquals("Juggling Craziness (CRD3359)", model.getCompetition());
        assertEquals("Competition Technologist", model.getLeadTechnologist());
        assertEquals(competition.getAssessorAcceptsDate(), model.getAcceptDeadline());
        assertEquals(competition.getAssessorDeadlineDate(), model.getSubmitDeadline());
        assertEquals(expectedSubmitted, model.getSubmitted());
        assertEquals(expectedOutstanding, model.getOutstanding());
        assertTrue(model.isSubmitVisible());
    }

    @Test
    public void competitionDashboard_submitNotVisible() throws Exception {
        Long userId = 1L;

        CompetitionResource competition = buildTestCompetition();
        List<ApplicationResource> applications = buildTestApplications();

        List<AssessmentResource> assessments = newAssessmentResource()
                .withId(1L, 2L, 3L, 4L)
                .withApplication(applications.get(0).getId(), applications.get(1).getId(), applications.get(2).getId(), applications.get(3).getId())
                .withCompetition(competition.getId())
                .withActivityState(PENDING, ACCEPTED, OPEN, SUBMITTED)
                .withProcessOutcome(null, emptyList(), asList(0L, 1L), singletonList(2L))
                .build(4);

        List<ProcessOutcomeResource> processOutcomes = newProcessOutcomeResource()
                .withId(1L, 2L)
                .withOutcome(null, Boolean.toString(true))
                .build(2);

        List<AssessmentTotalScoreResource> totalScores = newAssessmentTotalScoreResource()
                .withTotalScoreGiven(0, 0, 50, 55)
                .withTotalScorePossible(100, 100, 100, 100)
                .build(4);

        RoleResource role = buildLeadApplicantRole();
        List<OrganisationResource> organisations = buildTestOrganisations();
        List<ProcessRoleResource> participants = newProcessRoleResource()
                .withRole(role)
                .withOrganisation(organisations.get(0).getId(), organisations.get(1).getId(), organisations.get(2).getId(), organisations.get(3).getId())
                .build(4);

        when(competitionService.getById(competition.getId())).thenReturn(competition);
        when(assessmentService.getByUserAndCompetition(userId, competition.getId())).thenReturn(assessments);
        applications.forEach(application -> when(applicationService.getById(application.getId())).thenReturn(application));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(0).getId())).thenReturn(asList(participants.get(0)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(1).getId())).thenReturn(asList(participants.get(1)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(2).getId())).thenReturn(asList(participants.get(2)));
        when(processRoleService.findProcessRolesByApplicationId(applications.get(3).getId())).thenReturn(asList(participants.get(3)));
        when(processOutcomeService.getByProcessIdAndOutcomeType(assessments.get(2).getId(), AssessmentOutcomes.FUNDING_DECISION.getType())).thenReturn(processOutcomes.get(0));
        when(processOutcomeService.getByProcessIdAndOutcomeType(assessments.get(3).getId(), AssessmentOutcomes.FUNDING_DECISION.getType())).thenReturn(processOutcomes.get(1));
        when(assessmentService.getTotalScore(assessments.get(0).getId())).thenReturn(totalScores.get(0));
        when(assessmentService.getTotalScore(assessments.get(1).getId())).thenReturn(totalScores.get(1));
        when(assessmentService.getTotalScore(assessments.get(2).getId())).thenReturn(totalScores.get(2));
        when(assessmentService.getTotalScore(assessments.get(3).getId())).thenReturn(totalScores.get(3));

        organisations.forEach(organisation -> when(organisationRestService.getOrganisationById(organisation.getId())).thenReturn(restSuccess(organisation)));

        MvcResult result = mockMvc.perform(get("/assessor/dashboard/competition/{competitionId}", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessor-competition-dashboard"))
                .andReturn();

        InOrder inOrder = inOrder(competitionService, assessmentService, applicationService, processRoleService, organisationRestService, processOutcomeService);
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(assessmentService).getByUserAndCompetition(userId, competition.getId());

        assessments.forEach(assessment -> {
            inOrder.verify(applicationService).getById(assessment.getApplication());
            inOrder.verify(processRoleService).findProcessRolesByApplicationId(assessment.getApplication());
            inOrder.verify(organisationRestService).getOrganisationById(isA(Long.class));
            if (assessment.getAssessmentState() == SUBMITTED || assessment.getAssessmentState() == READY_TO_SUBMIT) {
                inOrder.verify(processOutcomeService).getByProcessIdAndOutcomeType(assessment.getId(), AssessmentOutcomes.FUNDING_DECISION.getType());
            } else {
                inOrder.verify(processOutcomeService, never()).getByProcessIdAndOutcomeType(assessment.getId(), AssessmentOutcomes.FUNDING_DECISION.getType());
            }
            inOrder.verify(assessmentService).getTotalScore(assessment.getId());
        });

        inOrder.verifyNoMoreInteractions();

        List<AssessorCompetitionDashboardApplicationViewModel> expectedSubmitted = asList(
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(3).getId(), assessments.get(3).getId(), applications.get(3).getName(), organisations.get(3).getName(), assessments.get(3).getAssessmentState(), totalScores.get(3).getTotalScorePercentage(), true)
        );

        List<AssessorCompetitionDashboardApplicationViewModel> expectedOutstanding = asList(
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(0).getId(), assessments.get(0).getId(), applications.get(0).getName(), organisations.get(0).getName(), assessments.get(0).getAssessmentState(), totalScores.get(0).getTotalScorePercentage(), false),
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(1).getId(), assessments.get(1).getId(), applications.get(1).getName(), organisations.get(1).getName(), assessments.get(1).getAssessmentState(), totalScores.get(1).getTotalScorePercentage(), false),
                new AssessorCompetitionDashboardApplicationViewModel(applications.get(2).getId(), assessments.get(2).getId(), applications.get(2).getName(), organisations.get(2).getName(), assessments.get(2).getAssessmentState(), totalScores.get(2).getTotalScorePercentage(), false)
        );

        AssessorCompetitionDashboardViewModel model = (AssessorCompetitionDashboardViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(competition.getName(), model.getCompetitionTitle());
        assertEquals("Juggling Craziness (CRD3359)", model.getCompetition());
        assertEquals("Competition Technologist", model.getLeadTechnologist());
        assertEquals(competition.getAssessorAcceptsDate(), model.getAcceptDeadline());
        assertEquals(competition.getAssessorDeadlineDate(), model.getSubmitDeadline());
        assertEquals(expectedSubmitted, model.getSubmitted());
        assertEquals(expectedOutstanding, model.getOutstanding());
        assertFalse(model.isSubmitVisible());
    }

    @Test
    public void competitionDashboard_empty() throws Exception {
        Long userId = 1L;

        CompetitionResource competition = buildTestCompetition();

        when(competitionService.getById(competition.getId())).thenReturn(competition);
        when(assessmentService.getByUserAndCompetition(userId, competition.getId())).thenReturn(emptyList());

        MvcResult result = mockMvc.perform(get("/assessor/dashboard/competition/{competitionId}", competition.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessor-competition-dashboard"))
                .andReturn();

        InOrder inOrder = inOrder(competitionService, assessmentService);
        inOrder.verify(competitionService).getById(competition.getId());
        inOrder.verify(assessmentService).getByUserAndCompetition(userId, competition.getId());
        inOrder.verifyNoMoreInteractions();

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(processRoleService);
        verifyZeroInteractions(organisationRestService);

        AssessorCompetitionDashboardViewModel model = (AssessorCompetitionDashboardViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(competition.getName(), model.getCompetitionTitle());
        assertEquals("Juggling Craziness (CRD3359)", model.getCompetition());
        assertEquals("Competition Technologist", model.getLeadTechnologist());
        assertEquals(competition.getAssessorAcceptsDate(), model.getAcceptDeadline());
        assertEquals(competition.getAssessorDeadlineDate(), model.getSubmitDeadline());
        assertTrue(model.getSubmitted().isEmpty());
        assertTrue(model.getOutstanding().isEmpty());
        assertFalse(model.isSubmitVisible());
    }

    private CompetitionResource buildTestCompetition() {
        LocalDateTime assessorAcceptsDate = LocalDateTime.now().minusDays(2);
        LocalDateTime assessorDeadlineDate = LocalDateTime.now().plusDays(4);

        return newCompetitionResource()
                .withName("Juggling Craziness")
                .withDescription("Juggling Craziness (CRD3359)")
                .withLeadTechnologist(2L)
                .withLeadTechnologistName("Competition Technologist")
                .withAssessorAcceptsDate(assessorAcceptsDate)
                .withAssessorDeadlineDate(assessorDeadlineDate)
                .build();
    }

    private List<ApplicationResource> buildTestApplications() {
        return newApplicationResource()
                .withId(11L, 12L, 13L, 14L)
                .withName("Juggling is fun", "Juggling is very fun", "Juggling is not fun", "Juggling is word that sounds funny to say")
                .build(4);
    }

    private RoleResource buildLeadApplicantRole() {
        return newRoleResource().withType(UserRoleType.LEADAPPLICANT).build();
    }

    private List<OrganisationResource> buildTestOrganisations() {
        return newOrganisationResource()
                .withId(1L, 2L, 3L, 4L)
                .withName("The Best Juggling Company", "Juggle Ltd", "Jugglez Ltd", "Mo Juggling Mo Problems Ltd")
                .build(4);
    }
}