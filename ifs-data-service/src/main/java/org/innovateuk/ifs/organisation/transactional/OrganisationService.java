package org.innovateuk.ifs.organisation.transactional;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Set;

/**
 * Represents operations surrounding the use of Organisations in the system
 */
public interface OrganisationService {

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<Set<OrganisationResource>> findByApplicationId(Long applicationId);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<OrganisationResource> findById(Long organisationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<OrganisationResource> findByUserId(Long userId);

    @PreAuthorize("hasPermission(#organisation, 'CREATE')")
    ServiceResult<OrganisationResource> create(@P("organisation") OrganisationResource organisation);

    @PreAuthorize("hasPermission(#organisation, 'UPDATE')")
    ServiceResult<OrganisationResource> update(@P("organisation") OrganisationResource organisationResource);

    @PreAuthorize("hasPermission(#organisationId, 'org.innovateuk.ifs.user.resource.OrganisationResource', 'UPDATE')")
    public ServiceResult<OrganisationResource> updateOrganisationNameAndRegistration(final Long organisationId, final String organisationName, final String registrationNumber);

    @PreAuthorize("hasPermission(#organisationId, 'org.innovateuk.ifs.user.resource.OrganisationResource', 'UPDATE')")
    ServiceResult<OrganisationResource> addAddress(@P("organisationId") Long organisationId, OrganisationAddressType addressType, AddressResource addressResource);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<OrganisationSearchResult>> searchAcademic(String organisationName, int maxItems);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<OrganisationSearchResult> getSearchOrganisation(@P("organisationId") Long searchOrganisationId);
}
