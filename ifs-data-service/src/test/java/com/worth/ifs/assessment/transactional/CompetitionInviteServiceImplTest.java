package com.worth.ifs.assessment.transactional;

import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.invite.builder.RejectionReasonResourceBuilder;
import com.worth.ifs.invite.domain.CompetitionInvite;
import com.worth.ifs.invite.domain.CompetitionParticipant;
import com.worth.ifs.invite.domain.ParticipantStatus;
import com.worth.ifs.invite.domain.RejectionReason;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.invite.resource.RejectionReasonResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;

import static com.worth.ifs.assessment.builder.CompetitionInviteBuilder.newCompetitionInvite;
import static com.worth.ifs.assessment.builder.CompetitionInviteResourceBuilder.newCompetitionInviteResource;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.invite.builder.RejectionReasonBuilder.newRejectionReason;
import static com.worth.ifs.invite.constant.InviteStatus.CREATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class CompetitionInviteServiceImplTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private CompetitionInviteService competitionInviteService = new CompetitionInviteServiceImpl();

    private CompetitionParticipant competitionParticipant;

    @Before
    public void setUp() {
        Competition competition = newCompetition().withName("my competition").build();
        CompetitionInvite competitionInvite = newCompetitionInvite().withCompetition(competition).build();
        competitionParticipant = new CompetitionParticipant(competition, competitionInvite);
        CompetitionInviteResource expected = newCompetitionInviteResource().withCompetitionName("my competition").build();
        RejectionReason rejectionReason = newRejectionReason().withId(1L).withReason("not available").build();

        when(competitionInviteRepositoryMock.getByHash("inviteHash")).thenReturn(competitionInvite);


        when(competitionInviteRepositoryMock.save(same(competitionInvite))).thenReturn(competitionInvite);
        when(competitionInviteMapperMock.mapToResource(same(competitionInvite))).thenReturn(expected);

        when(competitionParticipantRepositoryMock.getByInviteHash("inviteHash")).thenReturn(competitionParticipant);

        when(rejectionReasonRepositoryMock.findOne(1L)).thenReturn(rejectionReason);
    }

    @Test
    public void getInvite() throws Exception {
        ServiceResult<CompetitionInviteResource> inviteServiceResult = competitionInviteService.getInvite("inviteHash");

        assertTrue(inviteServiceResult.isSuccess());

        CompetitionInviteResource competitionInviteResource = inviteServiceResult.getSuccessObjectOrThrowException();
        assertEquals("my competition", competitionInviteResource.getCompetitionName());

        InOrder inOrder = inOrder(competitionInviteRepositoryMock, competitionInviteMapperMock);
        inOrder.verify(competitionInviteRepositoryMock, calls(1)).getByHash("inviteHash");
        inOrder.verify(competitionInviteMapperMock, calls(1)).mapToResource(any(CompetitionInvite.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getInvite_hashNotExists() throws Exception {
        when(competitionInviteRepositoryMock.getByHash(anyString())).thenReturn(null);

        ServiceResult<CompetitionInviteResource> inviteServiceResult = competitionInviteService.getInvite("inviteHashNotExists");

        assertTrue(inviteServiceResult.isFailure());
        assertEquals(GENERAL_NOT_FOUND.getErrorKey(), inviteServiceResult.getFailure().getErrors().get(0).getErrorKey());

        InOrder inOrder = inOrder(competitionInviteRepositoryMock, competitionInviteMapperMock);
        inOrder.verify(competitionInviteRepositoryMock, calls(1)).getByHash("inviteHashNotExists");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void openInvite() throws Exception {
        ServiceResult<CompetitionInviteResource> inviteServiceResult = competitionInviteService.openInvite("inviteHash");

        assertTrue(inviteServiceResult.isSuccess());

        CompetitionInviteResource competitionInviteResource = inviteServiceResult.getSuccessObjectOrThrowException();
        assertEquals("my competition", competitionInviteResource.getCompetitionName());

        InOrder inOrder = inOrder(competitionInviteRepositoryMock, competitionInviteMapperMock);
        inOrder.verify(competitionInviteRepositoryMock, calls(1)).getByHash("inviteHash");
        inOrder.verify(competitionInviteRepositoryMock, calls(1)).save(any(CompetitionInvite.class));
        inOrder.verify(competitionInviteMapperMock, calls(1)).mapToResource(any(CompetitionInvite.class));
        inOrder.verifyNoMoreInteractions();
    }


    @Test
    public void openInvite_hashNotExists() throws Exception {
        when(competitionInviteRepositoryMock.getByHash(anyString())).thenReturn(null);

        ServiceResult<CompetitionInviteResource> inviteServiceResult = competitionInviteService.openInvite("inviteHashNotExists");

        assertTrue(inviteServiceResult.isFailure());
        assertEquals(GENERAL_NOT_FOUND.getErrorKey(), inviteServiceResult.getFailure().getErrors().get(0).getErrorKey());


        InOrder inOrder = inOrder(competitionInviteRepositoryMock, competitionInviteMapperMock);
        inOrder.verify(competitionInviteRepositoryMock, calls(1)).getByHash("inviteHashNotExists");
        inOrder.verifyNoMoreInteractions();
    }

    // accept

    @Test
    public void acceptInvite() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        ServiceResult<Void> serviceResult = competitionInviteService.acceptInvite("inviteHash");

        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.ACCEPTED, competitionParticipant.getStatus());

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvite_hashNotExists() {
        ServiceResult<Void> serviceResult = competitionInviteService.acceptInvite("inviteHashNotExists");

        assertTrue(serviceResult.isFailure());
        assertEquals(GENERAL_NOT_FOUND.getErrorKey(), serviceResult.getFailure().getErrors().get(0).getErrorKey());

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHashNotExists");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvite_notOpened() {
        assertEquals(CREATED, competitionParticipant.getInvite().get().getStatus());
        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        ServiceResult<Void> serviceResult = competitionInviteService.acceptInvite("inviteHash");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE));

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvite_alreadyAccepted() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        // accept the invite
        ServiceResult<Void> serviceResult = competitionInviteService.acceptInvite("inviteHash");
        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.ACCEPTED, competitionParticipant.getStatus());

        // accept a second time
        serviceResult = competitionInviteService.acceptInvite("inviteHash");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_ACCEPTED_INVITE));

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void acceptInvite_alreadyRejected() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());


        // reject the invite
        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();

        ServiceResult<Void> serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "too busy");
        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.REJECTED, competitionParticipant.getStatus());

        // accept the invite
        serviceResult = competitionInviteService.acceptInvite("inviteHash");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE));

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock, rejectionReasonRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }

    // reject

    @Test
    public void rejectInvite() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());


        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();

        ServiceResult<Void> serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "too busy");

        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.REJECTED, competitionParticipant.getStatus());
        assertEquals("too busy", competitionParticipant.getRejectionReasonComment());

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock, rejectionReasonRepositoryMock);
        inOrder.verify(rejectionReasonRepositoryMock, calls(1)).findOne(1L);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvite_hashNotExists() {
        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());
        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();

        ServiceResult<Void> serviceResult = competitionInviteService.rejectInvite("inviteHashNotExists", rejectionReasonResource, "too busy");

        assertTrue(serviceResult.isFailure());
        assertEquals(GENERAL_NOT_FOUND.getErrorKey(), serviceResult.getFailure().getErrors().get(0).getErrorKey());

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHashNotExists");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvite_notOpened() {
        assertEquals(CREATED, competitionParticipant.getInvite().get().getStatus());
        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();

        ServiceResult<Void> serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "too busy");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE));

        InOrder inOrder = inOrder(rejectionReasonRepositoryMock, competitionParticipantRepositoryMock);
        inOrder.verify(rejectionReasonRepositoryMock, calls(1)).findOne(1L);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvite_alreadyAccepted() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        // accept the invite
        ServiceResult<Void> serviceResult = competitionInviteService.acceptInvite("inviteHash");
        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.ACCEPTED, competitionParticipant.getStatus());

        // reject
        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();

        serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "too busy");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE));

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock, rejectionReasonRepositoryMock);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);
        inOrder.verify(rejectionReasonRepositoryMock, calls(1)).findOne(1L);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvite_alreadyRejected() {
        competitionInviteService.openInvite("inviteHash");

        assertEquals(ParticipantStatus.PENDING, competitionParticipant.getStatus());

        // reject the invite
        RejectionReasonResource rejectionReasonResource = RejectionReasonResourceBuilder
                .newRejectionReasonResource()
                .withId(1L)
                .build();
        ServiceResult<Void> serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "too busy");
        assertTrue(serviceResult.isSuccess());
        assertEquals(ParticipantStatus.REJECTED, competitionParticipant.getStatus());

        // reject again

        serviceResult = competitionInviteService.rejectInvite("inviteHash", rejectionReasonResource, "still too busy");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_REJECTED_INVITE));

        InOrder inOrder = inOrder(competitionParticipantRepositoryMock, rejectionReasonRepositoryMock);

        inOrder.verify(rejectionReasonRepositoryMock, calls(1)).findOne(1L);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).save(competitionParticipant);
        inOrder.verify(competitionParticipantRepositoryMock, calls(1)).getByInviteHash("inviteHash");
        inOrder.verifyNoMoreInteractions();
    }
}