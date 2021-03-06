package org.innovateuk.ifs.competition.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.application.resource.ApplicationPageResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionCountResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResult;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.user.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.builder.CompetitionSearchResultItemBuilder.newCompetitionSearchResultItem;
import static org.innovateuk.ifs.user.builder.OrganisationTypeResourceBuilder.newOrganisationTypeResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_FINANCE;
import static org.innovateuk.ifs.user.resource.UserRoleType.SYSTEM_REGISTRATION_USER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Testing the permission rules applied to the secured methods in OrganisationService.  This set of tests tests for the
 * individual rules that are called whenever an OrganisationService method is called.  They do not however test the logic
 * within those rules
 */
public class CompetitionServiceSecurityTest extends BaseServiceSecurityTest<CompetitionService> {

    private CompetitionLookupStrategy competitionLookupStrategy;

    private CompetitionPermissionRules rules;

    @Before
    public void lookupPermissionRules() {

        rules = getMockPermissionRulesBean(CompetitionPermissionRules.class);
        competitionLookupStrategy = getMockPermissionEntityLookupStrategiesBean(CompetitionLookupStrategy.class);

        initMocks(this);
    }

    @Override
    protected Class<? extends CompetitionService> getClassUnderTest() {
        return TestCompetitionService.class;
    }

