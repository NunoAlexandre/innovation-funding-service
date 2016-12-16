package org.innovateuk.ifs.assessment.model;

import org.innovateuk.ifs.application.UserApplicationRole;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.assessment.resource.AssessmentOutcomes;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessmentTotalScoreResource;
import org.innovateuk.ifs.assessment.service.AssessmentService;
import org.innovateuk.ifs.assessment.viewmodel.AssessorCompetitionDashboardApplicationViewModel;
import org.innovateuk.ifs.assessment.viewmodel.AssessorCompetitionDashboardViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.workflow.ProcessOutcomeService;
import org.innovateuk.ifs.workflow.resource.ProcessOutcomeResource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.assessment.resource.AssessmentStates.SUBMITTED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;

/**
 * Build the model for the Assessor Competition Dashboard view.
 */
@Component
public class AssessorCompetitionDashboardModelPopulator {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ProcessOutcomeService processOutcomeService;

    public AssessorCompetitionDashboardViewModel populateModel(Long competitionId, Long userId) {
        CompetitionResource competition = competitionService.getById(competitionId);
        LocalDateTime acceptDeadline = competition.getAssessorAcceptsDate();
        LocalDateTime submitDeadline = competition.getAssessorDeadlineDate();

        Map<Boolean, List<AssessorCompetitionDashboardApplicationViewModel>> applicationsPartitionedBySubmitted =
                getApplicationsPartitionedBySubmitted(userId, competitionId);
        List<AssessorCompetitionDashboardApplicationViewModel> submitted = applicationsPartitionedBySubmitted.get(TRUE);
        List<AssessorCompetitionDashboardApplicationViewModel> outstanding = applicationsPartitionedBySubmitted.get(FALSE);

        boolean submitVisible = outstanding.stream()
                .filter(AssessorCompetitionDashboardApplicationViewModel::isReadyToSubmit)
                .findAny()
                .isPresent();

        return new AssessorCompetitionDashboardViewModel(
                competition.getId(),
                competition.getName(),
                competition.getDescription(),
                competition.getLeadTechnologistName(),
                acceptDeadline,
                submitDeadline,
                submitted,
                outstanding,
                submitVisible
        );
    }

    private Map<Boolean, List<AssessorCompetitionDashboardApplicationViewModel>> getApplicationsPartitionedBySubmitted(Long userId, Long competitionId) {
        return assessmentService.getByUserAndCompetition(userId, competitionId).stream()
                .collect(partitioningBy(this::isAssessmentSubmitted, mapping(this::createApplicationViewModel, Collectors.toList())));
    }

    private boolean isAssessmentSubmitted(AssessmentResource assessmentResource) {
        return SUBMITTED == assessmentResource.getAssessmentState();
    }

    private AssessorCompetitionDashboardApplicationViewModel createApplicationViewModel(AssessmentResource assessment) {
        ApplicationResource application = applicationService.getById(assessment.getApplication());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());
        Optional<OrganisationResource> leadOrganisation = getApplicationLeadOrganisation(userApplicationRoles);
        boolean recommended = getRecommended(assessment);
        return new AssessorCompetitionDashboardApplicationViewModel(application.getId(),
                assessment.getId(),
                application.getApplicationDisplayName(),
                leadOrganisation.get().getName(),
                assessment.getAssessmentState(),
                getOverallScore(assessment),
                recommended);
    }

    private Optional<OrganisationResource> getApplicationLeadOrganisation(List<ProcessRoleResource> userApplicationRoles) {
        return userApplicationRoles.stream()
                .filter(uar -> uar.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccessObjectOrThrowException())
                .findFirst();
    }

    private int getOverallScore(AssessmentResource assessmentResource) {
        switch (assessmentResource.getAssessmentState()) {
            case READY_TO_SUBMIT:
            case SUBMITTED:
                AssessmentTotalScoreResource assessmentTotalScore = assessmentService.getTotalScore(assessmentResource.getId());
                return assessmentTotalScore.getTotalScorePercentage();
            default:
                return 0;
        }
    }

    private boolean getRecommended(AssessmentResource assessmentResource) {
        switch (assessmentResource.getAssessmentState()) {
            case READY_TO_SUBMIT:
            case SUBMITTED:
                ProcessOutcomeResource outcome = processOutcomeService.getByProcessIdAndOutcomeType(assessmentResource.getId(), AssessmentOutcomes.FUNDING_DECISION.getType());
                return BooleanUtils.toBoolean(outcome.getOutcome());
            default:
                return false;
        }
    }
}
