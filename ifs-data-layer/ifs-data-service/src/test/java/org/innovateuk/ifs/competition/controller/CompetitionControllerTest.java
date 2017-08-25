package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompetitionControllerTest extends BaseControllerMockMVCTest<CompetitionController> {

    @Override
    protected CompetitionController supplyControllerUnderTest() {
        return new CompetitionController();
    }


    @Test
    public void getCompetitionsByUserId() throws Exception {
        final Long userId = 1L;

        when(competitionServiceMock.getCompetitionsByUserId(userId)).thenReturn(serviceSuccess(newCompetitionResource().build(1)));

        mockMvc.perform(get("/competition/getCompetitionsByUserId/{userId}", userId))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).getCompetitionsByUserId(userId);
    }

    @Test
    public void notifyAssessors() throws Exception {
        final Long competitionId = 1L;

        when(competitionServiceMock.notifyAssessors(competitionId)).thenReturn(serviceSuccess());
        when(assessorServiceMock.notifyAssessorsByCompetition(competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/{id}/notify-assessors", competitionId))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).notifyAssessors(competitionId);
        verify(assessorServiceMock).notifyAssessorsByCompetition(competitionId);
    }

    @Test
    public void closeAssessment() throws Exception {
        final Long competitionId = 1L;

        when(competitionServiceMock.closeAssessment(competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/{id}/close-assessment", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(competitionServiceMock, only()).closeAssessment(competitionId);

    }

    @Test
    public void findInnovationLeads() throws Exception {
        final Long competitionId = 1L;

        List<UserResource> innovationLeads = new ArrayList<>();
        when(competitionServiceMock.findInnovationLeads(competitionId)).thenReturn(serviceSuccess(innovationLeads));

        mockMvc.perform(get("/competition/{id}/innovation-leads", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(innovationLeads)));

        verify(competitionServiceMock, only()).findInnovationLeads(competitionId);

    }

    @Test
    public void addInnovationLead() throws Exception {
        final Long competitionId = 1L;
        final Long innovationLeadUserId = 2L;

        when(competitionServiceMock.addInnovationLead(competitionId, innovationLeadUserId )).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competition/{id}/add-innovation-lead/{innovationLeadUserId}", competitionId, innovationLeadUserId))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).addInnovationLead(competitionId, innovationLeadUserId);

    }

    @Test
    public void removeInnovationLead() throws Exception {
        final Long competitionId = 1L;
        final Long innovationLeadUserId = 2L;

        when(competitionServiceMock.removeInnovationLead(competitionId, innovationLeadUserId )).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competition/{id}/remove-innovation-lead/{innovationLeadUserId}", competitionId, innovationLeadUserId))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).removeInnovationLead(competitionId, innovationLeadUserId);

    }

    @Test
    public void releaseFeedback() throws Exception {
        final Long competitionId = 1L;

        when(competitionServiceMock.releaseFeedback(competitionId)).thenReturn(serviceSuccess());
        when(applicationServiceMock.notifyApplicantsByCompetition(competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/{id}/release-feedback", competitionId))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).releaseFeedback(competitionId);
        verify(applicationServiceMock).notifyApplicantsByCompetition(competitionId);
    }
}
