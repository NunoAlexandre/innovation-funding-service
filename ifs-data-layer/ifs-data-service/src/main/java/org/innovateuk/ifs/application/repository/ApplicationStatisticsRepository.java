package org.innovateuk.ifs.application.repository;

import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.resource.AssessorCountSummaryResource;
import org.innovateuk.ifs.workflow.resource.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ApplicationStatisticsRepository extends PagingAndSortingRepository<ApplicationStatistics, Long> {

    String APPLICATION_FILTER = "SELECT a FROM ApplicationStatistics a WHERE a.competition = :compId " +
            "AND (a.applicationProcess.activityState.state IN :states) " +
            "AND (str(a.id) LIKE CONCAT('%', :filter, '%'))";

    List<ApplicationStatistics> findByCompetitionAndApplicationProcessActivityStateStateIn(long competitionId, Collection<State> applicationStates);

    @Query(APPLICATION_FILTER)
    Page<ApplicationStatistics> findByCompetitionAndApplicationProcessActivityStateStateIn(@Param("compId") long competitionId,
                                                                                           @Param("states") Collection<State> applicationStates,
                                                                                           @Param("filter") String filter,
                                                                                           Pageable pageable);

    @Query("SELECT NEW org.innovateuk.ifs.application.resource.AssessorCountSummaryResource(" +
            "  user.id, " +
            "  concat(user.firstName, ' ', user.lastName), " +
            "  profile.skillsAreas, " +
            "  sum(case when activityState.state NOT IN :rejectedAndSubmittedStates THEN 1 ELSE 0 END), " + // total assigned
            "  sum(case when competitionParticipant.competition.id = :competitionId AND activityState.state NOT IN :rejectedAndSubmittedStates   THEN 1 ELSE 0 END), " + // assigned
            "  sum(case when competitionParticipant.competition.id = :competitionId AND activityState.state NOT IN :notAcceptedOrSubmittedStates THEN 1 ELSE 0 END), " + // accepted
            "  sum(case when competitionParticipant.competition.id = :competitionId AND activityState.state     IN :submittedStates              THEN 1 ELSE 0 END)  " + // submitted
            ") " +
            "FROM User user " +
            "JOIN CompetitionParticipant competitionParticipant ON competitionParticipant.user = user " +
            "JOIN Profile profile ON profile.id = user.profileId " +
            "LEFT JOIN Application application ON application.competition = competitionParticipant.competition " +
            "LEFT JOIN ProcessRole processRole ON processRole.user = user AND processRole.applicationId = application.id " +
            "LEFT JOIN Assessment assessment ON assessment.participant = processRole.id AND assessment.target = application " +
            "LEFT JOIN ActivityState activityState ON assessment.activityState = activityState.id " +
            "WHERE " +
            "  competitionParticipant.status = org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED AND " +
            "  competitionParticipant.role = 'ASSESSOR' " +
            "GROUP BY user " +
            "HAVING sum(case when competitionParticipant.competition.id = :competitionId THEN 1 ELSE 0 END) > 0")
    Page<AssessorCountSummaryResource> getAssessorCountSummaryByCompetition(@Param("competitionId") long competitionId,
                                                                            @Param("rejectedAndSubmittedStates") Set<State> rejectedAndSubmittedStates,
                                                                            @Param("notAcceptedOrSubmittedStates") Set<State> notAcceptedOrSubmittedStates,
                                                                            @Param("submittedStates") Set<State> submittedStates,
                                                                            Pageable pageable);
}
