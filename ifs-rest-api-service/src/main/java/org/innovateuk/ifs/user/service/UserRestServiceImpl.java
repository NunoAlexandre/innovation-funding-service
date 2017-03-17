package org.innovateuk.ifs.user.service;

import org.innovateuk.ifs.commons.error.CommonErrors;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.user.resource.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.Future;

import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.*;
import static java.lang.String.format;
import static org.innovateuk.ifs.user.resource.UserRelatedURLs.*;


/**
 * UserRestServiceImpl is a utility for CRUD operations on {@link UserResource}.
 * This class connects to the {org.innovateuk.ifs.user.controller.UserController}
 * through a REST call.
 */
@Service
public class UserRestServiceImpl extends BaseRestService implements UserRestService {

    private static final Log LOG = LogFactory.getLog(UserRestServiceImpl.class);

    private String userRestURL = "/user";

    private String processRoleRestURL = "/processrole";

    @Override
    public RestResult<UserResource> retrieveUserResourceByUid(String uid) {
        if(StringUtils.isEmpty(uid))
            return restFailure(CommonErrors.notFoundError(UserResource.class, uid));

        return getWithRestResultAnonymous(userRestURL + "/uid/" + uid, UserResource.class);
    }

    @Override
    public RestResult<Void> sendPasswordResetNotification(String email) {
        if(StringUtils.isEmpty(email))
            return restFailure(CommonErrors.notFoundError(UserResource.class, email));

        return getWithRestResultAnonymous(userRestURL + "/"+URL_SEND_PASSWORD_RESET_NOTIFICATION+"/"+ email+"/", Void.class);
    }

    @Override
    public RestResult<Void> checkPasswordResetHash(String hash) {
        LOG.warn("checkPasswordResetHash");

        if(StringUtils.isEmpty(hash))
            return restFailure(CommonErrors.badRequestError("Missing the hash to reset the password with"));

        LOG.warn("checkPasswordResetHash 2 " + userRestURL + "/"+ URL_CHECK_PASSWORD_RESET_HASH+"/"+hash);
        return getWithRestResultAnonymous(userRestURL + "/"+ URL_CHECK_PASSWORD_RESET_HASH+"/"+hash, Void.class);
    }

    @Override
    public RestResult<Void> resetPassword(String hash, String password) {
        LOG.warn("resetPassword");

        if(StringUtils.isEmpty(hash))
            return restFailure(CommonErrors.badRequestError("Missing the hash to reset the password with"));

        LOG.warn("resetPassword 2 " + userRestURL + "/"+ URL_PASSWORD_RESET+"/"+hash+" body: "+password);
        return postWithRestResultAnonymous(String.format("%s/%s/%s", userRestURL, URL_PASSWORD_RESET, hash), password,  Void.class);
    }

    @Override
    public RestResult<UserResource> findUserByEmail(String email) {
        if(StringUtils.isEmpty(email)) {
            return restFailure(CommonErrors.notFoundError(UserResource.class, email));
        }

        return getWithRestResultAnonymous(userRestURL + "/findByEmail/" + email + "/", UserResource.class);
    }

    @Override
    public RestResult<UserResource> findUserByEmailForAnonymousUserFlow(String email) {
        if(StringUtils.isEmpty(email)) {
            return restFailure(CommonErrors.notFoundError(UserResource.class, email));
        }

        return getWithRestResultAnonymous(userRestURL + "/findByEmail/" + email + "/", UserResource.class);
    }

    @Override
    public RestResult<UserResource> retrieveUserById(Long id) {
        if(id == null || id.equals(0L)) {
            return restFailure(CommonErrors.notFoundError(UserResource.class, id));
        }

        return getWithRestResult(userRestURL + "/id/" + id, UserResource.class);
    }

    @Override
    public RestResult<List<UserResource>> findAll() {
        return getWithRestResult(userRestURL + "/findAll/", userListType());
    }

    @Override
    public RestResult<List<UserResource>> findByUserRoleType(UserRoleType userRoleType) {
        String roleName = userRoleType.getName();
        return getWithRestResult(userRestURL + "/findByRole/"+roleName, userListType());
    }

    @Override
    public RestResult<ProcessRoleResource> findProcessRole(Long userId, Long applicationId) {
        return getWithRestResult(processRoleRestURL + "/findByUserApplication/" + userId + "/" + applicationId, ProcessRoleResource.class);
    }

    @Override
    public Future<RestResult<ProcessRoleResource>> findProcessRoleById(Long processRoleId) {
        return getWithRestResultAsync(processRoleRestURL + "/" + processRoleId, ProcessRoleResource.class);
    }

    @Override
    public RestResult<List<ProcessRoleResource>> findProcessRole(Long applicationId) {
        return getWithRestResult(processRoleRestURL + "/findByApplicationId/" + applicationId, processRoleResourceListType());
    }

    @Override
    public RestResult<List<UserResource>> findAssignableUsers(Long applicationId){
        return getWithRestResult(userRestURL + "/findAssignableUsers/" + applicationId, userListType());
    }

    @Override
    public Future<RestResult<ProcessRoleResource[]>> findAssignableProcessRoles(Long applicationId){
        return getWithRestResultAsync(processRoleRestURL + "/findAssignable/" + applicationId, ProcessRoleResource[].class);
    }

