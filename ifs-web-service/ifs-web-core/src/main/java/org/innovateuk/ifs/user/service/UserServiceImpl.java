package org.innovateuk.ifs.user.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.UserApplicationRole;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;


/**
 * This class contains methods to retrieve and store {@link UserResource} related data,
 * through the RestService {@link UserRestService}.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Log LOG = LogFactory.getLog(UserServiceImpl.class);
    @Autowired
    private UserRestService userRestService;
    @Autowired
    private ProcessRoleService processRoleService;

    @Override
    public UserResource findById(Long userId) {
        return userRestService.retrieveUserById(userId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<UserResource> getAssignable(Long applicationId) {
        return userRestService.findAssignableUsers(applicationId).getSuccessObjectOrThrowException();
    }

    @Override
    public Boolean isLeadApplicant(Long userId, ApplicationResource application) {
        List<ProcessRoleResource> userApplicationRoles = processRoleService.getByApplicationId(application.getId());
        return userApplicationRoles.stream().anyMatch(uar -> uar.getRoleName()
                .equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()) && uar.getUser().equals(userId));

    }

    @Override
    public boolean isCompetitionExecutive(Long userId) {
        return isUserRoleType(userId, UserRoleType.COMP_ADMIN);
    }

    @Override
    public boolean isCompetitionTechnologist(Long userId) {
        return isUserRoleType(userId, UserRoleType.COMP_TECHNOLOGIST);
    }

    @Override
    public ProcessRoleResource getLeadApplicantProcessRoleOrNull(ApplicationResource application) {
        List<ProcessRoleResource> userApplicationRoles = processRoleService.getByApplicationId(application.getId());
        for(final ProcessRoleResource processRole : userApplicationRoles){
            if(processRole.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())){
                return processRole;
            }
        }
        return null;
    }

	@Override
	public List<ProcessRoleResource> getOrganisationProcessRoles(ApplicationResource application, Long organisation) {
		List<ProcessRoleResource> userApplicationRoles = processRoleService.getByApplicationId(application.getId());
		return userApplicationRoles.stream()
				.filter(prr -> organisation.equals(prr.getOrganisationId()))
				.collect(Collectors.toList());
	}

    @Override
	public List<ProcessRoleResource> getLeadPartnerOrganisationProcessRoles(ApplicationResource application) {
		ProcessRoleResource leadProcessRole = getLeadApplicantProcessRoleOrNull(application);
		if(leadProcessRole == null) {
			return new ArrayList<>();
		}
		return processRoleService.getByApplicationId(application.getId()).stream()
				.filter(pr -> leadProcessRole.getOrganisationId().equals(pr.getOrganisationId()))
				.collect(Collectors.toList());
	}

    @Override
    public Set<UserResource> getAssignableUsers(ApplicationResource application) {
        return userRestService.findAssignableUsers(application.getId()).andOnSuccessReturn(a -> new HashSet<>(a)).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<UserResource> createUserForOrganisation(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId, Boolean allowMarketingEmails) {
        return userRestService.createLeadApplicantForOrganisation(firstName, lastName, password, email, title, phoneNumber, null, null, null, organisationId, allowMarketingEmails).toServiceResult();
    }

    @Override
    public ServiceResult<UserResource> createLeadApplicantForOrganisationWithCompetitionId(String firstName, String lastName, String password, String email,
                                                                                           String title, String phoneNumber,
                                                                                           String gender, Long ethnicity, String disability,
                                                                                           Long organisationId, Long competitionId, Boolean allowMarketingEmails) {
        return userRestService.createLeadApplicantForOrganisationWithCompetitionId(firstName, lastName, password, email, title, phoneNumber, gender, ethnicity, disability, organisationId, competitionId, allowMarketingEmails).toServiceResult();
    }

    @Override
    public ServiceResult<UserResource> createOrganisationUser(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId, Boolean allowMarketingEmails) {
        return createUserForOrganisation(firstName, lastName, password, email, title, phoneNumber, organisationId, allowMarketingEmails);
    }

    @Override
    public ServiceResult<UserResource> updateDetails(Long id, String email, String firstName, String lastName, String title, String phoneNumber, String gender, Long ethnicity, String disability, boolean allowMarketingEmails) {
        return userRestService.updateDetails(id, email, firstName, lastName, title, phoneNumber, gender, ethnicity, disability, allowMarketingEmails).toServiceResult();
    }

    @Override
    public Long getUserOrganisationId(Long userId, Long applicationId) {
        ProcessRoleResource userApplicationRole = userRestService.findProcessRole(userId, applicationId).getSuccessObjectOrThrowException();
        return userApplicationRole.getOrganisationId();
    }

    @Override
    public List<UserResource> findUserByType(UserRoleType type) {
        return userRestService.findByUserRoleType(type).getSuccessObjectOrThrowException();
    }

    @Override
    public Void verifyEmail(String hash) {
        return userRestService.verifyEmail(hash).getSuccessObjectOrThrowException();
    }

    @Override
    public void resendEmailVerificationNotification(String email) {
        try {
            userRestService.resendEmailVerificationNotification(email).getSuccessObjectOrThrowException();
        }
        catch (ObjectNotFoundException e) {
            // Do nothing. We don't want to reveal that the address was not recognised
            LOG.debug(format("Purposely ignoring ObjectNotFoundException for email address: [%s] when resending email verification notification.", email));
        }
    }

    @Override
    public Boolean userHasApplicationForCompetition(Long userId, Long competitionId) {
        return userRestService.userHasApplicationForCompetition(userId, competitionId).getSuccessObjectOrThrowException();
    }

    @Override
    public UserResource retrieveUserById(Long id) {
        return userRestService.retrieveUserById(id).getSuccessObjectOrThrowException();
    }

    @Override
    public void sendPasswordResetNotification(String email) {
            userRestService.sendPasswordResetNotification(email);
    }

    @Override
    public Void checkPasswordResetHash(String hash) {
        return userRestService.checkPasswordResetHash(hash).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<Void> resetPassword(String hash, String password) {
        return userRestService.resetPassword(hash,password).toServiceResult();
    }

    @Override
    public Optional<UserResource> findUserByEmail(String email) {
        return userRestService.findUserByEmail(email).getOptionalSuccessObject();
    }

    private boolean isUserRoleType(Long userId, UserRoleType role) {
        RestResult<UserResource> result = userRestService.retrieveUserById(userId);

        if (result.isFailure()) {
            return false;
        }

        UserResource execUser = result.getSuccessObject();

        return execUser != null && execUser.hasRole(role);
    }
}
