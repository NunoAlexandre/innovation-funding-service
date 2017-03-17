package org.innovateuk.ifs.user.transactional;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.authentication.service.RestIdentityProviderService;
import org.innovateuk.ifs.commons.error.CommonErrors;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationSource;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.token.resource.TokenType;
import org.innovateuk.ifs.user.domain.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.innovateuk.ifs.LambdaMatcher.createLambdaMatcher;
import static org.innovateuk.ifs.address.builder.AddressBuilder.newAddress;
import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.commons.error.CommonErrors.badRequestError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static org.innovateuk.ifs.user.builder.CompAdminEmailBuilder.newCompAdminEmail;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.ProfileBuilder.newProfile;
import static org.innovateuk.ifs.user.builder.ProjectFinanceEmailBuilder.newProjectFinanceEmail;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.*;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Tests around Registration Service
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RegistrationServiceImpl.class, StandardPasswordEncoder.class})
public class RegistrationServiceImplTest extends BaseServiceUnitTest<RegistrationServiceImpl> {

    private static final String webBaseUrl = "http://ifs-local-dev";

    @Mock
    private StandardPasswordEncoder standardPasswordEncoder;

    @Override
    protected RegistrationServiceImpl supplyServiceUnderTest() {
        final RegistrationServiceImpl service = new RegistrationServiceImpl();
        ReflectionTestUtils.setField(service, "webBaseUrl", webBaseUrl);
        return service;
    }

