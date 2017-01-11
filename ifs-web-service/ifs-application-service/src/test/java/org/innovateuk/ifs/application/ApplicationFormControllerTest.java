package org.innovateuk.ifs.application;

import org.hamcrest.*;
import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.builder.SectionResourceBuilder;
import org.innovateuk.ifs.application.model.*;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.Materials;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.application.service.Futures.settable;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.name;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.error.Error.globalError;
import static org.innovateuk.ifs.commons.rest.ValidationMessages.noErrors;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations="classpath:application.properties")
public class ApplicationFormControllerTest extends BaseControllerMockMVCTest<ApplicationFormController> {

    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Spy
    @InjectMocks
    private QuestionModelPopulator questionModelPopulator;

    @Spy
    @InjectMocks
    private OpenSectionModelPopulator openSectionModel;

    @Spy
    @InjectMocks
    private OpenFinanceSectionModelPopulator openFinanceSectionModel;

    @Mock
    private ApplicationModelPopulator applicationModelPopulator;

    @Mock
    private ApplicationSectionAndQuestionModelPopulator applicationSectionAndQuestionModelPopulator;

    @Mock
    private ApplicationNavigationPopulator applicationNavigationPopulator;

    @Mock
    private Model model;

    private ApplicationResource application;
    private Long sectionId;
    private Long questionId;
    private Long formInputId;
    private Long costId;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    @Override
    protected ApplicationFormController supplyControllerUnderTest() {
        return new ApplicationFormController();
    }

