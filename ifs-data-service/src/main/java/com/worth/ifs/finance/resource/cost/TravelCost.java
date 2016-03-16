package com.worth.ifs.finance.resource.cost;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;

/**
 * {@code TravelCost} implements {@link CostItem}
 */
public class TravelCost implements CostItem {
    private Long id;
    private String item;
    private BigDecimal cost;
    private Integer quantity;
    private CostType costType;
    private String name;

    public TravelCost() {
        this.costType = CostType.TRAVEL;
        this.name = this.costType.getType();
    }

    public TravelCost(Long id, String item, BigDecimal cost, Integer quantity) {
        this();
        this.id = id;
        this.item = item;
        this.cost = cost;
        this.quantity = quantity;
    }

    @Override
    public Long getId() {
        return id;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public String getItem() {
        return item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public BigDecimal getTotal() {
        if(cost == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return cost.multiply(new BigDecimal(quantity));
    }

    @Override
    public CostType getCostType() {
        return costType;
    }

    @Override
    public String getName() {
        return name;
    }
}
