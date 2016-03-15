package com.worth.ifs.finance.handler.item;

import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.GrantClaim;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the grant claims, i.e. converts the costs to be stored into the database
 * or for sending it over.
 */
public class GrantClaimHandler extends CostHandler {
    public static final String GRANT_CLAIM = "Grant Claim";
    public static final String COST_KEY = "grant-claim";

    @Override
    public Cost toCost(CostItem costItem) {
        Cost cost = null;
        if (costItem instanceof GrantClaim) {
            GrantClaim grantClaim = (GrantClaim) costItem;
            return new Cost(grantClaim.getId(), COST_KEY, "", GRANT_CLAIM, grantClaim.getGrantClaimPercentage(), BigDecimal.ZERO, null,null);
        }
        return cost;
    }

    @Override
    public CostItem toCostItem(Cost cost) {
        return new GrantClaim(cost.getId(), cost.getQuantity());
    }

    @Override
    public List<Cost> initializeCost() {
        ArrayList<Cost> costs = new ArrayList<>();
        costs.add(initializeFundingLevel());
        return costs;
    }

    private Cost initializeFundingLevel() {
        GrantClaim costItem = new GrantClaim();
        costItem.setGrantClaimPercentage(null);
        return toCost(costItem);
    }
}
