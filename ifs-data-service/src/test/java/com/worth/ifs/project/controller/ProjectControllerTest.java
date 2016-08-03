package com.worth.ifs.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.address.resource.OrganisationAddressType;
import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestErrorResponse;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.FileAndContents;
import com.worth.ifs.invite.builder.InviteResourceBuilder;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import com.worth.ifs.project.builder.MonitoringOfficerResourceBuilder;
import com.worth.ifs.project.resource.MonitoringOfficerResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.transactional.ProjectService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.worth.ifs.JsonTestUtil.fromJson;
import static com.worth.ifs.JsonTestUtil.toJson;
import static com.worth.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static com.worth.ifs.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static com.worth.ifs.commons.error.CommonFailureKeys.NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE;
import static com.worth.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED;
import static com.worth.ifs.commons.error.Error.fieldError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static com.worth.ifs.project.builder.MonitoringOfficerResourceBuilder.newMonitoringOfficerResource;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseControllerMockMVCTest<ProjectController> {

    private MonitoringOfficerResource monitoringOfficerResource;

    private InviteResource inviteResource;

    private RestDocumentationResultHandler document;

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


        inviteResource = InviteResourceBuilder.newInviteResource()
                .withId(1L)
                .withName("Ben Dishman")
                .withEmail("abc.xyz@gmail.com")
                .withOrganisation(1L)
                .build();

    }

    @Before
    public void setUpDocumentation() throws Exception {
        this.document = document("project/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Override
    protected ProjectController supplyControllerUnderTest() {
        return new ProjectController();
    }

    @Test
    public void projectControllerShouldReturnProjectById() throws Exception {
        Long project1Id = 1L;
        Long project2Id = 2L;

        ProjectResource testProjectResource1 = newProjectResource().withId(project1Id).build();
        ProjectResource testProjectResource2 = newProjectResource().withId(project2Id).build();

        when(projectServiceMock.getProjectById(project1Id)).thenReturn(serviceSuccess(testProjectResource1));
        when(projectServiceMock.getProjectById(project2Id)).thenReturn(serviceSuccess(testProjectResource2));

        mockMvc.perform(get("/project/{id}", project1Id))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writeValueAsString(testProjectResource1)));

        mockMvc.perform(get("/project/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writeValueAsString(testProjectResource2)));
    }

    @Test
    public void projectControllerShouldReturnAllProjects() throws Exception {
        int projectNumber = 3;
        List<ProjectResource> projects = newProjectResource().build(projectNumber);
        when(projectServiceMock.findAll()).thenReturn(serviceSuccess(projects));

        mockMvc.perform(get("/project/").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(projectNumber)));
    }
    
    @Test
    public void projectControllerSetProjectManager() throws Exception {
    	when(projectServiceMock.setProjectManager(3L, 5L)).thenReturn(serviceSuccess());
    	
        mockMvc.perform(post("/project/3/project-manager/5").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(projectServiceMock).setProjectManager(3L, 5L);
    }

    @Test
    public void updateFinanceContact() throws Exception {

        when(projectServiceMock.updateFinanceContact(123L, 456L, 789L)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/finance-contact?financeContact=789", 123L, 456L))
                .andExpect(status().isOk());

        verify(projectServiceMock).updateFinanceContact(123L, 456L, 789L);
    }

    @Test
    public void inviteFinanceContact() throws Exception {

        Long projectId = 1L;


        when(projectServiceMock.inviteFinanceContact(projectId, inviteResource)).
                thenReturn(serviceSuccess());

        mockMvc.perform(put("/project/{projectId}/invite-finance-contact", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(inviteResource)))
                .andExpect(status().isOk());

        verify(projectServiceMock).inviteFinanceContact(projectId, inviteResource);

    }

    @Test
    public void getProjectUsers() throws Exception {

        List<ProjectUserResource> projectUsers = newProjectUserResource().build(3);

        when(projectServiceMock.getProjectUsers(123L)).thenReturn(serviceSuccess(projectUsers));

        mockMvc.perform(get("/project/{projectId}/project-users", 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(projectUsers)));
    }

    @Test
    public void getMonitoringOfficer() throws Exception {

        MonitoringOfficerResource monitoringOfficer = newMonitoringOfficerResource().build();

        when(projectServiceMock.getMonitoringOfficer(123L)).thenReturn(serviceSuccess(monitoringOfficer));

        mockMvc.perform(get("/project/{projectId}/monitoring-officer", 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(monitoringOfficer)));
    }

    @Test
    public void updateProjectAddress() throws Exception {
        AddressResource addressResource = newAddressResource().withId(1L).build();

        when(projectServiceMock.updateProjectAddress(123L, 456L, OrganisationAddressType.REGISTERED, addressResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/address", 456L)
                .param("leadOrganisationId", "123")
                .param("addressType", OrganisationAddressType.REGISTERED.name())
                .contentType(APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(addressResource)))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(projectServiceMock).updateProjectAddress(123L, 456L, OrganisationAddressType.REGISTERED, addressResource);
    }

    @Test
    public void isSubmitAllowed() throws Exception {
        when(projectServiceMock.isSubmitAllowed(123L)).thenReturn(serviceSuccess(true));

        mockMvc.perform(get("/project/{projectId}/isSubmitAllowed", 123L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andReturn();
    }

    @Test
    public void isSubmitAllowedFalse() throws Exception {
        when(projectServiceMock.isSubmitAllowed(123L)).thenReturn(serviceSuccess(false));

        mockMvc.perform(get("/project/{projectId}/isSubmitAllowed", 123L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andReturn();
    }

    @Test
    public void setApplicationDetailsSubmitted() throws Exception {
        when(projectServiceMock.saveProjectSubmitDateTime(isA(Long.class), isA(LocalDateTime.class))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/setApplicationDetailsSubmitted", 123L))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void saveMOWhenErrorWhistSaving() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).
                thenReturn(serviceFailure(new Error(PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED)));


        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(monitoringOfficerResource)))
                .andExpect(status().isBadRequest());

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);

        // Ensure that notification is not sent when there is error whilst saving
        verify(projectServiceMock, never()).notifyMonitoringOfficer(monitoringOfficerResource);

    }

    @Test
    public void saveMOWhenUnableToSendNotifications() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).thenReturn(serviceSuccess());
        when(projectServiceMock.notifyMonitoringOfficer(monitoringOfficerResource)).
                thenReturn(serviceFailure(new Error(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE)));

        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(monitoringOfficerResource)))
                .andExpect(status().isInternalServerError());

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);
        verify(projectServiceMock).notifyMonitoringOfficer(monitoringOfficerResource);

    }

    @Test
    public void saveMonitoringOfficer() throws Exception {

        Long projectId = 1L;

        when(projectServiceMock.saveMonitoringOfficer(projectId, monitoringOfficerResource)).thenReturn(serviceSuccess());
        when(projectServiceMock.notifyMonitoringOfficer(monitoringOfficerResource)).
                thenReturn(serviceSuccess());

        mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(monitoringOfficerResource)))
                .andExpect(status().isOk());

        verify(projectServiceMock).saveMonitoringOfficer(projectId, monitoringOfficerResource);
        verify(projectServiceMock).notifyMonitoringOfficer(monitoringOfficerResource);

    }

    @Test
    public void saveMonitoringOfficerWithBindExceptions() throws Exception {

        Long projectId = 1L;

        MonitoringOfficerResource monitoringOfficerResource = MonitoringOfficerResourceBuilder.newMonitoringOfficerResource()
                .withId(null)
                .withProject(projectId)
                .withFirstName("")
                .withLastName("")
                .withEmail("abc")
                .withPhoneNumber("hello")
                .build();

        Error firstNameError = fieldError("firstName", "", "NotEmpty");
        Error lastNameError = fieldError("lastName", "", "NotEmpty");
        Error emailError = fieldError("email", "abc", "Email");
        Error phoneNumberError = fieldError("phoneNumber", "hello", "Pattern");
        Error phoneNumberLengthError = fieldError("phoneNumber", "hello", "Size");

        MvcResult result = mockMvc.perform(put("/project/{projectId}/monitoring-officer", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(monitoringOfficerResource)))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        RestErrorResponse response = fromJson(result.getResponse().getContentAsString(), RestErrorResponse.class);
        assertEquals(5, response.getErrors().size());
        asList(firstNameError, lastNameError, emailError, phoneNumberError, phoneNumberLengthError).forEach(e -> {
            String fieldName = e.getFieldName();
            String errorKey = e.getErrorKey();
            List<Error> matchingErrors = simpleFilter(response.getErrors(), error -> fieldName.equals(error.getFieldName()) && errorKey.equals(error.getErrorKey()));
            assertEquals(1, matchingErrors.size());
        });

        verify(projectServiceMock, never()).saveMonitoringOfficer(projectId, monitoringOfficerResource);
    }

    @Test
    public void updateBanksDetailsSuccessfully() throws Exception {
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

        mockMvc.perform(post("/project/{projectId}/bank-details", projectId).contentType(APPLICATION_JSON).content(toJson(bankDetailsResource))).andExpect(status().isOk()).andReturn();
    }

    @Test
    public void updateBanksDetailsWithInvalidAccountDetailsReturnsError() throws Exception {
        Long projectId = 1L;
        Long organisationId = 1L;
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().build();
        BankDetailsResource bankDetailsResource = newBankDetailsResource()
                .withProject(projectId).withSortCode("123")
                .withAccountNumber("1234567")
                .withOrganisation(organisationId)
                .withOrganiationAddress(organisationAddressResource)
                .build();

        when(bankDetailsServiceMock.updateBankDetails(bankDetailsResource)).thenReturn(serviceSuccess());

        Error invalidSortCodeError = fieldError("sortCode", "123", "Pattern");
        Error sortCodeNotProvided = fieldError("sortCode", null, "NotBlank");
        Error invalidAccountNumberError = fieldError("accountNumber", "1234567", "Pattern");
        Error accountNumberNotProvided = fieldError("accountNumber", null, "NotBlank");
        Error organisationAddressNotProvided = fieldError("organisationAddress", null, "NotNull");
        Error organisationIdNotProvided = fieldError("organisation", null, "NotNull");
        Error projectIdNotProvided = fieldError("project", null, "NotNull");

        RestErrorResponse expectedErrors = new RestErrorResponse(asList(invalidSortCodeError, invalidAccountNumberError));

        mockMvc.perform(post("/project/{projectId}/bank-details", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(bankDetailsResource)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().json(toJson(expectedErrors)))
                .andReturn();

        bankDetailsResource = newBankDetailsResource().build();

        expectedErrors = new RestErrorResponse(asList(sortCodeNotProvided, accountNumberNotProvided, organisationAddressNotProvided, organisationIdNotProvided, projectIdNotProvided));

        mockMvc.perform(post("/project/{projectId}/bank-details", projectId)
                .contentType(APPLICATION_JSON)
                .content(toJson(bankDetailsResource)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().json(toJson(expectedErrors)));
    }

    @Test
    public void addCollaborationAgreement() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileEntryResource>> serviceCallToUpload =
                (service, fileToUpload) -> service.createCollaborationAgreementFileEntry(eq(projectId), eq(fileToUpload), fileUploadInputStreamExpectations());

        assertFileUploadProcess("/project/" + projectId + "/collaboration-agreement", projectServiceMock, serviceCallToUpload).
                andDo(documentFileUploadMethod(document));
    }

    @Test
    public void updateCollaborationAgreement() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<Void>> serviceCallToUpload =
                (service, fileToUpload) -> service.updateCollaborationAgreementFileEntry(eq(projectId), eq(fileToUpload), fileUploadInputStreamExpectations());

        assertFileUpdateProcess("/project/" + projectId + "/collaboration-agreement", projectServiceMock, serviceCallToUpload).
                andDo(documentFileUpdateMethod(document));
    }

    @Test
    public void getCollaborationAgreementFileDetails() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileEntryResource>> serviceCallToUpload =
                (service, fileToUpload) -> service.getCollaborationAgreementFileEntryDetails(projectId);

        assertGetFileDetails("/project/{projectId}/collaboration-agreement/details", new Object[] {projectId}, emptyMap(),
                projectServiceMock, serviceCallToUpload).
                andDo(documentFileGetDetailsMethod(document));
    }

    @Test
    public void getCollaborationAgreementFileContent() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileAndContents>> serviceCallToUpload =
                (service, fileToUpload) -> service.getCollaborationAgreementFileContents(projectId);

        assertGetFileContents("/project/{projectId}/collaboration-agreement", new Object[] {projectId},
                emptyMap(), projectServiceMock, serviceCallToUpload).
                andDo(documentFileGetContentsMethod(document));
    }

    @Test
    public void deleteCollaborationAgreement() throws Exception {

        Long projectId = 123L;

        Function<ProjectService, ServiceResult<Void>> serviceCallToDelete =
                service -> service.deleteCollaborationAgreementFile(projectId);

        assertDeleteFile("/project/{projectId}/collaboration-agreement", new Object[] {projectId},
                emptyMap(), projectServiceMock, serviceCallToDelete).
                andDo(documentFileDeleteMethod(document));
    }



    @Test
    public void addExploitationPlan() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileEntryResource>> serviceCallToUpload =
                (service, fileToUpload) -> service.createExploitationPlanFileEntry(eq(projectId), eq(fileToUpload), fileUploadInputStreamExpectations());

        assertFileUploadProcess("/project/" + projectId + "/exploitation-plan", projectServiceMock, serviceCallToUpload).
                andDo(documentFileUploadMethod(document));
    }

    @Test
    public void updateExploitationPlan() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<Void>> serviceCallToUpload =
                (service, fileToUpload) -> service.updateExploitationPlanFileEntry(eq(projectId), eq(fileToUpload), fileUploadInputStreamExpectations());

        assertFileUpdateProcess("/project/" + projectId + "/exploitation-plan", projectServiceMock, serviceCallToUpload).
                andDo(documentFileUpdateMethod(document));
    }

    @Test
    public void getExploitationPlanFileDetails() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileEntryResource>> serviceCallToUpload =
                (service, fileToUpload) -> service.getExploitationPlanFileEntryDetails(projectId);

        assertGetFileDetails("/project/{projectId}/exploitation-plan/details", new Object[] {projectId}, emptyMap(),
                projectServiceMock, serviceCallToUpload).
                andDo(documentFileGetDetailsMethod(document));
    }

    @Test
    public void getExploitationPlanFileContent() throws Exception {

        Long projectId = 123L;

        BiFunction<ProjectService, FileEntryResource, ServiceResult<FileAndContents>> serviceCallToUpload =
                (service, fileToUpload) -> service.getExploitationPlanFileContents(projectId);

        assertGetFileContents("/project/{projectId}/exploitation-plan", new Object[] {projectId},
                emptyMap(), projectServiceMock, serviceCallToUpload).
                andDo(documentFileGetContentsMethod(document));
    }

    @Test
    public void deleteExploitationPlan() throws Exception {

        Long projectId = 123L;

        Function<ProjectService, ServiceResult<Void>> serviceCallToDelete =
                service -> service.deleteExploitationPlanFile(projectId);

        assertDeleteFile("/project/{projectId}/exploitation-plan", new Object[] {projectId},
                emptyMap(), projectServiceMock, serviceCallToDelete).
                andDo(documentFileDeleteMethod(document));
    }
}