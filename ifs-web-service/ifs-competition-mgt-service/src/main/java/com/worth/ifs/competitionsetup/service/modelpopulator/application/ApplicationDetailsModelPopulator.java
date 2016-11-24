package com.worth.ifs.competitionsetup.service.modelpopulator.application;

import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSubsection;
import com.worth.ifs.competitionsetup.service.modelpopulator.CompetitionSetupSubsectionModelPopulator;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

import static com.worth.ifs.competitionsetup.controller.CompetitionSetupController.COMPETITION_ID_KEY;
import static com.worth.ifs.competitionsetup.controller.CompetitionSetupController.COMPETITION_NAME_KEY;

/**
 * populates the model for the Application Details sub-section under the Application of competition setup section.
 */
@Service
public class ApplicationDetailsModelPopulator implements CompetitionSetupSubsectionModelPopulator {

	@Override
	public CompetitionSetupSubsection sectionToPopulateModel() {
		return CompetitionSetupSubsection.APPLICATION_DETAILS;
	}

	@Override
	public void populateModel(Model model, CompetitionResource competitionResource, Optional<Long> objectId) {

		model.addAttribute(COMPETITION_ID_KEY, competitionResource.getId());
		model.addAttribute(COMPETITION_NAME_KEY, competitionResource.getName());
	}
}
