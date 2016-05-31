package com.worth.ifs.project.security;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.transactional.ProjectService;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.method.P;

import java.util.List;
import java.util.Map;

import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.resource.UserRoleType.APPLICANT;
import static com.worth.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Testing how the secured methods in ProjectService interact with Spring Security
 */
public class ProjectServiceSecurityTest extends BaseServiceSecurityTest<ProjectService> {

    private ProjectPermissionRules projectPermissionRules;
    private ProjectLookupStrategy projectLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        projectPermissionRules = getMockPermissionRulesBean(ProjectPermissionRules.class);
        projectLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ProjectLookupStrategy.class);
    }

    @Test
    public void testGetProjectById() {
        final Long projectId = 1L;

        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(newProjectResource().build());

        assertAccessDenied(
                () -> service.getProjectById(projectId),
                () -> {
                    verify(projectPermissionRules, times(1)).usersConnectedToTheProjectCanView(isA(ProjectResource.class), isA(UserResource.class));
                    verify(projectPermissionRules, times(1)).compAdminsCanViewProjects(isA(ProjectResource.class), isA(UserResource.class));
                }
        );
    }

    @Test
    public void test_createProjectFromApplicationAllowedIfCompAdminRole() {
        setLoggedInUser(newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(COMP_ADMIN).build())).build());
        service.createProjectFromApplication(123L);
    }

    @Test
    public void test_createProjectFromApplicationDeniedForApplicant() {
        setLoggedInUser(newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(APPLICANT).build())).build());
        try {
            service.createProjectFromApplication(123L);
            fail("Should not have been able to create project from application as applicant");
        } catch(AccessDeniedException ade){
            //expected behaviour
        }
    }

    @Override
    protected Class<TestProjectService> getServiceClass() {
        return TestProjectService.class;
    }

    public static class TestProjectService implements ProjectService {

        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<ProjectResource> getProjectById(@P("projectId") Long projectId) {
            return ServiceResult.serviceSuccess(newProjectResource().withId(1L).build());
        }

        @Override
        public ServiceResult<List<ProjectResource>> findAll() {
            return null;
        }

        @Override
        public ServiceResult<ProjectResource> createProjectFromApplication(Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<Void> createProjectsFromFundingDecisions(Map<Long, FundingDecision> applicationFundingDecisions) {
            return null;
        }
    }
}

