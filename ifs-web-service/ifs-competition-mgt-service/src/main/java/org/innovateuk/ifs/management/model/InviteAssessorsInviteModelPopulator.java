package org.innovateuk.ifs.management.model;

import org.innovateuk.ifs.application.service.CategoryService;
import org.innovateuk.ifs.assessment.service.CompetitionInviteRestService;
import org.innovateuk.ifs.category.resource.InnovationSectorResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.invite.resource.AssessorCreatedInvitePageResource;
import org.innovateuk.ifs.invite.resource.AssessorCreatedInviteResource;
import org.innovateuk.ifs.management.viewmodel.InviteAssessorsInviteViewModel;
import org.innovateuk.ifs.management.viewmodel.InvitedAssessorRowViewModel;
import org.innovateuk.ifs.management.viewmodel.PaginationViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * Build the model for the Invite assessors 'Invite' view.
 */
@Component
public class InviteAssessorsInviteModelPopulator extends InviteAssessorsModelPopulator<InviteAssessorsInviteViewModel> {

    @Autowired
    private CompetitionInviteRestService competitionInviteRestService;

    @Autowired
    private CategoryService categoryService;

    public InviteAssessorsInviteViewModel populateModel(CompetitionResource competition, int page, String originQuery) {
        InviteAssessorsInviteViewModel model = super.populateModel(competition);

        AssessorCreatedInvitePageResource pageResource = competitionInviteRestService.getCreatedInvites(competition.getId(), page)
                .getSuccessObjectOrThrowException();

        List<InvitedAssessorRowViewModel> assessors = simpleMap(pageResource.getContent(), this::getRowViewModel);

        model.setAssessors(assessors);
        model.setPagination(new PaginationViewModel(pageResource, originQuery));
        model.setInnovationSectorOptions(getInnovationSectors());

        return model;
    }

    private List<InnovationSectorResource> getInnovationSectors() {
        return categoryService.getInnovationSectors();
    }


    private InvitedAssessorRowViewModel getRowViewModel(AssessorCreatedInviteResource assessorCreatedInviteResource) {
        return new InvitedAssessorRowViewModel(
                assessorCreatedInviteResource.getId(),
                assessorCreatedInviteResource.getName(),
                assessorCreatedInviteResource.getInnovationAreas(),
                assessorCreatedInviteResource.isCompliant(),
                assessorCreatedInviteResource.getEmail(),
                assessorCreatedInviteResource.getInviteId()
        );
    }

    @Override
    protected InviteAssessorsInviteViewModel createModel() {
        return new InviteAssessorsInviteViewModel();
    }
}
