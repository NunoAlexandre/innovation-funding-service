package org.innovateuk.ifs.finance.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;

import java.util.List;

/**
 * Interface for CRUD operations on {@link FinanceRowItem} related data.
 */
public interface ProjectFinanceRowRestService {
    RestResult<ValidationMessages> add(Long projectFinanceId, Long questionId, FinanceRowItem costItem);
    RestResult<FinanceRowItem> addWithoutPersisting(Long projectFinanceId, Long questionId);
    RestResult<List<FinanceRowItem>> getCosts(Long projectFinanceId);
}
