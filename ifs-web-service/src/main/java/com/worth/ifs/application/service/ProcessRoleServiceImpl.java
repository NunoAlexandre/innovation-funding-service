package com.worth.ifs.application.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

import static com.worth.ifs.application.service.ListenableFutures.adapt;
import static java.util.Arrays.asList;

/**
 * This class contains methods to retrieve and store {@link ProcessRole} related data,
 * through the RestService {@link UserRestService}.
 */
// TODO DW - INFUND-1555 - return RestResults from this Service
@Service
public class ProcessRoleServiceImpl implements ProcessRoleService {
    @Autowired
    UserRestService userRestService;

    @Override
    public ProcessRole findProcessRole(Long userId, Long applicationId) {
        return userRestService.findProcessRole(userId, applicationId).getSuccessObjectOrNull();
    }

    @Override
    public List<ProcessRole> findProcessRolesByApplicationId(Long applicationId) {
        return userRestService.findProcessRole(applicationId).getSuccessObjectOrNull();
    }

    @Override
    public ListenableFuture<List<ProcessRole>> findAssignableProcessRoles(Long applicationId) {
        return adapt(userRestService.findAssignableProcessRoles(applicationId), re -> asList(re.getSuccessObject()));
    }

    @Override
    public ListenableFuture<ProcessRole> getById(Long id){
        return adapt(userRestService.findProcessRoleById(id), RestResult::getSuccessObjectOrNull);
    }
}
