package org.innovateuk.ifs.assessment.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.invite.resource.*;

import java.util.List;

/**
 * REST service for managing {@link org.innovateuk.ifs.invite.resource.InviteResource} to {@link org.innovateuk.ifs.competition.resource.CompetitionResource }
 */
public interface CompetitionInviteRestService {

    RestResult<AssessorInviteToSendResource> getCreated(long inviteId);

    RestResult<CompetitionInviteResource> getInvite(String inviteHash);

    RestResult<CompetitionInviteResource> openInvite(String inviteHash);

    RestResult<Void> acceptInvite(String inviteHash);

    RestResult<Void> rejectInvite(String inviteHash, CompetitionRejectionResource rejectionReason);

    RestResult<Boolean> checkExistingUser(String inviteHash);

    RestResult<AvailableAssessorPageResource> getAvailableAssessors(long competitionId);

    RestResult<List<AssessorCreatedInviteResource>> getCreatedInvites(long competitionId);

    RestResult<List<AssessorInviteOverviewResource>> getInvitationOverview(long competitionId);

    RestResult<CompetitionInviteStatisticsResource> getInviteStatistics(long competitionId);

    RestResult<CompetitionInviteResource> inviteUser(ExistingUserStagedInviteResource existingUserStagedInvite);

    RestResult<Void> inviteNewUsers(NewUserStagedInviteListResource newUserStagedInvites, long competitionId);

    RestResult<Void> deleteInvite(String email, long competitionId);

    RestResult<AssessorInviteToSendResource> sendInvite(long inviteId, EmailContent content);
}
