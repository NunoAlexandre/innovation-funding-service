package org.innovateuk.ifs.project.security;

import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.security.BasePermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;

import static org.innovateuk.ifs.security.SecurityRuleUtil.isCompAdmin;
import static org.innovateuk.ifs.security.SecurityRuleUtil.isInternal;
import static org.innovateuk.ifs.security.SecurityRuleUtil.isProjectFinanceUser;

@PermissionRules
public class PartnerOrganisationPermissionRules extends BasePermissionRules {
    @PermissionRule(value = "READ", description = "A partner can see a list of all partner organisations on their project")
    public boolean partnersOnProjectCanView(PartnerOrganisationResource partnerOrganisation, UserResource user) {
        return isPartner(partnerOrganisation.getProject(), user.getId());
    }

    @PermissionRule(value = "READ", description = "Internal users can see partner organisations for any project")
    public boolean internalUsersCanViewProjects(PartnerOrganisationResource partnerOrganisation, UserResource user) {
        return isInternal(user);
    }

}
