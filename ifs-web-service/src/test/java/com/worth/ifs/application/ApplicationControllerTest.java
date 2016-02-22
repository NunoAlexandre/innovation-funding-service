package com.worth.ifs.application;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.user.domain.User;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.worth.ifs.application.service.Futures.settable;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationControllerTest extends BaseUnitTest {
    @InjectMocks
    private ApplicationController applicationController;

    @Before
    public void setUp(){
        super.setup();
        MockitoAnnotations.initMocks(this);


        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setViewResolvers(viewResolver())
                .setHandlerExceptionResolvers(createExceptionResolver())
                .build();


        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupFinances();
    }

    @Test
     public void testApplicationDetails() throws Exception {
        ApplicationResource app = applications.get(0);

       // when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        System.out.println("Show dashboard for application: " + app.getId());
        mockMvc.perform(get("/application/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("completedSections", Arrays.asList(1L, 2L)))
                .andExpect(model().attribute("currentCompetition", competitionService.getById(app.getCompetition())))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses));
    }

    @Test
    public void testApplicationSummary() throws Exception {
        ApplicationResource app = applications.get(0);
        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        mockMvc.perform(get("/application/" + app.getId()+"/summary"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-summary"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("currentCompetition",  competitionService.getById(app.getCompetition())))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses));
    }

    @Test
    public void testNotExistingApplicationDetails() throws Exception {
        ApplicationResource app = applications.get(0);

        when(applicationService.getById(app.getId())).thenReturn(app);
        when(applicationService.getById(1234l)).thenThrow(new ObjectNotFoundException("Application not found"));

        System.out.println("Show dashboard for application: " + app.getId());
        mockMvc.perform(get("/application/1234"))
                .andExpect(view().name("404"))
                .andExpect(model().attribute("url", "http://localhost/application/1234"))
                .andExpect(model().attribute("exception", new InstanceOf(ObjectNotFoundException.class)))
                .andExpect(model().attribute("message", "Application not found"))
                .andExpect(model().attributeExists("stacktrace"));
    }

    @Test
    public void testApplicationDetailsOpenSection() throws Exception {
        ApplicationResource app = applications.get(0);
        SectionResource section = sectionResources.get(2);



        Map<Long, SectionResource> collectedSections =
                sectionResources.stream().collect(Collectors.toMap(SectionResource::getId,
                        Function.identity()));

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        System.out.println("Show dashboard for application: " + app.getId());
        mockMvc.perform(get("/application/" + app.getId() +"/section/"+ section.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("application-details"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("currentCompetition", competitionService.getById(app.getCompetition())))
                .andExpect(model().attribute("sections", collectedSections))
                .andExpect(model().attribute("currentSectionId", section.getId()))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("responses", formInputsToFormInputResponses));
    }

    @Test
    public void testApplicationConfirmSubmit() throws Exception {
            ApplicationResource app = applications.get(0);

            //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
            when(applicationService.getById(app.getId())).thenReturn(app);
            when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

            mockMvc.perform(get("/application/1/confirm-submit"))
                    .andExpect(view().name("application-confirm-submit"))
                    .andExpect(model().attribute("currentApplication", app))
                    .andExpect(model().attribute("responses", formInputsToFormInputResponses));

    }

    @Test
    public void testApplicationSubmit() throws Exception {
        ApplicationResource app = applications.get(0);


        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        MvcResult result = mockMvc.perform(get("/application/1/submit"))
                .andExpect(view().name("application-submitted"))
                .andExpect(model().attribute("currentApplication", app))
                .andReturn();

        // TODO: test the application status, but how without having a database in place?
        //        Application updatedApplication = (Application) result.getModelAndView().getModel().get("currentApplication");
        //        String name = updatedApplication.getApplicationStatus().getName();
        //        assertEquals(name, "submitted");
    }

    @Test
    public void testApplicationCreateView() throws Exception {
        MvcResult result = mockMvc.perform(get("/application/create/1"))
                .andExpect(view().name("application-create"))
                .andReturn();
    }

    @Test
     public void testApplicationCreateWithoutApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");

        User user = new User(1L, "testname", null, null, null, null, null);


        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        MvcResult result = mockMvc.perform(post("/application/create/1").param("application_name", ""))
                .andExpect(view().name("application-create"))
                .andExpect(model().attribute("applicationNameEmpty", true))
                .andReturn();
    }

    @Test
    public void testApplicationCreateWithWhitespaceAsApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");

        User user = new User(1L, "testname", null, null, null, null, null);


        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        MvcResult result = mockMvc.perform(post("/application/create/1").param("application_name", "     "))
                .andExpect(view().name("application-create"))
                .andExpect(model().attribute("applicationNameEmpty", true))
                .andReturn();
    }

    @Test
    public void testApplicationCreateWithApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");
        application.setId(1L);

        User user = new User(1L, "testname", null, null, null, null, null);


        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        MvcResult result = mockMvc.perform(post("/application/create/1").param("application_name", "testApplication"))
                .andExpect(view().name("redirect:/application/"+application.getId()))
                .andExpect(model().attributeDoesNotExist("applicationNameEmpty"))
                .andReturn();
    }

    @Test
    public void testApplicationCreateConfirmCompetitionView() throws Exception {
        MvcResult result = mockMvc.perform(get("/application/create-confirm-competition"))
                .andExpect(view().name("application-create-confirm-competition"))
                .andReturn();
    }
}