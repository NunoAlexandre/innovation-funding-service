package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.BaseControllerIntegrationTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.CompletedPercentageResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.innovateuk.ifs.workflow.domain.ActivityType;
import org.innovateuk.ifs.workflow.resource.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.commons.security.SecuritySetter.swapOutForUser;
import static org.junit.Assert.*;

@Rollback
public class ApplicationControllerIntegrationTest extends BaseControllerIntegrationTest<ApplicationController> {

    public static final long APPLICATION_ID = 1L;

    @Autowired
    private UserMapper userMapper;

    private QuestionController questionController;
    private Long leadApplicantProcessRole;
    private Long leadApplicantId;

    @Before
    public void setUp() throws Exception {
        leadApplicantId = 1L;
        leadApplicantProcessRole = 1L;
        List<ProcessRole> processRoles = new ArrayList<>();
        Application application = new Application(
                "",
                new ActivityState(ActivityType.APPLICATION, State.CREATED)
        );
        application.setId(APPLICATION_ID);
        processRoles.add(
            new ProcessRole(
                leadApplicantProcessRole,
                null,
                 application.getId(),
                null,
                null
            )
        );
        User user = new User(leadApplicantId, "steve", "smith", "steve.smith@empire.com", "", "123abc");
        processRoles.get(0).setUser(user);
        swapOutForUser(userMapper.mapToResource(user));
    }

    @After
    public void tearDown() throws Exception {
        swapOutForUser(null);
    }

    @Override
    @Autowired
    protected void setControllerUnderTest(ApplicationController controller) {
        this.controller = controller;
    }

    @Autowired
    public void setQuestionController(QuestionController questionController) {
        this.questionController = questionController;
    }

    @Test
    public void testFindAll() {
        RestResult<List<ApplicationResource>> all = controller.findAll();
        assertEquals(5, all.getSuccessObject().size());
    }

    @Test
    public void testUpdateApplication() {
        String originalTitle= "A novel solution to an old problem";
        String newTitle = "A new title";

        ApplicationResource application = controller.getApplicationById(APPLICATION_ID).getSuccessObject();
        assertEquals(originalTitle, application.getName());

        application.setName(newTitle);
        controller.saveApplicationDetails(APPLICATION_ID, application);

        ApplicationResource updated = controller.getApplicationById(APPLICATION_ID).getSuccessObject();
        assertEquals(newTitle, updated.getName());

    }

    /**
     * Check if progress decreases when marking a question as incomplete.
     */
    @Test
    public void testGetProgressPercentageByApplicationId() throws Exception {
        CompletedPercentageResource response = controller.getProgressPercentageByApplicationId(APPLICATION_ID).getSuccessObject();
        BigDecimal completedPercentage = response.getCompletedPercentage();
        double delta = 0.10;
        assertEquals(33.8709677418, completedPercentage.doubleValue(), delta); //Changed after enabling mark as complete on some more questions for INFUND-446

        questionController.markAsInComplete(28L, APPLICATION_ID, leadApplicantProcessRole);

        CompletedPercentageResource response2  = controller.getProgressPercentageByApplicationId(APPLICATION_ID).getSuccessObject();
        BigDecimal completedPercentage2 = response2.getCompletedPercentage();
        assertEquals(32.258064516, completedPercentage2.doubleValue(), delta); //Changed after enabling mark as complete on some more questions for INFUND-446
    }

    @Test
    public void testUpdateApplicationStateApproved() throws Exception {
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.OPEN);
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.SUBMITTED);
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.APPROVED);
        assertEquals(ApplicationState.APPROVED, controller.getApplicationById(APPLICATION_ID).getSuccessObject().getApplicationState());
    }

    @Test
    public void testUpdateApplicationStateRejected() throws Exception {
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.OPEN);
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.SUBMITTED);
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.REJECTED);
        assertEquals(ApplicationState.REJECTED, controller.getApplicationById(APPLICATION_ID).getSuccessObject().getApplicationState());
    }

    @Test
    public void testUpdateApplicationStateOpened() throws Exception {
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.OPEN);
        assertEquals(ApplicationState.OPEN, controller.getApplicationById(APPLICATION_ID).getSuccessObject().getApplicationState());
    }

    @Test
    public void testUpdateApplicationStateSubmitted() throws Exception {
        ApplicationResource applicationBefore = controller.getApplicationById(APPLICATION_ID).getSuccessObject();
        assertNull(applicationBefore.getSubmittedDate());

        controller.updateApplicationState(APPLICATION_ID, ApplicationState.SUBMITTED);
        assertEquals(ApplicationState.SUBMITTED, controller.getApplicationById(APPLICATION_ID).getSuccessObject().getApplicationState());

        ApplicationResource applicationAfter = controller.getApplicationById(APPLICATION_ID).getSuccessObject();
        assertNotNull(applicationAfter.getSubmittedDate());
    }

    @Test
    public void testGetApplicationsByCompetitionIdAndUserId() throws Exception {
        Long competitionId = 1L;
        Long userId = 1L ;
        UserRoleType role = UserRoleType.LEADAPPLICANT;
        List<ApplicationResource> applications = controller.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role).getSuccessObject();

        assertEquals(5, applications.size());
        Optional<ApplicationResource> application = applications.stream().filter(a -> a.getId().equals(APPLICATION_ID)).findAny();
        assertTrue(application.isPresent());
        assertEquals(competitionId, application.get().getCompetition());
    }

    @Test
    public void markAsIneligible() throws Exception {
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.OPEN);
        controller.updateApplicationState(APPLICATION_ID, ApplicationState.SUBMITTED);
        loginCompAdmin();
        controller.markAsIneligible(APPLICATION_ID, "Test reason");
        ApplicationResource applicationAfter = controller.getApplicationById(APPLICATION_ID).getSuccessObject();
        assertEquals(ApplicationState.INELIGIBLE, applicationAfter.getApplicationState());
        assertEquals("Test reason", applicationAfter.getIneligibleReason());
    }
}
