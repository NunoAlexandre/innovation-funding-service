package com.worth.ifs.testdata;

import com.worth.ifs.category.domain.Category;
import com.worth.ifs.category.repository.CategoryRepository;
import com.worth.ifs.competition.domain.CompetitionType;
import com.worth.ifs.competition.repository.CompetitionTypeRepository;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.transactional.CompetitionService;
import com.worth.ifs.competition.transactional.CompetitionSetupService;
import com.worth.ifs.user.transactional.UserService;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.category.resource.CategoryType.*;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;


public class CompetitionDataBuilder extends BaseDataBuilder<CompetitionData, CompetitionDataBuilder> {

    private CompetitionService competitionService;
    private CompetitionTypeRepository competitionTypeRepository;
    private CategoryRepository categoryRepository;
    private CompetitionSetupService competitionSetupService;

    public static CompetitionDataBuilder newCompetitionData(
            UserService userService,
            CompetitionService competitionService,
            CompetitionTypeRepository competitionTypeRepository,
            CategoryRepository categoryRepository,
            CompetitionSetupService competitionSetupService) {

        return new CompetitionDataBuilder(emptyList(), userService, competitionService, competitionTypeRepository,
                categoryRepository, competitionSetupService);
    }

    private CompetitionDataBuilder(List<BiConsumer<Integer, CompetitionData>> multiActions,
                                   UserService userService,
                                   CompetitionService competitionService,
                                   CompetitionTypeRepository competitionTypeRepository,
                                   CategoryRepository categoryRepository,
                                   CompetitionSetupService competitionSetupService) {
        super(multiActions, userService);
        this.competitionService = competitionService;
        this.competitionTypeRepository = competitionTypeRepository;
        this.categoryRepository = categoryRepository;
        this.competitionSetupService = competitionSetupService;
    }

    @Override
    protected CompetitionDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, CompetitionData>> actions) {
        return new CompetitionDataBuilder(actions, userService, competitionService, competitionTypeRepository,
                categoryRepository, competitionSetupService);
    }

    @Override
    protected CompetitionData createInitial() {
        return new CompetitionData();
    }

    public CompetitionDataBuilder createCompetition() {

        return with(competitionData -> {

            doAs(compAdmin(), () -> {

                CompetitionResource newCompetition = competitionSetupService.
                        create().
                        getSuccessObjectOrThrowException();

                updateCompetitionInCompetitionData(competitionData, newCompetition.getId());
            });
        });
    }

    public CompetitionDataBuilder withBasicData(String name, String description, String competitionTypeName, String innovationAreaName, String innovationSectorName, String researchCategoryName) {

        return with(competitionData -> {

            doAs(compAdmin(), () -> {

                CompetitionResource competition = competitionData.getCompetition();

                CompetitionType competitionType = competitionTypeRepository.findByName(competitionTypeName).get(0);
                Category innovationArea = simpleFindFirst(categoryRepository.findByType(INNOVATION_AREA), c -> innovationAreaName.equals(c.getName())).get();
                Category innovationSector = simpleFindFirst(categoryRepository.findByType(INNOVATION_SECTOR), c -> innovationSectorName.equals(c.getName())).get();
                Category researchCategory = simpleFindFirst(categoryRepository.findByType(RESEARCH_CATEGORY), c -> researchCategoryName.equals(c.getName())).get();

                CompetitionResource newCompetitionDetails = newCompetitionResource().
                        withId(competition.getId()).
                        withName(name).
                        withDescription(description).
                        withInnovationArea(innovationArea.getId()).
                        withInnovationSector(innovationSector.getId()).
                        withResearchCategories(singleton(researchCategory.getId())).
                        withMaxResearchRatio(30).
                        withAcademicGrantClaimPercentage(100).
                        withCompetitionType(competitionType.getId()).
                        build();

                competitionSetupService.update(competition.getId(), newCompetitionDetails).getSuccessObjectOrThrowException();

                updateCompetitionInCompetitionData(competitionData, competition.getId());
            });
        });
    }

    public CompetitionDataBuilder withApplicationFormFromTemplate() {

        return with(competitionData -> {

            doAs(compAdmin(), () -> {

                CompetitionResource competition = competitionData.getCompetition();

                competitionSetupService.initialiseFormForCompetitionType(competition.getId(), competition.getCompetitionType()).
                        getSuccessObjectOrThrowException();

                updateCompetitionInCompetitionData(competitionData, competition.getId());
            });
        });
    }

    private void updateCompetitionInCompetitionData(CompetitionData competitionData, Long competitionId) {
        CompetitionResource newCompetitionSaved = competitionService.getCompetitionById(competitionId).getSuccessObjectOrThrowException();
        competitionData.setCompetition(newCompetitionSaved);
    }
}
