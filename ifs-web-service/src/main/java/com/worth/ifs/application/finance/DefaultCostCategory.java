package com.worth.ifs.application.finance;

import com.worth.ifs.application.finance.cost.CostItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code DefaultCostCategory} implementation for {@link CostCategory}.
 * Default representation for costs and defaults to summing up the costs.
 */
public class DefaultCostCategory implements CostCategory {
    List<CostItem> costs = new ArrayList<>();
    BigDecimal total = new BigDecimal(0);

    @Override
    public List<CostItem> getCosts() {
        return costs;
    }

    @Override
    public BigDecimal getTotal() {
        total = costs.stream()
                .map(c -> c.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    @Override
    public void addCost(CostItem costItem) {
        if(costItem!=null) {
            costs.add(costItem);
        }
    }
}
