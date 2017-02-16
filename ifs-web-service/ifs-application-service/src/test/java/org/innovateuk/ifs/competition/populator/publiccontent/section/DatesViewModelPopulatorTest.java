package org.innovateuk.ifs.competition.populator.publiccontent.section;


import org.innovateuk.ifs.application.service.MilestoneService;
import org.innovateuk.ifs.competition.publiccontent.resource.FundingType;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.section.DatesViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.competition.builder.MilestoneResourceBuilder.newMilestoneResource;
import static org.innovateuk.ifs.publiccontent.builder.ContentEventResourceBuilder.newContentEventResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentResourceBuilder.newPublicContentResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentSectionResourceBuilder.newPublicContentSectionResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Testing {@link DatesViewModelPopulator}
 */
@RunWith(MockitoJUnitRunner.class)
public class DatesViewModelPopulatorTest {

    @Mock
    private MilestoneService milestoneService;

    @InjectMocks
    private DatesViewModelPopulator populator;

    private DatesViewModel viewModel;
    private PublicContentResource publicContentResource;
    private PublicContentSectionResource publicContentSectionResource;

    @Before
    public void setup() {
        viewModel = new DatesViewModel();

        publicContentSectionResource = newPublicContentSectionResource()
                .with(sectionResource -> {
                    sectionResource.setId(98125L);
                })
                .withPublicContent(1L)
                .build();
        publicContentResource = newPublicContentResource()
                .with(contentResource ->  {
                    contentResource.setId(89235L);
                })
                .withCompetitionId(5372L)
                .withSummary("Summary")
                .withFundingType(FundingType.GRANT)
                .withProjectSize("5M")
                .withContentSections(asList(publicContentSectionResource))
                .build();
    }

    @Test
    public void populateSectionWithMilestonesFound() {
        when(milestoneService.getAllPublicMilestonesByCompetitionId(publicContentResource.getCompetitionId()))
                .thenReturn(newMilestoneResource()
                        .withType(MilestoneType.OPEN_DATE, MilestoneType.RELEASE_FEEDBACK, MilestoneType.SUBMISSION_DATE)
                        .build(3));
        publicContentResource.setContentEvents(Collections.emptyList());

        populator.populateSection(viewModel, publicContentResource, publicContentSectionResource);

        assertEquals(3, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithMilestonesNotFound() {
        when(milestoneService.getAllPublicMilestonesByCompetitionId(publicContentResource.getCompetitionId()))
                .thenReturn(Collections.emptyList());
        publicContentResource.setContentEvents(Collections.emptyList());

        populator.populateSection(viewModel, publicContentResource, publicContentSectionResource);

        assertEquals(0, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithNoPublicContentDates() {
        when(milestoneService.getAllPublicMilestonesByCompetitionId(publicContentResource.getCompetitionId()))
                .thenReturn(Collections.emptyList());
        publicContentResource.setContentEvents(Collections.emptyList());

        populator.populateSection(viewModel, publicContentResource, publicContentSectionResource);

        assertEquals(0, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithPublicContentDatesAndMilestones() {
        when(milestoneService.getAllPublicMilestonesByCompetitionId(publicContentResource.getCompetitionId()))
                .thenReturn(newMilestoneResource()
                        .withType(MilestoneType.OPEN_DATE, MilestoneType.RELEASE_FEEDBACK, MilestoneType.SUBMISSION_DATE)
                        .build(3));
        publicContentResource.setContentEvents(newContentEventResource().build(2));

        populator.populateSection(viewModel, publicContentResource, publicContentSectionResource);

        assertEquals(5, viewModel.getPublicContentDates().size());
    }

    @Test
    public void getType() {
        assertEquals(PublicContentSectionType.DATES, populator.getType());
    }
}
