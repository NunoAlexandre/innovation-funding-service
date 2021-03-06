package org.innovateuk.ifs.user.transactional;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.registration.resource.InternalUserRegistrationResource;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.user.builder.UserResourceBuilder;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.security.UserLookupStrategies;
import org.innovateuk.ifs.user.security.UserPermissionRules;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.util.Optional;

import static org.innovateuk.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static java.util.Optional.of;
import static org.mockito.Mockito.*;

/**
 * Testing how this service integrates with Spring Security
 */
public class RegistrationServiceSecurityTest extends BaseServiceSecurityTest<RegistrationService> {

    private UserPermissionRules rules;
    private UserLookupStrategies lookup;

    @Before
    public void lookupPermissionRules() {
        rules = getMockPermissionRulesBean(UserPermissionRules.class);
        lookup = getMockPermissionEntityLookupStrategiesBean(UserLookupStrategies.class);
    }

    @Test
    public void testCreateUser() {
        UserRegistrationResource userToCreate = newUserRegistrationResource().build();

        assertAccessDenied(() -> classUnderTest.createUser(userToCreate), () -> {
            verify(rules).systemRegistrationUserCanCreateUsers(userToCreate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void testCreateOrganisationUser() {

        UserResource userToCreate = newUserResource().build();

        assertAccessDenied(() -> classUnderTest.createOrganisationUser(123L, userToCreate), () -> {
            verify(rules).systemRegistrationUserCanCreateUsers(userToCreate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void testActivateUser() {

        UserResource userToActivate = newUserResource().build();

        when(lookup.findById(123L)).thenReturn(userToActivate);

        assertAccessDenied(() -> classUnderTest.activateUser(123L), () -> {
            verify(rules).systemRegistrationUserCanActivateUsers(userToActivate, getLoggedInUser());
            verify(rules).ifsAdminCanReactivateUsers(userToActivate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void testReactivateUser() {

        UserResource userToActivate = newUserResource().build();

        when(lookup.findById(123L)).thenReturn(userToActivate);

        assertAccessDenied(() -> classUnderTest.activateUser(123L), () -> {
            verify(rules).systemRegistrationUserCanActivateUsers(userToActivate, getLoggedInUser());
            verify(rules).ifsAdminCanReactivateUsers(userToActivate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }


    @Test
    public void testDeactivateUser() {

        UserResource userToActivate = newUserResource().build();

        when(lookup.findById(123L)).thenReturn(userToActivate);

        assertAccessDenied(() -> classUnderTest.deactivateUser(123L), () -> {
            verify(rules).ifsAdminCanDeactivateUsers(userToActivate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }


    @Test
    public void testSendUserVerificationEmail() throws Exception {
        final UserResource userToSendVerificationEmail = newUserResource().build();

        assertAccessDenied(
                () -> classUnderTest.sendUserVerificationEmail(userToSendVerificationEmail, of(123L)),
                () -> {
                    verify(rules).systemRegistrationUserCanSendUserVerificationEmail(userToSendVerificationEmail, getLoggedInUser());
                    verifyNoMoreInteractions(rules);
                });
    }

    @Test
    public void testResendUserVerificationEmail() throws Exception {
        final UserResource userToSendVerificationEmail = newUserResource().build();

        assertAccessDenied(
                () -> classUnderTest.resendUserVerificationEmail(userToSendVerificationEmail),
                () -> {
                    verify(rules).systemRegistrationUserCanSendUserVerificationEmail(userToSendVerificationEmail, getLoggedInUser());
                    verifyNoMoreInteractions(rules);
                });
    }

    @Test
    public void testEditInternalUser() throws Exception {
        UserResource userToEdit = UserResourceBuilder.newUserResource().build();
        UserRoleType userRoleType = UserRoleType.SUPPORT;

        assertAccessDenied(
                () -> classUnderTest.editInternalUser(userToEdit, userRoleType),
                () -> {
                    verify(rules).ifsAdminCanEditInternalUser(userToEdit, getLoggedInUser());
                    verifyNoMoreInteractions(rules);
                });
    }

    @Override
    protected Class<? extends RegistrationService> getClassUnderTest() {
        return TestRegistrationService.class;
    }

    public static class TestRegistrationService implements RegistrationService {

        @Override
        public ServiceResult<UserResource> createUser(@P("user") UserRegistrationResource userResource) {
            return null;
        }

        @Override
        public ServiceResult<UserResource> createOrganisationUser(long organisationId, UserResource userResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> activateUser(long userId) {
            return null;
        }

        @Override
        public ServiceResult<Void> activateApplicantAndSendDiversitySurvey(long userId) {
            return null;
        }

        @Override
        public ServiceResult<Void> activateAssessorAndSendDiversitySurvey(long userId) {
            return null;
        }

        @Override
        public ServiceResult<Void> createInternalUser(String inviteHash, InternalUserRegistrationResource userRegistrationResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> editInternalUser(UserResource userToEdit, UserRoleType userRoleType) {
            return null;
        }

        @Override
        public ServiceResult<Void> sendUserVerificationEmail(UserResource user, Optional<Long> competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> resendUserVerificationEmail(UserResource user) {
            return null;
        }

        @Override
        public ServiceResult<Void> deactivateUser(long userId) { return null; }
    }
}
