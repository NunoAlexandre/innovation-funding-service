package org.innovateuk.ifs.project.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.project.finance.resource.Eligibility;
import org.innovateuk.ifs.project.finance.resource.EligibilityResource;
import org.innovateuk.ifs.project.finance.resource.EligibilityStatus;
import org.innovateuk.ifs.project.finance.resource.Viability;
import org.innovateuk.ifs.project.finance.resource.ViabilityResource;
import org.innovateuk.ifs.project.finance.resource.ViabilityStatus;
import org.innovateuk.ifs.project.finance.transactional.ProjectFinanceService;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.fail;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_FINANCE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ProjectFinanceServiceSecurityTest extends BaseServiceSecurityTest<ProjectFinanceService> {

    private ProjectFinancePermissionRules projectFinancePermissionRules;

    @Before
    public void lookupPermissionRules() {
        projectFinancePermissionRules = getMockPermissionRulesBean(ProjectFinancePermissionRules.class);
    }

    @Test
    public void testGenerateSpendProfile() {

        asList(UserRoleType.values()).forEach(role -> {
            RoleResource roleResource = newRoleResource().withType(role).build();
            UserResource userWithRole = newUserResource().withRolesGlobal(singletonList(roleResource)).build();
            setLoggedInUser(userWithRole);

            if (PROJECT_FINANCE.equals(role) || COMP_ADMIN.equals(role)) {
                classUnderTest.generateSpendProfile(123L);
            } else {
                try {
                    classUnderTest.generateSpendProfile(123L);
                    fail("Should have thrown an AccessDeniedException for any non-Finance Team members");
                } catch (AccessDeniedException e) {
                    // expected behaviour
                }
            }
        });
    }

    @Test
    public void testGetSpendProfileTable() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.getSpendProfileTable(projectOrganisationCompositeId),
                () -> {

                    verify(projectFinancePermissionRules).partnersCanViewTheirOwnSpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).leadPartnerCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testGetSpendProfileCSV() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.getSpendProfileCSV(projectOrganisationCompositeId),
                () -> {
                    verify(projectFinancePermissionRules).partnersCanViewTheirOwnSpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).leadPartnerCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testGetSpendProfile() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.getSpendProfile(projectOrganisationCompositeId),
                () -> {

                    verify(projectFinancePermissionRules).partnersCanViewTheirOwnSpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verify(projectFinancePermissionRules).leadPartnerCanViewAnySpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testSaveSpendProfile() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        SpendProfileTableResource table = new SpendProfileTableResource();

        assertAccessDenied(() -> classUnderTest.saveSpendProfile(projectOrganisationCompositeId, table),
                () -> {

                    verify(projectFinancePermissionRules).partnersCanEditTheirOwnSpendProfileData(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testApproveOrRejectSpendProfile() {

        List<UserRoleType> nonCompAdminRoles = getNonProjectFinanceUserRoles();
        nonCompAdminRoles.forEach(role -> {
            setLoggedInUser(
                    newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
            try {
                classUnderTest.approveOrRejectSpendProfile(1L, ApprovalType.APPROVED);
                Assert.fail("Should not have been able to create project from application without the global Comp Admin role");
            } catch (AccessDeniedException e) {
                // expected behaviour
            }
        });
    }

    @Test
    public void testGetSpendProfileStatusByProjectId() {

        List<UserRoleType> nonCompAdminRoles = getNonProjectFinanceUserRoles();
        nonCompAdminRoles.forEach(role -> {
            setLoggedInUser(
                    newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
            try {
                classUnderTest.getSpendProfileStatusByProjectId(1L);
                Assert.fail("Should not have been able to create project from application without the global Comp Admin role");
            } catch (AccessDeniedException e) {
                // expected behaviour
            }
        });
    }

    @Test
    public void testMarkSpendProfileComplete() {

        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.markSpendProfileComplete(projectOrganisationCompositeId),
                () -> {

                    verify(projectFinancePermissionRules).partnersCanMarkSpendProfileAsComplete(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testCompleteSpendProfilesReview() {
        Long projectId = 1L;

        assertAccessDenied(() -> classUnderTest.completeSpendProfilesReview(projectId),
                () -> {
                    verify(projectFinancePermissionRules).projectManagerCanCompleteSpendProfile(projectId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testGetViability() {
        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.getViability(projectOrganisationCompositeId),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewViability(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testSaveViability() {
        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.saveViability(projectOrganisationCompositeId, Viability.APPROVED, ViabilityStatus.RED),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanSaveViability(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testGetEligibility() {
        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.getEligibility(projectOrganisationCompositeId),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewEligibility(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testSaveEligibility() {
        Long projectId = 1L;
        Long organisationId = 1L;

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        assertAccessDenied(() -> classUnderTest.saveEligibility(projectOrganisationCompositeId, Eligibility.APPROVED, EligibilityStatus.RED),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanSaveEligibility(projectOrganisationCompositeId, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testGetCreditReport() {
        assertAccessDenied(() -> classUnderTest.getCreditReport(1L, 2L),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanViewCreditReport(1L, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Test
    public void testSetCreditReport() {
        assertAccessDenied(() -> classUnderTest.saveCreditReport(1L, 2L, Boolean.TRUE),
                () -> {
                    verify(projectFinancePermissionRules).projectFinanceUserCanSaveCreditReport(1L, getLoggedInUser());
                    verifyNoMoreInteractions(projectFinancePermissionRules);
                });
    }

    @Override
    protected Class<TestProjectFinanceService> getClassUnderTest() {
        return TestProjectFinanceService.class;
    }

    public static class TestProjectFinanceService implements ProjectFinanceService {

        @Override
        public ServiceResult<Void> generateSpendProfile(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<ApprovalType> getSpendProfileStatusByProjectId(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<SpendProfileTableResource> getSpendProfileTable(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        @Override
        public ServiceResult<SpendProfileResource> getSpendProfile(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        @Override
        public ServiceResult<Void> saveSpendProfile(ProjectOrganisationCompositeId projectOrganisationCompositeId, SpendProfileTableResource table) {
            return null;
        }

        @Override
        public ServiceResult<Void> markSpendProfileComplete(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        @Override
        public ServiceResult<Void> markSpendProfileIncomplete(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        public ServiceResult<Void> completeSpendProfilesReview(Long projectId) {
            return null;
        }

        @Override
        public ServiceResult<ViabilityResource> getViability(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        @Override
        public ServiceResult<Void> saveViability(ProjectOrganisationCompositeId projectOrganisationCompositeId, Viability viability, ViabilityStatus viabilityStatus) {
            return null;
        }

        @Override
        public ServiceResult<EligibilityResource> getEligibility(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }

        @Override
        public ServiceResult<Void> saveEligibility(ProjectOrganisationCompositeId projectOrganisationCompositeId, Eligibility eligibility, EligibilityStatus eligibilityStatus) {
            return null;
        }

        @Override
        public ServiceResult<Void> approveOrRejectSpendProfile(Long projectId, ApprovalType approvalType) {
            return null;
        }

        @Override
        public ServiceResult<SpendProfileCSVResource> getSpendProfileCSV(ProjectOrganisationCompositeId projectOrganisationCompositeId) {
            return null;
        }
        @Override
        public ServiceResult<Boolean> getCreditReport(Long projectId, Long organisationId) { return null; }

        @Override
        public ServiceResult<Void> saveCreditReport(Long projectId, Long organisationId, boolean creditReportPresent) { return null; }

        @Override
        public ServiceResult<List<ProjectFinanceResource>> getProjectFinances(Long projectId) {
            return null;
        }
    }

    private List<UserRoleType> getNonProjectFinanceUserRoles() {
        return asList(UserRoleType.values()).stream().filter(type -> type != PROJECT_FINANCE && type != COMP_ADMIN)
                .collect(toList());
    }
}

