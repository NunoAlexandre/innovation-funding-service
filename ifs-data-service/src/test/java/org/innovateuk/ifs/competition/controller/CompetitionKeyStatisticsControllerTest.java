package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.resource.CompetitionClosedKeyStatisticsResource;
import org.innovateuk.ifs.competition.resource.CompetitionInAssessmentKeyStatisticsResource;
import org.innovateuk.ifs.competition.resource.CompetitionOpenKeyStatisticsResource;
import org.innovateuk.ifs.competition.resource.CompetitionReadyToOpenKeyStatisticsResource;
import org.junit.Test;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionClosedKeyStatisticsResourceBuilder.newCompetitionClosedKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionInAssessmentKeyStatisticsResourceBuilder.newCompetitionInAssessmentKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionOpenKeyStatisticsResourceBuilder.newCompetitionOpenKeyStatisticsResource;
import static org.innovateuk.ifs.competition.builder.CompetitionReadyToOpenKeyStatisticsResourceBuilder.newCompetitionReadyToOpenKeyStatisticsResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CompetitionKeyStatisticsControllerTest extends BaseControllerMockMVCTest<CompetitionKeyStatisticsController> {

    @Override
    protected CompetitionKeyStatisticsController supplyControllerUnderTest() {
        return new CompetitionKeyStatisticsController();
    }

    @Test
    public void getReadyToOpenKeyStatistics() throws Exception {
        final long competitionId = 1L;

        CompetitionReadyToOpenKeyStatisticsResource keyStatisticsResource = newCompetitionReadyToOpenKeyStatisticsResource().build();

        when(competitionKeyStatisticsServiceMock.getReadyToOpenKeyStatisticsByCompetition(competitionId)).thenReturn(serviceSuccess(keyStatisticsResource));

        mockMvc.perform(get("/competitionStatistics/{id}/readyToOpen", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(keyStatisticsResource)));
    }

    @Test
    public void getOpenKeyStatistics() throws Exception {
        final long competitionId = 1L;

        CompetitionOpenKeyStatisticsResource keyStatisticsResource = newCompetitionOpenKeyStatisticsResource().build();
        when(competitionKeyStatisticsServiceMock.getOpenKeyStatisticsByCompetition(competitionId)).thenReturn(serviceSuccess(keyStatisticsResource));

        mockMvc.perform(get("/competitionStatistics/{id}/open", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(keyStatisticsResource)));
    }

    @Test
    public void getClosedKeyStatistics() throws Exception {
        final long competitionId = 1L;

        CompetitionClosedKeyStatisticsResource keyStatisticsResource = newCompetitionClosedKeyStatisticsResource().build();
        when(competitionKeyStatisticsServiceMock.getClosedKeyStatisticsByCompetition(competitionId)).thenReturn(serviceSuccess(keyStatisticsResource));

        mockMvc.perform(get("/competitionStatistics/{id}/closed", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(keyStatisticsResource)));
    }

    @Test
    public void getInAssessmentKeyStatistics() throws Exception {
        final long competitionId = 1L;

        CompetitionInAssessmentKeyStatisticsResource keyStatisticsResource = newCompetitionInAssessmentKeyStatisticsResource().build();
        when(competitionKeyStatisticsServiceMock.getInAssessmentKeyStatisticsByCompetition(competitionId)).thenReturn(serviceSuccess(keyStatisticsResource));

        mockMvc.perform(get("/competitionStatistics/{id}/inAssessment", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(keyStatisticsResource)));
    }

}
