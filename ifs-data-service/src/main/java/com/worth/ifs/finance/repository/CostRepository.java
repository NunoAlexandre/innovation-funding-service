package com.worth.ifs.finance.repository;

import com.worth.ifs.finance.domain.Cost;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface CostRepository extends PagingAndSortingRepository<Cost, Long> {
    public List<Cost> findByApplicationFinanceId(@Param("applicationFinanceId") Long applicationFinanceId);
    public Cost findOneByApplicationFinanceIdAndNameAndQuestionId(Long applicationFinanceId, String name, Long questionId);
}
