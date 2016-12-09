package com.worth.ifs.competitionsetup.service.sectionupdaters.application;

import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionSetupSubsection;
import com.worth.ifs.competition.resource.GuidanceRowResource;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.application.AbstractApplicationQuestionForm;
import com.worth.ifs.competitionsetup.form.application.ApplicationProjectForm;
import com.worth.ifs.competitionsetup.service.sectionupdaters.CompetitionSetupSubsectionSaver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.resource.CompetitionSetupSubsection.PROJECT_DETAILS;

/**
 * Competition setup section saver for the application form section.
 */
@Service
public class ApplicationProjectDetailsSectionSaver extends AbstractApplicationSectionSaver implements CompetitionSetupSubsectionSaver {

    @Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return ApplicationProjectForm.class.equals(clazz);
	}

    @Override
    public CompetitionSetupSubsection sectionToSave() {
        return PROJECT_DETAILS;
    }

    @Override
    protected void mapGuidanceRows(AbstractApplicationQuestionForm form) {
        //nothing to do here. The guidance rows are set on the form already.
    }

    @Override
    protected ServiceResult<Void> autoSaveGuidanceRowSubject(GuidanceRowResource guidanceRow, String fieldName, String value) {
        if (fieldName.endsWith("subject")) {
            guidanceRow.setSubject(value);
            return serviceSuccess();
        } else {
            return serviceFailure(new Error("Field not found", HttpStatus.BAD_REQUEST));
        }
    }
}
