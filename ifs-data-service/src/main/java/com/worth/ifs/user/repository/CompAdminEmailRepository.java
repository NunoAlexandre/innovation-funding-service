package com.worth.ifs.user.repository;

import com.worth.ifs.user.domain.CompAdminEmail;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CompAdminEmailRepository extends PagingAndSortingRepository<CompAdminEmail, Long> {
    CompAdminEmail findOneByEmail(@Param("email") String email);
}
