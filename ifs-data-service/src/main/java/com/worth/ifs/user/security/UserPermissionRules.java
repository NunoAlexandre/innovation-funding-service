package com.worth.ifs.user.security;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

import static com.worth.ifs.user.domain.UserRoleType.*;
import static com.worth.ifs.util.CollectionFunctions.*;
import static java.util.Arrays.asList;

/**
 * Permission rules that determines who can perform CRUD operations based around Users.
 */
@Component
@PermissionRules
public class UserPermissionRules {

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    private static List<String> CONSORTIUM_ROLES = asList(LEADAPPLICANT.getName(), COLLABORATOR.getName());

    private static Predicate<ProcessRole> consortiumProcessRoleFilter = role -> CONSORTIUM_ROLES.contains(role.getRole().getName());

    private static Predicate<ProcessRole> assessorProcessRoleFilter = role -> role.getRole().getName().equals(ASSESSOR);

    @PermissionRule(value = "CREATE", description = "A System Registration User can create new Users")
    public boolean systemRegistrationUserCanCreateUsers(UserResource userToCreate, UserResource user) {
        return isSystemRegistrationUser(user);
    }

    @PermissionRule(value = "READ", description = "Any user can view themselves")
    public boolean anyUserCanViewThemselves(UserResource userToView, UserResource user) {
        return userToView.getId().equals(user.getId());
    }

    @PermissionRule(value = "READ", description = "Comp Admins can view everyone")
    public boolean compAdminsCanViewEveryone(UserResource userToView, UserResource user) {
        return user.hasRole(COMP_ADMIN);
    }

    @PermissionRule(value = "READ", description = "The System Registration user can view everyone")
    public boolean systemRegistrationUserCanViewEveryone(UserResource userToView, UserResource user) {
        return isSystemRegistrationUser(user);
    }

    @PermissionRule(value = "READ", description = "Consortium members (Lead Applicants and Collaborators) can view the others in their Consortium Teams on their various Applications")
    public boolean consortiumMembersCanViewOtherConsortiumMembers(UserResource userToView, UserResource user) {
        List<Long> applicationRoles = user.getProcessRoles();
        List<ProcessRole> processRoles = simpleMap(applicationRoles, processRoleRepository::findOne);
        List<ProcessRole> processRolesWhereUserIsOnConsortium = simpleFilter(processRoles, consortiumProcessRoleFilter);
        List<Application> applicationsWhereThisUserIsInConsortium = simpleMap(processRolesWhereUserIsOnConsortium, ProcessRole::getApplication);
        List<ProcessRole> otherProcessRolesForThoseApplications = flattenLists(simpleMap(applicationsWhereThisUserIsInConsortium, Application::getProcessRoles));
        List<ProcessRole> allConsortiumProcessRoles = simpleFilter(otherProcessRolesForThoseApplications, consortiumProcessRoleFilter);
        List<User> allConsortiumUsers = simpleMap(allConsortiumProcessRoles, ProcessRole::getUser);
        return simpleMap(allConsortiumUsers, User::getId).contains(userToView.getId());
    }

    @PermissionRule(value = "READ", description = "Assessors can view users on the Applications that they are assessing")
    public boolean assessorsCanViewUsersOnTheApplicationsTheyAreAssessing(UserResource userToView, UserResource user) {
        List<Long> applicationRoles = user.getProcessRoles();
        List<ProcessRole> processRoles = simpleMap(applicationRoles, processRoleRepository::findOne);
        List<ProcessRole> processRolesWhereUserIsAssessor = simpleFilter(processRoles, assessorProcessRoleFilter);
        List<Application> applicationsThatThisUserIsAssessing = simpleMap(processRolesWhereUserIsAssessor, ProcessRole::getApplication);
        List<ProcessRole> processRolesForAllApplications = flattenLists(simpleMap(applicationsThatThisUserIsAssessing, Application::getProcessRoles));
        List<ProcessRole> allConsortiumProcessRoles = simpleFilter(processRolesForAllApplications, consortiumProcessRoleFilter);
        List<User> allConsortiumUsers = simpleMap(allConsortiumProcessRoles, ProcessRole::getUser);
        return simpleMap(allConsortiumUsers, User::getId).contains(userToView.getId());
    }

    @PermissionRule(value = "CHANGE_PASSWORD", description = "A User should be able to change their own password")
    public boolean anyoneCanChangeTheirOwnPassword(UserResource userToUpdate, UserResource user) {
        return userToUpdate.getId().equals(user.getId());
    }

    @PermissionRule(value = "CHANGE_PASSWORD", description = "The System Registration user should be able to change passwords on behalf of other Users")
    public boolean systemRegistrationUserCanChangePasswordsForUsers(UserResource userToUpdate, UserResource user) {
        return isSystemRegistrationUser(user);
    }

    @PermissionRule(value = "ACTIVATE", description = "A System Registration User can activate Users")
    public boolean systemRegistrationUserCanActivateUsers(UserResource userToCreate, UserResource user) {
        return isSystemRegistrationUser(user);
    }

    @PermissionRule(value = "UPDATE", description = "A User can update their own profile")
    public boolean usersCanUpdateTheirOwnProfiles(UserResource userToUpdate, UserResource user) {
        return userToUpdate.getId().equals(user.getId());
    }

    private boolean isSystemRegistrationUser(UserResource user) {
        return user.hasRole(SYSTEM_REGISTRATION_USER);
    }
}
