package org.innovateuk.ifs.competition.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Service for operations around the usage and processing of Milestones
 */
public interface MilestoneService {
    @PreAuthorize("hasAnyAuthority('system_registrar')")
    @SecuredBySpring(value="READ", securedType=MilestoneResource.class,
            description = "All users can get see the public milestones for the given competition")
    ServiceResult<List<MilestoneResource>> getAllPublicMilestonesByCompetitionId(final Long id);

    @PreAuthorize("hasAnyAuthority('system_registrar', 'comp_admin' , 'project_finance')")
    @SecuredBySpring(value="READ", securedType=MilestoneResource.class,
            description = "Only Comp Admins and project finance users can see all the milestones for the given competition")
    ServiceResult<List<MilestoneResource>> getAllMilestonesByCompetitionId(final Long id);

    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    @SecuredBySpring(value="READ", securedType=MilestoneResource.class,
            description = "Only Comp Admins and project finance users can request the milestones for the competition by its type")
    ServiceResult<MilestoneResource> getMilestoneByTypeAndCompetitionId(final MilestoneType type, final Long id);

    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    @SecuredBySpring(value="UPDATE", securedType=MilestoneResource.class,
            description = "Only Comp Admins and project finance users are able to save all the milestones for the given competitions")
    ServiceResult<Void> updateMilestones(List<MilestoneResource> milestones);

    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    @SecuredBySpring(value="UPDATE", securedType=MilestoneResource.class,
            description = "Only Comp Admins and project finance users are able to save single milestone for the given competitions")
    ServiceResult<Void> updateMilestone(MilestoneResource milestone);

    @PreAuthorize("hasAnyAuthority('comp_admin' , 'project_finance')")
    @SecuredBySpring(value="UPDATE", securedType=MilestoneResource.class,
            description = "Only Comp Admins and project finance users are able to create the milestone for the given competitions")
    ServiceResult<MilestoneResource> create(MilestoneType type, Long id);
}
