package org.innovateuk.ifs.invite.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.ApplicationInvite;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.resource.InviteResultsResource;
import org.innovateuk.ifs.invite.transactional.InviteService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.ApplicationInviteBuilder.newInvite;
import static org.innovateuk.ifs.invite.builder.InviteOrganisationResourceBuilder.newInviteOrganisationResource;
import static org.innovateuk.ifs.invite.builder.InviteResourceBuilder.newInviteResource;
import static org.innovateuk.ifs.invite.builder.InviteResultResourceBuilder.newInviteResultResource;
import static org.innovateuk.ifs.invite.security.InviteServiceSecurityTest.TestInviteService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Testing how the secured methods in InviteService interact with Spring Security
 */
public class InviteServiceSecurityTest extends BaseServiceSecurityTest<InviteService> {

    ApplicationInvitePermissionRules invitePermissionRules;
    InviteOrganisationPermissionRules inviteOrganisationPermissionRules;

    @Before
    public void lookupPermissionRules() {
        invitePermissionRules = getMockPermissionRulesBean(ApplicationInvitePermissionRules.class);
        inviteOrganisationPermissionRules = getMockPermissionRulesBean(InviteOrganisationPermissionRules.class);
    }

    @Test
    public void testInviteCollaborators() {
        final int nInvites = 2;
        final String baseUrl = "test";
        final List<ApplicationInvite> invites = newInvite().build(nInvites);
        classUnderTest.inviteCollaborators(baseUrl, invites);
        verify(invitePermissionRules, times(nInvites)).leadApplicantCanInviteToTheApplication(any(ApplicationInvite.class), any(UserResource.class));
        verify(invitePermissionRules, times(nInvites)).collaboratorCanInviteToApplicationForTheirOrganisation(any(ApplicationInvite.class), any(UserResource.class));
    }

    @Test
    public void testInviteCollaboratorToApp() {
        final String baseUrl = "test";
        final ApplicationInvite invite = newInvite().build();
        assertAccessDenied(
                () -> classUnderTest.inviteCollaboratorToApplication(baseUrl, invite),
                () -> {
                    verify(invitePermissionRules).leadApplicantCanInviteToTheApplication(eq(invite), any(UserResource.class));
                    verify(invitePermissionRules).collaboratorCanInviteToApplicationForTheirOrganisation(eq(invite), any(UserResource.class));
                });
    }

    @Test
    public void testCreateApplicationInvites() {
        final InviteOrganisationResource inviteOrganisation = newInviteOrganisationResource().build();
        assertAccessDenied(
                () -> classUnderTest.createApplicationInvites(inviteOrganisation),
                () -> {
                    verify(inviteOrganisationPermissionRules).leadApplicantCanInviteAnOrganisationToTheApplication(eq(inviteOrganisation), any(UserResource.class));
                });
    }


    @Test
    public void testFindOne() {
        final long inviteId = 1L;
        assertAccessDenied(
                () -> classUnderTest.findOne(inviteId),
                () -> {
                    verify(invitePermissionRules).collaboratorCanReadInviteForTheirApplicationForTheirOrganisation(any(ApplicationInvite.class), any(UserResource.class));
                    verify(invitePermissionRules).leadApplicantReadInviteToTheApplication(any(ApplicationInvite.class), any(UserResource.class));
                });
    }

    @Test
    public void testGetInvitesByApplication() {
        long applicationId = 1L;
        final ServiceResult<Set<InviteOrganisationResource>> results = classUnderTest.getInvitesByApplication(applicationId);
        verify(inviteOrganisationPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).leadApplicantCanViewOrganisationInviteToTheApplication(any(InviteOrganisationResource.class), any(UserResource.class));
        verify(inviteOrganisationPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).collaboratorCanViewOrganisationInviteToTheApplication(any(InviteOrganisationResource.class), any(UserResource.class));
        assertTrue(results.getSuccessObject().isEmpty());
    }

    @Test
    public void testSaveInvites() {
        int nInvites = 2;
        final List<ApplicationInviteResource> invites = newInviteResource().build(nInvites);
        classUnderTest.saveInvites(invites);
        verify(invitePermissionRules, times(nInvites)).collaboratorCanSaveInviteToApplicationForTheirOrganisation(any(ApplicationInviteResource.class), any(UserResource.class));
        verify(invitePermissionRules, times(nInvites)).leadApplicantCanSaveInviteToTheApplication(any(ApplicationInviteResource.class), any(UserResource.class));
    }

    @Override
    protected Class<? extends InviteService> getClassUnderTest() {
        return TestInviteService.class;
    }

    public static class TestInviteService implements InviteService {

        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public List<ServiceResult<Void>> inviteCollaborators(String baseUrl, List<ApplicationInvite> invites) {
            return new ArrayList<>();
        }

        @Override
        public ServiceResult<Void> inviteCollaboratorToApplication(String baseUrl, ApplicationInvite invite) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationInvite> findOne(Long id) {
            return serviceSuccess(newInvite().build());
        }

        @Override
        public ServiceResult<InviteResultsResource> createApplicationInvites(InviteOrganisationResource inviteOrganisationResource) {
            return null;
        }

        @Override
        public ServiceResult<InviteOrganisationResource> getInviteOrganisationByHash(String hash) {
            return null;
        }

        @Override
        public ServiceResult<Set<InviteOrganisationResource>> getInvitesByApplication(Long applicationId) {
            return serviceSuccess(newInviteOrganisationResource().buildSet(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<InviteResultsResource> saveInvites(List<ApplicationInviteResource> inviteResources) {
            return serviceSuccess(newInviteResultResource().build());
        }

        @Override
        public ServiceResult<Void> acceptInvite(String inviteHash, Long userId) {
            return null;
        }

        @Override
        public ServiceResult<ApplicationInviteResource> getInviteByHash(String hash) {
            return null;
        }

        @Override
        public ServiceResult<Boolean> checkUserExistingByInviteHash(@P("hash") String hash) {
            return null;
        }

        @Override
        public ServiceResult<UserResource> getUserByInviteHash(@P("hash") String hash) {
            return null;
        }

        @Override
        public ServiceResult<Void> removeApplicationInvite(Long applicationInviteId) {
            return null;
        }
    }
}
