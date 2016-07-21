package com.worth.ifs.competitionsetup.service.sectionupdaters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.competition.form.enumerable.ResearchParticipationAmount;
import com.worth.ifs.competition.resource.CollaborationLevel;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competition.resource.LeadApplicantType;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.EligibilityForm;

/**
 * Competition setup section saver for the eligibility section.
 */
@Service
public class EligibilitySectionSaver implements CompetitionSetupSectionSaver {

	@Autowired
	private CompetitionService competitionService;
	
	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.ELIGIBILITY;
	}

	@Override
	public ValidationMessages saveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
		
		EligibilityForm eligibilityForm = (EligibilityForm) competitionSetupForm;
		
		competition.setResearchCategories(eligibilityForm.getResearchCategoryId());
		
		ResearchParticipationAmount amount = ResearchParticipationAmount.fromId(eligibilityForm.getResearchParticipationAmountId());
		if(amount != null) {
			competition.setMaxResearchRatio(amount.getAmount());
		}
		
		boolean multiStream = "yes".equals(eligibilityForm.getMultipleStream());
		competition.setMultiStream(multiStream);
		if(multiStream) {
			competition.setStreamName(eligibilityForm.getStreamName());
		} else {
			competition.setStreamName(null);
		}

		CollaborationLevel level = CollaborationLevel.fromCode(eligibilityForm.getSingleOrCollaborative());
		competition.setCollaborationLevel(level);
		
		LeadApplicantType type = LeadApplicantType.fromCode(eligibilityForm.getLeadApplicantType());
		competition.setLeadApplicantType(type);
		
		competitionService.update(competition);
		
        return null;
	}
	
	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return EligibilityForm.class.equals(clazz);
	}

}
