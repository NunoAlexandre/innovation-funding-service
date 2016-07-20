package com.worth.ifs.assessment.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.form.Form;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.assessment.model.AssessmentFeedbackApplicationDetailsModelPopulator;
import com.worth.ifs.assessment.model.AssessmentFeedbackModelPopulator;
import com.worth.ifs.assessment.model.AssessmentFeedbackNavigationModelPopulator;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.assessment.service.AssessorFormInputResponseService;
import com.worth.ifs.assessment.viewmodel.AssessmentFeedbackApplicationDetailsViewModel;
import com.worth.ifs.assessment.viewmodel.AssessmentFeedbackViewModel;
import com.worth.ifs.assessment.viewmodel.AssessmentNavigationViewModel;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.form.resource.FormInputTypeResource;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.worth.ifs.BaseBuilderAmendFunctions.idBasedValues;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.AssessorFormInputResponseResourceBuilder.newAssessorFormInputResponseResource;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static com.worth.ifs.form.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static com.worth.ifs.util.CollectionFunctions.simpleToMap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessmentFeedbackControllerTest extends BaseControllerMockMVCTest<AssessmentFeedbackController> {
    @Mock
    private AssessmentService assessmentService;

    @Mock
    private AssessorFormInputResponseService assessorFormInputResponseService;

    @Spy
    @InjectMocks
    private AssessmentFeedbackModelPopulator assessmentFeedbackModelPopulator;

    @Spy
    @InjectMocks
    private AssessmentFeedbackNavigationModelPopulator assessmentFeedbackNavigationModelPopulator;

    @Spy
    @InjectMocks
    private AssessmentFeedbackApplicationDetailsModelPopulator assessmentFeedbackApplicationDetailsModelPopulator;

    private static Long APPLICATION_ID = 2L; // "Providing sustainable childcare"
    private static Long QUESTION_ID = 20L; // 1. What is the business opportunity that this project addresses?
    private static Long APPLICATION_DETAILS_QUESTION_ID = 1L;
    private static Long PROCESS_ROLE_ID = 6L;
    private static Long ASSESSMENT_ID = 1L;
    private static Map<String, FormInputTypeResource> FORM_INPUT_TYPES = simpleToMap(asList(
            new FormInputTypeResource(1L, "textarea"),
            new FormInputTypeResource(2L, "application_details"),
            new FormInputTypeResource(3L, "assessor_score")
    ), FormInputTypeResource::getTitle);

    @Before
    public void setUp() {
        super.setUp();

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupAssessment(PROCESS_ROLE_ID);
    }

    @Override
    protected AssessmentFeedbackController supplyControllerUnderTest() {
        return new AssessmentFeedbackController();
    }

    @Test
    public void testGetQuestion() throws Exception {
        Long expectedPreviousQuestionId = 10L;
        Long expectedNextQuestionId = 21L;
        CompetitionResource expectedCompetition = competitionResource;
        ApplicationResource expectedApplication = simpleToMap(applications, ApplicationResource::getId).get(APPLICATION_ID);

        List<FormInputResource> applicationFormInputs = this.setupApplicationFormInputs(QUESTION_ID, FORM_INPUT_TYPES.get("textarea"));
        this.setupApplicantResponses(APPLICATION_ID, applicationFormInputs);

        List<FormInputResource> assessmentFormInputs = this.setupAssessmentFormInputs(QUESTION_ID, FORM_INPUT_TYPES.get("textarea"), FORM_INPUT_TYPES.get("assessor_score"));
        List<AssessorFormInputResponseResource> assessorResponses = this.setupAssessorResponses(ASSESSMENT_ID, QUESTION_ID, assessmentFormInputs);

        Form expectedForm = new Form();
        expectedForm.setFormInput(simpleToMap(assessorResponses, assessorFormInputResponseResource -> String.valueOf(assessorFormInputResponseResource.getFormInput()), AssessorFormInputResponseResource::getValue));
        AssessmentNavigationViewModel expectedNavigation = new AssessmentNavigationViewModel(ASSESSMENT_ID, of(questionResources.get(expectedPreviousQuestionId)), of(questionResources.get(expectedNextQuestionId)));

        MvcResult result = mockMvc.perform(get("/{assessmentId}/question/{questionId}", ASSESSMENT_ID, QUESTION_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().attribute("navigation", expectedNavigation))
                .andExpect(view().name("assessment-question"))
                .andReturn();

        AssessmentFeedbackViewModel model = (AssessmentFeedbackViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(50, model.getDaysLeftPercentage());
        assertEquals(3, model.getDaysLeft());
        assertEquals(expectedCompetition, model.getCompetition());
        assertEquals(expectedApplication, model.getApplication());
        assertEquals(QUESTION_ID, model.getQuestionId());
        assertEquals("1", model.getQuestionNumber());
        assertEquals("Market opportunity", model.getQuestionShortName());
        assertEquals("1. What is the business opportunity that this project addresses?", model.getQuestionName());
        assertEquals(Integer.valueOf(50), model.getMaximumScore());
        assertEquals("Value 1", model.getApplicantResponse());
        assertEquals(assessmentFormInputs, model.getAssessmentFormInputs());
        assertEquals(simpleToMap(assessorResponses, AssessorFormInputResponseResource::getFormInput), model.getAssessorResponses());
        assertEquals("Value 1", model.getApplicantResponse());
        assertFalse(model.isAppendixExists());
        assertNull(model.getAppendixDetails());

        verify(assessmentService, only()).getById(ASSESSMENT_ID);
        verify(processRoleService, only()).getById(PROCESS_ROLE_ID);
        verify(applicationService, only()).getById(APPLICATION_ID);
        verify(competitionService, only()).getById(competitionResource.getId());
        verify(questionService, atLeast(1)).getById(same(QUESTION_ID));
        verify(formInputService, times(2)).findApplicationInputsByQuestion(QUESTION_ID);
        verify(formInputService, times(1)).findAssessmentInputsByQuestion(QUESTION_ID);
        applicationFormInputs.forEach(formInput -> verify(formInputResponseService, times(1)).getByFormInputIdAndApplication(formInput.getId(), APPLICATION_ID));
        verify(questionService, times(1)).getPreviousQuestion(QUESTION_ID);
        verify(questionService, times(1)).getNextQuestion(QUESTION_ID);
        verify(assessorFormInputResponseService, only()).getAllAssessorFormInputResponsesByAssessmentAndQuestion(ASSESSMENT_ID, QUESTION_ID);
    }

    @Test
    public void testGetQuestion_applicationDetailsQuestion() throws Exception {
        Long expectedNextQuestionId = 10L;
        CompetitionResource expectedCompetition = competitionResource;
        ApplicationResource expectedApplication = simpleToMap(applications, ApplicationResource::getId).get(APPLICATION_ID);
        AssessmentNavigationViewModel expectedNavigation = new AssessmentNavigationViewModel(ASSESSMENT_ID, empty(), of(questionResources.get(expectedNextQuestionId)));

        List<FormInputResource> applicationFormInputs = this.setupApplicationFormInputs(APPLICATION_DETAILS_QUESTION_ID, FORM_INPUT_TYPES.get("application_details"));
        this.setupApplicantResponses(APPLICATION_ID, applicationFormInputs);
        this.setupInvites();

        MvcResult result = mockMvc.perform(get("/{assessmentId}/question/{questionId}", ASSESSMENT_ID, APPLICATION_DETAILS_QUESTION_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(model().attribute("navigation", expectedNavigation))
                .andExpect(view().name("assessment-application-details"))
                .andReturn();

        AssessmentFeedbackApplicationDetailsViewModel model = (AssessmentFeedbackApplicationDetailsViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(50, model.getDaysLeftPercentage());
        assertEquals(3, model.getDaysLeft());
        assertEquals(expectedCompetition, model.getCompetition());
        assertEquals(expectedApplication, model.getApplication());
        assertEquals("Application details", model.getQuestionShortName());

        verify(assessmentService, only()).getById(ASSESSMENT_ID);
        verify(processRoleService, times(1)).getById(PROCESS_ROLE_ID);
        verify(applicationService, only()).getById(APPLICATION_ID);
        verify(competitionService, only()).getById(competitionResource.getId());
        verify(formInputService, only()).findApplicationInputsByQuestion(APPLICATION_DETAILS_QUESTION_ID);
        verify(formInputService, never()).findAssessmentInputsByQuestion(APPLICATION_DETAILS_QUESTION_ID);
        verify(formInputResponseService, never()).getByFormInputIdAndApplication(anyLong(), anyLong());
        verify(questionService, times(1)).getPreviousQuestion(APPLICATION_DETAILS_QUESTION_ID);
        verify(questionService, times(1)).getNextQuestion(APPLICATION_DETAILS_QUESTION_ID);
        verify(assessorFormInputResponseService, never()).getAllAssessorFormInputResponsesByAssessmentAndQuestion(anyLong(), anyLong());
    }

    @Test
    public void testUpdateFormInputResponse() throws Exception {
        String value = "Feedback";
        Long formInputId = 1L;
        when(assessorFormInputResponseService.updateFormInputResponse(ASSESSMENT_ID, formInputId, value)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/{assessmentId}/formInput/{formInputId}", ASSESSMENT_ID, formInputId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("formInputId", String.valueOf(formInputId))
                .param("value", value))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success", is("true")))
                .andReturn();

        verify(assessorFormInputResponseService, only()).updateFormInputResponse(ASSESSMENT_ID, formInputId, value);
    }

    @Test
    public void testSave() throws Exception {
        List<FormInputResource> formInputs = this.setupApplicationFormInputs(QUESTION_ID, FORM_INPUT_TYPES.get("assessor_score"), FORM_INPUT_TYPES.get("textarea"));

        Long formInputIdScore = formInputs.get(0).getId();
        Long formInputIdFeedback = formInputs.get(1).getId();
        Pair<String, String> scoreResponse = Pair.of(format("formInput[%s]", formInputIdScore), "10");
        Pair<String, String> feedbackResponse = Pair.of(format("formInput[%s]", formInputIdFeedback), "Feedback");

        mockMvc.perform(post("/{assessmentId}/question/{questionId}", ASSESSMENT_ID, QUESTION_ID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param(scoreResponse.getLeft(), scoreResponse.getRight())
                .param(feedbackResponse.getLeft(), feedbackResponse.getRight()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/" + ASSESSMENT_ID))
                .andReturn();

        InOrder inOrder = inOrder(assessorFormInputResponseService);
        inOrder.verify(assessorFormInputResponseService, calls(1)).updateFormInputResponse(ASSESSMENT_ID, formInputIdScore, "10");
        inOrder.verify(assessorFormInputResponseService, calls(1)).updateFormInputResponse(ASSESSMENT_ID, formInputIdFeedback, "Feedback");
    }

    @Override
    public void setupCompetition() {
        super.setupCompetition();

        competitionResource.setAssessmentStartDate(LocalDateTime.now().minusDays(2));
        competitionResource.setAssessmentEndDate(LocalDateTime.now().plusDays(4));

        questionResources.get(QUESTION_ID).setShortName("Market opportunity");
        questionResources.get(QUESTION_ID).setAssessorMaximumScore(50);
        questionResources.get(APPLICATION_DETAILS_QUESTION_ID).setShortName("Application details");
    }

    private List<FormInputResource> setupApplicationFormInputs(Long questionId, FormInputTypeResource... formInputTypes) {
        List<FormInputResource> formInputs = stream(formInputTypes).map(formInputType ->
                newFormInputResource()
                        .withFormInputType(formInputType.getId())
                        .withFormInputTypeTitle(formInputType.getTitle())
                        .build()
        ).collect(toList());
        when(formInputService.findApplicationInputsByQuestion(questionId)).thenReturn(formInputs);
        return formInputs;
    }

    private List<FormInputResource> setupAssessmentFormInputs(Long questionId, FormInputTypeResource... formInputTypes) {
        List<FormInputResource> formInputs = stream(formInputTypes).map(formInputType ->
                newFormInputResource()
                        .withFormInputType(formInputType.getId())
                        .withFormInputTypeTitle(formInputType.getTitle())
                        .build()
        ).collect(toList());
        when(formInputService.findAssessmentInputsByQuestion(questionId)).thenReturn(formInputs);
        return formInputs;
    }

    private List<FormInputResponseResource> setupApplicantResponses(Long applicationId, List<FormInputResource> formInputs) {
        List<FormInputResponseResource> applicantResponses = formInputs.stream().map(formInput ->
                newFormInputResponseResource()
                        .withFormInputs(formInput.getId())
                        .with(idBasedValues("Value "))
                        .build()
        ).collect(Collectors.toList());
        applicantResponses.forEach(formInputResponse -> when(formInputResponseService.getByFormInputIdAndApplication(formInputResponse.getFormInput(), applicationId)).thenReturn(restSuccess(asList(formInputResponse))));
        return applicantResponses;
    }

    private List<AssessorFormInputResponseResource> setupAssessorResponses(Long assessmentId, Long questionId, List<FormInputResource> formInputs) {
        List<AssessorFormInputResponseResource> assessorResponses = formInputs.stream().map(formInput ->
                newAssessorFormInputResponseResource()
                        .withFormInput(formInput.getId())
                        .withValue("Assessor Response")
                        .build()
        ).collect(toList());
        when(assessorFormInputResponseService.getAllAssessorFormInputResponsesByAssessmentAndQuestion(assessmentId, questionId)).thenReturn(assessorResponses);
        return assessorResponses;
    }

    private AssessmentResource setupAssessment(Long processRoleId) {
        AssessmentResource assessment = newAssessmentResource()
                .withId(1L)
                .withProcessRole(processRoleId)
                .build();
        when(assessmentService.getById(assessment.getId())).thenReturn(assessment);
        return assessment;
    }
}