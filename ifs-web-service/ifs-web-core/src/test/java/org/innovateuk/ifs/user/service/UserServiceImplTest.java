package org.innovateuk.ifs.user.service;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.commons.error.exception.GeneralUnexpectedErrorException;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test Class for functionality in {@link UserServiceImpl}
 */
public class UserServiceImplTest extends BaseServiceUnitTest<UserService> {
    private static final String EMAIL_THAT_EXISTS_FOR_USER = "sample@me.com";

    private static final String EMAIL_THAT_EXISTS_FOR_USER_BUT_CAUSES_OTHER_ERROR = "i-am-bad@me.com";
    @Mock
    private UserRestService userRestService;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        when(userRestService.resendEmailVerificationNotification(eq(EMAIL_THAT_EXISTS_FOR_USER))).thenReturn(restSuccess());
        when(userRestService.resendEmailVerificationNotification(eq(EMAIL_THAT_EXISTS_FOR_USER_BUT_CAUSES_OTHER_ERROR))).thenReturn(restFailure(internalServerErrorError()));
        when(userRestService.resendEmailVerificationNotification(not(or(eq(EMAIL_THAT_EXISTS_FOR_USER), eq(EMAIL_THAT_EXISTS_FOR_USER_BUT_CAUSES_OTHER_ERROR))))).thenReturn(restFailure(notFoundError(UserResource.class)));
    }

    @Override
    protected UserService supplyServiceUnderTest() {
        return new UserServiceImpl();
    }

    @Test
    public void resendEmailVerificationNotification() throws Exception {
        service.resendEmailVerificationNotification(EMAIL_THAT_EXISTS_FOR_USER);
        verify(userRestService, only()).resendEmailVerificationNotification(EMAIL_THAT_EXISTS_FOR_USER);
    }

    @Test(expected = Test.None.class /* No exception expected here even though the mock returns an ObjectNotFoundException. We don't want to reveal that an email address was not recognised. */)
    public void resendEmailVerificationNotification_notExists() throws Exception {
        // Try sending the verification link to an email address which doesn't exist for a user
        final String email = "i-dont-exist@me.com";

        service.resendEmailVerificationNotification(email);
        verify(userRestService, only()).resendEmailVerificationNotification(email);
    }

    @Test(expected = GeneralUnexpectedErrorException.class)
    public void resendEmailVerificationNotification_otherError() throws Exception {
        // Try sending the verification link to an email address that exists but cause another error to occur

        service.resendEmailVerificationNotification(EMAIL_THAT_EXISTS_FOR_USER_BUT_CAUSES_OTHER_ERROR);
        verify(userRestService, only()).resendEmailVerificationNotification(EMAIL_THAT_EXISTS_FOR_USER_BUT_CAUSES_OTHER_ERROR);
    }

    @Test
    public void findUserByType() throws Exception {
        UserResource userOne = new UserResource();
        userOne.setId(1L);

        UserResource userTwo = new UserResource();
        userTwo.setId(2L);

        List<UserResource> expected = new ArrayList<>(asList(userOne, userTwo));
        when(userRestService.findByUserRoleType(UserRoleType.COMP_ADMIN)).thenReturn(restSuccess(expected));

        List<UserResource> found = service.findUserByType(UserRoleType.COMP_ADMIN);
        assertTrue(found.size() > 0);
    }

    @Test
    public void userHasApplicationForCompetition() throws Exception {
        Long userId = 1L;
        Long competitionId = 2L;
        Boolean expected = true;

        when(userRestService.userHasApplicationForCompetition(userId, competitionId)).thenReturn(restSuccess(expected));

        Boolean response = service.userHasApplicationForCompetition(userId, competitionId);
        assertEquals(expected, response);

        verify(userRestService, only()).userHasApplicationForCompetition(userId, competitionId);
    }

    @Test
    public void isCompetitionExecutive() {
        Long userId = 1L;
        RoleResource roleResource = newRoleResource()
                .withType(UserRoleType.COMP_ADMIN)
                .build();
        UserResource userResource = newUserResource()
                .withId(userId)
                .withRolesGlobal(singletonList(roleResource))
                .build();

        when(userRestService.retrieveUserById(userId)).thenReturn(restSuccess(userResource));

        assertTrue(service.isCompetitionExecutive(userId));
    }

    @Test
    public void isCompetitionExecutive_wrongRole() {
        Long userId = 1L;
        RoleResource roleResource = newRoleResource()
                .withType(UserRoleType.FINANCE_CONTACT)
                .build();
        UserResource userResource = newUserResource()
                .withId(userId)
                .withRolesGlobal(singletonList(roleResource))
                .build();

        when(userRestService.retrieveUserById(userId)).thenReturn(restSuccess(userResource));

        assertFalse(service.isCompetitionExecutive(userId));
    }

    @Test
    public void isCompetitionExecutive_userNotFound() {
        Long userId = 1L;

        when(userRestService.retrieveUserById(userId)).thenThrow(new ObjectNotFoundException());

        assertFalse(service.isCompetitionExecutive(userId));
    }

    @Test
    public void isCompetitionTechnologist() {
        Long userId = 1L;
        RoleResource roleResource = newRoleResource()
                .withType(UserRoleType.COMP_TECHNOLOGIST)
                .build();
        UserResource userResource = newUserResource()
                .withId(userId)
                .withRolesGlobal(singletonList(roleResource))
                .build();

        when(userRestService.retrieveUserById(userId)).thenReturn(restSuccess(userResource));

        assertTrue(service.isCompetitionTechnologist(userId));
    }

    @Test
    public void isCompetitionTechnologist_wrongRole() {
        Long userId = 1L;
        RoleResource roleResource = newRoleResource()
                .withType(UserRoleType.FINANCE_CONTACT)
                .build();
        UserResource userResource = newUserResource()
                .withId(userId)
                .withRolesGlobal(singletonList(roleResource))
                .build();

        when(userRestService.retrieveUserById(userId)).thenReturn(restSuccess(userResource));

        assertFalse(service.isCompetitionTechnologist(userId));
    }

    @Test
    public void isCompetitionTechnologist_userNotFound() {
        Long userId = 1L;

        when(userRestService.retrieveUserById(userId)).thenThrow(new ObjectNotFoundException());

        assertFalse(service.isCompetitionTechnologist(userId));
    }
}
