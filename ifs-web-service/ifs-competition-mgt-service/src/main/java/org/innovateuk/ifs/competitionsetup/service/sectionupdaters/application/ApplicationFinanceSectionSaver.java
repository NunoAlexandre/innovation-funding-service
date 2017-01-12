package org.innovateuk.ifs.competitionsetup.service.sectionupdaters.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupFinanceResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationFinanceForm;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupFinanceService;
import org.innovateuk.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import org.innovateuk.ifs.competitionsetup.service.sectionupdaters.AbstractSectionSaver;
import org.innovateuk.ifs.competitionsetup.service.sectionupdaters.CompetitionSetupSubsectionSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.innovateuk.ifs.competition.resource.CompetitionSetupSection.APPLICATION_FORM;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection.FINANCES;

/**
 * Competition setup section saver for the application -> finance form sub-section.
 */
@Service
public class ApplicationFinanceSectionSaver extends AbstractSectionSaver implements CompetitionSetupSubsectionSaver {

    @Autowired
    private CompetitionService competitionService;

	@Autowired
	private CompetitionSetupFinanceService competitionSetupFinanceService;

	@Override
	public CompetitionSetupSection sectionToSave() { return APPLICATION_FORM; }

	@Override
	public CompetitionSetupSubsection subsectionToSave() { return FINANCES; }

	@Override
	protected ServiceResult<Void> doSaveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
        ApplicationFinanceForm form = (ApplicationFinanceForm) competitionSetupForm;
		CompetitionSetupFinanceResource csfr = new CompetitionSetupFinanceResource();
		csfr.setFullApplicationFinance(form.isFullApplicationFinance());
		csfr.setIncludeGrowthTable(form.isIncludeGrowthTable());
        csfr.setCompetitionId(competition.getId());
		return competitionSetupFinanceService.updateFinance(csfr);
	}

	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return ApplicationFinanceForm.class.equals(clazz);
	}

}
