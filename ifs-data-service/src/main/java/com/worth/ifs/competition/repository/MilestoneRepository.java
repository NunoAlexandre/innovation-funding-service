package com.worth.ifs.competition.repository;


import com.worth.ifs.competition.domain.Milestone;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface MilestoneRepository extends CrudRepository<Milestone, Long> {
    <List> java.util.List<Milestone> findAllByCompetitionId(Long id);
}
