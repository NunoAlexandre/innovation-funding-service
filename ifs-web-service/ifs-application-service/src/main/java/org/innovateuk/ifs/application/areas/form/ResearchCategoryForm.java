package org.innovateuk.ifs.application.areas.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;

import javax.validation.constraints.NotNull;

/**
 * Bean serves as a container for form parameters.
 */

public class ResearchCategoryForm extends BaseBindingResultTarget {
    @NotNull(message = "{validation.field.must.not.be.blank}")
    private String researchCategoryChoice;

    public String getResearchCategoryChoice() {
        return researchCategoryChoice;
    }

    public void setResearchCategoryChoice(String researchCategoryChoice) {
        this.researchCategoryChoice = researchCategoryChoice;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ResearchCategoryForm that = (ResearchCategoryForm) o;

        return new EqualsBuilder()
                .append(researchCategoryChoice, that.researchCategoryChoice)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(researchCategoryChoice)
                .toHashCode();
    }
}