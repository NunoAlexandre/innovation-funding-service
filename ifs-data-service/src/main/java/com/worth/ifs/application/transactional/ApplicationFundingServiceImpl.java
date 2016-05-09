package com.worth.ifs.application.transactional;

import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.notifications.resource.Notification;
import com.worth.ifs.notifications.resource.NotificationTarget;
import com.worth.ifs.notifications.resource.SystemNotificationSource;
import com.worth.ifs.notifications.resource.UserNotificationTarget;
import com.worth.ifs.notifications.service.NotificationService;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.util.EntityLookupCallbacks;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.application.constant.ApplicationStatusConstants.APPROVED;
import static com.worth.ifs.application.constant.ApplicationStatusConstants.REJECTED;
import static com.worth.ifs.application.resource.FundingDecision.FUNDED;
import static com.worth.ifs.application.resource.FundingDecision.NOT_FUNDED;
import static com.worth.ifs.application.transactional.ApplicationFundingServiceImpl.Notifications.FUNDED_APPLICATION;
import static com.worth.ifs.application.transactional.ApplicationFundingServiceImpl.Notifications.UNFUNDED_APPLICATION;
import static com.worth.ifs.commons.error.CommonErrors.badRequestError;
import static com.worth.ifs.commons.error.CommonErrors.internalServerErrorError;
import static com.worth.ifs.commons.service.ServiceResult.*;
import static com.worth.ifs.notifications.resource.NotificationMedium.EMAIL;
import static com.worth.ifs.user.domain.UserRoleType.LEADAPPLICANT;
import static com.worth.ifs.util.CollectionFunctions.*;
import static com.worth.ifs.util.MapFunctions.toListOfPairs;
import static java.util.Arrays.asList;

@Service
class ApplicationFundingServiceImpl extends BaseTransactionalService implements ApplicationFundingService {

	@Autowired
	private NotificationService notificationService;

    @Autowired
    private ApplicationRepository applicationRepository;

	@Autowired
	private SystemNotificationSource systemNotificationSource;

	enum Notifications {
        FUNDED_APPLICATION,
        UNFUNDED_APPLICATION,
    }

