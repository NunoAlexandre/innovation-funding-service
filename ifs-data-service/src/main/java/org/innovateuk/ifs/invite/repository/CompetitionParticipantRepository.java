package org.innovateuk.ifs.invite.repository;

import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface CompetitionParticipantRepository extends CrudRepository<CompetitionParticipant, Long> {

    @Override
    List<CompetitionParticipant> findAll();

    CompetitionParticipant getByInviteHash(String hash);

    List<CompetitionParticipant> getByUserIdAndRole(Long userId, CompetitionParticipantRole role);

    List<CompetitionParticipant> getByCompetitionIdAndRole(Long competitionId, CompetitionParticipantRole role);

    List<CompetitionParticipant> getByCompetitionIdAndRoleAndStatus(Long competitionId, CompetitionParticipantRole role, ParticipantStatus status);

    List<CompetitionParticipant> getByInviteEmail(String email);

    long countByCompetitionIdAndRoleAndStatus(Long competitionId, CompetitionParticipantRole role, ParticipantStatus status);
}
