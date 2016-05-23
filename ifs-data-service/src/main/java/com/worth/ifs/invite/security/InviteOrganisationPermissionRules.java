package com.worth.ifs.invite.security;

import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.worth.ifs.security.SecurityRuleUtil.checkRole;
import static com.worth.ifs.user.resource.UserRoleType.COLLABORATOR;
import static com.worth.ifs.user.resource.UserRoleType.LEADAPPLICANT;


/**
 * Permission rules for {@link InviteOrganisationResource} permissioning
 */
@Component
@PermissionRules
public class InviteOrganisationPermissionRules {

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @PermissionRule(value = "SEND", description = "lead applicant can send an organisation invite for the application")
    public boolean leadApplicantCanInviteAnOrganisationToTheApplication(final InviteOrganisationResource inviteOrganisation, final UserResource user) {
        return hasRoleForAllApplicationsInOrganisationInvite(LEADAPPLICANT, inviteOrganisation, user);
    }

    @PermissionRule(value = "READ", description = "a lead applicant can view an organisation invite to the application")
    public boolean leadApplicantCanViewOrganisationInviteToTheApplication(final InviteOrganisationResource inviteOrganisation, final UserResource user) {
        return hasRoleForAllApplicationsInOrganisationInvite(LEADAPPLICANT, inviteOrganisation, user);
    }

    @PermissionRule(value = "READ", description = "a collaborator can view an organisation invite to the application")
    public boolean collaboratorCanViewOrganisationInviteToTheApplication(final InviteOrganisationResource inviteOrganisation, final UserResource user) {
        return hasRoleForAllApplicationsInOrganisationInvite(COLLABORATOR, inviteOrganisation, user);
    }

    @PermissionRule(value = "SAVE", description = "lead applicant can save an organisation invite for the application")
    public boolean leadApplicantCanSaveInviteAnOrganisationToTheApplication(final InviteOrganisationResource inviteOrganisation, final UserResource user) {
        return hasRoleForAllApplicationsInOrganisationInvite(LEADAPPLICANT, inviteOrganisation, user);
    }

    private final boolean hasRoleForAllApplicationsInOrganisationInvite(final UserRoleType userRoleType, final InviteOrganisationResource inviteOrganisation, final UserResource user) {
        final List<InviteResource> invites = inviteOrganisation.getInviteResources();
        if (invites == null || invites.isEmpty()) {
            return false; // Unable to check the application so default to false;
        }
        return invites.stream()
                .allMatch(inviteResource -> hasRoleOnApplication(userRoleType, user, inviteResource.getApplication()));
    }

    private boolean hasRoleOnApplication(final UserRoleType userRoleType, final UserResource user, final Long applicationId) {
        return checkRole(user, applicationId, userRoleType, processRoleRepository);
    }
}
