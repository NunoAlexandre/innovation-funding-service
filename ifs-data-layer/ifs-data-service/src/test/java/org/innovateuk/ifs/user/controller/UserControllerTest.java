package org.innovateuk.ifs.user.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.registration.resource.InternalUserRegistrationResource;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserPageResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.transactional.CrmService;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.core.Is.is;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.USERS_EMAIL_VERIFICATION_TOKEN_EXPIRED;
import static org.innovateuk.ifs.commons.service.BaseRestService.buildPaginationUri;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.registration.builder.InternalUserRegistrationResourceBuilder.newInternalUserRegistrationResource;
import static org.innovateuk.ifs.token.resource.TokenType.VERIFY_EMAIL_ADDRESS;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Title.Mr;
import static org.innovateuk.ifs.user.resource.UserRelatedURLs.URL_PASSWORD_RESET;
import static org.innovateuk.ifs.user.resource.UserRelatedURLs.URL_VERIFY_EMAIL;
import static org.innovateuk.ifs.user.resource.UserStatus.INACTIVE;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends BaseControllerMockMVCTest<UserController> {

    @Override
    protected UserController supplyControllerUnderTest() {
        return new UserController();
    }

    @Mock
    private CrmService crmService;

    @Test
    public void resendEmailVerificationNotification() throws Exception {
        final String emailAddress = "sample@me.com";

        final UserResource userResource = newUserResource().build();

        when(userServiceMock.findInactiveByEmail(emailAddress)).thenReturn(serviceSuccess(userResource));
        when(registrationServiceMock.resendUserVerificationEmail(userResource)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/user/resendEmailVerificationNotification/{emailAddress}/", emailAddress))
                .andExpect(status().isOk());

        verify(registrationServiceMock, only()).resendUserVerificationEmail(userResource);
    }

    @Test
    public void resendEmailVerificationNotification_notFound() throws Exception {
        final String emailAddress = "sample@me.com";

        when(userServiceMock.findInactiveByEmail(emailAddress)).thenReturn(serviceFailure(notFoundError(User.class, emailAddress, INACTIVE)));

        mockMvc.perform(put("/user/resendEmailVerificationNotification/{emailAddress}/", emailAddress))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUser() throws Exception {
        final Long organisationId = 9999L;

        final UserResource userResource = newUserResource().build();
        when(registrationServiceMock.createOrganisationUser(organisationId, userResource)).thenReturn(serviceSuccess(userResource));
        when(registrationServiceMock.sendUserVerificationEmail(userResource, empty())).thenReturn(serviceSuccess());

        mockMvc.perform(post("/user/createLeadApplicantForOrganisation/{organisationId}", organisationId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userResource)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(userResource)));

        verify(registrationServiceMock, times(1)).createOrganisationUser(organisationId, userResource);
        verify(registrationServiceMock, times(1)).sendUserVerificationEmail(userResource, empty());
        verifyNoMoreInteractions(registrationServiceMock);
    }

    @Test
    public void createUserWithCompetitionId() throws Exception {
        final Long organisationId = 9999L;
        final Long competitionId = 8888L;

        final UserResource userResource = newUserResource().build();
        when(registrationServiceMock.createOrganisationUser(organisationId, userResource)).thenReturn(serviceSuccess(userResource));
        when(registrationServiceMock.sendUserVerificationEmail(userResource, of(competitionId))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/user/createLeadApplicantForOrganisation/{organisationId}/{competitionId}", organisationId, competitionId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userResource)))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(userResource)));

        verify(registrationServiceMock, times(1)).createOrganisationUser(organisationId, userResource);
        verify(registrationServiceMock, times(1)).sendUserVerificationEmail(userResource, of(competitionId));
        verifyNoMoreInteractions(registrationServiceMock);
    }

    @Test
    public void userControllerShouldReturnAllUsers() throws Exception {
        UserResource testUser1 = newUserResource().withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();
        UserResource testUser2 = newUserResource().withId(2L).withFirstName("test").withLastName("User2").withEmail("email2@email.nl").build();
        UserResource testUser3 = newUserResource().withId(3L).withFirstName("test").withLastName("User3").withEmail("email3@email.nl").build();

        List<UserResource> users = new ArrayList<>();
        users.add(testUser1);
        users.add(testUser2);
        users.add(testUser3);

        when(baseUserServiceMock.findAll()).thenReturn(serviceSuccess(users));
        mockMvc.perform(get("/user/findAll/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("[0]firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("[0]lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("[0]imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("[0]uid", is(testUser1.getUid())))
                .andExpect(jsonPath("[1]id", is((Number) testUser2.getId().intValue())))
                .andExpect(jsonPath("[1]firstName", is(testUser2.getFirstName())))
                .andExpect(jsonPath("[1]lastName", is(testUser2.getLastName())))
                .andExpect(jsonPath("[1]imageUrl", is(testUser2.getImageUrl())))
                .andExpect(jsonPath("[1]uid", is(testUser2.getUid())))
                .andExpect(jsonPath("[2]id", is((Number) testUser3.getId().intValue())))
                .andExpect(jsonPath("[2]firstName", is(testUser3.getFirstName())))
                .andExpect(jsonPath("[2]lastName", is(testUser3.getLastName())))
                .andExpect(jsonPath("[2]imageUrl", is(testUser3.getImageUrl())))
                .andExpect(jsonPath("[2]uid", is(testUser3.getUid())))
                .andDo(document("user/get-all-users"));
    }

    @Test
    public void userControllerShouldReturnUserById() throws Exception {
        UserResource testUser1 = newUserResource().withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();

        when(baseUserServiceMock.getUserById(testUser1.getId())).thenReturn(serviceSuccess(testUser1));
        mockMvc.perform(get("/user/id/" + testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("uid", is(testUser1.getUid())))
                .andDo(document("user/get-user"));
    }

    @Test
    public void updatePassword() throws Exception {
        final String password = "Passw0rd";
        final String hash = "bf5b6392-1e08-4acc-b667-f0a16d6744de";
        when(userServiceMock.changePassword(hash, password)).thenReturn(serviceSuccess(null));
        mockMvc.perform(post("/user/" + URL_PASSWORD_RESET + "/{hash}", hash).content(password))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("user/update-password",
                        pathParameters(
                                parameterWithName("hash").description("The hash to validate the legitimacy of the request")
                        )
                ));
    }

    @Test
    public void verifyEmail() throws Exception {
        final String hash = "8eda60ad3441ee883cc95417e2abaa036c308dd9eb19468fcc8597fb4cb167c32a7e5daf5e237385";
        final Long userId = 1L;
        final Token token = new Token(VERIFY_EMAIL_ADDRESS, User.class.getName(), userId, hash, now(), null);
        when(tokenServiceMock.getEmailToken(hash)).thenReturn(serviceSuccess((token)));
        when(registrationServiceMock.activateApplicantAndSendDiversitySurvey(1L)).thenReturn(serviceSuccess());
        mockMvc.perform(get("/user/" + URL_VERIFY_EMAIL + "/{hash}", hash))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("user/verify-email",
                        pathParameters(
                                parameterWithName("hash").description("The hash to validate the legitimacy of the request")
                        ))
                );

        verify(crmService).syncCrmContact(userId);
    }


    @Test
    public void verifyEmailNotFound() throws Exception {
        final String hash = "5f415b7ec9e9cc497996e251294b1d6bccfebba8dfc708d87b52f1420c19507ab24683bd7e8f49a0";
        final Error error = notFoundError(Token.class, hash);
        when(tokenServiceMock.getEmailToken(hash)).thenReturn(serviceFailure(error));
        mockMvc.perform(get("/user/" + URL_VERIFY_EMAIL + "/{hash}", hash))
                .andExpect(status().isNotFound())
                .andExpect(contentError(error));
    }

    @Test
    public void verifyEmailExpired() throws Exception {
        final String hash = "5f415b7ec9e9cc497996e251294b1d6bccfebba8dfc708d87b52f1420c19507ab24683bd7e8f49a0";
        final Error error = new Error(USERS_EMAIL_VERIFICATION_TOKEN_EXPIRED);
        when(tokenServiceMock.getEmailToken(hash)).thenReturn(serviceFailure(error));
        mockMvc.perform(get("/user/" + URL_VERIFY_EMAIL + "/{hash}", hash))
                .andExpect(status().isBadRequest())
                .andExpect(contentError(error));
    }

    @Test
    public void updatePasswordTokenNotFound() throws Exception {
        final String password = "Passw0rd";
        final String hash = "bf5b6392-1e08-4acc-b667-f0a16d6744de";
        final Error error = notFoundError(Token.class, hash);
        when(userServiceMock.changePassword(hash, password)).thenReturn(serviceFailure(error));
        mockMvc.perform(post("/user/" + URL_PASSWORD_RESET + "/" + hash).content(password))
                .andExpect(status().isNotFound())
                .andExpect(contentError(error))
                .andDo(document("user/update-password-token-not-found"));
    }

    @Test
    public void userControllerShouldReturnUserByUid() throws Exception {
        UserResource testUser1 = newUserResource().withUID("aebr34-ab345g-234gae-agewg").withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();

        when(baseUserServiceMock.getUserResourceByUid(testUser1.getUid())).thenReturn(serviceSuccess(testUser1));

        mockMvc.perform(get("/user/uid/" + testUser1.getUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("uid", is(testUser1.getUid())))
                .andDo(document("user/get-user-by-token"));
    }

    @Test
    public void userControllerShouldReturnListOfSingleUserWhenFoundByEmail() throws Exception {
        User user = new User();
        user.setEmail("testemail@email.email");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setPhoneNumber("testPhoneNumber");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setTitle(Mr);

        when(userServiceMock.findByEmail(user.getEmail())).thenReturn(serviceFailure(notFoundError(User.class, user.getEmail())));

        mockMvc.perform(get("/user/findByEmail/" + user.getEmail() + "/", "json")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userControllerShouldReturnEmptyListWhenNoUserIsFoundByEmail() throws Exception {

        String email = "testemail@email.com";

        when(userServiceMock.findByEmail(email)).thenReturn(serviceFailure(notFoundError(User.class, email)));

        mockMvc.perform(get("/user/findByEmail/" + email + "/", "json")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindActiveInternalUsers() throws Exception {
        when(userServiceMock.findActiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(0, 5))).thenReturn(serviceSuccess(new UserPageResource()));
        mockMvc.perform(get(buildPaginationUri("/user/internal/active", 0, 5, null, new LinkedMultiValueMap<>()))).andExpect(status().isOk());
    }

    @Test
    public void testFindInactiveInternalUsers() throws Exception {
        when(userServiceMock.findInactiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(0, 5))).thenReturn(serviceSuccess(new UserPageResource()));
        mockMvc.perform(get(buildPaginationUri("/user/internal/inactive", 0, 5, null, new LinkedMultiValueMap<>()))).andExpect(status().isOk());
    }

    @Test
    public void testCreateInternalUser() throws Exception {
        List<RoleResource> roleResources = newRoleResource().withType(UserRoleType.PROJECT_FINANCE).build(1);
        InternalUserRegistrationResource internalUserRegistrationResource = newInternalUserRegistrationResource()
                .withFirstName("First")
                .withLastName("Last")
                .withEmail("email@example.com")
                .withPassword("Passw0rd123")
                .withRoles(roleResources)
                .build();

        when(registrationServiceMock.createInternalUser("SomeHashString", internalUserRegistrationResource)).thenReturn(serviceSuccess());
        mockMvc.perform(
                post("/user/internal/create/SomeHashString")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(internalUserRegistrationResource))
        ).andExpect(status().isCreated());
    }
}
