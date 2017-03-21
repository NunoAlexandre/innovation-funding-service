package org.innovateuk.ifs.application.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatus;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.application.constant.ApplicationStatusConstants.*;
import static org.innovateuk.ifs.application.resource.FundingDecision.FUNDED;
import static org.innovateuk.ifs.application.resource.FundingDecision.UNFUNDED;
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

    @Override
    public ServiceResult<Void> saveFundingDecisionData(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {
        return getCompetition(competitionId).andOnSuccess(competition -> {
            List<Application> allowedApplicationForCompetition = findAllowedApplicationsForCompetition(competitionId);

            return saveFundingDecisionData(allowedApplicationForCompetition, applicationFundingDecisions);
        });
    }


    @Override
    public ServiceResult<Void> notifyLeadApplicantsOfFundingDecisions(NotificationResource notificationResource) {

        setApplicationStatus(notificationResource.getFundingDecisions());

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

    private void setApplicationStatus(Map<Long, FundingDecision> applicationFundingDecisions) {

        List<Long> applicationIds = new ArrayList<>(applicationFundingDecisions.keySet());
        List<Application> applications = findApplicationsByIds(applicationIds);

        applications.forEach(app -> {
            FundingDecision applicationFundingDecision = applicationFundingDecisions.get(app.getId());
            ApplicationStatus status = statusFromDecision(applicationFundingDecision);
            app.setApplicationStatus(status);
        });
    }

    private List<Application> findApplicationsByIds(List<Long> applicationIds) {
        return (List) applicationRepository.findAll(applicationIds);
    }

    private List<Application> findAllowedApplicationsForCompetition(Long competitionId) {
        List<Application> applicationsInCompetition = applicationRepository.findByCompetitionId(competitionId);

        List<Application> allowedApplications = applicationsInCompetition.stream()
                .filter(application -> applicationHasBeenSubmitted(application))
                .filter(application -> !applicationDecisionIsSuccessfulAndNotified(application))
                .collect(Collectors.toList());

        return allowedApplications;
    }

    private boolean applicationHasBeenSubmitted(Application application) {
        List<Long> submittedApplicationStatusIds = new ArrayList<>();
        submittedApplicationStatusIds.add(ApplicationStatusConstants.SUBMITTED.getId());
        submittedApplicationStatusIds.add(ApplicationStatusConstants.REJECTED.getId());
        submittedApplicationStatusIds.add(ApplicationStatusConstants.APPROVED.getId());

        boolean hasBeenSubmitted = submittedApplicationStatusIds.contains(application.getApplicationStatus().getId());;

        return hasBeenSubmitted;
    }

    private boolean applicationDecisionIsSuccessfulAndNotified(Application application) {
        boolean isSuccessful = application.getFundingDecision() != null && application.getFundingDecision().equals(FundingDecision.FUNDED);
        boolean isNotified = application.getManageFundingEmailDate() != null;

        return isSuccessful && isNotified;
    }

    private ServiceResult<Void> saveFundingDecisionData(List<Application> applicationsForCompetition, Map<Long, FundingDecision> applicationDecisions) {
        applicationDecisions.forEach((applicationId, decisionValue) -> {
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

    private ApplicationStatus statusFromDecision(FundingDecision applicationFundingDecision) {
        if (FUNDED.equals(applicationFundingDecision)) {
            return applicationStatusRepository.findOne(APPROVED.getId());
        } else if (UNFUNDED.equals(applicationFundingDecision)) {
            return applicationStatusRepository.findOne(REJECTED.getId());
        } else {
            return applicationStatusRepository.findOne(SUBMITTED.getId());
        }
    }
}
