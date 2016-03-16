package com.worth.ifs.application.repository;

import com.worth.ifs.application.domain.ApplicationStatus;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ApplicationStatusRepository extends PagingAndSortingRepository<ApplicationStatus, Long> {
    List<ApplicationStatus> findByName(@Param("name") String name);
    @Override
    List<ApplicationStatus> findAll();
}
