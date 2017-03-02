package org.innovateuk.ifs.competitionsetup.service.formpopulator.application;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupFinanceResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationFinanceForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupFinanceService;
import org.innovateuk.ifs.competitionsetup.service.formpopulator.CompetitionSetupSubsectionFormPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Form populator for the Finances sub-section under the Application form of competition setup section.
 */
@Service
public class ApplicationFinanceFormPopulator implements CompetitionSetupSubsectionFormPopulator {

	@Autowired
	private CompetitionSetupFinanceService competitionSetupFinanceService;

	@Override
	public CompetitionSetupSubsection sectionToFill() {
		return CompetitionSetupSubsection.FINANCES;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource, Optional<Long> objectId) {

		CompetitionSetupFinanceResource csfr = competitionSetupFinanceService.getByCompetitionId(competitionResource.getId());
		ApplicationFinanceForm competitionSetupForm = new ApplicationFinanceForm();

		competitionSetupForm.setFullApplicationFinance(csfr.isFullApplicationFinance());
		competitionSetupForm.setIncludeGrowthTable(csfr.isIncludeGrowthTable());

		return competitionSetupForm;
	}


}
