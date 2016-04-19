package com.worth.ifs.application.security;

import com.worth.ifs.application.mapper.QuestionStatusMapper;
import com.worth.ifs.application.repository.QuestionStatusRepository;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.security.PermissionEntityLookupStrategies;
import com.worth.ifs.security.PermissionEntityLookupStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@PermissionEntityLookupStrategies
public class QuestionStatusResourceLookupStrategy {

    @Autowired
    private QuestionStatusRepository questionStatusRepository;

    @Autowired
    private QuestionStatusMapper questionStatusMapper;

    @PermissionEntityLookupStrategy
    public QuestionStatusResource findResourceById(Long id){
        return questionStatusMapper.mapIdToResource(id);
    }
}
