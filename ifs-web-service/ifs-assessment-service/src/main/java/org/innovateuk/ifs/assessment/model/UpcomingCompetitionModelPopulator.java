package org.innovateuk.ifs.assessment.model;

import org.innovateuk.ifs.assessment.viewmodel.UpcomingCompetitionViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionsRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Build the model for the Upcoming Competition view.
 */
@Component
public class UpcomingCompetitionModelPopulator {

    @Autowired
    private CompetitionsRestService competitionsRestService;

    public UpcomingCompetitionViewModel populateModel(Long competitionId) {
        CompetitionResource competition = competitionsRestService.getCompetitionById(competitionId).getSuccessObjectOrThrowException();
        return new UpcomingCompetitionViewModel(competition);
    }
}
