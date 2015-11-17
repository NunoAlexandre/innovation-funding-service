package com.worth.ifs.application;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.finance.CostCategory;
import com.worth.ifs.application.finance.CostType;
import com.worth.ifs.exception.ErrorController;
import com.worth.ifs.security.CookieFlashMessageFilter;
import com.worth.ifs.user.domain.ProcessRole;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.EnumMap;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations="classpath:application.properties")
public class ApplicationFormControllerTest  extends BaseUnitTest {

    @InjectMocks
    private ApplicationFormController applicationFormController;

    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Mock
    private Model model;

    private Application application;
    private Long sectionId;
    private Long questionId;
    private Long formInputId;
    private Long costId;

    private static ResultMatcher matchUrl(final String expectedString) {
        return new ResultMatcher() {
            public void match(MvcResult result) {
                assertTrue(result.getResponse().getRedirectedUrl().equals(expectedString));
            }
        };
    }

    @Before
    public void setUp(){
        super.setup();

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(applicationFormController, new ErrorController())
//                .setHandlerExceptionResolvers(withExceptionControllerAdvice())
                .setViewResolvers(viewResolver())
                .addFilter(new CookieFlashMessageFilter())
                .build();

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupFinances();


        application = applications.get(0);
        sectionId = Long.valueOf(1);
        questionId = Long.valueOf(1);
        formInputId = Long.valueOf(111);
        costId = Long.valueOf(1);

        // save actions should always succeed.
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), anyString())).thenReturn(new ArrayList<>());
    }

    @Test
    public void testApplicationForm() throws Exception {
        com.worth.ifs.application.domain.Application app = applications.get(0);
        ProcessRole userAppRole = new ProcessRole();

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(processRoleService.findProcessRole(loggedInUser.getId(), app.getId())).thenReturn(userAppRole);

        mockMvc.perform(get("/application-form/1"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("currentSectionId", 1L));

    }

    @Test
    public void testApplicationFormWithOpenSection() throws Exception {
        EnumMap<CostType, CostCategory> costCategories = new EnumMap<>(CostType.class);

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(application.getId())).thenReturn(application);

        mockMvc.perform(get("/application-form/1/section/1"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", application))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("userIsLeadApplicant", true))
                .andExpect(model().attribute("currentSectionId", 1L));

    }

    @Test
    public void costControllerShouldRedirectToCorrectLocationAfterCostDelete() throws Exception {

        doNothing().when(costService).delete(costId);

        mockMvc.perform(get("/application-form/deletecost/" + application.getId() + "/" + sectionId + "/0"))
//                .andExpect(matchUrl("/application-form/10/section/20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application-form/" + application.getId() + "/section/" + sectionId));;
    }

    @Test
    public void testAddAnother() throws Exception {
        mockMvc.perform(
                get(
                        "/application-form/addcost/{applicationId}/{sectionId}/{questionId}",
                        application.getId(),
                        sectionId,
                        questionId
                )
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application-form/"+application.getId()+"/section/" + sectionId));

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(financeService).verify(financeService, calls(1)).addCost(applicationFinance.getId(), questionId);
    }

    @Test
    public void testApplicationFormSubmit() throws Exception {
        Long userId = loggedInUser.getId();

        MvcResult result = mockMvc.perform(
                post("/application-form/{applicationId}/section/{sectionId}", application.getId(), sectionId)
                        .param("question[1]", "Question 1 Response")
                        .param("question[2]", "Question 2 Response")
                        .param("question[3]", "Question 3 Response")
                        .param("question[application_details-startdate][year]", "2015")
                        .param("question[application_details-startdate][day]", "15")
                        .param("question[application_details-startdate][month]", "11")
                        .param("question[application_details-title]", "New Application Title")
                        .param("question[application_details-duration]", "12")
                        .param("mark_as_complete", "12")
                        .param("mark_as_incomplete", "13")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application-form/" + application.getId() + "/section/" + sectionId + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
//                .andExpect(cookie().value(CookieFlashMessageFilter.COOKIE_NAME, "applicationSaved"))
                .andReturn();
    }
    @Test
    public void testApplicationFormSubmitAssignQuestion() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/application-form/{applicationId}/section/{sectionId}", application.getId(), sectionId)
                        .param("question[1]", "Question 1 Response")
                        .param("question[2]", "Question 2 Response")
                        .param("question[3]", "Question 3 Response")
                        .param("submit-section", "Save")
                        .param("assign_question", questionId + "_" + loggedInUser.getId())
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application-form/" + application.getId() + "/section/" + sectionId + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
//                .andExpect(cookie().value(CookieFlashMessageFilter.COOKIE_NAME, "assignedQuestion"))
                .andReturn();
    }



    @Test
    public void testSaveFormElement() throws Exception {
        String value = "Form Input "+formInputId+" Response";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", formInputId.toString())
                        .param("fieldName", "formInput["+formInputId+"]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
        ).andExpect(status().isOk())
                .andReturn();

        Mockito.inOrder(formInputResponseService).verify(formInputResponseService, calls(1)).save(loggedInUser.getId(), application.getId(), formInputId, value);
    }

    @Test
    public void testSaveFormElementApplicationTitle() throws Exception {
        String value = "New application title #216";
        String questionId = "application_details-title";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "formInput["+questionId+"]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(content, jsonExpectedContent);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(Application.class));

    }

    @Test
     public void testSaveFormElementApplicationDuration() throws Exception {
        String value = "123";
        String questionId = "application_details-duration";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "formInput["+questionId+"]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(content, jsonExpectedContent);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(Application.class));
    }

    @Test
    public void testSaveFormElementApplicationInvalidDuration() throws Exception {
        String value = "aaaa";
        String questionId = "application_details-duration";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "formInput[" + questionId + "]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid value.\"]}";
        Assert.assertEquals(content, jsonExpectedContent);
    }

    @Test
    public void testSaveFormElementCostSubcontracting() throws Exception {
        String value = "123";
        String questionId = "cost-subcontracting-13-subcontractingCost";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "subcontracting_costs-cost-13")
                        .param("value", value)
                        .param("applicationId", "1")
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(content, jsonExpectedContent);
    }

    @Test
    public void testSaveFormElementApplicationStartDate() throws Exception {
        String value = "22";
        String questionId = "application_details-startdate_day";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "formInput["+questionId+"]")
                        .param("value", value)
                        .param("applicationId", "1")
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(content, jsonExpectedContent);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(Application.class));

    }

    //TODO: Change this to AutosaveElementException
