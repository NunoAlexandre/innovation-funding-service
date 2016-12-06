package com.worth.ifs.competition.transactional;

import com.worth.ifs.commons.security.NotSecured;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.resource.CompetitionCountResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSearchResult;
import com.worth.ifs.competition.resource.CompetitionSearchResultItem;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Service for operations around the usage and processing of Competitions
 */
public interface CompetitionService {
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<CompetitionResource> getCompetitionById(final Long id);

    @NotSecured(value = "Not secured here, because this is only added extra information on the competition instance.", mustBeSecuredByOtherServices = true)
    Competition addCategories(@P("competition") Competition competition);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<CompetitionResource>> findAll();

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<CompetitionSearchResultItem>> findLiveCompetitions();

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions();

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions();

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    ServiceResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size);

    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    ServiceResult<CompetitionCountResource> countCompetitions();

    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    ServiceResult<Void> closeAssessment(Long competitionId);

    @PreAuthorize("hasAuthority('comp_admin')")
    ServiceResult<Void> notifyAssessors(Long competitionId);
}
