package org.innovateuk.ifs.invite.transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceFailure;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.RoleInvite;
import org.innovateuk.ifs.invite.mapper.RoleInviteMapper;
import org.innovateuk.ifs.invite.repository.InviteRoleRepository;
import org.innovateuk.ifs.invite.resource.RoleInviteResource;
import org.innovateuk.ifs.notifications.resource.ExternalUserNotificationTarget;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.project.transactional.EmailService;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.mapper.RoleMapper;
import org.innovateuk.ifs.user.repository.RoleRepository;
import org.innovateuk.ifs.user.resource.AdminRoleType;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.USER_ROLE_INVITE_INVALID;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.USER_ROLE_INVITE_TARGET_USER_ALREADY_INVITED;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Transactional and secured service implementation providing operations around invites for users.
 */
@Service
public class InviteUserServiceImpl extends BaseTransactionalService implements InviteUserService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private InviteRoleRepository inviteRoleRepository;

    @Autowired
    RoleInviteMapper roleInviteMapper;

    private EmailService emailService;

    @Autowired
    private LoggedInUserSupplier loggedInUserSupplier;

    @Autowired
    private RoleMapper roleMapper;

    private static final Log LOG = LogFactory.getLog(InviteUserServiceImpl.class);

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    public static final String WEB_CONTEXT = "/internal-user";

    enum Notifications {
        INVITE_INTERNAL_USER
    }

    @Override
    @Transactional
    public ServiceResult<Void> saveUserInvite(UserResource invitedUser, AdminRoleType adminRoleType) {

        return validateInvite(invitedUser, adminRoleType)
                .andOnSuccess(() -> getRole(adminRoleType))
                .andOnSuccess((Role role) -> validateUserNotAlreadyInvited(invitedUser, role)
                        .andOnSuccess(() -> saveInvite(invitedUser, role))
                );
    }

    private ServiceResult<Void> validateInvite(UserResource invitedUser, AdminRoleType adminRoleType) {

        if (StringUtils.isEmpty(invitedUser.getEmail()) || StringUtils.isEmpty(invitedUser.getFirstName())
                || StringUtils.isEmpty(invitedUser.getLastName()) || adminRoleType == null){
            return serviceFailure(USER_ROLE_INVITE_INVALID);
        }
        return serviceSuccess();
    }

    private ServiceResult<Void> validateUserNotAlreadyInvited(UserResource invitedUser, Role role) {

        List<RoleInvite> existingInvites = inviteRoleRepository.findByRoleIdAndEmail(role.getId(), invitedUser.getEmail());
        return existingInvites.isEmpty() ? serviceSuccess() : serviceFailure(USER_ROLE_INVITE_TARGET_USER_ALREADY_INVITED);
    }

    private ServiceResult<Void> saveInvite(UserResource invitedUser, Role role) {
        RoleInvite roleInvite = new RoleInvite(invitedUser.getFirstName() + " " + invitedUser.getLastName(),
                invitedUser.getEmail(),
                generateInviteHash(),
                role,
                InviteStatus.CREATED);

        inviteRoleRepository.save(roleInvite);

        return serviceSuccess();
    }

    private ServiceResult<Void> inviteInternalUser(RoleInvite roleInvite) {

        try {
            Map<String, Object> globalArgs = createGlobalArgsForInternalUserInvite(roleInvite);
            ServiceResult<Void> inviteContactEmailSendResult = emailService.sendEmail(
                    Collections.singletonList(createInviteInternalUserNotificationTarget(roleInvite)),
                    globalArgs,
                    Notifications.INVITE_INTERNAL_USER);

            inviteContactEmailSendResult.handleSuccessOrFailure(
                    failure -> handleInviteError(roleInvite, failure),
                    success -> handleInviteSuccess(roleInvite)
            );
            return inviteContactEmailSendResult;
        } catch (IllegalArgumentException e) {
            LOG.error(String.format("Role %s lookup failed for user %s", roleInvite.getEmail(), roleInvite.getTarget().getName()));
            return ServiceResult.serviceFailure(new Error(CommonFailureKeys.ADMIN_INVALID_USER_ROLE));
        }
    }

    private NotificationTarget createInviteInternalUserNotificationTarget(RoleInvite roleInvite) {
        return new ExternalUserNotificationTarget(roleInvite.getName(), roleInvite.getEmail());
    }

    private Map<String, Object> createGlobalArgsForInternalUserInvite(RoleInvite roleInvite) {
        Map<String, Object> globalArguments = new HashMap<>();
        RoleResource roleResource = roleMapper.mapIdToResource(roleInvite.getTarget().getId());
        globalArguments.put("role", roleResource.getDisplayName());
        globalArguments.put("inviteUrl", getInviteUrl(webBaseUrl + WEB_CONTEXT, roleInvite));
        return globalArguments;
    }

    private String getInviteUrl(String baseUrl, RoleInvite inviteResource) {
        return String.format("%s/accept-invite/%s", baseUrl, inviteResource.getHash());
    }

    @Override
    public ServiceResult<RoleInviteResource> getInvite(String inviteHash) {
        RoleInvite roleInvite = inviteRoleRepository.getByHash(inviteHash);
        return serviceSuccess(roleInviteMapper.mapToResource(roleInvite));
    }

    @Override
    public ServiceResult<Boolean> checkExistingUser(String inviteHash) {
        return getByHash(inviteHash)
                .andOnSuccessReturn(i -> userRepository.findByEmail(i.getEmail()))
                .andOnSuccess(u -> serviceSuccess(u.isPresent()));
    }

    private ServiceResult<RoleInvite> getByHash(String hash) {
        return find(inviteRoleRepository.getByHash(hash), notFoundError(RoleInvite.class, hash));
    }

    private ServiceResult<Role> getRole(AdminRoleType adminRoleType) {
        return find(roleRepository.findOneByName(adminRoleType.getName()), notFoundError(Role.class, adminRoleType.getName()));
    }

    private ServiceResult<Boolean> handleInviteError(RoleInvite i, ServiceFailure failure) {
        LOG.error(String.format("Invite failed %s, %s, %s (error count: %s)", i.getId(), i.getEmail(), i.getTarget().getName(), failure.getErrors().size()));
        List<Error> errors = failure.getErrors();
        return serviceFailure(errors);
    }

    private boolean handleInviteSuccess(RoleInvite roleInvite) {
        inviteRoleRepository.save(roleInvite.send(loggedInUserSupplier.get(), ZonedDateTime.now()));
        return true;
    }
}
