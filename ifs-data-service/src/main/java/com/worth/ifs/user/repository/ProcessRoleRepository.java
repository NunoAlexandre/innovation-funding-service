package com.worth.ifs.user.repository;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ProcessRoleRepository extends PagingAndSortingRepository<ProcessRole, Long> {
    List<ProcessRole> findByUser(@Param("user") User user);
    List<ProcessRole> findByUserId(@Param("userId") Long userId);
    List<ProcessRole> findByUserAndApplication(@Param("user") User user, @Param("application") Application application);
    List<ProcessRole> findByApplication(@Param("application") Application application);
    List<ProcessRole> findByApplicationId(@Param("applicationId") Long applicationId);
    ProcessRole findByUserIdAndApplicationId(@Param("userId") Long userId, @Param("applicationId") Long applicationId);
}
