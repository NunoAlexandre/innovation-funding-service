package com.worth.ifs.finance.resource.cost;

import java.math.BigDecimal;

public class GrantClaim implements CostItem {
    private Long id;
    private Integer grantClaimPercentage;
    private CostType costType;
    public GrantClaim() {
        this.costType = CostType.FINANCE;
    }

    public GrantClaim(Long id, Integer grantClaimPercentage) {
        this();
        this.id = id;
        this.grantClaimPercentage = grantClaimPercentage;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public BigDecimal getTotal() {
        if (grantClaimPercentage == null) {
            return null;
        }
        return new BigDecimal(grantClaimPercentage);
    }

    public Integer getGrantClaimPercentage() {
        return grantClaimPercentage;
    }

    public void setGrantClaimPercentage(Integer grantClaimPercentage) {
        this.grantClaimPercentage = grantClaimPercentage;
    }

    @Override
    public CostType getCostType() {
        return costType;
    }
}
