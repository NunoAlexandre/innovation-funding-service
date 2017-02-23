package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.CompetitionFunder;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Generates data from Competition Funders and attaches it to a competition
 */
public class CompetitionFunderDataBuilder extends BaseDataBuilder<Void, CompetitionFunderDataBuilder>{

    public CompetitionFunderDataBuilder withCompetitionFunderData(String competitionName, String funder, BigInteger funderBudget, boolean isCoFunder) {
        return with(data -> {

            Competition competition = retrieveCompetitionByName(competitionName);

            CompetitionFunder competitionFunder = new CompetitionFunder();
            competitionFunder.setCompetition(competition);
            competitionFunder.setFunder(funder);
            competitionFunder.setFunderBudget(funderBudget);
            competitionFunder.setCoFunder(isCoFunder);
            competitionFunderRepository.save(competitionFunder);
        });
    }

    public static CompetitionFunderDataBuilder newCompetitionFunderData(ServiceLocator serviceLocator) {
        return new CompetitionFunderDataBuilder(Collections.emptyList(), serviceLocator);
    }

    private CompetitionFunderDataBuilder(List<BiConsumer<Integer, Void>> multiActions,
                                  ServiceLocator serviceLocator) {

        super(multiActions, serviceLocator);
    }

    @Override
    protected CompetitionFunderDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Void>> actions) {
        return new CompetitionFunderDataBuilder(actions, serviceLocator);
    }

    @Override
    protected Void createInitial() {
        return null;
    }
}
