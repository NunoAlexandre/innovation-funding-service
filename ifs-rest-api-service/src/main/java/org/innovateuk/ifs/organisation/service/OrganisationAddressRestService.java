package org.innovateuk.ifs.organisation.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;

public interface OrganisationAddressRestService {

    RestResult<OrganisationAddressResource> findOne(Long id);
}
