package com.worth.ifs.application.service;

import com.worth.ifs.application.model.UserApplicationRole;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.resource.ResourceEnvelope;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.worth.ifs.util.CollectionFunctions.simpleMap;

/**
 * This class contains methods to retrieve and store {@link User} related data,
 * through the RestService {@link UserRestService}.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRestService userRestService;

    @Autowired
    ProcessRoleService processRoleService;

    @Override
    // TODO DW - INFUND-1555 - get service to return RestResult
    public List<User> getAssignable(Long applicationId) {
        return userRestService.findAssignableUsers(applicationId).getSuccessObject();
    }

    @Override
    public Boolean isLeadApplicant(Long userId, ApplicationResource application) {
        List<ProcessRole> userApplicationRoles = simpleMap(application.getProcessRoles(),id -> processRoleService.getById(id));
        return userApplicationRoles.stream().anyMatch(uar -> uar.getRole().getName()
                .equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()) && uar.getUser().getId().equals(userId));

    }

    @Override
    public ProcessRole getLeadApplicantProcessRoleOrNull(ApplicationResource application) {
        List<ProcessRole> userApplicationRoles = simpleMap(application.getProcessRoles(),id -> processRoleService.getById(id));
        for(final ProcessRole processRole : userApplicationRoles){
            if(processRole.getRole().getName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())){
                return processRole;
            }
        }
        return null;
    }

    @Override
    public Set<User> getAssignableUsers(ApplicationResource application) {
        List<ProcessRole> userApplicationRoles = application.getProcessRoles().stream()
            .map(id -> processRoleService.getById(id))
            .collect(Collectors.toList());
        return userApplicationRoles.stream()
                .filter(uar -> uar.getRole().getName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()) || uar.getRole().getName().equals(UserApplicationRole.COLLABORATOR.getRoleName()))
                .map(ProcessRole::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<User> getApplicationUsers(ApplicationResource application) {
        List<ProcessRole> userApplicationRoles = application.getProcessRoles().stream()
            .map(id -> processRoleService.getById(id))
            .collect(Collectors.toList());
        return userApplicationRoles.stream()
                .map(ProcessRole::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    // TODO DW - INFUND-1555 - get service to return RestResult
    public ResourceEnvelope<UserResource> createLeadApplicantForOrganisation(String firstName, String lastName, String password, String email, String title, String phoneNumber, Long organisationId) {
        ResourceEnvelope<UserResource> userResourceResourceStatusEnvelope = userRestService.createLeadApplicantForOrganisation(firstName, lastName, password, email, title, phoneNumber, organisationId).getSuccessObject();
        return userResourceResourceStatusEnvelope;
    }

    @Override
    // TODO DW - INFUND-1555 - get service to return RestResult
    public ResourceEnvelope<UserResource> updateDetails(String email, String firstName, String lastName, String title, String phoneNumber) {
        return userRestService.updateDetails(email, firstName, lastName, title, phoneNumber).getSuccessObject();
    }

    @Override
    // TODO DW - INFUND-1555 - get service to return RestResult
    public List<UserResource> findUserByEmail(String email) {
        return userRestService.findUserByEmail(email).getSuccessObject();
    }
}
