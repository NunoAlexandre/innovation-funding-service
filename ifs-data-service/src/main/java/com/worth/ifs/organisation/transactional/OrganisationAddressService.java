package com.worth.ifs.organisation.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import com.worth.ifs.security.NotSecured;

public interface OrganisationAddressService {

    @NotSecured("TODO")
    ServiceResult<OrganisationAddressResource> findOne(Long id);
}