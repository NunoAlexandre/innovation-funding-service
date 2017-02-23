package org.innovateuk.ifs.user.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;

import java.util.List;

/**
 * Service that encompasses functions that relate to users and their roles
 */
public interface UsersRolesService {

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<ProcessRoleResource> getProcessRoleById(final Long id);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<List<ProcessRoleResource>> getProcessRolesByIds(final Long[] ids);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<List<ProcessRoleResource>> getProcessRolesByApplicationId(final Long applicationId);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<ProcessRoleResource> getProcessRoleByUserIdAndApplicationId(final Long userId, final Long applicationId);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<List<ProcessRoleResource>> getProcessRolesByUserId(final Long userId);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<List<ProcessRoleResource>> getAssignableProcessRolesByApplicationId(final Long applicationId);

    @NotSecured(value = "TODO DW - INFUND-7132 - add correct permissions", mustBeSecuredByOtherServices = false)
    ServiceResult<Boolean> userHasApplicationForCompetition(Long userId, Long competitionId);
}