    @Override
    public RestResult<List<UserResource>> findRelatedUsers(Long applicationId){
        return getWithRestResult(userRestURL + "/findRelatedUsers/"+applicationId, userListType());
    }

    @Override
    public RestResult<Boolean> userHasApplicationForCompetition(Long userId, Long competitionId) {
        return getWithRestResult(processRoleRestURL + "/userHasApplicationForCompetition/" + userId + "/" + competitionId, Boolean.class);
    }

    @Override
    public RestResult<Void> verifyEmail(String hash){
        return getWithRestResultAnonymous(String.format("%s/%s/%s", userRestURL, URL_VERIFY_EMAIL, hash), Void.class);
    }

    @Override
    public RestResult<Void> resendEmailVerificationNotification(String email) {
        return putWithRestResultAnonymous(String.format("%s/%s/%s/", userRestURL, URL_RESEND_EMAIL_VERIFICATION_NOTIFICATION, email), Void.class);
    }

    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisationWithCompetitionId(String firstName, String lastName, String password, String email, String title,
                                                                                        String phoneNumber, String gender, Long ethnicity, String disability, Long organisationId, Long competitionId) {
        UserResource user = new UserResource();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setEmail(email);
        if(!StringUtils.isEmpty(title)) {
            user.setTitle(Title.valueOf(title));
        }
        user.setPhoneNumber(phoneNumber);
        if(!StringUtils.isEmpty(gender)) {
            user.setGender(Gender.valueOf(gender));
        }
        user.setEthnicity(ethnicity);
        if(!StringUtils.isEmpty(disability)) {
            user.setDisability(Disability.valueOf(disability));
        }

        String url;
        if(competitionId != null){
            url = userRestURL + "/createLeadApplicantForOrganisation/" + organisationId +"/"+competitionId;
        }else{
            url = userRestURL + "/createLeadApplicantForOrganisation/" + organisationId;
        }

        return postWithRestResultAnonymous(url, user, UserResource.class);
    }

    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisation(String firstName, String lastName, String password, String email, String title,
                                                                       String phoneNumber, String gender, Long ethnicity, String disability, Long organisationId) {
        return this.createLeadApplicantForOrganisationWithCompetitionId(firstName, lastName, password, email, title, phoneNumber, gender, ethnicity, disability, organisationId, null);
    }

    @Override
    public RestResult<UserResource> updateDetails(Long id, String email, String firstName, String lastName, String title, String phoneNumber,
                                                  String gender, Long ethnicity, String disability) {
        UserResource user = new UserResource();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if(!StringUtils.isEmpty(title)) {
            user.setTitle(Title.valueOf(title));
        }
        user.setPhoneNumber(phoneNumber);
        if(!StringUtils.isEmpty(gender)) {
            user.setGender(Gender.valueOf(gender));
        }
        user.setEthnicity(ethnicity);
        if(!StringUtils.isEmpty(disability)) {
            user.setDisability(Disability.valueOf(disability));
        }
        String url = userRestURL + "/updateDetails";
        return postWithRestResult(url, user, UserResource.class);
    }

    @Override
    public RestResult<ProfileSkillsResource> getProfileSkills(Long userId) {
        return getWithRestResult(format("%s/id/%s/getProfileSkills", userRestURL, userId), ProfileSkillsResource.class);
    }

    @Override
    public RestResult<Void> updateProfileSkills(Long userId, ProfileSkillsEditResource profileSkillsEditResource) {
        return putWithRestResult(format("%s/id/%s/updateProfileSkills", userRestURL, userId), profileSkillsEditResource, Void.class);
    }

    @Override
    public RestResult<ProfileAgreementResource> getProfileAgreement(Long userId) {
        return getWithRestResult(format("%s/id/%s/getProfileAgreement", userRestURL, userId), ProfileAgreementResource.class);
    }

    @Override
    public RestResult<Void> updateProfileAgreement(Long userId) {
        return putWithRestResult(format("%s/id/%s/updateProfileAgreement", userRestURL, userId), Void.class);
    }

    @Override
    public RestResult<List<AffiliationResource>> getUserAffiliations(Long userId) {
        return getWithRestResult(format("%s/id/%s/getUserAffiliations", userRestURL, userId), affiliationResourceListType());
    }

    @Override
    public RestResult<Void> updateUserAffiliations(Long userId, List<AffiliationResource> affiliations) {
        return putWithRestResult(format("%s/id/%s/updateUserAffiliations", userRestURL, userId), affiliations, Void.class);
    }

    @Override
    public RestResult<UserProfileResource> getUserProfile(Long userId) {
        return getWithRestResult(format("%s/id/%s/getUserProfile", userRestURL, userId), UserProfileResource.class);
    }

    @Override
    public RestResult<Void> updateUserProfile(Long userId, UserProfileResource userProfile) {
        return putWithRestResult(format("%s/id/%s/updateUserProfile", userRestURL, userId), userProfile, Void.class);
    }

    @Override
    public RestResult<UserProfileStatusResource> getUserProfileStatus(Long userId) {
        return getWithRestResult(format("%s/id/%s/profileStatus", userRestURL, userId), UserProfileStatusResource.class);
    }

}
