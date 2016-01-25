package com.worth.ifs.finance.resource.category;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.LabourCost;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code LabourCostCategory} implementation for {@link CostCategory}. Calculating the Labour costs
 * for an application.
 */
public class LabourCostCategory implements CostCategory {
    public static final String WORKING_DAYS_PER_YEAR = "Working days per year";
    private LabourCost workingDaysPerYearCostItem;

    List<CostItem> costs = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    @Override
    public List<CostItem> getCosts() {
        return costs;
    }

    @Override
    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public void calculateTotal() {
        if(workingDaysPerYearCostItem != null) {
            total = costs.stream()
                    .map(c -> ((LabourCost) c).getTotal(workingDaysPerYearCostItem.getLabourDays()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            total = BigDecimal.ZERO;
        }
    }

    public Integer getWorkingDaysPerYear() {
        if (workingDaysPerYearCostItem!=null) {
            return workingDaysPerYearCostItem.getLabourDays();
        } else {
            return 0;
        }
    }

    public LabourCost getWorkingDaysPerYearCostItem() {
        return workingDaysPerYearCostItem;
    }

    @Override
    public void addCost(CostItem costItem) {
        if(costItem != null) {
            LabourCost labourCost = (LabourCost) costItem;
            if (labourCost.getDescription().equals(WORKING_DAYS_PER_YEAR)) {
                workingDaysPerYearCostItem = (LabourCost) costItem;
            } else {
                costs.add(costItem);
            }
        }
    }

    @Override
    public boolean excludeFromTotalCost() {
        return false;
    }

    @Override
    public void setCosts(List<CostItem> costItems) {
        costs = costItems;
    }
}
