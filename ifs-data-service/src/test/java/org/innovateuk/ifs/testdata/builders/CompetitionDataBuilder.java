package org.innovateuk.ifs.testdata.builders;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.category.resource.CategoryType;
import org.innovateuk.ifs.competition.domain.CompetitionType;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.testdata.builders.data.CompetitionData;
import org.innovateuk.ifs.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.category.resource.CategoryType.*;
import static org.innovateuk.ifs.competition.resource.MilestoneType.*;
import static org.innovateuk.ifs.testdata.builders.ApplicationDataBuilder.newApplicationData;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

/**
 * Generates data from Competitions, including any Applications taking part in this Competition
 */
public class CompetitionDataBuilder extends BaseDataBuilder<CompetitionData, CompetitionDataBuilder> {

    public CompetitionDataBuilder createCompetition() {

        return asCompAdmin(data -> {

            CompetitionResource newCompetition = competitionSetupService.
                    create().
                    getSuccessObjectOrThrowException();

            updateCompetitionInCompetitionData(data, newCompetition.getId());
        });
    }

    public CompetitionDataBuilder withExistingCompetition(Long competitionId) {

        return asCompAdmin(data -> {
            CompetitionResource existingCompetition = competitionService.getCompetitionById(competitionId).getSuccessObjectOrThrowException();
            updateCompetitionInCompetitionData(data, existingCompetition.getId());
        });
    }

    public CompetitionDataBuilder withBasicData(String name, String description, String competitionTypeName, String innovationAreaName,
                                                String innovationSectorName, String researchCategoryName, String leadTechnologist,
                                                String compExecutive, String budgetCode, String pafCode, String code, String activityCode, Integer assessorCount, BigDecimal assessorPay,
                                                Boolean multiStream, String collaborationLevelCode, String leadApplicantTypeCode, Integer researchRatio, Boolean resubmission) {

        return asCompAdmin(data -> {

            doCompetitionDetailsUpdate(data, competition -> {

                CompetitionType competitionType = competitionTypeRepository.findByName(competitionTypeName).get(0);
                Long innovationArea = getCategoryIdOrNull(INNOVATION_AREA, innovationAreaName);
                Long innovationSector = getCategoryIdOrNull(INNOVATION_SECTOR, innovationSectorName);
                Long researchCategory = getCategoryIdOrNull(RESEARCH_CATEGORY, researchCategoryName);

                CollaborationLevel collaborationLevel =  CollaborationLevel.fromCode(collaborationLevelCode);
                LeadApplicantType leadApplicantType = LeadApplicantType.BUSINESS.fromCode(leadApplicantTypeCode);

                competition.setName(name);
                competition.setDescription(description);
                competition.setInnovationArea(innovationArea);
                competition.setInnovationSector(innovationSector);
                competition.setResearchCategories(singleton(researchCategory));
                competition.setMaxResearchRatio(30);
                competition.setAcademicGrantPercentage(100);
                competition.setCompetitionType(competitionType.getId());
                competition.setLeadTechnologist(userRepository.findByEmail(leadTechnologist).map(User::getId).orElse(null));
                competition.setExecutive(userRepository.findByEmail(compExecutive).map(User::getId).orElse(null));
                competition.setPafCode(pafCode);
                competition.setCode(code);
                competition.setBudgetCode(budgetCode);
                competition.setActivityCode(activityCode);
                competition.setCollaborationLevel(collaborationLevel);
                competition.setLeadApplicantType(leadApplicantType);
                competition.setMaxResearchRatio(researchRatio);
                competition.setResubmission(resubmission);
                competition.setMultiStream(multiStream);
                competition.setAssessorPay(assessorPay);
                competition.setAssessorCount(assessorCount);
            });
        });
    }

    private Long getCategoryIdOrNull(CategoryType type, String name) {
        return !isBlank(name) ? simpleFindFirst(categoryRepository.findByType(type), c -> name.equals(c.getName())).get().getId() : null;
    }

