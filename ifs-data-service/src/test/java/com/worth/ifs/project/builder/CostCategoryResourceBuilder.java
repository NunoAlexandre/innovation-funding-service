package com.worth.ifs.project.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.project.finance.domain.CostCategory;
import com.worth.ifs.project.finance.domain.CostCategoryGroup;
import com.worth.ifs.project.finance.resource.CostCategoryGroupResource;
import com.worth.ifs.project.finance.resource.CostCategoryResource;
import com.worth.ifs.project.finance.resource.CostResource;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class CostCategoryResourceBuilder extends BaseBuilder<CostCategoryResource, CostCategoryResourceBuilder> {

    private CostCategoryResourceBuilder(List<BiConsumer<Integer, CostCategoryResource>> multiActions) {
        super(multiActions);
    }

    public static CostCategoryResourceBuilder newCostCategoryResource() {
        return new CostCategoryResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected CostCategoryResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CostCategoryResource>> actions) {
        return new CostCategoryResourceBuilder(actions);
    }

    @Override
    protected CostCategoryResource createInitial() {
        return new CostCategoryResource();
    }


    public CostCategoryResourceBuilder withName(String... names) {
        return withArray((name, costCategory) -> setField("name", name, costCategory), names);
    }

    public CostCategoryResourceBuilder withCostCategoryGroup(CostCategoryGroupResource... costCategoryGroupResources) {
        return withArray((costCategoryGroupResource, costCategory) -> setField("costCategoryGroup", costCategoryGroupResource, costCategory), costCategoryGroupResources);
    }

}
