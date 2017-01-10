package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Service for retrieving statistics about applications
 */
public interface ApplicationCountSummaryService {

    @PreAuthorize("hasAuthority('comp_admin')")
    @SecuredBySpring(value = "READ", description = "Comp Admins can see all Application Summary counts accros the whole system", securedType = ApplicationCountSummaryResource.class)
    ServiceResult<List<ApplicationCountSummaryResource>> getApplicationCountSummariesByCompetitionId(Long competitionId);
}

