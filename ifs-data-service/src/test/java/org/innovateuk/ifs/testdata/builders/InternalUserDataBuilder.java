package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.testdata.builders.data.InternalUserData;
import org.innovateuk.ifs.user.domain.CompAdminEmail;
import org.innovateuk.ifs.user.domain.ProjectFinanceEmail;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserRoleType;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

/**
 * Generates internal users (Comp Admins, Project Finance, Comp Execs and Comp Technologists)
 */
public class InternalUserDataBuilder extends BaseUserDataBuilder<InternalUserData, InternalUserDataBuilder> {

    @Override
    public InternalUserDataBuilder registerUser(String firstName, String lastName, String emailAddress, String organisationName, String phoneNumber) {
        return with(data -> {

            doAs(systemRegistrar(), () ->
                    registerUser(firstName, lastName, data.getEmailAddress(), organisationName, phoneNumber, data.getRole(), data));
        });
    }

    @Override
    public InternalUserDataBuilder createUserDirectly(String firstName, String lastName, String emailAddress, String organisationName, String phoneNumber) {
        return with(data -> {

            User user = userRepository.save(new User(firstName, lastName, emailAddress, null, UUID.randomUUID().toString()));
            Role role = roleRepository.findOneByName(data.getRole().getName());
            user.getRoles().add(role);
            userRepository.save(user);
        });
    }

    public static InternalUserDataBuilder newInternalUserData(ServiceLocator serviceLocator) {

        return new InternalUserDataBuilder(emptyList(), serviceLocator);
    }

    private InternalUserDataBuilder(List<BiConsumer<Integer, InternalUserData>> multiActions,
                                    ServiceLocator serviceLocator) {

        super(multiActions, serviceLocator);
    }

    @Override
    protected InternalUserDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, InternalUserData>> actions) {
        return new InternalUserDataBuilder(actions, serviceLocator);
    }

    @Override
    protected InternalUserData createInitial() {
        return new InternalUserData();
    }

    public InternalUserDataBuilder withRole(UserRoleType role) {
        return with(data -> {
           data.setRole(role);
        });
    }

    public InternalUserDataBuilder createPreRegistrationEntry(String emailAddress) {
        return with(data -> {
            switch (data.getRole()) {
                case COMP_ADMIN: {
                    CompAdminEmail preregistrationEntry = new CompAdminEmail();
                    preregistrationEntry.setEmail(emailAddress);
                    compAdminEmailRepository.save(preregistrationEntry);
                }
                case PROJECT_FINANCE: {
                    ProjectFinanceEmail preregistrationEntry = new ProjectFinanceEmail();
                    preregistrationEntry.setEmail(emailAddress);
                    projectFinanceEmailRepository.save(preregistrationEntry);
                }
                default: {
                    // no pre-reg entry
                }
            }

            data.setEmailAddress(emailAddress);
        });
    }
}
