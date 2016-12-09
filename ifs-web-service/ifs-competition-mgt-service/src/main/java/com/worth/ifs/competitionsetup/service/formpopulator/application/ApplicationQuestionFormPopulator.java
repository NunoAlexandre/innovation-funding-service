package com.worth.ifs.competitionsetup.service.formpopulator.application;

import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupQuestionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSubsection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.application.ApplicationQuestionForm;
import com.worth.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import com.worth.ifs.competitionsetup.service.formpopulator.CompetitionSetupSubsectionFormPopulator;
import com.worth.ifs.competitionsetup.viewmodel.GuidanceRowForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Form populator for the application form competition setup section.
 */
@Service
public class ApplicationQuestionFormPopulator implements CompetitionSetupSubsectionFormPopulator {

	@Autowired
	private CompetitionSetupQuestionService competitionSetupQuestionService;

	@Override
	public CompetitionSetupSubsection sectionToFill() {
		return CompetitionSetupSubsection.QUESTIONS;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource, Optional<Long> objectId) {

		ApplicationQuestionForm competitionSetupForm = new ApplicationQuestionForm();

		if(objectId.isPresent()) {
			CompetitionSetupQuestionResource questionResource = competitionSetupQuestionService.getQuestion(objectId.get()).getSuccessObjectOrThrowException();
			competitionSetupForm.setQuestion(questionResource);

			competitionSetupForm.getQuestion().getGuidanceRows().forEach(guidanceRowResource ->  {
				GuidanceRowForm grvm = new GuidanceRowForm(guidanceRowResource);
				competitionSetupForm.getGuidanceRows().add(grvm);
			});

        } else {
            throw new ObjectNotFoundException();
        }

		return competitionSetupForm;
	}

}
