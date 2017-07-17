package org.innovateuk.ifs.assessment.transactional;

import com.google.common.collect.Sets;
import org.innovateuk.ifs.assessment.domain.ApplicationAssessmentCount;
import org.innovateuk.ifs.assessment.repository.AssessmentRepository;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.assessment.resource.AssessorAssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessorCompetitionSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.innovateuk.ifs.workflow.resource.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

@Service
public class AssessorCompetitionSummaryServiceImpl implements AssessorCompetitionSummaryService {

    public static final Set<State> INVALID_ASSESSMENT_STATES = AssessmentStates.getBackingStates(EnumSet.of(
            AssessmentStates.CREATED,
            AssessmentStates.REJECTED,
            AssessmentStates.WITHDRAWN
    ));

    public static final Set<State> VALID_ASSESSMENT_STATES = Sets.complementOf(INVALID_ASSESSMENT_STATES);

    @Autowired
    private AssessorService assessorService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Transactional(readOnly = true)
    @Override
    public ServiceResult<AssessorCompetitionSummaryResource> getAssessorSummary(long assessorId, long competitionId) {
        return assessorService.getAssessorProfile(assessorId).andOnSuccess(assessorProfile ->
                competitionService.getCompetitionById(competitionId).andOnSuccess(competition -> {
                    long allAssessmentCount = assessmentRepository.countByParticipantUserIdAndActivityStateStateNotIn(
                            assessorProfile.getUser().getId(),
                            VALID_ASSESSMENT_STATES
                    );

                    List<ApplicationAssessmentCount> applicationAssessmentCounts = assessmentRepository
                            .getAssessorApplicationAssessmentCountsForStates(
                                    competition.getId(),
                                    assessorId,
                                    VALID_ASSESSMENT_STATES
                            );

                    return serviceSuccess(new AssessorCompetitionSummaryResource(
                            competition.getId(),
                            competition.getName(),
                            assessorProfile,
                            allAssessmentCount,
                            mapCountsToResource(applicationAssessmentCounts)
                    ));
                })
        );
    }

    private List<AssessorAssessmentResource> mapCountsToResource(List<ApplicationAssessmentCount> applicationAssessmentCounts) {
        return simpleMap(applicationAssessmentCounts, applicationAssessmentCount -> {

            Organisation leadOrganisation = organisationRepository.findOne(
                    applicationAssessmentCount.getApplication().getLeadOrganisationId()
            );

            return new AssessorAssessmentResource(
                    applicationAssessmentCount.getApplication().getId(),
                    applicationAssessmentCount.getApplication().getName(),
                    leadOrganisation.getName(),
                    applicationAssessmentCount.getAssessmentCount(),
                    applicationAssessmentCount.getAssessment().getActivityState()
            );
        });
    }
}
