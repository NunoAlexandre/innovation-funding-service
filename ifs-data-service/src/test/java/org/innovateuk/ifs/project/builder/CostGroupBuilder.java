package org.innovateuk.ifs.project.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.project.financecheck.domain.Cost;
import org.innovateuk.ifs.project.financecheck.domain.CostGroup;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class CostGroupBuilder extends BaseBuilder<CostGroup, CostGroupBuilder> {

    private CostGroupBuilder(List<BiConsumer<Integer, CostGroup>> multiActions) {
        super(multiActions);
    }

    public static CostGroupBuilder newCostGroup() {
        return new CostGroupBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected CostGroupBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CostGroup>> actions) {
        return new CostGroupBuilder(actions);
    }

    @Override
    protected CostGroup createInitial() {
        return new CostGroup();
    }


    public CostGroupBuilder withCosts(List<Cost>... costs) {
        return withArray((cost, costGroup) -> setField("costs", cost, costGroup), costs);
    }

    public CostGroupBuilder withDescription(String... descriptions) {
        return withArray((description, costGroupResource) -> setField("description", description, costGroupResource), descriptions);
    }
}
