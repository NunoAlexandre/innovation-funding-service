package org.innovateuk.ifs.application;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.time.ZonedDateTime;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentItemResourceBuilder.newPublicContentItemResource;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationCreationControllerTest extends BaseControllerMockMVCTest<ApplicationCreationController> {

    @Override
    protected ApplicationCreationController supplyControllerUnderTest() {
        return new ApplicationCreationController();
    }

    private String COMPANY_ID = "08241216";
    private String COMPANY_NAME = "NETWORTHNET LTD";
    private OrganisationResource organisationResource;
    private ApplicationResource applicationResource;

    @Before
    public void setUp() {
        super.setUp();
        setupCookieUtil();

        applicationResource = newApplicationResource().withId(6L).withName("some application").build();
        OrganisationSearchResult organisationSearchResult = new OrganisationSearchResult(COMPANY_ID, COMPANY_NAME);
        organisationResource = newOrganisationResource().withId(5L).withName(COMPANY_NAME).build();
        when(organisationService.getCompanyHouseOrganisation(COMPANY_ID)).thenReturn(organisationSearchResult);
        when(organisationService.save(any(OrganisationResource.class))).thenReturn(organisationResource);
        when(applicationService.createApplication(anyLong(), anyLong(), anyString())).thenReturn(applicationResource);
    }

    @Test
    public void checkEligibility() throws Exception {
        long competitionId = 1L;
        PublicContentItemResource publicContentItem = newPublicContentItemResource()
                .withCompetitionOpenDate(ZonedDateTime.now().minusDays(1))
                .withCompetitionCloseDate(ZonedDateTime.now().plusDays(1))
                .withNonIfs(false)
                .build();
        when(competitionService.getPublicContentOfCompetition(competitionId)).thenReturn(publicContentItem);

        MvcResult result = mockMvc.perform(get("/application/create/check-eligibility/{competitionId}", competitionId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("create-application/check-eligibility"))
                .andExpect(cookie().exists("competitionId"))
                .andExpect(cookie().value("invite_hash", ""))
                .andReturn();

        assertEquals(String.valueOf(competitionId), getDecryptedCookieValue(result.getResponse().getCookies(), "competitionId"));
    }


    @Test
    public void checkEligibility_nonIfs() throws Exception {
        long competitionId = 1L;
        PublicContentItemResource publicContentItem = newPublicContentItemResource()
                .withCompetitionOpenDate(ZonedDateTime.now().minusDays(1))
                .withCompetitionCloseDate(ZonedDateTime.now().plusDays(1))
                .withNonIfs(true)
                .build();
        when(competitionService.getPublicContentOfCompetition(competitionId)).thenReturn(publicContentItem);

        mockMvc.perform(get("/application/create/check-eligibility/{competitionId}", competitionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/search"));
    }


    @Test
    public void checkEligibility_early() throws Exception {
        long competitionId = 1L;
        PublicContentItemResource publicContentItem = newPublicContentItemResource()
                .withCompetitionOpenDate(ZonedDateTime.now().plusDays(1))
                .withCompetitionCloseDate(ZonedDateTime.now().plusDays(2))
                .withNonIfs(false)
                .build();
        when(competitionService.getPublicContentOfCompetition(competitionId)).thenReturn(publicContentItem);

        mockMvc.perform(get("/application/create/check-eligibility/{competitionId}", competitionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/search"));
    }

 @Test
    public void checkEligibility_late() throws Exception {
        long competitionId = 1L;
        PublicContentItemResource publicContentItem = newPublicContentItemResource()
                .withCompetitionOpenDate(ZonedDateTime.now().minusDays(2))
                .withCompetitionCloseDate(ZonedDateTime.now().minusDays(1))
                .withNonIfs(false)
                .build();
        when(competitionService.getPublicContentOfCompetition(competitionId)).thenReturn(publicContentItem);

        mockMvc.perform(get("/application/create/check-eligibility/{competitionId}", competitionId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/competition/search"));
    }


    @Test
    public void testInitializeApplication() throws Exception {
        mockMvc.perform(get("/application/create/initialize-application")
                .cookie(new Cookie(ApplicationCreationController.COMPETITION_ID, encryptor.encrypt("1")))
                .cookie(new Cookie(ApplicationCreationController.USER_ID, encryptor.encrypt("1")))
        )
                .andExpect(status().is3xxRedirection())
//                .andExpect(view().name("redirect:/application/" + applicationResource.getId()+"/contributors/invite?newApplication"));
                // TODO INFUND-936 temporary measure to redirect to login screen until email verification is in place
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void testYourDetails() throws Exception {
        mockMvc.perform(get("/application/create/your-details"))
                .andExpect(view().name("create-application/your-details"));
    }

    @Test
    public void testApplicationCreateWithoutApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");

        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();

        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        mockMvc.perform(post("/application/create/1").param("application_name", ""))
                .andExpect(view().name("application-create"))
                .andExpect(model().attribute("applicationNameEmpty", true));
    }

    @Test
    public void testApplicationCreateWithWhitespaceAsApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");

        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();

        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        mockMvc.perform(post("/application/create/1").param("application_name", "     "))
                .andExpect(view().name("application-create"))
                .andExpect(model().attribute("applicationNameEmpty", true));
    }

    @Test
    public void testApplicationCreateWithApplicationName() throws Exception {
        ApplicationResource application = new ApplicationResource();
        application.setName("application");
        application.setId(1L);

        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();

        when(userAuthenticationService.getAuthenticatedUser(anyObject())).thenReturn(user);
        when(applicationService.createApplication(eq(1L), eq(1L), anyString())).thenReturn(application);
        mockMvc.perform(post("/application/create/1").param("application_name", "testApplication"))
                .andExpect(view().name("redirect:/application/" + application.getId()))
                .andExpect(model().attributeDoesNotExist("applicationNameEmpty"));
    }

    @Test
    public void testApplicationCreateView() throws Exception {
        mockMvc.perform(get("/application/create/1"))
                .andExpect(view().name("application-create"));
    }
}
