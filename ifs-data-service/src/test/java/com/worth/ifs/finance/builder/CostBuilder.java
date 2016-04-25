package com.worth.ifs.finance.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.idBasedDescriptions;
import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for Cost entities.
 */
public class CostBuilder extends BaseBuilder<Cost, CostBuilder> {

    private CostBuilder(List<BiConsumer<Integer, Cost>> newMultiActions) {
        super(newMultiActions);
    }

    public static CostBuilder newCost() {
        return new CostBuilder(emptyList()).
                with(uniqueIds()).
                with(idBasedDescriptions("Description "));
    }

    @Override
    protected CostBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Cost>> actions) {
        return new CostBuilder(actions);
    }

    @Override
    protected Cost createInitial() {
        return new Cost();
    }

    public CostBuilder withItem(String item){
        return with(cost -> setField("item", item, cost));
    }

    public CostBuilder withApplicationFinance(ApplicationFinance applicationFinance){
        return with(cost -> setField("applicationFinance", applicationFinance, cost));
    }
}
