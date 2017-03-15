package org.innovateuk.ifs.project;

import org.innovateuk.ifs.commons.rest.LocalDateResource;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.project.finance.ProjectFinanceService;
import org.innovateuk.ifs.project.form.TotalSpendProfileForm;
import org.innovateuk.ifs.project.model.SpendProfileSummaryModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.SpendProfileTableResource;
import org.innovateuk.ifs.project.util.SpendProfileTableCalculator;
import org.innovateuk.ifs.project.viewmodel.TotalProjectSpendProfileTableViewModel;
import org.innovateuk.ifs.project.viewmodel.TotalSpendProfileViewModel;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.util.PrioritySorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.util.CollectionFunctions.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * This controller will handle all requests that are related to the reviewing and sending of total project spend profiles.
 */
@Controller
@RequestMapping("/" + TotalProjectSpendProfileController.BASE_DIR + "/{projectId}/spend-profile/total")
@PreAuthorize("hasAnyAuthority('project_finance', 'applicant', 'comp_admin')")
public class TotalProjectSpendProfileController {

    public static final String BASE_DIR = "project";
    private static final String FORM_ATTR_NAME = "form";
    private static final String SPEND_PROFILE_TOTALS_TEMPLATE = BASE_DIR + "/spend-profile-totals";

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectFinanceService projectFinanceService;
    @Autowired
    private SpendProfileTableCalculator spendProfileTableCalculator;

    @RequestMapping(method = GET)
    public String totals(Model model, @PathVariable("projectId") final Long projectId) {
        model.addAttribute("model", buildTotalViewModel(projectId));
        model.addAttribute(FORM_ATTR_NAME, new TotalSpendProfileForm());
        return SPEND_PROFILE_TOTALS_TEMPLATE;
    }

    @RequestMapping(value = "confirmation", method = GET)
    public String confirmation(@PathVariable("projectId") final Long projectId) {
        return BASE_DIR + "/spend-profile-total-confirmation";
    }

    @RequestMapping(method = POST)
    public String sendForReview(@PathVariable("projectId") final Long projectId,
                                @ModelAttribute(FORM_ATTR_NAME) TotalSpendProfileForm form,
                                @SuppressWarnings("unused") BindingResult bindingResult,
                                ValidationHandler validationHandler,
                                Model model) {
        return validationHandler.performActionOrBindErrorsToField("",
                () -> {
                    model.addAttribute("model", buildTotalViewModel(projectId));
                    model.addAttribute(FORM_ATTR_NAME, form);
                    return SPEND_PROFILE_TOTALS_TEMPLATE;
                },
                () -> "redirect:/project/" + projectId,
                () -> projectFinanceService.completeSpendProfilesReview(projectId));

    }

    private TotalSpendProfileViewModel buildTotalViewModel(final Long projectId) {
        ProjectResource projectResource = projectService.getById(projectId);
        TotalProjectSpendProfileTableViewModel tableView = buildTableViewModel(projectId);
        SpendProfileSummaryModel summary = spendProfileTableCalculator.createSpendProfileSummary(projectResource, tableView.getMonthlyCostsPerOrganisationMap(), tableView.getMonths());
        return new TotalSpendProfileViewModel(projectResource, tableView, summary);
    }

    private TotalProjectSpendProfileTableViewModel buildTableViewModel(final Long projectId) {
        final OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        List<OrganisationResource> organisations = new PrioritySorting<>(projectService.getPartnerOrganisationsForProject(projectId),
                leadOrganisation, OrganisationResource::getName).unwrap();

        Map<Long, SpendProfileTableResource> organisationSpendProfiles = simpleToLinkedMap(organisations, OrganisationResource::getId,
                organisation -> projectFinanceService.getSpendProfileTable(projectId, organisation.getId()));

        Map<Long, List<BigDecimal>> monthlyCostsPerOrganisationMap = simpleLinkedMapValue(organisationSpendProfiles, spendTableResource ->
                spendProfileTableCalculator.calculateMonthlyTotals(spendTableResource.getMonthlyCostsPerCategoryMap(), spendTableResource.getMonths().size()));

        Map<Long, BigDecimal> eligibleCostPerOrganisationMap = simpleLinkedMapValue(organisationSpendProfiles, tableResource ->
                spendProfileTableCalculator.calculateTotalOfAllEligibleTotals(tableResource.getEligibleCostPerCategoryMap()));

        List<LocalDateResource> months = organisationSpendProfiles.values().iterator().next().getMonths();
        Map<Long, BigDecimal> organisationToActualTotal = spendProfileTableCalculator.calculateRowTotal(monthlyCostsPerOrganisationMap);
        List<BigDecimal> totalForEachMonth = spendProfileTableCalculator.calculateMonthlyTotals(monthlyCostsPerOrganisationMap, months.size());
        BigDecimal totalOfAllActualTotals = spendProfileTableCalculator.calculateTotalOfAllActualTotals(monthlyCostsPerOrganisationMap);
        BigDecimal totalOfAllEligibleTotals = spendProfileTableCalculator.calculateTotalOfAllEligibleTotals(eligibleCostPerOrganisationMap);

        return new TotalProjectSpendProfileTableViewModel(months, monthlyCostsPerOrganisationMap, eligibleCostPerOrganisationMap,
                organisationToActualTotal, totalForEachMonth, totalOfAllActualTotals, totalOfAllEligibleTotals,
                simpleToMap(organisations, OrganisationResource::getId, OrganisationResource::getName), leadOrganisation);

    }
}