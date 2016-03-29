package com.worth.ifs.application.finance.view.item;

import com.worth.ifs.application.finance.model.FinanceFormField;
import com.worth.ifs.finance.resource.cost.CapitalUsage;
import com.worth.ifs.finance.resource.cost.CostItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles the conversion of form fields to a cost item
 */
public class CapitalUsageHandler extends CostHandler {

    @Override
    public CostItem toCostItem(Long id, List<FinanceFormField> financeFormFields) {
        Integer deprecation = null;
        String description = null;
        String existing = null;
        BigDecimal npv = null;
        BigDecimal residualValue = null;
        Integer utilisation = null;

        for(FinanceFormField financeFormField : financeFormFields) {
            final String costFormValue = financeFormField.getValue();
            switch (financeFormField.getCostName()) {
                case "item":
                    description = costFormValue;
                    break;
                case "existing":
                    existing = costFormValue;
                    break;
                case "deprecation_period":
                    deprecation = Integer.valueOf(costFormValue);
                    break;
                case "npv":
                    npv = getBigDecimalValue(costFormValue, 0d);
                    break;
                case "residual_value":
                    residualValue = getBigDecimalValue(costFormValue, 0d);
                    break;
                case "utilisation":
                    utilisation = Integer.valueOf(costFormValue);
                    break;
                default:
                    log.info("Unused costField: " + financeFormField.getCostName());
                    break;
            }
        }

        return new CapitalUsage(id, deprecation, description, existing,
                npv, residualValue, utilisation);
    }

}
