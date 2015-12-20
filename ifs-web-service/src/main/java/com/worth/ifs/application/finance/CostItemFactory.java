package com.worth.ifs.application.finance;

import com.worth.ifs.application.finance.cost.*;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.domain.CostValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Creates specific costs for each category and maps the cost to cost items, which
 * can be used in the view.
 */
public class CostItemFactory {
    private final Log log = LogFactory.getLog(getClass());

    public static final String COST_FIELD_EXISTING = "existing";
    public static final String COST_FIELD_RESIDUAL_VALUE = "residual_value";
    public static final String COST_FIELD_UTILISATION = "utilisation";
    public static final String COST_FIELD_COUNTRY = "country";

    public CostItem createCostItem(CostType costType, Cost cost) {
        switch(costType) {
            case LABOUR:
                return new LabourCost(cost.getId(), cost.getItem(), cost.getCost(), cost.getQuantity(), cost.getDescription());
            case CAPITAL_USAGE:
                return createCapitalUsage(cost);
            case MATERIALS:
                return new Materials(cost.getId(),cost.getItem(),cost.getCost(),cost.getQuantity());
            case OTHER_COSTS:
                return new OtherCost(cost.getId(), cost.getCost(),cost.getDescription());
            case OVERHEADS:
                return new Overhead(cost.getId(), cost.getItem(), cost.getQuantity());
            case SUBCONTRACTING_COSTS:
                return createSubcontractingCost(cost);
            case TRAVEL:
                return new TravelCost(cost.getId(), cost.getCost(), cost.getItem(), cost.getQuantity());
            case OTHER_FUNDING:
                return createOtherFunding(cost);
        }
        return null;
    }

    private CostItem createCapitalUsage(Cost cost) {
        String existing = "";
        BigDecimal residualValue = BigDecimal.ZERO;
        Integer utilisation = 0;

        for(CostValue costValue : cost.getCostValues()) {
            if(costValue.getCostField().getTitle().equals(COST_FIELD_EXISTING)) {
                existing = costValue.getValue();
            } else if(costValue.getCostField().getTitle().equals(COST_FIELD_RESIDUAL_VALUE)) {
                residualValue = new BigDecimal(costValue.getValue());
            } else if(costValue.getCostField().getTitle().equals(COST_FIELD_UTILISATION)) {
                utilisation = Integer.valueOf(costValue.getValue());
            }
        }

        return new CapitalUsage(cost.getId(), cost.getQuantity(), cost.getDescription(), existing,
                cost.getCost(), residualValue, utilisation);

    }

    private CostItem createSubcontractingCost(Cost cost) {
        String country = "";
        for(CostValue costValue : cost.getCostValues()) {
            if(costValue.getCostField().getTitle().equals(COST_FIELD_COUNTRY)) {
                country = costValue.getValue();
            }
        }

        return new SubContractingCost(cost.getId(), cost.getCost(), country, cost.getItem(), cost.getDescription());
    }

    private CostItem createOtherFunding(Cost cost) {
        String securedDateMonth = null;
        String securedDateYear = null;

        if(cost.getItem()!=null && !cost.getItem().isEmpty()) {
            String [] date = cost.getItem().split("-");
            if(date.length > 0 && !date[0].equals("null")) {
                securedDateMonth = date[0];
            } else {
                securedDateMonth = "";
            }
            if(date.length > 1 && !date[1].equals("null")) {
                securedDateYear = date[1];
            } else {
                securedDateYear = "";
            }
        }

        return new OtherFunding(cost.getId(), cost.getItem(), cost.getDescription(), securedDateMonth, securedDateYear, cost.getCost());
    }
}
