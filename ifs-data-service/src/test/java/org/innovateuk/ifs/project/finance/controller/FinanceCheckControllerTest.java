package org.innovateuk.ifs.project.finance.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.controller.FinanceCheckController;
import org.innovateuk.ifs.project.finance.resource.*;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.junit.Test;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckProcessResourceBuilder.newFinanceCheckProcessResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckResourceBuilder.newFinanceCheckResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckPartnerStatusResourceBuilder.FinanceCheckEligibilityResourceBuilder.newFinanceCheckEligibilityResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckSummaryResourceBuilder.newFinanceCheckSummaryResource;

import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceCheckControllerTest extends BaseControllerMockMVCTest<FinanceCheckController> {

    @Test
    public void testGetFinanceCheck() throws Exception {
        Long projectId = 123L;
        Long organisationId = 456L;
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        FinanceCheckResource expected = newFinanceCheckResource().build();
        when(financeCheckServiceMock.getByProjectAndOrganisation(projectOrganisationCompositeId)).thenReturn(serviceSuccess(expected));
        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +  FinanceCheckURIs.PATH, projectId, organisationId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));
        verify(financeCheckServiceMock).getByProjectAndOrganisation(projectOrganisationCompositeId);
    }

    @Test
    public void testGetFinanceCheckSummary() throws Exception {
        Long projectId = 123L;
        FinanceCheckSummaryResource expected = newFinanceCheckSummaryResource().build();
        when(financeCheckServiceMock.getFinanceCheckSummary(projectId)).thenReturn(serviceSuccess(expected));
        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.PATH, projectId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));
        verify(financeCheckServiceMock).getFinanceCheckSummary(projectId);
    }

    @Test
    public void testGetFinanceCheckEligibility() throws Exception {
        Long projectId = 123L;
        Long organisationId = 234L;
        FinanceCheckEligibilityResource expected = newFinanceCheckEligibilityResource().build();
        when(financeCheckServiceMock.getFinanceCheckEligibilityDetails(projectId, organisationId)).thenReturn(serviceSuccess(expected));
        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" + FinanceCheckURIs.PATH + "/eligibility", projectId, organisationId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));
        verify(financeCheckServiceMock).getFinanceCheckEligibilityDetails(projectId, organisationId);
    }

    @Test
    public void testGetFinanceCheckApprovalStatus() throws Exception {
        Long projectId = 123L;
        Long organisationId = 456L;
        FinanceCheckProcessResource expected = newFinanceCheckProcessResource().build();
        when(financeCheckServiceMock.getFinanceCheckApprovalStatus(projectId, organisationId)).thenReturn(serviceSuccess(expected));

        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +  FinanceCheckURIs.PATH + "/status", projectId, organisationId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));

        verify(financeCheckServiceMock).getFinanceCheckApprovalStatus(projectId, organisationId);
    }

    @Test
    public void testGetFinanceCheckOverview() throws Exception {
        Long projectId = 123L;
        when(financeCheckServiceMock.getFinanceCheckOverview(projectId)).thenReturn(serviceSuccess(new FinanceCheckOverviewResource()));

        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.PATH + "/overview", projectId)).andExpect(status().isOk());

        verify(financeCheckServiceMock).getFinanceCheckOverview(projectId);
    }

    @Override
    protected FinanceCheckController supplyControllerUnderTest() {
        return new FinanceCheckController();
    }
}
