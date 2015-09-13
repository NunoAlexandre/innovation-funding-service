package com.worth.ifs.finance.service;

import com.worth.ifs.finance.domain.ApplicationFinance;
import java.util.List;

public interface ApplicationFinanceRestService {
    public ApplicationFinance getApplicationFinance(Long applicationId, Long organisationId);
    public List<ApplicationFinance> getApplicationFinances(Long applicationId);
    public ApplicationFinance addApplicationFinanceForOrganisation(Long applicationId, Long organisationId);
}
