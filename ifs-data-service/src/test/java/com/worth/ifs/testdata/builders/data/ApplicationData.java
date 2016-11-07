package com.worth.ifs.testdata.builders.data;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.user.resource.UserResource;

/**
 * TODO DW - document this class
 */
public class ApplicationData {

    private CompetitionResource competition;
    private ApplicationResource application;
    private UserResource leadApplicant;

    public CompetitionResource getCompetition() {
        return competition;
    }

    public void setCompetition(CompetitionResource competition) {
        this.competition = competition;
    }

    public void setApplication(ApplicationResource application) {
        this.application = application;
    }

    public ApplicationResource getApplication() {
        return application;
    }

    public void setLeadApplicant(UserResource leadApplicant) {
        this.leadApplicant = leadApplicant;
    }

    public UserResource getLeadApplicant() {
        return leadApplicant;
    }
}
