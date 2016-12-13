package org.innovateuk.ifs.project.viability.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.resource.category.FinanceRowCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.project.finance.resource.Viability;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.viability.form.FinanceChecksViabilityForm;
import org.innovateuk.ifs.project.viability.viewmodel.FinanceChecksViabilityViewModel;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationSize;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.finance.builder.DefaultCostCategoryBuilder.newDefaultCostCategory;
import static org.innovateuk.ifs.finance.builder.GrantClaimCostBuilder.newGrantClaim;
import static org.innovateuk.ifs.finance.builder.GrantClaimCostCategoryBuilder.newGrantClaimCostCategory;
import static org.innovateuk.ifs.finance.builder.LabourCostBuilder.newLabourCost;
import static org.innovateuk.ifs.finance.builder.LabourCostCategoryBuilder.newLabourCostCategory;
import static org.innovateuk.ifs.finance.builder.MaterialsCostBuilder.newMaterials;
import static org.innovateuk.ifs.finance.builder.OtherCostBuilder.newOtherCost;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostBuilder.newOtherFunding;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostCategoryBuilder.newOtherFundingCostCategory;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static org.innovateuk.ifs.finance.resource.category.LabourCostCategory.WORKING_DAYS_PER_YEAR;
import static org.innovateuk.ifs.finance.resource.category.OtherFundingCostCategory.OTHER_FUNDING;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FinanceChecksViabilityControllerTest extends BaseControllerMockMVCTest<FinanceChecksViabilityController> {

    private OrganisationResource industrialOrganisation = newOrganisationResource().
            withName("Industrial Org").
            withOrganisationSize(OrganisationSize.MEDIUM).
            withCompanyHouseNumber("123456789").
            build();

    private OrganisationResource academicOrganisation = newOrganisationResource().
            withName("Academic Org").
            withOrganisationSize(OrganisationSize.LARGE).
            withCompanyHouseNumber("987654321").
            build();

    private ProjectResource project = newProjectResource().build();

    private Map<FinanceRowType, FinanceRowCostCategory> industrialOrganisationFinances = asMap(
            FinanceRowType.LABOUR, newLabourCostCategory().withCosts(
                    newLabourCost().
                            withGrossAnnualSalary(new BigDecimal("10000.23"), new BigDecimal("5100.11"), BigDecimal.ZERO).
                            withDescription("Developers", "Testers", WORKING_DAYS_PER_YEAR).
                            withLabourDays(100, 120, 250).
                            build(3)).
                    build(),
            FinanceRowType.MATERIALS, newDefaultCostCategory().withCosts(
                    newMaterials().
                            withCost(new BigDecimal("33.33"), new BigDecimal("98.51")).
                            withQuantity(1, 2).
                            build(2)).
                    build(),
            FinanceRowType.FINANCE, newGrantClaimCostCategory().withCosts(
                    newGrantClaim().
                            withGrantClaimPercentage(30).
                            build(1)).
                    build(),
            FinanceRowType.OTHER_FUNDING, newOtherFundingCostCategory().withCosts(
                    newOtherFunding().
                            withOtherPublicFunding("Yes", "").
                            withFundingSource(OTHER_FUNDING, "Some source of funding").
                            withFundingAmount(null, BigDecimal.valueOf(1000)).
                            build(2)).
                    build());

    private Map<FinanceRowType, FinanceRowCostCategory> academicOrganisationFinances = asMap(
            FinanceRowType.LABOUR, newLabourCostCategory().withCosts(
                    newLabourCost().
                            withGrossAnnualSalary(new BigDecimal("10000.23"), new BigDecimal("5100.11"), new BigDecimal("600.11"), BigDecimal.ZERO).
                            withDescription("Developers", "Testers", "Something else", WORKING_DAYS_PER_YEAR).
                            withLabourDays(100, 120, 120, 250).
                            withName("direct_staff", "direct_staff", "exceptions_staff").
                            build(4)).
                    build(),
            FinanceRowType.OTHER_COSTS, newDefaultCostCategory().withCosts(
                    newOtherCost().
                            withCost(new BigDecimal("33.33"), new BigDecimal("98.51")).
                            withName("direct_costs", "exceptions_costs").
                            build(2)).
                    build(),
            FinanceRowType.FINANCE, newGrantClaimCostCategory().withCosts(
                    newGrantClaim().
                            withGrantClaimPercentage(100).
                            build(1)).
                    build(),
            FinanceRowType.OTHER_FUNDING, newOtherFundingCostCategory().withCosts(
                    newOtherFunding().
                            withOtherPublicFunding("Yes", "").
                            withFundingSource(OTHER_FUNDING, "Some source of funding").
                            withFundingAmount(null, BigDecimal.valueOf(1000)).
                            build(2)).
                    build());

    private List<ProjectFinanceResource> projectFinances = newProjectFinanceResource().
            withProject(project.getId()).
            withOrganisation(academicOrganisation.getId(), industrialOrganisation.getId()).
            withFinanceOrganisationDetails(academicOrganisationFinances, industrialOrganisationFinances).
            build(2);

    @Before
    public void setupFinanceTotals() {
        industrialOrganisationFinances.forEach((type, category) -> category.calculateTotal());
        academicOrganisationFinances.forEach((type, category) -> category.calculateTotal());
    }

    @Test
    public void testViewViabilityIndustrial() throws Exception {

        when(organisationService.getOrganisationById(industrialOrganisation.getId())).thenReturn(industrialOrganisation);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(industrialOrganisation);
        when(projectFinanceService.getProjectFinances(project.getId())).thenReturn(projectFinances);
        when(projectFinanceService.getViability(project.getId(), industrialOrganisation.getId())).thenReturn(Viability.APPROVED);

        MvcResult result = mockMvc.perform(get("/project/{projectId}/finance-check/organisation/{organisationId}/viability",
                project.getId(), industrialOrganisation.getId())).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("model")).
                andExpect(view().name("project/financecheck/viability")).
                andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();

        FinanceChecksViabilityViewModel viewModel = (FinanceChecksViabilityViewModel) model.get("model");

        assertTrue(viewModel.isLeadPartnerOrganisation());
        assertOrganisationDetails(industrialOrganisation, viewModel);

        assertEquals(Integer.valueOf(6678), viewModel.getTotalCosts());
        assertEquals(Integer.valueOf(30), viewModel.getPercentageGrant());
        assertEquals(Integer.valueOf(1004), viewModel.getFundingSought());
        assertEquals(Integer.valueOf(1000), viewModel.getOtherPublicSectorFunding());
        assertEquals(Integer.valueOf(4675), viewModel.getContributionToProject());

        FinanceChecksViabilityForm form = (FinanceChecksViabilityForm) model.get("form");
        assertEquals("Red", form.getRagStatus());
        assertEquals(false, form.isCreditReportConfirmed());
        assertEquals(true, form.isViabilityConfirmedChecked());
    }

    @Test
    public void testViewViabilityAcademic() throws Exception {

        when(organisationService.getOrganisationById(academicOrganisation.getId())).thenReturn(academicOrganisation);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(industrialOrganisation);
        when(projectFinanceService.getProjectFinances(project.getId())).thenReturn(projectFinances);
        when(projectFinanceService.getViability(project.getId(), academicOrganisation.getId())).thenReturn(Viability.APPROVED);

        MvcResult result = mockMvc.perform(get("/project/{projectId}/finance-check/organisation/{organisationId}/viability",
                project.getId(), academicOrganisation.getId())).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("model")).
                andExpect(view().name("project/financecheck/viability")).
                andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        FinanceChecksViabilityViewModel viewModel = (FinanceChecksViabilityViewModel) model.get("model");

        assertFalse(viewModel.isLeadPartnerOrganisation());
        assertOrganisationDetails(academicOrganisation, viewModel);

        assertEquals(Integer.valueOf(6868), viewModel.getTotalCosts());
        assertEquals(Integer.valueOf(100), viewModel.getPercentageGrant());
        assertEquals(Integer.valueOf(5868), viewModel.getFundingSought());
        assertEquals(Integer.valueOf(1000), viewModel.getOtherPublicSectorFunding());
        assertEquals(Integer.valueOf(0), viewModel.getContributionToProject());

        FinanceChecksViabilityForm form = (FinanceChecksViabilityForm) model.get("form");

        assertEquals("Red", form.getRagStatus());
        assertEquals(false, form.isCreditReportConfirmed());
        assertEquals(true, form.isViabilityConfirmedChecked());
    }

    @Test
    public void testConfirmViability() throws Exception {

        Long projectId = 123L;
        Long organisationId = 456L;

        mockMvc.perform(
            post("/project/{projectId}/finance-check/organisation/{organisationId}/viability", projectId, organisationId).
                param("confirm-viability", "").
                param("creditReportConfirmed", "true").
                param("ragStatus", "Red")).
            andExpect(status().is3xxRedirection()).
            andExpect(view().name("redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId + "/viability"));

        verify(projectFinanceService).saveViability(projectId, organisationId, Viability.APPROVED);
    }

    @Test
    public void testSaveAndContinue() throws Exception {

        Long projectId = 123L;
        Long organisationId = 456L;

        mockMvc.perform(
                post("/project/{projectId}/finance-check/organisation/{organisationId}/viability", projectId, organisationId).
                        param("save-and-continue", "").
                        param("creditReportConfirmed", "true").
                        param("ragStatus", "Red")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId + "/viability"));

        verify(projectFinanceService).saveViability(projectId, organisationId, Viability.PENDING);
    }

    private void assertOrganisationDetails(OrganisationResource organisation, FinanceChecksViabilityViewModel viewModel) {
        assertEquals(organisation.getName(), viewModel.getOrganisationName());
        assertEquals(organisation.getOrganisationSize(), viewModel.getOrganisationSize());
        assertEquals(organisation.getCompanyHouseNumber(), viewModel.getCompanyRegistrationNumber());
        assertNull(viewModel.getHeadCount());
        assertNull(viewModel.getTurnover());
        assertEquals(organisation.getCompanyHouseNumber(), viewModel.getCompanyRegistrationNumber());
    }

    @Override
    protected FinanceChecksViabilityController supplyControllerUnderTest() {
        return new FinanceChecksViabilityController();
    }
}
