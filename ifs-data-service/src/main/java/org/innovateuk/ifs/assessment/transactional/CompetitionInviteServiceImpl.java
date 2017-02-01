package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.assessment.mapper.AssessorInviteToSendMapper;
import org.innovateuk.ifs.assessment.mapper.CompetitionInviteMapper;
import org.innovateuk.ifs.assessment.mapper.CompetitionInviteStatisticsMapper;
import org.innovateuk.ifs.category.domain.Category;
import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.category.mapper.InnovationAreaMapper;
import org.innovateuk.ifs.category.repository.InnovationAreaRepository;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.CompetitionInvite;
import org.innovateuk.ifs.invite.domain.CompetitionInviteStatistics;
import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.RejectionReason;
import org.innovateuk.ifs.invite.mapper.ParticipantStatusMapper;
import org.innovateuk.ifs.invite.repository.CompetitionInviteRepository;
import org.innovateuk.ifs.invite.repository.CompetitionInviteStatisticsRepository;
import org.innovateuk.ifs.invite.repository.CompetitionParticipantRepository;
import org.innovateuk.ifs.invite.repository.RejectionReasonRepository;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.service.senders.NotificationSender;
import org.innovateuk.ifs.user.domain.Profile;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.ProfileRepository;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.innovateuk.ifs.category.resource.CategoryType.INNOVATION_AREA;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.*;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.domain.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.REJECTED;
import static org.innovateuk.ifs.util.CollectionFunctions.mapWithIndex;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.innovateuk.ifs.util.MapFunctions.asMap;

/**
 * Service for managing {@link org.innovateuk.ifs.invite.domain.CompetitionInvite}s.
 */
@Service
@Transactional
public class CompetitionInviteServiceImpl implements CompetitionInviteService {

    private static final String WEB_CONTEXT = "/assessment";

    @Autowired
    private CompetitionInviteRepository competitionInviteRepository;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private RejectionReasonRepository rejectionReasonRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private InnovationAreaRepository innovationAreaRepository;

    @Autowired
    private CompetitionInviteStatisticsRepository competitionInviteStatisticsRepository;

    @Autowired
    private CompetitionInviteMapper competitionInviteMapper;

    @Autowired
    private InnovationAreaMapper innovationAreaMapper;

    @Autowired
    private AssessorInviteToSendMapper toSendMapper;

    @Autowired
    private ParticipantStatusMapper participantStatusMapper;

