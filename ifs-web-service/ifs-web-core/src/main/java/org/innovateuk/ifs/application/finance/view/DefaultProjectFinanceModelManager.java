package org.innovateuk.ifs.application.finance.view;

import org.innovateuk.ifs.application.finance.viewmodel.FinanceViewModel;
import org.innovateuk.ifs.application.finance.viewmodel.ProjectFinanceViewModel;
import org.innovateuk.ifs.application.form.Form;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.category.LabourCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.project.finance.ProjectFinanceService;
import org.innovateuk.ifs.user.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.service.OrganisationTypeRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Managing all the view attributes for the finances
 */
@Component
public class DefaultProjectFinanceModelManager implements FinanceModelManager {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ProjectFinanceService financeService;

    @Autowired
    private OrganisationTypeRestService organisationTypeService;

    //TODO: INFUND-7849 - make sure this function is not going to be used anymore
    @Override
    public void addOrganisationFinanceDetails(Model model, Long projectId, List<QuestionResource> costsQuestions, Long userId, Form form, Long organisationId) {

        ProjectFinanceResource projectFinanceResource = getOrganisationFinances(projectId, userId, organisationId);

        if (projectFinanceResource != null) {
            OrganisationTypeResource organisationType = organisationTypeService.getForOrganisationId(projectFinanceResource.getOrganisation()).getSuccessObjectOrThrowException();
            model.addAttribute("organisationFinance", projectFinanceResource.getFinanceOrganisationDetails());
            model.addAttribute("organisationFinanceSize", projectFinanceResource.getOrganisationSize());
            model.addAttribute("organisationType", organisationType);
            model.addAttribute("organisationFinanceId", projectFinanceResource.getId());
            model.addAttribute("organisationFinanceTotal", projectFinanceResource.getTotal());
            model.addAttribute("financeView", "finance");
            addGrantClaim(model, form, projectFinanceResource);
        }
    }

    private void addGrantClaim(Model model, Form form, ProjectFinanceResource projectFinanceResource) {
        if(projectFinanceResource.getGrantClaim()!=null) {
            model.addAttribute("organisationGrantClaimPercentage", projectFinanceResource.getGrantClaim().getGrantClaimPercentage());
            model.addAttribute("organisationgrantClaimPercentageId", projectFinanceResource.getGrantClaim().getId());
            String formInputKey = "finance-grantclaimpercentage-" + projectFinanceResource.getGrantClaim();
            String formInputValue = projectFinanceResource.getGrantClaimPercentage() != null ? projectFinanceResource.getGrantClaimPercentage().toString() : "";
            form.addFormInput(formInputKey, formInputValue);
        }
    }

    @Override
    public FinanceViewModel getFinanceViewModel(Long projectId, List<QuestionResource> costsQuestions, Long userId, Form form, Long organisationId) {
        ProjectFinanceViewModel financeViewModel = new ProjectFinanceViewModel();
        ProjectFinanceResource projectFinanceResource = getOrganisationFinances(projectId, userId, organisationId);

        if (projectFinanceResource != null) {
            OrganisationTypeResource organisationType = organisationTypeService.getForOrganisationId(projectFinanceResource.getOrganisation()).getSuccessObjectOrThrowException();
            financeViewModel.setOrganisationFinance(projectFinanceResource.getFinanceOrganisationDetails());
            financeViewModel.setOrganisationFinanceSize(projectFinanceResource.getOrganisationSize());
            financeViewModel.setOrganisationType(organisationType);
            financeViewModel.setOrganisationFinanceId(projectFinanceResource.getId());
            financeViewModel.setOrganisationFinanceTotal(projectFinanceResource.getTotal());
            financeViewModel.setFinanceView("finance");
            addGrantClaim(financeViewModel, form, projectFinanceResource);
            financeViewModel.setChanges(projectFinanceResource.getCostChanges());
        }

        return financeViewModel;
    }

    private void addGrantClaim(FinanceViewModel financeViewModel, Form form, ProjectFinanceResource projectFinanceResource) {
        if(projectFinanceResource.getGrantClaim()!=null) {
            financeViewModel.setOrganisationGrantClaimPercentage(projectFinanceResource.getGrantClaim().getGrantClaimPercentage());
            financeViewModel.setOrganisationGrantClaimPercentageId(projectFinanceResource.getGrantClaim().getId());
            String formInputKey = "finance-grantclaimpercentage-" + projectFinanceResource.getGrantClaim();
            String formInputValue = projectFinanceResource.getGrantClaimPercentage() != null ? projectFinanceResource.getGrantClaimPercentage().toString() : "";
            form.addFormInput(formInputKey, formInputValue);
        }
    }

    protected ProjectFinanceResource getOrganisationFinances(Long projectId, Long userId, Long organisationId) {
        ProjectFinanceResource projectFinanceResource = financeService.getProjectFinance(projectId, organisationId);
        if(projectFinanceResource == null) {
            financeService.addProjectFinance(userId, projectId);
            // ugly fix since the addProjectFinance method does not return the correct results.
            projectFinanceResource = financeService.getProjectFinance(projectId, organisationId);
        }
        return projectFinanceResource;
    }

	@Override
    public void addCost(Model model, FinanceRowItem costItem, long projectId, long organisationId, long userId, Long questionId, FinanceRowType costType) {
        if (FinanceRowType.LABOUR == costType) {
            ProjectFinanceResource projectFinanceResource = financeService.getProjectFinance(projectId, organisationId);
            LabourCostCategory costCategory = (LabourCostCategory) projectFinanceResource.getFinanceOrganisationDetails(FinanceRowType.LABOUR);
            model.addAttribute("costCategory", costCategory);
        }

        model.addAttribute("type", costType.getType());
        model.addAttribute("question", questionService.getById(questionId));
        model.addAttribute("cost", costItem);
    }
}