    @Test
    public void findAll() {
        setLoggedInUser(null);

        ServiceResult<List<CompetitionResource>> results = classUnderTest.findAll();
        assertEquals(0, results.getSuccessObject().size());

        verify(rules, times(2)).externalUsersCannotViewCompetitionsInSetup(isA(CompetitionResource.class), isNull(UserResource.class));
        verify(rules, times(2)).internalUserCanViewAllCompetitions(isA(CompetitionResource.class), isNull(UserResource.class));
        verify(rules, times(2)).innovationLeadCanViewCompetitionAssignedToThem(isA(CompetitionResource.class), isNull(UserResource.class));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void getCompetitionById() {
        setLoggedInUser(null);

        assertAccessDenied(() -> classUnderTest.getCompetitionById(1L), () -> {
            verify(rules).externalUsersCannotViewCompetitionsInSetup(isA(CompetitionResource.class), isNull(UserResource.class));
            verify(rules).internalUserCanViewAllCompetitions(isA(CompetitionResource.class), isNull(UserResource.class));
            verify(rules).innovationLeadCanViewCompetitionAssignedToThem(isA(CompetitionResource.class), isNull(UserResource.class));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void findInnovationLeads() {

        Long competitionId = 1L;
        CompetitionResource competitionResource = CompetitionResourceBuilder.newCompetitionResource().build();

        when(competitionLookupStrategy.getCompetititionResource(competitionId)).thenReturn(competitionResource);

        assertAccessDenied(
                () -> classUnderTest.findInnovationLeads(1L),
                () -> {
                    verify(rules).internalAdminCanManageInnovationLeadsForCompetition(any(CompetitionResource.class), any(UserResource.class));
                    verifyNoMoreInteractions(rules);
                });
    }

    @Test
    public void addInnovationLead() {
        Long competitionId = 1L;
        Long innovationLeadUserId = 2L;
        CompetitionResource competitionResource = CompetitionResourceBuilder.newCompetitionResource().build();

        when(competitionLookupStrategy.getCompetititionResource(competitionId)).thenReturn(competitionResource);

        assertAccessDenied(() -> classUnderTest.addInnovationLead(competitionId, innovationLeadUserId), () -> {
            verify(rules).internalAdminCanManageInnovationLeadsForCompetition(any(CompetitionResource.class), any(UserResource.class));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void removeInnovationLead() {
        Long competitionId = 1L;
        Long innovationLeadUserId = 2L;
        CompetitionResource competitionResource = CompetitionResourceBuilder.newCompetitionResource().build();

        when(competitionLookupStrategy.getCompetititionResource(competitionId)).thenReturn(competitionResource);

        assertAccessDenied(() -> classUnderTest.removeInnovationLead(competitionId, innovationLeadUserId), () -> {
            verify(rules).internalAdminCanManageInnovationLeadsForCompetition(any(CompetitionResource.class), any(UserResource.class));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void getCompetitionOrganisationTypesById() {
        runAsRole(SYSTEM_REGISTRATION_USER, () -> classUnderTest.getCompetitionOrganisationTypes(1L));
    }

    @Test
    public void findLiveCompetitions() {
        setLoggedInUser(null);

        ServiceResult<List<CompetitionSearchResultItem>> results = classUnderTest.findLiveCompetitions();
        assertEquals(0, results.getSuccessObject().size());

        verify(rules, times(2)).internalUserCanViewAllCompetitionSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verify(rules, times(2)).innovationLeadCanViewCompetitionAssignedToThemInSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void findProjectSetupCompetitions() {
        setLoggedInUser(null);

        ServiceResult<List<CompetitionSearchResultItem>> results = classUnderTest.findProjectSetupCompetitions();
        assertEquals(0, results.getSuccessObject().size());

        verify(rules, times(2)).internalUserCanViewAllCompetitionSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verify(rules, times(2)).innovationLeadCanViewCompetitionAssignedToThemInSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void findUpcomingCompetitions() {
        setLoggedInUser(null);

        ServiceResult<List<CompetitionSearchResultItem>> results = classUnderTest.findUpcomingCompetitions();
        assertEquals(0, results.getSuccessObject().size());

        verify(rules, times(2)).internalUserCanViewAllCompetitionSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verify(rules, times(2)).innovationLeadCanViewCompetitionAssignedToThemInSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void findUnsuccessfulApplications() {
        Long competitionId = 1L;
        CompetitionResource competitionResource = CompetitionResourceBuilder.newCompetitionResource().build();

        when(competitionLookupStrategy.getCompetititionResource(competitionId)).thenReturn(competitionResource);

        assertAccessDenied(() -> classUnderTest.findUnsuccessfulApplications(competitionId, 0, 0, ""), () -> {
            verify(rules).internalUsersAndIFSAdminCanViewUnsuccessfulApplications(any(CompetitionResource.class), any(UserResource.class));
            verify(rules).innovationLeadForCompetitionCanViewUnsuccessfulApplications(any(CompetitionResource.class), any(UserResource.class));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void countCompetitions() {
        setLoggedInUser(null);

        assertAccessDenied(() -> classUnderTest.countCompetitions(), () -> verifyNoMoreInteractions(rules));
    }

    @Test
    public void searchCompetitions() {
        setLoggedInUser(null);

        assertAccessDenied(() -> classUnderTest.searchCompetitions("string", 1, 1), () -> {
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void closeAssessment() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.notifyAssessors(1L), PROJECT_FINANCE, COMP_ADMIN);
    }

    @Test
    public void notifyAssessors() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.notifyAssessors(1L), PROJECT_FINANCE, COMP_ADMIN);
    }

    @Test
    public void releaseFeedback() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.releaseFeedback(1L), PROJECT_FINANCE, COMP_ADMIN);
    }

    @Test
    public void manageInformState() {
        testOnlyAUserWithOneOfTheGlobalRolesCan(() -> classUnderTest.manageInformState(1L), PROJECT_FINANCE, COMP_ADMIN);
    }

    @Test
    public void findPreviousCompetitions() {
        setLoggedInUser(null);

        ServiceResult<List<CompetitionSearchResultItem>> results = classUnderTest.findFeedbackReleasedCompetitions();
        assertEquals(0, results.getSuccessObject().size());

        verify(rules, times(2)).internalUserCanViewAllCompetitionSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verify(rules, times(2)).innovationLeadCanViewCompetitionAssignedToThemInSearchResults(isA(CompetitionSearchResultItem.class), isNull(UserResource.class));
        verifyNoMoreInteractions(rules);
    }


    private void runAsRole(UserRoleType roleType, Runnable serviceCall) {
        setLoggedInUser(
                newUserResource()
                        .withRolesGlobal(singletonList(
                                newRoleResource()
                                        .withType(roleType)
                                        .build()
                                )
                        )
                        .build());
        serviceCall.run();
    }

    /**
     * Dummy implementation (for satisfying Spring Security's need to read parameter information from
     * methods, which is lost when using mocks)
     */
    public static class TestCompetitionService implements CompetitionService {

        @Override
        public ServiceResult<CompetitionResource> getCompetitionById(Long id) {
            return serviceSuccess(newCompetitionResource().build());
        }

        @Override
        public ServiceResult<List<CompetitionResource>> getCompetitionsByUserId(Long userId) {
            return serviceSuccess(newCompetitionResource().build(2));
        }

        @Override
        public ServiceResult<List<UserResource>> findInnovationLeads(Long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> addInnovationLead(Long competitionId, Long innovationLeadUserId) {
            return null;
        }

        @Override
        public ServiceResult<Void> removeInnovationLead(Long competitionId, Long innovationLeadUserId) {
            return null;
        }

        @Override
        public ServiceResult<List<OrganisationTypeResource>> getCompetitionOrganisationTypes(long id) {
            return serviceSuccess(newOrganisationTypeResource().build(2));
        }

        @Override
        public ServiceResult<List<CompetitionResource>> findAll() {
            return serviceSuccess(newCompetitionResource().build(2));
        }

        @Override
        public ServiceResult<List<CompetitionSearchResultItem>> findLiveCompetitions() {
            return serviceSuccess(newCompetitionSearchResultItem().build(2));
        }

        @Override
        public ServiceResult<List<CompetitionSearchResultItem>> findProjectSetupCompetitions() {
            return serviceSuccess(newCompetitionSearchResultItem().build(2));
        }

        @Override
        public ServiceResult<List<CompetitionSearchResultItem>> findUpcomingCompetitions() {
            return serviceSuccess(newCompetitionSearchResultItem().build(2));
        }

        @Override
        public ServiceResult<List<CompetitionSearchResultItem>> findNonIfsCompetitions() {
            return serviceSuccess(newCompetitionSearchResultItem().build(2));
        }

        @Override
        public ServiceResult<ApplicationPageResource> findUnsuccessfulApplications(Long competitionId, int pageIndex, int pageSize, String sortField) {
            return serviceSuccess(new ApplicationPageResource());
        }

        @Override
        public ServiceResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size) {
            return serviceSuccess(new CompetitionSearchResult());
        }

        @Override
        public ServiceResult<CompetitionCountResource> countCompetitions() {
            return serviceSuccess(new CompetitionCountResource());
        }

        @Override
        public ServiceResult<Void> closeAssessment(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> notifyAssessors(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> releaseFeedback(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<Void> manageInformState(long competitionId) {
            return null;
        }

        @Override
        public ServiceResult<List<CompetitionSearchResultItem>> findFeedbackReleasedCompetitions() {
            return serviceSuccess(newCompetitionSearchResultItem().build(2));
        }
    }
}
