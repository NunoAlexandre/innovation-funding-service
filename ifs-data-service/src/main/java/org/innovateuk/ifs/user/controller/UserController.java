package org.innovateuk.ifs.user.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.token.transactional.TokenService;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserPageResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.transactional.BaseUserService;
import org.innovateuk.ifs.user.transactional.CrmService;
import org.innovateuk.ifs.user.transactional.RegistrationService;
import org.innovateuk.ifs.user.transactional.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.user.resource.UserRelatedURLs.*;

/**
 * This RestController exposes CRUD operations to both the
 * {org.innovateuk.ifs.user.service.UserRestServiceImpl} and other REST-API users
 * to manage {@link User} related data.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Log LOG = LogFactory.getLog(UserController.class);

    private static final String DEFAULT_PAGE_SIZE = "20";

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CrmService crmService;

    @GetMapping("/uid/{uid}")
    public RestResult<UserResource> getUserByUid(@PathVariable("uid") final String uid) {
        return baseUserService.getUserResourceByUid(uid).toGetResponse();
    }

    @GetMapping("/id/{id}")
    public RestResult<UserResource> getUserById(@PathVariable("id") final Long id) {
        return baseUserService.getUserById(id).toGetResponse();
    }

    @GetMapping("/findByRole/{userRoleName}")
    public RestResult<List<UserResource>> findByRole(@PathVariable("userRoleName") final String userRoleName) {
        return baseUserService.findByProcessRole(UserRoleType.fromName(userRoleName)).toGetResponse();
    }

    @GetMapping("/internal/active/count")
    public RestResult<Long> countActiveInternalUsers(){
        return baseUserService.countActiveByProcessRoles(UserRoleType.internalRoles()).toGetResponse();
    }

    @GetMapping("/internal/inactive/count")
    public RestResult<Long> countInactiveInternalUsers(){
        return baseUserService.countInactiveByProcessRoles(UserRoleType.internalRoles()).toGetResponse();
    }

    @GetMapping("/internal/active")
    public RestResult<UserPageResource> findActiveInternalUsers(@RequestParam(value = "page", defaultValue = "0") int pageIndex,
                                                                @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize){
        return baseUserService.findActiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(pageIndex, pageSize)).toGetResponse();
    }

    @GetMapping("/internal/inactive")
    public RestResult<UserPageResource> findInactiveInternalUsers(@RequestParam(value = "page", defaultValue = "0") int pageIndex,
                                                                  @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize){
        return baseUserService.findInactiveByProcessRoles(UserRoleType.internalRoles(), new PageRequest(pageIndex, pageSize)).toGetResponse();
    }

    @GetMapping("/findAll/")
    public RestResult<List<UserResource>> findAll() {
        return baseUserService.findAll().toGetResponse();
    }

    @GetMapping("/findByEmail/{email}/")
    public RestResult<UserResource> findByEmail(@PathVariable("email") final String email) {
        return userService.findByEmail(email).toGetResponse();
    }

    @GetMapping("/findAssignableUsers/{applicationId}")
    public RestResult<Set<UserResource>> findAssignableUsers(@PathVariable("applicationId") final Long applicationId) {
        return userService.findAssignableUsers(applicationId).toGetResponse();
    }

    @GetMapping("/findRelatedUsers/{applicationId}")
    public RestResult<Set<UserResource>> findRelatedUsers(@PathVariable("applicationId") final Long applicationId) {
        return userService.findRelatedUsers(applicationId).toGetResponse();
    }

    @GetMapping("/" + URL_SEND_PASSWORD_RESET_NOTIFICATION + "/{emailaddress}/")
    public RestResult<Void> sendPasswordResetNotification(@PathVariable("emailaddress") final String emailAddress) {
        return userService.findByEmail(emailAddress)
                .andOnSuccessReturn(userService::sendPasswordResetNotification)
                .toPutResponse();
    }

    @GetMapping("/" + URL_CHECK_PASSWORD_RESET_HASH + "/{hash}")
    public RestResult<Void> checkPasswordReset(@PathVariable("hash") final String hash) {
        return tokenService.getPasswordResetToken(hash).andOnSuccessReturnVoid().toPutResponse();
    }

    @PostMapping("/" + URL_PASSWORD_RESET + "/{hash}")
    public RestResult<Void> resetPassword(@PathVariable("hash") final String hash, @RequestBody final String password) {
        return userService.changePassword(hash, password)
                .toPutResponse();
    }

    @GetMapping("/" + URL_VERIFY_EMAIL + "/{hash}")
    public RestResult<Void> verifyEmail(@PathVariable("hash") final String hash) {
        final ServiceResult<Token> result = tokenService.getEmailToken(hash);
        LOG.debug(String.format("UserController verifyHash: %s", hash));
        return result.handleSuccessOrFailure(
                failure -> restFailure(failure.getErrors()),
                token -> {
                    registrationService.activateApplicantAndSendDiversitySurvey(token.getClassPk()).andOnSuccessReturnVoid(v -> {
                        tokenService.handleExtraAttributes(token);
                        tokenService.removeToken(token);
                        crmService.syncCrmContact(token.getClassPk());
                    });
                    return restSuccess();
                });
    }

    @PutMapping("/" + URL_RESEND_EMAIL_VERIFICATION_NOTIFICATION + "/{emailAddress}/")
    public RestResult<Void> resendEmailVerificationNotification(@PathVariable("emailAddress") final String emailAddress) {
        return userService.findInactiveByEmail(emailAddress)
                .andOnSuccessReturn(user -> registrationService.resendUserVerificationEmail(user))
                .toPutResponse();
    }

    @PostMapping("/createLeadApplicantForOrganisation/{organisationId}")
    public RestResult<UserResource> createUser(@PathVariable("organisationId") final Long organisationId, @RequestBody UserResource userResource) {
        return registrationService.createOrganisationUser(organisationId, userResource).andOnSuccessReturn(created ->
                {
                    registrationService.sendUserVerificationEmail(created, empty()).getSuccessObjectOrThrowException();
                    return created;
                }
        ).toPostCreateResponse();
    }

    @PostMapping("/createLeadApplicantForOrganisation/{organisationId}/{competitionId}")
    public RestResult<UserResource> createUser(@PathVariable("organisationId") final Long organisationId, @PathVariable("competitionId") final Long competitionId, @RequestBody UserResource userResource) {
        return registrationService.createOrganisationUser(organisationId, userResource).andOnSuccessReturn(created ->
                {
                    registrationService.sendUserVerificationEmail(created, of(competitionId)).getSuccessObjectOrThrowException();
                    return created;
                }
        ).toPostCreateResponse();
    }

    @PostMapping("/updateDetails")
    public RestResult<Void> updateDetails(@RequestBody UserResource userResource) {
        return userService.updateDetails(userResource).andOnSuccessReturnVoid(() -> crmService.syncCrmContact(userResource.getId())).toPutResponse();
    }
 }
