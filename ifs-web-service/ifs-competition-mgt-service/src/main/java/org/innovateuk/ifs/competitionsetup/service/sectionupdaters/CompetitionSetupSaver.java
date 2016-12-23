package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;

import java.util.Optional;

/**
 * The interface for saving and autosaving competition forms.
 */
public interface CompetitionSetupSaver {

	ServiceResult<Void> autoSaveSectionField(CompetitionResource competitionResource, CompetitionSetupForm form, String fieldName, String value, Optional<Long> ObjectId);

	boolean supportsForm(Class<? extends CompetitionSetupForm> clazz);

	ServiceResult<Void> saveSection(CompetitionResource competitionResource, CompetitionSetupForm competitionSetupForm);

	CompetitionSetupSection sectionToSave();
}
