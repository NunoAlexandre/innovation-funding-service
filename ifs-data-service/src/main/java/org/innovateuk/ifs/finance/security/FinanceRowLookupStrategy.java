package org.innovateuk.ifs.finance.security;

import org.innovateuk.ifs.commons.security.PermissionEntityLookupStrategies;
import org.innovateuk.ifs.commons.security.PermissionEntityLookupStrategy;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRepository;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRowRepository;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.organisation.mapper.OrganisationMapper;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Lookup strategies for {@link FinanceRow} and {@link FinanceRowItem} for permissioning
 */
@Component
@PermissionEntityLookupStrategies
public class FinanceRowLookupStrategy {
    @Autowired
    private ApplicationFinanceRepository applicationFinanceRepository;

    @Autowired
    private ApplicationFinanceRowRepository financeRowRepository;

    @PermissionEntityLookupStrategy
    public FinanceRow getFinanceRow(final Long costId) {
        return financeRowRepository.findOne(costId);
    }

}
