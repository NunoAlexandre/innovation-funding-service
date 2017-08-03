package org.innovateuk.ifs.admin.security;


import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.resource.UserStatus;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class UserPermissionRulesTest extends BasePermissionRulesTest<UserPermissionRules> {

    @Override
    protected UserPermissionRules supplyPermissionRulesUnderTest() {
        return new UserPermissionRules();
    }

    @Test
    public void activeUserCanBeEdited() {
        RoleResource role = newRoleResource().withType(UserRoleType.PROJECT_FINANCE).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        UserResource editUser = newUserResource().withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(editUser);
        assertTrue(rules.activeUsersCanBeEdited(1L, user));
    }

    @Test
    public void projectFinanceUserCanBeAccessed() {
        RoleResource role = newRoleResource().withType(UserRoleType.PROJECT_FINANCE).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(1L, user));
    }

    @Test
    public void compAdminUserCanBeAccessed() {
        RoleResource role = newRoleResource().withType(UserRoleType.COMP_ADMIN).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(1L, user));
    }

    @Test
    public void supportUserCanBeAccessed() {
        RoleResource role = newRoleResource().withType(UserRoleType.SUPPORT).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(1L, user));
    }

    @Test
    public void innovationLeadUserCanBeAccessed() {
        RoleResource role = newRoleResource().withType(UserRoleType.INNOVATION_LEAD).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.internalUser(1L, user));
    }

    @Test
    public void projectFinanceUserCanBeEdited() {
        RoleResource role = newRoleResource().withType(UserRoleType.PROJECT_FINANCE).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(1L, user));
    }

    @Test
    public void compAdminUserCanBeEdited() {
        RoleResource role = newRoleResource().withType(UserRoleType.COMP_ADMIN).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(1L, user));
    }

    @Test
    public void supportUserCanBeEdited() {
        RoleResource role = newRoleResource().withType(UserRoleType.SUPPORT).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(1L, user));
    }

    @Test
    public void innovationLeadUserCanBeEdited() {
        RoleResource role = newRoleResource().withType(UserRoleType.INNOVATION_LEAD).build();
        UserResource user = newUserResource().withRolesGlobal(singletonList(role)).withStatus(UserStatus.ACTIVE).build();
        when(userServiceMock.findById(1L)).thenReturn(user);
        assertTrue(rules.canEditInternalUser(1L, user));
    }
}
