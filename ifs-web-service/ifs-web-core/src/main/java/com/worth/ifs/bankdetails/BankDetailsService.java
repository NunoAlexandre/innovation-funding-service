package com.worth.ifs.bankdetails;

import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.commons.service.ServiceResult;

/**
 * A service for dealing with project bank details via the appropriate Rest services
 */
public interface BankDetailsService {
    BankDetailsResource getById(final Long projectId, final Long bankDetailsId);
    ServiceResult<Void> updateBankDetails(final Long projectId, final BankDetailsResource bankDetailsResource);
    BankDetailsResource getBankDetailsByProjectAndOrganisation(final Long projectId, final Long organisationId);
}
