package org.innovateuk.ifs.project.service;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.invite.resource.InviteProjectResource;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.spendprofile.resource.SpendProfileResource;
import org.innovateuk.ifs.project.status.resource.ProjectStatusResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectResourceListType;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectUserResourceList;

@Service
public class ProjectRestServiceImpl extends BaseRestService implements ProjectRestService {

    private String projectRestURL = "/project";

    @Override
    public RestResult<ProjectResource> getProjectById(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId, ProjectResource.class);
    }

    @Override
    public RestResult<SpendProfileResource> getSpendProfile(final Long projectId, final Long organisationId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile/", SpendProfileResource.class);
    }

    @Override
    public RestResult<Void> updateProjectManager(Long projectId, Long projectManagerUserId) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/project-manager/" + projectManagerUserId, Void.class);
    }
    @Override
    public RestResult<Void> updateProjectStartDate(Long projectId, LocalDate projectStartDate) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/startdate?projectStartDate=" + projectStartDate, Void.class);
    }

    @Override
    public RestResult<Void> updateProjectAddress(long leadOrganisationId, long projectId, OrganisationAddressType addressType, AddressResource address) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/address?addressType=" + addressType.name() + "&leadOrganisationId=" + leadOrganisationId, address, Void.class);
    }

    @Override
    public RestResult<List<ProjectResource>> findByUserId(long userId) {
        return getWithRestResult(projectRestURL + "/user/" + userId, projectResourceListType());
    }

    @Override
    public RestResult<Void> updateFinanceContact(ProjectOrganisationCompositeId composite, Long financeContactUserId) {
        return postWithRestResult(projectRestURL + "/" + composite.getProjectId() + "/organisation/" + composite.getOrganisationId() + "/finance-contact?financeContact=" + financeContactUserId, Void.class);
    }

    @Override
    public RestResult<List<ProjectUserResource>> getProjectUsersForProject(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/project-users", projectUserResourceList());
    }

    @Override
    public RestResult<ProjectResource> getByApplicationId(Long applicationId) {
        return getWithRestResult(projectRestURL + "/application/" + applicationId, ProjectResource.class);
    }

    @Override
    public RestResult<Void> setApplicationDetailsSubmitted(Long projectId) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/setApplicationDetailsSubmitted", Void.class);
    }

    @Override
    public RestResult<Boolean> isSubmitAllowed(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/isSubmitAllowed", Boolean.class);
    }

    @Override
    public RestResult<OrganisationResource> getOrganisationByProjectAndUser(Long projectId, Long userId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/getOrganisationByUser/" + userId, OrganisationResource.class);
    }

    @Override
    public RestResult<Void> addPartner(Long projectId, Long userId, Long organisationId) {
        return postWithRestResultAnonymous(projectRestURL + "/" + projectId + "/partners?userId=" + userId + "&organisationId=" + organisationId, Void.class);
    }

    @Override
    public RestResult<ProjectTeamStatusResource> getProjectTeamStatus(Long projectId, Optional<Long> filterByUserId){
        return filterByUserId.
                map(userId -> getWithRestResult(projectRestURL + "/" + projectId + "/team-status?filterByUserId=" + userId, ProjectTeamStatusResource.class))
                .orElseGet(() -> getWithRestResult(projectRestURL + "/" + projectId + "/team-status", ProjectTeamStatusResource.class));
    }

    @Override
    public RestResult<ProjectStatusResource> getProjectStatus(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/status", ProjectStatusResource.class);
    }

    public RestResult<Void> inviteFinanceContact(Long projectId, InviteProjectResource inviteResource) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/invite-finance-contact", inviteResource, Void.class);
    }

    @Override public RestResult<Void> inviteProjectManager(final Long projectId, final InviteProjectResource inviteResource) {
        return postWithRestResult(projectRestURL + "/" + projectId + "/invite-project-manager", inviteResource, Void.class);
    }

    @Override
    public RestResult<ProjectUserResource> getProjectManager(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/project-manager", ProjectUserResource.class);
    }

}
