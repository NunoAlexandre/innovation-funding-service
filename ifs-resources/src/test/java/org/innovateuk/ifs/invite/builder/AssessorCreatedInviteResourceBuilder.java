package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.invite.resource.AssessorCreatedInviteResource;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

public class AssessorCreatedInviteResourceBuilder extends BaseBuilder<AssessorCreatedInviteResource, AssessorCreatedInviteResourceBuilder> {

    private AssessorCreatedInviteResourceBuilder(List<BiConsumer<Integer, AssessorCreatedInviteResource>> newMultiActions) {
        super(newMultiActions);
    }

    @Override
    protected AssessorCreatedInviteResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorCreatedInviteResource>> actions) {
        return new AssessorCreatedInviteResourceBuilder(actions);
    }

    @Override
    protected AssessorCreatedInviteResource createInitial() {
        return new AssessorCreatedInviteResource();
    }

    public static AssessorCreatedInviteResourceBuilder newAssessorCreatedInviteResource() {
        return new AssessorCreatedInviteResourceBuilder(emptyList());
    }

    public AssessorCreatedInviteResourceBuilder withInviteId(Long... value) {
        return withArraySetFieldByReflection("inviteId", value);
    }

    public AssessorCreatedInviteResourceBuilder withFirstName(String... value) {
        return withArraySetFieldByReflection("firstName", value);
    }

    public AssessorCreatedInviteResourceBuilder withLastName(String... value) {
        return withArraySetFieldByReflection("lastName", value);
    }

    public AssessorCreatedInviteResourceBuilder withInnovationArea(CategoryResource... value) {
        return withArraySetFieldByReflection("innovationArea", value);
    }

    public AssessorCreatedInviteResourceBuilder withCompliant(Boolean... value) {
        return withArraySetFieldByReflection("compliant", value);
    }

    public AssessorCreatedInviteResourceBuilder withEmail(String... value) {
        return withArraySetFieldByReflection("email", value);
    }
}