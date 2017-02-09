package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Interface for CRUD operations on {@link org.innovateuk.ifs.competition.resource.CompetitionResource} related data.
 */
public interface CompetitionsRestService {
    RestResult<List<CompetitionResource>> getAll();
    RestResult<List<CompetitionSearchResultItem>> findLiveCompetitions();
    RestResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions();
    RestResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions();
    RestResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size);
    RestResult<CompetitionCountResource> countCompetitions();
    RestResult<CompetitionResource> getCompetitionById(Long competitionId);
    RestResult<CompetitionResource> getPublishedCompetitionById(Long competitionId);
    RestResult<List<CompetitionTypeResource>> getCompetitionTypes();
    RestResult<Void> update(CompetitionResource competition);
    RestResult<CompetitionResource> create();
    RestResult<Void> markSectionComplete(Long competitionId, CompetitionSetupSection section);
    RestResult<Void> markSectionInComplete(Long competitionId, CompetitionSetupSection section);
    RestResult<String> generateCompetitionCode(Long competitionId, LocalDateTime openingDate);
    RestResult<Void> initApplicationForm(Long competitionId, Long competitionTypeId);
    RestResult<Void> markAsSetup(Long competitionId);
    RestResult<Void> returnToSetup(Long competitionId);
    RestResult<Void> closeAssessment(Long competitionId);
    RestResult<Void> notifyAssessors(Long competitionId);
}
