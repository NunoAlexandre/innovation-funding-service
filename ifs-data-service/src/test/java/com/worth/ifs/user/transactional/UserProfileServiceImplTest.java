package com.worth.ifs.user.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.Affiliation;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.AffiliationResource;
import com.worth.ifs.user.resource.ProfileResource;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.user.builder.AffiliationBuilder.newAffiliation;
import static com.worth.ifs.user.builder.AffiliationResourceBuilder.newAffiliationResource;
import static com.worth.ifs.user.builder.ProfileResourceBuilder.newProfileResource;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Tests around the User Profile Service
 */
public class UserProfileServiceImplTest extends BaseServiceUnitTest<UserProfileServiceImpl> {
    @Override
    protected UserProfileServiceImpl supplyServiceUnderTest() {
        return new UserProfileServiceImpl();
    }

    @Test
    public void testGetUserProfileSkills() {
        User existingUser = newUser().build();
        Profile profile = newProfile()
                .withUser(existingUser)
                .withAddress(newAddress().build())
                .withContract(newContract().build())
                .withBusinessType(ACADEMIC)
                .withSkillsAreas("Skills")
                .build();
        existingUser.setProfile(profile);

        when(userRepositoryMock.findOne(existingUser.getId())).thenReturn(existingUser);

        ProfileSkillsResource expected = newProfileSkillsResource()
                .withUser(existingUser.getId())
                .withBusinessType(ACADEMIC)
                .withSkillsAreas("Skills")
                .build();

        ProfileSkillsResource response = service.getProfileSkills(existingUser.getId()).getSuccessObject();
        assertEquals(expected, response);

        verify(userRepositoryMock).findOne(existingUser.getId());
        verifyNoMoreInteractions(userRepositoryMock);
    }

