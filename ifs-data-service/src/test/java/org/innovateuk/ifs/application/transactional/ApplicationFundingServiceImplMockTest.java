package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.builder.ApplicationStatusBuilder;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatus;
import org.innovateuk.ifs.application.domain.FundingDecisionStatus;
import org.innovateuk.ifs.application.mapper.FundingDecisionMapper;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.NotificationResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.util.MapFunctions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.LambdaMatcher.createLambdaMatcher;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.constant.ApplicationStatusConstants.APPROVED;
import static org.innovateuk.ifs.application.constant.ApplicationStatusConstants.REJECTED;
import static org.innovateuk.ifs.application.constant.ApplicationStatusConstants.SUBMITTED;
import static org.innovateuk.ifs.application.resource.FundingDecision.UNDECIDED;
import static org.innovateuk.ifs.application.transactional.ApplicationFundingServiceImpl.Notifications.APPLICATION_FUNDING;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ApplicationFundingServiceImplMockTest extends BaseServiceUnitTest<ApplicationFundingService> {

    private static final String webBaseUrl = "http://ifs-local-dev";

    @Mock
    private FundingDecisionMapper fundingDecisionMapper;
    
    private ApplicationStatus approvedStatus;
    private ApplicationStatus rejectedStatus;
    private ApplicationStatus openStatus;
    
    private Competition competition;
    
    @Override
    protected ApplicationFundingService supplyServiceUnderTest() {
        ApplicationFundingServiceImpl service = new ApplicationFundingServiceImpl();
        ReflectionTestUtils.setField(service, "webBaseUrl", webBaseUrl);
        return service;
    }

    @Before
    public void setup() {
    	approvedStatus = ApplicationStatusBuilder.newApplicationStatus().build();
    	rejectedStatus = ApplicationStatusBuilder.newApplicationStatus().build();
    	openStatus = ApplicationStatusBuilder.newApplicationStatus().build();

    	when(applicationStatusRepositoryMock.findOne(ApplicationStatusConstants.APPROVED.getId())).thenReturn(approvedStatus);
    	when(applicationStatusRepositoryMock.findOne(ApplicationStatusConstants.REJECTED.getId())).thenReturn(rejectedStatus);
    	
    	competition = newCompetition().withAssessorFeedbackDate("01/02/2017 17:30:00").withCompetitionStatus(CompetitionStatus.FUNDERS_PANEL).withId(123L).build();
    	when(competitionRepositoryMock.findOne(123L)).thenReturn(competition);
    	
    	when(fundingDecisionMapper.mapToDomain(any(FundingDecision.class))).thenAnswer(new Answer<FundingDecisionStatus>(){
			@Override
			public FundingDecisionStatus answer(InvocationOnMock invocation) throws Throwable {
				return FundingDecisionStatus.valueOf(((FundingDecision)invocation.getArguments()[0]).name());
			}});
    	when(fundingDecisionMapper.mapToResource(any(FundingDecisionStatus.class))).thenAnswer(new Answer<FundingDecision>(){
			@Override
			public FundingDecision answer(InvocationOnMock invocation) throws Throwable {
				return FundingDecision.valueOf(((FundingDecisionStatus)invocation.getArguments()[0]).name());
			}});
    }

    @Test
    public void testNotifyLeadApplicantsOfFundingDecisions() {
        
        Application application1 = newApplication().build();
        Application application2 = newApplication().build();
        Application application3 = newApplication().build();

        User application1LeadApplicant = newUser().build();
        User application2LeadApplicant = newUser().build();
        User application3LeadApplicant = newUser().build();

        Role leadApplicantRole = newRole().with(id(456L)).build();

        List<ProcessRole> leadApplicantProcessRoles = newProcessRole().
                withUser(application1LeadApplicant, application2LeadApplicant, application3LeadApplicant).
                withApplication(application1, application2, application3).
                withRole(leadApplicantRole, leadApplicantRole, leadApplicantRole).
                build(3);

        UserNotificationTarget application1LeadApplicantTarget = new UserNotificationTarget(application1LeadApplicant);
        UserNotificationTarget application2LeadApplicantTarget = new UserNotificationTarget(application2LeadApplicant);
        UserNotificationTarget application3LeadApplicantTarget = new UserNotificationTarget(application3LeadApplicant);
        List<NotificationTarget> expectedLeadApplicants = asList(application1LeadApplicantTarget, application2LeadApplicantTarget, application3LeadApplicantTarget);

        Map<Long, FundingDecision> decisions = MapFunctions.asMap(
                application1.getId(), FundingDecision.FUNDED,
                application2.getId(), FundingDecision.UNFUNDED,
                application3.getId(), FundingDecision.ON_HOLD);

        NotificationResource notificationResource = new NotificationResource("Subject", "The message body.", decisions);
        Map<String, Object> expectedGlobalNotificationArguments = asMap(
                "subject", notificationResource.getSubject(),
                "message", notificationResource.getMessageBody());

        Notification expectedFundingNotification = new Notification(systemNotificationSourceMock, expectedLeadApplicants, APPLICATION_FUNDING, expectedGlobalNotificationArguments);

        List<Long> applicationIds = Arrays.asList(application1.getId(), application2.getId(), application3.getId());
        List<Application> applications = Arrays.asList(application1, application2, application3);
        when(applicationRepositoryMock.findAll(applicationIds)).thenReturn(applications);
        when(applicationStatusRepositoryMock.findOne(APPROVED.getId())).thenReturn(ApplicationStatusBuilder.newApplicationStatus().withName(APPROVED).build());
        when(applicationStatusRepositoryMock.findOne(REJECTED.getId())).thenReturn(ApplicationStatusBuilder.newApplicationStatus().withName(REJECTED).build());
        when(applicationStatusRepositoryMock.findOne(SUBMITTED.getId())).thenReturn(ApplicationStatusBuilder.newApplicationStatus().withName(SUBMITTED).build());

        when(roleRepositoryMock.findOneByName(LEADAPPLICANT.getName())).thenReturn(leadApplicantRole);
        leadApplicantProcessRoles.forEach(processRole ->
                when(processRoleRepositoryMock.findByApplicationIdAndRoleId(processRole.getApplicationId(), processRole.getRole().getId())).thenReturn(singletonList(processRole))
        );
        when(notificationServiceMock.sendNotification(createNotificationExpectationsWithGlobalArgs(expectedFundingNotification), eq(EMAIL))).thenReturn(serviceSuccess());
        when(applicationServiceMock.setApplicationFundingEmailDateTime(any(Long.class), any(LocalDateTime.class))).thenReturn(serviceSuccess(new ApplicationResource()));

        ServiceResult<Void> result = service.notifyLeadApplicantsOfFundingDecisions(notificationResource);
        assertTrue(result.isSuccess());

        verify(notificationServiceMock).sendNotification(createNotificationExpectationsWithGlobalArgs(expectedFundingNotification), eq(EMAIL));
        verifyNoMoreInteractions(notificationServiceMock);

        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application1.getId()), any(LocalDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application2.getId()), any(LocalDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application3.getId()), any(LocalDateTime.class));
        verifyNoMoreInteractions(applicationServiceMock);
    }

    @Test
    public void testNotifyLeadApplicantsOfFundingDecisionsAndJustLeadApplicants() {

        Application application1 = newApplication().build();
        Application application2 = newApplication().build();

        // add some collaborators into the mix - they should not receive Notifications
        User application1LeadApplicant = newUser().build();
        User application1Collaborator = newUser().build();
        User application2LeadApplicant = newUser().build();
        User application2Collaborator = newUser().build();

        Role leadApplicantRole = newRole().with(id(456L)).build();
        Role collaboratorRole = newRole().with(id(789L)).build();

        List<ProcessRole> allProcessRoles = newProcessRole().
                withUser(application1LeadApplicant, application1Collaborator, application2LeadApplicant, application2Collaborator).
                withApplication(application1, application1, application2, application2).
                withRole(leadApplicantRole, collaboratorRole, leadApplicantRole, collaboratorRole).
                build(4);

        UserNotificationTarget application1LeadApplicantTarget = new UserNotificationTarget(application1LeadApplicant);
        UserNotificationTarget application2LeadApplicantTarget = new UserNotificationTarget(application2LeadApplicant);
        List<NotificationTarget> expectedLeadApplicants = asList(application1LeadApplicantTarget, application2LeadApplicantTarget);

        Map<Long, FundingDecision> decisions = MapFunctions.asMap(
                application1.getId(), FundingDecision.FUNDED,
                application2.getId(), FundingDecision.UNFUNDED);
        NotificationResource notificationResource = new NotificationResource("Subject", "The message body.", decisions);

        Notification expectedFundingNotification =
                new Notification(systemNotificationSourceMock, expectedLeadApplicants, APPLICATION_FUNDING, emptyMap());
        
        List<Long> applicationIds = Arrays.asList(application1.getId(), application2.getId());
        List<Application> applications = Arrays.asList(application1, application2);
        when(applicationRepositoryMock.findAll(applicationIds)).thenReturn(applications);
        when(applicationStatusRepositoryMock.findOne(APPROVED.getId())).thenReturn(ApplicationStatusBuilder.newApplicationStatus().withName(APPROVED).build());
        when(applicationStatusRepositoryMock.findOne(REJECTED.getId())).thenReturn(ApplicationStatusBuilder.newApplicationStatus().withName(REJECTED).build());

        asList(application1, application2).forEach(application ->
                when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application)
        );

        when(roleRepositoryMock.findOneByName(LEADAPPLICANT.getName())).thenReturn(leadApplicantRole);

        allProcessRoles.forEach(processRole ->
                when(processRoleRepositoryMock.findByApplicationIdAndRoleId(processRole.getApplicationId(), processRole.getRole().getId())).thenReturn(singletonList(processRole))
        );

        when(notificationServiceMock.sendNotification(createSimpleNotificationExpectations(expectedFundingNotification), eq(EMAIL))).thenReturn(serviceSuccess());
        when(applicationServiceMock.setApplicationFundingEmailDateTime(any(Long.class), any(LocalDateTime.class))).thenReturn(serviceSuccess(new ApplicationResource()));

        ServiceResult<Void> result = service.notifyLeadApplicantsOfFundingDecisions(notificationResource);
        assertTrue(result.isSuccess());

        verify(notificationServiceMock).sendNotification(createSimpleNotificationExpectations(expectedFundingNotification), eq(EMAIL));
        verifyNoMoreInteractions(notificationServiceMock);

        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application1.getId()), any(LocalDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application2.getId()), any(LocalDateTime.class));
        verifyNoMoreInteractions(applicationServiceMock);
    }
    
    @Test
    public void testSaveFundingDecisionData() {
    	
    	Application application1 = newApplication().withId(1L).withCompetition(competition).withFundingDecision(FundingDecisionStatus.FUNDED).withApplicationStatus(openStatus).build();
     	Application application2 = newApplication().withId(2L).withCompetition(competition).withFundingDecision(FundingDecisionStatus.UNFUNDED).withApplicationStatus(openStatus).build();
    	when(applicationRepositoryMock.findByCompetitionIdAndApplicationStatusId(competition.getId(), ApplicationStatusConstants.SUBMITTED.getId())).thenReturn(Arrays.asList(application1, application2));

    	Map<Long, FundingDecision> decision = asMap(1L, UNDECIDED);
    	
    	ServiceResult<Void> result = service.saveFundingDecisionData(competition.getId(), decision);
    	
    	assertTrue(result.isSuccess());
    	verify(applicationRepositoryMock).findByCompetitionIdAndApplicationStatusId(competition.getId(), ApplicationStatusConstants.SUBMITTED.getId());
    	assertEquals(openStatus, application1.getApplicationStatus());
    	assertEquals(openStatus, application2.getApplicationStatus());
    	assertEquals(FundingDecisionStatus.UNDECIDED, application1.getFundingDecision());
    	assertEquals(FundingDecisionStatus.UNFUNDED, application2.getFundingDecision());
    	assertNull(competition.getFundersPanelEndDate());
    }

    public static Notification createNotificationExpectationsWithGlobalArgs(Notification expectedNotification) {

        return createLambdaMatcher(notification -> {
            assertEquals(expectedNotification.getFrom(), notification.getFrom());

            List<String> expectedToEmailAddresses = simpleMap(expectedNotification.getTo(), NotificationTarget::getEmailAddress);
            List<String> actualToEmailAddresses = simpleMap(notification.getTo(), NotificationTarget::getEmailAddress);
            assertEquals(expectedToEmailAddresses, actualToEmailAddresses);

            assertEquals(expectedNotification.getMessageKey(), notification.getMessageKey());
            assertEquals(expectedNotification.getGlobalArguments(), notification.getGlobalArguments());

        });
    }

    public static Notification createSimpleNotificationExpectations(Notification expectedNotification) {

        return createLambdaMatcher(notification -> {
            assertEquals(expectedNotification.getFrom(), notification.getFrom());

            List<String> expectedToEmailAddresses = simpleMap(expectedNotification.getTo(), NotificationTarget::getEmailAddress);
            List<String> actualToEmailAddresses = simpleMap(notification.getTo(), NotificationTarget::getEmailAddress);
            assertEquals(expectedToEmailAddresses, actualToEmailAddresses);

            assertEquals(expectedNotification.getMessageKey(), notification.getMessageKey());
        });
    }
	
}
