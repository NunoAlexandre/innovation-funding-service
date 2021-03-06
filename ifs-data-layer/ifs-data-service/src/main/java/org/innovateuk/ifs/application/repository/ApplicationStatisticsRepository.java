package org.innovateuk.ifs.application.repository;

import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.resource.AssessorCountSummaryResource;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.innovateuk.ifs.workflow.resource.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ApplicationStatisticsRepository extends PagingAndSortingRepository<ApplicationStatistics, Long> {

    Sort SORT_BY_FIRSTNAME = new Sort("firstName");

    String SUBMITTED_STATES_STRING = "(org.innovateuk.ifs.workflow.resource.State.SUBMITTED)";

    String APPLICATION_FILTER = "SELECT a FROM ApplicationStatistics a WHERE a.competition = :compId " +
            "AND (a.applicationProcess.activityState.state IN :states) " +
            "AND (str(a.id) LIKE CONCAT('%', :filter, '%'))";

    String INNOVATION_AREA_FILTER = "SELECT a FROM ApplicationStatistics a " +
            "LEFT JOIN ApplicationInnovationAreaLink innovationArea ON innovationArea.application.id = a.id " +
            "AND (innovationArea.className = 'org.innovateuk.ifs.application.domain.Application#innovationArea') " +
            "WHERE a.competition = :compId " +
            "AND (a.applicationProcess.activityState.state IN :states) " +
            "AND (innovationArea.category.id = :innovationArea OR :innovationArea IS NULL) " +
            "AND NOT EXISTS (SELECT 'found' FROM Assessment b WHERE b.participant.user.id = :assessorId AND b.target.id = a.id) " +
            "AND (str(a.id) LIKE CONCAT('%', :filter, '%'))";

    String REJECTED_AND_SUBMITTED_STATES_STRING =
            "(org.innovateuk.ifs.workflow.resource.State.REJECTED," +
                    "org.innovateuk.ifs.workflow.resource.State.WITHDRAWN," +
                    "org.innovateuk.ifs.workflow.resource.State.SUBMITTED)";

    String NOT_ACCEPTED_OR_SUBMITTED_STATES_STRING =
            "(org.innovateuk.ifs.workflow.resource.State.PENDING,org.innovateuk.ifs.workflow.resource.State.REJECTED," +
                    "org.innovateuk.ifs.workflow.resource.State.WITHDRAWN,org.innovateuk.ifs.workflow.resource.State.CREATED,org.innovateuk.ifs.workflow.resource.State.SUBMITTED)";


    List<ApplicationStatistics> findByCompetitionAndApplicationProcessActivityStateStateIn(long competitionId, Collection<State> applicationStates);

    @Query(APPLICATION_FILTER)
    Page<ApplicationStatistics> findByCompetitionAndApplicationProcessActivityStateStateIn(@Param("compId") long competitionId,
                                                                                           @Param("states") Collection<State> applicationStates,
                                                                                           @Param("filter") String filter,
                                                                                           Pageable pageable);

    @Query(INNOVATION_AREA_FILTER)
    Page<ApplicationStatistics> findByCompetitionAndInnovationAreaProcessActivityStateStateIn(@Param("compId") long competitionId,
                                                                                           @Param("assessorId") long assessorId,
                                                                                           @Param("states") Collection<State> applicationStates,
                                                                                           @Param("filter") String filter,
                                                                                           @Param("innovationArea") Long innovationArea,
                                                                                           Pageable pageable);
    @Query("SELECT NEW org.innovateuk.ifs.application.resource.AssessorCountSummaryResource(" +
            "  user.id, " +
            "  concat(user.firstName, ' ', user.lastName), " +
            "  profile.skillsAreas, " +
            "  sum(case when activityState.state NOT IN " + REJECTED_AND_SUBMITTED_STATES_STRING + " THEN 1 ELSE 0 END), " + // total assigned
            "  sum(case when competitionParticipant.competition.id = :compId AND activityState.state NOT IN " + REJECTED_AND_SUBMITTED_STATES_STRING + " THEN 1 ELSE 0 END), " + // assigned
            "  sum(case when competitionParticipant.competition.id = :compId AND activityState.state NOT IN " + NOT_ACCEPTED_OR_SUBMITTED_STATES_STRING + " THEN 1 ELSE 0 END), " + // accepted
            "  sum(case when competitionParticipant.competition.id = :compId AND activityState.state     IN " + SUBMITTED_STATES_STRING    + " THEN 1 ELSE 0 END)  " +  // submitted
            ") " +
            "FROM User user " +
            "JOIN CompetitionParticipant competitionParticipant ON competitionParticipant.user.id = user.id " +
            "JOIN Profile profile ON profile.id = user.profileId " +
            // join on all applications for each invited assessor on the system
            "LEFT JOIN ProcessRole processRole ON processRole.user.id = user.id " +
            "LEFT JOIN Assessment assessment ON assessment.participant = processRole.id " +
            "LEFT JOIN Application application ON assessment.target.id = application.id AND application.competition.id = competitionParticipant.competition.id  " +
            "LEFT JOIN ActivityState activityState ON application.id IS NOT NULL AND assessment.activityState.id = activityState.id " +
            "WHERE " +
            "  competitionParticipant.status = org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED AND " +
            "  competitionParticipant.role = 'ASSESSOR' AND " +
            " (:innovationSectorId IS NULL OR :innovationSectorId IN (SELECT innovationAreaLink.category.sector.id " +
            "                                             FROM ProfileInnovationAreaLink innovationAreaLink" +
            "                                             WHERE innovationAreaLink.profile = profile)) AND " +
            "  (:businessType IS NULL OR profile.businessType = :businessType) " +
            "GROUP BY user " +
            "HAVING sum(case when competitionParticipant.competition.id = :compId THEN 1 ELSE 0 END) > 0")
    Page<AssessorCountSummaryResource> getAssessorCountSummaryByCompetition(@Param("compId") long competitionId,
                                                                            @Param("innovationSectorId") Optional<Long> innovationSectorId,
                                                                            @Param("businessType") Optional<BusinessType> businessType,
                                                                            Pageable pageable);
}