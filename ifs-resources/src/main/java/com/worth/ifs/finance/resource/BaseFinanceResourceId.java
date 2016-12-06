package com.worth.ifs.finance.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;


/**
 * Compound class for holding the application finance resource keys
 */
public abstract class BaseFinanceResourceId implements Serializable {
    private Long targetId;
    private Long organisationId;

    public BaseFinanceResourceId(Long targetId, Long organisationId) {
        this.targetId = targetId;
        this.organisationId = organisationId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BaseFinanceResourceId that = (BaseFinanceResourceId) o;

        return new EqualsBuilder()
                .append(targetId, that.targetId)
                .append(organisationId, that.organisationId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(targetId)
                .append(organisationId)
                .toHashCode();
    }
}
