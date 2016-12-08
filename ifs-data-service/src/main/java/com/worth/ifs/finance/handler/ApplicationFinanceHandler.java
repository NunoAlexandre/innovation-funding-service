package com.worth.ifs.finance.handler;

import com.worth.ifs.commons.security.SecuredBySpring;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResourceId;
import com.worth.ifs.finance.resource.ProjectFinanceResource;
import com.worth.ifs.finance.resource.ProjectFinanceResourceId;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;

public interface ApplicationFinanceHandler {

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ApplicationFinanceResource getApplicationOrganisationFinances(final ApplicationFinanceResourceId applicationFinanceResourceId);

    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'READ_FINANCE_TOTALS')")
    List<ApplicationFinanceResource> getApplicationTotals(@P("applicationId")final Long applicationId);

    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'READ_RESEARCH_PARTICIPATION_PERCENTAGE')")
    BigDecimal getResearchParticipationPercentage(@P("applicationId")final Long applicationId);

    @PreAuthorize("hasAnyAuthority('comp_admin','project_finance')")
    @SecuredBySpring(value = "READ", description = "Internal users can view the Project Finances during the Finance Checks process")
    ProjectFinanceResource getProjectOrganisationFinances(ProjectFinanceResourceId projectFinanceResourceId);
}