    private void doCompetitionDetailsUpdate(CompetitionData data, Consumer<CompetitionResource> updateFn) {

        CompetitionResource competition =
                competitionService.getCompetitionById(data.getCompetition().getId()).getSuccessObjectOrThrowException();

        updateFn.accept(competition);

        competitionSetupService.update(competition.getId(), competition).getSuccessObjectOrThrowException();

        updateCompetitionInCompetitionData(data, competition.getId());
    }

    public CompetitionDataBuilder withApplicationFormFromTemplate() {

        return asCompAdmin(data -> {

            CompetitionResource competition = data.getCompetition();

            competitionSetupService.copyFromCompetitionTypeTemplate(competition.getId(), competition.getCompetitionType()).
                    getSuccessObjectOrThrowException();

            updateCompetitionInCompetitionData(data, competition.getId());
        });
    }

    public CompetitionDataBuilder withSetupComplete() {
        return asCompAdmin(data -> {
            asList(CompetitionSetupSection.values()).forEach(competitionSetupSection -> {
                competitionSetupService.markSectionComplete(data.getCompetition().getId(), competitionSetupSection);
            });
            competitionSetupService.markAsSetup(data.getCompetition().getId());
        });
    }

    public CompetitionDataBuilder moveCompetitionIntoOpenStatus() {
        return asCompAdmin(data -> shiftMilestoneToTomorrow(data, MilestoneType.SUBMISSION_DATE));
    }

    public CompetitionDataBuilder moveCompetitionIntoFundersPanelStatus() {
        return asCompAdmin(data -> shiftMilestoneToTomorrow(data, MilestoneType.NOTIFICATIONS));
    }

    public CompetitionDataBuilder sendFundingDecisions(Pair<String, FundingDecision>... fundingDecisions) {
        return sendFundingDecisions(asList(fundingDecisions));
    }

    public CompetitionDataBuilder sendFundingDecisions(List<Pair<String, FundingDecision>> fundingDecisions) {
        return asCompAdmin(data -> {

            List<Pair<Long, FundingDecision>> applicationIdAndDecisions = simpleMap(fundingDecisions, decisionInfo -> {
                FundingDecision decision = decisionInfo.getRight();
                Application application = applicationRepository.findByName(decisionInfo.getLeft()).get(0);
                return Pair.of(application.getId(), decision);
            });

            applicationFundingService.makeFundingDecision(data.getCompetition().getId(), pairsToMap(applicationIdAndDecisions)).
                    getSuccessObjectOrThrowException();

            projectService.createProjectsFromFundingDecisions(pairsToMap(applicationIdAndDecisions));
        });
    }

    private void shiftMilestoneToTomorrow(CompetitionData data, MilestoneType milestoneType) {
        List<MilestoneResource> milestones = milestoneService.getAllMilestonesByCompetitionId(data.getCompetition().getId()).getSuccessObjectOrThrowException();
        MilestoneResource submissionDateMilestone = simpleFindFirst(milestones, m -> milestoneType.equals(m.getType())).get();

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime submissionDeadline = submissionDateMilestone.getDate();
        long daysPassedSinceSubmissionEnded = submissionDeadline.until(now, ChronoUnit.DAYS);

        milestones.forEach(m -> {
            if (m.getDate() != null) {
                m.setDate(m.getDate().plusDays(daysPassedSinceSubmissionEnded + 1));
                milestoneService.updateMilestone(m).getSuccessObjectOrThrowException();
            }
        });
    }

    public CompetitionDataBuilder restoreOriginalMilestones() {
        return asCompAdmin(data -> {

            data.getOriginalMilestones().forEach(original -> {

                MilestoneResource amendedMilestone =
                        milestoneService.getMilestoneByTypeAndCompetitionId(original.getType(), data.getCompetition().getId()).
                                getSuccessObjectOrThrowException();

                amendedMilestone.setDate(original.getDate());

                milestoneService.updateMilestone(amendedMilestone).getSuccessObjectOrThrowException();
            });
        });
    }

