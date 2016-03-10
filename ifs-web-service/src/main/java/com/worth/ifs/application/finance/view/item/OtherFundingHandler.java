package com.worth.ifs.application.finance.view.item;

import com.worth.ifs.application.finance.model.FinanceFormField;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.OtherFunding;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles the conversion of form fields to other funding item
 */
public class OtherFundingHandler extends CostHandler {

    @Override
    public CostItem toCostItem(Long id, List<FinanceFormField> financeFormFields) {
        String otherPublicFunding = null;
        String fundingSource = null;
        String dateSecured = null;
        BigDecimal fundingAmount = null;

        for (FinanceFormField financeFormField : financeFormFields) {
            String fieldValue = financeFormField.getValue();
            if (fieldValue != null) {
                switch (financeFormField.getCostName()) {
                    case "otherPublicFunding":
                        fundingSource = "Other Funding";
                        otherPublicFunding = fieldValue;
                        break;
                    case "fundingAmount":
                        fundingAmount = getBigDecimalValue(fieldValue, 0d);
                        break;
                    case "fundingSource":
                        fundingSource = fieldValue;
                        break;
                    case "dateSecured":
                        dateSecured = fieldValue;
                        break;
                    default:
                        log.info("Unused costField: " + financeFormField.getCostName());
                        break;
                }
            }
        }

        return new OtherFunding(id, otherPublicFunding, fundingSource, dateSecured, fundingAmount);
    }
}
