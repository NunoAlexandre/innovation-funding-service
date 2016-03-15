package com.worth.ifs.application.finance.view.item;

import com.worth.ifs.application.finance.model.FinanceFormField;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.SubContractingCost;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles the conversion of form fields to subcontracting costs
 */
public class SubContractingCostHandler extends CostHandler {

    @Override
    public CostItem toCostItem(Long id, List<FinanceFormField> financeFormFields) {
        BigDecimal cost = null;
        String country = null;
        String name = null;
        String role = null;

        for(FinanceFormField financeFormField : financeFormFields) {
            String fieldValue = financeFormField.getValue();
            if (fieldValue != null) {
                switch (financeFormField.getCostName()) {
                    case "country":
                        country = fieldValue;
                        break;
                    case "subcontractingCost":
                        cost = getBigDecimalValue(fieldValue, 0D);
                        break;
                    case "name":
                        name = fieldValue;
                        break;
                    case "role":
                        role = fieldValue;
                        break;
                    default:
                        log.info("Unused costField: " + financeFormField.getCostName());
                        break;
                }
            }
        }

        return new SubContractingCost(id, cost, country, name, role);
    }
}
