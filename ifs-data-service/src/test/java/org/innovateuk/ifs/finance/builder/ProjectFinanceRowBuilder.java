package org.innovateuk.ifs.finance.builder;

import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.ProjectFinance;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.BuilderAmendFunctions.idBasedDescriptions;
import static org.innovateuk.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for ProjectFinanceRow entities.
 */
public class ProjectFinanceRowBuilder extends BaseFinanceRowBuilder<ProjectFinance, ProjectFinanceRow, ProjectFinanceRowBuilder> {

    public ProjectFinanceRowBuilder withOriginalApplicationFinanceRow(ApplicationFinanceRow... value) {
        return withArray((v, financeRow) -> financeRow.setApplicationRowId(v.getId()), value);
    }

    private ProjectFinanceRowBuilder(List<BiConsumer<Integer, ProjectFinanceRow>> newMultiActions) {
        super(newMultiActions);
    }

    public static ProjectFinanceRowBuilder newProjectFinanceRow() {
        return new ProjectFinanceRowBuilder(emptyList()).
                with(uniqueIds()).
                with(idBasedDescriptions("Description "));
    }

    @Override
    protected ProjectFinanceRowBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ProjectFinanceRow>> actions) {
        return new ProjectFinanceRowBuilder(actions);
    }

    @Override
    protected ProjectFinanceRow createInitial() {
        return new ProjectFinanceRow();
    }
}
