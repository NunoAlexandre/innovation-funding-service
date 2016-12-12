package org.innovateuk.ifs.login.form;

import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;

/**
 * Form field model for the selection of either the Assessor or Applicant roles.
 */
public class RoleSelectionForm extends BaseBindingResultTarget {

    @NotNull(message = "{validation.standard.role.required}")
    private UserRoleType selectedRole;

    public UserRoleType getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(UserRoleType selectedRole) {
        this.selectedRole = selectedRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RoleSelectionForm that = (RoleSelectionForm) o;

        return new EqualsBuilder()
                .append(selectedRole, that.selectedRole)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(selectedRole)
                .toHashCode();
    }
}
