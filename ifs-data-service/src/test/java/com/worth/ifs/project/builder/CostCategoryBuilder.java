package com.worth.ifs.project.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.project.finance.domain.CostCategory;
import com.worth.ifs.project.finance.resource.CostCategoryResource;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class CostCategoryBuilder extends BaseBuilder<CostCategory, CostCategoryBuilder> {

    private CostCategoryBuilder(List<BiConsumer<Integer, CostCategory>> multiActions) {
        super(multiActions);
    }

    public static CostCategoryBuilder newCostCategory() {
        return new CostCategoryBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected CostCategoryBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CostCategory>> actions) {
        return new CostCategoryBuilder(actions);
    }

    @Override
    protected CostCategory createInitial() {
        return new CostCategory();
    }


    public CostCategoryBuilder withName(String... names) {
        return withArray((name, costCategory) -> setField("name", name, costCategory), names);
    }



}
