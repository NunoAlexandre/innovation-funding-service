package com.worth.ifs.application.repository;

import com.worth.ifs.application.domain.Question;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface QuestionRepository extends PagingAndSortingRepository<Question, Long> {
    List<Question> findAll();
}