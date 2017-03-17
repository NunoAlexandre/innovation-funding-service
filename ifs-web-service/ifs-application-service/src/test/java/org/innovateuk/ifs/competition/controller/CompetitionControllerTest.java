package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.populator.CompetitionOverviewPopulator;
import org.innovateuk.ifs.competition.populator.publiccontent.section.SummaryViewModelPopulator;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.viewmodel.CompetitionOverviewViewModel;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.AbstractPublicSectionContentViewModel;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.section.SummaryViewModel;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentItemResourceBuilder.newPublicContentItemResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentResourceBuilder.newPublicContentResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CompetitionControllerTest extends BaseControllerMockMVCTest<CompetitionController> {

    @Override
    protected CompetitionController supplyControllerUnderTest() {
        return new CompetitionController();
    }

    @Mock
    private CompetitionOverviewPopulator overviewPopulator;

    @Mock
    private AbstractPublicSectionContentViewModel sectionContentViewModel;

    @Mock
    private SummaryViewModelPopulator summaryViewModelPopulator;

    @Before
    public void setup() {
        when(sectionContentViewModel.getSectionType()).thenReturn(PublicContentSectionType.SUMMARY);
        when(summaryViewModelPopulator.getType()).thenReturn(PublicContentSectionType.SUMMARY);
        when(summaryViewModelPopulator.populate(any(PublicContentResource.class), anyBoolean())).thenReturn(new SummaryViewModel());
        controller.setSectionPopulator(asList(summaryViewModelPopulator));
    }

    @Test
    public void testCompetitionOverview() throws Exception {
        final Long compId = 20L;
        final LocalDateTime openDate = LocalDateTime.of(2017,1,1,0,0);
        final LocalDateTime closeDate = LocalDateTime.of(2017,1,1,0,0);
        final String competitionTitle = "Title of competition";
        final PublicContentResource publicContentResource = newPublicContentResource().build();

        PublicContentItemResource publicContentItem = newPublicContentItemResource()
                .withCompetitionOpenDate(openDate)
                .withCompetitionCloseDate(closeDate)
                .withCompetitionTitle(competitionTitle)
                .withContentSection(publicContentResource)
                .build();
        when(competitionService.getPublicContentOfCompetition(compId)).thenReturn(publicContentItem);

        CompetitionOverviewViewModel viewModel = new CompetitionOverviewViewModel();
        viewModel.setCompetitionOpenDate(openDate);
        viewModel.setCompetitionCloseDate(closeDate);
        viewModel.setCompetitionTitle("Title");
        viewModel.setUserIsLoggedIn(false);

        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(null);
        when(overviewPopulator.populateViewModel(any(PublicContentItemResource.class), anyBoolean(), any(AbstractPublicSectionContentViewModel.class))).thenReturn(viewModel);

        mockMvc.perform(get("/competition/{id}/overview", compId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name("competition/overview"));
    }


    @Test
    public void testCompetitionDetailsCompetitionId() throws Exception {
        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();
        loginUser(user);

        Long compId = 20L;

        CompetitionResource competition = newCompetitionResource().with(target -> setField("id", compId, target)).build();
        when(competitionService.getPublishedById(compId)).thenReturn(competition);

        mockMvc.perform(get("/competition/{id}/details/", compId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentCompetition", competition))
                .andExpect(model().attribute("userIsLoggedIn", true))
                .andExpect(view().name("competition/details"));
    }


    @Test
    public void testCompetitionDetailsWithInvalidAuthentication() throws Exception {
        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();;
        loginUser(user);

        Long compId = 20L;

        CompetitionResource competition = newCompetitionResource().with(target -> setField("id", compId, target)).build();
        when(competitionService.getPublishedById(compId)).thenReturn(competition);
        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(get("/competition/{id}/details/", compId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentCompetition", competition))
                .andExpect(model().attribute("userIsLoggedIn", false))
                .andExpect(view().name("competition/details"));
    }


    @Test
    public void testCompetitionInfo() throws Exception {
        UserResource user = newUserResource().withId(1L).withFirstName("test").withLastName("name").build();;
        loginUser(user);

        Long compId = 20L;
        String templateName = "a string";

        CompetitionResource competition = newCompetitionResource().with(target -> setField("id", compId, target)).build();
        when(competitionService.getPublishedById(compId)).thenReturn(competition);

        mockMvc.perform(get("/competition/{id}/info/{templateName}", compId, templateName))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentCompetition", competition))
                .andExpect(model().attribute("userIsLoggedIn", true))
                .andExpect(view().name("competition/info/" + templateName));
    }
}
