package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.handler.OrganisationFinanceDelegate;
import com.worth.ifs.finance.resource.ProjectFinanceResource;
import com.worth.ifs.finance.resource.category.FinanceRowCostCategory;
import com.worth.ifs.finance.resource.cost.AcademicCostCategoryGenerator;
import com.worth.ifs.finance.resource.cost.FinanceRowItem;
import com.worth.ifs.finance.resource.cost.FinanceRowType;
import com.worth.ifs.finance.transactional.FinanceRowService;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.project.finance.domain.CostCategory;
import com.worth.ifs.project.finance.domain.CostCategoryType;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.user.resource.OrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * Implementation of SpendProfileCostCategorySummaryStrategy that looks to the Project Finances (i.e. the Project's
 * Finance Checks version of the original Application Finances) in order to generate a summary of each Cost Category
 * for a Partner Organisation for the purposes of generating a Spend Profile
 */
@Component
@ConditionalOnProperty(value = "ifs.spend.profile.generation.strategy", havingValue = "ByProjectFinanceCostCategorySummaryStrategy")
public class ByProjectFinanceCostCategorySummaryStrategy implements SpendProfileCostCategorySummaryStrategy {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FinanceRowService financeRowService;

    @Autowired
    private CostCategoryTypeStrategy costCategoryTypeStrategy;

    @Autowired
    private OrganisationFinanceDelegate organisationFinanceDelegate;

    @Autowired
    private OrganisationService organisationService;

    @Override
    public ServiceResult<SpendProfileCostCategorySummaries> getCostCategorySummaries(Long projectId, Long organisationId) {

        return projectService.getProjectById(projectId).andOnSuccess(project ->
               organisationService.findById(organisationId).andOnSuccess(organisation ->
               financeRowService.financeChecksDetails(project.getId(), organisationId).andOnSuccess(finances ->
               createCostCategorySummariesWithCostCategoryType(projectId, organisationId, project, organisation, finances))));
    }

    private ServiceResult<SpendProfileCostCategorySummaries> createCostCategorySummariesWithCostCategoryType(
            Long projectId, Long organisationId, ProjectResource project, OrganisationResource organisation, ProjectFinanceResource finances) {

        boolean useAcademicFinances = organisationFinanceDelegate.isUsingJesFinances(organisation.getOrganisationTypeName());

        return costCategoryTypeStrategy.getOrCreateCostCategoryTypeForSpendProfile(projectId, organisationId).andOnSuccessReturn(
                costCategoryType ->
                       createCostCategorySummariesWithCostCategoryType(project, finances, useAcademicFinances, costCategoryType));
    }

    private SpendProfileCostCategorySummaries createCostCategorySummariesWithCostCategoryType(
            ProjectResource project, ProjectFinanceResource finances, boolean useAcademicFinances, CostCategoryType costCategoryType) {

        List<SpendProfileCostCategorySummary> costCategorySummaries = new ArrayList<>();
        Map<CostCategory, BigDecimal> totalsPerCostCategory = getTotalsPerCostCategory(finances, useAcademicFinances, costCategoryType);
        totalsPerCostCategory.forEach((cc, total) -> costCategorySummaries.add(new SpendProfileCostCategorySummary(cc, total, project.getDurationInMonths())));

        return new SpendProfileCostCategorySummaries(costCategorySummaries, costCategoryType);
    }

    private Map<CostCategory, BigDecimal> getTotalsPerCostCategory(ProjectFinanceResource finances, boolean useAcademicFinances, CostCategoryType costCategoryType) {

        Map<FinanceRowType, FinanceRowCostCategory> spendRows = getSpendProfileCostCategories(finances);
        return getTotalsPerCostCategory(useAcademicFinances, costCategoryType, spendRows);
    }

    private Map<CostCategory, BigDecimal> getTotalsPerCostCategory(boolean useAcademicFinances, CostCategoryType costCategoryType, Map<FinanceRowType, FinanceRowCostCategory> spendRows) {

        return useAcademicFinances ?
                getAcademicTotalsPerCostCategory(costCategoryType, spendRows) :
                getIndustrialTotalsPerCostCategory(costCategoryType, spendRows);
    }

    private Map<FinanceRowType, FinanceRowCostCategory> getSpendProfileCostCategories(ProjectFinanceResource finances) {

        Map<FinanceRowType, FinanceRowCostCategory> financeOrganisationDetails = finances.getFinanceOrganisationDetails();
        return simpleFilter(financeOrganisationDetails, (category, costs) -> category.isSpendCostCategory());
    }

    private Map<CostCategory, BigDecimal> getIndustrialTotalsPerCostCategory(CostCategoryType costCategoryType, Map<FinanceRowType, FinanceRowCostCategory> spendRows) {

        Map<CostCategory, BigDecimal> valuesPerCostCategory = new HashMap<>();

        for (Map.Entry<FinanceRowType, FinanceRowCostCategory> costCategoryDetails : spendRows.entrySet()) {
            CostCategory costCategory = findIndustrialCostCategoryForName(costCategoryType, costCategoryDetails.getKey().getName());
            valuesPerCostCategory.put(costCategory, costCategoryDetails.getValue().getTotal());
        }

        return valuesPerCostCategory;
    }

    private Map<CostCategory, BigDecimal> getAcademicTotalsPerCostCategory(CostCategoryType costCategoryType, Map<FinanceRowType, FinanceRowCostCategory> spendRows) {

        Map<CostCategory, BigDecimal> valuesPerCostCategory = new HashMap<>();
        costCategoryType.getCostCategories().forEach(cc -> valuesPerCostCategory.put(cc, BigDecimal.ZERO));

        for (FinanceRowCostCategory costCategoryDetails : spendRows.values()) {

            List<FinanceRowItem> costs = costCategoryDetails.getCosts();

            for (FinanceRowItem cost : costs) {

                String costCategoryName = cost.getName();
                CostCategory costCategory = findAcademicCostCategoryForName(costCategoryType, costCategoryName);
                BigDecimal value = cost.getTotal();

                BigDecimal currentValue = valuesPerCostCategory.get(costCategory);
                valuesPerCostCategory.put(costCategory, currentValue.add(value));
            }
        }
        return valuesPerCostCategory;
    }

    private CostCategory findAcademicCostCategoryForName(CostCategoryType costCategoryType, String costCategoryName) {
        AcademicCostCategoryGenerator academicCostCategoryMatch = AcademicCostCategoryGenerator.fromFinanceRowName(costCategoryName);
        return simpleFindFirst(costCategoryType.getCostCategories(), cat ->
                        cat.getName().equals(academicCostCategoryMatch.getName()) &&
                        cat.getLabel().equals(academicCostCategoryMatch.getLabel())).
                get();
    }

    private CostCategory findIndustrialCostCategoryForName(CostCategoryType costCategoryType, String costCategoryName) {
        return simpleFindFirst(costCategoryType.getCostCategories(), cat -> cat.getName().equals(costCategoryName)).get();
    }
}