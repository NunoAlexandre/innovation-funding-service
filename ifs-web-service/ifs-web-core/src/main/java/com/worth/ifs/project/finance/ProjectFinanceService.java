package com.worth.ifs.project.finance;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.resource.SpendProfileResource;
import com.worth.ifs.project.resource.SpendProfileTableResource;

import java.util.Optional;

/**
 * A service for dealing with a Project's finance operations
 */
public interface ProjectFinanceService {

    ServiceResult<Void> generateSpendProfile(Long projectId);

    Optional<SpendProfileResource> getSpendProfile(Long projectId, Long organisationId);

    SpendProfileTableResource getSpendProfileTable(Long projectId, Long organisationId);

    ServiceResult<Void> saveSpendProfile(Long projectId, Long organisationId, SpendProfileTableResource table);

    ServiceResult<Void> markSpendProfileComplete(Long projectId, Long organisationId);
}
