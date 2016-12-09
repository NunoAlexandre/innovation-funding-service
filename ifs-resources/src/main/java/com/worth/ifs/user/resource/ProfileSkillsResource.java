package com.worth.ifs.user.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.Size;

/**
 * Profile Skills Data Transfer Object
 */
public class ProfileSkillsResource {

    private Long user;
    @Size(max = 5000, message = "{validation.field.too.many.characters}")
    private String skillsAreas;
    private BusinessType businessType;

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public String getSkillsAreas() {
        return skillsAreas;
    }

    public void setSkillsAreas(String skillsAreas) {
        this.skillsAreas = skillsAreas;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProfileSkillsResource that = (ProfileSkillsResource) o;

        return new EqualsBuilder()
                .append(user, that.user)
                .append(skillsAreas, that.skillsAreas)
                .append(businessType, that.businessType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(user)
                .append(skillsAreas)
                .append(businessType)
                .toHashCode();
    }
}