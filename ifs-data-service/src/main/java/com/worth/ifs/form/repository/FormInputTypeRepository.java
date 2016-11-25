package com.worth.ifs.form.repository;

import com.worth.ifs.form.domain.FormInputType;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by worthsystems on 07/01/16.
 */
public interface FormInputTypeRepository extends PagingAndSortingRepository<FormInputType, Long> {

    FormInputType findByTitle(@Param("title") String title);

}