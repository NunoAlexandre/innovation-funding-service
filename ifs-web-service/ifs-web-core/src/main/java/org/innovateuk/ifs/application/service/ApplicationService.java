package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.user.resource.OrganisationResource;

import java.util.List;
import java.util.Map;

/**
 * Interface for CRUD operations on {@link ApplicationResource} related data.
 */
public interface ApplicationService {
    ApplicationResource getById(Long applicationId);
    List<ApplicationResource> getInProgress(Long userId);
    List<ApplicationResource> getFinished(Long userId);
    Boolean isApplicationReadyForSubmit(Long applicationId);
    void updateStatus(Long applicationId, Long statusId);
    ApplicationResource createApplication(Long competitionId, Long userId, String applicationName);
    Integer getCompleteQuestionsPercentage(Long applicationId);
    void save(ApplicationResource application);
    Map<Long, Integer> getProgress(Long userId);
    int getAssignedQuestionsCount(Long applicantId, Long processRoleId);
    RestResult<ApplicationResource> findByProcessRoleId(Long id);
    OrganisationResource getLeadOrganisation(Long applicationId);
    void removeCollaborator(Long applicationInviteId);
    RestResult<Long> getTurnover(Long applicationId);
    RestResult<Long> getHeadcount(Long applicationId);
}
