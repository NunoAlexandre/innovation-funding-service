package org.innovateuk.ifs.admin.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.innovateuk.ifs.admin.form.validation.Primary;
import org.innovateuk.ifs.admin.form.validation.Secondary;
import org.innovateuk.ifs.commons.validation.ValidationConstants;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.innovateuk.ifs.user.resource.AdminRoleType;

import javax.validation.GroupSequence;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Form to capture the posted details of the newly invited user
 */
@GroupSequence({Primary.class, Secondary.class, InviteUserForm.class})
public class InviteUserForm extends BaseBindingResultTarget {

    @NotEmpty(message = "{validation.standard.firstname.required}", groups = Primary.class)
    @Pattern(regexp = "[\\p{L} \\-']*", message = "{validation.standard.firstname.required}", groups = Secondary.class)
    @Size.List ({
            @Size(min=2, message="{validation.standard.firstname.length.min}", groups = Secondary.class),
            @Size(max=70, message="{validation.standard.firstname.length.max}", groups = Secondary.class),
    })
    private String firstName;

    @NotEmpty(message = "{validation.standard.lastname.required}", groups = Primary.class)
    @Pattern(regexp = "[\\p{L} \\-']*", message = "{validation.standard.lastname.required}", groups = Secondary.class)
    @Size.List ({
            @Size(min=2, message="{validation.standard.lastname.length.min}", groups = Secondary.class),
            @Size(max=70, message="{validation.standard.lastname.length.max}", groups = Secondary.class),
    })
    private String lastName;

    @NotEmpty(message = "{validation.invite.email.required}", groups = Primary.class)
    @Size(max = 256, message = "{validation.standard.email.length.max}", groups = Secondary.class)
    @Email(regexp = ValidationConstants.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX, message = "{validation.standard.email.format}", groups = Secondary.class)
    private String emailAddress;

    private AdminRoleType role;

    // for spring form binding
    public InviteUserForm() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public AdminRoleType getRole() {
        return role;
    }

    public void setRole(AdminRoleType role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InviteUserForm form = (InviteUserForm) o;

        return new EqualsBuilder()
                .append(firstName, form.firstName)
                .append(lastName, form.lastName)
                .append(emailAddress, form.emailAddress)
                .append(role, form.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(firstName)
                .append(lastName)
                .append(emailAddress)
                .append(role)
                .toHashCode();
    }
}
