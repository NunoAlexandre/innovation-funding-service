package org.innovateuk.ifs.application.finance.view.jes;

import org.innovateuk.ifs.application.finance.form.AcademicFinance;
import org.innovateuk.ifs.application.finance.model.AcademicFinanceFormField;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.view.FinanceModelManager;
import org.innovateuk.ifs.application.finance.viewmodel.AcademicFinanceViewModel;
import org.innovateuk.ifs.application.form.Form;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.AcademicCost;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class JESProjectFinanceModelManager implements FinanceModelManager {

    @Autowired
    ProcessRoleService processRoleService;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    FinanceService financeService;

    @Override
    public void addOrganisationFinanceDetails(Model model, Long applicationId, List<QuestionResource> costsQuestions, Long userId, Form form, Long organisationId) {
        ApplicationFinanceResource applicationFinanceResource = getOrganisationFinances(applicationId, userId);

        if (applicationFinanceResource != null) {

            ProcessRoleResource processRole = processRoleService.findProcessRole(userId, applicationId);
            OrganisationResource organisationResource = organisationService.getOrganisationById(processRole.getOrganisationId());

            Map<FinanceRowType, FinanceRowCostCategory> organisationFinanceDetails = applicationFinanceResource.getFinanceOrganisationDetails();
            AcademicFinance academicFinance = mapFinancesToFields(organisationFinanceDetails);
            if(applicationFinanceResource.getFinanceFileEntry() != null) {
                financeService.getFinanceEntry(applicationFinanceResource.getFinanceFileEntry()).andOnSuccessReturn(
                        fileEntry -> {
                            model.addAttribute("filename", fileEntry.getName());
                            return fileEntry;
                        }
                );
            }

            model.addAttribute("title", organisationResource.getName() + " finances");
            model.addAttribute("applicationFinanceId", applicationFinanceResource.getId());
            model.addAttribute("academicFinance", academicFinance);
        }

        model.addAttribute("financeView", "academic-finance");
    }

    @Override
    public AcademicFinanceViewModel getFinanceViewModel(Long applicationId, List<QuestionResource> costsQuestions, Long userId, Form form, Long organisationId) {
        AcademicFinanceViewModel financeViewModel = new AcademicFinanceViewModel();
        ApplicationFinanceResource applicationFinanceResource = getOrganisationFinances(applicationId, userId);

        if (applicationFinanceResource != null) {

            ProcessRoleResource processRole = processRoleService.findProcessRole(userId, applicationId);
            OrganisationResource organisationResource = organisationService.getOrganisationById(processRole.getOrganisationId());

            Map<FinanceRowType, FinanceRowCostCategory> organisationFinanceDetails = applicationFinanceResource.getFinanceOrganisationDetails();
            AcademicFinance academicFinance = mapFinancesToFields(organisationFinanceDetails);
            if(applicationFinanceResource.getFinanceFileEntry() != null) {
                financeService.getFinanceEntry(applicationFinanceResource.getFinanceFileEntry()).andOnSuccessReturn(
                        fileEntry -> {
                            financeViewModel.setFilename(fileEntry.getName());
                            return fileEntry;
                        }
                );
            }

            financeViewModel.setTitle(organisationResource.getName() + " finances");
            financeViewModel.setApplicationFinanceId(applicationFinanceResource.getId());
            financeViewModel.setAcademicFinance(academicFinance);
        }

        financeViewModel.setFinanceView("academic-finance");
        return financeViewModel;
    }

    @Override
    public void addCost(Model model, FinanceRowItem costItem, long applicationId, long organisationId,  long userId, Long questionId, FinanceRowType costType) {

    }

    protected ApplicationFinanceResource getOrganisationFinances(Long applicationId, Long userId) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        if(applicationFinanceResource == null) {
            financeService.addApplicationFinance(userId, applicationId);
            applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        }
        return applicationFinanceResource;
    }

    protected AcademicFinance mapFinancesToFields(Map<FinanceRowType, FinanceRowCostCategory> organisationFinanceDetails) {
        AcademicFinance academicFinance = new AcademicFinance();
        organisationFinanceDetails.values()
                .stream()
                .flatMap(cc -> cc.getCosts().stream())
                .filter(c -> c != null)
                .forEach(c -> mapFinanceToField((AcademicCost) c, academicFinance));
        return academicFinance;
    }

    private void mapFinanceToField(AcademicCost cost, AcademicFinance academicFinance) {
        String key = cost.getName();
        if(key==null) {
            return;
        }
        BigDecimal total = cost.getTotal();
        String totalValue = "";
        if(total!=null) {
            totalValue = total.toPlainString();
        }

        switch (key) {
            case "tsb_reference":
                academicFinance.setTsbReference(new AcademicFinanceFormField(cost.getId(), cost.getItem(), BigDecimal.ZERO));
                break;
            case "incurred_staff":
                academicFinance.setIncurredStaff(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "incurred_travel_subsistence":
                academicFinance.setIncurredTravelAndSubsistence(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "incurred_other_costs":
                academicFinance.setIncurredOtherCosts(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "allocated_investigators":
                academicFinance.setAllocatedInvestigators(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "allocated_estates_costs":
                academicFinance.setAllocatedEstatesCosts(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "allocated_other_costs":
                academicFinance.setAllocatedOtherCosts(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "indirect_costs":
                academicFinance.setIndirectCosts(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "exceptions_staff":
                academicFinance.setExceptionsStaff(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
            case "exceptions_other_costs":
                academicFinance.setExceptionsOtherCosts(new AcademicFinanceFormField(cost.getId(), totalValue, total));
                break;
        }
    }
 }
