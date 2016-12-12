package org.innovateuk.ifs.competitionsetup.service.formpopulator;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionFunderResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competitionsetup.form.AdditionalInfoForm;
import org.innovateuk.ifs.competitionsetup.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.viewmodel.FunderViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;

/**
 * Form populator for the additional info competition setup section.
 */
@Service
public class AdditionalInfoFormPopulator implements CompetitionSetupFormPopulator {

    @Autowired
    CompetitionService competitionService;

	@Override
	public CompetitionSetupSection sectionToFill() {
		return CompetitionSetupSection.ADDITIONAL_INFO;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
		AdditionalInfoForm competitionSetupForm = new AdditionalInfoForm();

		competitionSetupForm.setActivityCode(competitionResource.getActivityCode());
		competitionSetupForm.setInnovateBudget(competitionResource.getInnovateBudget());

		competitionSetupForm.setCompetitionCode(competitionResource.getCode());
		competitionSetupForm.setPafNumber(competitionResource.getPafCode());
		competitionSetupForm.setBudgetCode(competitionResource.getBudgetCode());

		competitionResource.getFunders().forEach(funderResource ->  {
			FunderViewModel funder = new FunderViewModel(funderResource);
			competitionSetupForm.getFunders().add(funder);
		});

        if(competitionResource.getFunders().isEmpty()) {
			CompetitionFunderResource competitionFunderResource = initFirstFunder();
			competitionSetupForm.setFunders(asList(new FunderViewModel(competitionFunderResource)));
			competitionResource.setFunders(asList(competitionFunderResource));
			competitionService.update(competitionResource);
        }

        return competitionSetupForm;
	}

    public CompetitionFunderResource initFirstFunder() {
        CompetitionFunderResource competitionFunderResource = new CompetitionFunderResource();
        competitionFunderResource.setCoFunder(false);

		return competitionFunderResource;
    }

}
