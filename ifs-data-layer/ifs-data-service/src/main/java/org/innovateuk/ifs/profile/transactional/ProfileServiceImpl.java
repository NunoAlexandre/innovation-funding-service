package org.innovateuk.ifs.profile.transactional;

import org.innovateuk.ifs.address.mapper.AddressMapper;
import org.innovateuk.ifs.category.mapper.InnovationAreaMapper;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.profile.domain.Profile;
import org.innovateuk.ifs.profile.repository.ProfileRepository;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Agreement;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.AgreementMapper;
import org.innovateuk.ifs.user.mapper.EthnicityMapper;
import org.innovateuk.ifs.user.repository.AgreementRepository;
import org.innovateuk.ifs.user.resource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.innovateuk.ifs.commons.error.CommonErrors.badRequestError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * A Service that covers basic operations concerning Profiles
 */
@Service
public class ProfileServiceImpl extends BaseTransactionalService implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private AgreementMapper agreementMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private EthnicityMapper ethnicityMapper;

    @Autowired
    private InnovationAreaMapper innovationAreaMapper;

    @Override
    public ServiceResult<ProfileSkillsResource> getProfileSkills(long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> {
                    ProfileSkillsResource profileSkillsResource = new ProfileSkillsResource();
                    profileSkillsResource.setUser(user.getId());
                    Profile profile = profileRepository.findOne(user.getProfileId());
                    if (profile != null) {
                        profileSkillsResource.setInnovationAreas(simpleMap(profile.getInnovationAreas(),
                                innovationArea -> innovationAreaMapper.mapToResource(innovationArea)));
                        profileSkillsResource.setBusinessType(profile.getBusinessType());
                        profileSkillsResource.setSkillsAreas(profile.getSkillsAreas());
                    }
                    return serviceSuccess(profileSkillsResource);
                });
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateProfileSkills(long userId, ProfileSkillsEditResource profileSkills) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> updateUserProfileSkills(user, profileSkills));
    }

    private ServiceResult<Void> updateUserProfileSkills(User user, ProfileSkillsEditResource profileSkills) {
        Profile profile = getOrCreateUserProfile(user);
        profile.setBusinessType(profileSkills.getBusinessType());
        profile.setSkillsAreas(profileSkills.getSkillsAreas());
        profileRepository.save(profile);
        return serviceSuccess();
    }

    @Override
    public ServiceResult<ProfileAgreementResource> getProfileAgreement(long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user ->
                        getCurrentAgreement().andOnSuccess(currentAgreement -> {
                            Profile profile = profileRepository.findOne(user.getProfileId());
                            boolean hasAgreement = profile != null && profile.getAgreement() != null;
                            boolean hasCurrentAgreement = hasAgreement && currentAgreement.getId().equals(profile.getAgreement().getId());
                            ProfileAgreementResource profileAgreementResource = new ProfileAgreementResource();
                            profileAgreementResource.setUser(user.getId());
                            profileAgreementResource.setAgreement(agreementMapper.mapToResource(currentAgreement));
                            profileAgreementResource.setCurrentAgreement(hasCurrentAgreement);
                            if (hasCurrentAgreement) {
                                profileAgreementResource.setAgreementSignedDate(profile.getAgreementSignedDate());
                            }
                            return serviceSuccess(profileAgreementResource);
                        })
                );
    }

    private void updateProfileAgreement(User user, Agreement agreement) {
        Profile profile = getOrCreateUserProfile(user);
        profile.setAgreementSignedDate(ZonedDateTime.now());
        profile.setAgreement(agreement);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateProfileAgreement(long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user ->
                        getCurrentAgreement().andOnSuccess(agreement ->
                                validateAgreement(agreement, user).andOnSuccess(() -> {
                                    updateProfileAgreement(user, agreement);
                                    return serviceSuccess();
                                })
                        )
                );
    }

    @Override
    public ServiceResult<UserProfileResource> getUserProfile(Long userId) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> {
                    UserProfileResource profileDetails = assignUserProfileDetails(user);

                    if (user.getProfileId() != null) {
                        Profile profile = profileRepository.findOne(user.getProfileId());
                        profileDetails.setAddress(addressMapper.mapToResource(profile.getAddress()));
                        profileDetails.setCreatedBy(profile.getCreatedBy().getName());
                        profileDetails.setCreatedOn(profile.getCreatedOn());
                        profileDetails.setModifiedBy(profile.getModifiedBy().getName());
                        profileDetails.setModifiedOn(profile.getModifiedOn());
                    }
                    return serviceSuccess(profileDetails);
                });
    }

    @Override
    @Transactional
    public ServiceResult<Void> updateUserProfile(Long userId, UserProfileResource profileDetails) {
        return find(userRepository.findOne(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> updateUserProfileDetails(user, profileDetails));
    }

    private ServiceResult<Void> updateUserProfileDetails(User user, UserProfileResource profileDetails) {
        updateBasicDetails(user, profileDetails);

        Profile profile = getOrCreateUserProfile(user);
        profile.setAddress(addressMapper.mapToDomain(profileDetails.getAddress()));
        profileRepository.save(profile);

        return serviceSuccess();
    }

    private void updateBasicDetails(User user, UserProfileResource profileDetails) {
        user.setTitle(profileDetails.getTitle());
        user.setFirstName(profileDetails.getFirstName());
        user.setLastName(profileDetails.getLastName());
        user.setGender(profileDetails.getGender());
        user.setDisability(profileDetails.getDisability());
        user.setEthnicity(ethnicityMapper.mapIdToDomain(profileDetails.getEthnicity().getId()));
        user.setPhoneNumber(profileDetails.getPhoneNumber());
    }

    private UserProfileResource assignUserProfileDetails(User user) {
        UserProfileResource profile = new UserProfileResource();

        profile.setUser(user.getId());
        profile.setTitle(user.getTitle());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setGender(user.getGender());
        profile.setDisability(user.getDisability());
        profile.setEthnicity(ethnicityMapper.mapToResource(user.getEthnicity()));
        profile.setEmail(user.getEmail());
        profile.setPhoneNumber(user.getPhoneNumber());

        return profile;
    }

    @Override
    public ServiceResult<UserProfileStatusResource> getUserProfileStatus(Long userId) {
        return getUser(userId).andOnSuccess(this::getProfileStatusForUser);
    }

    private ServiceResult<UserProfileStatusResource> getProfileStatusForUser(User user) {
        Profile profile = profileRepository.findOne(user.getProfileId());
        return serviceSuccess(
                new UserProfileStatusResource(
                        user.getId(),
                        profile != null && profile.getSkillsAreas() != null,
                        user.getAffiliations() != null && !user.getAffiliations().isEmpty(),
                        profile != null && profile.getAgreementSignedDate() != null
                )
        );
    }

    private ServiceResult<Void> validateAgreement(Agreement agreement, User user) {
        Profile profile = getOrCreateUserProfile(user);
        if (profile.getAgreement() != null && agreement.getId().equals(profile.getAgreement().getId())) {
            return serviceFailure(badRequestError("validation.assessorprofileagreementform.terms.alreadysigned"));
        }
        return serviceSuccess();
    }

    private Profile getOrCreateUserProfile(User user) {
        Profile profile = user.getProfileId() != null ? profileRepository.findOne(user.getProfileId()) : null;
        if (profile == null) {
            profile = profileRepository.save(new Profile());
            user.setProfileId(profile.getId());
            userRepository.save(user);
        }
        return profile;
    }

    private ServiceResult<Agreement> getCurrentAgreement() {
        return find(agreementRepository.findByCurrentTrue(), notFoundError(Agreement.class));
    }
}
