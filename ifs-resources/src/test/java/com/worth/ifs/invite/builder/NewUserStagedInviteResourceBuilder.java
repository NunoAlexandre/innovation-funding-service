package com.worth.ifs.invite.builder;

import com.worth.ifs.invite.resource.NewUserStagedInviteResource;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

public class NewUserStagedInviteResourceBuilder extends StagedInviteResourceBuilder<NewUserStagedInviteResource, NewUserStagedInviteResourceBuilder> {

    private NewUserStagedInviteResourceBuilder(List<BiConsumer<Integer, NewUserStagedInviteResource>> newActions) {
        super(newActions);
    }

    public static NewUserStagedInviteResourceBuilder newNewUserStagedInviteResource() {
        return new NewUserStagedInviteResourceBuilder(emptyList());
    }

    @Override
    protected NewUserStagedInviteResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, NewUserStagedInviteResource>> actions) {
        return new NewUserStagedInviteResourceBuilder(actions);
    }

    @Override
    protected NewUserStagedInviteResource createInitial() {
        return new NewUserStagedInviteResource();
    }

    public NewUserStagedInviteResourceBuilder withName(String... names) {
        return withArraySetFieldByReflection("name", names);
    }

    public NewUserStagedInviteResourceBuilder withInnovationCategoryId(Long... innovationCategoryIds) {
        return withArraySetFieldByReflection("innovationCategoryId", innovationCategoryIds);
    }
}