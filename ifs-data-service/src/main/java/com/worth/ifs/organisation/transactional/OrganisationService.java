package com.worth.ifs.organisation.transactional;

import com.worth.ifs.address.domain.AddressType;
import com.worth.ifs.address.resource.AddressResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.resource.OrganisationSearchResult;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.resource.OrganisationResource;
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

    @PreAuthorize("hasPermission(#organisation, 'CREATE')")
    ServiceResult<OrganisationResource> create(Organisation organisation);

    @PreAuthorize("hasPermission(#organisationResource, 'UPDATE')")
    ServiceResult<OrganisationResource> saveResource(OrganisationResource organisationResource);

    @PreAuthorize("hasPermission(#organisationId, 'com.worth.ifs.user.resource.OrganisationResource', 'UPDATE')")
    ServiceResult<OrganisationResource> addAddress(Long organisationId, AddressType addressType, AddressResource addressResource);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<OrganisationSearchResult>> searchAcademic(String organisationName, int maxItems);

    @PreAuthorize("hasPermission(#organisationId, 'com.worth.ifs.user.resource.OrganisationResource', 'READ')")
    ServiceResult<OrganisationSearchResult> getSearchOrganisation(Long searchOrganisationId);
}
