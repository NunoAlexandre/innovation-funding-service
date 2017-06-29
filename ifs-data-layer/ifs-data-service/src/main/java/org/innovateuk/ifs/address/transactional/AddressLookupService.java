package org.innovateuk.ifs.address.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.commons.security.NotSecured;

import java.util.List;

/**
 * Lookup for addresses
 */
public interface AddressLookupService {
    @NotSecured(value = "Everyone may do a lookup it is used as part of the registration process", mustBeSecuredByOtherServices = false)
    ServiceResult<List<AddressResource>> doLookup(String lookup);
    @NotSecured(value = "Everyone should be able to do a postcode verification", mustBeSecuredByOtherServices = false)
    ServiceResult<Boolean> validatePostcode(String postcode);
}
