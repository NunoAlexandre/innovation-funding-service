package org.innovateuk.ifs.project.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.spendprofile.resource.SpendProfileResource;
import org.innovateuk.ifs.project.status.resource.ProjectStatusResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;

import java.util.List;
import java.util.Optional;

public interface ProjectRestService {
    RestResult<ProjectResource> getProjectById(Long projectId);

    RestResult<SpendProfileResource> getSpendProfile(Long projectId, Long organisationId);

    RestResult<List<ProjectResource>> findByUserId(long userId);

    RestResult<List<ProjectUserResource>> getProjectUsersForProject(Long projectId);

    RestResult<ProjectResource> getByApplicationId(Long applicationId);

    RestResult<OrganisationResource> getOrganisationByProjectAndUser(Long projectId, Long userId);

    RestResult<Void> addPartner(Long projectId, Long userId, Long organisationId);

    RestResult<ProjectTeamStatusResource> getProjectTeamStatus(Long projectId, Optional<Long> filterByUserId);

    RestResult<ProjectStatusResource> getProjectStatus(Long projectId);

    RestResult<ProjectUserResource> getProjectManager(Long projectId);

}
