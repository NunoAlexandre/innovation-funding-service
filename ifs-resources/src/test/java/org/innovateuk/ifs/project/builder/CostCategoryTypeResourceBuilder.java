package org.innovateuk.ifs.project.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.project.finance.resource.CostCategoryResource;
import org.innovateuk.ifs.project.finance.resource.CostCategoryTypeResource;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class CostCategoryTypeResourceBuilder extends BaseBuilder<CostCategoryTypeResource, CostCategoryTypeResourceBuilder> {

    private CostCategoryTypeResourceBuilder(List<BiConsumer<Integer, CostCategoryTypeResource>> multiActions) {
        super(multiActions);
    }

    public static CostCategoryTypeResourceBuilder newCostCategoryTypeResource() {
        return new CostCategoryTypeResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected CostCategoryTypeResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CostCategoryTypeResource>> actions) {
        return new CostCategoryTypeResourceBuilder(actions);
    }

    @Override
    protected CostCategoryTypeResource createInitial() {
        return new CostCategoryTypeResource();
    }

}
