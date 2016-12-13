package org.innovateuk.ifs.user.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.user.resource.OrganisationResource;

import java.util.List;

/**
 * Interface for CRUD operations on {@link OrganisationResource} related data.
 */
public interface OrganisationRestService {

    RestResult<List<OrganisationResource>> getOrganisationsByApplicationId(Long applicationId);
    RestResult<OrganisationResource> getOrganisationById(Long organisationId);
    RestResult<OrganisationResource> getOrganisationByIdForAnonymousUserFlow(Long organisationId);
    RestResult<OrganisationResource> create(OrganisationResource organisation);
    RestResult<OrganisationResource> update(OrganisationResource organisation);
    RestResult<OrganisationResource> updateNameAndRegistration(OrganisationResource organisation);
    RestResult<OrganisationResource> updateByIdForAnonymousUserFlow(OrganisationResource organisation);
    RestResult<OrganisationResource> addAddress(OrganisationResource organisation, AddressResource address, OrganisationAddressType type);
}
