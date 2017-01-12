package org.innovateuk.ifs.assessment.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.mapper.AssessmentMapper;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.assessment.resource.AssessmentSubmissionsResource;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.Map;

import static org.innovateuk.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static org.innovateuk.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentSubmissionsResourceBuilder.newAssessmentSubmissionsResource;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.*;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.workflow.domain.ActivityType.APPLICATION_ASSESSMENT;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class AssessmentPermissionRulesTest extends BasePermissionRulesTest<AssessmentPermissionRules> {

    private UserResource assessorUser;
    private UserResource otherUser;
    private Map<AssessmentStates, AssessmentResource> assessments;

    @Autowired
    public AssessmentMapper assessmentMapper;

    @Override
    protected AssessmentPermissionRules supplyPermissionRulesUnderTest() {
        return new AssessmentPermissionRules();
    }

    @Before
    public void setup() {
        assessorUser = newUserResource().build();
        otherUser = newUserResource().build();

        ProcessRole processRole = newProcessRole()
                .withUser(newUser().with(id(assessorUser.getId())).build())
                .build();
        when(processRoleRepositoryMock.findOne(processRole.getId())).thenReturn(processRole);

        assessments = EnumSet.allOf(AssessmentStates.class).stream().collect(toMap(identity(), state -> setupAssessment(processRole, state)));
    }

    @Test
    public void ownersCanReadAssessmentsOnDashboard() {
        EnumSet<AssessmentStates> allowedStates = EnumSet.of(PENDING, ACCEPTED, OPEN, READY_TO_SUBMIT, SUBMITTED);

        allowedStates.forEach(state ->
                assertTrue("the owner of an assessment should be able to read that assessment on the dashboard",
                        rules.userCanReadAssessmentOnDashboard(assessments.get(state), assessorUser)));

        EnumSet.complementOf(allowedStates).forEach(state ->
                assertFalse("the owner of a an assessment should not be able to read that assessment on the dashboard",
                        rules.userCanReadAssessmentOnDashboard(assessments.get(state), assessorUser)));
    }

    @Test
    public void otherUsersCanNotReadAssessmentsOnDashboard() {
        EnumSet.allOf(AssessmentStates.class).forEach(state ->
                assertFalse("other users should not be able to read any assessments",
                        rules.userCanReadAssessmentOnDashboard(assessments.get(state), otherUser)));
    }

    @Test
    public void ownersCanReadAssessmentsNonDashboard() {
        EnumSet<AssessmentStates> allowedStates = EnumSet.of(PENDING, ACCEPTED, OPEN, READY_TO_SUBMIT);

        allowedStates.forEach(state ->
                assertTrue("the owner of an assessment should be able to read that assessment",
                        rules.userCanReadAssessment(assessments.get(state), assessorUser)));

        EnumSet.complementOf(allowedStates).forEach(state ->
                assertFalse("the owner of an assessment should not be able to read that assessment",
                        rules.userCanReadAssessment(assessments.get(state), assessorUser)));
    }

    @Test
    public void otherUsersCanNotReadAssessmentsNonDashboard() {
        EnumSet.allOf(AssessmentStates.class).forEach(state ->
                assertFalse("other users should not be able to read any assessments",
                        rules.userCanReadAssessment(assessments.get(state), otherUser)));
    }

    @Test
    public void ownersCanReadAssessmentScore() {
        EnumSet<AssessmentStates> allowedStates = EnumSet.of(ACCEPTED, OPEN, READY_TO_SUBMIT, SUBMITTED);

        allowedStates.forEach(state ->
                assertTrue("the owner of an assessment should be able to read the assessment score",
                        rules.userCanReadAssessmentScore(assessments.get(state), assessorUser)));

        EnumSet.complementOf(allowedStates).forEach(state ->
                assertFalse("the owner of an assessment should not be able to read the assessment score",
                        rules.userCanReadAssessmentScore(assessments.get(state), assessorUser)));
    }

    @Test
    public void otherUsersCanNotReadAssessmentScore() {
        EnumSet.allOf(AssessmentStates.class).forEach(state ->
                assertFalse("other users should not be able to read any assessment scores",
                        rules.userCanReadAssessmentScore(assessments.get(state), otherUser)));
    }

    @Test
    public void ownersCanOnlyAssignPendingAssessments() {
        EnumSet<AssessmentStates> allowedStates = EnumSet.of(PENDING);
        allowedStates.forEach(state ->
                assertTrue("the owner of an assessment should be able to assign the assessment",
                        rules.userCanAssignAssessment(assessments.get(state), assessorUser)));

        EnumSet.complementOf(allowedStates).forEach(state ->
                assertFalse("the owner of an assessment should not be able to re-assign the assessment",
                        rules.userCanAssignAssessment(assessments.get(state), assessorUser)));
    }

    @Test
    public void otherUsersCanNotAssignPendingAssessments() {
        EnumSet.allOf(AssessmentStates.class).forEach(state ->
            assertFalse("other users should not be able to assign the assessment",
                    rules.userCanAssignAssessment(assessments.get(state), otherUser)));
    }

    @Test
    public void ownersCanUpdateAssessments() {
        EnumSet<AssessmentStates> updatableStates = EnumSet.of(PENDING, ACCEPTED, OPEN, READY_TO_SUBMIT);

        updatableStates.forEach(state ->
                assertTrue("the owner of an assessment should able to update that assessment",
                        rules.userCanUpdateAssessment(assessments.get(state), assessorUser)));

        EnumSet.complementOf(updatableStates).forEach(state ->
                assertFalse("the owner of an assessment should not be able to update that assessment",
                        rules.userCanUpdateAssessment(assessments.get(state), assessorUser)));
    }

    @Test
    public void otherUsersCanNotUpdateAssessments() {
        EnumSet.allOf(AssessmentStates.class).forEach(state ->
                assertFalse("other users should not able to update assessments",
                        rules.userCanUpdateAssessment(assessments.get(state), otherUser)));
    }

    @Test
    public void ownersCanSubmitAssessments() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissionsResource = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();
        ProcessRole processRole = newProcessRole()
                .withUser(newUser().with(id(assessorUser.getId())).build())
                .build();
        Assessment assessment1 = newAssessment()
                .withParticipant(processRole)
                .build();
        Assessment assessment2 = newAssessment()
                .withParticipant(processRole)
                .build();

        when(assessmentRepositoryMock.findAll(asList(1L, 2L))).thenReturn(asList(assessment1, assessment2));

        assertTrue("the owner of a list of assessments can submit them", rules.userCanSubmitAssessments(assessmentSubmissionsResource, assessorUser));
    }

    @Test
    public void otherUsersCannotPartiallySubmitAssessments() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissionsResource = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();
        Assessment assessment1 = newAssessment()
                .withParticipant(
                        newProcessRole()
                                .withUser(newUser().with(id(assessorUser.getId())).build())
                                .build()
                )
                .build();
        Assessment assessment2 = newAssessment()
                .withParticipant(
                        newProcessRole()
                                .withUser(newUser().with(id(10L)).build())
                                .build()
                )
                .build();

        when(assessmentRepositoryMock.findAll(asList(1L, 2L))).thenReturn(asList(assessment1, assessment2));

        assertFalse("other users cannot partially submit assessments", rules.userCanSubmitAssessments(assessmentSubmissionsResource, assessorUser));
    }

    @Test
    public void otherUsersCannotSubmitAssessments() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissionsResource = newAssessmentSubmissionsResource()
                .withAssessmentIds(asList(1L, 2L))
                .build();
        Assessment assessment1 = newAssessment()
                .withParticipant(
                        newProcessRole()
                                .withUser(newUser().with(id(10L)).build())
                                .build()
                )
                .build();
        Assessment assessment2 = newAssessment()
                .withParticipant(
                        newProcessRole()
                                .withUser(newUser().with(id(10L)).build())
                                .build()
                )
                .build();

        when(assessmentRepositoryMock.findAll(asList(1L, 2L))).thenReturn(asList(assessment1, assessment2));

        assertFalse("other users cannot submit assessments", rules.userCanSubmitAssessments(assessmentSubmissionsResource, assessorUser));
    }

    private AssessmentResource setupAssessment(ProcessRole participant, AssessmentStates state) {
        Assessment assessment = newAssessment()
                .withActivityState(new ActivityState(APPLICATION_ASSESSMENT, state.getBackingState()))
                .build();

        when(assessmentRepositoryMock.findOne(assessment.getId())).thenReturn(assessment);

        return newAssessmentResource()
                .withId(assessment.getId())
                .withProcessRole(participant.getId())
                .build();
    }
}
