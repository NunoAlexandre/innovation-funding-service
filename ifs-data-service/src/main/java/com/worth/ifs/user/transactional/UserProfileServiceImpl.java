package com.worth.ifs.user.transactional;

import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Affiliation;
import com.worth.ifs.user.domain.Contract;
import com.worth.ifs.user.domain.Profile;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.mapper.AffiliationMapper;
import com.worth.ifs.user.mapper.ContractMapper;
import com.worth.ifs.user.mapper.UserMapper;
import com.worth.ifs.user.repository.ContractRepository;
import com.worth.ifs.user.resource.AffiliationResource;
import com.worth.ifs.user.resource.ProfileResource;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.worth.ifs.commons.error.CommonErrors.badRequestError;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.stream.Collectors.toList;

/**
 * A Service for operations regarding Users' profiles.  This implementation delegates some of this work to an Identity Provider Service
 */
@Service
public class UserProfileServiceImpl extends BaseTransactionalService implements UserProfileService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private AffiliationMapper affiliationMapper;


    public enum ServiceFailures {
        UNABLE_TO_UPDATE_USER
    }

    @Override
    public ServiceResult<Void> updateProfile(Long userId, ProfileResource profileResource) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> updateUserProfile(user, profileResource));
    }

    private void setUserProfileIfNoneExists(User user) {
        if (user.getProfile() == null) {
            user.setProfile(new Profile(user));
        }
    }

    private ServiceResult<Void> updateUserProfile(User user, ProfileResource profileResource) {
        setUserProfileIfNoneExists(user);

        final Profile profile = user.getProfile();

        profile.setBusinessType(profileResource.getBusinessType());
        profile.setSkillsAreas(profileResource.getSkillsAreas());

        userRepository.save(user);

        return serviceSuccess();
    }

    @Override
    public ServiceResult<Void> updateDetails(UserResource userResource) {
        if (userResource != null) {
            return userService.findByEmail(userResource.getEmail())
                    .andOnSuccess(existingUser ->
                            updateUser(existingUser, userResource));
        } else {
            return serviceFailure(badRequestError("User resource may not be null"));
        }
    }

    @Override
    public ServiceResult<List<AffiliationResource>> getUserAffiliations(Long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId)).andOnSuccessReturn(user -> user.getAffiliations().stream().map(affiliation -> affiliationMapper.mapToResource(affiliation)).collect(toList()));
    }

    @Override
    public ServiceResult<Void> updateUserAffiliations(Long userId, List<AffiliationResource> affiliations) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId)).andOnSuccess(user -> {
            List<Affiliation> targetAffiliations = user.getAffiliations();
            targetAffiliations.clear();
            affiliationMapper.mapToDomain(affiliations)
                    .forEach(affiliation -> {
                        affiliation.setUser(user);
                        targetAffiliations.add(affiliation);
                    });
            userRepository.save(user);
            return serviceSuccess();
        });
    }

    @Override
    public ServiceResult<Void> updateUserContract(Long userId, ProfileResource profileResource) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> {
                    setUserProfileIfNoneExists(user);
                    return validateContractAndAddToProfile(user, profileResource);
                });


    }

    private ServiceResult<Void> updateUser(UserResource existingUserResource, UserResource updatedUserResource) {
        existingUserResource.setPhoneNumber(updatedUserResource.getPhoneNumber());
        existingUserResource.setTitle(updatedUserResource.getTitle());
        existingUserResource.setLastName(updatedUserResource.getLastName());
        existingUserResource.setFirstName(updatedUserResource.getFirstName());
        existingUserResource.setProfile(updatedUserResource.getProfile());
        User existingUser = userMapper.mapToDomain(existingUserResource);
        return serviceSuccess(userRepository.save(existingUser)).andOnSuccessReturnVoid();
    }


    private ServiceResult<Void> validateContractAndAddToProfile(User user, ProfileResource profileResource) {
        if (profileResource.getContract() != null) {

            Contract currentContract = contractRepository.findByCurrentTrue();
            if (!profileResource.getContract().getId().equals(currentContract.getId())) {
                return serviceFailure(badRequestError("Cannot sign contract other than current contract"));
            }
            if (user.getProfile().getContract()!=null && profileResource.getContract().getId().equals(user.getProfile().getContract().getId())) {
                return serviceFailure(badRequestError("Cannot sign contract because contract is already signed"));
            } else {
                user.getProfile().setContractSignedDate(LocalDateTime.now());
                user.getProfile().setContract(currentContract);
                userRepository.save(user);
                return serviceSuccess();
            }
        } else {
            return serviceFailure(badRequestError("Cannot sign without contract identifier present"));
        }

    }
}
