package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.Builder;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.invite.domain.ApplicationInvite;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.user.domain.User;

import java.util.List;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Collections.emptyList;

public class ApplicationInviteBuilder extends BaseBuilder<ApplicationInvite, ApplicationInviteBuilder> {

    private ApplicationInviteBuilder(List<BiConsumer<Integer, ApplicationInvite>> multiActions) {
        super(multiActions);
    }

    public static ApplicationInviteBuilder newInvite() {
        return new ApplicationInviteBuilder(emptyList()).with(uniqueIds());
    }

    public static ApplicationInviteBuilder newApplicationInvite() {
        return new ApplicationInviteBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ApplicationInviteBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ApplicationInvite>> actions) {
        return new ApplicationInviteBuilder(actions);
    }

    public ApplicationInviteBuilder withId(Long... ids) {
        return withArray((id, invite) -> invite.setId(id), ids);
    }

    public ApplicationInviteBuilder withApplication(Builder<Application, ?> application) {
        return withApplication(application.build());
    }

    public ApplicationInviteBuilder withApplication(Application... applications) {
        return withArray((application, invite) -> invite.setTarget(application), applications);
    }

    public ApplicationInviteBuilder withUser(User... users) {
        return withArray((user, invite) -> invite.setUser(user), users);
    }

    public ApplicationInviteBuilder withInviteOrganisation(InviteOrganisation... organisations) {
        return withArray((organisation, invite) -> invite.setInviteOrganisation(organisation), organisations);
    }

    @Override
    public void postProcess(int index, ApplicationInvite invite) {

        // add back-refs to InviteOrganisations
        InviteOrganisation inviteOrganisation = invite.getInviteOrganisation();
        if (inviteOrganisation != null && !simpleMap(inviteOrganisation.getInvites(), ApplicationInvite::getId).contains(invite.getId())) {
            inviteOrganisation.getInvites().add(invite);
        }
    }

    @Override
    protected ApplicationInvite createInitial() {
        return new ApplicationInvite();
    }
}
