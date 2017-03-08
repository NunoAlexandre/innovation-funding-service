package org.innovateuk.ifs.project.finance.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.controller.FinanceCheckController;
import org.innovateuk.ifs.project.finance.resource.*;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.FinanceCheckDocs.*;
import static org.innovateuk.ifs.finance.resource.cost.FinanceRowType.LABOUR;
import static org.innovateuk.ifs.project.builder.CostCategoryResourceBuilder.newCostCategoryResource;
import static org.innovateuk.ifs.project.builder.CostGroupResourceBuilder.newCostGroupResource;
import static org.innovateuk.ifs.project.builder.CostResourceBuilder.newCostResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckOverviewResourceBuilder.newFinanceCheckOverviewResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckPartnerStatusResourceBuilder.FinanceCheckEligibilityResourceBuilder.newFinanceCheckEligibilityResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckPartnerStatusResourceBuilder.newFinanceCheckPartnerStatusResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckProcessResourceBuilder.newFinanceCheckProcessResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckResourceBuilder.newFinanceCheckResource;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckSummaryResourceBuilder.newFinanceCheckSummaryResource;
import static org.innovateuk.ifs.project.finance.resource.FinanceCheckState.READY_TO_APPROVE;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceCheckControllerDocumentation extends BaseControllerMockMVCTest<FinanceCheckController> {

    @Test
    public void saveFinanceCheck() throws Exception {
        CostCategoryResource costCategoryResource = newCostCategoryResource().withName(LABOUR.getName()).build();

        List<CostResource> costs = newCostResource().withCostCategory(costCategoryResource).withValue(new BigDecimal(500.00)).build(7);

        CostGroupResource costGroupResource = newCostGroupResource().withDescription("Finance check cost group").withCosts(costs).build();

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().withProject(123L).withOrganisation(456L).withCostGroup(costGroupResource).build();

        when(financeCheckServiceMock.save(any(FinanceCheckResource.class))).thenReturn(serviceSuccess());

        String url = FinanceCheckURIs.BASE_URL + FinanceCheckURIs.PATH;

        mockMvc.perform(post(url, 123L, 456L).
                contentType(APPLICATION_JSON).
                content(toJson(financeCheckResource))).
                andExpect(status().isOk()).
                andDo(document("project/{method-name}",
                        requestFields(financeCheckResourceFields)
                ));
    }

    @Test
    public void getByProjectAndOrganisation() throws Exception {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(123L, 456L);

        CostCategoryResource costCategoryResource = newCostCategoryResource().withName(LABOUR.getName()).build();

        List<CostResource> costs = newCostResource().withCostCategory(costCategoryResource).withValue(new BigDecimal(500.00)).build(7);

        CostGroupResource costGroupResource = newCostGroupResource().withDescription("Finance check cost group").withCosts(costs).build();

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().withProject(123L).withOrganisation(456L).withCostGroup(costGroupResource).build();

        when(financeCheckServiceMock.getByProjectAndOrganisation(projectOrganisationCompositeId)).thenReturn(serviceSuccess(financeCheckResource));

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +
                FinanceCheckURIs.PATH;

        mockMvc.perform(get(url, 123L, 456L)).
                andExpect(status().isOk()).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check is linked"),
                                parameterWithName("organisationId").description("Id of the organisation to which the Finance Check is linked")
                        ),
                        responseFields(financeCheckResourceFields)
                ));

        verify(financeCheckServiceMock).getByProjectAndOrganisation(projectOrganisationCompositeId);
    }

    @Test
    public void approveFinanceCheck() throws Exception {

        when(financeCheckServiceMock.approve(123L, 456L)).thenReturn(serviceSuccess());

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +
                FinanceCheckURIs.PATH + "/approve";

        mockMvc.perform(post(url, 123L, 456L)).
                andExpect(status().isOk()).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check is linked"),
                                parameterWithName("organisationId").description("Id of the organisation to which the Finance Check is linked")
                        )
                ));

        verify(financeCheckServiceMock).approve(123L, 456L);
    }

    @Test
    public void getFinanceCheckApprovalStatus() throws Exception {

        FinanceCheckProcessResource status = newFinanceCheckProcessResource().
                withCanApprove(true).
                withInternalParticipant(newUserResource().withFirstName("John").withLastName("Doe").withEmail("john.doe@innovateuk.gov.uk").build()).
                withParticipant(newProjectUserResource().withUserName("Steve Smith").withEmail("steve.smith@empire.com").withProject(123L).withOrganisation(456L).build()).
                withState(READY_TO_APPROVE).
                withModifiedDate(LocalDateTime.of(2016, 10, 04, 12, 10, 02)).
                build();

        when(financeCheckServiceMock.getFinanceCheckApprovalStatus(123L, 456L)).thenReturn(serviceSuccess(status));

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" +
                FinanceCheckURIs.PATH + "/status";

        mockMvc.perform(get(url, 123L, 456L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(status))).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check is linked"),
                                parameterWithName("organisationId").description("Id of the organisation to which the Finance Check is linked")
                        ),
                        responseFields(financeCheckApprovalStatusFields)
                ));

        verify(financeCheckServiceMock).getFinanceCheckApprovalStatus(123L, 456L);
    }

    @Test
    public void getFinanceCheckSummary() throws Exception {
        Long projectId = 123L;
        Long competitionId = 456L;

        List<FinanceCheckPartnerStatusResource> partnerStatusResources = newFinanceCheckPartnerStatusResource().withId(1L, 2L, 3L).withName("Organisation A", "Organisation B", "Organisation C").withEligibility(Eligibility.REVIEW, Eligibility.APPROVED, Eligibility.APPROVED).build(3);

        FinanceCheckSummaryResource expected = newFinanceCheckSummaryResource().
                withProjectId(projectId).
                withCompetitionId(competitionId).
                withProjectStartDate(LocalDate.now()).
                withDurationInMonths(6).
                withTotalProjectCost(new BigDecimal(10000.00)).
                withGrantAppliedFor(new BigDecimal(5000.00)).
                withOtherPublicSectorFunding(new BigDecimal(0.00)).
                withTotalPercentageGrant(new BigDecimal(50.00)).
                withSpendProfilesGenerated(false).
                withFinanceChecksAllApproved(false).
                withPartnerStatusResources(partnerStatusResources).
                build();

        when(financeCheckServiceMock.getFinanceCheckSummary(123L)).thenReturn(serviceSuccess(expected));

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.PATH;

        mockMvc.perform(get(url, 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(expected))).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check is linked")
                        ),
                        responseFields(financeCheckSummaryResourceFields)
                ));

        verify(financeCheckServiceMock).getFinanceCheckSummary(123L);
    }

    @Test
    public void getFinanceCheckOverview() throws Exception {
        Long projectId = 123L;

        FinanceCheckOverviewResource expected = newFinanceCheckOverviewResource().
                withProjectId(projectId).
                withProjectStartDate(LocalDate.now()).
                withDurationInMonths(6).
                withTotalProjectCost(new BigDecimal(10000.00)).
                withGrantAppliedFor(new BigDecimal(5000.00)).
                withOtherPublicSectorFunding(new BigDecimal(0.00)).
                withTotalPercentageGrants(new BigDecimal(30.00)).
                build();

        when(financeCheckServiceMock.getFinanceCheckOverview(123L)).thenReturn(serviceSuccess(expected));

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.PATH + "/overview";

        mockMvc.perform(get(url, 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(expected))).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check is linked")
                        ),
                        responseFields(financeCheckOverviewResourceFields)
                ));

        verify(financeCheckServiceMock).getFinanceCheckOverview(123L);
    }

    @Test
    public void getFinanceCheckEligibilityDetails() throws Exception {
        Long projectId = 123L;
        Long organisationId = 456L;

        FinanceCheckEligibilityResource expected = newFinanceCheckEligibilityResource().
                withProjectId(projectId).
                withOrganisationId(organisationId).
                withDurationInMonths(6L).
                withTotalCost(new BigDecimal(10000.00)).
                withPercentageGrant(new BigDecimal(50.00)).
                withFundingSought(new BigDecimal(5000.00)).
                withOtherPublicSectorFunding(new BigDecimal(0.00)).
                withContributionToProject(new BigDecimal(50.00)).
                build();

        when(financeCheckServiceMock.getFinanceCheckEligibilityDetails(123L, 456L)).thenReturn(serviceSuccess(expected));

        String url = FinanceCheckURIs.BASE_URL + "/{projectId}" + FinanceCheckURIs.ORGANISATION_PATH + "/{organisationId}" + FinanceCheckURIs.PATH + "/eligibility";

        mockMvc.perform(get(url, 123L, 456L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(expected))).
                andDo(document("project/{method-name}",
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Finance Check eligibility is linked"),
                                parameterWithName("organisationId").description("Id of the organisation to which the Finance Check eligibility is linked")
                        ),
                        responseFields(financeCheckEligibilityResourceFields)
                ));

        verify(financeCheckServiceMock).getFinanceCheckEligibilityDetails(123L, 456L);
    }

    @Override
    protected FinanceCheckController supplyControllerUnderTest() {
        return new FinanceCheckController();
    }
}
