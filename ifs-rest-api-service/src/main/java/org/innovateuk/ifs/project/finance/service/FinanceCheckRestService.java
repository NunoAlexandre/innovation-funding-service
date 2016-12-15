package org.innovateuk.ifs.project.finance.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;

/**
 * Rest Service for dealing with Project finance operations
 */
public interface FinanceCheckRestService {

    RestResult<FinanceCheckResource> getByProjectAndOrganisation(Long projectId, Long organisationId);

    RestResult<Void> update(FinanceCheckResource financeCheckResource);

    RestResult<FinanceCheckSummaryResource> getFinanceCheckSummary(Long projectId);

    RestResult<Void> approveFinanceCheck(Long projectId, Long organisationId);

    RestResult<FinanceCheckProcessResource> getFinanceCheckApprovalStatus(Long projectId, Long organisationId);
}
