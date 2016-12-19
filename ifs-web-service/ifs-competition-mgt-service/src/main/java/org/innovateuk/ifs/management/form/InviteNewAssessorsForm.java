package org.innovateuk.ifs.management.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class InviteNewAssessorsForm extends BaseBindingResultTarget {

    @Valid
    private List<InviteNewAssessorsRowForm> invites = new ArrayList<>();

    @NotNull(message = "{validation.inviteNewAssessorsForm.selectedInnovationArea.required}")
    private Long selectedInnovationArea;

    public List<InviteNewAssessorsRowForm> getInvites() {
        return invites;
    }

    public void setInvites(List<InviteNewAssessorsRowForm> invites) {
        this.invites = invites;
    }

    public Long getSelectedInnovationArea() {
        return selectedInnovationArea;
    }

    public void setSelectedInnovationArea(Long selectedInnovationArea) {
        this.selectedInnovationArea = selectedInnovationArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InviteNewAssessorsForm that = (InviteNewAssessorsForm) o;

        return new EqualsBuilder()
                .append(selectedInnovationArea, that.selectedInnovationArea)
                .append(invites, that.invites)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(invites)
                .append(selectedInnovationArea)
                .toHashCode();
    }
}
