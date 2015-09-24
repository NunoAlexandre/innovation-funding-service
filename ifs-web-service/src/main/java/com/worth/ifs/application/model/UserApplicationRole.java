package com.worth.ifs.application.model;

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
