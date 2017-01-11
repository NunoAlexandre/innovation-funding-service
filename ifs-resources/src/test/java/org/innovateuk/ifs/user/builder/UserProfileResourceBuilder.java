package org.innovateuk.ifs.user.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.user.resource.*;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.createDefault;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static java.util.Collections.emptyList;

public class UserProfileResourceBuilder extends UserProfileBaseResourceBuilder<UserProfileResource, UserProfileResourceBuilder> {

    private UserProfileResourceBuilder(List<BiConsumer<Integer, UserProfileResource>> multiActions) {
        super(multiActions);
    }

    public static UserProfileResourceBuilder newUserProfileResource() {
        return new UserProfileResourceBuilder(emptyList());
    }

    @Override
    protected UserProfileResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, UserProfileResource>> actions) {
        return new UserProfileResourceBuilder(actions);
    }

    @Override
    protected UserProfileResource createInitial() {
        return createDefault(UserProfileResource.class);
    }

    public UserProfileResourceBuilder withUser(Long... users) {
        return withArray((user, userProfileResource) -> setField("user", user, userProfileResource), users);
    }
}
