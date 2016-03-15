package com.worth.ifs.address.repository;

import com.worth.ifs.address.domain.Address;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface AddressRepository extends PagingAndSortingRepository<Address, Long> {

}
