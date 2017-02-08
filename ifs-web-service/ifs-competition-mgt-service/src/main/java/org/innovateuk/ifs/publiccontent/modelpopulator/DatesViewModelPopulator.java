package org.innovateuk.ifs.publiccontent.modelpopulator;


import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentEventResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.publiccontent.viewmodel.DatesViewModel;
import org.innovateuk.ifs.publiccontent.viewmodel.submodel.DateViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatesViewModelPopulator extends AbstractPublicContentViewModelPopulator<DatesViewModel> implements PublicContentViewModelPopulator<DatesViewModel> {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private MilestoneService milestoneService;

    @Override
    protected DatesViewModel createInitial() {
        return new DatesViewModel();
    }

    @Override
    protected void populateSection(DatesViewModel model, PublicContentResource publicContentResource, PublicContentSectionResource section) {
        List<MilestoneResource> milestonesNeeded = milestoneService.getAllMilestonesByCompetitionId(publicContentResource.getCompetitionId()).stream()
                .filter(milestoneResource -> MilestoneType.RELEASE_FEEDBACK.equals(milestoneResource.getType()) ||
                        MilestoneType.OPEN_DATE.equals(milestoneResource.getType()) ||
                        MilestoneType.SUBMISSION_DATE.equals(milestoneResource.getType()))
                .collect(Collectors.toList());

        List<DateViewModel> dates = new ArrayList<>();

        milestonesNeeded.forEach(milestoneResource -> {
            dates.add(mapMilestoneToDateViewModel(milestoneResource));
        });

        if(model.isReadOnly()) {
            publicContentResource.getContentEvents().forEach(publicContentEventResource -> {
                dates.add(mapContentEventDateViewModel(publicContentEventResource));
            });
        }

        model.setPublicContentDates(dates);
    }

    private DateViewModel mapContentEventDateViewModel(PublicContentEventResource publicContentEventResource) {
        DateViewModel dateViewModel = new DateViewModel();
        dateViewModel.setDateTime(publicContentEventResource.getDate());
        dateViewModel.setContent(publicContentEventResource.getContent());

        return dateViewModel;
    }

    private DateViewModel mapMilestoneToDateViewModel(MilestoneResource milestoneResource) {
        DateViewModel dateViewModel = new DateViewModel();
        dateViewModel.setDateTime(milestoneResource.getDate());
        switch (milestoneResource.getType()) {
            case OPEN_DATE:
                dateViewModel.setContent("Competition opens");
                break;
            case SUBMISSION_DATE:
                dateViewModel.setContent("Submission deadline, competition closed.");
                break;
            case RELEASE_FEEDBACK:
                dateViewModel.setContent("Applicants notified");
                break;
        }

        return dateViewModel;
    }

    @Override
    protected PublicContentSectionType getType() {
        return PublicContentSectionType.DATES;
    }
}
