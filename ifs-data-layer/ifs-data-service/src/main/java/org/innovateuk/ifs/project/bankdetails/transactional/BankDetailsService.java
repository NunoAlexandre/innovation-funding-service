package org.innovateuk.ifs.project.bankdetails.transactional;

import org.innovateuk.ifs.project.bankdetails.resource.BankDetailsResource;
import org.innovateuk.ifs.project.bankdetails.resource.ProjectBankDetailsStatusSummary;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface BankDetailsService {
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<BankDetailsResource> getById(final Long bankDetailsId);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<BankDetailsResource> getByProjectAndOrganisation(final Long projectId, final Long organisationId);

    @PreAuthorize("hasPermission(#bankDetailsResource, 'SUBMIT')")
    ServiceResult<Void> submitBankDetails(@P("bankDetailsResource") final BankDetailsResource bankDetailsResource);

    @PreAuthorize("hasPermission(#bankDetailsResource, 'UPDATE')")
    ServiceResult<Void> updateBankDetails(BankDetailsResource bankDetailsResource);

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "READ", description = "Project Finance users can see bank details status summary for all partners", securedType = ProjectBankDetailsStatusSummary.class)
    ServiceResult<ProjectBankDetailsStatusSummary> getProjectBankDetailsStatusSummary(final Long projectId);
}
