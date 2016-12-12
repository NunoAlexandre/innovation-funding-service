package org.innovateuk.ifs.competitionsetup.service.formpopulator;


import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;

import java.util.Optional;

public interface CompetitionSetupSubsectionFormPopulator {

	CompetitionSetupSubsection sectionToFill();
	
	CompetitionSetupForm populateForm(CompetitionResource competitionResource, Optional<Long> objectId);
}
