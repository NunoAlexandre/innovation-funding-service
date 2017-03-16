package org.innovateuk.ifs.assessment.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.assessment.transactional.CompetitionInviteService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.invite.builder.AssessorInviteSendResourceBuilder.newAssessorInviteSendResource;
import static org.innovateuk.ifs.invite.builder.CompetitionParticipantResourceBuilder.newCompetitionParticipantResource;
import static org.innovateuk.ifs.invite.builder.ExistingUserStagedInviteResourceBuilder.newExistingUserStagedInviteResource;
import static org.innovateuk.ifs.invite.builder.NewUserStagedInviteResourceBuilder.newNewUserStagedInviteResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.*;
import static org.mockito.Mockito.*;

public class CompetitionInviteServiceSecurityTest extends BaseServiceSecurityTest<CompetitionInviteService> {

    private CompetitionParticipantPermissionRules competitionParticipantPermissionRules;
    private CompetitionParticipantLookupStrategy competitionParticipantLookupStrategy;

    @Override
    protected Class<? extends CompetitionInviteService> getClassUnderTest() {
        return TestCompetitionInviteService.class;
    }

    @Before
    public void setUp() throws Exception {
        competitionParticipantPermissionRules = getMockPermissionRulesBean(CompetitionParticipantPermissionRules.class);
        competitionParticipantLookupStrategy = getMockPermissionEntityLookupStrategiesBean(CompetitionParticipantLookupStrategy.class);
    }

    @Test
    public void acceptInvite() {
        UserResource assessorUserResource = newUserResource()
                .withRolesGlobal(singletonList(
                        newRoleResource()
                                .withType(ASSESSOR)
                                .build()
                        )
                ).build();
        CompetitionParticipantResource competitionParticipantResource = newCompetitionParticipantResource().build();

        when(competitionParticipantLookupStrategy.getCompetitionParticipantResource("hash"))
                .thenReturn(competitionParticipantResource);
        when(competitionParticipantPermissionRules.userCanAcceptCompetitionInvite(competitionParticipantResource, assessorUserResource))
                .thenReturn(true);

        setLoggedInUser(assessorUserResource);

        classUnderTest.acceptInvite("hash", getLoggedInUser());

        verify(competitionParticipantLookupStrategy, only()).getCompetitionParticipantResource("hash");
        verify(competitionParticipantPermissionRules, only()).userCanAcceptCompetitionInvite(competitionParticipantResource, assessorUserResource);
    }

    @Test
    public void acceptInvite_notLoggedIn() {
        setLoggedInUser(null);
        assertAccessDenied(
                () -> classUnderTest.acceptInvite("hash", getLoggedInUser()),
                () -> {
                    verify(competitionParticipantLookupStrategy, only()).getCompetitionParticipantResource("hash");
                    verifyZeroInteractions(competitionParticipantPermissionRules);
                }
        );
    }

    @Test
    public void acceptInvite_notSameUser() {
        UserResource assessorUserResource = newUserResource()
                .withRolesGlobal(singletonList(
                        newRoleResource()
                                .withType(ASSESSOR)
                                .build()
                        )
                ).build();
        CompetitionParticipantResource competitionParticipantResource = newCompetitionParticipantResource().build();
        when(competitionParticipantLookupStrategy.getCompetitionParticipantResource("hash"))
                .thenReturn(competitionParticipantResource);
        when(competitionParticipantPermissionRules.userCanAcceptCompetitionInvite(competitionParticipantResource, assessorUserResource))
                .thenReturn(false);

        setLoggedInUser(assessorUserResource);

        assertAccessDenied(
                () -> classUnderTest.acceptInvite("hash", getLoggedInUser()),
                () -> {
                    verify(competitionParticipantLookupStrategy, only()).getCompetitionParticipantResource("hash");
                    verify(competitionParticipantPermissionRules, only()).userCanAcceptCompetitionInvite(competitionParticipantResource, assessorUserResource);
                }
        );
    }

