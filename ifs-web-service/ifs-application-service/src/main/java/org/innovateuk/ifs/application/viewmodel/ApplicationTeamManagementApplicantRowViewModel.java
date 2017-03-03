package org.innovateuk.ifs.application.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Holder of model attributes for an applicant in the Application Team Management view.
 */
public class ApplicationTeamManagementApplicantRowViewModel {

    private Long applicationInviteId;
    private String name;
    private String email;
    private boolean lead;
    private boolean pending;

    public ApplicationTeamManagementApplicantRowViewModel(String name, String email, boolean lead, boolean pending) {
        this(null, name, email, lead, pending);
    }

    public ApplicationTeamManagementApplicantRowViewModel(Long applicationInviteId, String name, String email, boolean lead, boolean pending) {
        this.applicationInviteId = applicationInviteId;
        this.name = name;
        this.email = email;
        this.lead = lead;
        this.pending = pending;
    }

    public Long getApplicationInviteId() {
        return applicationInviteId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLead() {
        return lead;
    }

    public boolean isPending() {
        return pending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationTeamManagementApplicantRowViewModel that = (ApplicationTeamManagementApplicantRowViewModel) o;

        return new EqualsBuilder()
                .append(applicationInviteId, that.applicationInviteId)
                .append(lead, that.lead)
                .append(pending, that.pending)
                .append(name, that.name)
                .append(email, that.email)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(applicationInviteId)
                .append(name)
                .append(email)
                .append(lead)
                .append(pending)
                .toHashCode();
    }
}
