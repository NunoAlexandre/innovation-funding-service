package com.worth.ifs.project.finance.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.project.finance.resource.FinanceCheckResource;
import com.worth.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.springframework.stereotype.Service;

import static com.worth.ifs.project.controller.FinanceCheckController.*;

/**
 * Rest Service for dealing with Project finance operations
 */
@Service
public class FinanceCheckRestServiceImpl extends BaseRestService implements FinanceCheckRestService {

    @Override
    public RestResult<FinanceCheckResource> getByProjectAndOrganisation(Long projectId, Long organisationId) {
        String url = FINANCE_CHECK_BASE_URL + "/" + projectId + FINANCE_CHECK_ORGANISATION_PATH + "/" + organisationId + FINANCE_CHECK_PATH;
        return getWithRestResult(url, FinanceCheckResource.class);
    }

    @Override
    public RestResult<Void> update(FinanceCheckResource financeCheckResource) {
        String url = FINANCE_CHECK_BASE_URL + FINANCE_CHECK_PATH;
        return postWithRestResult(url, financeCheckResource, Void.class);
    }

    @Override
    public RestResult<Void> approveFinanceCheck(Long projectId, Long organisationId) {
        String url = FINANCE_CHECK_BASE_URL + "/" + projectId + FINANCE_CHECK_ORGANISATION_PATH + "/" + organisationId + FINANCE_CHECK_PATH + "/approve";
        return postWithRestResult(url, Void.class);
    }

    @Override
    public RestResult<FinanceCheckProcessResource> getFinanceCheckApprovalStatus(Long projectId, Long organisationId) {
        String url = FINANCE_CHECK_BASE_URL + "/" + projectId + FINANCE_CHECK_ORGANISATION_PATH + "/" + organisationId + FINANCE_CHECK_PATH + "/status";
        return getWithRestResult(url, FinanceCheckProcessResource.class);
    }
}
