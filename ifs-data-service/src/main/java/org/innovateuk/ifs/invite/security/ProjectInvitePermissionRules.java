package org.innovateuk.ifs.invite.security;

import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.security.BasePermissionRules;
import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

/**
 * Permission rules for ProjectInvite Service
 */
@Component
@PermissionRules
public class ProjectInvitePermissionRules extends BasePermissionRules {

    @PermissionRule(value = "READ_PROJECT_INVITE", description = "A user can view a project invite that they are partners on")
    public boolean partnersOnProjectCanViewInvite(final InviteProjectResource invite, UserResource user) {
        return isUserMemberOfProjectTeam(invite, user);
    }


    @PermissionRule(value = "SEND_PROJECT_INVITE", description = "A user can send a project invite that they are partners on and belong to same organisation")
    public boolean partnersOnProjectCanSendInvite(final InviteProjectResource invite, UserResource user) {
        return isUserPartnerOnProjectWithinSameOrganisation(invite, user);
    }

    @PermissionRule(value = "SAVE_PROJECT_INVITE", description = "A user can save a project invite that they are partners on and belong to same organisation")
    public boolean partnersOnProjectCanSaveInvite(final InviteProjectResource invite, UserResource user) {
        return isUserPartnerOnProjectWithinSameOrganisation(invite, user);
    }


    private boolean isUserPartnerOnProjectWithinSameOrganisation(final InviteProjectResource invite, UserResource user){
        if (invite.getProject() != null && invite.getOrganisation() != null) {
            return partnerBelongsToOrganisation(invite.getProject(), user.getId(), invite.getOrganisation());
        }
        return false;
    }

    private boolean isUserMemberOfProjectTeam(final InviteProjectResource invite, UserResource user) {
            return isPartner(invite.getProject(), user.getId());
    }
}
