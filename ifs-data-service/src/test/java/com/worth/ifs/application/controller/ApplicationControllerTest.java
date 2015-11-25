package com.worth.ifs.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.resourceAssembler.ApplicationResourceAssembler;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class ApplicationControllerTest extends BaseControllerMockMVCTest<ApplicationController> {

    private final Log log = LogFactory.getLog(getClass());

    @Mock
    private ApplicationResourceAssembler applicationResourceAssembler;

    private MockHttpServletRequest request;

    @Before
    public void setUpForHateoas() {
        request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Override
    protected ApplicationController supplyControllerUnderTest() {
        ApplicationController applicationController = new ApplicationController(new ApplicationResourceAssembler());
        return applicationController;
    }

    @Test
    public void applicationControllerShouldReturnApplicationById() throws Exception {
        Application testApplication1 = new Application(null, "testApplication1Name", null, null, 1L);
        Application testApplication2 = new Application(null, "testApplication2Name", null, null, 2L);

        when(applicationRepositoryMock.findOne(1L)).thenReturn(testApplication1);
        when(applicationRepositoryMock.findOne(2L)).thenReturn(testApplication2);

        mockMvc.perform(get("/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testApplication1Name")))
                .andDo(document("application/get-application",
                        responseFields(
                                fieldWithPath("name").description("Name of the application"),
                                fieldWithPath("startDate").description("Estimated timescales: project start date"),
                                fieldWithPath("durationInMonths").description("Estimated timescales: project duration in months"),
                                fieldWithPath("links").description("HATEOAS style links"),
                                fieldWithPath("applicationStatus").description("Application Status Id"),
                                fieldWithPath("competition").description("Competition"))));
        mockMvc.perform(get("/application/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("testApplication2Name")));
    }

    @Test
    public void applicationControllerShouldReturnApplicationByUserId() throws Exception {
        User testUser2 = new User(2L, "testUser2",  "email2@email.nl", "password", "test/image/url/2", "testToken456def", null);
        User testUser1 = new User(1L, "testUser1",  "email1@email.nl", "password", "test/image/url/1", "testToken123abc", null);

        Application testApplication1 = new Application(null, "testApplication1Name", null, null, 1L);
        Application testApplication2 = new Application(null, "testApplication2Name", null, null, 2L);
        Application testApplication3 = new Application(null, "testApplication3Name", null, null, 3L);

        Organisation organisation1 = new Organisation(1L, "test organisation 1");
        Organisation organisation2 = new Organisation(2L, "test organisation 2");

        ProcessRole testProcessRole1 = new ProcessRole(0L, testUser1, testApplication1, new Role(), organisation1);
        ProcessRole testProcessRole2 = new ProcessRole(1L, testUser1, testApplication2, new Role(), organisation1);
        ProcessRole testProcessRole3 = new ProcessRole(2L, testUser2, testApplication2, new Role(), organisation2);
        ProcessRole testProcessRole4 = new ProcessRole(3L, testUser2, testApplication3, new Role(), organisation2);

        when(userRepositoryMock.findOne(1L)).thenReturn(testUser1);
        when(userRepositoryMock.findOne(2L)).thenReturn(testUser2);

        when(processRoleRepositoryMock.findByUser(testUser1)).thenReturn(new ArrayList<ProcessRole>() {{
            add(testProcessRole1);
            add(testProcessRole2);
        }});

        when(processRoleRepositoryMock.findByUser(testUser2)).thenReturn(new ArrayList<ProcessRole>() {{
            add(testProcessRole3);
            add(testProcessRole4);
        }});

        mockMvc.perform(get("/application/findByUser/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]name", is("testApplication1Name")))
                .andExpect(jsonPath("[0]id", is(1)))
                .andExpect(jsonPath("[1]name", is("testApplication2Name")))
                .andExpect(jsonPath("[1]id", is(2)))
                .andDo(document("application/find-user-applications"));
        mockMvc.perform(get("/application/findByUser/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]name", is("testApplication2Name")))
                .andExpect(jsonPath("[0]id", is(2)))
                .andExpect(jsonPath("[1]name", is("testApplication3Name")))
                .andExpect(jsonPath("[1]id", is(3)));
    }

    @Test
    public void applicationControllerShouldReturnAllApplications() throws Exception {

        List<Application> applications = newApplication().build(3);
        when(applicationRepositoryMock.findAll()).thenReturn(applications);

        String expected = new ObjectMapper().writeValueAsString(new ApplicationResourceAssembler().toEmbeddedList(applications));
        //ugly hack to ensure correct URI's in response
        expected = expected.replaceAll("localhost", "localhost:8090");

        mockMvc.perform(get("/application/").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is(expected)))
                .andDo(document("application/find-all-applications"));
    }

    @Test
    public void applicationControllerCanCreateApplication() throws Exception {
        Long competitionId = 1L;
        Long userId = 1L;
        String applicationName = "testApplication";
        String roleName = "leadapplicant";

        Application application = new Application();
        application.setName(applicationName);

        Competition competition = new Competition();
        Role role = new Role();
        role.setName(roleName);
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        Organisation organisation = new Organisation(1L , "testOrganisation");
        User user = new User();

        ProcessRole processRole = new ProcessRole(user, null, role, organisation);
        List<ProcessRole> processRoles = new ArrayList<>();
        processRoles.add(processRole);
        user.addUserApplicationRole(processRole);

        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setName(ApplicationStatusConstants.CREATED.getName());
        List<ApplicationStatus> applicationStatuses = new ArrayList<>();
        applicationStatuses.add(applicationStatus);

        ObjectMapper mapper = new ObjectMapper();
        String applicationJsonString = mapper.writeValueAsString(application);


        when(applicationStatusRepositoryMock.findByName(ApplicationStatusConstants.CREATED.getName())).thenReturn(applicationStatuses);
        when(competitionsRepositoryMock.findOne(competitionId)).thenReturn(competition);
        when(roleRepositoryMock.findByName(roleName)).thenReturn(roles);
        when(userRepositoryMock.findOne(userId)).thenReturn(user);

        mockMvc.perform(put("/application/createApplicationByName/" + competitionId + "/" + userId, "json")
                .contentType(APPLICATION_JSON)
                .content(applicationJsonString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processRoles[0]", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].user", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].organisation", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].role", notNullValue()))
                .andDo(document("application/create-application"));
    }
}