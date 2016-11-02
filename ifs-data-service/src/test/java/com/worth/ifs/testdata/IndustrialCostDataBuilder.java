package com.worth.ifs.testdata;

import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.cost.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.worth.ifs.finance.builder.LabourCostBuilder.newLabourCost;
import static java.util.Collections.emptyList;


public class IndustrialCostDataBuilder extends BaseDataBuilder<IndustrialCostData, IndustrialCostDataBuilder> {

    public IndustrialCostDataBuilder withApplicationFinance(ApplicationFinanceResource applicationFinance) {
        return with(data -> data.setApplicationFinance(applicationFinance));
    }

    public IndustrialCostDataBuilder withCompetition(CompetitionResource competitionResource) {
        return with(data -> data.setCompetition(competitionResource));
    }

    public IndustrialCostDataBuilder withLabourEntry(String role, Integer annualSalary, Integer daysToBeSpent) {
        return addCostItem(() ->
                newLabourCost().withId().
                    withName().
                    withRole(role).
                    withGrossAnnualSalary(bd(annualSalary)).
                    withLabourDays(daysToBeSpent).
                    withDescription().
                    build());
    }

    public IndustrialCostDataBuilder withAdministrationSupportCostsNone() {
        return doSetAdministrativeSupportCosts(OverheadRateType.NONE, OverheadRateType.NONE.getRate());
    }

    public IndustrialCostDataBuilder withAdministrationSupportCostsDefaultRate() {
        return doSetAdministrativeSupportCosts(OverheadRateType.DEFAULT_PERCENTAGE, OverheadRateType.DEFAULT_PERCENTAGE.getRate());
    }

    public IndustrialCostDataBuilder withAdministrationSupportCostsCustomRate(Integer customRate) {
        return doSetAdministrativeSupportCosts(OverheadRateType.CUSTOM_RATE, customRate);
    }

    private IndustrialCostDataBuilder doSetAdministrativeSupportCosts(OverheadRateType rateType, Integer rate) {
        return updateCostItem(FinanceRowType.OVERHEADS, existingCost -> {
            Overhead updated = new Overhead(existingCost.getId(), rateType, rate);
            financeRowService.updateCost(existingCost.getId(), updated);
        });
    }

    private <T extends FinanceRowItem> IndustrialCostDataBuilder updateCostItem(FinanceRowType financeRowType, Consumer<T> updateFn) {
        return with(data -> {

            QuestionResource question = retrieveQuestionByCompetitionAndName(financeRowType.getName(), data.getCompetition());

            List<FinanceRowItem> existingItems = financeRowService.getCostItems(data.getApplicationFinance().getId(), question.getId()).getSuccessObjectOrThrowException();
            existingItems.forEach(item -> updateFn.accept((T) item));
        });
    }

    private IndustrialCostDataBuilder addCostItem(Supplier<FinanceRowItem> cost) {
        return with(data -> {

            FinanceRowItem newCostItem = cost.get();

            QuestionResource question = retrieveQuestionByCompetitionAndName(newCostItem.getCostType().getName(),
                    data.getCompetition());

            financeRowService.addCost(data.getApplicationFinance().getId(), question.getId(), newCostItem).
                    getSuccessObjectOrThrowException();
        });
    }

    public static IndustrialCostDataBuilder newIndustrialCostData(ServiceLocator serviceLocator) {
        return new IndustrialCostDataBuilder(emptyList(), serviceLocator);
    }

    private IndustrialCostDataBuilder(List<BiConsumer<Integer, IndustrialCostData>> multiActions,
                                      ServiceLocator serviceLocator) {

        super(multiActions, serviceLocator);
    }

    @Override
    protected IndustrialCostDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, IndustrialCostData>> actions) {
        return new IndustrialCostDataBuilder(actions, serviceLocator);
    }

    @Override
    protected IndustrialCostData createInitial() {
        return new IndustrialCostData();
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private BigDecimal bd(Integer value) {
        return BigDecimal.valueOf(value);
    }

}
