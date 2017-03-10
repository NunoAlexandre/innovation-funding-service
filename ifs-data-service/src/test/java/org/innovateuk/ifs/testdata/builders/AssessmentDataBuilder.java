package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.resource.*;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.workflow.domain.ActivityState;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.*;
import static org.innovateuk.ifs.workflow.domain.ActivityType.APPLICATION_ASSESSMENT;

/**
 * Generates Assessments for Applications so that Assessors may start assessing them
 */
public class AssessmentDataBuilder extends BaseDataBuilder<Void, AssessmentDataBuilder> {

    public AssessmentDataBuilder withAssessmentData(String assessorEmail,
                                                    String applicationName,
                                                    AssessmentRejectOutcomeValue rejectReason,
                                                    String rejectComment,
                                                    AssessmentStates state,
                                                    String feedback,
                                                    String recommendComment) {
        return with(data -> {

            Application application = applicationRepository.findByName(applicationName).get(0);
            UserResource assessor = retrieveUserByEmail(assessorEmail);

            AssessmentResource assessmentResource = doAs(compAdmin(), () -> assessmentService.createAssessment(
                    new AssessmentCreateResource(application.getId(), assessor.getId())).getSuccessObjectOrThrowException()
            );

            Assessment assessment = assessmentRepository.findOne(assessmentResource.getId());
            doAs(compAdmin(), () -> assessmentWorkflowHandler.notify(assessment));

            switch (state) {
                case ACCEPTED:
                case READY_TO_SUBMIT:
                case SUBMITTED:
                    doAs(assessor, () -> assessmentService.acceptInvitation(assessment.getId())
                            .getSuccessObjectOrThrowException());
                    break;
                case REJECTED:
                    doAs(assessor, () -> assessmentService.rejectInvitation(assessment.getId(), new
                            AssessmentRejectOutcomeResource(rejectReason, rejectComment))
                            .getSuccessObjectOrThrowException());
                    break;
                case WITHDRAWN:
                    doAs(compAdmin(), () -> assessmentService.withdrawAssessment(assessment.getId())
                            .getSuccessObjectOrThrowException());
                    break;
            }

            if (EnumSet.of(OPEN).contains(state)) {
                ActivityState activityState = activityStateRepository.findOneByActivityTypeAndState(
                        APPLICATION_ASSESSMENT,
                        state.getBackingState()
                );
                assessment.setActivityState(activityState);
            }

            doAs(assessor, () -> {
                if (feedback == null && recommendComment == null) {
                    return;
                }

                AssessmentFundingDecisionOutcomeResource fundingDecision = new AssessmentFundingDecisionOutcomeResource(
                        true,
                        feedback,
                        recommendComment
                );

                assessmentService.recommend(assessment.getId(), fundingDecision).getSuccessObjectOrThrowException();
            });
        });
    }

    public AssessmentDataBuilder withSubmission(String applicationName,
                                                String assessorEmail,
                                                AssessmentStates state) {
        return with(data -> {
            if (state != SUBMITTED) {
                return;
            }

            Application application = applicationRepository.findByName(applicationName).get(0);
            UserResource assessor = retrieveUserByEmail(assessorEmail);
            Optional<Assessment> assessment = assessmentRepository.findFirstByParticipantUserIdAndTargetIdOrderByIdDesc(assessor.getId(), application.getId());

            if (!assessment.isPresent()) {
                return;
            }

            // We have to forcefully set the SUBMITTED state for the assessment, as the
            // relevant competition is not necessarily IN_ASSESSMENT.
            // This means that the state transition to SUBMITTED through the workflow
            // handler will fail due to the `CompetitionInAssessmentGuard`.
            ActivityState activityState = activityStateRepository.findOneByActivityTypeAndState(
                    APPLICATION_ASSESSMENT,
                    SUBMITTED.getBackingState()
            );

            assessment.ifPresent(a -> a.setActivityState(activityState));
        });
    }


    public static AssessmentDataBuilder newAssessmentData(ServiceLocator serviceLocator) {

        return new AssessmentDataBuilder(emptyList(), serviceLocator);
    }

    private AssessmentDataBuilder(List<BiConsumer<Integer, Void>> multiActions,
                                  ServiceLocator serviceLocator) {

        super(multiActions, serviceLocator);
    }

    @Override
    protected AssessmentDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Void>> actions) {
        return new AssessmentDataBuilder(actions, serviceLocator);
    }

    @Override
    protected Void createInitial() {
        return null;
    }
}
