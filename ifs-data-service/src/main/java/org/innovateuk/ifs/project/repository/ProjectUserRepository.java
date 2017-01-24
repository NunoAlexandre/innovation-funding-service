package org.innovateuk.ifs.project.repository;

import org.innovateuk.ifs.invite.domain.ProjectParticipantRole;
import org.innovateuk.ifs.project.domain.ProjectUser;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectUserRepository extends PagingAndSortingRepository<ProjectUser, Long> {

    List<ProjectUser> findByProjectId(Long projectId);

    List<ProjectUser> findByProjectIdAndOrganisationId(long projectId, long organisationId);

    ProjectUser findOneByProjectIdAndUserIdAndOrganisationIdAndRole(long projectId, long userId, long organisationId, ProjectParticipantRole role);

    List<ProjectUser> findByProjectIdAndUserIdAndRole(long projectId, long userId, ProjectParticipantRole role);

    List<ProjectUser> findByUserId(long userId);

    List<ProjectUser> findByUserIdAndRole(long userId, ProjectParticipantRole role);

    ProjectUser findByProjectIdAndRoleAndUserId(long projectId, ProjectParticipantRole role, long userId);

    ProjectUser findByProjectIdAndRole(long projectId, ProjectParticipantRole role);
}
