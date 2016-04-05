package com.worth.ifs.organisation.security;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.address.domain.AddressType;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import com.worth.ifs.organisation.transactional.OrganisationService;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.resource.OrganisationResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.util.CollectionFunctions.asLinkedSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing the permission rules applied to the secured methods in OrganisationService.  This set of tests tests for the
 * individual rules that are called whenever an OrganisationService method is called.  They do not however test the logic
 * within those rules
 */
public class OrganisationServiceSecurityTest extends BaseServiceSecurityTest<OrganisationService> {

    private OrganisationPermissionRules organisationRules;

    @Before
    public void lookupPermissionRules() {

        organisationRules = getMockPermissionRulesBean(OrganisationPermissionRules.class);

        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected Class<? extends OrganisationService> getServiceClass() {
        return TestOrganisationService.class;
    }

    @Test
    public void testFindByApplicationId() {

        when(organisationRules.memberOfOrganisationCanViewOwnOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()))).thenReturn(false);
        when(organisationRules.usersCanViewOrganisationsOnTheirOwnApplications(isA(OrganisationResource.class), eq(getLoggedInUser()))).thenReturn(false);

        service.findByApplicationId(1L);

        verify(organisationRules).memberOfOrganisationCanViewOwnOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(organisationRules).usersCanViewOrganisationsOnTheirOwnApplications(isA(OrganisationResource.class), eq(getLoggedInUser()));
    }

    /**
     * Dummy implementation (for satisfying Spring Security's need to read parameter information from
     * methods, which is lost when using mocks)
     */
    private static class TestOrganisationService implements OrganisationService {

        @Override
        public ServiceResult<Set<OrganisationResource>> findByApplicationId(Long applicationId) {
            return serviceSuccess(asLinkedSet(newOrganisationResource().build()));
        }

        @Override
        public ServiceResult<OrganisationResource> findById(Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<OrganisationResource> create(OrganisationResource organisation) {
            return null;
        }

        @Override
        public ServiceResult<OrganisationResource> update(OrganisationResource organisationResource) {
            return null;
        }

        @Override
        public ServiceResult<OrganisationResource> addAddress(Long organisationId, AddressType addressType, AddressResource addressResource) {
            return null;
        }

        @Override
        public ServiceResult<List<OrganisationSearchResult>> searchAcademic(String organisationName, int maxItems) {
            return null;
        }

        @Override
        public ServiceResult<OrganisationSearchResult> getSearchOrganisation(Long searchOrganisationId) {
            return null;
        }
    }
}
