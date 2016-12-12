package org.innovateuk.ifs.project.users;

import org.innovateuk.ifs.project.domain.ProjectUser;
import org.innovateuk.ifs.project.mapper.ProjectUserMapper;
import org.innovateuk.ifs.project.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * A helper component that can be wired into any service or controllers in data layers that requires getting partner organistions
 * Please keep extending this with any other useful methods so we avoid reinventing.
 */
@Component
public class ProjectUsersHelper {
    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private OrganisationRepository organisationRepository;

    public List<Organisation> getPartnerOrganisations(Long projectId) {
        List<ProjectUser> projectUserObjs = getProjectUsersByProjectId(projectId);
        List<ProjectUserResource> projectRoles = simpleMap(projectUserObjs, projectUserMapper::mapToResource);
        return getPartnerOrganisations(projectRoles);
    }

    public Optional<ProjectUser> getFinanceContact(Long projectId, Long organisationId) {
        return simpleFindFirst(getProjectUsersByProjectId(projectId), pr -> pr.isFinanceContact() && pr.getOrganisation().getId().equals(organisationId));
    }

    private List<ProjectUser> getProjectUsersByProjectId(Long projectId) {
        return projectUserRepository.findByProjectId(projectId);
    }

    private List<Organisation> getPartnerOrganisations(List<ProjectUserResource> projectRoles) {
        final Comparator<Organisation> compareById =
                Comparator.comparingLong(Organisation::getId);

        final Supplier<SortedSet<Organisation>> supplier = () -> new TreeSet<>(compareById);

        SortedSet<Organisation> organisationSet = projectRoles.stream()
                .filter(uar -> uar.getRoleName().equals(PROJECT_PARTNER.getName()))
                .map(uar -> organisationRepository.findOne(uar.getOrganisation()))
                .collect(Collectors.toCollection(supplier));

        return new ArrayList<>(organisationSet);
    }
}
