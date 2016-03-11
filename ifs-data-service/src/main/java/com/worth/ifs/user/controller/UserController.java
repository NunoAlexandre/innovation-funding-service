package com.worth.ifs.user.controller;

import com.worth.ifs.commons.error.CommonErrors;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.token.domain.Token;
import com.worth.ifs.token.domain.TokenType;
import com.worth.ifs.token.transactional.TokenService;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.transactional.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This RestController exposes CRUD operations to both the
 * {@link com.worth.ifs.user.service.UserRestServiceImpl} and other REST-API users
 * to manage {@link User} related data.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Log LOG = LogFactory.getLog(UserController.class);
    public static final String URL_CHECK_PASSWORD_RESET_HASH = "checkPasswordResetHash";
    public static final String URL_PASSWORD_RESET = "passwordReset";
    public static final String URL_SEND_PASSWORD_RESET_NOTIFICATION = "sendPasswordResetNotification";
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @RequestMapping("/token/{token}")
    public RestResult<User> getUserByToken(@PathVariable("token") final String token) {
        return userService.getUserByToken(token).toGetResponse();
    }

    @RequestMapping("/email/{email}/password/{password}")
    public RestResult<User> getUserByEmailandPassword(@PathVariable("email") final String email, @PathVariable("password") final String password) {
        return userService.getUserByEmailandPassword(email, password).toGetResponse();
    }

    @RequestMapping("/id/{id}")
    public RestResult<User> getUserById(@PathVariable("id") final Long id) {
        return userService.getUserById(id).toGetResponse();
    }

    @RequestMapping("/name/{name}")
    public RestResult<List<User>> getUserByName(@PathVariable("name") final String name) {
        return userService.getUserByName(name).toGetResponse();
    }

    @RequestMapping("/findAll/")
    public RestResult<List<User>> findAll() {
        return userService.findAll().toGetResponse();
    }

    @RequestMapping("/findByEmail/{email}/")
    public RestResult<UserResource> findByEmail(@PathVariable("email") final String email) {
        return userService.findByEmail(email).andOnSuccessReturn(UserResource::new).toGetResponse();
    }

    @RequestMapping("/findAssignableUsers/{applicationId}")
    public RestResult<Set<User>> findAssignableUsers(@PathVariable("applicationId") final Long applicationId) {
        return userService.findAssignableUsers(applicationId).toGetResponse();
    }

    @RequestMapping("/findRelatedUsers/{applicationId}")
    public RestResult<Set<User>> findRelatedUsers(@PathVariable("applicationId") final Long applicationId) {
        return userService.findRelatedUsers(applicationId).toGetResponse();
    }

    @RequestMapping("/" + URL_SEND_PASSWORD_RESET_NOTIFICATION + "/{emailaddress}/")
    public RestResult<Void> sendPasswordResetNotification(@PathVariable("emailaddress") final String emailAddress) {
        return userService.findByEmail(emailAddress)
                .andOnSuccessReturn(userService::sendPasswordResetNotification)
                .toPutResponse();
    }

    @RequestMapping("/" + URL_CHECK_PASSWORD_RESET_HASH + "/{hash}")
    public RestResult<Void> checkPasswordReset(@PathVariable("hash") final String hash) {
        LOG.warn("checkPasswordReset "+hash);
        return userService.checkPasswordResetHashValidity(hash)
                .toPutResponse();
    }

    @RequestMapping("/" + URL_PASSWORD_RESET + "/{hash}/{password}")
    public RestResult<Void> resetPassword(@PathVariable("hash") final String hash, @PathVariable("password") final String password) {
        return userService.changePassword(hash, password)
                .toPutResponse();
    }

    @RequestMapping("/verifyEmail/{hash}")
    public RestResult<Void> verifyEmail(@PathVariable("hash") final String hash) {
        Optional<Token> optionalToken = tokenService.getTokenByHash(hash);

        if(optionalToken.isPresent()){
            Token token = optionalToken.get();
            if(TokenType.VERIFY_EMAIL_ADDRESS.equals(token.getType()) &&
                    User.class.getName().equals(token.getClassName())
            ){
                userService.activateUser(token.getClassPk()).andOnSuccessReturnVoid(v -> {
                    tokenService.handleExtraAttributes(token);
                    tokenService.removeToken(token);
                });
            }
        }else{
            return RestResult.restFailure(CommonErrors.notFoundError(Token.class, hash));
        }

        LOG.warn(String.format("UserController verifyHash: %s", hash));
        return RestResult.restSuccess();
    }

    @RequestMapping("/createLeadApplicantForOrganisation/{organisationId}")
    public RestResult<UserResource> createUser(@PathVariable("organisationId") final Long organisationId, @RequestBody UserResource userResource) {
        return userService.createApplicantUser(organisationId, userResource).toPostCreateResponse();
    }
    @RequestMapping("/createLeadApplicantForOrganisation/{organisationId}/{competitionId}")
    public RestResult<UserResource> createUser(@PathVariable("organisationId") final Long organisationId, @PathVariable("competitionId") final Long competitionId, @RequestBody UserResource userResource) {
        return userService.createApplicantUser(organisationId, userResource, Optional.ofNullable(competitionId)).toPostCreateResponse();
    }

    @RequestMapping("/updateDetails")
    public RestResult<UserResource> createUser(@RequestBody UserResource userResource) {
        return userService.updateUser(userResource).toGetResponse();
    }
}
