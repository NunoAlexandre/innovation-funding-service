package org.innovateuk.ifs.application;

/**
 * This enumerations defines the available UserApplicationRoles.
 */
public enum UserApplicationRole {
    LEAD_APPLICANT("leadapplicant"),
    COLLABORATOR("collaborator");

    String roleName;
    UserApplicationRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
