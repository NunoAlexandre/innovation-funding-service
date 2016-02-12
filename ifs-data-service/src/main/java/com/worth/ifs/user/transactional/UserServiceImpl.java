package com.worth.ifs.user.transactional;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.user.repository.OrganisationRepository;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.repository.UserRepository;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.util.EntityLookupCallbacks;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.worth.ifs.commons.error.CommonFailureKeys.USERS_DUPLICATE_EMAIL_ADDRESS;
import static com.worth.ifs.commons.error.Errors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.*;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.stream.Collectors.toSet;

/**
 * A Service that covers basic operations concerning Users
 */
@Service
public class UserServiceImpl extends BaseTransactionalService implements UserService {

    private static final Log LOG = LogFactory.getLog(UserServiceImpl.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public ServiceResult<User> getUserByToken(final String token) {
        return find(repository.findByToken(token), notFoundError(User.class, token)).
                andOnSuccess(EntityLookupCallbacks::getOnlyElementOrFail);
    }

    @Override
    public ServiceResult<User> getUserByEmailandPassword(final String email, final String password) {

        return find(repository.findByEmail(email), notFoundError(User.class, email)).
                andOnSuccess(EntityLookupCallbacks::getOnlyElementOrFail).
                andOnSuccess(user -> user.passwordEquals(password) ? serviceSuccess(user) : serviceFailure(notFoundError(User.class)));
    }

    @Override
    public ServiceResult<User> getUserById(final Long id) {
        return super.getUser(id);
    }

    @Override
    public ServiceResult<List<User>> getUserByName(final String name) {
        return find(repository.findByName(name), notFoundError(User.class, name));
    }

    @Override
    public ServiceResult<List<User>> findAll() {
        return serviceSuccess(repository.findAll());
    }

    @Override
    public ServiceResult<List<UserResource>> findByEmail(final String email) {
        List<User> users = repository.findByEmail(email);
        return serviceSuccess(users.stream().map(UserResource::new).collect(Collectors.toList()));
    }

    @Override
    public ServiceResult<Set<User>> findAssignableUsers(final Long applicationId) {

        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);
        Set<User> assignables = roles.stream()
                .filter(r -> r.getRole().getName().equals("leadapplicant") || r.getRole().getName().equals("collaborator"))
                .map(ProcessRole::getUser)
                .collect(toSet());

        return serviceSuccess(assignables);
    }

    @Override
    public ServiceResult<Set<User>> findRelatedUsers(final Long applicationId) {

        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);

        Set<User> related = roles.stream()
                .map(ProcessRole::getUser)
                .collect(toSet());

        return serviceSuccess(related);
    }

    @Override
    public ServiceResult<UserResource> createUser(final Long organisationId, UserResource userResource) {

        User newUser = assembleUserFromResource(userResource);
        addOrganisationToUser(newUser, organisationId);
        addRoleToUser(newUser, UserRoleType.APPLICANT.getName());

        if (repository.findByEmail(userResource.getEmail()).isEmpty()) {
            UserResource createdUserResource = createUserWithToken(newUser);
            return serviceSuccess(createdUserResource);
        } else {
            return serviceFailure(new Error(USERS_DUPLICATE_EMAIL_ADDRESS, userResource.getEmail()));
        }
    }

    public ServiceResult<UserResource> updateUser(UserResource userResource) {
        List<User> existingUser = repository.findByEmail(userResource.getEmail());
        if (existingUser == null || existingUser.size() <= 0) {
            LOG.error("User with email " + userResource.getEmail() + " doesn't exist!");
            return serviceFailure(notFoundError(User.class, userResource.getEmail()));
        }
        User newUser = createUser(existingUser.get(0), userResource);
        UserResource updatedUser = createUser(newUser);
        return serviceSuccess(updatedUser);
    }

    private UserResource createUserWithToken(User user) {
        User createdUser = repository.save(user);
        User createdUserWithToken = addTokenBasedOnIdToUser(createdUser);
        User finalUser = repository.save(createdUserWithToken);
        return new UserResource(finalUser);
    }

    private User createUser(User existingUser, UserResource updatedUserResource) {
        existingUser.setPhoneNumber(updatedUserResource.getPhoneNumber());
        existingUser.setTitle(updatedUserResource.getTitle());
        existingUser.setLastName(updatedUserResource.getLastName());
        existingUser.setFirstName(updatedUserResource.getFirstName());
        return existingUser;
    }

    private UserResource createUser(User user) {
        User savedUser = repository.save(user);
        return new UserResource(savedUser);
    }

    private void addRoleToUser(User user, String roleName) {
        List<Role> userRoles = roleRepository.findByName(roleName);
        user.setRoles(userRoles);
    }

    private void addOrganisationToUser(User user, Long organisationId) {
        Organisation userOrganisation = organisationRepository.findOne(organisationId);
        List<Organisation> userOrganisationList = new ArrayList<>();
        userOrganisationList.add(userOrganisation);
        user.setOrganisations(userOrganisationList);
    }

    private User assembleUserFromResource(UserResource userResource) {
        User newUser = new User();
        newUser.setFirstName(userResource.getFirstName());
        newUser.setLastName(userResource.getLastName());
        newUser.setPassword(userResource.getPassword());
        newUser.setEmail(userResource.getEmail());
        newUser.setTitle(userResource.getTitle());
        newUser.setPhoneNumber(userResource.getPhoneNumber());

        String fullName = concatenateFullName(userResource.getFirstName(), userResource.getLastName());
        newUser.setName(fullName);

        return newUser;
    }

    private String concatenateFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    private User addTokenBasedOnIdToUser(User user) {
        String userToken = user.getId() + "abc123";
        user.setToken(userToken);
        return user;
    }
}
