package org.innovateuk.ifs.competitionsetup.service.modelpopulator;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competitionsetup.viewmodel.ManageInnovationLeadsViewModel;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManageInnovationLeadsModelPopulator {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private UserService userService;

    public void populateModel(Model model, long competitionId) {

        CompetitionResource competition = competitionService.getById(competitionId);

        List<UserResource> availableInnovationLeads = userService.findUserByType(UserRoleType.INNOVATION_LEAD);
        List<UserResource> innovationLeadsAssignedToCompetition = competitionService.findInnovationLeads(competitionId);
        availableInnovationLeads.removeAll(innovationLeadsAssignedToCompetition);

        model.addAttribute("model", new ManageInnovationLeadsViewModel(competitionId, competition.getName(),
                competition.getLeadTechnologistName(), competition.getExecutiveName(), competition.getInnovationSectorName(),
                competition.getInnovationAreaNames(), sortByName(innovationLeadsAssignedToCompetition),
                sortByName(availableInnovationLeads)));
        model.addAttribute("initialDetailsComplete", competition.isInitialDetailsComplete());
    }

    private List<UserResource> sortByName(List<UserResource> userResources) {
        return userResources.stream().sorted(Comparator.comparing(userResource -> userResource.getName().toUpperCase())).collect(Collectors.toList());
    }
}
