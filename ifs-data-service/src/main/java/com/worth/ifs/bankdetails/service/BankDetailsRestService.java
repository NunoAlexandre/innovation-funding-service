package com.worth.ifs.bankdetails.service;

import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.commons.rest.RestResult;

public interface BankDetailsRestService {
    RestResult<BankDetailsResource> getById(final Long projectId, final Long bankDetailsId);
    RestResult<Void> updateBankDetails(final Long projectId, final BankDetailsResource bankDetailsResource);
    RestResult<BankDetailsResource> getBankDetailsByProjectAndOrganisation(final Long projectId, final Long organisationId);
}
