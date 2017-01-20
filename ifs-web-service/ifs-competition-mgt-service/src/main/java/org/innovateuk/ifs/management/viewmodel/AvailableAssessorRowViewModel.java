package org.innovateuk.ifs.management.viewmodel;

import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.user.resource.BusinessType;

import java.util.List;

/**
 * Holder of model attributes for the available assessors shown in the 'Find' tab of the Invite Assessors view.
 */
public class AvailableAssessorRowViewModel extends InviteAssessorsRowViewModel {

    private Long id;
    private String email;
    private BusinessType businessType;
    private boolean added;

    public AvailableAssessorRowViewModel(Long id, (String name, List<InnovationAreaResource> innovationAreas, boolean compliant, String email, BusinessType businessType, boolean added) {
        super(name, innovationAreas, compliant);
        this.id = id;
        this.email = email;
        this.businessType = businessType;
        this.added = added;
    }

    public Long getId() {
        return id;
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