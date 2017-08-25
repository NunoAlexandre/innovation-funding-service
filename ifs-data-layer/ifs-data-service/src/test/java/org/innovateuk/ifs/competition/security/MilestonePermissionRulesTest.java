package org.innovateuk.ifs.competition.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.assessment.builder.CompetitionParticipantBuilder.newCompetitionParticipant;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests the logic within the individual CompetitionPermissionRules methods that secures basic Competition details
 */
public class MilestonePermissionRulesTest extends BasePermissionRulesTest<MilestonePermissionRules> {

	@Override
	protected MilestonePermissionRules supplyPermissionRulesUnderTest() {
		return new MilestonePermissionRules();
	}

    @Test
    public void testInternalUsersOtherThanInnovationLeadsCanViewAllMilestones() {
        allGlobalRoleUsers.forEach(user -> {
            if (!user.hasRole(UserRoleType.INNOVATION_LEAD) && allInternalUsers.contains(user)) {
                assertTrue(rules.allInternalUsersCanViewCompetitionMilestonesOtherThanInnovationLeads(1L, user));
            } else {
                assertFalse(rules.allInternalUsersCanViewCompetitionMilestonesOtherThanInnovationLeads(1L, user));
            }
        });
    }

    @Test
    public void testOnlyInnovationLeadUsersAssignedToCompCanAccess() {
        List<RoleResource> innovationLeadRoles = singletonList(newRoleResource().withType(UserRoleType.INNOVATION_LEAD).build());
        UserResource innovationLeadAssignedToCompetition = newUserResource().withRolesGlobal(innovationLeadRoles).build();
        UserResource innovationLeadNotAssignedToCompetition = newUserResource().withRolesGlobal(innovationLeadRoles).build();
        List<CompetitionParticipant> competitionParticipants = newCompetitionParticipant().withUser(newUser().withId(innovationLeadAssignedToCompetition.getId()).build()).build(1);

        when(competitionParticipantRepositoryMock.getByCompetitionIdAndRole(1L, CompetitionParticipantRole.INNOVATION_LEAD)).thenReturn(competitionParticipants);

        assertTrue(rules.innovationLeadsCanViewMilestonesOnAssginedComps(1L, innovationLeadAssignedToCompetition));
        assertFalse(rules.innovationLeadsCanViewMilestonesOnAssginedComps(1L, innovationLeadNotAssignedToCompetition));
    }
}