    @Test
    public void testUpdateProfileSkills() {
        Long userId = 1L;

        User existingUser = newUser().build();
        Profile profile = newProfile()
                .withUser(existingUser)
                .withAddress(newAddress().build())
                .withContract(newContract().build())
                .withBusinessType(ACADEMIC)
                .withSkillsAreas("Skills")
                .build();
        existingUser.setProfile(profile);

        when(userRepositoryMock.findOne(existingUser.getId())).thenReturn(existingUser);

        User expectedUser = createLambdaMatcher(
                user -> {
                    assertEquals(userId, user.getId());
                    assertEquals(existingUser.getProfile().getId(), user.getProfile().getId());
                    assertEquals(existingUser.getProfile().getUser(), user.getProfile().getUser().getId());
                    assertEquals(existingUser.getProfile().getAddress(), user.getProfile().getAddress());
                    assertEquals(existingUser.getProfile().getContract(), user.getProfile().getContract());
                    assertEquals(BUSINESS, user.getProfile().getBusinessType());
                    assertEquals("Updated", user.getProfile().getSkillsAreas());
                }
        );

        when(userRepositoryMock.save(expectedUser)).thenReturn(newUser().build());

        ServiceResult<Void> result = service.updateProfileSkills(existingUser.getId(), newProfileSkillsResource()
                .withUser(existingUser.getId())
                .withBusinessType(BUSINESS)
                .withSkillsAreas("Skills")
                .build());

        assertTrue(result.isSuccess());

        InOrder inOrder = inOrder(userRepositoryMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(userRepositoryMock).save(isA(User.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testUpdateProfileSkillsButUserNotFound() {
        Long userId = 1L;

        ServiceResult<Void> result = service.updateProfileSkills(userId, newProfileSkillsResource().build());
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(User.class, userId)));

        verify(userRepositoryMock).findOne(userId);
        verifyNoMoreInteractions(userRepositoryMock);
    }

    @Test
    public void updateUserAffiliations() throws Exception {
        Long userId = 1L;
        List<AffiliationResource> affiliationResources = newAffiliationResource().build(2);
        List<Affiliation> affiliations = newAffiliation().build(2);

        User existingUser = newUser()
                .withAffiliations(new ArrayList<>())
                .build();

        when(userRepositoryMock.findOne(userId)).thenReturn(existingUser);
        when(affiliationMapperMock.mapToDomain(affiliationResources)).thenReturn(affiliations);

        User userWithAffiliationsExpectation = createLambdaMatcher(user -> {
            assertEquals(affiliations, user.getAffiliations());
        });

        when(userRepositoryMock.save(userWithAffiliationsExpectation)).thenReturn(newUser().build());

        ServiceResult<Void> response = service.updateUserAffiliations(userId, affiliationResources);
        assertTrue(response.isSuccess());

        InOrder inOrder = inOrder(userRepositoryMock, affiliationMapperMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(affiliationMapperMock).mapToDomain(affiliationResources);
        inOrder.verify(userRepositoryMock).save(isA(User.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getUserAffiliations() throws Exception {
        Long userId = 1L;

        List<Affiliation> affiliations = newAffiliation().build(2);

        List<AffiliationResource> affiliationResources = newAffiliationResource().build(2);

        when(affiliationMapperMock.mapToResource(affiliations.get(0))).thenReturn(affiliationResources.get(0));
        when(affiliationMapperMock.mapToResource(affiliations.get(1))).thenReturn(affiliationResources.get(1));

        User user = newUser()
                .withAffiliations(affiliations)
                .build();

        when(userRepositoryMock.findOne(userId)).thenReturn(user);

        List<AffiliationResource> response = service.getUserAffiliations(userId).getSuccessObject();
        assertEquals(affiliationResources, response);

        InOrder inOrder = inOrder(userRepositoryMock, affiliationMapperMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(affiliationMapperMock, times(2)).mapToResource(isA(Affiliation.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void updateUserContract() throws Exception {
        Long userId = 1L;
        Long contractId = 2L;

        User user = newUser()
                .withId(userId)
                .withProfile(newProfile()
                        .build())
                .build();
        ProfileResource profileResource = newProfileResource()
                .withContract(newContractResource()
                        .withId(contractId)
                        .build())
                .build();

        Contract contract = newContract()
                .withId(contractId)
                .build();

        when(userRepositoryMock.findOne(1L)).thenReturn(user);
        when(contractRepositoryMock.findByCurrentTrue()).thenReturn(contract);

        ServiceResult<Void> result = service.updateUserContract(user.getId(), profileResource);

        assertTrue(result.isSuccess());
        InOrder inOrder = inOrder(userRepositoryMock, contractRepositoryMock, userRepositoryMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(contractRepositoryMock).findByCurrentTrue();
        inOrder.verify(userRepositoryMock).save(isA(User.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void updateUserContract_noContractResourceAttached() throws Exception {
        Long userId = 1L;
        Long contractId = 2L;

        User user = newUser()
                .withId(userId)
                .withProfile(newProfile()
                        .build())
                .build();
        ProfileResource profileResource = newProfileResource().build();

        Contract contract = newContract()
                .withId(contractId)
                .build();

        when(userRepositoryMock.findOne(1L)).thenReturn(user);
        when(contractRepositoryMock.findByCurrentTrue()).thenReturn(contract);

        ServiceResult<Void> result = service.updateUserContract(user.getId(), profileResource);

        verify(userRepositoryMock).findOne(userId);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), "Cannot sign without contract identifier present");
    }

    @Test
    public void updateUserContract_contractAlreadySigned() throws Exception {
        Long userId = 1L;
        Long contractId = 2L;

        User user = newUser()
                .withId(userId)
                .withProfile(newProfile()
                        .withContract(
                                newContract()
                                        .withId(contractId)
                                        .build()
                        )
                        .build())
                .build();
        ProfileResource profileResource = newProfileResource()
                .withContract(newContractResource()
                        .withId(contractId)
                        .build())
                .build();

        Contract contract = newContract()
                .withId(contractId)
                .build();

        when(userRepositoryMock.findOne(1L)).thenReturn(user);
        when(contractRepositoryMock.findByCurrentTrue()).thenReturn(contract);

        ServiceResult<Void> result = service.updateUserContract(user.getId(), profileResource);

        verify(userRepositoryMock).findOne(userId);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), "validation.assessorprofiletermsform.terms.alreadysigned");

    }

    @Test
    public void updateUserContract_contractNotCurrent() throws Exception {
        Long userId = 1L;
        Long profileResourceContractId = 2L;

        Long currentContractId = 3L;

        User user = newUser()
                .withId(userId)
                .withProfile(newProfile().build())
                .build();
        ProfileResource profileResource = newProfileResource()
                .withContract(newContractResource()
                        .withId(profileResourceContractId)
                        .build())
                .build();

        Contract contract = newContract()
                .withId(currentContractId)
                .build();

        when(userRepositoryMock.findOne(1L)).thenReturn(user);
        when(contractRepositoryMock.findByCurrentTrue()).thenReturn(contract);

        ServiceResult<Void> result = service.updateUserContract(user.getId(), profileResource);

        InOrder inOrder = inOrder(userRepositoryMock, contractRepositoryMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(contractRepositoryMock).findByCurrentTrue();
        inOrder.verifyNoMoreInteractions();

        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), "validation.assessorprofiletermsform.terms.oldterms");
    }

    @Test
    public void updateUserContract_userDoesNotExist() throws Exception {
        Long userId = 1L;
        Long profileResourceContractId = 2L;

        Long currentContractId = 3L;

        ProfileResource profileResource = newProfileResource().build();

        when(userRepositoryMock.findOne(userId)).thenReturn(null);

        ServiceResult<Void> result = service.updateUserContract(userId, profileResource);

        InOrder inOrder = inOrder(userRepositoryMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verifyNoMoreInteractions();

        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), GENERAL_NOT_FOUND.getErrorKey());
    }

    @Test
    public void updateUserContract_userDoesNotHaveProfileYet() throws Exception {
        Long userId = 1L;
        Long contractId = 2L;

        User user = newUser()
                .withId(userId)
                .build();
        ProfileResource profileResource = newProfileResource()
                .withContract(newContractResource()
                        .withId(contractId)
                        .build())
                .build();

        Contract contract = newContract()
                .withId(contractId)
                .build();

        when(userRepositoryMock.findOne(1L)).thenReturn(user);
        when(contractRepositoryMock.findByCurrentTrue()).thenReturn(contract);

        ServiceResult<Void> result = service.updateUserContract(user.getId(), profileResource);

        assertTrue(result.isSuccess());
        InOrder inOrder = inOrder(userRepositoryMock, contractRepositoryMock, userRepositoryMock);
        inOrder.verify(userRepositoryMock).findOne(userId);
        inOrder.verify(contractRepositoryMock).findByCurrentTrue();
        inOrder.verify(userRepositoryMock).save(isA(User.class));
        inOrder.verifyNoMoreInteractions();
    }
}
