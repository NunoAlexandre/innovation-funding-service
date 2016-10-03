package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.category.FinanceRowCostCategory;
import com.worth.ifs.finance.resource.cost.AcademicCostCategoryGenerator;
import com.worth.ifs.finance.resource.cost.CostCategoryGenerator;
import com.worth.ifs.finance.resource.cost.FinanceRowType;
import com.worth.ifs.finance.transactional.FinanceRowService;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.project.finance.domain.CostCategory;
import com.worth.ifs.project.finance.domain.CostCategoryGroup;
import com.worth.ifs.project.finance.domain.CostCategoryType;
import com.worth.ifs.project.finance.repository.CostCategoryTypeRepository;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.transactional.OrganisationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.user.resource.OrganisationTypeEnum.isResearch;
import static com.worth.ifs.util.CollectionFunctions.*;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.EnumSet.allOf;

/**
 * An implementation of CostCategoryTypeStrategy that uses the original Application Finances for the given Organisation
 * to search for an existing CostCategoryType that supports its Cost Categories
 */
@Component
public class ByApplicationFinanceCostCategoriesStrategy implements CostCategoryTypeStrategy {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private FinanceRowService financeRowService;

    @Autowired
    private CostCategoryTypeRepository costCategoryTypeRepository;

    @Override
    public ServiceResult<CostCategoryType> getOrCreateCostCategoryTypeForSpendProfile(Long projectId, Long organisationId) {
        return find(project(projectId), organisation(organisationId)).
                andOnSuccess((project, organisation) ->
                        find(applicationFinanceResource(project.getApplication(), organisation.getId())).
                                andOnSuccess((finances) -> {
                                    Set<? extends CostCategoryGenerator> costCategoryGenerators;
                                    if (!isResearch(organisation.getOrganisationType())) {
                                        Map<FinanceRowType, FinanceRowCostCategory> financeOrganisationDetails = finances.getFinanceOrganisationDetails();
                                        costCategoryGenerators = financeOrganisationDetails.keySet();
                                    }
                                    else {
                                        costCategoryGenerators = allOf(AcademicCostCategoryGenerator.class);
                                    }
                                    return serviceSuccess(getOrCreateSupportingCostCategoryType(costCategoryGenerators));
                                }));
    }

    private CostCategoryType getOrCreateSupportingCostCategoryType(Set<? extends CostCategoryGenerator> summaryPerCategory) {
        // Get the generators for the CostCategories we may need to generate
        List<CostCategoryGenerator> costCategoryGenerators = simpleFilter(summaryPerCategory, CostCategoryGenerator::isSpendCostCategory);
        // Get all of the CostCategoryTypes so we can find out if there is already a logical grouping of CostCategories that fulfils our needs
        List<CostCategoryType> existingCostCategoryTypes = costCategoryTypeRepository.findAll();
        Optional<CostCategoryType> existingCostCategoryTypeWithMatchingCategories = simpleFindFirst(existingCostCategoryTypes, costCategoryType -> {
            List<CostCategory> costCategories = costCategoryType.getCostCategories();
            return costCategories.size() == costCategoryGenerators.size() &&
                    containsAll(costCategories, CostCategory::getName, costCategoryGenerators, CostCategoryGenerator::getName);
        });

        return existingCostCategoryTypeWithMatchingCategories.orElseGet(() -> {
            // We do not have the relevant CostCategories so we generate them.
            // We need CostCategories
            List<CostCategory> costCategories = simpleMap(costCategoryGenerators, this::newCostCategory);
            // We need a CostCategoryGroup - a logical grouping of the CostCategories with a description
            String costCategoryGroupDescription = "Cost Category Group for Categories " + simpleJoiner(costCategoryGenerators, CostCategoryGenerator::getName, ", ");
            CostCategoryGroup costCategoryGroup = new CostCategoryGroup(costCategoryGroupDescription, costCategories);
            // We need a CostCategoryType - a description of the CostCategoryGroup. E.g. currently we would expect one for Industrial and one for Academic
            String costCategoryTypeName = "Cost Category Type for Categories " + simpleJoiner(costCategoryGenerators, CostCategoryGenerator::getName, ", ");
            CostCategoryType costCategoryTypeToCreate = new CostCategoryType(costCategoryTypeName, costCategoryGroup);
            return costCategoryTypeRepository.save(costCategoryTypeToCreate);
        });
    }

    private CostCategory newCostCategory(CostCategoryGenerator costCategoryGenerator){
        CostCategory newCostCategory = new CostCategory(costCategoryGenerator.getName());
        newCostCategory.setLabel(costCategoryGenerator.getLabel());
        return newCostCategory;
    }

    private Supplier<ServiceResult<ProjectResource>> project(Long projectId) {
        return () -> projectService.getProjectById(projectId);
    }

    private Supplier<ServiceResult<OrganisationResource>> organisation(Long organisationId) {
        return () -> organisationService.findById(organisationId);
    }

    private Supplier<ServiceResult<ApplicationFinanceResource>> applicationFinanceResource(Long applicationId, Long organisationId) {
        return () -> financeRowService.financeDetails(applicationId, organisationId);
    }
}
