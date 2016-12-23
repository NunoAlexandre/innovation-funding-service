package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.BuilderAmendFunctions;
import org.innovateuk.ifs.authentication.service.RestIdentityProviderService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.CompetitionInvite;
import org.innovateuk.ifs.invite.resource.CompetitionInviteResource;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;

import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static org.innovateuk.ifs.user.builder.EthnicityResourceBuilder.newEthnicityResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Disability.NO;
import static org.innovateuk.ifs.user.resource.Gender.NOT_STATED;
import static org.innovateuk.ifs.user.resource.UserRoleType.ASSESSOR;
import static org.junit.Assert.assertTrue;
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
                .withAddress(newAddressResource().withAddressLine1("Electric Works").withTown("Sheffield").withPostcode("S1 2BJ").build())
                .build();

        RoleResource roleResource = newRoleResource().build();

        CompetitionInviteResource competitionInviteResource = newCompetitionInviteResource()
                .withEmail("email@example.com")
                .build();

        when(competitionInviteServiceMock.getInvite(hash)).thenReturn(serviceSuccess(competitionInviteResource));
        when(roleServiceMock.findByUserRoleType(ASSESSOR)).thenReturn(serviceSuccess(roleResource));

        UserResource createdUser = newUserResource().build();

        when(registrationServiceMock.createUser(userRegistrationResource)).thenReturn(serviceSuccess(createdUser));

        when(registrationServiceMock.activateUser(createdUser.getId())).thenReturn(serviceSuccess());
        when(competitionInviteServiceMock.acceptInvite(hash, createdUser)).thenReturn(serviceSuccess());

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);

        assertTrue(serviceResult.isSuccess());

        InOrder inOrder = inOrder(competitionInviteServiceMock, roleServiceMock, registrationServiceMock);
        inOrder.verify(competitionInviteServiceMock).getInvite(hash);
        inOrder.verify(roleServiceMock).findByUserRoleType(ASSESSOR);
        inOrder.verify(registrationServiceMock).createUser(userRegistrationResource);
        inOrder.verify(registrationServiceMock).activateUser(createdUser.getId());
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

        when(registrationServiceMock.createUser(userRegistrationResource)).thenReturn(serviceFailure(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));

        ServiceResult<Void> serviceResult = assessorService.registerAssessorByHash(hash, userRegistrationResource);

        InOrder inOrder = inOrder(competitionInviteServiceMock, roleServiceMock, registrationServiceMock);
        inOrder.verify(competitionInviteServiceMock).getInvite(hash);
        inOrder.verify(roleServiceMock).findByUserRoleType(ASSESSOR);
        inOrder.verify(registrationServiceMock).createUser(userRegistrationResource);
        inOrder.verifyNoMoreInteractions();

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(new Error(RestIdentityProviderService.ServiceFailures.UNABLE_TO_CREATE_USER, INTERNAL_SERVER_ERROR)));
    }
}
