package com.worth.ifs.assessment.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.assessment.form.AssessmentAssignmentForm;
import com.worth.ifs.assessment.model.AssessmentAssignmentModelPopulator;
import com.worth.ifs.assessment.model.RejectAssessmentModelPopulator;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.assessment.viewmodel.RejectAssessmentViewModel;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.invite.resource.RejectionReasonResource;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.util.List;

import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.idBasedValues;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.commons.error.CommonFailureKeys.ASSESSMENT_REJECTION_FAILED;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.form.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static com.worth.ifs.invite.builder.RejectionReasonResourceBuilder.newRejectionReasonResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessmentAssignmentControllerTest extends BaseControllerMockMVCTest<AssessmentAssignmentController> {

    @Spy
    @InjectMocks
    private AssessmentAssignmentModelPopulator assessmentAssignmentModelPopulator;

    @Spy
    @InjectMocks
    private RejectAssessmentModelPopulator rejectAssessmentModelPopulator;

    @Mock
    private AssessmentService assessmentService;

    private List<RejectionReasonResource> rejectionReasons = newRejectionReasonResource()
            .withReason("Reason 1", "Reason 2")
            .build(2);

    private static final String restUrl = "/assign/application/";


    @Override
    protected AssessmentAssignmentController supplyControllerUnderTest() {
        return new AssessmentAssignmentController();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        when(rejectionReasonRestService.findAllActive()).thenReturn(restSuccess(rejectionReasons));
    }

    @Test
    public void viewAssessmentAssignment() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        Long formInput = 11L;
        Long competitionId = 3L;

        FormInputResponseResource applicantResponse =
                newFormInputResponseResource()
                        .withFormInputs(formInput)
                        .with(idBasedValues("Value "))
                        .build();

        when(assessmentService.getAssignableById(assessmentId)).thenReturn(newAssessmentResource().withApplication(applicationId).build());
        when(competitionService.getById(competitionId)).thenReturn(newCompetitionResource().build());
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());
        when(formInputResponseService.getByFormInputIdAndApplication(formInput, applicationId)).thenReturn(restSuccess(asList(applicantResponse)));

        mockMvc.perform(get("/{assessmentId}/assignment", assessmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment/assessment-invitation"));

    }

    @Test
    public void acceptAssignment() throws Exception {
        Long assessmentId = 1L;
        Long competitionId = 2L;

        when(assessmentService.acceptInvitation(assessmentId)).thenReturn(serviceSuccess());
        when(assessmentService.getById(assessmentId)).thenReturn(newAssessmentResource().withCompetition(competitionId).build());

        mockMvc.perform(post("/{assessmentId}/assignment/accept", assessmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard/competition/2"));
    }

    @Test
    public void rejectAssignment() throws Exception {
        Long assessmentId = 1L;
        Long competitionId = 2L;
        String reason = "reason";
        String comment = String.join(" ", nCopies(100, "comment"));

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withCompetition(competitionId)
                .build();

        when(assessmentService.getById(assessmentId)).thenReturn(assessment);
        when(assessmentService.rejectInvitation(assessmentId, reason, comment)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/{assessmentId}/assignment/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason)
                .param("rejectComment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessor/dashboard/competition/%s", competitionId)))
                .andReturn();

        verify(assessmentService, times(1)).getById(assessmentId);
        verify(assessmentService, times(1)).rejectInvitation(assessmentId, reason, comment);
        verifyNoMoreInteractions(assessmentService);
    }

    @Test
    public void rejectAssignment_eventNotAccepted() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        String reason = "reason";
        String comment = String.join(" ", nCopies(100, "comment"));

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withApplication(applicationId)
                .build();

        ApplicationResource application = newApplicationResource().build();

        when(assessmentService.getById(assessmentId)).thenReturn(assessment);
        when(applicationService.getById(applicationId)).thenReturn(application);
        when(assessmentService.rejectInvitation(assessmentId, reason, comment)).thenReturn(serviceFailure(ASSESSMENT_REJECTION_FAILED));

        // The non-js confirmation view should be returned with the fields pre-populated in the form and a global error

        AssessmentAssignmentForm expectedForm = new AssessmentAssignmentForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/assignment/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason)
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form"))
                .andExpect(view().name("assessment/assessment-reject-confirm"))
                .andReturn();

        RejectAssessmentViewModel model = (RejectAssessmentViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(application, model.getApplication());

        AssessmentAssignmentForm form = (AssessmentAssignmentForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(1, bindingResult.getGlobalErrorCount());
        assertEquals(0, bindingResult.getFieldErrorCount());
        assertEquals(ASSESSMENT_REJECTION_FAILED.name(), bindingResult.getGlobalError().getCode());

        InOrder inOrder = Mockito.inOrder(assessmentService, applicationService);
        inOrder.verify(assessmentService, calls(1)).getById(assessmentId);
        inOrder.verify(assessmentService, calls(1)).rejectInvitation(assessmentId, reason, comment);
        inOrder.verify(applicationService, calls(1)).getById(applicationId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectAssignment_noReason() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        String reason = "";
        String comment = String.join(" ", nCopies(100, "comment"));

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withApplication(applicationId)
                .build();

        ApplicationResource application = newApplicationResource().build();

        when(assessmentService.getById(assessmentId)).thenReturn(assessment);
        when(applicationService.getById(applicationId)).thenReturn(application);

        // The non-js confirmation view should be returned with the comment pre-populated in the form and an error for the missing reason

        AssessmentAssignmentForm expectedForm = new AssessmentAssignmentForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/assignment/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason)
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectReason"))
                .andExpect(view().name("assessment/assessment-reject-confirm"))
                .andReturn();

        RejectAssessmentViewModel model = (RejectAssessmentViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(application, model.getApplication());

        AssessmentAssignmentForm form = (AssessmentAssignmentForm) result.getModelAndView().getModel().get("form");

        assertEquals(reason, form.getRejectReason());
        assertEquals(comment, form.getRejectComment());

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectReason"));
        assertEquals("Please enter a reason", bindingResult.getFieldError("rejectReason").getDefaultMessage());

        InOrder inOrder = Mockito.inOrder(assessmentService, applicationService);
        inOrder.verify(assessmentService, calls(1)).getById(assessmentId);
        inOrder.verify(applicationService, calls(1)).getById(applicationId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectAssignment_exceedsCharacterSizeLimit() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        String reason = "reason";
        String comment = RandomStringUtils.random(5001);

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withApplication(applicationId)
                .build();

        ApplicationResource application = newApplicationResource().build();

        when(assessmentService.getById(assessmentId)).thenReturn(assessment);
        when(applicationService.getById(applicationId)).thenReturn(application);

        // The non-js confirmation view should be returned with the comment pre-populated in the form and an error for the missing reason

        AssessmentAssignmentForm expectedForm = new AssessmentAssignmentForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/assignment/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason)
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectComment"))
                .andExpect(view().name("assessment/assessment-reject-confirm"))
                .andReturn();

        RejectAssessmentViewModel model = (RejectAssessmentViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(application, model.getApplication());

        AssessmentAssignmentForm form = (AssessmentAssignmentForm) result.getModelAndView().getModel().get("form");

        assertEquals(reason, form.getRejectReason());
        assertEquals(comment, form.getRejectComment());

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectComment"));
        assertEquals("This field cannot contain more than {1} characters", bindingResult.getFieldError("rejectComment").getDefaultMessage());
        assertEquals(5000, bindingResult.getFieldError("rejectComment").getArguments()[1]);

        InOrder inOrder = Mockito.inOrder(assessmentService, applicationService);
        inOrder.verify(assessmentService, calls(1)).getById(assessmentId);
        inOrder.verify(applicationService, calls(1)).getById(applicationId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectAssignment_exceedsWordLimit() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        String reason = "reason";
        String comment = String.join(" ", nCopies(101, "comment"));

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withApplication(applicationId)
                .build();

        ApplicationResource application = newApplicationResource().build();

        when(assessmentService.getById(assessmentId)).thenReturn(assessment);
        when(applicationService.getById(applicationId)).thenReturn(application);

        // The non-js confirmation view should be returned with the comment pre-populated in the form and an error for the missing reason

        AssessmentAssignmentForm expectedForm = new AssessmentAssignmentForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/assignment/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason)
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectComment"))
                .andExpect(view().name("assessment/assessment-reject-confirm"))
                .andReturn();

        RejectAssessmentViewModel model = (RejectAssessmentViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        assertEquals(application, model.getApplication());

        AssessmentAssignmentForm form = (AssessmentAssignmentForm) result.getModelAndView().getModel().get("form");

        assertEquals(reason, form.getRejectReason());
        assertEquals(comment, form.getRejectComment());

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectComment"));
        assertEquals("Maximum word count exceeded. Please reduce your word count to {1}.", bindingResult.getFieldError("rejectComment").getDefaultMessage());
        assertEquals(100, bindingResult.getFieldError("rejectComment").getArguments()[1]);

        InOrder inOrder = Mockito.inOrder(assessmentService, applicationService);
        inOrder.verify(assessmentService, calls(1)).getById(assessmentId);
        inOrder.verify(applicationService, calls(1)).getById(applicationId);
        inOrder.verifyNoMoreInteractions();
    }


    @Test
    public void rejectAssignmentConfirm() throws Exception {
        Long assessmentId = 1L;
        Long applicationId = 2L;
        AssessmentAssignmentForm expectedForm = new AssessmentAssignmentForm();

        when(assessmentService.getById(assessmentId)).thenReturn(newAssessmentResource()
                .withId(assessmentId)
                .withApplication(applicationId).build());
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().build());

        MvcResult result = mockMvc.perform(get("/{assessmentId}/assignment/reject/confirm", assessmentId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(view().name("assessment/assessment-reject-confirm"))
                .andReturn();

        RejectAssessmentViewModel model = (RejectAssessmentViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(assessmentId, model.getAssessmentId());
        verify(assessmentService, times(1)).getById(assessmentId);
        verify(applicationService, times(1)).getById(applicationId);
    }
}
