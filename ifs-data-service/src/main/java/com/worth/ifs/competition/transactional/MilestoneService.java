package com.worth.ifs.competition.transactional;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.resource.MilestoneResource.MilestoneName;

/**
 * Service for operations around the usage and processing of Milestones
 */
public interface MilestoneService {
    @PreAuthorize("hasAuthority('comp_admin')")
    ServiceResult<List<MilestoneResource>> getAllDatesByCompetitionId(final Long id);

    @PreAuthorize("hasAuthority('comp_admin')")
    ServiceResult<Void> update(Long id, List<MilestoneResource> milestones);

    @PreAuthorize("hasAuthority('comp_admin')")
    ServiceResult<MilestoneResource> create(MilestoneName name, Long id);
}