//    (expected = NestedServletException.class)
    @Test
     public void testSaveFormElementApplicationAttributeInvalidDay() throws Exception {

        String questionId = "application_details-startdate_day";
        Long userId = loggedInUser.getId();
        String value = "35";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "formInput[" + questionId + "]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid date.\"]}";
        Assert.assertEquals(content, jsonExpectedContent);
    }

    @Test
    public void testSaveFormElementApplicationAttributeInvalidMonth() throws Exception {

        String questionId = "application_details-startdate_month";
        Long userId = loggedInUser.getId();
        String value = "13";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "question[" + questionId + "]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        log.info("Response : "+ content);

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid date.\"]}";
        Assert.assertEquals(content, jsonExpectedContent);
    }

    @Test
    public void testSaveFormElementApplicationAttributeValidYear() throws Exception {

        String questionId = "application_details-startdate_year";
        Long userId = loggedInUser.getId();
        String value = "2015";

        MvcResult result = mockMvc.perform(
                post("/application-form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "question[" + questionId + "]")
                        .param("value", value)
                        .param("applicationId", application.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testDeleteCost() throws Exception {
        String sectionId = "1";
        Long costId = 1L;

        mockMvc.perform(
                get(
                        "/application-form/deletecost/{applicationId}/{sectionId}/{costId}",
                        application.getId(),
                        sectionId,
                        costId
                )
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application-form/"+application.getId()+"/section/" + sectionId));

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(costService).verify(costService, calls(1)).delete(costId);
    }

    @Test
    public void testDeleteCostWithFragmentResponse() throws Exception {
        mockMvc.perform(
                get(
                        "/application-form/deletecost/{applicationId}/{sectionId}/{costId}/{renderQuestionId}",
                        application.getId(),
                        sectionId,
                        costId,
                        questionId
                ).param("singleFragment", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk());

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(costService).verify(costService, calls(1)).delete(costId);
    }

    @Test
    public void testDeleteCostWithFragmentResponseNullValue() throws Exception {
        mockMvc.perform(
                get(
                        "/application-form/deletecost/{applicationId}/{sectionId}/{costId}/{renderQuestionId}",
                        application.getId(),
                        sectionId,
                        null,
                        questionId
                ).param("singleFragment", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is3xxRedirection());
        //  TODO: also test if there is an error thrown when using null value, or negative number.
    }

    @Test
    public void testAddAnotherWithFragmentResponse() throws Exception {
        mockMvc.perform(
                get(
                        "/application-form/addcost/{applicationId}/{sectionId}/{costId}/{renderQuestionId}",
                        application.getId(),
                        sectionId,
                        costId,
                        questionId
                ).param("singleFragment", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk()).andExpect(view().name("single-question"));
    }

//    @Test
//    public void testAssignQuestion() throws Exception {
//        Model model = new RequestModel;
//        applicationFormController.assignQuestion(model, application.getId(), sectionId);
//    }

}