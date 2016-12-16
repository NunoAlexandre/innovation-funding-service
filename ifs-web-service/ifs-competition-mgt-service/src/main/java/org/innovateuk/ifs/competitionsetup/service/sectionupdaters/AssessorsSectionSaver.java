package org.innovateuk.ifs.competitionsetup.service.sectionupdaters;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competitionsetup.form.AssessorsForm;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;

/**
 * Competition setup section saver for the assessor section.
 */
@Service
public class AssessorsSectionSaver extends AbstractSectionSaver implements CompetitionSetupSectionSaver {

	@Autowired
	private CompetitionService competitionService;
	
	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.ASSESSORS;
	}

	@Override
	public ServiceResult<Void> saveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {

		AssessorsForm assessorsForm = (AssessorsForm) competitionSetupForm;

		if(!sectionToSave().preventEdit(competition)) {
			setFieldsDisallowedFromChangeAfterSetupAndLive(competition, assessorsForm);
			setFieldsAllowedFromChangeAfterSetupAndLive(competition, assessorsForm);

			return competitionService.update(competition);
		}
		else {
			return serviceFailure(asList(new Error("COMPETITION_NOT_EDITABLE")));
		}
	}

	private void setFieldsDisallowedFromChangeAfterSetupAndLive(CompetitionResource competition, AssessorsForm assessorsForm) {
		if(!competition.isSetupAndLive()) {
			competition.setAssessorPay(assessorsForm.getAssessorPay());
		}
	}

	private void setFieldsAllowedFromChangeAfterSetupAndLive(CompetitionResource competition, AssessorsForm assessorsForm) {
		competition.setAssessorCount(assessorsForm.getAssessorCount());
	}

	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return AssessorsForm.class.equals(clazz);
	}

}
