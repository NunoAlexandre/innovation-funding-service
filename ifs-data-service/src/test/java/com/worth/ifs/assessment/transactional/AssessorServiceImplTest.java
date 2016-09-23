package com.worth.ifs.assessment.transactional;

import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.BuilderAmendFunctions;
import com.worth.ifs.authentication.service.RestIdentityProviderService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.invite.domain.CompetitionInvite;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.registration.resource.UserRegistrationResource;
import com.worth.ifs.user.resource.RoleResource;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;

import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static com.worth.ifs.user.builder.EthnicityResourceBuilder.newEthnicityResource;
import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.resource.Disability.NO;
import static com.worth.ifs.user.resource.Gender.NOT_STATED;
import static com.worth.ifs.user.resource.UserRoleType.ASSESSOR;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class AssessorServiceImplTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private AssessorService assessorService = new AssessorServiceImpl();

    @Test
    public void registerAssessorByHash_callCorrectServicesAndHaveSuccessfulOutcome() throws Exception {
        String hash = "testhash";

        UserRegistrationResource userRegistrationResource = newUserRegistrationResource()
                .withTitle("Mr")
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withGender(NOT_STATED)
                .withEthnicity(newEthnicityResource().with(BuilderAmendFunctions.id(1L)).build())
                .withDisability(NO)
                .withPassword("Password123")
                .build();

        RoleResource roleResource = newRoleResource().build();

        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource()
                .withEmail("email@example.com")
                .build();

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));
        when(roleServiceMock.findByUserRoleType(ASSESSOR)).thenReturn(serviceSuccess(roleResource));

        UserResource userToCreate = createLambdaMatcher(user -> {
            assertNull(user.getId());
            assertEquals("Mr", user.getTitle());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals(NOT_STATED, user.getGender());
            assertEquals(Long.valueOf(1L), user.getEthnicity());
            assertEquals(NO, user.getDisability());
            assertEquals("email@example.com", user.getEmail());
            assertEquals(asList(roleResource), user.getRoles());

            return true;
        });

        UserResource createdUser = newUserResource().build();

        when(registrationServiceMock.createUser(userToCreate)).thenReturn(serviceSuccess(createdUser));
        when(registrationServiceMock.activateUser(createdUser.getId())).thenReturn(serviceSuccess());
        when(competitionInviteServiceMock.acceptInvite(hash)).thenReturn(serviceSuccess());

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);
        assertTrue(serviceResult.isSuccess());

        InOrder inOrder = inOrder(competitionInviteServiceMock, roleServiceMock, registrationServiceMock);
        inOrder.verify(competitionInviteServiceMock).getInvite(hash);
        inOrder.verify(roleServiceMock).findByUserRoleType(ASSESSOR);
        inOrder.verify(registrationServiceMock).createUser(isA(UserResource.class));
        inOrder.verify(registrationServiceMock).activateUser(createdUser.getId());
        inOrder.verify(competitionInviteServiceMock).acceptInvite(hash);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void registerAssessorByHash_inviteDoesNotExistResultsInFailureAndSkippingUserRegistrationAndInviteAcceptance() throws Exception {
        String hash = "inviteHashNotExists";

        UserRegistrationResource userRegistrationResource = newUserRegistrationResource()
                .withTitle("Mr")
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withGender(NOT_STATED)
                .withEthnicity(newEthnicityResource().with(BuilderAmendFunctions.id(1L)).build())
                .withDisability(NO)
                .withPassword("Password123")
                .build();

        ServiceResult<CompetitionInviteResource> inviteResult = serviceFailure(notFoundError(CompetitionInvite.class, hash));

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(inviteResult);

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);

        verify(competitionInviteServiceMock).getInvite(hash);
        verifyNoMoreInteractions(roleServiceMock);
        verifyNoMoreInteractions(registrationServiceMock);
        verifyNoMoreInteractions(competitionInviteServiceMock);

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(CompetitionInvite.class, "inviteHashNotExists")));
    }

    @Test
    public void registerAssessorByHash_unopenedInviteCannotBeAcceptedResultsInServiceFailure() throws Exception {
        String hash = "testhash";

        UserRegistrationResource userRegistrationResource = newUserRegistrationResource()
                .withTitle("Mr")
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withGender(NOT_STATED)
                .withEthnicity(newEthnicityResource().with(BuilderAmendFunctions.id(1L)).build())
                .withDisability(NO)
                .withPassword("Password123")
                .build();

        RoleResource roleResource = newRoleResource().build();

        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource()
                .withEmail("email@example.com")
                .build();

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));
        when(roleServiceMock.findByUserRoleType(ASSESSOR)).thenReturn(serviceSuccess(roleResource));

        UserResource userToCreate = createLambdaMatcher(user -> {
            assertNull(user.getId());
            assertEquals("Mr", user.getTitle());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals(NOT_STATED, user.getGender());
            assertEquals(Long.valueOf(1L), user.getEthnicity());
            assertEquals(NO, user.getDisability());
            assertEquals("email@example.com", user.getEmail());
            assertEquals(asList(roleResource), user.getRoles());

            return true;
        });

        UserResource createdUser = newUserResource().build();

        Error notFoundError = new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, "Juggling Craziness");

        when(registrationServiceMock.createUser(userToCreate)).thenReturn(serviceSuccess(createdUser));
        when(registrationServiceMock.activateUser(createdUser.getId())).thenReturn(serviceSuccess());
        when(competitionInviteServiceMock.acceptInvite(hash)).thenReturn(serviceFailure(notFoundError));

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);

        InOrder inOrder = inOrder(competitionInviteServiceMock, roleServiceMock, registrationServiceMock);
        inOrder.verify(competitionInviteServiceMock).getInvite(hash);
        inOrder.verify(roleServiceMock).findByUserRoleType(ASSESSOR);
        inOrder.verify(registrationServiceMock).createUser(isA(UserResource.class));
        inOrder.verify(registrationServiceMock).activateUser(createdUser.getId());
        inOrder.verify(competitionInviteServiceMock).acceptInvite(hash);
        inOrder.verifyNoMoreInteractions();

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, "Juggling Craziness"));
    }

    @Test
    public void registerAssessorByHash_userValidationFailureResultsInFailureAndNotAcceptingInvite() throws Exception {
        String hash = "testhash";

        UserRegistrationResource userRegistrationResource = newUserRegistrationResource()
                .withTitle("Mr")
                .withFirstName("First")
                .withLastName("Last")
                .withPhoneNumber("01234 567890")
                .withGender(NOT_STATED)
                .withEthnicity(newEthnicityResource().with(BuilderAmendFunctions.id(1L)).build())
                .withDisability(NO)
                .withPassword("Password123")
                .build();

        RoleResource roleResource = newRoleResource().build();

        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource()
                .withEmail("email@example.com")
                .build();

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));
        when(roleServiceMock.findByUserRoleType(ASSESSOR)).thenReturn(serviceSuccess(roleResource));

        UserResource userToCreate = createLambdaMatcher(user -> {
            assertNull(user.getId());
            assertEquals("Mr", user.getTitle());
            assertEquals("First", user.getFirstName());
            assertEquals("Last", user.getLastName());
            assertEquals("01234 567890", user.getPhoneNumber());
            assertEquals(NOT_STATED, user.getGender());
            assertEquals(Long.valueOf(1L), user.getEthnicity());
            assertEquals(NO, user.getDisability());
            assertEquals("email@example.com", user.getEmail());
            assertEquals(asList(roleResource), user.getRoles());

            return true;
        });

        when(registrationServiceMock.createUser(userToCreate)).thenReturn(serviceFailure(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);

        verify(registrationServiceMock).createUser(isA(UserResource.class));
        verifyNoMoreInteractions(registrationServiceMock);
        verify(competitionInviteServiceMock).getInvite(hash);
        verifyNoMoreInteractions(competitionInviteServiceMock);

        InOrder inOrder = inOrder(competitionInviteServiceMock, roleServiceMock, registrationServiceMock);
        inOrder.verify(competitionInviteServiceMock).getInvite(hash);
        inOrder.verify(roleServiceMock).findByUserRoleType(ASSESSOR);
        inOrder.verify(registrationServiceMock).createUser(isA(UserResource.class));
        inOrder.verifyNoMoreInteractions();

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));
    }
}