package org.innovateuk.ifs.management.model;

import org.innovateuk.ifs.application.builder.ApplicationSummaryResourceBuilder;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationSummaryService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.management.viewmodel.CompetitionInFlightStatsViewModel;
import org.innovateuk.ifs.management.viewmodel.SendNotificationsViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.innovateuk.ifs.application.resource.FundingDecision.FUNDED;
import static org.innovateuk.ifs.application.resource.FundingDecision.ON_HOLD;
import static org.innovateuk.ifs.application.resource.FundingDecision.UNFUNDED;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendNotificationsModelPopulatorTest {

    public static final Long COMPETITION_ID = 7L;
    public static final String COMPETITION_NAME = "name";

    @InjectMocks
    private SendNotificationsModelPopulator sendNotificationsModelPopulator;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private CompetitionInFlightStatsModelPopulator competitionInFlightStatsModelPopulatorMock;

    @Mock
    private ApplicationSummaryService applicationSummaryServiceMock;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void populateModel() throws Exception {

        CompetitionResource competition = CompetitionResourceBuilder.newCompetitionResource().withName(COMPETITION_NAME).build();

        CompetitionInFlightStatsViewModel keyStatistics = new CompetitionInFlightStatsViewModel();

        ApplicationSummaryResource application1
                = ApplicationSummaryResourceBuilder.newApplicationSummaryResource().withId(1L).withFundingDecision(ON_HOLD).build();
        ApplicationSummaryResource application2
                = ApplicationSummaryResourceBuilder.newApplicationSummaryResource().withId(2L).withFundingDecision(FUNDED).build();
        ApplicationSummaryResource application3
                = ApplicationSummaryResourceBuilder.newApplicationSummaryResource().withId(3L).withFundingDecision(UNFUNDED).build();

        ApplicationSummaryPageResource applicationResults = new ApplicationSummaryPageResource();
        applicationResults.setContent(Arrays.asList(application1, application2, application3));

        when(applicationSummaryServiceMock.findByCompetitionId(COMPETITION_ID, null, null, null, null)).thenReturn(applicationResults);
        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);
        when(competitionInFlightStatsModelPopulatorMock.populateStatsViewModel(competition)).thenReturn(keyStatistics);

        List<Long> requestedIds = Arrays.asList(application1.getId(), application3.getId());
        List<ApplicationSummaryResource> expectedApplications = Arrays.asList(application1, application3);
        Map<Long, FundingDecision> expectedDecisions = asMap(application1.getId(), ON_HOLD, application3.getId(), UNFUNDED);

        SendNotificationsViewModel viewModel = sendNotificationsModelPopulator.populate(COMPETITION_ID, requestedIds);

        assertThat(viewModel.getCompetitionId(), is(equalTo(COMPETITION_ID)));
        assertThat(viewModel.getCompetitionName(), is(equalTo(COMPETITION_NAME)));
        assertThat(viewModel.getApplications(), is(equalTo(expectedApplications)));
        assertThat(viewModel.getKeyStatistics(), is(equalTo(keyStatistics)));
        assertThat(viewModel.getFundingDecisions(), is(equalTo(expectedDecisions)));
    }
}