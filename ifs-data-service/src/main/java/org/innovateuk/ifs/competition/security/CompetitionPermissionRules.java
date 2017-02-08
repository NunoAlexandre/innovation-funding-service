package org.innovateuk.ifs.competition.security;

import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.security.BasePermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.security.SecurityRuleUtil.isCompAdmin;
import static org.innovateuk.ifs.security.SecurityRuleUtil.isInternal;
import static org.innovateuk.ifs.security.SecurityRuleUtil.isProjectFinanceUser;

/**
 * Provides the permissions around CRUD for Competitions
 */
@Component
@PermissionRules
public class CompetitionPermissionRules extends BasePermissionRules {

    @PermissionRule(value = "READ", description = "External users cannot view competitions in setup")
    public boolean externalUsersCannotViewCompetitionsInSetup(CompetitionResource competition, UserResource user) {
        return !CompetitionStatus.COMPETITION_SETUP.equals(competition.getCompetitionStatus());
    }

    @PermissionRule(value = "READ", description = "Internal users can see all competitions")
    public boolean internalUserCanViewAllCompetitions(CompetitionResource competition, UserResource user) {
        return isInternal(user);
    }

    @PermissionRule(value = "READ", description = "Internal users can see all competition search results")
    public boolean internalUserCanViewAllCompetitionSearchResults(CompetitionSearchResultItem competition, UserResource user) {
        return isInternal(user);
    }
}
