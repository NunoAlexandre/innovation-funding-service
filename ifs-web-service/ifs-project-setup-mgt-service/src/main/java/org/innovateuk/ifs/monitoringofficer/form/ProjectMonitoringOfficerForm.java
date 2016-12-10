package org.innovateuk.ifs.monitoringofficer.form;

import org.innovateuk.ifs.commons.validation.ValidationConstants;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.innovateuk.ifs.project.resource.MonitoringOfficerResource;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Optional;

/**
 * Form to capture the posted details of the Monitoring Officer
 */
public class ProjectMonitoringOfficerForm extends BaseBindingResultTarget {

    @NotEmpty(message = "{validation.standard.firstname.required}")
    private String firstName;

    @NotEmpty(message = "{validation.standard.lastname.required}")
    private String lastName;

    @Email(regexp = ValidationConstants.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX, message = "{validation.standard.email.format}")
    @NotEmpty(message = "{validation.standard.email.required}")
    @Size(max = 256, message = "{validation.standard.email.length.max}")
    private String emailAddress;

    @NotEmpty(message = "{validation.standard.phonenumber.required}")
    @Size.List ({
            @Size(min=8, message="{validation.standard.phonenumber.length.min}"),
            @Size(max=20, message="{validation.standard.phonenumber.length.max}")
    })
    @Pattern(regexp = "([0-9\\ +-])+",  message= "{validation.standard.phonenumber.format}")
    private String phoneNumber;

    // for spring form binding
    public ProjectMonitoringOfficerForm() {
    }

    public ProjectMonitoringOfficerForm(Optional<MonitoringOfficerResource> existingMonitoringOfficer) {
        existingMonitoringOfficer.ifPresent(mo -> {
            setFirstName(mo.getFirstName());
            setLastName(mo.getLastName());
            setEmailAddress(mo.getEmail());
            setPhoneNumber(mo.getPhoneNumber());
        });
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
