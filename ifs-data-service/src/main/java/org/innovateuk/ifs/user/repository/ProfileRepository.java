package org.innovateuk.ifs.user.repository;

import org.innovateuk.ifs.user.domain.Profile;
import org.springframework.data.repository.CrudRepository;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface ProfileRepository extends CrudRepository<Profile, Long> {
}
