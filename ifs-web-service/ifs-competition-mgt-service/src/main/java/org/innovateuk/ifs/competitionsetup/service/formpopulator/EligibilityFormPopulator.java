package org.innovateuk.ifs.competitionsetup.service.formpopulator;

import org.innovateuk.ifs.competition.form.enumerable.ResearchParticipationAmount;
import org.innovateuk.ifs.competition.resource.CollaborationLevel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.LeadApplicantType;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.form.EligibilityForm;
import org.innovateuk.ifs.competitionsetup.utils.CompetitionUtils;
import org.springframework.stereotype.Service;

/**
 * Form modelpopulator for the eligibility competition setup section.
 */
@Service
public class EligibilityFormPopulator implements CompetitionSetupFormPopulator {

	@Override
	public CompetitionSetupSection sectionToFill() {
		return CompetitionSetupSection.ELIGIBILITY;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
		EligibilityForm competitionSetupForm = new EligibilityForm();
		
		competitionSetupForm.setResearchCategoryId(competitionResource.getResearchCategories());
		
		ResearchParticipationAmount amount = ResearchParticipationAmount.fromAmount(competitionResource.getMaxResearchRatio());
		if(amount != null) {
			competitionSetupForm.setResearchParticipationAmountId(amount.getId());
		}

		competitionSetupForm.setMultipleStream("no");

		CollaborationLevel level = competitionResource.getCollaborationLevel();
		if(level != null) {
			competitionSetupForm.setSingleOrCollaborative(level.getCode());
		}
		
		LeadApplicantType type = competitionResource.getLeadApplicantType();
		if(type != null) {
			competitionSetupForm.setLeadApplicantType(type.getCode());
		}

        competitionSetupForm.setResubmission(CompetitionUtils.booleanToText(competitionResource.getResubmission()));

		return competitionSetupForm;
	}


}