    @Before
    @Override
    public void setUp(){

        // Process mock annotations
        super.setUp();

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupUserRoles();
        this.setupFinances();
        this.setupInvites();
        this.setupQuestionStatus(applications.get(0));

        application = applications.get(0);
        sectionId = Long.valueOf(1);
        questionId = Long.valueOf(1);
        formInputId = Long.valueOf(111);
        costId = Long.valueOf(1);

        // save actions should always succeed.
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), eq(""), anyBoolean())).thenReturn(new ValidationMessages(fieldError("value", "", "Please enter some text 123")));
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), anyString(), anyBoolean())).thenReturn(noErrors());
    }

    @Test
    public void testApplicationFormWithOpenSection() throws Exception {

        Long currentSectionId = sectionResources.get(2).getId();

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        mockMvc.perform(get("/application/1/form/section/"+currentSectionId).header("referer", "/application/1"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", application))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("userIsLeadApplicant", true))
                .andExpect(model().attribute("leadApplicant", users.get(0)))
                .andExpect(model().attribute("currentSectionId", currentSectionId));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFormWithOpenSectionWhenTraveresedFromSummaryPage() throws Exception {

        Long currentSectionId = sectionResources.get(2).getId();

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        mockMvc.perform(get("/application/1/form/section/"+currentSectionId).header("referer", "/application/1/summary"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", application))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("userIsLeadApplicant", true))
                .andExpect(model().attribute("leadApplicant", users.get(0)))
                .andExpect(model().attribute("currentSectionId", currentSectionId));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFormWithOpenFinanceSection() throws Exception {

        Long currentSectionId = sectionResources.get(6).getId();

        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        mockMvc.perform(get("/application/1/form/section/"+currentSectionId))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", application))
                .andExpect(model().attribute("userIsLeadApplicant", true))
                .andExpect(model().attribute("leadApplicant", users.get(0)))
                .andExpect(model().attribute("currentSectionId", currentSectionId))
                .andExpect(model().attribute("hasFinanceSection", true))
                .andExpect(model().attribute("financeSectionId", currentSectionId))
                .andExpect(model().attribute("allReadOnly", true));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testQuestionPage() throws Exception {
        ApplicationResource application = applications.get(0);

        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        when(applicationService.getById(application.getId())).thenReturn(application);
        when(competitionService.getById(anyLong())).thenReturn(newCompetitionResource().withCompetitionStatus(CompetitionStatus.OPEN).build());
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        // just check if these pages are not throwing errors.
        mockMvc.perform(get("/application/1/form/question/10")).andExpect(status().isOk());
        mockMvc.perform(get("/application/1/form/question/21")).andExpect(status().isOk());
        mockMvc.perform(get("/application/1/form/section/1")).andExpect(status().isOk());
        mockMvc.perform(get("/application/1/form/section/2")).andExpect(status().isOk());
        mockMvc.perform(get("/application/1/form/question/edit/1")).andExpect(status().isOk());
        mockMvc.perform(get("/application/1/form/question/edit/21")).andExpect(status().isOk());
    }

    @Test
    public void testQuestionSubmit() throws Exception {
        ApplicationResource application = applications.get(0);

        when(applicationService.getById(application.getId())).thenReturn(application);
        mockMvc.perform(
                post("/application/1/form/question/1")
                .param("formInput[1]", "Some Value...")

        )
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testQuestionSubmitEdit() throws Exception {
        ApplicationResource application = applications.get(0);

        when(applicationService.getById(application.getId())).thenReturn(application);
        mockMvc.perform(
                post("/application/1/form/question/1")
                        .param(ApplicationFormController.EDIT_QUESTION, "1_2")
        )
                .andExpect(view().name("application-form"));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }


    @Test
    public void testQuestionSubmitAssign() throws Exception {
        ApplicationResource application = applications.get(0);

        when(applicationService.getById(application.getId())).thenReturn(application);
        mockMvc.perform(
                post("/application/1/form/question/1")
                    .param(ApplicationFormController.ASSIGN_QUESTION_PARAM, "1_2")

        )
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testQuestionSubmitMarkAsCompleteQuestion() throws Exception {
        ApplicationResource application = applications.get(0);

        when(applicationService.getById(application.getId())).thenReturn(application);
        mockMvc.perform(
                post("/application/1/form/question/1")
                        .param(ApplicationFormController.MARK_AS_COMPLETE, "1")
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void testQuestionSubmitSaveElement() throws Exception {
        ApplicationResource application = applications.get(0);

        when(applicationService.getById(application.getId())).thenReturn(application);

        mockMvc.perform(post("/application/1/form/question/1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testAddAnother() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("add_cost", String.valueOf(questionId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/" + application.getId() + "/form/section/" + sectionId));
    }

    @Test
    public void testAjaxAddCost() throws Exception {
        FinanceRowItem costItem = new Materials();
        when(defaultFinanceFormHandler.addCostWithoutPersisting(anyLong(), anyLong(), anyLong())).thenReturn(costItem);
        mockMvc.perform(
                get("/application/{applicationId}/form/add_cost/{questionId}", application.getId(), questionId)
        );
    }

    @Test
    public void testAjaxRemoveCost() throws Exception {
        ValidationMessages costItemMessages = new ValidationMessages();
        when(financeRowService.add(anyLong(),anyLong(), any())).thenReturn(costItemMessages);
        mockMvc.perform(
            get("/application/{applicationId}/form/remove_cost/{costId}", application.getId(), costId)
        );
    }

    @Test
    public void testApplicationFormSubmit() throws Exception {

        LocalDate futureDate = LocalDate.now().plusDays(1);

        MvcResult result =  mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("application.startDate", futureDate.format(FORMATTER))
                        .param("application.startDate.year", Integer.toString(futureDate.getYear()))
                        .param("application.startDate.dayOfMonth", Integer.toString(futureDate.getDayOfMonth()))
                        .param("application.startDate.monthValue", Integer.toString(futureDate.getMonthValue()))
                        .param("application.name", "New Application Title")
                        .param("application.durationInMonths", "12")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() +"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitMarkSectionComplete() throws Exception {

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(ApplicationFormController.MARK_SECTION_AS_COMPLETE, String.valueOf(sectionId))
                        .param(ApplicationFormController.TERMS_AGREED_KEY, "1")
                        .param(ApplicationFormController.STATE_AID_AGREED_KEY, "1")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() +"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void testApplicationFinanceMarkAsCompleteFailWithTerms() throws Exception {
        SectionResourceBuilder sectionResourceBuilder = SectionResourceBuilder.newSectionResource();
        when(sectionService.getById(anyLong())).thenReturn(sectionResourceBuilder.with(id(1L)).with(name("Your funding")).withType(SectionType.FUNDING_FINANCES).build());
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), "1")
                        .param(ApplicationFormController.MARK_SECTION_AS_COMPLETE, String.valueOf("1"))
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().attributeHasFieldErrors("form", ApplicationFormController.TERMS_AGREED_KEY));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFinanceMarkAsCompleteFailWithStateAid() throws Exception {
        SectionResourceBuilder sectionResourceBuilder = SectionResourceBuilder.newSectionResource();
        when(sectionService.getById(anyLong())).thenReturn(sectionResourceBuilder.with(id(1L)).with(name("Your project costs")).withType(SectionType.PROJECT_COST_FINANCES).build());
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), "1")
                        .param(ApplicationFormController.MARK_SECTION_AS_COMPLETE, String.valueOf("1"))
                        .param(ApplicationFormController.TERMS_AGREED_KEY, "1")
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().attributeHasFieldErrors("form", ApplicationFormController.STATE_AID_AGREED_KEY));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFormSubmitMarkSectionInComplete() throws Exception {

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(ApplicationFormController.MARK_SECTION_AS_INCOMPLETE, String.valueOf(sectionId))

        )
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() +"/form/section/**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void testApplicationFormSubmitMarkAsComplete() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(ApplicationFormController.MARK_AS_COMPLETE, "12")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId+"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void testApplicationFormSubmitMarkAsIncomplete() throws Exception {

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(ApplicationFormController.MARK_AS_INCOMPLETE, "3")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId +"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorsWithEmptyFields() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.name", "")
                        .param("application.resubmission", "")
                        .param("application.startDate", "")
                        .param("application.startDate.year", "")
                        .param("application.startDate.dayOfMonth", "")
                        .param("application.startDate.monthValue", "")
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");

        assertEquals("NotBlank", bindingResult.getFieldError("application.name").getCode());
        assertEquals("NotNull", bindingResult.getFieldError("application.durationInMonths").getCode());
        assertEquals("FutureLocalDate", bindingResult.getFieldError("application.startDate").getCode());
        assertEquals("NotNull", bindingResult.getFieldError("application.resubmission").getCode());

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorsWithResubmissionSelected() throws Exception {

        LocalDate yesterday = LocalDate.now().minusDays(1L);

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.resubmission", "1")
                        .param("application.previousApplicationNumber", "")
                        .param("application.previousApplicationTitle", "")
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");

        assertEquals("FieldRequiredIf", bindingResult.getFieldError("application.previousApplicationNumber").getCode());
        assertEquals("FieldRequiredIf", bindingResult.getFieldError("application.previousApplicationTitle").getCode());

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorsForPastDate() throws Exception {

        LocalDate yesterday = LocalDate.now().minusDays(1L);

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.startDate", "")
                        .param("application.startDate.year", String.valueOf(yesterday.getDayOfYear()))
                        .param("application.startDate.dayOfMonth", String.valueOf(yesterday.getDayOfMonth()))
                        .param("application.startDate.monthValue", String.valueOf(yesterday.getMonthValue()))
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");
        assertEquals("FutureLocalDate", bindingResult.getFieldError("application.startDate").getCode());
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorForTooFewMonths() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.durationInMonths", "0")
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");
        assertEquals("Min", bindingResult.getFieldError("application.durationInMonths").getCode());
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorForTooManyMonths() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.durationInMonths", "37")
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");
        assertEquals("Max", bindingResult.getFieldError("application.durationInMonths").getCode());
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationDetailsFormSubmitMarkAsComplete_returnsErrorsWithInvalidValues() throws Exception {

        LocalDate yesterday = LocalDate.now().minusDays(1L);

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/question/{questionId}", application.getId(), questionId)
                        .param("mark_as_complete", questionId.toString())
                        .param("application.resubmission", "0")
                        .param("application.previousApplicationNumber", "")
                        .param("application.previousApplicationTitle", "")
        ).andReturn();

        BindingResult bindingResult = (BindingResult)result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.form");

        assertNull(bindingResult.getFieldError("application.previousApplicationNumber"));
        assertNull(bindingResult.getFieldError("application.previousApplicationTitle"));
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFormSubmitGivesNoValidationErrorsIfNoQuestionIsEmptyOnSectionSubmit() throws Exception {
        Long userId = loggedInUser.getId();

        when(formInputResponseService.save(userId, application.getId(), 1L, "", false)).thenReturn(new ValidationMessages(globalError("Please enter some text")));
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection());
    }

    // See INFUND-1222 - not checking empty values on save now (only on mark as complete).
    @Test
    public void testApplicationFormSubmitGivesNoValidationErrorsIfQuestionIsEmptyOnSectionSubmit() throws Exception {
        Long userId = loggedInUser.getId();

        when(formInputResponseService.save(userId, application.getId(), 1L, "", false)).thenReturn(new ValidationMessages(globalError("Please enter some text")));
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param("formInput[2]", "Question 2 Response")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void testApplicationFormSubmitNotAllowedMarkAsComplete() throws Exception {
        // Question should not be marked as complete, since the input is not valid.

        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), eq(""), eq(false))).thenReturn(new ValidationMessages(globalError("please.enter.some.text")));
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param(ApplicationFormController.MARK_AS_COMPLETE, "1")
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().hasErrors());
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(HttpServletRequest.class), any(Model.class), any(SectionResource.class));
    }

    @Test
    public void testApplicationFormSubmitAssignQuestion() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("formInput[3]", "Question 3 Response")
                        .param("submit-section", "Save")
                        .param("assign_question", questionId + "_" + loggedInUser.getId())
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void testSaveFormElement() throws Exception {
        String value = "Form Input "+formInputId+" Response";

        mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", formInputId.toString())
                        .param("fieldName", "formInput["+formInputId+"]")
                        .param("value", value)
        ).andExpect(status().isOk());

        Mockito.inOrder(formInputResponseService).verify(formInputResponseService, calls(1)).save(loggedInUser.getId(), application.getId(), formInputId, value, false);
    }

    @Test
    public void testSaveFormElementApplicationTitle() throws Exception {
        String value = "New application title #216";
        String fieldName = "application.name";

        mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
        		.andExpect(content().json("{\"success\":\"true\"}"));

        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));
    }

    @Test
    public void testSaveFormElementEmptyApplicationTitle() throws Exception {
        String value = "";
        String fieldName = "application.name";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementSpacesApplicationTitle() throws Exception {
        String value = " ";
        String fieldName = "application.name";

        MvcResult result = mockMvc.perform(
                post("/application/"+application.getId().toString()+"/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        assertEquals(jsonExpectedContent, content);
    }

    @Test
     public void testSaveFormElementApplicationDuration() throws Exception {
        String value = "12";
        String fieldName = "application.durationInMonths";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));
    }

    @Test
    public void testSaveFormElementApplicationInvalidDurationNonInteger() throws Exception {
        String value = "aaaa";
        String fieldName = "application.durationInMonths";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\"}";
        assertEquals(jsonExpectedContent, content);
    }


    @Test
    public void testSaveFormElementApplicationInvalidDurationLength() throws Exception {
        String value = "37";
        String fieldName = "application.durationInMonths";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementCostSubcontracting() throws Exception {
        String value = "123";
        String questionId = "cost-subcontracting-13-subcontractingCost";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "subcontracting_costs-cost-13")
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementCostSubcontractingWithErrors() throws Exception {
        String value = "BOB";
        String questionId = "cost-subcontracting-13-subcontractingCost";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "bobbins")
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementFinancePosition() throws Exception {
        String value = "222";
        String questionId = "financePosition-organisationSize";
        String fieldName = "financePosition.organisationSize";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
    }



    @Test
    public void testSaveFormElementApplicationValidStartDateDDMMYYYY() throws Exception {
        String value = "25-10-2025";
        String questionId= "application_details-startdate";
        String fieldName = "application.startDate";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }


    @Test
    public void testSaveFormElementApplicationInvalidStartDateMMDDYYYY() throws Exception {
        String value = "10-25-2025";
        String questionId= "application_details-startdate";
        String fieldName = "application.startDate";

        mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        ).andExpect(status().isOk())
                .andExpect(content().json("{\"success\":\"false\"}"));
    }

    @Test
    public void testSaveFormElementApplicationStartDateValidDay() throws Exception {
        String value = "25";
        String questionId= "application_details-startdate_day";
        String fieldName = "application.startDate.dayOfMonth";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }

    //TODO: Change this to AutosaveElementException
    @Test
     public void testSaveFormElementApplicationAttributeInvalidDay() throws Exception {
        String questionId= "application_details-startdate_day";
        String fieldName = "application.startDate.dayOfMonth";
        String value = "35";

        mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        		.andExpect(content().json("{\"success\":\"false\"}"));
    }

    @Test
    public void testSaveFormElementApplicationAttributeInvalidMonth() throws Exception {
        String questionId= "application_details-startdate_month";
        String fieldName = "application.startDate.monthValue";
        String value = "13";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        log.info("Response : "+ content);

        String jsonExpectedContent = "{\"success\":\"false\"}";
        assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementApplicationAttributeInvalidYear() throws Exception {

        String questionId = "application_details-startdate_year";
        String fieldName  = "application.startDate.year";
        String value = "2015";

        when(sectionService.getById(anyLong())).thenReturn(null);

        mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().json("{\"success\":\"true\"}"));
    }

    @Test
    public void testSaveFormElementApplicationResubmission() throws Exception {
        String value = "true";
        String questionId= "application_details-resubmission";
        String fieldName = "application.resubmission";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }

    @Test
    public void testSaveFormElementApplicationPreviousApplicationNumber() throws Exception {
        String value = "999";
        String questionId= "application_details-previousapplicationnumber";
        String fieldName = "application.previousApplicationNumber";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }

    @Test
    public void testSaveFormElementApplicationPreviousApplicationTitle() throws Exception {
        String value = "test";
        String questionId= "application_details-previousapplicationtitle";
        String fieldName = "application.previousApplicationTitle";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/123/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }

    @Test
    public void testDeleteCost() throws Exception {
        String sectionId = "1";
        Long costId = 1L;

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("remove_cost", String.valueOf(costId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/"+application.getId()+"/form/section/" + sectionId));

        // verify that the method is called to send the data to the data services.
        //Mockito.inOrder(financeRowService).verify(financeRowService, calls(1)).delete(costId);
    }

//    @Test
//    public void testAssignQuestion() throws Exception {
//        Model model = new RequestModel;
//        applicationFormController.assignQuestion(model, application.getId(), sectionId);
//    }


    @Test
    public void testRedirectToSectionUnique() throws Exception {
        SectionResource financeSection = newSectionResource().withType(SectionType.FINANCE).build();
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList(financeSection));

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/"+application.getId()+"/form/section/" + financeSection.getId()));

    }

    @Test
    public void testRedirectToSectionNotUnique() throws Exception {
        SectionResource financeSection = newSectionResource().withType(SectionType.FINANCE).build();
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList(financeSection, newSectionResource().build()));

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/"+application.getId()));

    }

    @Test
    public void testRedirectToSectionMissing() throws Exception {
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList());

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/"+application.getId()));

    }



}