    @Test
    public void acceptInvite_hashNotExists() {
        UserResource assessorUserResource = newUserResource()
                .withRolesGlobal(singletonList(
                        newRoleResource()
                                .withType(ASSESSOR)
                                .build()
                        )
                ).build();

        when(competitionParticipantLookupStrategy.getCompetitionParticipantResource("hash not exists")).thenReturn(null);

        setLoggedInUser(assessorUserResource);

        assertAccessDenied(
                () -> classUnderTest.acceptInvite("hash not exists", getLoggedInUser()),
                () -> {
                    verify(competitionParticipantLookupStrategy, only()).getCompetitionParticipantResource("hash not exists");
                    verifyZeroInteractions(competitionParticipantPermissionRules);
                }
        );
    }

    @Test
    public void getCreatedInvites() {
        Pageable pageable = new PageRequest(0, 20);

        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getCreatedInvites(1L, pageable), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void getInvitationOverview() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getInvitationOverview(1L), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void getAvailableAssessors() {
        Optional<Long> innovationArea = Optional.of(1L);
        Pageable pageable = new PageRequest(0, 20);

        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getAvailableAssessors(1L, pageable, innovationArea), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void getInviteStatistics() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.getInviteStatistics(1L), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void inviteUser_existing() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.inviteUser(newExistingUserStagedInviteResource().build()), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void inviteUser_new() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.inviteUser(newNewUserStagedInviteResource().build()), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void inviteNewUsers() throws Exception {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.inviteNewUsers(
                newNewUserStagedInviteResource().build(2), 1L)
                , COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void sendInvite() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.sendInvite(1L, newAssessorInviteSendResource().build()), COMP_ADMIN, PROJECT_FINANCE);
    }

    @Test
    public void deleteInvite() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.deleteInvite("email", 1L), COMP_ADMIN, PROJECT_FINANCE);
    }

    public static class TestCompetitionInviteService implements CompetitionInviteService {

        @Override
        public ServiceResult<AssessorInviteToSendResource> getCreatedInvite(long inviteId) {
            return null;
        }

        @Override
        public ServiceResult<CompetitionInviteResource> getInvite(@P("inviteHash") String inviteHash) {
            return null;
        }

        @Override
        public ServiceResult<CompetitionInviteResource> openInvite(@P("inviteHash") String inviteHash) {
            return null;
        }

        @Override
        public ServiceResult<Void> acceptInvite(@P("inviteHash") String inviteHash, UserResource userResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> rejectInvite(@P("inviteHash") String inviteHash, RejectionReasonResource rejectionReason, Optional<String> rejectionComment) {
            return null;
        }

        @Override
        public ServiceResult<Boolean> checkExistingUser(@P("inviteHash") String inviteHash) {
            return null;
        }

        @Override
        public ServiceResult<AvailableAssessorPageResource> getAvailableAssessors(long competitionId, Pageable pageable, Optional<Long> innovationArea) {
            return null;
        }

        @Override
        public ServiceResult<AssessorCreatedInvitePageResource> getCreatedInvites(long competitionId, Pageable pageable) {
            return null;
        }

        @Override
        public ServiceResult<List<AssessorInviteOverviewResource>> getInvitationOverview(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<CompetitionInviteStatisticsResource> getInviteStatistics(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<CompetitionInviteResource> inviteUser(NewUserStagedInviteResource stagedInvite) {
            return null;
        }

        @Override
        public ServiceResult<CompetitionInviteResource> inviteUser(ExistingUserStagedInviteResource existingUserStagedInviteResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> inviteNewUsers(List<NewUserStagedInviteResource> newUserStagedInvites, long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> sendInvite(long inviteId, AssessorInviteSendResource assessorInviteSendResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> deleteInvite(String email, long competitionId) {
            return null;
        }
    }
}
