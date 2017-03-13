package org.innovateuk.ifs.application.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.FundingDecisionStatus;
import org.innovateuk.ifs.application.mapper.FundingDecisionMapper;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.NotificationResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.util.EntityLookupCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.innovateuk.ifs.application.transactional.ApplicationFundingServiceImpl.Notifications.APPLICATION_FUNDING;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.NOTIFICATIONS_UNABLE_TO_DETERMINE_NOTIFICATION_TARGETS;
import static org.innovateuk.ifs.commons.service.ServiceResult.*;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

@Service
class ApplicationFundingServiceImpl extends BaseTransactionalService implements ApplicationFundingService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private FundingDecisionMapper fundingDecisionMapper;

    @Autowired
    private ApplicationService applicationService;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        APPLICATION_FUNDED,
        APPLICATION_NOT_FUNDED,
        APPLICATION_FUNDING,
    }

    private static final Log LOG = LogFactory.getLog(ApplicationFundingServiceImpl.class);

    @Override
    public ServiceResult<Void> saveFundingDecisionData(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {
        return getCompetition(competitionId).andOnSuccess(competition -> {
            List<Application> applicationsForCompetition = findSubmittedApplicationsForCompetition(competitionId);

            return saveFundingDecisionData(applicationsForCompetition, applicationFundingDecisions);
        });
    }

    @Override
    public ServiceResult<Void> notifyLeadApplicantsOfFundingDecisions(NotificationResource notificationResource) {

        List<ServiceResult<Pair<Long, NotificationTarget>>> fundingNotificationTargets = getLeadApplicantNotificationTargets(notificationResource.calculateApplicationIds());
        ServiceResult<List<Pair<Long, NotificationTarget>>> aggregatedFundingTargets = aggregate(fundingNotificationTargets);

        return aggregatedFundingTargets.handleSuccessOrFailure(
                failure -> serviceFailure(NOTIFICATIONS_UNABLE_TO_DETERMINE_NOTIFICATION_TARGETS),
                success -> {

                    Notification fundingNotification = createFundingDecisionNotification(notificationResource, aggregatedFundingTargets.getSuccessObject(), APPLICATION_FUNDING);
                    ServiceResult<Void> fundedEmailSendResult = notificationService.sendNotification(fundingNotification, EMAIL);

                    return fundedEmailSendResult.andOnSuccess(() ->
                            aggregate(simpleMap(
                                    notificationResource.calculateApplicationIds(), applicationId ->
                                            applicationService.setApplicationFundingEmailDateTime(applicationId, LocalDateTime.now()))))
                            .andOnSuccessReturnVoid();
                });
    }

    private List<Application> findSubmittedApplicationsForCompetition(Long competitionId) {
        return applicationRepository.findByCompetitionIdAndApplicationStatusId(competitionId, ApplicationStatusConstants.SUBMITTED.getId());
    }

    private ServiceResult<Void> saveFundingDecisionData(List<Application> applicationsForCompetition, Map<Long, FundingDecision> decision) {
        decision.forEach((applicationId, decisionValue) -> {
            Optional<Application> applicationForDecision = applicationsForCompetition.stream().filter(application -> applicationId.equals(application.getId())).findFirst();
            if (applicationForDecision.isPresent()) {
                Application application = applicationForDecision.get();
                FundingDecisionStatus fundingDecision = fundingDecisionMapper.mapToDomain(decisionValue);
                application.setFundingDecision(fundingDecision);
            }
        });

        return serviceSuccess();
    }

    private Notification createFundingDecisionNotification(NotificationResource notificationResource, List<Pair<Long, NotificationTarget>> notificationTargetsByApplicationId, Notifications notificationType) {

        Map<String, Object> globalArguments = new HashMap<>();
        globalArguments.put("subject", notificationResource.getSubject());
        globalArguments.put("message",  notificationResource.getMessageBody());

        List<NotificationTarget> notificationTargets = simpleMap(notificationTargetsByApplicationId, Pair::getValue);
        return new Notification(systemNotificationSource, notificationTargets, notificationType, globalArguments);
    }

    private List<ServiceResult<Pair<Long, NotificationTarget>>> getLeadApplicantNotificationTargets(List<Long> applicationIds) {
        return simpleMap(applicationIds, applicationId -> {
            ServiceResult<ProcessRole> leadApplicantResult = getProcessRoles(applicationId, LEADAPPLICANT).andOnSuccess(EntityLookupCallbacks::getOnlyElementOrFail);
            return leadApplicantResult.andOnSuccessReturn(leadApplicant -> Pair.of(applicationId, new UserNotificationTarget(leadApplicant.getUser())));
        });
    }

}
