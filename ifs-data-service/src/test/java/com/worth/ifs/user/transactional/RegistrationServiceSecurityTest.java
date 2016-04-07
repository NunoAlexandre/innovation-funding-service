package com.worth.ifs.user.transactional;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.security.FormInputResponseFileUploadLookupStrategies;
import com.worth.ifs.application.security.FormInputResponseFileUploadRules;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.security.UserLookupStrategies;
import com.worth.ifs.user.security.UserPermissionRules;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Testing how this service integrates with Spring Security
 */
public class RegistrationServiceSecurityTest extends BaseServiceSecurityTest<RegistrationService> {

    private UserPermissionRules rules;
    private UserLookupStrategies lookup;

    @Before
    public void lookupPermissionRules() {
        rules = getMockPermissionRulesBean(UserPermissionRules.class);
        lookup = getMockPermissionEntityLookupStrategiesBean(UserLookupStrategies.class);
    }

    @Test
    public void testCreateApplicantUser() {

        UserResource userToCreate = newUserResource().build();

        assertAccessDenied(() -> service.createApplicantUser(123L, userToCreate), () -> {
            verify(rules).systemUserCanCreateUsers(userToCreate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void testCreateApplicantUserWithCompetitionId() {

        UserResource userToCreate = newUserResource().build();

        assertAccessDenied(() -> service.createApplicantUser(123L, Optional.of(456L), userToCreate), () -> {
            verify(rules).systemUserCanCreateUsers(userToCreate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void testActivateUser() {

        UserResource userToActivate = newUserResource().build();

        when(lookup.findById(123L)).thenReturn(userToActivate);
        
        assertAccessDenied(() -> service.activateUser(123L), () -> {
            verify(rules).systemUserCanActivateUsers(userToActivate, getLoggedInUser());
            verifyNoMoreInteractions(rules);
        });
    }

    @Override
    protected Class<? extends RegistrationService> getServiceClass() {
        return TestRegistrationService.class;
    }

    private static class TestRegistrationService implements RegistrationService {

        @Override
        public ServiceResult<UserResource> createApplicantUser(Long organisationId, UserResource userResource) {
            return null;
        }

        @Override
        public ServiceResult<UserResource> createApplicantUser(Long organisationId, Optional<Long> competitionId, UserResource userResource) {
            return null;
        }

        @Override
        public ServiceResult<Void> activateUser(Long userId) {
            return null;
        }
    }
}