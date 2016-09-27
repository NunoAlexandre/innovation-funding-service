package com.worth.ifs.assessment.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Form field model for the Assessor Registration -Declaration of Interest/Family affiliations/Appointments, directorships or consultancies
 */
public class FamilyAffiliationForm {

    private String relation;
    private String organisation;
    private String position;

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FamilyAffiliationForm that = (FamilyAffiliationForm) o;

        return new EqualsBuilder()
                .append(relation, that.relation)
                .append(organisation, that.organisation)
                .append(position, that.position)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(relation)
                .append(organisation)
                .append(position)
                .toHashCode();
    }
}
