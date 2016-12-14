package org.innovateuk.ifs.management.viewmodel;

import org.innovateuk.ifs.user.resource.BusinessType;

/**
 * Holder of model attributes for the available assessors shown in the 'Find' tab of the Invite Assessors view.
 */
public class AvailableAssessorRowViewModel extends InviteAssessorsRowViewModel {

    private String email;
    private BusinessType businessType;
    private boolean added;

    public AvailableAssessorRowViewModel(String name, String innovationArea, boolean compliant, String email, BusinessType businessType, boolean added) {
        super(name, innovationArea, compliant);
        this.email = email;
        this.businessType = businessType;
        this.added = added;
    }

    public String getEmail() {
        return email;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public boolean isAdded() {
        return added;
    }
}