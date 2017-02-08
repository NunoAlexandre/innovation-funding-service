package org.innovateuk.ifs.project.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.transactional.PartnerOrganisationService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationResourceBuilder.newPartnerOrganisationResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PartnerOrganisationServiceSecurityTest extends BaseServiceSecurityTest<PartnerOrganisationService> {

    private PartnerOrganisationPermissionRules partnerOrganisationPermissionRules;
    private static List<PartnerOrganisationResource> partnerOrganisations = newPartnerOrganisationResource().withProject(123L).build(3);;

    @Before
    public void lookupPermissionRules() {
        partnerOrganisationPermissionRules = getMockPermissionRulesBean(PartnerOrganisationPermissionRules.class);
    }

    @Test
    public void testGetProjectPartnerOrganisationsIsNotOpenToAll(){
        assertPostFilter(classUnderTest.getProjectPartnerOrganisations(123L).getSuccessObject(), () -> {
            verify(partnerOrganisationPermissionRules, times(3)).partnersOnProjectCanView(isA(PartnerOrganisationResource.class), isA(UserResource.class));
            verify(partnerOrganisationPermissionRules, times(3)).internalUsersCanViewProjects(isA(PartnerOrganisationResource.class), isA(UserResource.class));
            verifyNoMoreInteractions(partnerOrganisationPermissionRules);
        });
    }

    @Test
    public void testCompAdminCanSeeAllPartnerOrganisationsForAnyProject(){
        setLoggedInUser(newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(COMP_ADMIN).build())).build());
        ServiceResult<List<PartnerOrganisationResource>> result = classUnderTest.getProjectPartnerOrganisations(123L);
        assertTrue(result.isSuccess());
        //assertEquals(3, result.getSuccessObject().size());
    }

    @Override
    protected Class<TestPartnerOrganisationService> getClassUnderTest() {
        return TestPartnerOrganisationService.class;
    }

    static class TestPartnerOrganisationService implements PartnerOrganisationService {
        @Override
        public ServiceResult<List<PartnerOrganisationResource>> getProjectPartnerOrganisations(Long projectId) {
            return serviceSuccess(partnerOrganisations);
        }
    }
}

