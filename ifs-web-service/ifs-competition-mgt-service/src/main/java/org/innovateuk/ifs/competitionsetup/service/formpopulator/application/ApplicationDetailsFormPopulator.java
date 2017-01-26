package org.innovateuk.ifs.competitionsetup.service.formpopulator.application;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationDetailsForm;
import org.innovateuk.ifs.competitionsetup.service.formpopulator.CompetitionSetupSubsectionFormPopulator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Form modelpopulator for the Application Details sub-section under the Application form of competition setup section.
 */
@Service
public class ApplicationDetailsFormPopulator implements CompetitionSetupSubsectionFormPopulator {

	@Override
	public CompetitionSetupSubsection sectionToFill() {
		return CompetitionSetupSubsection.APPLICATION_DETAILS;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource, Optional<Long> objectId) {
		ApplicationDetailsForm competitionSetupForm = new ApplicationDetailsForm();

		competitionSetupForm.setUseResubmissionQuestion(competitionResource.isUseResubmissionQuestion());

		return competitionSetupForm;
	}


}
