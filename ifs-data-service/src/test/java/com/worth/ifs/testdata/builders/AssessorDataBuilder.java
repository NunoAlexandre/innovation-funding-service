package com.worth.ifs.testdata.builders;

import com.worth.ifs.registration.resource.UserRegistrationResource;
import com.worth.ifs.testdata.builders.data.AssessorData;
import com.worth.ifs.user.domain.Ethnicity;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.*;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.worth.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static com.worth.ifs.testdata.builders.AssessorInviteDataBuilder.newAssessorInviteData;
import static com.worth.ifs.user.builder.EthnicityResourceBuilder.newEthnicityResource;
import static com.worth.ifs.user.resource.UserRoleType.ASSESSOR;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Generates data for an Assessor on the platform
 */
public class AssessorDataBuilder extends BaseDataBuilder<AssessorData, AssessorDataBuilder> {

    public AssessorDataBuilder registerUser(String firstName, String lastName, String emailAddress, String phoneNumber, String ethnicity, Gender gender, Disability disability, String hash) {

        return with(data -> doAs(systemRegistrar(), () -> {

            EthnicityResource ethnicityResource;

            if (!isBlank(ethnicity)) {
                Ethnicity ethnicitySelected = ethnicityRepository.findOneByDescription(ethnicity);
                ethnicityResource = newEthnicityResource().withId(ethnicitySelected.getId()).build();
            } else {
                ethnicityResource = newEthnicityResource().withId().build();
            }

            UserRegistrationResource registration = newUserRegistrationResource().
                    withFirstName(firstName).
                    withLastName(lastName).
                    withEmail(emailAddress).
                    withPhoneNumber(phoneNumber).
                    withEthnicity(ethnicityResource).
                    withDisability(disability).
                    withGender(gender).
                    withPassword("Passw0rd").
                    withRoles(singletonList(getAssessorRoleResource())).
                    build();

//            assessorService.registerAssessorByHash(hash, registration).getSuccessObjectOrThrowException();
            registrationService.createUser(registration).andOnSuccess(created ->
                    registrationService.activateUser(created.getId())).getSuccessObjectOrThrowException();

            data.setEmail(emailAddress);
        }));
    }

    public AssessorDataBuilder withInviteToAssessCompetition(String competitionName, String emailAddress, String name, String inviteHash, Optional<User> existingUser) {
        return with(data -> {
            newAssessorInviteData(serviceLocator).withInviteToAssessCompetition(competitionName, emailAddress, name, inviteHash, existingUser).build();
            data.setEmail(emailAddress);
        });
    }


    public AssessorDataBuilder addAssessorRole() {
        return with(data -> {
            User user = userRepository.findByEmail(data.getEmail()).get();
            Role assessorRole = roleRepository.findOneByName(UserRoleType.ASSESSOR.getName());

            if (!user.getRoles().contains(assessorRole)) {
                user.getRoles().add(assessorRole);
                userRepository.save(user);
            }
        });
    }

    public AssessorDataBuilder acceptInvite(String hash) {
        return with(data -> newAssessorInviteData(serviceLocator).acceptInvite(hash, data.getEmail()).build());
    }

    private RoleResource getAssessorRoleResource() {
        return roleService.findByUserRoleType(ASSESSOR).getSuccessObjectOrThrowException();
    }


    public static AssessorDataBuilder newAssessorData(ServiceLocator serviceLocator) {
        return new AssessorDataBuilder(emptyList(), serviceLocator);
    }

    private AssessorDataBuilder(List<BiConsumer<Integer, AssessorData>> multiActions,
                                ServiceLocator serviceLocator) {
        super(multiActions, serviceLocator);
    }

    @Override
    protected AssessorDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorData>> actions) {
        return new AssessorDataBuilder(actions, serviceLocator);
    }

    @Override
    protected AssessorData createInitial() {
        return new AssessorData();
    }
}
