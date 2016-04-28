package com.worth.ifs.finance.resource.cost;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OtherFunding implements CostItem {
    private Long id;

    private String otherPublicFunding;
    private String fundingSource;
    private String securedDate;

    @NotNull
    @DecimalMin(value = "0")
    @Digits(integer = MAX_DIGITS, fraction = 0)
    private BigDecimal fundingAmount;

    private String name;

    public OtherFunding() {
        this.name = getCostType().getType();
    }

    public OtherFunding(Long id, String otherPublicFunding, String fundingSource, String securedDate, BigDecimal fundingAmount) {
        this();
        this.id = id;
        this.otherPublicFunding = otherPublicFunding;
        this.fundingSource = fundingSource;
        this.securedDate = securedDate;
        this.fundingAmount = fundingAmount;

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public BigDecimal getTotal() {
        return this.fundingAmount;
    }

    public String getOtherPublicFunding() {
        return otherPublicFunding;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public String getSecuredDate() {
        return securedDate;
    }

    public BigDecimal getFundingAmount() {
        return fundingAmount;
    }

    @Override
    public CostType getCostType() {
        return CostType.OTHER_FUNDING;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEmpty() {
        if((StringUtils.isBlank(fundingSource) && StringUtils.isBlank(securedDate) && (fundingAmount == null || fundingAmount.compareTo(BigDecimal.ZERO) == 0))){
            return true;
        }
        return false;
    }

    @Override
    public int getMinRows() {
        return 1;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOtherPublicFunding(String otherPublicFunding) {
        this.otherPublicFunding = otherPublicFunding;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public void setSecuredDate(String securedDate) {
        this.securedDate = securedDate;
    }

    public void setFundingAmount(BigDecimal fundingAmount) {
        this.fundingAmount = fundingAmount;
    }

    public void setName(String name) {
        this.name = name;
    }
}
