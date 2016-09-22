package com.worth.ifs.assessment.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.invite.resource.CompetitionInviteResource;
import com.worth.ifs.registration.resource.UserRegistrationResource;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.mapper.RoleMapper;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.resource.RoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.transactional.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.user.resource.UserRoleType.ASSESSOR;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.Collections.singletonList;

@Service
public class AssessorServiceImpl implements AssessorService {
    @Autowired
    CompetitionInviteService competitionInviteService;

    @Autowired
    RegistrationService userRegistrationService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleMapper roleMapper;

    @Override
    public ServiceResult<Void> registerAssessorByHash(String inviteHash, UserRegistrationResource userRegistrationResource) {
        UserResource userResource = new UserResource();
        userResource.setTitle(userRegistrationResource.getTitle());
        userResource.setFirstName(userRegistrationResource.getFirstName());
        userResource.setLastName(userRegistrationResource.getLastName());
        userResource.setPhoneNumber(userRegistrationResource.getPhoneNumber());
        userResource.setGender(userRegistrationResource.getGender());
        userResource.setDisability(userRegistrationResource.getDisability());
        userResource.setEthnicity(userRegistrationResource.getEthnicity().getId());
        userResource.setPassword(userRegistrationResource.getPassword());

        // TODO: Handle failures gracefully and hand them back to the webservice
        // TODO: Retrieve and add assessor role through RoleService before account creation
        return retrieveInvite(inviteHash).andOnSuccess(inviteResource -> {
            userResource.setEmail(inviteResource.getEmail());
            return getAssessorRoleResource().andOnSuccess(assessorRole -> {
                userResource.setRoles(singletonList(assessorRole));
                return createUser(userResource)
                        .andOnSuccess(() -> acceptInvite(inviteHash))
                        .andOnSuccessReturnVoid();
            });
        });
    }

    private ServiceResult<CompetitionInviteResource> retrieveInvite(String inviteHash) {
        return competitionInviteService.getInvite(inviteHash);
    }

    private ServiceResult<RoleResource> getAssessorRoleResource() {
        return find(roleRepository.findOneByName(ASSESSOR.name()), notFoundError(Role.class, ASSESSOR.name())).andOnSuccessReturn(roleMapper::mapToResource);
    }

    private ServiceResult<Void> createUser(UserResource userResource) {
        return userRegistrationService.createUser(userResource).andOnSuccess(created -> userRegistrationService.activateUser(created.getId()));
    }

    private ServiceResult<Void> acceptInvite(String inviteHash) {
        return competitionInviteService.acceptInvite(inviteHash);
    }
}
