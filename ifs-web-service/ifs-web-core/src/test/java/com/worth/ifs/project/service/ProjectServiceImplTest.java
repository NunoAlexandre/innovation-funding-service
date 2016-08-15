package com.worth.ifs.project.service;

import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.project.ProjectServiceImpl;
import com.worth.ifs.project.builder.SpendProfileResourceBuilder;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.resource.SpendProfileResource;
import com.worth.ifs.user.resource.OrganisationResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.worth.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static com.worth.ifs.address.resource.OrganisationAddressType.REGISTERED;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_OTHER_DOCUMENTS_MUST_BE_UPLOADED_BEFORE_SUBMIT;
import static com.worth.ifs.commons.rest.RestResult.restFailure;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceImplTest {

    @InjectMocks
    private ProjectServiceImpl service;

    @Mock
    private ProjectRestService projectRestService;

    @Mock
    private ApplicationService applicationService;

    @Test
    public void testGetById() {
        ProjectResource projectResource = newProjectResource().build();

        when(projectRestService.getProjectById(projectResource.getId())).thenReturn(restSuccess(projectResource));

        ProjectResource returnedProjectResource = service.getById(projectResource.getId());

        assertEquals(projectResource, returnedProjectResource);

        verify(projectRestService).getProjectById(projectResource.getId());
    }

    @Test
    public void testUpdateFinanceContact() {
        Long projectId = 1L;
        Long organisationId = 2L;
        Long financeContactId = 3L;

        when(projectRestService.updateFinanceContact(projectId, organisationId, financeContactId)).thenReturn(restSuccess());

        service.updateFinanceContact(projectId, organisationId, financeContactId);

        verify(projectRestService).updateFinanceContact(projectId, organisationId, financeContactId);
    }

    @Test
    public void testGetProjectUsersForProject() {
        Long projectId = 1L;

        List<ProjectUserResource> projectUsers = newProjectUserResource().build(5);

        when(projectRestService.getProjectUsersForProject(projectId)).thenReturn(restSuccess(projectUsers));

        List<ProjectUserResource> returnedProjectUsers = service.getProjectUsersForProject(projectId);

        assertEquals(returnedProjectUsers, projectUsers);

        verify(projectRestService).getProjectUsersForProject(projectId);
    }

    @Test
    public void testGetByApplicationId() {
        ApplicationResource applicationResource = newApplicationResource().build();

        ProjectResource projectResource = newProjectResource().withApplication(applicationResource).build();

        when(projectRestService.getByApplicationId(applicationResource.getId())).thenReturn(restSuccess(projectResource));

        ProjectResource returnedProjectResource = service.getByApplicationId(applicationResource.getId());

        assertEquals(returnedProjectResource, projectResource);

        verify(projectRestService).getByApplicationId(applicationResource.getId());
    }

    @Test
    public void testUpdateProjectManager() {
        when(projectRestService.updateProjectManager(1L, 2L)).thenReturn(restSuccess());

        service.updateProjectManager(1L, 2L);

        verify(projectRestService).updateProjectManager(1L, 2L);
    }

    @Test
    public void testFindByUser() {
        List<ProjectResource> projects = newProjectResource().build(3);

        when(projectRestService.findByUserId(1L)).thenReturn(restSuccess(projects));

        ServiceResult<List<ProjectResource>> result = service.findByUser(1L);

        assertTrue(result.isSuccess());

        assertEquals(result.getSuccessObject(), projects);

        verify(projectRestService).findByUserId(1L);
    }

    public void testUpdateProjectStartDate() {
        LocalDate date = LocalDate.now();

        when(projectRestService.updateProjectStartDate(1L, date)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.updateProjectStartDate(1L, date);

        assertTrue(result.isSuccess());

        verify(projectRestService).updateProjectStartDate(1L, date).toServiceResult();
    }

    @Test
    public void testUpdateAddress() {
        Long leadOrgId = 1L;
        Long projectId = 2L;
        AddressResource addressResource = newAddressResource().build();

        when(projectRestService.updateProjectAddress(leadOrgId, projectId, REGISTERED, addressResource)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.updateAddress(leadOrgId, projectId, REGISTERED, addressResource);

        assertTrue(result.isSuccess());

        verify(projectRestService).updateProjectAddress(leadOrgId, projectId, REGISTERED, addressResource);
    }

    @Test
    public void testSetApplicationDetailsSubmitted() {
        when(projectRestService.setApplicationDetailsSubmitted(1L)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.setApplicationDetailsSubmitted(1L);

        assertTrue(result.isSuccess());

        verify(projectRestService).setApplicationDetailsSubmitted(1L);
    }

    @Test
    public void testIsSubmitAllowed() {
        when(projectRestService.isSubmitAllowed(1L)).thenReturn(restSuccess(false));

        ServiceResult<Boolean> result = service.isSubmitAllowed(1L);

        assertTrue(result.isSuccess());

        verify(projectRestService).isSubmitAllowed(1L);
    }

    @Test
    public void testGetLeadOrganisation() {
        OrganisationResource organisationResource = newOrganisationResource().build();

        ApplicationResource applicationResource = newApplicationResource().build();

        ProjectResource projectResource = newProjectResource().withApplication(applicationResource).build();

        when(projectRestService.getProjectById(projectResource.getId())).thenReturn(restSuccess(projectResource));

        when(applicationService.getLeadOrganisation(projectResource.getApplication())).thenReturn(organisationResource);

        OrganisationResource returnedOrganisationResource = service.getLeadOrganisation(projectResource.getId());

        assertEquals(organisationResource, returnedOrganisationResource);

        verify(projectRestService).getProjectById(projectResource.getId());

        verify(applicationService).getLeadOrganisation(projectResource.getApplication());
    }

    @Test
    public void testAddCollaborationAgreement() {

        FileEntryResource createdFile = newFileEntryResource().build();

        when(projectRestService.addCollaborationAgreementDocument(123L, "text/plain", 1000, "filename.txt", "My content!".getBytes())).
                thenReturn(restSuccess(createdFile));

        ServiceResult<FileEntryResource> result =
                service.addCollaborationAgreementDocument(123L, "text/plain", 1000, "filename.txt", "My content!".getBytes());

        assertTrue(result.isSuccess());
        assertEquals(createdFile, result.getSuccessObject());
    }

    @Test
    public void testGetCollaborationAgreementFile() {

        Optional<ByteArrayResource> content = Optional.of(new ByteArrayResource("My content!".getBytes()));
        when(projectRestService.getCollaborationAgreementFile(123L)).thenReturn(restSuccess(content));

        Optional<ByteArrayResource> result = service.getCollaborationAgreementFile(123L);
        assertEquals(content, result);
    }

    @Test
    public void testGetCollaborationAgreementFileDetails() {

        FileEntryResource returnedFile = newFileEntryResource().build();

        Optional<FileEntryResource> response = Optional.of(returnedFile);
        when(projectRestService.getCollaborationAgreementFileDetails(123L)).thenReturn(restSuccess(response));

        Optional<FileEntryResource> result = service.getCollaborationAgreementFileDetails(123L);
        assertEquals(response, result);
    }

    @Test
    public void testRemoveCollaborationAgreement() {

        when(projectRestService.removeCollaborationAgreementDocument(123L)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.removeCollaborationAgreementDocument(123L);

        assertTrue(result.isSuccess());

        verify(projectRestService).removeCollaborationAgreementDocument(123L);
    }

    @Test
    public void testAddExploitationPlan() {

        FileEntryResource createdFile = newFileEntryResource().build();

        when(projectRestService.addExploitationPlanDocument(123L, "text/plain", 1000, "filename.txt", "My content!".getBytes())).
                thenReturn(restSuccess(createdFile));

        ServiceResult<FileEntryResource> result =
                service.addExploitationPlanDocument(123L, "text/plain", 1000, "filename.txt", "My content!".getBytes());

        assertTrue(result.isSuccess());
        assertEquals(createdFile, result.getSuccessObject());
    }

    @Test
    public void testGetCExploitationPlanFile() {

        Optional<ByteArrayResource> content = Optional.of(new ByteArrayResource("My content!".getBytes()));
        when(projectRestService.getExploitationPlanFile(123L)).thenReturn(restSuccess(content));

        Optional<ByteArrayResource> result = service.getExploitationPlanFile(123L);
        assertEquals(content, result);
    }

    @Test
    public void testGetExploitationPlanFileDetails() {

        FileEntryResource returnedFile = newFileEntryResource().build();

        Optional<FileEntryResource> response = Optional.of(returnedFile);
        when(projectRestService.getExploitationPlanFileDetails(123L)).thenReturn(restSuccess(response));

        Optional<FileEntryResource> result = service.getExploitationPlanFileDetails(123L);
        assertEquals(response, result);
    }

    @Test
    public void testRemoveExploitationPlan() {

        when(projectRestService.removeExploitationPlanDocument(123L)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.removeExploitationPlanDocument(123L);

        assertTrue(result.isSuccess());

        verify(projectRestService).removeExploitationPlanDocument(123L);
    }

    @Test
    public void testOtherDocumentsSubmitAllowedWhenAllFilesUploaded() throws Exception {

        when(projectRestService.isOtherDocumentsSubmitAllowed(123L)).thenReturn(restSuccess(true));

        ServiceResult<Boolean> submitAllowed = service.isOtherDocumentSubmitAllowed(123L);

        assertTrue(submitAllowed.isSuccess());

        verify(projectRestService).isOtherDocumentsSubmitAllowed(123L);
    }

    @Test
    public void testOtherDocumentsSubmitAllowedWhenNotAllFilesUploaded() throws Exception {

        when(projectRestService.isOtherDocumentsSubmitAllowed(123L)).thenReturn(restFailure(new Error(PROJECT_SETUP_OTHER_DOCUMENTS_MUST_BE_UPLOADED_BEFORE_SUBMIT)));

        ServiceResult<Boolean> submitAllowed = null;

        submitAllowed = service.isOtherDocumentSubmitAllowed(123L);

        assertTrue(submitAllowed
                .getFailure().is(new Error(PROJECT_SETUP_OTHER_DOCUMENTS_MUST_BE_UPLOADED_BEFORE_SUBMIT)));

        verify(projectRestService).isOtherDocumentsSubmitAllowed(123L);
    }


}
