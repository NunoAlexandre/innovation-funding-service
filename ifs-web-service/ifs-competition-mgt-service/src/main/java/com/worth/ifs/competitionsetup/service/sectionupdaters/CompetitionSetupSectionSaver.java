package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;

import java.util.List;
import java.util.Optional;

public interface CompetitionSetupSectionSaver {

	CompetitionSetupSection sectionToSave();
	
	boolean supportsForm(Class<? extends CompetitionSetupForm> clazz);
	
	ServiceResult<Void> saveSection(CompetitionResource competitionResource, CompetitionSetupForm competitionSetupForm);

    List<Error> autoSaveSectionField(CompetitionResource competitionResource, String fieldName, String value, Optional<Long> ObjectId);

}
