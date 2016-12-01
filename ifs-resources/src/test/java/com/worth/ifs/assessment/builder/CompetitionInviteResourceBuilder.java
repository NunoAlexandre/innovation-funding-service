package com.worth.ifs.assessment.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.Builder;
import com.worth.ifs.base.amend.BaseBuilderAmendFunctions;
import com.worth.ifs.invite.resource.CompetitionInviteResource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

/**
 * Builder for {@link com.worth.ifs.invite.resource.CompetitionInviteResource}
 */
public class CompetitionInviteResourceBuilder extends BaseBuilder<CompetitionInviteResource, CompetitionInviteResourceBuilder> {

    private CompetitionInviteResourceBuilder(List<BiConsumer<Integer, CompetitionInviteResource>> multiActions) {
        super(multiActions);
    }

    public static CompetitionInviteResourceBuilder newCompetitionInviteResource() {
        return new CompetitionInviteResourceBuilder(emptyList());
    }

    public CompetitionInviteResourceBuilder withIds(Long... ids) {
        return withArray(BaseBuilderAmendFunctions::setId, ids);
    }

    public CompetitionInviteResourceBuilder withCompetitionName(String... competitionNames) {
        return withArray((competitionName, inviteResource) -> inviteResource.setCompetitionName(competitionName), competitionNames);
    }

    public CompetitionInviteResourceBuilder withCompetitionName(Builder<String, ?> competitionName) {
        return withCompetitionName(competitionName.build());
    }

    public CompetitionInviteResourceBuilder withAcceptsDate(LocalDateTime... acceptsDates) {
        return withArray((acceptsDate, inviteResource) -> inviteResource.setAcceptsDate(acceptsDate), acceptsDates);
    }

    public CompetitionInviteResourceBuilder withAcceptsDate(Builder<LocalDateTime, ?> acceptsDate) {
        return withAcceptsDate(acceptsDate.build());
    }

    public CompetitionInviteResourceBuilder withDeadlineDate(LocalDateTime... deadlineDates) {
        return withArray((deadlineDate, inviteResource) -> inviteResource.setDeadlineDate(deadlineDate), deadlineDates);
    }

    public CompetitionInviteResourceBuilder withDeadlineDate(Builder<LocalDateTime, ?> deadlineDate) {
        return withDeadlineDate(deadlineDate.build());
    }

    public CompetitionInviteResourceBuilder withBriefingDate(LocalDateTime... briefingDates) {
        return withArray((briefingDate, inviteResource) -> inviteResource.setBriefingDate(briefingDate), briefingDates);
    }

    public CompetitionInviteResourceBuilder withBriefingDate(Builder<LocalDateTime, ?> briefingDate) {
        return withBriefingDate(briefingDate.build());
    }

    public CompetitionInviteResourceBuilder withAssessorPay(BigDecimal... assessorPays) {
        return withArray((assessorPay, inviteResource) -> inviteResource.setAssessorPay(assessorPay), assessorPays);
    }

    public CompetitionInviteResourceBuilder withAssessorPay(Builder<BigDecimal, ?> assessorPay) {
        return withAssessorPay(assessorPay.build());
    }

    public CompetitionInviteResourceBuilder withEmail(String... emails) {
        return withArray((email, inviteResource) -> inviteResource.setEmail(email), emails);
    }

    @Override
    protected CompetitionInviteResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionInviteResource>> actions) {
        return new CompetitionInviteResourceBuilder(actions);
    }

    @Override
    protected CompetitionInviteResource createInitial() {
        return new CompetitionInviteResource();
    }
}
