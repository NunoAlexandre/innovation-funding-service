package org.innovateuk.ifs.competitionsetup.service.modelpopulator;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competitionsetup.utils.CompetitionUtils;
import org.innovateuk.ifs.competitionsetup.viewmodel.AdditionalModelViewModel;
import org.innovateuk.ifs.competitionsetup.viewmodel.CompetitionSetupViewModel;
import org.innovateuk.ifs.competitionsetup.viewmodel.InitialDetailsViewModel;
import org.innovateuk.ifs.competitionsetup.viewmodel.fragments.GeneralSetupViewModel;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * populates the model for the initial details competition setup section.
 */
@Service
public class AdditionalModelPopulator implements CompetitionSetupSectionModelPopulator {

	@Autowired
	private CompetitionService competitionService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryRestService categoryRestService;
	
	@Override
	public CompetitionSetupSection sectionToPopulateModel() {
		return CompetitionSetupSection.ADDITIONAL_INFO;
	}

    @Override
	public CompetitionSetupViewModel populateModel(GeneralSetupViewModel generalViewModel, CompetitionResource competitionResource) {
		return new AdditionalModelViewModel(generalViewModel);
	}
}
