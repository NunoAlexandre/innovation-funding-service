package org.innovateuk.ifs.project.finance.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.controller.FinanceCheckController;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckEligibilityResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckURIs;
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
        when(financeCheckServiceMock.getFinanceCheckEligibility(projectId, organisationId)).thenReturn(serviceSuccess(expected));
        mockMvc.perform(get(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" + FinanceCheckURIs.PATH + "/eligibility", projectId, organisationId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));
        verify(financeCheckServiceMock).getFinanceCheckEligibility(projectId, organisationId);
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
    public void testApproveFinanceCheck() throws Exception {
        Long projectId = 123L;
        Long organisationId = 456L;
        when(financeCheckServiceMock.approve(projectId, organisationId)).thenReturn(serviceSuccess());

        mockMvc.perform(post(FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +  FinanceCheckURIs.PATH + "/approve", projectId, organisationId))
                .andExpect(status().isOk());

        verify(financeCheckServiceMock).approve(123L, 456L);
    }

    @Test
    public void testUpdateFinanceCheck() throws Exception {
        FinanceCheckResource financeCheckResource = newFinanceCheckResource().build();
        when(financeCheckServiceMock.save(any(FinanceCheckResource.class))).thenReturn(serviceSuccess());
        mockMvc.perform(post(FinanceCheckURIs.BASE_URL + FinanceCheckURIs.PATH).
                contentType(APPLICATION_JSON).
                content(toJson(financeCheckResource))).
                andExpect(status().isOk());
    }

    @Override
    protected FinanceCheckController supplyControllerUnderTest() {
        return new FinanceCheckController();
    }
}
