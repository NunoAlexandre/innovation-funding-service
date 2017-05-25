package org.innovateuk.ifs.project.status.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.status.resource.CompetitionProjectsStatusResource;
import org.innovateuk.ifs.project.status.viewmodel.CompetitionProjectStatusViewModel;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.any;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.project.builder.CompetitionProjectsStatusResourceBuilder.newCompetitionProjectsStatusResource;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CompetitionStatusControllerTest extends BaseControllerMockMVCTest<CompetitionStatusController> {

    @Test
    public void testViewCompetitionStatusPage() throws Exception {
        Long competitionId = 123L;

        CompetitionProjectsStatusResource competitionProjectsStatus = newCompetitionProjectsStatusResource().build();

        when(projectStatusRestService.getCompetitionStatus(competitionId)).thenReturn(restSuccess(competitionProjectsStatus));

        mockMvc.perform(get("/competition/" + competitionId + "/status"))
                .andExpect(view().name("project/competition-status"))
                .andExpect(model().attribute("model", any(CompetitionProjectStatusViewModel.class)))
                .andReturn();
    }

    @Test
    public void exportBankDetails() throws Exception {

        Long competitionId = 123L;

        ByteArrayResource result = new ByteArrayResource("My content!".getBytes());

        when(bankDetailsRestService.downloadByCompetition(competitionId)).thenReturn(restSuccess(result));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

        mockMvc.perform(get("/competition/123/status/bank-details/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(("text/csv")))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-disposition", "attachment;filename=" + String.format("Bank_details_%s_%s.csv", competitionId, ZonedDateTime.now().format(formatter))))
                .andExpect(content().string("My content!"));

        verify(bankDetailsRestService).downloadByCompetition(123L);
    }

    @Override
    protected CompetitionStatusController supplyControllerUnderTest() {
        return new CompetitionStatusController();
    }
}
