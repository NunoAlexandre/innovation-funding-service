package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.user.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.resource.UserResource;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Interface for CRUD operations on {@link org.innovateuk.ifs.competition.resource.CompetitionResource} related data.
 */
public interface CompetitionsRestService {
    RestResult<List<CompetitionResource>> getAll();

    RestResult<List<CompetitionResource>> getCompetitionsByUserId(Long userId);

    RestResult<List<CompetitionSearchResultItem>> findLiveCompetitions();
    RestResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions();
    RestResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions();
    RestResult<List<CompetitionSearchResultItem>> findNonIfsCompetitions();
    RestResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size);
    RestResult<CompetitionCountResource> countCompetitions();
    RestResult<CompetitionResource> getCompetitionById(long competitionId);
    RestResult<List<UserResource>> findInnovationLeads(long competitionId);
    RestResult<Void> addInnovationLead(long competitionId, long innovationLeadUserId);
    RestResult<Void> removeInnovationLead(long competitionId, long innovationLeadUserId);
    RestResult<CompetitionResource> getPublishedCompetitionById(long competitionId);
    RestResult<List<CompetitionTypeResource>> getCompetitionTypes();
    RestResult<Void> update(CompetitionResource competition);
    RestResult<Void> updateCompetitionInitialDetails(CompetitionResource competition);
    RestResult<CompetitionResource> create();
    RestResult<Void> markSectionComplete(long competitionId, CompetitionSetupSection section);
    RestResult<Void> markSectionInComplete(long competitionId, CompetitionSetupSection section);
    RestResult<String> generateCompetitionCode(long competitionId, ZonedDateTime openingDate);
    RestResult<Void> initApplicationForm(long competitionId, long competitionTypeId);
    RestResult<Void> markAsSetup(long competitionId);
    RestResult<Void> returnToSetup(long competitionId);
    RestResult<Void> closeAssessment(long competitionId);
    RestResult<Void> notifyAssessors(long competitionId);
    RestResult<Void> releaseFeedback(long competitionId);
    RestResult<CompetitionResource> createNonIfs();
    RestResult<List<OrganisationTypeResource>> getCompetitionOrganisationType(long id);
    RestResult<List<ApplicationResource>> findInformedNotInProjectSetup(Long competitionId);
}
