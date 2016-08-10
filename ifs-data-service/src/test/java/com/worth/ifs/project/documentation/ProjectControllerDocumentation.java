package com.worth.ifs.project.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestErrorResponse;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import com.worth.ifs.project.builder.MonitoringOfficerResourceBuilder;
import com.worth.ifs.project.builder.SpendProfileResourceBuilder;
import com.worth.ifs.project.controller.ProjectController;
import com.worth.ifs.project.resource.MonitoringOfficerResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.resource.SpendProfileResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.documentation.SpendProfileDocs.spendProfileResourceFields;
import static com.worth.ifs.util.JsonMappingUtil.toJson;
import static com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.commons.error.Error.fieldError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.documentation.BankDetailsDocs.bankDetailsResourceFields;
import static com.worth.ifs.documentation.MonitoringOfficerDocs.monitoringOfficerResourceFields;
import static com.worth.ifs.documentation.ProjectDocs.projectResourceBuilder;
import static com.worth.ifs.documentation.ProjectDocs.projectResourceFields;
import static com.worth.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectControllerDocumentation extends BaseControllerMockMVCTest<ProjectController> {

    private RestDocumentationResultHandler document;

    private MonitoringOfficerResource monitoringOfficerResource;

    @Before
    public void setUp() {

        monitoringOfficerResource = MonitoringOfficerResourceBuilder.newMonitoringOfficerResource()
                .withId(null)
                .withProject(1L)
                .withFirstName("abc")
                .withLastName("xyz")
                .withEmail("abc.xyz@gmail.com")
                .withPhoneNumber("078323455")
                .build();
    }

    @Override
    protected ProjectController supplyControllerUnderTest() {
        return new ProjectController();
    }

    @Before
    public void setup(){
        this.document = document("project/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Test
    public void getProjectById() throws Exception {
        Long project1Id = 1L;
        ProjectResource testProjectResource1 = projectResourceBuilder.build();

        when(projectServiceMock.getProjectById(project1Id)).thenReturn(serviceSuccess(testProjectResource1));

        mockMvc.perform(get("/project/{id}", project1Id))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("Id of the project that is being requested")
                        ),
                        responseFields(projectResourceFields)
                ));
    }

    @Test
    public void projectFindAll() throws Exception {
        int projectNumber = 3;
        List<ProjectResource> projects = projectResourceBuilder.build(projectNumber);
        when(projectServiceMock.findAll()).thenReturn(serviceSuccess(projects));

        mockMvc.perform(get("/project/").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andDo(
                        this.document.snippets(
                        responseFields(
                                fieldWithPath("[]").description("List of projects the user is allowed to see")
                        )
                ));
    }

    @Test
    public void getSpendProfile()  throws Exception {

        Long projectId = 1L;

        SpendProfileResource spendProfileResource = SpendProfileResourceBuilder.newSpendProfileResource()
                .withEligibleCostPerCategoryMap(buildEligibleCostPerCategoryMap())
                .build();

        when(projectServiceMock.getSpendProfileById(projectId)).thenReturn(serviceSuccess(spendProfileResource));

        mockMvc.perform(get("/project/{projectId}/spend-profile", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writeValueAsString(spendProfileResource)))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project for which the Spend Profile data is being retrieved")
                        ),
                        responseFields(spendProfileResourceFields)
                ));
    }

    @Test
    public void getSpendProfileWhenSpendProfileDataNotInDb()  throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.getSpendProfileById(projectId)).
                thenReturn(serviceFailure(new Error(GENERAL_NOT_FOUND, " SpendProfile not found", NOT_FOUND)));

        mockMvc.perform(get("/project/{projectId}/spend-profile", projectId)
        )
                .andExpect(status().isNotFound())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project for which the Spend Profile data is being retrieved")
                        )
                ));
    }

    @Test
    public void updateStartDate() throws Exception {

        when(projectServiceMock.updateProjectStartDate(123L, LocalDate.of(2017, 2, 1))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/startdate", 123L).
                param("projectStartDate", "2017-02-01"))
                .andExpect(status().isOk())
                .andDo(this.document);

        verify(projectServiceMock).updateProjectStartDate(123L, LocalDate.of(2017, 2, 1));
    }

    @Test
    public void updateStartDateButDateInPast() throws Exception {

        when(projectServiceMock.updateProjectStartDate(123L, LocalDate.of(2015, 1, 1))).thenReturn(serviceFailure(PROJECT_SETUP_DATE_MUST_BE_IN_THE_FUTURE));

        mockMvc.perform(post("/project/{id}/startdate", 123L).
                param("projectStartDate", "2015-01-01"))
                .andExpect(status().isBadRequest())
                .andDo(this.document);
    }

    @Test
    public void updateStartDateButDateNotFirstOfMonth() throws Exception {

        when(projectServiceMock.updateProjectStartDate(123L, LocalDate.of(2015, 1, 5))).thenReturn(serviceFailure(PROJECT_SETUP_DATE_MUST_START_ON_FIRST_DAY_OF_MONTH));

        mockMvc.perform(post("/project/{id}/startdate", 123L).
                param("projectStartDate", "2015-01-05"))
                .andExpect(status().isBadRequest())
                .andDo(this.document);
    }
    
    @Test
    public void setProjectManager() throws Exception {
        Long project1Id = 1L;
        Long projectManagerId = 8L;

        when(projectServiceMock.setProjectManager(project1Id, projectManagerId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/project-manager/{projectManagerId}", project1Id, projectManagerId))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("Id of the project"),
                                parameterWithName("projectManagerId").description("User id of the project manager being assigned")
                        )
                ));
    }

    @Test
    public void setProjectManagerButInvalidProjectManager() throws Exception {
        Long project1Id = 1L;
        Long projectManagerId = 8L;

        when(projectServiceMock.setProjectManager(project1Id, projectManagerId)).thenReturn(serviceFailure(PROJECT_SETUP_PROJECT_MANAGER_MUST_BE_LEAD_PARTNER));

        mockMvc.perform(post("/project/{id}/project-manager/{projectManagerId}", project1Id, projectManagerId))
                .andExpect(status().isBadRequest())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("id").description("Id of the project"),
                                parameterWithName("projectManagerId").description("User id of the project manager being assigned")
                        )
                ));
    }

    @Test
    public void updateFinanceContact() throws Exception {

        when(projectServiceMock.updateFinanceContact(123L, 456L, 789L)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/finance-contact?financeContact=789", 123L, 456L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the Project that is having a Finance Contact applied to"),
                                parameterWithName("organisationId").description("Id of the Organisation that is having its Finance Contact set")
                        ),
                        requestParameters(
                                parameterWithName("financeContact").description("Id of the user who is to be the Finance Contact for the given Project and Organisation")
                        ))
                );
    }

    @Test
    public void updateFinanceContactButUserIsNotOnProjectForOrganisation() throws Exception {

        when(projectServiceMock.updateFinanceContact(123L, 456L, 789L)).thenReturn(serviceFailure(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_USER_ON_THE_PROJECT_FOR_THE_ORGANISATION));

        mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/finance-contact?financeContact=789", 123L, 456L))
                .andExpect(status().isBadRequest())
                .andDo(this.document);
    }

    @Test
    public void updateFinanceContactButUserIsNotPartnerOnProjectForOrganisation() throws Exception {

        when(projectServiceMock.updateFinanceContact(123L, 456L, 789L)).thenReturn(serviceFailure(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_PARTNER_ON_THE_PROJECT_FOR_THE_ORGANISATION));

        mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/finance-contact?financeContact=789", 123L, 456L))
                .andExpect(status().isBadRequest())
                .andDo(this.document);
    }

    @Test
    public void getProjectUsers() throws Exception {

        List<ProjectUserResource> projectUsers = newProjectUserResource().build(3);

        when(projectServiceMock.getProjectUsers(123L)).thenReturn(serviceSuccess(projectUsers));

        mockMvc.perform(get("/project/{projectId}/project-users", 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(projectUsers))).
                andDo(this.document.snippets(
                    pathParameters(
                            parameterWithName("projectId").description("Id of the project that the Project Users are being requested from")
                    ),
                    responseFields(fieldWithPath("[]").description("List of Project Users the user is allowed to see"))
                ));
    }

    @Test
    public void saveMoWithDiffProjectIdInUrlAndMoResource() throws Exception {

        Long projectId = 1L;

        MonitoringOfficerResource monitoringOfficerResource = MonitoringOfficerResourceBuilder.newMonitoringOfficerResource()
                .withId(null)
                .withProject(3L)
                .withFirstName("abc")
                .withLastName("xyz")
                .withEmail("abc.xyz@gmail.com")
                .withPhoneNumber("078323455")
                .build();

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).
                thenReturn(serviceFailure(new Error(PROJECT_SETUP_PROJECT_ID_IN_URL_MUST_MATCH_PROJECT_ID_IN_MONITORING_OFFICER_RESOURCE)));


        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(monitoringOfficerResource)))
                .andExpect(status().isBadRequest())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Monitoring Officer is assigned")
                        ),
                        requestFields(monitoringOfficerResourceFields)
                ));

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);

        // Ensure that notification is not sent when there is error whilst saving
        verify(projectServiceMock, never()).notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource);

    }

    @Test
    public void saveMoWhenProjectDetailsNotYetSubmitted() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).
                thenReturn(serviceFailure(new Error(PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED)));

        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(monitoringOfficerResource)))
                .andExpect(status().isBadRequest())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Monitoring Officer is assigned")
                        ),
                        requestFields(monitoringOfficerResourceFields)
                ));

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);

        // Ensure that notification is not sent when there is error whilst saving
        verify(projectServiceMock, never()).notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource);

    }

    @Test
    public void saveMoWhenUnableToSendNotifications() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).thenReturn(serviceSuccess());
        when(projectServiceMock.notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource)).
                thenReturn(serviceFailure(new Error(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE)));

        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(monitoringOfficerResource)))
                .andExpect(status().isInternalServerError())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Monitoring Officer is assigned")
                        ),
                        requestFields(monitoringOfficerResourceFields)
                ));

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);
        verify(projectServiceMock).notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource);

    }

    @Test
    public void saveMonitoringOfficer() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).thenReturn(serviceSuccess());
        when(projectServiceMock.notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource)).
                thenReturn(serviceSuccess());


        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(monitoringOfficerResource)))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to which the Monitoring Officer is assigned")
                        ),
                        requestFields(monitoringOfficerResourceFields)
                ));

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);
        verify(projectServiceMock).notifyStakeholdersOfMonitoringOfficerChange(monitoringOfficerResource);

    }

    @Test
    public void setApplicationDetailsSubmittedDateButDetailsNotFilledIn() throws Exception {
        when(projectServiceMock.saveProjectSubmitDateTime(isA(Long.class), isA(LocalDateTime.class))).thenReturn(serviceFailure(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_SUBMITTED_IF_INCOMPLETE));
        mockMvc.perform(post("/project/{projectId}/setApplicationDetailsSubmitted", 123L))
                .andExpect(status().isBadRequest())
                .andDo(this.document.snippets(
                    pathParameters(
                        parameterWithName("projectId").description("Id of the project that the Project Users are being requested from")
                    )));
    }

    @Test
    public void setApplicationDetailsSubmittedDate() throws Exception {
        when(projectServiceMock.saveProjectSubmitDateTime(isA(Long.class), isA(LocalDateTime.class))).thenReturn(serviceSuccess());
        mockMvc.perform(post("/project/{projectId}/setApplicationDetailsSubmitted", 123L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project that the Project Users are being requested from")
                        )));
    }

    @Test
    public void isSubmitAllowedReturnsFalseWhenDetailsNotProvided() throws Exception {
        when(projectServiceMock.isSubmitAllowed(123L)).thenReturn(serviceSuccess(false));
        MvcResult mvcResult = mockMvc.perform(get("/project/{projectId}/isSubmitAllowed", 123L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project that the Project Users are being requested from")
                        )))
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().equals("false"));
    }

    @Test
    public void isSubmitAllowed() throws Exception {
        when(projectServiceMock.isSubmitAllowed(123L)).thenReturn(serviceSuccess(true));
        MvcResult mvcResult = mockMvc.perform(get("/project/{projectId}/isSubmitAllowed", 123L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project that the Project Users are being requested from")
                        )))
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().equals("true"));
    }

    @Test
    public void updateBanksDetail() throws Exception {
        Long projectId = 1L;
        Long organisationId = 1L;
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().build();
        BankDetailsResource bankDetailsResource = newBankDetailsResource()
                .withProject(projectId).withSortCode("123456")
                .withAccountNumber("12345678")
                .withOrganisation(organisationId)
                .withOrganiationAddress(organisationAddressResource)
                .build();

        when(bankDetailsServiceMock.updateBankDetails(bankDetailsResource)).thenReturn(serviceSuccess());

        mockMvc.perform(
                post("/project/{projectId}/bank-details", projectId).
                        contentType(APPLICATION_JSON).
                        content(toJson(bankDetailsResource)))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to be updated with bank details")
                        ),
                        requestFields(bankDetailsResourceFields)
                ))
                .andReturn();
    }

    @Test
    public void updateBanksDetailWithInvalidDetailsReturnsErrors() throws Exception {
        Long projectId = 1L;
        Long organisationId = 1L;
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().build();
        BankDetailsResource bankDetailsResource = newBankDetailsResource()
                .withProject(projectId).withSortCode("123")
                .withAccountNumber("1234567")
                .withOrganisation(organisationId)
                .withOrganiationAddress(organisationAddressResource)
                .build();

        Error invalidSortCodeError = fieldError("sortCode", "123", "Pattern");
        Error invalidAccountNumberError = fieldError("accountNumber", "1234567", "Pattern");

        RestErrorResponse expectedErrors = new RestErrorResponse(asList(invalidSortCodeError, invalidAccountNumberError));

        when(bankDetailsServiceMock.updateBankDetails(bankDetailsResource)).thenReturn(serviceSuccess());
        mockMvc.perform(
                post("/project/{projectId}/bank-details", projectId).
                        contentType(APPLICATION_JSON).
                        content(toJson(bankDetailsResource)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().json(toJson(expectedErrors)))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project to be updated with bank details")
                        ),
                        requestFields(bankDetailsResourceFields)
                ))
                .andReturn();
    }

    @Test
    public void isOtherDocumentsSubmitAllowed() throws Exception {
        when(projectServiceMock.isOtherDocumentsSubmitAllowed(123L)).thenReturn(serviceSuccess(true));
        MvcResult mvcResult = mockMvc.perform(get("/project/{projectId}/partner/documents/submit", 123L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project for which the documents are being submitted to.")
                        )))
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().equals("true"));
    }

    @Test
    public void isOtherDocumentsSubmitNotAllowedWhenDocumentsNotFullyUploaded() throws Exception {
        when(projectServiceMock.isOtherDocumentsSubmitAllowed(123L)).thenReturn(serviceSuccess(false));
        MvcResult mvcResult = mockMvc.perform(get("/project/{projectId}/partner/documents/submit", 123L))
                .andExpect(status().isOk())
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("projectId").description("Id of the project for which the documents are being submitted to.")
                        )))
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().equals("false"));
    }

    private Map<String, BigDecimal> buildEligibleCostPerCategoryMap() {

        Map<String, BigDecimal> eligibleCostPerCategoryMap = new LinkedHashMap<>();

        eligibleCostPerCategoryMap.put("LabourCost", new BigDecimal("240"));
        eligibleCostPerCategoryMap.put("CapitalCost", new BigDecimal("190"));
        eligibleCostPerCategoryMap.put("OtherCost", new BigDecimal("149"));

        return eligibleCostPerCategoryMap;
    }
}