            public CompetitionDataBuilder withNewMilestones() {

        return asCompAdmin(data -> {

            Stream.of(MilestoneType.presetValues()).forEach(type -> {
                milestoneService.create(type, data.getCompetition().getId());
            });
        });
    }

    public CompetitionDataBuilder withOpenDate(LocalDateTime date) {
        return withMilestoneUpdate(date, OPEN_DATE);
    }

    public CompetitionDataBuilder withSubmissionDate(LocalDateTime date) {
        return withMilestoneUpdate(date, SUBMISSION_DATE);
    }

    public CompetitionDataBuilder withFundersPanelDate(LocalDateTime date) {
        return withMilestoneUpdate(date, FUNDERS_PANEL);
    }

    public CompetitionDataBuilder withFundersPanelEndDate(LocalDateTime date) {
        return withMilestoneUpdate(date, NOTIFICATIONS);
    }

    public CompetitionDataBuilder withAssessorAcceptsDate(LocalDateTime date) {
        return withMilestoneUpdate(date, ASSESSOR_ACCEPTS);
    }

    public CompetitionDataBuilder withAssessorsNotifiedDate(LocalDateTime date) {
        return withMilestoneUpdate(date, ASSESSORS_NOTIFIED);
    }

    public CompetitionDataBuilder withAssessorEndDate(LocalDateTime date) {
        return withMilestoneUpdate(date, ASSESSOR_DEADLINE);
    }

    public CompetitionDataBuilder withAssessmentClosedDate(LocalDateTime date) {
        return withMilestoneUpdate(date, ASSESSMENT_CLOSED);
    }

    public CompetitionDataBuilder withAssessorBriefingDate(LocalDateTime date) {
        return withMilestoneUpdate(date, ASSESSOR_BRIEFING);
    }



    private CompetitionDataBuilder withMilestoneUpdate(LocalDateTime date, MilestoneType milestoneType) {

        if (date == null) {
            return this;
        }

        return asCompAdmin(data -> {

            MilestoneResource milestone =
                    milestoneService.getMilestoneByTypeAndCompetitionId(milestoneType, data.getCompetition().getId()).getSuccessObjectOrThrowException();

            if (milestone.getId() == null) {
                milestone = milestoneService.create(milestoneType, data.getCompetition().getId()).getSuccessObjectOrThrowException();
            }

            milestone.setDate(date);
            milestoneService.updateMilestone(milestone);

            data.addOriginalMilestone(milestone);
        });
    }

    public CompetitionDataBuilder withApplications(UnaryOperator<ApplicationDataBuilder>... applicationDataBuilders) {
        return withApplications(asList(applicationDataBuilders));
    }

    public CompetitionDataBuilder withApplications(List<UnaryOperator<ApplicationDataBuilder>> applicationDataBuilders) {
        return with(data -> applicationDataBuilders.forEach(fn -> fn.apply(newApplicationData(serviceLocator).withCompetition(data.getCompetition())).build()));
    }


    private void updateCompetitionInCompetitionData(CompetitionData competitionData, Long competitionId) {
        CompetitionResource newCompetitionSaved = competitionService.getCompetitionById(competitionId).getSuccessObjectOrThrowException();
        competitionData.setCompetition(newCompetitionSaved);
    }

    private CompetitionDataBuilder asCompAdmin(Consumer<CompetitionData> action) {
        return with(data -> {
            doAs(compAdmin(), () -> action.accept(data));
        });
    }

    public static CompetitionDataBuilder newCompetitionData(ServiceLocator serviceLocator) {
        return new CompetitionDataBuilder(emptyList(), serviceLocator);
    }

    private CompetitionDataBuilder(List<BiConsumer<Integer, CompetitionData>> multiActions,
                                   ServiceLocator serviceLocator) {
        super(multiActions, serviceLocator);
    }

    @Override
    protected CompetitionDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionData>> actions) {
        return new CompetitionDataBuilder(actions, serviceLocator);
    }

    @Override
    protected CompetitionData createInitial() {
        return new CompetitionData();
    }


}
