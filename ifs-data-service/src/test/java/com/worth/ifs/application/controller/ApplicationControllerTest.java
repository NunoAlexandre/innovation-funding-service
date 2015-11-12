package com.worth.ifs.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.competition.builder.CompetitionBuilder;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.domain.ProcessRole;
import org.junit.Test;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.commons.service.BaseRestServiceProvider.getJSONHeaders;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationControllerTest extends BaseControllerMockMVCTest<ApplicationController> {

    @Override
    protected ApplicationController supplyControllerUnderTest() {
        return new ApplicationController();
    }

    @Test
    public void applicationControllerShouldReturnApplicationById() throws Exception {
        Application testApplication1 = new Application(null, "testApplication1Name", null, null, 1L);
        Application testApplication2 = new Application(null, "testApplication2Name", null, null, 2L);

        when(applicationRepositoryMock.findOne(1L)).thenReturn(testApplication1);
        when(applicationRepositoryMock.findOne(2L)).thenReturn(testApplication2);

        mockMvc.perform(get("/application/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testApplication1Name")))
                .andExpect(jsonPath("id", is(1)));
        mockMvc.perform(get("/application/id/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("testApplication2Name")))
                .andExpect(jsonPath("id", is(2)));
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
                .andExpect(jsonPath("[1]id", is(2)));
        mockMvc.perform(get("/application/findByUser/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]name", is("testApplication2Name")))
                .andExpect(jsonPath("[0]id", is(2)))
                .andExpect(jsonPath("[1]name", is("testApplication3Name")))
                .andExpect(jsonPath("[1]id", is(3)));
    }

    @Test
    public void applicationControllerShouldReturnAllApplications() throws Exception {

        List<Application> applications = new ArrayList<Application>();
        applications.add(new Application(null, "testApplication1Name", null, null, 1L));
        applications.add(new Application(null, "testApplication2Name", null, null, 2L));
        applications.add(new Application(null, "testApplication3Name", null, null, 3L));

        when(applicationRepositoryMock.findAll()).thenReturn(applications);
        mockMvc.perform(get("/application/findAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]name", is("testApplication1Name")))
                .andExpect(jsonPath("[0]id", is(1)))
                .andExpect(jsonPath("[1]name", is("testApplication2Name")))
                .andExpect(jsonPath("[1]id", is(2)))
                .andExpect(jsonPath("[2]name", is("testApplication3Name")))
                .andExpect(jsonPath("[2]id", is(3)));
    }

    @Test
    public void applicationControllerCanCreateApplication() throws Exception {
        Long competitionId = 1L;
        String userToken = "123abc";
        String applicationName = "testApplication";
        String roleName = "leadapplicant";
        Long organisationId = 1L;

        Application application = new Application();
        application.setName(applicationName);

        Competition competition = new Competition();
        Role role = new Role();
        role.setName(roleName);
        List<Role> roles = new ArrayList<Role>();
        roles.add(role);
        Organisation organisation = new Organisation();
        User user = new User();
        List<User> users = new ArrayList<User>();
        users.add(user);

        ObjectMapper mapper = new ObjectMapper();
        String applicationJsonString = mapper.writeValueAsString(application);

        when(competitionsRepositoryMock.findOne(competitionId)).thenReturn(competition);
        when(roleRepositoryMock.findByName(roleName)).thenReturn(roles);
        when(organisationRepositoryMock.findOne(organisationId)).thenReturn(organisation);
        when(userRepositoryMock.findByToken(userToken)).thenReturn(users);

        mockMvc.perform(post("/application/createApplicationByName/" + competitionId + "/" + userToken, "json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(applicationJsonString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processRoles[0]", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].user", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].organisation", notNullValue()))
                .andExpect(jsonPath("$.processRoles[0].role", notNullValue()));
    }
}