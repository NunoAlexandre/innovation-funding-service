package org.innovateuk.ifs.competition.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.commons.resource.PageResource;

import java.util.List;
import java.util.Map;

public class CompetitionSearchResult extends PageResource<CompetitionSearchResultItem> {

    private Map<CompetitionStatus, List<CompetitionSearchResultItem>> mappedCompetitions;

    @JsonIgnore
    public Map<CompetitionStatus, List<CompetitionSearchResultItem>> getMappedCompetitions() {
        return mappedCompetitions;
    }

    public void setMappedCompetitions(Map<CompetitionStatus, List<CompetitionSearchResultItem>> mappedCompetitions) { this.mappedCompetitions = mappedCompetitions; }
}
