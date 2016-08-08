package com.worth.ifs.assessment.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.assessment.form.AssessmentApplicationSummaryForm;
import com.worth.ifs.assessment.model.AssessmentSummaryModelPopulator;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.assessment.service.AssessorFormInputResponseService;
import com.worth.ifs.assessment.viewmodel.AssessmentSummaryQuestionViewModel;
import com.worth.ifs.assessment.viewmodel.AssessmentSummaryViewModel;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static com.worth.ifs.application.service.Futures.settable;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.AssessorFormInputResponseResourceBuilder.newAssessorFormInputResponseResource;
import static com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder.newProcessOutcomeResource;
import static com.worth.ifs.assessment.resource.AssessorFormInputType.*;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static com.worth.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessmentSummaryControllerTest extends BaseControllerMockMVCTest<AssessmentSummaryController> {

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private AssessorFormInputResponseService assessorFormInputResponseService;

    @Spy
    @InjectMocks
    private AssessmentSummaryModelPopulator assessmentSummaryModelPopulator;

    @Override
    protected AssessmentSummaryController supplyControllerUnderTest() {
        return new AssessmentSummaryController();
    }

    @Test
    public void testGetSummary() throws Exception {
        Long competitionId = 1L;
        Long processRoleId = 2L;
        Long applicationId = 3L;
        Long assessmentId = 4L;
        String anotherTypeOfFormInputTitle = RESEARCH_CATEGORY.getTitle();

        when(assessmentService.getById(assessmentId)).thenReturn(newAssessmentResource()
                .withProcessRole(processRoleId)
                .withProcessOutcome(asList())
                .build());

        when(processRoleService.getById(processRoleId)).thenReturn(settable(newProcessRoleResource()
                .withApplication(applicationId)
                .build()));

        ApplicationResource expectedApplication = newApplicationResource()
                .withCompetition(competitionId)
                .build();
        when(applicationService.getById(applicationId)).thenReturn(expectedApplication);

        CompetitionResource expectedCompetition = newCompetitionResource()
                .withAssessmentStartDate(now().minusDays(2))
                .withAssessmentEndDate(now().plusDays(4))
                .build();
        when(competitionService.getById(competitionId)).thenReturn(expectedCompetition);

        // The first question will have no form inputs, therefore no assessment required and should not appear in the summary
        List<FormInputResource> formInputsForQuestion1 = asList();

        // The second question will have 'application in scope' type amongst the form inputs meaning that the AssessmentSummaryQuestionViewModel.applicationInScope should get populated with any response to this input
        List<FormInputResource> formInputsForQuestion2 = newFormInputResource()
                .withId(1L, 2L)
                .withFormInputTypeTitle(anotherTypeOfFormInputTitle, APPLICATION_IN_SCOPE.getTitle())
                .withQuestion(2L, 2L)
                .build(2);

        // The third question will have 'feedback' and 'score' types amongst the form inputs meaning that the AssessmentSummaryQuestionViewModel.feedback and .scoreGiven should get populated with any response to this input
        List<FormInputResource> formInputsForQuestion3 = newFormInputResource()
                .withId(3L, 4L, 5L)
                .withFormInputTypeTitle(anotherTypeOfFormInputTitle, SCORE.getTitle(), FEEDBACK.getTitle())
                .withQuestion(3L, 3L, 3L)
                .build(3);
        when(formInputService.findAssessmentInputsByCompetition(competitionId)).thenReturn(concat(concat(formInputsForQuestion1.stream(), formInputsForQuestion2.stream()), formInputsForQuestion3.stream()).collect(toList()));

        // The fourth question will have form inputs without a complete set of responses meaning that it should be incomplete
        List<FormInputResource> formInputsForQuestion4 = newFormInputResource()
                .withId(6L, 7L)
                .withFormInputTypeTitle(anotherTypeOfFormInputTitle, FEEDBACK.getTitle())
                .withQuestion(4L, 4L)
                .build(2);
        when(formInputService.findAssessmentInputsByCompetition(competitionId)).thenReturn(concat(concat(concat(formInputsForQuestion1.stream(), formInputsForQuestion2.stream()), formInputsForQuestion3.stream()), formInputsForQuestion4.stream()).collect(toList()));

        List<AssessorFormInputResponseResource> assessorResponses = newAssessorFormInputResponseResource()
                .withQuestion(2L, 2L, 3L, 3L, 3L, 4L)
                .withFormInput(1L, 2L, 3L, 4L, 5L, 6L)
                .withValue("another response", "true", "another response", "15", "feedback", "another response")
                .build(6);
        when(assessorFormInputResponseService.getAllAssessorFormInputResponses(assessmentId))
                .thenReturn(assessorResponses);

        List<QuestionResource> questions = newQuestionResource()
                .withId(1L, 2L, 3L, 4L)
                .withSection(1L, 2L, 2L, 2L)
                .withQuestionNumber(null, null, "1", "2")
                .withShortName("Application details", "Scope", "Business opportunity", "Potential market")
                .withAssessorMaximumScore(null, null, 20, null)
                .build(4);

        when(questionService.getQuestionsByAssessment(assessmentId)).thenReturn(questions);

        MvcResult result = mockMvc.perform(get("/{assessmentId}/summary", assessmentId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessment/application-summary"))
                .andReturn();

        AssessmentSummaryViewModel model = (AssessmentSummaryViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(50, model.getDaysLeftPercentage());
        assertEquals(3, model.getDaysLeft());
        assertEquals(expectedCompetition, model.getCompetition());
        assertEquals(expectedApplication, model.getApplication());
        assertEquals(1, model.getQuestionsForScoreOverview().size());
        assertEquals(3, model.getQuestionsForReview().size());
        assertEquals(15, model.getTotalScoreGiven());
        assertEquals(20, model.getTotalScorePossible());
        assertEquals(75, model.getTotalScorePercentage());

        AssessmentSummaryQuestionViewModel scoreOverviewQuestion1 = model.getQuestionsForScoreOverview().get(0);
        assertEquals(Long.valueOf(3L), scoreOverviewQuestion1.getQuestionId());
        assertEquals("1. Business opportunity", scoreOverviewQuestion1.getDisplayLabel());
        assertEquals("Q1", scoreOverviewQuestion1.getDisplayLabelShort());
        assertTrue(scoreOverviewQuestion1.isScoreFormInputExists());
        assertEquals(Integer.valueOf(15), scoreOverviewQuestion1.getScoreGiven());
        assertEquals(Integer.valueOf(20), scoreOverviewQuestion1.getScorePossible());
        assertEquals("feedback", scoreOverviewQuestion1.getFeedback());
        assertNull(scoreOverviewQuestion1.getApplicationInScope());
        assertTrue(scoreOverviewQuestion1.isComplete());

        AssessmentSummaryQuestionViewModel reviewQuestion1 = model.getQuestionsForReview().get(0);
        assertEquals(Long.valueOf(2L), reviewQuestion1.getQuestionId());
        assertEquals("Scope", reviewQuestion1.getDisplayLabel());
        assertEquals("", reviewQuestion1.getDisplayLabelShort());
        assertFalse(reviewQuestion1.isScoreFormInputExists());
        assertNull(reviewQuestion1.getScoreGiven());
        assertNull(reviewQuestion1.getScorePossible());
        assertNull(reviewQuestion1.getFeedback());
        assertTrue(reviewQuestion1.getApplicationInScope());
        assertTrue(reviewQuestion1.isComplete());

        AssessmentSummaryQuestionViewModel reviewQuestion2 = model.getQuestionsForReview().get(1);
        assertEquals(Long.valueOf(3L), reviewQuestion2.getQuestionId());
        assertEquals("1. Business opportunity", reviewQuestion2.getDisplayLabel());
        assertEquals("Q1", reviewQuestion2.getDisplayLabelShort());
        assertTrue(reviewQuestion2.isScoreFormInputExists());
        assertEquals(Integer.valueOf(15), reviewQuestion2.getScoreGiven());
        assertEquals(Integer.valueOf(20), reviewQuestion2.getScorePossible());
        assertEquals("feedback", reviewQuestion2.getFeedback());
        assertNull(reviewQuestion2.getApplicationInScope());
        assertTrue(reviewQuestion2.isComplete());

        AssessmentSummaryQuestionViewModel reviewQuestion3 = model.getQuestionsForReview().get(2);
        assertEquals(Long.valueOf(4L), reviewQuestion3.getQuestionId());
        assertEquals("2. Potential market", reviewQuestion3.getDisplayLabel());
        assertEquals("Q2", reviewQuestion3.getDisplayLabelShort());
        assertFalse(reviewQuestion3.isScoreFormInputExists());
        assertNull(reviewQuestion3.getScoreGiven());
        assertNull(reviewQuestion3.getScorePossible());
        assertNull(reviewQuestion3.getFeedback());
        assertNull(reviewQuestion3.getApplicationInScope());
        assertFalse(reviewQuestion3.isComplete());
    }

    @Test
    public void testGetSummary_withExistingOutcome() throws Exception {
        Long competitionId = 1L;
        Long processRoleId = 2L;
        Long applicationId = 3L;
        Long assessmentId = 4L;
        Long latestProcessOutcomeId = 100L;
        Boolean expectedFundingConfirmation = TRUE;
        String expectedFeedback = "feedback";
        String expectedComments = "comments";

        when(assessmentService.getById(assessmentId)).thenReturn(newAssessmentResource()
                .withProcessRole(processRoleId)
                .withProcessOutcome(asList(1L, 2L, 3L, latestProcessOutcomeId))
                .build());

        when(processRoleService.getById(processRoleId)).thenReturn(settable(newProcessRoleResource()
                .withApplication(applicationId)
                .build()));

        ApplicationResource expectedApplication = newApplicationResource()
                .withCompetition(competitionId)
                .build();
        when(applicationService.getById(applicationId)).thenReturn(expectedApplication);

        CompetitionResource expectedCompetition = newCompetitionResource()
                .withAssessmentStartDate(now().minusDays(2))
                .withAssessmentEndDate(now().plusDays(4))
                .build();
        when(competitionService.getById(competitionId)).thenReturn(expectedCompetition);

        when(questionService.getQuestionsByAssessment(assessmentId)).thenReturn(asList());

        ProcessOutcomeResource processOutcome = newProcessOutcomeResource()
                .withOutcome(expectedFundingConfirmation.toString())
                .withDescription(expectedFeedback)
                .withComment(expectedComments)
                .build();

        when(processOutcomeService.getById(latestProcessOutcomeId)).thenReturn(processOutcome);

        AssessmentApplicationSummaryForm expectedForm = new AssessmentApplicationSummaryForm();
        expectedForm.setFundingConfirmation(expectedFundingConfirmation);
        expectedForm.setFeedback(expectedFeedback);
        expectedForm.setComments(expectedComments);

        MvcResult result = mockMvc.perform(get("/{assessmentId}/summary", assessmentId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessment/application-summary"))
                .andReturn();

        AssessmentSummaryViewModel model = (AssessmentSummaryViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(expectedCompetition, model.getCompetition());
        assertEquals(expectedApplication, model.getApplication());
    }
}
