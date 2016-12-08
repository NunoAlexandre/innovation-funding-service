package com.worth.ifs.finance.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.finance.domain.FinanceRowMetaField;
import com.worth.ifs.finance.domain.FinanceRowMetaValue;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for {@link FinanceRowMetaValue} entities.
 */
public class FinanceRowMetaValueBuilder extends BaseBuilder<FinanceRowMetaValue, FinanceRowMetaValueBuilder> {

    public FinanceRowMetaValueBuilder withFinanceRowMetaField(final FinanceRowMetaField... costFieldId){
        return withArray((v, metaValue) -> metaValue.setFinanceRowMetaField(v), costFieldId);
    }

    public FinanceRowMetaValueBuilder withFinanceRow(final Long... costId) {
        return withArray((v, metaValue) -> metaValue.setFinanceRowId(v), costId);
    }

    public FinanceRowMetaValueBuilder withValue(final String... value) {
        return withArray((v, metaValue) -> metaValue.setValue(v), value);
    }

    private FinanceRowMetaValueBuilder(List<BiConsumer<Integer, FinanceRowMetaValue>> newMultiActions) {
        super(newMultiActions);
    }

    public static FinanceRowMetaValueBuilder newFinanceRowMetaValue() {
        return new FinanceRowMetaValueBuilder(emptyList()).
                with(uniqueIds());
    }

    @Override
    protected FinanceRowMetaValueBuilder createNewBuilderWithActions(List<BiConsumer<Integer, FinanceRowMetaValue>> actions) {
        return new FinanceRowMetaValueBuilder(actions);
    }

    @Override
    protected FinanceRowMetaValue createInitial() {
        return new FinanceRowMetaValue();
    }
}
