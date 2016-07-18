package com.worth.ifs.competition.repository;

import com.worth.ifs.competition.domain.CompetitionCoFunder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface CompetitionCoFunderRepository extends CrudRepository<CompetitionCoFunder, Long> {

    List<CompetitionCoFunder> findByCompetitionId(Long competitionId);

}
