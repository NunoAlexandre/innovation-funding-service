package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.model.UpcomingCompetitionModelPopulator;
import org.innovateuk.ifs.assessment.viewmodel.UpcomingCompetitionViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource("classpath:application.properties")
public class UpcomingCompetitionControllerTest extends BaseControllerMockMVCTest<UpcomingCompetitionController> {

    @Spy
    @InjectMocks
    private UpcomingCompetitionModelPopulator upcomingCompetitionModelPopulator;

    private static final String restUrl = "/competition";

    @Override
    protected UpcomingCompetitionController supplyControllerUnderTest() {
        return new UpcomingCompetitionController();
    }

    @Test
    public void viewSummary_loggedIn() throws Exception {
        LocalDateTime dateTime = LocalDateTime.now();

        CompetitionResource competitionResource = newCompetitionResource()
                .withId(1L)
                .withName("name")
                .withDescription("description")
                .withAssessorAcceptsDate(dateTime)
                .withAssessorDeadlineDate(dateTime)
                .build();

        UpcomingCompetitionViewModel expectedViewModel = new UpcomingCompetitionViewModel(competitionResource);

        when(competitionRestService.getCompetitionById(1L)).thenReturn(restSuccess(competitionResource));

        mockMvc.perform(get(restUrl + "/{competitionId}/upcoming", "1"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(status().isOk());

        verify(competitionRestService).getCompetitionById(1L);
    }

    @Test
    public void viewSummary_competitionNotExists() throws Exception {
        when(competitionRestService.getCompetitionById(1L)).thenReturn(restFailure(notFoundError(CompetitionResource.class, "notExistId")));

        mockMvc.perform(get(restUrl + "/{competitionId}/upcoming", "1"))
                .andExpect(model().attributeDoesNotExist("model"))
                .andExpect(status().isNotFound());

        verify(competitionRestService).getCompetitionById(1L);
    }
}
