package org.innovateuk.ifs.user.repository;

import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserStatus;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
        Optional<User> findByEmail(@Param("email") String email);

        Optional<User> findByEmailAndStatus(@Param("email") String email, @Param("status") final UserStatus status);

        @Override
        List<User> findAll();
        List<User> findByRoles_Name(@Param("name") String name);
        User findOneByUid(@Param("uid") String uid);
}
