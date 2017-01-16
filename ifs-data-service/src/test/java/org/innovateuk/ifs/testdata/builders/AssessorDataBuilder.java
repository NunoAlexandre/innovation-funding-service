package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.testdata.builders.data.AssessorData;
import org.innovateuk.ifs.user.domain.Ethnicity;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.registration.builder.UserRegistrationResourceBuilder.newUserRegistrationResource;
import static org.innovateuk.ifs.testdata.builders.AssessorInviteDataBuilder.newAssessorInviteData;
import static org.innovateuk.ifs.user.builder.EthnicityResourceBuilder.newEthnicityResource;
import static org.innovateuk.ifs.user.resource.UserRoleType.ASSESSOR;

/**
 * Generates data for an Assessor on the platform
 */
public class AssessorDataBuilder extends BaseDataBuilder<AssessorData, AssessorDataBuilder> {

    public AssessorDataBuilder registerUser(String firstName,
                                            String lastName,
                                            String emailAddress,
                                            String phoneNumber,
                                            String ethnicity,
                                            Gender gender,
                                            Disability disability,
                                            String hash
    ) {
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

            assessorService.registerAssessorByHash(hash, registration).getSuccessObjectOrThrowException();

            data.setEmail(emailAddress);
        }));
    }

    public AssessorDataBuilder withInviteToAssessCompetition(String competitionName,
                                                             String emailAddress,
                                                             String name,
                                                             String inviteHash,
                                                             Optional<User> existingUser,
                                                             String innovationAreaName
    ) {
        return with(data -> {
            newAssessorInviteData(serviceLocator).withInviteToAssessCompetition(
                    competitionName,
                    emailAddress,
                    name,
                    inviteHash,
                    existingUser,
                    innovationAreaName
            ).build();
            data.setEmail(emailAddress);
        });
    }


    public AssessorDataBuilder addAssessorRoleAndInnovationAreas(List<String> innovationAreas) {
        return with((AssessorData data) -> {
            User user = userRepository.findByEmail(data.getEmail()).get();

            Set<InnovationArea> userInnovationAreas = innovationAreas.stream()
                    .map(innovationAreaName -> {
                        InnovationArea innovationArea = innovationAreaRepository.findByName(innovationAreaName);

                        if (innovationArea == null) {
                            throw new IllegalArgumentException("Invalid innovation area '" + innovationAreaName + "' for assessor user");
                        }

                        return innovationArea;
                    })
                    .collect(toSet());

            user.addInnovationAreas(userInnovationAreas);

            Role assessorRole = roleRepository.findOneByName(UserRoleType.ASSESSOR.getName());

            if (!user.getRoles().contains(assessorRole)) {
                user.getRoles().add(assessorRole);
            }

            userRepository.save(user);
        });
    }

    public AssessorDataBuilder acceptInvite(String hash) {
        return with(data -> newAssessorInviteData(serviceLocator).acceptInvite(hash, data.getEmail()).build());
    }

    public AssessorDataBuilder rejectInvite(String hash, String rejectionReason, String rejectionComment) {
        return with(data -> newAssessorInviteData(serviceLocator).rejectInvite(
                hash,
                data.getEmail(),
                rejectionReason,
                Optional.of(rejectionComment)
        )
                .build());
    }

    private RoleResource getAssessorRoleResource() {
        return roleService.findByUserRoleType(ASSESSOR).getSuccessObjectOrThrowException();
    }


    public static AssessorDataBuilder newAssessorData(ServiceLocator serviceLocator) {
        return new AssessorDataBuilder(emptyList(), serviceLocator);
    }

    private AssessorDataBuilder(List<BiConsumer<Integer, AssessorData>> multiActions,
                                ServiceLocator serviceLocator
    ) {
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
