package org.innovateuk.ifs.invite.builder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.email.resource.EmailContent;
import org.innovateuk.ifs.invite.resource.AssessorInviteToSendResource;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

public class AssessorInviteToSendResourceBuilder extends BaseBuilder<AssessorInviteToSendResource, AssessorInviteToSendResourceBuilder> {

    private AssessorInviteToSendResourceBuilder(List<BiConsumer<Integer, AssessorInviteToSendResource>> actions) {
        super(actions);
    }

    @Override
    protected AssessorInviteToSendResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessorInviteToSendResource>> actions) {
        return new AssessorInviteToSendResourceBuilder(actions);
    }

    @Override
    protected AssessorInviteToSendResource createInitial() {
        return new AssessorInviteToSendResource();
    }

    public static AssessorInviteToSendResourceBuilder newAssessorInviteToSendResource() {
        return new AssessorInviteToSendResourceBuilder(emptyList());
    }

    public AssessorInviteToSendResourceBuilder withRecipient(String... value) {
        return withArraySetFieldByReflection("recipient", value);
    }

    public AssessorInviteToSendResourceBuilder withCompetitionId(Long... value) {
        return withArraySetFieldByReflection("competitionId", value);
    }

    public AssessorInviteToSendResourceBuilder withCompetitionName(String... value) {
        return withArraySetFieldByReflection("competitionName", value);
    }

    public AssessorInviteToSendResourceBuilder withEmailSubject(String... value) {
        return withArraySetFieldByReflection("emailSubject", value);
    }

    public AssessorInviteToSendResourceBuilder withEmailContent(EmailContent... value) {
        return withArraySetFieldByReflection("emailContent", value);
    }

}
