package org.innovateuk.ifs.competition.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class CompetitionResourceBuilder extends BaseBuilder<CompetitionResource, CompetitionResourceBuilder> {

    private CompetitionResourceBuilder(List<BiConsumer<Integer, CompetitionResource>> newMultiActions) {
        super(newMultiActions);
    }

    public static CompetitionResourceBuilder newCompetitionResource() {
        return new CompetitionResourceBuilder(emptyList()).with(uniqueIds());
    }

    public CompetitionResourceBuilder withName(String name) {
        return with(competition -> setField("name", name, competition));
    }

    public CompetitionResourceBuilder withStartDate(LocalDateTime startDate) {
        return with(competition -> setField("startDate", startDate, competition));
    }

    public CompetitionResourceBuilder withEndDate(LocalDateTime endDate) {
        return with(competition -> setField("endDate", endDate, competition));
    }
    
    public CompetitionResourceBuilder withResearchCategories(Set<Long> categories) {
        return with(competition -> competition.setResearchCategories(categories));
    }

    public CompetitionResourceBuilder withResubmission(Boolean resubmission) {
        return with(competition -> competition.setResubmission(resubmission));
    }

    public CompetitionResourceBuilder withMultiStream(boolean multiStream) {
        return with(competition -> competition.setMultiStream(multiStream));
    }
    
    public CompetitionResourceBuilder withStreamName(String... streamNames) {
        return withArray((streamName, object) -> setField("streamName", streamName, object), streamNames);
    }

    public CompetitionResourceBuilder withId(Long... ids) {
        return withArray((id, object) -> setField("id", id, object), ids);
    }

    public CompetitionResourceBuilder withName(String... names) {
        return withArray((name, object) -> setField("name", name, object), names);
    }

    public CompetitionResourceBuilder withDescription(String... descriptions) {
        return withArray((description, object) -> setField("description", description, object), descriptions);
    }

    public CompetitionResourceBuilder withAssessorAcceptsDate(LocalDateTime... assessorAcceptsDates) {
        return withArray((assessorAcceptsDate, object) -> setField("assessorAcceptsDate", assessorAcceptsDate, object), assessorAcceptsDates);
    }

    public CompetitionResourceBuilder withAssessorDeadlineDate(LocalDateTime... assessorDeadlineDates) {
        return withArray((assessorDeadlineDate, object) -> setField("assessorDeadlineDate", assessorDeadlineDate, object), assessorDeadlineDates);
    }

    public CompetitionResourceBuilder withFundersPanelDate(LocalDateTime... fundersPanelDates) {
        return withArray((fundersPanelDate, object) -> setField("fundersPanelDate", fundersPanelDate, object), fundersPanelDates);
    }

    public CompetitionResourceBuilder withFundersPanelEndDate(LocalDateTime... fundersPanelEndDates) {
        return withArray((fundersPanelEndDate, object) -> setField("fundersPanelEndDate", fundersPanelEndDate, object), fundersPanelEndDates);
    }

    public CompetitionResourceBuilder withAssessorFeedbackDate(LocalDateTime... assessorFeedbackDate) {
        return withArray((date, object) -> object.setAssessorFeedbackDate(date), assessorFeedbackDate);
    }

    public CompetitionResourceBuilder withMaxResearchRatio(Integer... maxResearchRatios) {
        return withArray((maxResearchRatio, object) -> setField("maxResearchRatio", maxResearchRatio, object), maxResearchRatios);
    }

    public CompetitionResourceBuilder withAcademicGrantClaimPercentage(Integer... grantClaimPercentages) {
        return withArray((grantClaimPercentage, object) -> setField("academicGrantPercentage", grantClaimPercentage, object), grantClaimPercentages);
    }
    
    public CompetitionResourceBuilder withCompetitionStatus(CompetitionStatus... statuses) {
    	return withArray((status, object) -> setField("competitionStatus", status, object), statuses);
    }

    public CompetitionResourceBuilder withLeadTechnologist(Long... userIds) {
        return withArray((id, object) -> setField("leadTechnologist", id, object), userIds);
    }

    public CompetitionResourceBuilder withLeadTechnologistName(String... names) {
        return withArray((name, object) -> setField("leadTechnologistName", name, object), names);
    }

    public CompetitionResourceBuilder withExecutive(Long... userIds) {
        return withArray((id, object) -> setField("executive", id, object), userIds);
    }

    public CompetitionResourceBuilder withCompetitionType(Long... typeId) {
        return withArray((id, object) -> setField("competitionType", id, object), typeId);
    }

    public CompetitionResourceBuilder withInnovationSector(Long... ids) {
        return withArray((id, object) -> setField("innovationSector", id, object), ids);
    }
    public CompetitionResourceBuilder withInnovationSectorName(String... names) {
        return withArray((name, object) -> setField("innovationSectorName", name, object), names);
    }
    public CompetitionResourceBuilder withInnovationArea(Long... ids) {
        return withArray((id, object) -> setField("innovationArea", id, object), ids);
    }
    public CompetitionResourceBuilder withInnovationAreaName(String... names) {
        return withArray((name, object) -> setField("innovationAreaName", name, object), names);
    }

    public CompetitionResourceBuilder withPafCode(String... codes) {
        return withArray((code, object) -> setField("pafCode", code, object), codes);
    }
    public CompetitionResourceBuilder withBudgetCode(String... codes) {
        return withArray((code, object) -> setField("budgetCode", code, object), codes);
    }
    public CompetitionResourceBuilder withCompetitionCode(String... codes) {
        return withArray((code, object) -> setField("code", code, object), codes);
    }
    public CompetitionResourceBuilder withCollaborationLevel(CollaborationLevel... collaborationLevels) {
        return withArray((collaborationLevel, object) -> setField("collaborationLevel", collaborationLevel, object), collaborationLevels);
    }
    public CompetitionResourceBuilder withLeadApplicantType(LeadApplicantType... leadApplicantTypes) {
        return withArray((leadApplicantType, object) -> setField("leadApplicantType", leadApplicantType, object), leadApplicantTypes);
    }

    public CompetitionResourceBuilder withActivityCode(String... activityCodes) {
        return withArray((activityCode, object) -> setField("activityCode", activityCode, object), activityCodes);
    }

    public CompetitionResourceBuilder withInnovateBudget(String... innovateBudgets) {
        return withArray((innovateBudget, object) -> setField("innovateBudget", innovateBudget, object), innovateBudgets);
    }

    public CompetitionResourceBuilder withFunders(List<CompetitionFunderResource> fundersList) {
        return withList(fundersList, (funders, section) -> section.setFunders(fundersList));
    }

    public CompetitionResourceBuilder withMilestones(List<Long> milestonesList) {
        return withList(milestonesList, (milestones, object) -> object.setMilestones(milestonesList));
    }

    public CompetitionResourceBuilder withUseResubmissionQuestion(Boolean useResubmissionQuestion) {
        return with(competition -> competition.setUseResubmissionQuestion(useResubmissionQuestion));
    }

    public CompetitionResourceBuilder withSetupComplete(Boolean setupComplete) {
        return with(competition -> competition.setSetupComplete(setupComplete));
    }

    public CompetitionResourceBuilder withAssessorCount(Integer... assessorCount) {
        return withArray((id, object) -> setField("assessorCount", id, object), assessorCount);
    }

    public CompetitionResourceBuilder withAssessorPay(BigDecimal... assessorPay) {
        return withArray((id, object) -> setField("assessorPay", id, object), assessorPay);
    }

    @Override
    protected CompetitionResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionResource>> actions) {
        return new CompetitionResourceBuilder(actions);
    }

    @Override
    protected CompetitionResource createInitial() {
        return new CompetitionResource();
    }
}
