package org.innovateuk.ifs.management.model;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.assessment.service.AssessmentRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.service.CompetitionKeyStatisticsRestService;
import org.innovateuk.ifs.management.viewmodel.CompetitionInFlightStatsViewModel;
import org.innovateuk.ifs.management.viewmodel.CompetitionInFlightViewModel;
import org.innovateuk.ifs.management.viewmodel.MilestonesRowViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * Build the model for the Competition Management Competition in flight stats dashboard.
 */
@Component
public class CompetitionInFlightStatsModelPopulator {

    @Autowired
    private CompetitionKeyStatisticsRestService competitionKeyStatisticsRestService;

    @Autowired
    private CompetitionService competitionService;


    public CompetitionInFlightStatsViewModel populateStatsViewModel(Long competitionId) {
        return populateStatsViewModel(competitionService.getById(competitionId));
    }

    public CompetitionInFlightStatsViewModel populateStatsViewModel(CompetitionResource competitionResource) {
        switch (competitionResource.getCompetitionStatus()) {
            case READY_TO_OPEN:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getReadyToOpenKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
            case OPEN:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getOpenKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
            case CLOSED:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getClosedKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
            case IN_ASSESSMENT:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getInAssessmentKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
            case FUNDERS_PANEL:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getFundedKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
            case ASSESSOR_FEEDBACK:
                return new CompetitionInFlightStatsViewModel(competitionKeyStatisticsRestService.getFundedKeyStatisticsByCompetition(competitionResource.getId()).getSuccessObjectOrThrowException());
        }
        return new CompetitionInFlightStatsViewModel();
    }
}
