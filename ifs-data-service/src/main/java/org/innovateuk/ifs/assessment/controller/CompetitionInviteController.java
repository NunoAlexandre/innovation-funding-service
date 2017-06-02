package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.assessment.transactional.CompetitionInviteService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing Invites to Competitions.
 */
@RestController
@RequestMapping("/competitioninvite")
public class CompetitionInviteController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private CompetitionInviteService competitionInviteService;

    // TODO we might be able to remove this as part of IFS-39
    /**
     * Gets the {@link AssessorInviteToSendResource} with the given {@code inviteId} only if it's in the
     * {@link org.innovateuk.ifs.invite.constant.InviteStatus#CREATED} state.
     *
     * @param inviteId the id of the {@link AssessorInviteToSendResource} to get
     * @return the {@link AssessorInviteToSendResource} with the given {@code inviteId} only if it's in the
     * {@link org.innovateuk.ifs.invite.constant.InviteStatus#CREATED} state.
     */
    @GetMapping("/getCreatedInviteToSend/{inviteId}")
    public RestResult<AssessorInviteToSendResource> getCreatedInviteToSend(@PathVariable long inviteId) {
        return competitionInviteService.getCreatedInviteToSend(inviteId).toGetResponse();
    }

    /**
     * Gets the {@link AssessorInviteToSendResource} with the given {@code inviteId} irrespective of it's state.
     *
     * @param inviteId the id of the {@link AssessorInviteToSendResource} to get
     * @return the {@link AssessorInviteToSendResource} with the given {@code inviteId}
     */
    @GetMapping("/getInviteToSend/{inviteId}")
    public RestResult<AssessorInviteToSendResource> getInviteToSend(@PathVariable long inviteId) {
        return competitionInviteService.getInviteToSend(inviteId).toGetResponse();
    }

    @GetMapping("/getInvite/{inviteHash}")
    public RestResult<CompetitionInviteResource> getInvite(@PathVariable String inviteHash) {
        return competitionInviteService.getInvite(inviteHash).toGetResponse();
    }

    @PostMapping("/openInvite/{inviteHash}")
    public RestResult<CompetitionInviteResource> openInvite(@PathVariable String inviteHash) {
        return competitionInviteService.openInvite(inviteHash).toPostWithBodyResponse();
    }

    @PostMapping("/acceptInvite/{inviteHash}")
    public RestResult<Void> acceptInvite(@PathVariable String inviteHash) {
        final UserResource currentUser = (UserResource) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return competitionInviteService.acceptInvite(inviteHash, currentUser).toPostResponse();
    }

    @PostMapping("/rejectInvite/{inviteHash}")
    public RestResult<Void> rejectInvite(@PathVariable String inviteHash, @Valid @RequestBody CompetitionRejectionResource rejection) {
        return competitionInviteService.rejectInvite(inviteHash, rejection.getRejectReason(), Optional.ofNullable(rejection.getRejectComment())).toPostResponse();
    }

    @GetMapping("/checkExistingUser/{inviteHash}")
    public RestResult<Boolean> checkExistingUser(@PathVariable String inviteHash) {
        return competitionInviteService.checkExistingUser(inviteHash).toGetResponse();
    }

    @GetMapping("/getAvailableAssessors/{competitionId}")
    public RestResult<AvailableAssessorPageResource> getAvailableAssessors(
            @PathVariable long competitionId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = {"firstName", "lastName"}, direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Optional<Long> innovationArea
    ) {
        return competitionInviteService.getAvailableAssessors(competitionId, pageable, innovationArea).toGetResponse();
    }

    @GetMapping(value = "/getAvailableAssessors/{competitionId}", params = "all")
    public RestResult<List<AvailableAssessorResource>> getAvailableAssessors(
            @PathVariable long competitionId,
            @RequestParam Optional<Long> innovationArea
    ) {
        return competitionInviteService.getAvailableAssessors(competitionId, innovationArea).toGetResponse();
    }

    @GetMapping("/getCreatedInvites/{competitionId}")
    public RestResult<AssessorCreatedInvitePageResource> getCreatedInvites(
            @PathVariable long competitionId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return competitionInviteService.getCreatedInvites(competitionId, pageable).toGetResponse();
    }

    @GetMapping("/getInvitationOverview/{competitionId}")
    public RestResult<AssessorInviteOverviewPageResource> getInvitationOverview(
            @PathVariable long competitionId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "invite.name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Optional<Long> innovationArea,
            @RequestParam Optional<ParticipantStatus> status,
            @RequestParam Optional<Boolean> compliant
    ) {
        return competitionInviteService.getInvitationOverview(competitionId, pageable, innovationArea, status, compliant).toGetResponse();
    }

    @GetMapping("/getInviteStatistics/{competitionId}")
    public RestResult<CompetitionInviteStatisticsResource> getInviteStatistics(@PathVariable Long competitionId) {
        return competitionInviteService.getInviteStatistics(competitionId).toGetResponse();
    }

    @PostMapping("/inviteUser")
    public RestResult<CompetitionInviteResource> inviteUser(@Valid @RequestBody ExistingUserStagedInviteResource existingUserStagedInvite) {
        return competitionInviteService.inviteUser(existingUserStagedInvite).toPostWithBodyResponse();
    }

    @PostMapping("/inviteUsers")
    public RestResult<Void> inviteUsers(@Valid @RequestBody ExistingUserStagedInviteListResource existingUserStagedInvites) {
        return competitionInviteService.inviteUsers(existingUserStagedInvites.getInvites()).toPostWithBodyResponse();
    }

    @PostMapping("/inviteNewUser")
    public RestResult<CompetitionInviteResource> inviteNewUser(@Valid @RequestBody NewUserStagedInviteResource newUserStagedInvite) {
        return competitionInviteService.inviteUser(newUserStagedInvite).toPostWithBodyResponse();
    }

    @PostMapping("/inviteNewUsers/{competitionId}")
    public RestResult<Void> inviteNewUsers(@Valid @RequestBody NewUserStagedInviteListResource newUserStagedInvites,
                                           @PathVariable Long competitionId) {
        return competitionInviteService.inviteNewUsers(newUserStagedInvites.getInvites(), competitionId).toPostWithBodyResponse();
    }

    @DeleteMapping("/deleteInvite")
    public RestResult<Void> deleteInvite(@RequestParam String email, @RequestParam Long competitionId) {
        return competitionInviteService.deleteInvite(email, competitionId).toDeleteResponse();
    }

    @PostMapping("/sendInvite/{inviteId}")
    public RestResult<Void> sendInvite(@PathVariable long inviteId, @RequestBody AssessorInviteSendResource assessorInviteSendResource) {
        return competitionInviteService.sendInvite(inviteId, assessorInviteSendResource).toPostWithBodyResponse();
    }

    @PostMapping("/resendInvite/{inviteId}")
    public RestResult<Void> resendInvite(@PathVariable long inviteId, @RequestBody AssessorInviteSendResource assessorInviteSendResource) {
        return competitionInviteService.resendInvite(inviteId, assessorInviteSendResource).toPostWithBodyResponse();
    }
}
