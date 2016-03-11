package com.worth.ifs.user.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Future;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.rest.RestResult.restFailure;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.processRoleListType;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.userListType;

/**
 * UserRestServiceImpl is a utility for CRUD operations on {@link User}.
 * This class connects to the {@link com.worth.ifs.user.controller.UserController}
 * through a REST call.
 */
@Service
public class UserRestServiceImpl extends BaseRestService implements UserRestService {
    private static final Log LOG = LogFactory.getLog(UserRestServiceImpl.class);
    private String userRestURL;
    private String processRoleRestURL;

    @Value("${ifs.data.service.rest.user}")
    void setUserRestUrl(String userRestURL) {
        this.userRestURL = userRestURL;
    }

    @Value("${ifs.data.service.rest.processrole}")
    void setProcessRoleRestUrl(String processRoleRestURL) {
        this.processRoleRestURL = processRoleRestURL;
    }

    @Override
    public RestResult<User> retrieveUserByUid(String uid) {
        if(StringUtils.isEmpty(uid))
            return restFailure(notFoundError(User.class, uid));

        return getWithRestResult(userRestURL + "/uid/" + uid, User.class);
    }

    @Override
    public RestResult<Void> sendPasswordResetNotification(String email) {
        if(StringUtils.isEmpty(email))
            return restFailure(notFoundError(User.class, email));

        try {
            email = UriUtils.encodePathSegment(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return getWithRestResult(userRestURL + "/sendPasswordResetNotification/"+ email+"/", Void.class);
    }

    @Override
    public RestResult<UserResource> findUserByEmail(String email) {
        if(StringUtils.isEmpty(email)) {
            return restFailure(notFoundError(User.class, email));
        }

        return getWithRestResult(userRestURL + "/findByEmail/" + email + "/", UserResource.class);
    }

    @Override
    public RestResult<User> retrieveUserById(Long id) {
        if(id == null || id.equals(0L)) {
            return restFailure(notFoundError(User.class, id));
        }

        return getWithRestResult(userRestURL + "/id/" + id, User.class);
    }

    @Override
    public RestResult<List<User>> findAll() {
        return getWithRestResult(userRestURL + "/findAll/", userListType());
    }

    @Override
    public RestResult<ProcessRole> findProcessRole(Long userId, Long applicationId) {
        return getWithRestResult(processRoleRestURL + "/findByUserApplication/" + userId + "/" + applicationId, ProcessRole.class);
    }

    @Override
    public Future<RestResult<ProcessRole>> findProcessRoleById(Long processRoleId) {
        return getWithRestResultAsync(processRoleRestURL + "/" + processRoleId, ProcessRole.class);
    }

    @Override
    public RestResult<List<ProcessRole>> findProcessRole(Long applicationId) {
        return getWithRestResult(processRoleRestURL + "/findByApplicationId/" + applicationId, processRoleListType());
    }

    @Override
    public RestResult<List<User>> findAssignableUsers(Long applicationId){
        return getWithRestResult(userRestURL + "/findAssignableUsers/" + applicationId, userListType());
    }

    @Override
    public Future<RestResult<ProcessRole[]>> findAssignableProcessRoles(Long applicationId){
        return getWithRestResultAsync(processRoleRestURL + "/findAssignable/" + applicationId, ProcessRole[].class);
    }

    @Override
    public RestResult<List<User>> findRelatedUsers(Long applicationId){
        return getWithRestResult(userRestURL + "/findRelatedUsers/"+applicationId, userListType());
    }

    @Override
    public RestResult<Void> verifyEmail(String hash){
        return getWithRestResult(userRestURL + "/verifyEmail/"+hash, Void.class);
    }

    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisationWithCompetitionId(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId, Long competitionId) {
        UserResource user = new UserResource();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
        user.setEmail(email);
        user.setTitle(title);
        user.setPhoneNumber(phoneNumber);

        String url;
        if(competitionId != null){
            url = userRestURL + "/createLeadApplicantForOrganisation/" + organisationId +"/"+competitionId;
        }else{
            url = userRestURL + "/createLeadApplicantForOrganisation/" + organisationId;
        }

        return postWithRestResult(url, user, UserResource.class);
    }
    @Override
    public RestResult<UserResource> createLeadApplicantForOrganisation(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId) {
        return this.createLeadApplicantForOrganisationWithCompetitionId(firstName, lastName, password, email, title, phoneNumber, organisationId, null);
    }

    @Override
    public RestResult<UserResource> updateDetails(String email, String firstName, String lastName, String title, String phoneNumber) {
        UserResource user = new UserResource();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTitle(title);
        user.setPhoneNumber(phoneNumber);
        String url = userRestURL + "/updateDetails";
        return postWithRestResult(url, user, UserResource.class);
    }
}
