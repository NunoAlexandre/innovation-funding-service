package org.innovateuk.ifs.competitionsetup.service.formpopulator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.InitialDetailsForm;
import org.springframework.stereotype.Service;

/**
 * Form populator for the initial details competition setup section.
 */
@Service
public class InitialDetailsFormPopulator implements CompetitionSetupFormPopulator {

	@Override
	public CompetitionSetupSection sectionToFill() {
		return CompetitionSetupSection.INITIAL_DETAILS;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
		InitialDetailsForm competitionSetupForm = new InitialDetailsForm();

		competitionSetupForm.setCompetitionTypeId(competitionResource.getCompetitionType());
		competitionSetupForm.setExecutiveUserId(competitionResource.getExecutive());

		competitionSetupForm.setInnovationSectorCategoryId(competitionResource.getInnovationSector());
		competitionSetupForm.setInnovationAreaCategoryId(competitionResource.getInnovationArea());
		competitionSetupForm.setLeadTechnologistUserId(competitionResource.getLeadTechnologist());

		if (competitionResource.getStartDate() != null) {
			competitionSetupForm.setOpeningDateDay(competitionResource.getStartDate().getDayOfMonth());
			competitionSetupForm.setOpeningDateMonth(competitionResource.getStartDate().getMonth().getValue());
			competitionSetupForm.setOpeningDateYear(competitionResource.getStartDate().getYear());
		}

		competitionSetupForm.setTitle(competitionResource.getName());

		return competitionSetupForm;
	}

}