	@Override
	public ServiceResult<Void> makeFundingDecision(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {
		
		List<Application> applicationsForCompetition = applicationRepository.findByCompetitionId(competitionId);
		
		boolean allPresent = applicationsForCompetition.stream().noneMatch(app -> !applicationFundingDecisions.containsKey(app.getId()));
		
		if(!allPresent) {
			return serviceFailure(badRequestError("not all applications represented in funding decision"));
		}
		
		applicationsForCompetition.forEach(app -> {
			FundingDecision applicationFundingDecision = applicationFundingDecisions.get(app.getId());
			ApplicationStatus status = statusFromDecision(applicationFundingDecision);
			app.setApplicationStatus(status);
			applicationRepository.save(app);
		});
		
		return serviceSuccess();
	}

	@Override
	public ServiceResult<Void> notifyLeadApplicantsOfFundingDecisions(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {

		List<Pair<Long, FundingDecision>> decisions = toListOfPairs(applicationFundingDecisions);
		List<Pair<Long, FundingDecision>> fundedApplicationDecisions = simpleFilter(decisions, decision -> FUNDED.equals(decision.getValue()));
		List<Pair<Long, FundingDecision>> unfundedApplicationDecisions = simpleFilter(decisions, decision -> NOT_FUNDED.equals(decision.getValue()));
        List<Long> fundedApplicationIds = simpleMap(fundedApplicationDecisions, Pair::getKey);
        List<Long> unfundedApplicationIds = simpleMap(unfundedApplicationDecisions, Pair::getKey);

		List<ServiceResult<Pair<Long, NotificationTarget>>> fundedNotificationTargets = getLeadApplicantNotificationSources(fundedApplicationIds);
        List<ServiceResult<Pair<Long, NotificationTarget>>> unfundedNotificationTargets = getLeadApplicantNotificationSources(unfundedApplicationIds);

        ServiceResult<List<Pair<Long, NotificationTarget>>> aggregatedFundedTargets = aggregate(fundedNotificationTargets);
        ServiceResult<List<Pair<Long, NotificationTarget>>> aggregatedUnfundedTargets = aggregate(unfundedNotificationTargets);

        if (aggregatedFundedTargets.isSuccess() && aggregatedUnfundedTargets.isSuccess()) {

            ServiceResult<Notification> fundedNotification = createFundingDecisionNotification(competitionId, aggregatedFundedTargets.getSuccessObject(), FUNDED_APPLICATION);
            ServiceResult<Void> fundedEmailSendResult = fundedNotification.andOnSuccessReturnVoid(
                    notification -> notificationService.sendNotification(notification, EMAIL));

            ServiceResult<Notification> unfundedNotification = createFundingDecisionNotification(competitionId, aggregatedUnfundedTargets.getSuccessObject(), UNFUNDED_APPLICATION);
            ServiceResult<Void> unfundedEmailSendResult = unfundedNotification.andOnSuccessReturnVoid(
                    notification -> notificationService.sendNotification(notification, EMAIL));

            List<ServiceResult<Void>> allEmailResults = asList(fundedEmailSendResult, unfundedEmailSendResult);
            return processAnyFailuresOrSucceed(allEmailResults, serviceSuccess());
        } else {
            return serviceFailure(internalServerErrorError("Unable to determine all Notification targets for funding decision emails"));
        }
	}

	private ServiceResult<Notification> createFundingDecisionNotification(Long competitionId, List<Pair<Long, NotificationTarget>> notificationTargetsByApplicationId, Notifications notificationType) {

        return getCompetition(competitionId).andOnSuccessReturn(competition -> {

            Map<String, Object> globalArguments = new HashMap<>();
            globalArguments.put("competitionName", competition.getName());
            globalArguments.put("dashboardUrl", "#");
            globalArguments.put("feedbackDate", "01/02/2017");

            List<Pair<NotificationTarget, Map<String, Object>>> notificationTargetSpecificArgumentList = simpleMap(notificationTargetsByApplicationId, pair -> {

                Long applicationId = pair.getKey();
                Application application = applicationRepository.findOne(applicationId);

                Map<String, Object> perNotificationTargetArguments = new HashMap<>();
                perNotificationTargetArguments.put("applicationName", application.getName());
                return Pair.of(pair.getValue(), perNotificationTargetArguments);
            });

            List<NotificationTarget> notificationTargets = simpleMap(notificationTargetsByApplicationId, Pair::getValue);
            Map<NotificationTarget, Map<String, Object>> notificationTargetSpecificArguments = pairsToMap(notificationTargetSpecificArgumentList);
            return new Notification(systemNotificationSource, notificationTargets, notificationType, globalArguments, notificationTargetSpecificArguments);
        });
	}

	private List<ServiceResult<Pair<Long, NotificationTarget>>> getLeadApplicantNotificationSources(List<Long> applicationIds) {
		return simpleMap(applicationIds, applicationId -> {
			ServiceResult<ProcessRole> leadApplicantResult = getProcessRoles(applicationId, LEADAPPLICANT).andOnSuccess(EntityLookupCallbacks::getOnlyElementOrFail);
			return leadApplicantResult.andOnSuccessReturn(leadApplicant -> Pair.of(applicationId, new UserNotificationTarget(leadApplicant.getUser())));
		});
	}

	private ApplicationStatus statusFromDecision(FundingDecision applicationFundingDecision) {
		if(FUNDED.equals(applicationFundingDecision)) {
			return statusFromConstant(APPROVED);
		} else {
			return statusFromConstant(REJECTED);
		}
	}

	private ApplicationStatus statusFromConstant(ApplicationStatusConstants applicationStatusConstant) {
		return applicationStatusRepository.findOne(applicationStatusConstant.getId());
	}
	
}