    @Autowired
    private CompetitionInviteStatisticsMapper competitionInviteStatisticsMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private NotificationSender notificationSender;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        INVITE_ASSESSOR
    }

    @Override
    public ServiceResult<AssessorInviteToSendResource> getCreatedInvite(long inviteId) {
        return getById(inviteId).andOnSuccess(invite -> {
            if (invite.getStatus() != CREATED) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_ALREADY_SENT, invite.getTarget().getName()));
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
            Notification notification = new Notification(systemNotificationSource, singletonList(recipient), Notifications.INVITE_ASSESSOR,
                    asMap("name", invite.getName(),
                            "competitionName", invite.getTarget().getName(),
                            "innovationArea", invite.getInnovationArea(),
                            "acceptsDate", invite.getTarget().getAssessorAcceptsDate().format(formatter),
                            "deadlineDate", invite.getTarget().getAssessorDeadlineDate().format(formatter),
                            "inviteUrl", format("%s/invite/competition/%s", webBaseUrl + WEB_CONTEXT, invite.getHash())));
            EmailContent content = notificationSender.renderTemplates(notification).getSuccessObject().get(recipient);
            AssessorInviteToSendResource resource = toSendMapper.mapToResource(invite);
            resource.setEmailContent(content);
            return serviceSuccess(resource);
        });
    }

    @Override
    public ServiceResult<CompetitionInviteResource> getInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<CompetitionInviteResource> openInvite(String inviteHash) {
        return getByHashIfOpen(inviteHash)
                .andOnSuccessReturn(this::openInvite)
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    @Override
    public ServiceResult<Void> acceptInvite(String inviteHash, UserResource currentUser) {
        final User user = userRepository.findOne(currentUser.getId());
        return getParticipantByInviteHash(inviteHash)
                .andOnSuccess(p -> accept(p, user))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Void> rejectInvite(String inviteHash, RejectionReasonResource rejectionReason, Optional<String> rejectionComment) {
        return getRejectionReason(rejectionReason)
                .andOnSuccess(reason -> getParticipantByInviteHash(inviteHash)
                        .andOnSuccess(invite -> reject(invite, reason, rejectionComment)))
                .andOnSuccessReturnVoid();
    }

    @Override
    public ServiceResult<Boolean> checkExistingUser(@P("inviteHash") String inviteHash) {
        return getByHash(inviteHash).andOnSuccessReturn(invite -> {
            if (invite.getUser() != null) {
                return TRUE;
            }

            return userRepository.findByEmail(invite.getEmail()).isPresent();
        });
    }

    @Override
    public ServiceResult<List<AvailableAssessorResource>> getAvailableAssessors(long competitionId) {
        List<User> assessors = userRepository.findAllAvailableAssessorsByCompetition(competitionId);

        return serviceSuccess(assessors.stream()
                .map(assessor -> {
                    AvailableAssessorResource availableAssessor = new AvailableAssessorResource();
                    availableAssessor.setId(assessor.getId());
                    availableAssessor.setEmail(assessor.getEmail());
                    availableAssessor.setName(assessor.getName());
                    availableAssessor.setBusinessType(getBusinessType(assessor));
                    Profile profile = profileRepository.findOne(assessor.getProfileId());
                    availableAssessor.setCompliant(profile.isCompliant(assessor));
                    availableAssessor.setAdded(wasInviteCreated(assessor.getEmail(), competitionId));
                    availableAssessor.setInnovationAreas(simpleMap(profile.getInnovationAreas(), innovationAreaMapper::mapToResource));
                    return availableAssessor;
                }).collect(toList()));
    }

    @Override
    public ServiceResult<List<AssessorCreatedInviteResource>> getCreatedInvites(long competitionId) {
        return serviceSuccess(simpleMap(competitionInviteRepository.getByCompetitionIdAndStatus(competitionId, CREATED), competitionInvite ->
                new AssessorCreatedInviteResource(competitionInvite.getName(), getInnovationAreasForInvite(competitionInvite), isUserCompliant(competitionInvite), competitionInvite.getEmail(), competitionInvite.getId())));
    }

    @Override
    public ServiceResult<CompetitionInviteStatisticsResource> getInviteStatistics(long competitionId) {
        return find(competitionInviteStatisticsRepository.findOne(competitionId), notFoundError(CompetitionInviteStatistics.class, competitionId))
                .andOnSuccessReturn(competitionInviteStatisticsMapper::mapToResource);

    }

    @Override
    public ServiceResult<List<AssessorInviteOverviewResource>> getInvitationOverview(long competitionId) {
        return serviceSuccess(simpleMap(competitionParticipantRepository.getByCompetitionIdAndRole(competitionId, ASSESSOR),
                participant -> {
                    AssessorInviteOverviewResource assessorInviteOverview = new AssessorInviteOverviewResource();
                    assessorInviteOverview.setName(participant.getInvite().getName());
                    assessorInviteOverview.setStatus(participantStatusMapper.mapToResource(participant.getStatus()));
                    assessorInviteOverview.setDetails(getDetails(participant));

                    if (participant.getUser() != null) {
                        assessorInviteOverview.setBusinessType(getBusinessType(participant.getUser()));
                        Profile profile = profileRepository.findOne(participant.getUser().getProfileId());
                        assessorInviteOverview.setCompliant(profile.isCompliant(participant.getUser()));
                        assessorInviteOverview.setInnovationAreas(simpleMap(profile.getInnovationAreas(), innovationAreaMapper::mapToResource));
                    } else {
                        assessorInviteOverview.setInnovationAreas(asList(innovationAreaMapper.mapToResource(participant.getInvite().getInnovationArea())));
                    }

                    return assessorInviteOverview;
                }));
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(NewUserStagedInviteResource stagedInvite) {
        return getByEmailAndCompetition(stagedInvite.getEmail(), stagedInvite.getCompetitionId()).handleSuccessOrFailure(
                failure -> getCompetition(stagedInvite.getCompetitionId())
                        .andOnSuccess(competition -> getInnovationArea(stagedInvite.getInnovationCategoryId())
                                .andOnSuccess(innovationArea ->
                                        inviteUserToCompetition(
                                                stagedInvite.getName(),
                                                stagedInvite.getEmail(),
                                                competition,
                                                innovationArea
                                        )
                                )
                        )
                        .andOnSuccessReturn(competitionInviteMapper::mapToResource),
                success -> serviceFailure(Error.globalError(
                        "validation.competitionInvite.create.email.exists",
                        singletonList(stagedInvite.getEmail())
                ))
        );
    }

    @Override
    public ServiceResult<Void> inviteNewUsers(List<NewUserStagedInviteResource> newUserStagedInvites, long competitionId) {
        return getCompetition(competitionId).andOnSuccessReturn(competition ->
                mapWithIndex(newUserStagedInvites, (index, invite) ->
                        getByEmailAndCompetition(invite.getEmail(), competitionId).handleSuccessOrFailure(
                                failure -> getInnovationArea(invite.getInnovationCategoryId())
                                        .andOnSuccess(innovationArea ->
                                                inviteUserToCompetition(invite.getName(), invite.getEmail(), competition, innovationArea)
                                        )
                                        .andOnFailure(() -> serviceFailure(Error.fieldError(
                                                "invites[" + index + "].innovationArea",
                                                invite.getInnovationCategoryId(),
                                                "validation.competitionInvite.create.innovationArea.required"
                                                ))
                                        ),
                                success -> serviceFailure(Error.fieldError(
                                        "invites[" + index + "].email",
                                        invite.getEmail(),
                                        "validation.competitionInvite.create.email.exists"
                                ))
                        )
                ))
                .andOnSuccess(list -> aggregate(list))
                .andOnSuccessReturnVoid();
    }

    private String getDetails(CompetitionParticipant participant) {
        String details = null;

        if (participant.getStatus() == REJECTED) {
            details = format("Invite declined as %s", lowerCase(participant.getRejectionReason().getReason()));
        }

        return details;
    }

    private BusinessType getBusinessType(User assessor) {
        Profile profile = profileRepository.findOne(assessor.getProfileId());
        return (profile != null) ? profile.getBusinessType() : null;
    }

    private boolean wasInviteCreated(String email, long competitionId) {
        ServiceResult<CompetitionInvite> result = getByEmailAndCompetition(email, competitionId);
        return result.isSuccess() ? result.getSuccessObject().getStatus() == CREATED : FALSE;
    }

    private ServiceResult<InnovationArea> getInnovationArea(long innovationCategoryId) {
        return find( innovationAreaRepository.findOne(innovationCategoryId), notFoundError(Category.class, innovationCategoryId, INNOVATION_AREA));
    }

    private ServiceResult<CompetitionInvite> inviteUserToCompetition(String name, String email, Competition competition, InnovationArea innovationArea) {
        return serviceSuccess(
                competitionInviteRepository.save(new CompetitionInvite(name, email, generateInviteHash(), competition, innovationArea))
        );
    }

    @Override
    public ServiceResult<CompetitionInviteResource> inviteUser(ExistingUserStagedInviteResource stagedInvite) {
        return getUserByEmail(stagedInvite.getEmail()) // I'm not particularly tied to finding by email, vs id
                .andOnSuccess(user -> inviteUserToCompetition(user, stagedInvite.getCompetitionId()))
                .andOnSuccessReturn(competitionInviteMapper::mapToResource);
    }

    private ServiceResult<CompetitionInvite> inviteUserToCompetition(User user, long competitionId) {
        return getCompetition(competitionId)
                .andOnSuccessReturn(
                        competition -> competitionInviteRepository.save(new CompetitionInvite(user, generateInviteHash(), competition))
                );
    }

    private ServiceResult<Competition> getCompetition(long competitionId) {
        return find(competitionRepository.findOne(competitionId), notFoundError(Competition.class, competitionId));
    }

    private ServiceResult<User> getUserByEmail(String email) {
        return find(userRepository.findByEmail(email), notFoundError(User.class, email));
    }

    @Override
    public ServiceResult<AssessorInviteToSendResource> sendInvite(long inviteId, EmailContent content) {
        return getById(inviteId).andOnSuccessReturn(invite -> sendInvite(invite, content)).andOnSuccessReturn(toSendMapper::mapToResource);
    }

    private CompetitionInvite sendInvite(CompetitionInvite invite, EmailContent content) {
        competitionParticipantRepository.save(new CompetitionParticipant(invite.send()));

        NotificationTarget recipient = new ExternalUserNotificationTarget(invite.getName(), invite.getEmail());
        Notification notification = new Notification(systemNotificationSource, singletonList(recipient), Notifications.INVITE_ASSESSOR, emptyMap());
        notificationSender.sendEmailWithContent(notification, recipient, content);

        return invite;
    }

    @Override
    public ServiceResult<Void> deleteInvite(String email, long competitionId) {
        return getByEmailAndCompetition(email, competitionId).andOnSuccess(this::deleteInvite);
    }

    private ServiceResult<CompetitionInvite> getByHash(String inviteHash) {
        return find(competitionInviteRepository.getByHash(inviteHash), notFoundError(CompetitionInvite.class, inviteHash));
    }

    private ServiceResult<CompetitionInvite> getById(long id) {
        return find(competitionInviteRepository.findOne(id), notFoundError(CompetitionInvite.class, id));
    }

    private ServiceResult<CompetitionInvite> getByEmailAndCompetition(String email, long competitionId) {
        return find(competitionInviteRepository.getByEmailAndCompetitionId(email, competitionId), notFoundError(CompetitionInvite.class, email, competitionId));
    }

    private ServiceResult<Void> deleteInvite(CompetitionInvite invite) {
        if (invite.getStatus() != CREATED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CANNOT_DELETE_ONCE_SENT, invite.getEmail()));
        }

        competitionInviteRepository.delete(invite);
        return serviceSuccess();
    }

    private ServiceResult<CompetitionInvite> getByHashIfOpen(String inviteHash) {
        return getByHash(inviteHash).andOnSuccess(invite -> {

            if (!EnumSet.of(READY_TO_OPEN, IN_ASSESSMENT, CLOSED, OPEN).contains(invite.getTarget().getCompetitionStatus())) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_EXPIRED, invite.getTarget().getName()));
            }

            CompetitionParticipant participant = competitionParticipantRepository.getByInviteHash(inviteHash);

            if (participant == null) {
                return serviceSuccess(invite);
            }

            if (participant.getStatus() == ACCEPTED || participant.getStatus() == REJECTED) {
                return ServiceResult.serviceFailure(new Error(COMPETITION_INVITE_CLOSED, invite.getTarget().getName()));
            }

            return serviceSuccess(invite);
        });
    }

    private CompetitionInvite openInvite(CompetitionInvite invite) {
        return competitionInviteRepository.save(invite.open());
    }

    private ServiceResult<CompetitionParticipant> getParticipantByInviteHash(String inviteHash) {
        return find(competitionParticipantRepository.getByInviteHash(inviteHash), notFoundError(CompetitionParticipant.class, inviteHash));
    }

    private ServiceResult<List<CompetitionParticipant>> getParticipantsByCompetition(long competitionId) {
        return find(competitionParticipantRepository.getByCompetitionIdAndRole(competitionId, ASSESSOR), notFoundError(CompetitionParticipant.class, competitionId));
    }

    private ServiceResult<CompetitionParticipant> accept(CompetitionParticipant participant, User user) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return serviceSuccess(participant.acceptAndAssignUser(user));
        }
    }

    private ServiceResult<CompetitionParticipant> reject(CompetitionParticipant participant, RejectionReason rejectionReason, Optional<String> rejectionComment) {
        if (participant.getInvite().getStatus() != OPENED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == ACCEPTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE, getInviteCompetitionName(participant)));
        } else if (participant.getStatus() == REJECTED) {
            return ServiceResult.serviceFailure(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_REJECTED_INVITE, getInviteCompetitionName(participant)));
        } else {
            return serviceSuccess(participant.reject(rejectionReason, rejectionComment));
        }
    }

    private ServiceResult<RejectionReason> getRejectionReason(final RejectionReasonResource rejectionReason) {
        return find(rejectionReasonRepository.findOne(rejectionReason.getId()), notFoundError(RejectionReason.class, rejectionReason.getId()));
    }

    private String getInviteCompetitionName(CompetitionParticipant participant) {
        return participant.getInvite().getTarget().getName();
    }

    private boolean isUserCompliant(CompetitionInvite competitionInvite) {
        if (competitionInvite == null || competitionInvite.getUser() == null) {
            return false;
        }
        Profile profile = profileRepository.findOne(competitionInvite.getUser().getProfileId());
        return profile.isCompliant(competitionInvite.getUser());
    }

    private List<InnovationAreaResource> getInnovationAreasForInvite(CompetitionInvite competitionInvite) {
        boolean inviteForNewUser = competitionInvite.getUser() == null;

        if (inviteForNewUser) {
            return asList(innovationAreaMapper.mapToResource(competitionInvite.getInnovationArea()));
        } else {
            return profileRepository.findOne(competitionInvite.getUser().getProfileId()).getInnovationAreas().stream()
                    .map(innovationAreaMapper::mapToResource)
                    .collect(toList());
        }
    }
}
