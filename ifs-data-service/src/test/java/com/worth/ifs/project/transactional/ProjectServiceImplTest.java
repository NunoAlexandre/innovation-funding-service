package com.worth.ifs.project.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.address.domain.Address;
import com.worth.ifs.address.domain.AddressType;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.bankdetails.domain.BankDetails;
import com.worth.ifs.commons.error.CommonFailureKeys;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.FileAndContents;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.invite.builder.ProjectInviteResourceBuilder;
import com.worth.ifs.invite.domain.ProjectParticipantRole;
import com.worth.ifs.invite.resource.InviteProjectResource;
import com.worth.ifs.organisation.domain.OrganisationAddress;
import com.worth.ifs.project.builder.MonitoringOfficerBuilder;
import com.worth.ifs.project.builder.ProjectBuilder;
import com.worth.ifs.project.domain.MonitoringOfficer;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.domain.ProjectUser;
import com.worth.ifs.project.finance.domain.SpendProfile;
import com.worth.ifs.project.resource.*;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.user.resource.OrganisationTypeEnum;
import com.worth.ifs.user.resource.UserRoleType;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.address.builder.AddressBuilder.newAddress;
import static com.worth.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static com.worth.ifs.address.builder.AddressTypeBuilder.newAddressType;
import static com.worth.ifs.address.resource.OrganisationAddressType.*;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.bankdetails.builder.BankDetailsBuilder.newBankDetails;
import static com.worth.ifs.commons.error.CommonErrors.badRequestError;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.file.domain.builders.FileEntryBuilder.newFileEntry;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static com.worth.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static com.worth.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static com.worth.ifs.invite.domain.ProjectParticipantRole.*;
import static com.worth.ifs.organisation.builder.OrganisationAddressBuilder.newOrganisationAddress;
import static com.worth.ifs.project.builder.MonitoringOfficerResourceBuilder.newMonitoringOfficerResource;
import static com.worth.ifs.project.builder.ProjectBuilder.newProject;
import static com.worth.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static com.worth.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.project.builder.SpendProfileBuilder.newSpendProfile;
import static com.worth.ifs.project.constant.ProjectActivityStates.*;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.user.builder.OrganisationTypeBuilder.newOrganisationType;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.resource.UserRoleType.FINANCE_CONTACT;
import static com.worth.ifs.user.resource.UserRoleType.PARTNER;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest extends BaseServiceUnitTest<ProjectService> {

    private Long projectId = 123L;
    private Long applicationId = 456L;
    private Long userId = 7L;
    private Long otherUserId = 8L;

    private Application application;
    private Organisation organisation;
    private Role leadApplicantRole;
    private Role projectManagerRole;
    private User user;
    private ProcessRole leadApplicantProcessRole;
    private ProjectUser leadPartnerProjectUser;
    private Project project;
    private MonitoringOfficerResource monitoringOfficerResource;

    @Before
    public void setUp() {

        organisation = newOrganisation().build();

        leadApplicantRole = newRole(UserRoleType.LEADAPPLICANT).build();
        projectManagerRole = newRole(UserRoleType.PROJECT_MANAGER).build();

        user = newUser().
                withid(userId).
                build();

        leadApplicantProcessRole = newProcessRole().
                withOrganisation(organisation).
                withRole(leadApplicantRole).
                withUser(user).
                build();

        leadPartnerProjectUser = newProjectUser().
                withOrganisation(organisation).
                withRole(PROJECT_PARTNER).
                withUser(user).
                build();

        application = newApplication().
                withId(applicationId).
                withProcessRoles(leadApplicantProcessRole).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                build();

        project = newProject().
                withId(projectId).
                withApplication(application).
                withProjectUsers(singletonList(leadPartnerProjectUser)).
                build();

        monitoringOfficerResource = newMonitoringOfficerResource()
                .withProject(1L)
                .withFirstName("abc")
                .withLastName("xyz")
                .withEmail("abc.xyz@gmail.com")
                .withPhoneNumber("078323455")
                .build();

        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(application);
        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
    }

    @Test
    public void testCreateProjectFromApplication() {

        Role partnerRole = newRole().withType(PARTNER).build();

        ProjectResource newProjectResource = newProjectResource().build();

        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(application);

        Project savedProject = newProject().build();

        when(roleRepositoryMock.findOneByName(PARTNER.getName())).thenReturn(partnerRole);

        Project newProjectExpectations = createProjectExpectationsFromOriginalApplication(application);
        when(projectRepositoryMock.save(newProjectExpectations)).thenReturn(savedProject);

        when(projectMapperMock.mapToResource(savedProject)).thenReturn(newProjectResource);

        ServiceResult<ProjectResource> project = service.createProjectFromApplication(applicationId);
        assertTrue(project.isSuccess());
        assertEquals(newProjectResource, project.getSuccessObject());
    }

    @Test
    public void testInvalidProjectManagerProvided() {

        ServiceResult<Void> result = service.setProjectManager(projectId, otherUserId);
        assertFalse(result.isSuccess());
        assertTrue(result.getFailure().is(PROJECT_SETUP_PROJECT_MANAGER_MUST_BE_LEAD_PARTNER));
    }

    @Test
    public void testSetProjectManagerWhenProjectDetailsAlreadySubmitted() {

        Project existingProject = newProject().withSubmittedDate(LocalDateTime.now()).build();

        assertTrue(existingProject.getProjectUsers().isEmpty());

        when(projectRepositoryMock.findOne(projectId)).thenReturn(existingProject);

        ServiceResult<Void> result = service.setProjectManager(projectId, userId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_UPDATED_IF_ALREADY_SUBMITTED));

        assertTrue(existingProject.getProjectUsers().isEmpty());
    }

    @Test
    public void testValidProjectManagerProvided() {

        when(roleRepositoryMock.findOneByName(PROJECT_MANAGER.getName())).thenReturn(projectManagerRole);

        ServiceResult<Void> result = service.setProjectManager(projectId, userId);
        assertTrue(result.isSuccess());

        ProjectUser expectedProjectManager = newProjectUser().
                withId().
                withProject(project).
                withOrganisation(organisation).
                withRole(PROJECT_MANAGER).
                withUser(user).
                build();

        assertEquals(expectedProjectManager, project.getProjectUsers().get(project.getProjectUsers().size() - 1));
    }

    @Test
    public void testValidProjectManagerProvidedWithExistingProjectManager() {

        User differentUser = newUser().build();
        Organisation differentOrganisation = newOrganisation().build();

        @SuppressWarnings("unused")
        ProjectUser existingProjenullctManager = newProjectUser().
                withId(456L).
                withProject(project).
                withRole(PROJECT_MANAGER).
                withOrganisation(differentOrganisation).
                withUser(differentUser).
                build();

        when(roleRepositoryMock.findOneByName(PROJECT_MANAGER.getName())).thenReturn(projectManagerRole);

        ServiceResult<Void> result = service.setProjectManager(projectId, userId);
        assertTrue(result.isSuccess());

        ProjectUser expectedProjectManager = newProjectUser().
                withId(456L).
                withProject(project).
                withOrganisation(organisation).
                withRole(PROJECT_MANAGER).
                withUser(user).
                build();

        assertEquals(expectedProjectManager, project.getProjectUsers().get(project.getProjectUsers().size() - 1));
    }

    @Test
    public void testUpdateProjectStartDate() {

        LocalDate now = LocalDate.now();
        LocalDate validDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).plusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, validDate);
        assertTrue(updateResult.isSuccess());

        verify(projectRepositoryMock).findOne(123L);
        assertEquals(validDate, existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateProjectStartDateButProjectDoesntExist() {

        LocalDate now = LocalDate.now();
        LocalDate validDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).plusMonths(1);

        when(projectRepositoryMock.findOne(123L)).thenReturn(null);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, validDate);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(notFoundError(Project.class, 123L)));
    }

    @Test
    public void testUpdateProjectStartDateButStartDateDoesntBeginOnFirstDayOfMonth() {

        LocalDate now = LocalDate.now();
        LocalDate dateNotOnFirstDayOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 2).plusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, dateNotOnFirstDayOfMonth);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_DATE_MUST_START_ON_FIRST_DAY_OF_MONTH));

        verify(projectRepositoryMock, never()).findOne(123L);
        assertNull(existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateProjectStartDateButStartDateNotInFuture() {

        LocalDate now = LocalDate.now();
        LocalDate pastDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).minusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, pastDate);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_DATE_MUST_BE_IN_THE_FUTURE));

        verify(projectRepositoryMock, never()).findOne(123L);
        assertNull(existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateProjectStartDateWhenProjectDetailsAlreadySubmitted() {

        LocalDate now = LocalDate.now();
        LocalDate validDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).plusMonths(1);

        Project existingProject = newProject().withSubmittedDate(LocalDateTime.now()).build();
        assertNull(existingProject.getTargetStartDate());
        assertNotNull(existingProject.getSubmittedDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, validDate);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_UPDATED_IF_ALREADY_SUBMITTED));

        verify(projectRepositoryMock).findOne(123L);
        assertNull(existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateFinanceContact() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();

        Role partnerRole = newRole().withType(PARTNER).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_PARTNER).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isSuccess());

        List<ProjectUser> foundFinanceContacts = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getUser().equals(user) &&
                        projectUser.getProcess().equals(project) &&
                        projectUser.getRole().equals(PROJECT_FINANCE_CONTACT));

        assertEquals(1, foundFinanceContacts.size());
    }

    @Test
    public void testUpdateFinanceContactWithExistingFinanceContactChosenForSameOrganisation() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();

        User newFinanceContactUser = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(newFinanceContactUser).withProject(project).withRole(PROJECT_PARTNER).build();

        User existingFinanceContactUser = newUser().withid(9999L).build();
        newProjectUser().withOrganisation(organisation).withUser(existingFinanceContactUser).withProject(project).withRole(PROJECT_PARTNER).build();
        newProjectUser().withOrganisation(organisation).withUser(existingFinanceContactUser).withProject(project).withRole(PROJECT_FINANCE_CONTACT).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        List<ProjectUser> existingFinanceContactForOrganisation = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getProcess().equals(project) &&
                        projectUser.getRole().equals(PROJECT_FINANCE_CONTACT));

        assertEquals(1, existingFinanceContactForOrganisation.size());

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isSuccess());

        List<ProjectUser> foundFinanceContacts = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getUser().equals(newFinanceContactUser) &&
                        projectUser.getProcess().equals(project) &&
                        projectUser.getRole().equals(PROJECT_FINANCE_CONTACT));

        assertEquals(1, foundFinanceContacts.size());
    }

    @Test
    public void testUpdateFinanceContactButUserIsNotExistingPartner() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_MANAGER).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_PARTNER_ON_THE_PROJECT_FOR_THE_ORGANISATION));

        verify(processRoleRepositoryMock, never()).save(isA(ProcessRole.class));
    }

    @Test
    public void testUpdateFinanceContactWhenNotPresentOnTheProject() {

        long userIdForUserNotOnProject = 6L;

        Project existingProject = newProject().withId(123L).build();
        Project anotherProject = newProject().withId(9999L).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(anotherProject).withRole(PROJECT_PARTNER).build();

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, userIdForUserNotOnProject);

        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_USER_ON_THE_PROJECT_FOR_THE_ORGANISATION));
    }

    @Test
    public void testUpdateFinanceContactWhenProjectDetailsAlreadySubmitted() {

        Project project = newProject().withId(123L).withSubmittedDate(LocalDateTime.now()).build();

        assertTrue(project.getProjectUsers().isEmpty());

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_UPDATED_IF_ALREADY_SUBMITTED));

        verify(projectRepositoryMock).findOne(123L);
        assertTrue(project.getProjectUsers().isEmpty());
    }

    @Test
    public void testInviteProjectManagerWhenProjectNotInDB() {

        Long projectId = 1L;

        InviteProjectResource inviteResource = ProjectInviteResourceBuilder.newInviteProjectResource()
                .withName("Abc Xyz")
                .withEmail("Abc.xyz@gmail.com")
                .withLeadOrganisation("Lead Organisation 1")
                .withInviteOrganisationName("Invite Organisation 1")
                .withHash("sample/url")
                .build();


        when(projectRepositoryMock.findOne(projectId)).thenThrow(new IllegalArgumentException());

        ServiceResult<Void> result = null;

        try {
            result = service.inviteProjectManager(projectId, inviteResource);
        } catch (Exception e) {

            // We expect an exception to be thrown
            assertTrue(e instanceof IllegalArgumentException);

            assertNull(result);
            verify(notificationServiceMock, never()).sendNotification(any(), any());

            // This exception flow is the only expected flow, so return from here and assertFalse if no exception
            return;
        }

        // Should not reach here - we must get an exception
        assertFalse(true);
    }

    @Test
    public void testInviteProjectManagerWhenUnableToSendNotification() {

        Long projectId = 1L;

        InviteProjectResource inviteResource = ProjectInviteResourceBuilder.newInviteProjectResource()
                .withName("Abc Xyz")
                .withEmail("Abc.xyz@gmail.com")
                .withLeadOrganisation("Lead Organisation 1")
                .withInviteOrganisationName("Invite Organisation 1")
                .withHash("sample/url")
                .build();

        Project projectInDB = ProjectBuilder.newProject()
                .withName("Project 1")
                .build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);

        when(notificationServiceMock.sendNotification(any(), any())).
                thenReturn(serviceFailure(new Error(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE)));

        ServiceResult<Void> result = service.inviteProjectManager(projectId, inviteResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE));
    }

    @Test
    public void testInviteProjectManagerSuccess() {

        Long projectId = 1L;

        InviteProjectResource inviteResource = ProjectInviteResourceBuilder.newInviteProjectResource()
                .withName("Abc Xyz")
                .withEmail("Abc.xyz@gmail.com")
                .withLeadOrganisation("Lead Organisation 1")
                .withInviteOrganisationName("Invite Organisation 1")
                .withHash("sample/url")
                .build();

        Project projectInDB = ProjectBuilder.newProject()
                .withName("Project 1")
                .build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);

        when(notificationServiceMock.sendNotification(any(), any())).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.inviteProjectManager(projectId, inviteResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testInviteFinanceContactSuccess() {

        Long projectId = 1L;

        InviteProjectResource inviteResource = ProjectInviteResourceBuilder.newInviteProjectResource()
                .withName("Abc Xyz")
                .withEmail("Abc.xyz@gmail.com")
                .withLeadOrganisation("Lead Organisation 1")
                .withInviteOrganisationName("Invite Organisation 1")
                .withHash("sample/url")
                .build();

        Project projectInDB = ProjectBuilder.newProject()
                .withName("Project 1")
                .build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(projectInDB);

        when(notificationServiceMock.sendNotification(any(), any())).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.inviteFinanceContact(projectId, inviteResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testFindByUserIdReturnsOnlyDistinctProjects() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();

        ProjectUser projectUserWithPartnerRole = newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUserWithFinanceRole = newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_FINANCE_CONTACT).build();

        List<ProjectUser> projectUserRecords = asList(projectUserWithPartnerRole, projectUserWithFinanceRole);

        ProjectResource projectResource = newProjectResource().withId(project.getId()).build();

        when(projectUserRepositoryMock.findByUserId(user.getId())).thenReturn(projectUserRecords);

        when(projectMapperMock.mapToResource(project)).thenReturn(projectResource);

        ServiceResult<List<ProjectResource>> result = service.findByUserId(user.getId());

        assertTrue(result.isSuccess());

        assertEquals(result.getSuccessObject().size(), 1L);
    }

    @Test
    public void testUpdateProjectAddressToBeRegisteredAddress() {

        Project project = newProject().withId(1L).build();
        Organisation leadOrganisation = newOrganisation().withId(1L).build();
        AddressResource existingRegisteredAddressResource = newAddressResource().build();
        Address registeredAddress = newAddress().build();

        when(projectRepositoryMock.findOne(project.getId())).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisation.getId())).thenReturn(organisation);
        when(addressRepositoryMock.exists(existingRegisteredAddressResource.getId())).thenReturn(true);
        when(addressRepositoryMock.findOne(existingRegisteredAddressResource.getId())).thenReturn(registeredAddress);

        ServiceResult<Void> result = service.updateProjectAddress(leadOrganisation.getId(), project.getId(), REGISTERED, existingRegisteredAddressResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testUpdateProjectAddressToBeOperatingAddress() {
        Project project = newProject().withId(1L).build();
        Organisation leadOrganisation = newOrganisation().withId(1L).build();
        AddressResource existingOperatingAddressResource = newAddressResource().build();
        Address operatingAddress = newAddress().build();

        when(projectRepositoryMock.findOne(project.getId())).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisation.getId())).thenReturn(organisation);
        when(addressRepositoryMock.exists(existingOperatingAddressResource.getId())).thenReturn(true);
        when(addressRepositoryMock.findOne(existingOperatingAddressResource.getId())).thenReturn(operatingAddress);

        ServiceResult<Void> result = service.updateProjectAddress(leadOrganisation.getId(), project.getId(), OPERATING, existingOperatingAddressResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testUpdateProjectAddressToNewProjectAddress() {
        Project project = newProject().withId(1L).build();
        Organisation leadOrganisation = newOrganisation().withId(1L).build();
        AddressResource newAddressResource = newAddressResource().build();
        Address newAddress = newAddress().build();
        AddressType projectAddressType = newAddressType().withId((long) PROJECT.getOrdinal()).withName(PROJECT.name()).build();
        OrganisationAddress organisationAddress = newOrganisationAddress().withOrganisation(leadOrganisation).withAddress(newAddress).withAddressType(projectAddressType).build();

        when(projectRepositoryMock.findOne(project.getId())).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisation.getId())).thenReturn(organisation);
        when(addressRepositoryMock.exists(newAddressResource.getId())).thenReturn(false);
        when(addressMapperMock.mapToDomain(newAddressResource)).thenReturn(newAddress);
        when(addressTypeRepositoryMock.findOne((long) PROJECT.getOrdinal())).thenReturn(projectAddressType);
        when(organisationAddressRepositoryMock.findByOrganisationIdAndAddressType(leadOrganisation.getId(), projectAddressType)).thenReturn(emptyList());
        when(organisationAddressRepositoryMock.save(organisationAddress)).thenReturn(organisationAddress);

        ServiceResult<Void> result = service.updateProjectAddress(leadOrganisation.getId(), project.getId(), PROJECT, newAddressResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveProjectSubmitDateTimeIsSuccessfulWhenAllProjectDetailsHaveBeenProvided() {
        Organisation organisation1 = newOrganisation().build();
        Organisation organisation2 = newOrganisation().build();
        Organisation organisation3 = newOrganisation().build();

        Role projectManagerRole = newRole().withType(UserRoleType.PROJECT_MANAGER).build();

        ProjectUser projectManagerProjectUser = newProjectUser().withRole(PROJECT_MANAGER).build();
        Address address = newAddress().build();
        Project project = newProject().withId(1L).withAddress(address).withProjectUsers(singletonList(projectManagerProjectUser)).withTargetStartDate(LocalDate.now()).build();

        Role financeContactRole = newRole().withType(FINANCE_CONTACT).build();
        Role partnerRole = newRole().withType(PARTNER).build();

        List<ProjectUser> projectUserObjs;

        ProjectUser projectUser1WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser1WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUser2WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser2WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUser3WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation3).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser3WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation3).withRole(PROJECT_FINANCE_CONTACT).build();

        ProjectUserResource projectUser1WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser1WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUser2WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser2WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUser3WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation3.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser3WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation3.getId()).withRole(partnerRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectManagerProjectUserResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation3.getId()).withRole(projectManagerRole.getId()).withRoleName(PROJECT_MANAGER.getName()).build();

        projectUserObjs = asList(projectManagerProjectUser, projectUser1WithPartnerRole, projectUser1WithFinanceRole, projectUser2WithPartnerRole, projectUser2WithFinanceRole, projectUser3WithPartnerRole, projectUser3WithFinanceRole);

        when(projectRepositoryMock.findOne(1L)).thenReturn(project);
        when(projectUserRepositoryMock.findByProjectId(1L)).thenReturn(projectUserObjs);
        when(organisationRepositoryMock.findOne(organisation1.getId())).thenReturn(organisation1);
        when(organisationRepositoryMock.findOne(organisation2.getId())).thenReturn(organisation2);
        when(organisationRepositoryMock.findOne(organisation3.getId())).thenReturn(organisation3);

        when(projectUserMapperMock.mapToResource(projectUser1WithFinanceRole)).thenReturn(projectUser1WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser1WithPartnerRole)).thenReturn(projectUser1WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithPartnerRole)).thenReturn(projectUser2WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithFinanceRole)).thenReturn(projectUser2WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser3WithPartnerRole)).thenReturn(projectUser3WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser3WithFinanceRole)).thenReturn(projectUser3WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectManagerProjectUser)).thenReturn(projectManagerProjectUserResource);

        ServiceResult result = service.saveProjectSubmitDateTime(1L, LocalDateTime.now());
        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveProjectSubmitDateTimeIsUnSuccessfulWhenAFinanceContactIsMissing() {
        Organisation organisation1 = newOrganisation().build();
        Organisation organisation2 = newOrganisation().build();
        Organisation organisation3 = newOrganisation().build();

        ProjectUser projectManagerProjectUser = newProjectUser().withRole(PROJECT_MANAGER).build();
        Address address = newAddress().build();
        Project project = newProject().withId(1L).withAddress(address).withProjectUsers(singletonList(projectManagerProjectUser)).withTargetStartDate(LocalDate.now()).build();

        Role financeContactRole = newRole().withType(FINANCE_CONTACT).build();
        Role partnerRole = newRole().withType(PARTNER).build();

        List<ProjectUser> projectUserObjs;

        ProjectUser projectUser1WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser1WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUser2WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser2WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUserWithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation3).withRole(PROJECT_PARTNER).build();

        ProjectUserResource projectUser1WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser1WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUser2WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser2WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUserWithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation3.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();

        projectUserObjs = asList(projectUser1WithPartnerRole, projectUser1WithFinanceRole, projectUser2WithPartnerRole, projectUser2WithFinanceRole, projectUserWithPartnerRole);

        when(projectRepositoryMock.findOne(1L)).thenReturn(project);
        when(projectUserRepositoryMock.findByProjectId(1L)).thenReturn(projectUserObjs);
        when(organisationRepositoryMock.findOne(organisation1.getId())).thenReturn(organisation1);
        when(organisationRepositoryMock.findOne(organisation2.getId())).thenReturn(organisation2);
        when(organisationRepositoryMock.findOne(organisation3.getId())).thenReturn(organisation3);

        when(projectUserMapperMock.mapToResource(projectUser1WithFinanceRole)).thenReturn(projectUser1WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser1WithPartnerRole)).thenReturn(projectUser1WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithPartnerRole)).thenReturn(projectUser2WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithFinanceRole)).thenReturn(projectUser2WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUserWithPartnerRole)).thenReturn(projectUserWithPartnerRoleResource);

        ServiceResult<Void> result = service.saveProjectSubmitDateTime(1L, LocalDateTime.now());
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_SUBMITTED_IF_INCOMPLETE));
    }

    @Test
    public void testCannotSaveProjectSubmitDateTimeWhenNotAllProjectDetailsHaveBeenProvided() {
        Organisation organisation1 = newOrganisation().build();
        Organisation organisation2 = newOrganisation().build();
        Organisation organisation3 = newOrganisation().build();

        ProjectUser projectManagerProjectUser = newProjectUser().withRole(PROJECT_MANAGER).build();
        Address address = newAddress().build();
        Project project = newProject().withId(1L).withAddress(address).withProjectUsers(singletonList(projectManagerProjectUser)).build();


        Role financeContactRole = newRole().withType(FINANCE_CONTACT).build();
        Role partnerRole = newRole().withType(PARTNER).build();

        List<ProjectUser> projectUserObjs;

        ProjectUser projectUser1WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser1WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation1).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUser2WithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUser2WithFinanceRole = newProjectUser().withProject(project).withOrganisation(organisation2).withRole(PROJECT_FINANCE_CONTACT).build();
        ProjectUser projectUserWithPartnerRole = newProjectUser().withProject(project).withOrganisation(organisation3).withRole(PROJECT_PARTNER).build();

        ProjectUserResource projectUser1WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser1WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation1.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUser2WithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();
        ProjectUserResource projectUser2WithFinanceRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation2.getId()).withRole(financeContactRole.getId()).withRoleName(FINANCE_CONTACT.getName()).build();
        ProjectUserResource projectUserWithPartnerRoleResource = newProjectUserResource().withProject(project.getId()).withOrganisation(organisation3.getId()).withRole(partnerRole.getId()).withRoleName(PARTNER.getName()).build();

        projectUserObjs = asList(projectUser1WithPartnerRole, projectUser1WithFinanceRole, projectUser2WithPartnerRole, projectUser2WithFinanceRole, projectUserWithPartnerRole);

        when(projectRepositoryMock.findOne(1L)).thenReturn(project);
        when(projectUserRepositoryMock.findByProjectId(1L)).thenReturn(projectUserObjs);
        when(organisationRepositoryMock.findOne(organisation1.getId())).thenReturn(organisation1);
        when(organisationRepositoryMock.findOne(organisation2.getId())).thenReturn(organisation2);
        when(organisationRepositoryMock.findOne(organisation3.getId())).thenReturn(organisation3);

        when(projectUserMapperMock.mapToResource(projectUser1WithFinanceRole)).thenReturn(projectUser1WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser1WithPartnerRole)).thenReturn(projectUser1WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithPartnerRole)).thenReturn(projectUser2WithPartnerRoleResource);
        when(projectUserMapperMock.mapToResource(projectUser2WithFinanceRole)).thenReturn(projectUser2WithFinanceRoleResource);
        when(projectUserMapperMock.mapToResource(projectUserWithPartnerRole)).thenReturn(projectUserWithPartnerRoleResource);

        ServiceResult<Void> result = service.saveProjectSubmitDateTime(1L, LocalDateTime.now());
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_PROJECT_DETAILS_CANNOT_BE_SUBMITTED_IF_INCOMPLETE));
    }

    @Test
    public void testSaveMOWithDiffProjectIdInURLAndMOResource() {

        Long projectid = 1L;

        MonitoringOfficerResource monitoringOfficerResource = newMonitoringOfficerResource()
                .withProject(3L)
                .withFirstName("abc")
                .withLastName("xyz")
                .withEmail("abc.xyz@gmail.com")
                .withPhoneNumber("078323455")
                .build();

        ServiceResult<Void> result = service.saveMonitoringOfficer(projectid, monitoringOfficerResource);

        assertTrue(result.getFailure().is(PROJECT_SETUP_PROJECT_ID_IN_URL_MUST_MATCH_PROJECT_ID_IN_MONITORING_OFFICER_RESOURCE));
    }

    @Test
    public void testSaveMOWhenProjectDetailsNotYetSubmitted() {

        Long projectid = 1L;

        Project projectInDB = newProject().withId(1L).build();

        when(projectRepositoryMock.findOne(projectid)).thenReturn(projectInDB);

        ServiceResult<Void> result = service.saveMonitoringOfficer(projectid, monitoringOfficerResource);

        assertTrue(result.getFailure().is(PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED));
    }

    @Test
    public void testSaveMOWhenMOExistsForAProject() {

        Long projectid = 1L;

        // Set this to different values, so that we can assert that it gets updated
        MonitoringOfficer monitoringOfficerInDB = MonitoringOfficerBuilder.newMonitoringOfficer()
                .withFirstName("def")
                .withLastName("klm")
                .withEmail("def.klm@gmail.com")
                .withPhoneNumber("079237439")
                .build();


        Project projectInDB = newProject().withId(1L).withSubmittedDate(LocalDateTime.now()).build();

        when(projectRepositoryMock.findOne(projectid)).thenReturn(projectInDB);

        when(monitoringOfficerRepositoryMock.findOneByProjectId(monitoringOfficerResource.getProject())).thenReturn(monitoringOfficerInDB);

        ServiceResult<Void> result = service.saveMonitoringOfficer(projectid, monitoringOfficerResource);

        // Assert that the MO in DB is updated with the correct values from MO Resource
        Assert.assertEquals("First name of MO in DB should be updated with the value from MO Resource", monitoringOfficerInDB.getFirstName(), monitoringOfficerResource.getFirstName());
        Assert.assertEquals("Last name of MO in DB should be updated with the value from MO Resource", monitoringOfficerInDB.getLastName(), monitoringOfficerResource.getLastName());
        Assert.assertEquals("Email of MO in DB should be updated with the value from MO Resource", monitoringOfficerInDB.getEmail(), monitoringOfficerResource.getEmail());
        Assert.assertEquals("Phone number of MO in DB should be updated with the value from MO Resource", monitoringOfficerInDB.getPhoneNumber(), monitoringOfficerResource.getPhoneNumber());

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSaveMOWhenMODoesNotExistForAProject() {

        Long projectid = 1L;

        Project projectInDB = newProject().withId(1L).withSubmittedDate(LocalDateTime.now()).build();

        when(projectRepositoryMock.findOne(projectid)).thenReturn(projectInDB);

        when(monitoringOfficerRepositoryMock.findOneByProjectId(monitoringOfficerResource.getProject())).thenReturn(null);

        ServiceResult<Void> result = service.saveMonitoringOfficer(projectid, monitoringOfficerResource);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testGetMonitoringOfficerWhenMODoesNotExistInDB() {

        Long projectid = 1L;

        ServiceResult<MonitoringOfficerResource> result = service.getMonitoringOfficer(projectid);

        String errorKey = result.getFailure().getErrors().get(0).getErrorKey();
        Assert.assertEquals(CommonFailureKeys.GENERAL_NOT_FOUND.name(), errorKey);
    }

    @Test
    public void testGetMonitoringOfficerWhenMOExistsInDB() {

        Long projectid = 1L;

        MonitoringOfficer monitoringOfficerInDB = MonitoringOfficerBuilder.newMonitoringOfficer()
                .withFirstName("def")
                .withLastName("klm")
                .withEmail("def.klm@gmail.com")
                .withPhoneNumber("079237439")
                .build();

        when(monitoringOfficerRepositoryMock.findOneByProjectId(projectid)).thenReturn(monitoringOfficerInDB);

        ServiceResult<MonitoringOfficerResource> result = service.getMonitoringOfficer(projectid);

        assertTrue(result.isSuccess());

    }

    @Test
    public void testCreateCollaborationAgreementFileEntry() {
        projectServiceAssertions.assertCreateFile(
                project::getCollaborationAgreement,
                (fileToCreate, inputStreamSupplier) ->
                        service.createCollaborationAgreementFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testUpdateCollaborationAgreementFileEntry() {
        assertUpdateFile(
                project::getCollaborationAgreement,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateCollaborationAgreementFileEntry(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void testGetCollaborationAgreementFileEntryDetails() {
        projectServiceAssertions.assertGetFileDetails(
                project::setCollaborationAgreement,
                () -> service.getCollaborationAgreementFileEntryDetails(123L));
    }

    @Test
    public void testGetCollaborationAgreementFileContents() {
        projectServiceAssertions.assertGetFileContents(
                project::setCollaborationAgreement,
                () -> service.getCollaborationAgreementFileContents(123L));
    }

    @Test
    public void testDeleteCollaborationAgreementFile() {
        assertDeleteFile(
                project::getCollaborationAgreement,
                project::setCollaborationAgreement,
                () -> service.deleteCollaborationAgreementFile(123L));
    }

    @Test
    public void testCreateExploitationPlanFileEntry() {
        projectServiceAssertions.assertCreateFile(
                project::getExploitationPlan,
                (fileToCreate, inputStreamSupplier) ->
                        service.createExploitationPlanFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testUpdateExploitationPlanFileEntry() {
        assertUpdateFile(
                project::getExploitationPlan,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateExploitationPlanFileEntry(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void testGetExploitationPlanFileEntryDetails() {
        projectServiceAssertions.assertGetFileDetails(
                project::setExploitationPlan,
                () -> service.getExploitationPlanFileEntryDetails(123L));
    }

    @Test
    public void testGetExploitationPlanFileContents() {
        projectServiceAssertions.assertGetFileContents(
                project::setExploitationPlan,
                () -> service.getExploitationPlanFileContents(123L));
    }

    @Test
    public void testDeleteExploitationPlanFile() {
        assertDeleteFile(
                project::getExploitationPlan,
                project::setExploitationPlan,
                () -> service.deleteExploitationPlanFile(123L));
    }

    @Test
    public void testRetrieveUploadedFilesExist() {
        assertUploadedFilesExist(
                project::setCollaborationAgreement,
                project::setExploitationPlan,
                () -> service.retrieveUploadedDocuments(123L));
    }

    @Test
    public void testFilesCanBeSubmitted() {
        assertFilesCanBeSubmittedByProjectManagerAndFilesExist(
                project::setCollaborationAgreement,
                project::setExploitationPlan,
                () -> service.isOtherDocumentsSubmitAllowed(123L, 1L));

    }

    @Test
    public void testFilesCannotBeSubmittedIfUserNotProjectManager() {
        assertFilesCannotBeSubmittedIfNotByProjectManager(
                project::setCollaborationAgreement,
                project::setExploitationPlan,
                () -> service.isOtherDocumentsSubmitAllowed(123L, 1L));

    }

    @Test
    public void testSaveDocumentsSubmitDateTimeIsSuccessfulWhenUploadsComplete() {
        ProjectUser projectUserToSet = newProjectUser()
                .withId(1L)
                .withUser(newUser().withid(1L).build())
                .withRole(ProjectParticipantRole.PROJECT_MANAGER)
                .build();
        List<ProjectUser> projectUsers = new ArrayList<>();
        projectUsers.add(projectUserToSet);
        Project project = newProject().build();
        project.setProjectUsers(projectUsers);

        when(projectUserRepositoryMock.findByProjectId(project.getId())).thenReturn(projectUsers);
        when(projectRepositoryMock.findOne(project.getId())).thenReturn(project);

        assertSetDocumentsDateimeIfProjectManagerAndFilesExist(
                project::setCollaborationAgreement,
                project::setExploitationPlan,
                () -> service.saveDocumentsSubmitDateTime(project.getId(), LocalDateTime.now()));

        assertNotNull(project.getCollaborationAgreement());
        assertNotNull(project.getExploitationPlan());
        assertTrue(project.getProjectUsers().get(0).getRole().getName()
                .equals(UserRoleType.PROJECT_MANAGER.getName()));
        assertNotNull(project.getDocumentsSubmittedDate());
    }

    @Test
    public void testSaveDocumentsSubmitDateTimeFailsWhenUploadsImcomplete() {
        ProjectUser projectUserToSet = newProjectUser()
                .withId(1L)
                .withUser(newUser().withid(1L).build())
                .withRole(ProjectParticipantRole.PROJECT_MANAGER)
                .build();
        List<ProjectUser> projectUsers = new ArrayList<>();
        projectUsers.add(projectUserToSet);
        Project project = newProject().build();
        project.setProjectUsers(projectUsers);

        when(projectUserRepositoryMock.findByProjectId(project.getId())).thenReturn(projectUsers);
        when(projectRepositoryMock.findOne(project.getId())).thenReturn(project);

        ServiceResult<Void> result = service.saveDocumentsSubmitDateTime(project.getId(), LocalDateTime.now());

        assertTrue(result.isFailure());
        assertNull(project.getCollaborationAgreement());
        assertNull(project.getExploitationPlan());
        assertTrue(project.getProjectUsers().get(0).getRole().getName()
                .equals(UserRoleType.PROJECT_MANAGER.getName()));
        assertNull(project.getDocumentsSubmittedDate());
    }


    private void assertSetDocumentsDateimeIfProjectManagerAndFilesExist(Consumer<FileEntry> fileSetter1,
                                                                       Consumer<FileEntry> fileSetter2,
                                                                       Supplier<ServiceResult<Void>> getConditionFn) {
        Supplier<InputStream> inputStreamSupplier1 = () -> null;
        Supplier<InputStream> inputStreamSupplier2 = () -> null;

        getFileEntryResources(fileSetter1, fileSetter2, inputStreamSupplier1, inputStreamSupplier2);
        ServiceResult<Void> result = getConditionFn.get();

        assertTrue(result.isSuccess());

    }



    @Test
    public void testAddPartnerOrganisationNotOnProject(){
        Organisation o = newOrganisation().build();
        Organisation organisationNotOnProject = newOrganisation().build();
        User u = newUser().build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_PARTNER).withUser(u).withOrganisation(o).build(1);
        Project p = newProject().withProjectUsers(pu).build();
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(organisationRepositoryMock.findOne(organisationNotOnProject.getId())).thenReturn(organisationNotOnProject);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        // Method under test
        ServiceResult<Void> shouldFail = service.addPartner(p.getId(), u.getId(), organisationNotOnProject.getId());
        // Expectations
        assertTrue(shouldFail.isFailure());
        assertTrue(shouldFail.getFailure().is(badRequestError("project does not contain organisation")));
    }

    @Test
    public void testAddPartnerPartnerAlreadyExists(){
        Organisation o = newOrganisation().build();
        User u = newUser().build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_PARTNER).withUser(u).withOrganisation(o).build(1);
        Project p = newProject().withProjectUsers(pu).build();
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        // Method under test
        ServiceResult<Void> shouldFail = service.addPartner(p.getId(), u.getId(), o.getId());
        // Expectations
        verifyZeroInteractions(projectUserRepositoryMock);
        assertTrue(shouldFail.isSuccess());
    }

    @Test
    public void testAddPartner(){
        Organisation o = newOrganisation().build();
        User u = newUser().build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_PARTNER).withUser(u).withOrganisation(o).build(1);
        Project p = newProject().withProjectUsers(pu).build();
        User newUser = newUser().build();
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(userRepositoryMock.findOne(newUser.getId())).thenReturn(u);
        // Method under test
        ServiceResult<Void> shouldFail = service.addPartner(p.getId(), newUser.getId(), o.getId());
        // Expectations
        verify(projectUserRepositoryMock).save(isA(ProjectUser.class));
        assertTrue(shouldFail.isSuccess());
    }

    @Test
    public void testGetProjectTeamStatus(){
        Role partnerRole = newRole().withType(PARTNER).build();

        /**
         * Create 3 organisations:
         * 2 Business, 1 Academic
         * **/
        OrganisationType businessOrganisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        OrganisationType academicOrganisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.ACADEMIC).build();
        List<Organisation> organisation = newOrganisation().withOrganisationType(businessOrganisationType).build(2);
        organisation.add(newOrganisation().withOrganisationType(academicOrganisationType).build());

        /**
         * Create 3 users project partner roles for each of the 3 organisations above
         */
        List<User> users = newUser().build(3);
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_PARTNER).withUser(users.get(0), users.get(1), users.get(2)).withOrganisation(organisation.get(0), organisation.get(1), organisation.get(2)).build(3);

        /**
         * Create a project with 3 Project Users from 3 different organisations with an associated application
         */
        Project p = newProject().withProjectUsers(pu).withApplication(application).build();

        /**
         * Create 3 bank detail records, one for each organisation
         */
        List<BankDetails> bankDetails = newBankDetails().withOrganisation(organisation.get(0), organisation.get(1), organisation.get(2)).build(3);

        /**
         * Build spend profile object for use with one of the partners
         */
        SpendProfile spendProfile = newSpendProfile().build();

        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);

        when(projectUserRepositoryMock.findByProjectId(p.getId())).thenReturn(pu);

        when(bankDetailsRepositoryMock.findByProjectIdAndOrganisationId(p.getId(), organisation.get(0).getId())).thenReturn(bankDetails.get(0));

        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(p.getId(), organisation.get(0).getId())).thenReturn(spendProfile);

        MonitoringOfficer monitoringOfficerInDB = MonitoringOfficerBuilder.newMonitoringOfficer().build();
        when(monitoringOfficerRepositoryMock.findOneByProjectId(p.getId())).thenReturn(monitoringOfficerInDB);

        when(organisationRepositoryMock.findOne(organisation.get(0).getId())).thenReturn(organisation.get(0));
        when(organisationRepositoryMock.findOne(organisation.get(1).getId())).thenReturn(organisation.get(1));
        when(organisationRepositoryMock.findOne(organisation.get(2).getId())).thenReturn(organisation.get(2));

        List<ApplicationFinance> applicationFinances = newApplicationFinance().build(3);
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(p.getApplication().getId(), organisation.get(0).getId())).thenReturn(applicationFinances.get(0));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(p.getApplication().getId(), organisation.get(1).getId())).thenReturn(applicationFinances.get(1));
        when(applicationFinanceRepositoryMock.findByApplicationIdAndOrganisationId(p.getApplication().getId(), organisation.get(2).getId())).thenReturn(applicationFinances.get(2));

        ApplicationFinanceResource applicationFinanceResource0 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisation.get(0).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(0))).thenReturn(applicationFinanceResource0);

        ApplicationFinanceResource applicationFinanceResource1 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisation.get(1).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(1))).thenReturn(applicationFinanceResource1);

        ApplicationFinanceResource applicationFinanceResource2 = newApplicationFinanceResource().withGrantClaimPercentage(20).withOrganisation(organisation.get(2).getId()).build();
        when(applicationFinanceMapperMock.mapToResource(applicationFinances.get(2))).thenReturn(applicationFinanceResource2);

        List<ProjectUserResource> puResource = newProjectUserResource().withProject(p.getId()).withOrganisation(organisation.get(0).getId(), organisation.get(1).getId(), organisation.get(2).getId()).withRole(partnerRole.getId()).withRoleName(PROJECT_PARTNER.getName()).build(3);

        when(projectUserMapperMock.mapToResource(pu.get(0))).thenReturn(puResource.get(0));
        when(projectUserMapperMock.mapToResource(pu.get(1))).thenReturn(puResource.get(1));
        when(projectUserMapperMock.mapToResource(pu.get(2))).thenReturn(puResource.get(2));

        when(financeRowServiceMock.organisationSeeksFunding(p.getId(), p.getApplication().getId(), organisation.get(0).getId())).thenReturn(serviceSuccess(true));
        when(financeRowServiceMock.organisationSeeksFunding(p.getId(), p.getApplication().getId(), organisation.get(1).getId())).thenReturn(serviceSuccess(false));
        when(financeRowServiceMock.organisationSeeksFunding(p.getId(), p.getApplication().getId(), organisation.get(2).getId())).thenReturn(serviceSuccess(false));

        List<ProjectPartnerStatusResource> expectedPartnerStatuses = newProjectPartnerStatusResource().
                withName(organisation.get(0).getName(), organisation.get(1).getName(), organisation.get(2).getName()).
                withOrganisationType(OrganisationTypeEnum.getFromId(organisation.get(0).getOrganisationType().getId()), OrganisationTypeEnum.getFromId(organisation.get(1).getOrganisationType().getId()), OrganisationTypeEnum.getFromId(organisation.get(2).getOrganisationType().getId())).
                withProjectDetailsStatus(ACTION_REQUIRED, ACTION_REQUIRED, ACTION_REQUIRED).
                withMonitoringOfficerStatus(NOT_REQUIRED, NOT_REQUIRED, NOT_REQUIRED).
                withBankDetailsStatus(PENDING, NOT_REQUIRED, NOT_REQUIRED).
                withFinanceChecksStatus(ACTION_REQUIRED, ACTION_REQUIRED, ACTION_REQUIRED).
                withSpendProfileStatus(ACTION_REQUIRED, NOT_STARTED, NOT_STARTED).
                withOtherDocumentsStatus(NOT_REQUIRED, NOT_REQUIRED, NOT_REQUIRED).
                withGrantOfferStatus(NOT_REQUIRED, NOT_REQUIRED, NOT_REQUIRED).
                build(3);

        ProjectTeamStatusResource expectedProjectTeamStatusResource = newProjectTeamStatusResource().withPartnerStatuses(expectedPartnerStatuses).build();

        ServiceResult<ProjectTeamStatusResource> result = service.getProjectTeamStatus(p.getId());

        result.isSuccess();

        assertEquals(expectedProjectTeamStatusResource, result.getSuccessObject());
    }

    private void assertFilesCannotBeSubmittedIfNotByProjectManager(Consumer<FileEntry> fileSetter1,
                                                                   Consumer<FileEntry> fileSetter2,
                                                                   Supplier<ServiceResult<Boolean>> getConditionFn) {
        List<ProjectUser> projectUsers = new ArrayList<>();
        Arrays.stream(ProjectParticipantRole.values())
                .filter(roleType -> roleType != PROJECT_MANAGER)
                .forEach(roleType -> {
                    ProjectUser projectUser = newProjectUser()
                            .withId(3L)
                            .withRole(roleType)
                            .build();
                    projectUsers.add(projectUser);

                });

        when(projectUserRepositoryMock.findByProjectId(123L)).thenReturn(projectUsers);

        Supplier<InputStream> inputStreamSupplier1 = () -> null;
        Supplier<InputStream> inputStreamSupplier2 = () -> null;

        getFileEntryResources(fileSetter1, fileSetter2, inputStreamSupplier1, inputStreamSupplier2);
        ServiceResult<Boolean> result = getConditionFn.get();

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());

    }


    private void assertFilesCanBeSubmittedByProjectManagerAndFilesExist(Consumer<FileEntry> fileSetter1,
                                                                        Consumer<FileEntry> fileSetter2,
                                                                        Supplier<ServiceResult<Boolean>> getConditionFn) {
        ProjectUser projectUserToSet = newProjectUser()
                .withId(1L)
                .withUser(newUser().withid(1L).build())
                .withRole(PROJECT_MANAGER)
                .build();
        List<ProjectUser> projectUsers = new ArrayList<>();
        projectUsers.add(projectUserToSet);

        when(projectUserRepositoryMock.findByProjectId(123L)).thenReturn(projectUsers);

        Supplier<InputStream> inputStreamSupplier1 = () -> null;
        Supplier<InputStream> inputStreamSupplier2 = () -> null;

        getFileEntryResources(fileSetter1, fileSetter2, inputStreamSupplier1, inputStreamSupplier2);
        ServiceResult<Boolean> result = getConditionFn.get();

        assertTrue(result.isSuccess());
        assertTrue(result.getSuccessObject());

    }

    private void assertUploadedFilesExist(Consumer<FileEntry> fileSetter1, Consumer<FileEntry> fileSetter2,
                                          Supplier<List<ServiceResult<FileAndContents>>> getFileContentsFnForFiles) {


        Supplier<InputStream> inputStreamSupplier1 = () -> null;
        Supplier<InputStream> inputStreamSupplier2 = () -> null;

        List<FileEntryResource> fileEntryResourcesToGet = getFileEntryResources(fileSetter1, fileSetter2, inputStreamSupplier1, inputStreamSupplier2);

        List<ServiceResult<FileAndContents>> results = getFileContentsFnForFiles.get();

        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals(fileEntryResourcesToGet.get(0), results.get(0).getSuccessObject().getFileEntry());
        assertEquals(fileEntryResourcesToGet.get(1), results.get(1).getSuccessObject().getFileEntry());
        assertEquals(inputStreamSupplier1, results.get(0).getSuccessObject().getContentsSupplier());
        assertEquals(inputStreamSupplier2, results.get(1).getSuccessObject().getContentsSupplier());

    }

    private List<FileEntryResource> getFileEntryResources(Consumer<FileEntry> fileSetter1, Consumer<FileEntry> fileSetter2,
                                                          Supplier<InputStream> inputStreamSupplier1,
                                                          Supplier<InputStream> inputStreamSupplier2) {
        FileEntry fileEntry1ToGet = newFileEntry().build();
        FileEntry fileEntry2ToGet = newFileEntry().build();

        List<FileEntryResource> fileEntryResourcesToGet = newFileEntryResource().withFilesizeBytes(100).build(2);

        fileSetter1.accept(fileEntry1ToGet);
        fileSetter2.accept(fileEntry2ToGet);

        when(fileServiceMock.getFileByFileEntryId(fileEntry1ToGet.getId())).thenReturn(serviceSuccess(inputStreamSupplier1));
        when(fileServiceMock.getFileByFileEntryId(fileEntry2ToGet.getId())).thenReturn(serviceSuccess(inputStreamSupplier2));

        when(fileEntryMapperMock.mapToResource(fileEntry1ToGet)).thenReturn(fileEntryResourcesToGet.get(0));
        when(fileEntryMapperMock.mapToResource(fileEntry2ToGet)).thenReturn(fileEntryResourcesToGet.get(1));
        return fileEntryResourcesToGet;
    }

    private void assertDeleteFile(Supplier<FileEntry> fileGetter, Consumer<FileEntry> fileSetter, Supplier<ServiceResult<Void>> deleteFileFn) {
        FileEntry fileToDelete = newFileEntry().build();

        fileSetter.accept(fileToDelete);
        when(fileServiceMock.deleteFile(fileToDelete.getId())).thenReturn(serviceSuccess(fileToDelete));

        ServiceResult<Void> result = deleteFileFn.get();
        assertTrue(result.isSuccess());
        assertNull(fileGetter.get());

        verify(fileServiceMock).deleteFile(fileToDelete.getId());
    }

    private void assertUpdateFile(Supplier<FileEntry> fileGetter, BiFunction<FileEntryResource, Supplier<InputStream>, ServiceResult<Void>> updateFileFn) {
        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntry updatedFile = newFileEntry().build();
        FileEntryResource updatedFileResource = newFileEntryResource().build();

        when(fileServiceMock.updateFile(fileToUpdate, inputStreamSupplier)).thenReturn(serviceSuccess(Pair.of(new File("blah"), updatedFile)));
        when(fileEntryMapperMock.mapToResource(updatedFile)).thenReturn(updatedFileResource);

        ServiceResult<Void> result = updateFileFn.apply(fileToUpdate, inputStreamSupplier);
        assertTrue(result.isSuccess());
        assertEquals(updatedFile, fileGetter.get());

        verify(fileServiceMock).updateFile(fileToUpdate, inputStreamSupplier);
    }

    private Project createProjectExpectationsFromOriginalApplication(Application application) {

        assertFalse(application.getProcessRoles().isEmpty());

        return createLambdaMatcher(project -> {
            assertEquals(application.getName(), project.getName());
            assertEquals(application.getDurationInMonths(), project.getDurationInMonths());
            assertEquals(application.getStartDate(), project.getTargetStartDate());
            assertFalse(project.getProjectUsers().isEmpty());
            assertNull(project.getAddress());

            List<ProcessRole> collaborativeRoles = simpleFilter(application.getProcessRoles(), ProcessRole::isLeadApplicantOrCollaborator);

            assertEquals(collaborativeRoles.size(), project.getProjectUsers().size());

            collaborativeRoles.forEach(processRole -> {

                List<ProjectUser> matchingProjectUser = simpleFilter(project.getProjectUsers(), projectUser ->
                        projectUser.getOrganisation().equals(processRole.getOrganisation()) &&
                                projectUser.getUser().equals(processRole.getUser()));

                assertEquals(1, matchingProjectUser.size());
                assertEquals(PARTNER.getName(), matchingProjectUser.get(0).getRole().getName());
                assertEquals(project, matchingProjectUser.get(0).getProcess());
            });
        });
    }

    @Override
    protected ProjectService supplyServiceUnderTest() {
        return new ProjectServiceImpl();
    }
}