    @Test
    public void createUser() throws Exception {
        Set<Role> roles = newRole().buildSet(1);
        AddressResource addressResource = newAddressResource().withAddressLine1("Electric Works").withTown("Sheffield").withPostcode("S1 2BJ").build();
        Address address = newAddress().withAddressLine1("Electric Works").withTown("Sheffield").withPostcode("S1 2BJ").build();

        UserRegistrationResource userToCreateResource = newUserRegistrationResource()
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withEmail("email@example.com")
                .withPassword("Passw0rd123")
                .withAddress(addressResource)
                .build();

        Long profileId = 1L;
        Profile userProfile = newProfile()
                .withId(profileId)
                .withAddress(address)
                .build();

        User userToCreate = newUser()
                .withId((Long) null)
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withEmailAddress("email@example.com")
                .withRoles(roles)
                .withProfileId(userProfile.getId())
                .build();

        when(profileRepositoryMock.findOne(userToCreate.getProfileId())).thenReturn(userProfile);
        when(profileRepositoryMock.save(any(Profile.class))).thenReturn(userProfile);
        when(passwordPolicyValidatorMock.validatePassword("Passw0rd123", userToCreateResource.toUserResource())).thenReturn(serviceSuccess());
        when(userMapperMock.mapToDomain(userToCreateResource.toUserResource())).thenReturn(userToCreate);
        when(addressMapperMock.mapToDomain(userToCreateResource.getAddress())).thenReturn(
                newAddress().withAddressLine1("Electric Works").withTown("Sheffield").withPostcode("S1 2BJ").build());
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "Passw0rd123")).thenReturn(serviceSuccess("new-uid"));

        User userToSave = createLambdaMatcher(user -> {
            assertNull(user.getId());
            assertEquals("First Last", user.getName());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals("email@example.com", user.getEmail());

            assertEquals("new-uid", user.getUid());
            assertEquals(1, user.getRoles().size());
            assertEquals(roles, user.getRoles());

            assertNotNull(user.getProfileId());
            Profile profile = profileRepositoryMock.findOne(user.getProfileId());
            assertEquals(profileId, profile.getId());
            assertNull(profile.getSkillsAreas());
            assertNull(profile.getBusinessType());
            assertNull(profile.getAgreement());
            assertNull(profile.getCreatedBy());
            assertNull(profile.getCreatedOn());
            assertNull(profile.getModifiedBy());
            assertNull(profile.getModifiedOn());
            assertEquals(address, profile.getAddress());


            return true;
        });

        User savedUser = newUser().build();
        UserResource savedUserResource = newUserResource().build();

        when(userRepositoryMock.save(userToSave)).thenReturn(savedUser);
        when(userMapperMock.mapToResource(savedUser)).thenReturn(savedUserResource);

        UserResource result = service.createUser(userToCreateResource).getSuccessObjectOrThrowException();

        assertEquals(savedUserResource, result);
    }

    @Test
    public void testCreateOrganisationUser() {

        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().withId(123L).build();
        Role applicantRole = newRole().withName(APPLICANT.getName()).build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(organisationRepositoryMock.findByUsersId(anyLong())).thenReturn(singletonList(selectedOrganisation));
        when(roleRepositoryMock.findOneByName(APPLICANT.getName())).thenReturn(applicantRole);
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "thepassword")).thenReturn(serviceSuccess("new-uid"));

        Profile expectedProfile = newProfile().withId(7L).build();
        when(profileRepositoryMock.save(any(Profile.class))).thenReturn(expectedProfile);

        User expectedCreatedUser = createLambdaMatcher(user -> {

            assertNull(user.getId());
            assertEquals("First Last", user.getName());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());

            assertEquals("email@example.com", user.getEmail());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals("new-uid", user.getUid());
            assertEquals(1, user.getRoles().size());
            assertTrue(user.getRoles().contains(applicantRole));
            List<Organisation> orgs = organisationRepositoryMock.findByUsersId(user.getId());
            assertEquals(1, orgs.size());
            assertEquals(selectedOrganisation, orgs.get(0));
            assertEquals(expectedProfile.getId(), user.getProfileId());

            return true;
        });

        User savedUser = newUser().with(id(999L)).build();

        when(userRepositoryMock.save(expectedCreatedUser)).thenReturn(savedUser);

        Token expectedToken = createLambdaMatcher(token -> {
            assertEquals(TokenType.VERIFY_EMAIL_ADDRESS, token.getType());
            assertEquals(User.class.getName(), token.getClassName());
            assertEquals(savedUser.getId(), token.getClassPk());
            assertFalse(token.getHash().isEmpty());
            return true;
        });

        when(tokenRepositoryMock.save(expectedToken)).thenReturn(expectedToken);
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(null);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        UserResource result = service.createOrganisationUser(123L, userToCreate).getSuccessObjectOrThrowException();

        assertEquals(userToCreate, result);
    }

    @Test
    public void testCreateApplicantUserButOrganisationNotFound() {

        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(null);
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(null);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Organisation.class, 123L)));
    }

    @Test
    public void testCreateApplicantUserButRoleNotFound() {

        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(roleRepositoryMock.findOneByName(APPLICANT.getName())).thenReturn(null);
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(null);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Role.class, APPLICANT.getName())));
    }

    @Test
    public void testCreateApplicantUserButIdpCallFails() {

        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().build();
        Role applicantRole = newRole().build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(roleRepositoryMock.findOneByName(APPLICANT.getName())).thenReturn(applicantRole);
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "thepassword")).thenReturn(serviceFailure(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(null);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));
    }

    @Test
    public void testCreateApplicantUserButPasswordValidationFails() {

        UserResource userToCreate = newUserResource().withPassword("thepassword").build();
        Organisation selectedOrganisation = newOrganisation().build();
        Role applicantRole = newRole().build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(roleRepositoryMock.findOneByName(APPLICANT.getName())).thenReturn(applicantRole);
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "thepassword")).thenReturn(serviceFailure(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(null);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceFailure(badRequestError("bad password")));

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonErrors.badRequestError("bad password")));
    }

    @Test
    public void testCreateCompAdminUserForOrganisation() {
        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().build();
        Role compAdminRole = newRole().build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(organisationRepositoryMock.findByUsersId(anyLong())).thenReturn(singletonList(selectedOrganisation));
        when(roleRepositoryMock.findOneByName(COMP_ADMIN.getName())).thenReturn(compAdminRole);
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "thepassword")).thenReturn(serviceSuccess("new-uid"));

        Profile expectedProfile = newProfile().withId(7L).build();
        when(profileRepositoryMock.save(any(Profile.class))).thenReturn(expectedProfile);

        User expectedCreatedUser = createLambdaMatcher(user -> {

            assertNull(user.getId());
            assertEquals("First Last", user.getName());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());

            assertEquals("email@example.com", user.getEmail());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals("new-uid", user.getUid());
            assertEquals(1, user.getRoles().size());
            assertTrue(user.getRoles().contains(compAdminRole));
            List<Organisation> orgs = organisationRepositoryMock.findByUsersId(user.getId());
            assertEquals(1, orgs.size());
            assertEquals(selectedOrganisation, orgs.get(0));
            assertEquals(expectedProfile.getId(), user.getProfileId());

            return true;
        });

        User savedUser = newUser().with(id(999L)).build();

        when(userRepositoryMock.save(expectedCreatedUser)).thenReturn(savedUser);

        Token expectedToken = createLambdaMatcher(token -> {
            assertEquals(TokenType.VERIFY_EMAIL_ADDRESS, token.getType());
            assertEquals(User.class.getName(), token.getClassName());
            assertEquals(savedUser.getId(), token.getClassPk());
            assertFalse(token.getHash().isEmpty());
            return true;
        });

        CompAdminEmail compAdminEmail = newCompAdminEmail().withEmail("email@example.com").build();

        when(tokenRepositoryMock.save(expectedToken)).thenReturn(expectedToken);
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(compAdminEmail);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isSuccess());
        assertEquals(userToCreate, result.getSuccessObject());
    }

    @Test
    public void testCreateCompAdminUserForOrganisationButRoleNotFound() {

        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().build();
        CompAdminEmail compAdminEmail = newCompAdminEmail().withEmail("email@example.com").build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(roleRepositoryMock.findOneByName(COMP_ADMIN.getName())).thenReturn(null);
        when(compAdminEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(compAdminEmail);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Role.class, COMP_ADMIN.getName())));
    }

    @Test
    public void testCreateProjectFinanceUserForOrganisation() {
        UserResource userToCreate = newUserResource().
                withFirstName("First").
                withLastName("Last").
                withEmail("email@example.com").
                withPhoneNumber("01234 567890").
                withPassword("thepassword").
                build();

        Organisation selectedOrganisation = newOrganisation().build();
        Role projectFinanceRole = newRole().build();

        when(organisationRepositoryMock.findOne(123L)).thenReturn(selectedOrganisation);
        when(organisationRepositoryMock.findByUsersId(anyLong())).thenReturn(singletonList(selectedOrganisation));
        when(roleRepositoryMock.findOneByName(PROJECT_FINANCE.getName())).thenReturn(projectFinanceRole);
        when(idpServiceMock.createUserRecordWithUid("email@example.com", "thepassword")).thenReturn(serviceSuccess("new-uid"));

        Profile expectedProfile = newProfile().withId(7L).build();
        when(profileRepositoryMock.save(any(Profile.class))).thenReturn(expectedProfile);

        User expectedCreatedUser = createLambdaMatcher(user -> {

            assertNull(user.getId());
            assertEquals("First Last", user.getName());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());

            assertEquals("email@example.com", user.getEmail());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals("new-uid", user.getUid());
            assertEquals(1, user.getRoles().size());
            assertTrue(user.getRoles().contains(projectFinanceRole));
            List<Organisation> orgs = organisationRepositoryMock.findByUsersId(user.getId());
            assertEquals(1, orgs.size());
            assertEquals(selectedOrganisation, orgs.get(0));
            assertEquals(expectedProfile.getId(), user.getProfileId());

            return true;
        });

        User savedUser = newUser().with(id(999L)).build();

        when(userRepositoryMock.save(expectedCreatedUser)).thenReturn(savedUser);

        Token expectedToken = createLambdaMatcher(token -> {
            assertEquals(TokenType.VERIFY_EMAIL_ADDRESS, token.getType());
            assertEquals(User.class.getName(), token.getClassName());
            assertEquals(savedUser.getId(), token.getClassPk());
            assertFalse(token.getHash().isEmpty());
            return true;
        });

        ProjectFinanceEmail projectFinanceEmail = newProjectFinanceEmail().withEmail("email@example.com").build();

        when(tokenRepositoryMock.save(expectedToken)).thenReturn(expectedToken);
        when(projectFinanceEmailRepositoryMock.findOneByEmail(userToCreate.getEmail())).thenReturn(projectFinanceEmail);
        when(userMapperMock.mapToResource(isA(User.class))).thenReturn(userToCreate);
        when(passwordPolicyValidatorMock.validatePassword("thepassword", userToCreate)).thenReturn(serviceSuccess());

        ServiceResult<UserResource> result = service.createOrganisationUser(123L, userToCreate);
        assertTrue(result.isSuccess());
        assertEquals(userToCreate, result.getSuccessObject());
    }

    @Test
    public void testSendUserVerificationEmail() {
        final UserResource userResource = newUserResource()
                .withId(1L)
                .withFirstName("Sample")
                .withLastName("User")
                .withEmail("sample@me.com")
                .build();

        // mock the random number that will be used to create the hash
        final double random = 0.6996293870272714;
        PowerMockito.mockStatic(Math.class);
        when(Math.random()).thenReturn(random);
        when(Math.ceil(random * 1000)).thenReturn(700d);

        final String hash = "1e627a59879066b44781ca584a23be742d3197dff291245150e62f3d4d3d303e1a87d34fc8a3a2e0";
        ReflectionTestUtils.setField(service, "encoder", standardPasswordEncoder);
        when(standardPasswordEncoder.encode("1==sample@me.com==700")).thenReturn(hash);

        final Token token = new Token(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getName(), userResource.getId(), hash, now(), JsonNodeFactory.instance.objectNode());
        final String verificationLink = String.format("%s/registration/verify-email/%s", webBaseUrl, hash);

        final Map<String, Object> expectedNotificationArguments = asMap("verificationLink", verificationLink);

        final NotificationSource from = systemNotificationSourceMock;
        final NotificationTarget to = new ExternalUserNotificationTarget(userResource.getName(), userResource.getEmail());

        final Notification notification = new Notification(from, singletonList(to), RegistrationServiceImpl.Notifications.VERIFY_EMAIL_ADDRESS, expectedNotificationArguments);
        when(tokenRepositoryMock.save(isA(Token.class))).thenReturn(token);
        when(notificationServiceMock.sendNotification(notification, EMAIL)).thenReturn(serviceSuccess());

        final ServiceResult<Void> result = service.sendUserVerificationEmail(userResource, empty());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testResendUserVerificationEmail() {
        final UserResource userResource = newUserResource()
                .withId(1L)
                .withFirstName("Sample")
                .withLastName("User")
                .withEmail("sample@me.com")
                .build();

        // mock the random number that will be used to create the hash
        final double random = 0.6996293870272714;
        PowerMockito.mockStatic(Math.class);
        when(Math.random()).thenReturn(random);
        when(Math.ceil(random * 1000)).thenReturn(700d);

        final String hash = "1e627a59879066b44781ca584a23be742d3197dff291245150e62f3d4d3d303e1a87d34fc8a3a2e0";
        ReflectionTestUtils.setField(service, "encoder", standardPasswordEncoder);
        when(standardPasswordEncoder.encode("1==sample@me.com==700")).thenReturn(hash);

        final Token existingToken = new Token(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getName(), userResource.getId(), "existing-token", now(), JsonNodeFactory.instance.objectNode());
        final Token newToken = new Token(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getName(), userResource.getId(), hash, now(), JsonNodeFactory.instance.objectNode());
        final String verificationLink = String.format("%s/registration/verify-email/%s", webBaseUrl, hash);

        final Map<String, Object> expectedNotificationArguments = asMap("verificationLink", verificationLink);

        final NotificationSource from = systemNotificationSourceMock;
        final NotificationTarget to = new ExternalUserNotificationTarget(userResource.getName(), userResource.getEmail());

        final Notification notification = new Notification(from, singletonList(to), RegistrationServiceImpl.Notifications.VERIFY_EMAIL_ADDRESS, expectedNotificationArguments);
        when(tokenRepositoryMock.findByTypeAndClassNameAndClassPk(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getName(), 1L)).thenReturn(of(existingToken));
        when(tokenRepositoryMock.save(isA(Token.class))).thenReturn(newToken);
        when(notificationServiceMock.sendNotification(notification, EMAIL)).thenReturn(serviceSuccess());

        final ServiceResult<Void> result = service.resendUserVerificationEmail(userResource);
        assertTrue(result.isSuccess());
    }
}
