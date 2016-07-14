package com.worth.ifs.application.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.competition.resource.CompetitionResource;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

public class ApplicationResourceBuilder extends BaseBuilder<ApplicationResource, ApplicationResourceBuilder> {

    private ApplicationResourceBuilder(List<BiConsumer<Integer, ApplicationResource>> multiActions) {
        super(multiActions);
    }

    public static ApplicationResourceBuilder newApplicationResource() {
        return new ApplicationResourceBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected ApplicationResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, ApplicationResource>> actions) {
        return new ApplicationResourceBuilder(actions);
    }

    @Override
    protected ApplicationResource createInitial() {
        return new ApplicationResource();
    }

    public ApplicationResourceBuilder withId(Long... ids) {
        return withArray((id, application) -> setField("id", id, application), ids);
    }

    public ApplicationResourceBuilder withCompetition(Long... competitionIds) {
        return withArray((competition, application) -> setField("competition", competition, application), competitionIds);
    }

    public ApplicationResourceBuilder withCompetitionStatus(CompetitionResource.Status... competitionStatus) {
        return withArray((status, application) -> application.setCompetitionStatus(status), competitionStatus);
    }

    public ApplicationResourceBuilder withApplicationStatus(ApplicationStatusConstants... applicationStatus) {
        return withArray((applicationState, application) -> application.setApplicationStatusConstant(applicationState), applicationStatus);
    }
    public ApplicationResourceBuilder withApplicationStatus(Long... applicationStatus) {
        return withArray((applicationState, application) -> application.setApplicationStatus(applicationState), applicationStatus);
    }

    public ApplicationResourceBuilder withStartDate(LocalDate... dates) {
        return withArray((date, application) -> application.setStartDate(date), dates);
    }

    public ApplicationResourceBuilder withProcessRoles(List<Long>... processRolesLists) {
        return withArray((processRoles, application) -> application.setProcessRoles(processRoles), processRolesLists);
    }

    public ApplicationResourceBuilder withName(String name) {
        return with(application -> application.setName(name));
    }

    public ApplicationResourceBuilder withSubmittedDate(LocalDateTime... submittedDates) {
        return withArray((submittedDate, address) -> setField("submittedDate", submittedDate, address), submittedDates);
    }

    public ApplicationResourceBuilder withDuration(Long... durations) {
        return withArray((duration, address) -> setField("durationInMonths", duration, address), durations);
    }

    public ApplicationResourceBuilder withApplicationFinance(List<Long>... applicationFinances) {
        return withArray((applicationFinance, address) -> setField("applicationFinances", applicationFinance, address), applicationFinances);
    }

    public ApplicationResourceBuilder withCompetitionName(String... competitionNames) {
        return withArray((competitionName, address) -> setField("competitionName", competitionName, address), competitionNames);
    }

    public ApplicationResourceBuilder withInviteList(List<Long>... inviteLists) {
        return withArray((inviteList, address) -> setField("invites", inviteList, address), inviteLists);
    }

    public ApplicationResourceBuilder withAssessorFeedbackFileEntry(Long... assessorFeedbackFileEntryId) {
        return withArray((fileEntryId, application) -> application.setAssessorFeedbackFileEntry(fileEntryId), assessorFeedbackFileEntryId);
    }

    public ApplicationResourceBuilder withCompletion(final BigDecimal... bigDecimals) {
        return withArray((completion, application) -> setField("completion", completion, application), bigDecimals);
    }
}
