package org.innovateuk.ifs.application.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.innovateuk.ifs.application.domain.Application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ApplicationRepository extends PagingAndSortingRepository<Application, Long> {
    List<Application> findByName(@Param("name") String name);

	static final String COMP_FILTER = "SELECT a FROM Application a WHERE " +
			"a.competition.id = :compId" +
			" AND (str(a.id) LIKE CONCAT('%', :filter, '%'))";

	static final String COMP_STATUS_FILTER = "SELECT a FROM Application a WHERE " +
			"a.competition.id = :compId " +
			"AND (a.applicationStatus.id IN :statuses) " +
			"AND (str(a.id) LIKE CONCAT('%', :filter, '%'))";

    @Override
    List<Application> findAll();
    
    Page<Application> findByCompetitionId(Long competitionId, Pageable pageable);

    @Query(COMP_FILTER)
    Page<Application> findByCompetitionIdAndIdLike(@Param("compId") Long competitionId, @Param("filter") String filter, Pageable pageable);

    List<Application> findByCompetitionId(Long competitionId);

    @Query(COMP_FILTER)
    List<Application> findByCompetitionIdAndIdLike(@Param("compId") Long competitionId, @Param("filter") String filter);
	
	Page<Application> findByCompetitionIdAndApplicationStatusIdIn(Long competitionId, Collection<Long> applicationStatusIds, Pageable pageable);

	@Query(COMP_STATUS_FILTER)
	Page<Application> findByCompetitionIdAndApplicationStatusIdInAndIdLike(@Param("compId") Long competitionId,
																		   @Param("statuses") Collection<Long> applicationStatusIds,
																		   @Param("filter") String filter,
																		   Pageable pageable);

	@Query(COMP_STATUS_FILTER)
	List<Application> findByCompetitionIdAndApplicationStatusIdInAndIdLike(@Param("compId") Long competitionId,
																		   @Param("statuses") Collection<Long> applicationStatusIds,
																		   @Param("filter") String filter);

	Page<Application> findByCompetitionIdAndApplicationStatusIdNotIn(Long competitionId, Collection<Long> applicationStatusIds, Pageable pageable);

	List<Application> findByCompetitionIdAndApplicationStatusId(Long competitionId, Long applicationStatusId);

	Page<Application> findByCompetitionIdAndApplicationStatusId(Long competitionId, Long applicationStatusId, Pageable pageable);

	List<Application> findByCompetitionIdAndApplicationStatusIdIn(Long competitionId, Collection<Long> applicationStatusIds);

	List<Application> findByCompetitionIdAndApplicationStatusIdNotIn(Long competitionId, Collection<Long> applicationStatusIds);

	Page<Application> findByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(Long competitionId, Collection<Long> applicationStatusIds, Pageable pageable);

	Page<Application> findByCompetitionIdAndFundingDecisionIsNotNull(Long competitionId, Pageable pageable);

	List<Application> findByCompetitionIdAndFundingDecisionIsNotNull(Long competitionId);
	
	List<Application> findByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(Long competitionId, Collection<Long> applicationStatusIds);

    int countByCompetitionId(Long competitionId);

	int countByCompetitionIdAndApplicationStatusId(Long competitionId, Long applicationStatusId);

	int countByCompetitionIdAndApplicationStatusIdIn(Long competitionId, Collection<Long> submittedStatusIds);

	int countByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(Long competitionId, Collection<Long> applicationStatusIds);

	int countByCompetitionIdAndApplicationStatusIdNotInAndCompletionGreaterThan(Long competitionId, Collection<Long> submittedStatusIds, BigDecimal limit);

	int countByCompetitionIdAndApplicationStatusIdInAndCompletionLessThanEqual(Long competitionId, Collection<Long> submittedStatusIds, BigDecimal limit);

	int countByProcessRolesUserIdAndCompetitionId(Long userId, Long competitionId);

}
