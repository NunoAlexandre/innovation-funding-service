package org.innovateuk.ifs.project.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.project.finance.domain.SpendProfile;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class SpendProfileBuilder extends BaseBuilder<SpendProfile, SpendProfileBuilder> {

    private SpendProfileBuilder(List<BiConsumer<Integer, SpendProfile>> multiActions) {
        super(multiActions);
    }

    public static SpendProfileBuilder newSpendProfile() {
        return new SpendProfileBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected SpendProfileBuilder createNewBuilderWithActions(List<BiConsumer<Integer, SpendProfile>> actions) {
        return new SpendProfileBuilder(actions);
    }

    @Override
    protected SpendProfile createInitial() {
        return new SpendProfile();
    }

    public SpendProfileBuilder withId(Long... ids) {
        return withArray((id, spendProfile) -> setField("id", id, spendProfile), ids);
    }

    public SpendProfileBuilder withOrganisation(Organisation... organisations) {
        return withArray((organisation, spendProfile) -> setField("organisation", organisation, spendProfile), organisations);
    }

    public SpendProfileBuilder withGeneratedBy(User... users) {
        return withArray((user, spendProfile) -> setField("generatedBy", user, spendProfile), users);
    }

    public SpendProfileBuilder withGeneratedDate(Calendar... dates) {
        return withArray((date, spendProfile) -> setField("generatedDate", date, spendProfile), dates);
    }

    public SpendProfileBuilder withApproval(ApprovalType... approvalTypes) {
        return withArray((approvalType, spendProfile) -> setField("approval", approvalType, spendProfile), approvalTypes);
    }

    public SpendProfileBuilder withMarkedComplete(Boolean... completed) {
        return withArray((complete, spendProfile) -> setField("markedAsComplete", complete, spendProfile), completed);
    }
}
