package com.worth.ifs.application.security;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.security.PermissionEntityLookupStrategies;
import com.worth.ifs.security.PermissionEntityLookupStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@PermissionEntityLookupStrategies
public class ApplicationLookupStrategy {
    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ApplicationMapper applicationMapper;

    @PermissionEntityLookupStrategy
    public Application getApplication(Long applicationId) {
        return applicationRepository.findOne(applicationId);
    }

    @PermissionEntityLookupStrategy
    public ApplicationResource getApplicationResource(Long applicationId) {
        return applicationMapper.mapToResource(applicationRepository.findOne(applicationId));
    }
}
