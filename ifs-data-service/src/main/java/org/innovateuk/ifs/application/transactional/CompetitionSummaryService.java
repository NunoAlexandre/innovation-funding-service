package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.springframework.security.access.prepost.PreAuthorize;

import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;

public interface CompetitionSummaryService {
	
	@PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
	@SecuredBySpring(value = "VIEW", securedType = CompetitionSummaryResource.class,
			description = "Comp Admins and Project Finance team members can see Competition Summaries")
	ServiceResult<CompetitionSummaryResource> getCompetitionSummaryByCompetitionId(Long competitionId);
}
