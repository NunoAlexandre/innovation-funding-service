package org.innovateuk.ifs.project.security;


import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileAndContents;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.transactional.ProjectGrantOfferService;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_FINANCE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class ProjectGrantOfferServiceSecurityTest extends BaseServiceSecurityTest<ProjectGrantOfferService> {

    private ProjectGrantOfferPermissionRules projectGrantOfferPermissionRules;
    private ProjectLookupStrategy projectLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        projectGrantOfferPermissionRules = getMockPermissionRulesBean(ProjectGrantOfferPermissionRules.class);
        projectLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ProjectLookupStrategy.class);
    }

    @Test
    public void testSignedGetGrantOfferLetterFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getSignedGrantOfferLetterFileEntryDetails(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetGrantOfferLetterFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getGrantOfferLetterFileEntryDetails(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }


    @Test
    public void testGetAdditionalContractFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getAdditionalContractFileEntryDetails(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testCreateSignedGrantOfferLetterFileEntry() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.createSignedGrantOfferLetterFileEntry(projectId, null, null), () -> {
            verify(projectGrantOfferPermissionRules).leadPartnerCanUploadGrantOfferLetter(project, getLoggedInUser());
            verify(projectGrantOfferPermissionRules).projectManagerCanUploadGrantOfferLetter(project, getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testGetGrantOfferLetterFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getGrantOfferLetterFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetSignedGrantOfferLetterFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getSignedGrantOfferLetterFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetAdditionalContractFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getAdditionalContractFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testSubmitGrantOfferLetter() {
        final Long projectId = 1L;

        assertAccessDenied(() -> classUnderTest.submitGrantOfferLetter(projectId), () -> {
            verify(projectGrantOfferPermissionRules).projectManagerSubmitGrantOfferLetter(projectId, getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testGenerateGrantOfferLetterDeniedIfNotCorrectGlobalRoles() {

        final Long projectId = 1L;

        FileEntryResource fileEntryResource = newFileEntryResource().build();

        List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN && type != PROJECT_FINANCE)
                .collect(toList());

        nonCompAdminRoles.forEach(role -> {

            setLoggedInUser(
                    newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
            try {
                classUnderTest.generateGrantOfferLetter(projectId, fileEntryResource);
                Assert.fail("Should not have been able to generate GOL without the global Comp Admin role");
            } catch (AccessDeniedException e) {
                // expected behaviour
            }
        });
    }


    @Override
    protected Class<? extends ProjectGrantOfferService> getClassUnderTest() {
        return TestProjectGrantOfferService.class;
    }

    public static class TestProjectGrantOfferService implements ProjectGrantOfferService {

        @Override
        public ServiceResult<FileAndContents> getSignedGrantOfferLetterFileAndContents(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileAndContents> getGrantOfferLetterFileAndContents(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileAndContents> getAdditionalContractFileAndContents(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> getSignedGrantOfferLetterFileEntryDetails(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> getGrantOfferLetterFileEntryDetails(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> getAdditionalContractFileEntryDetails(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> createSignedGrantOfferLetterFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> createGrantOfferLetterFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> generateGrantOfferLetter(Long projectId, FileEntryResource fileEntryResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> removeGrantOfferLetterFileEntry(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> createAdditionalContractFileEntry(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<Void> updateSignedGrantOfferLetterFile(Long projectId, FileEntryResource fileEntryResource, Supplier<InputStream> inputStreamSupplier) {
            return null;
        }

        @Override
        public ServiceResult<Void> submitGrantOfferLetter(Long projectId) {
            return null;
        }

    }
}
