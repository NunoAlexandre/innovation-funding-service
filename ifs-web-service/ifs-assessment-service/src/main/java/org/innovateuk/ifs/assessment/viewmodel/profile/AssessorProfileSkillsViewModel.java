package org.innovateuk.ifs.assessment.viewmodel.profile;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.user.resource.BusinessType;

import java.util.List;

/**
 * Holder of model attributes for the Assessor skills view.
 */
public class AssessorProfileSkillsViewModel {
    private List<InnovationAreaResource> innovationAreas;
    private String skillAreas;
    private BusinessType assessorType;

    public AssessorProfileSkillsViewModel(List<InnovationAreaResource> innovationAreas, String skillAreas, BusinessType assessorType) {
        this.innovationAreas = innovationAreas;
        this.skillAreas = skillAreas;
        this.assessorType = assessorType;
    }

    public List<InnovationAreaResource> getInnovationAreas() {
        return innovationAreas;
    }

    public String getSkillAreas() {
        return skillAreas;
    }

    public BusinessType getAssessorType() {
        return assessorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssessorProfileSkillsViewModel that = (AssessorProfileSkillsViewModel) o;

        return new EqualsBuilder()
                .append(innovationAreas, that.innovationAreas)
                .append(skillAreas, that.skillAreas)
                .append(assessorType, that.assessorType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(innovationAreas)
                .append(skillAreas)
                .append(assessorType)
                .toHashCode();
    }
}
