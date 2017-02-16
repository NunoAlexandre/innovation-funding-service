package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.application.service.ApplicationCountSummaryRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.management.model.ManageApplicationsModelPopulator;
import org.innovateuk.ifs.management.viewmodel.ManageApplicationsViewModel;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.List;

import static org.innovateuk.ifs.application.builder.ApplicationCountSummaryResourceBuilder.newApplicationCountSummaryResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.IN_ASSESSMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ApplicationAssessmentManagementControllerTest extends BaseControllerMockMVCTest<ApplicationAssessmentManagementController> {

    @Mock
    private ApplicationCountSummaryRestService applicationCountSummaryRestService;

    @InjectMocks
    @Spy
    private ManageApplicationsModelPopulator manageApplicationsPopulator;

    @Override
    protected ApplicationAssessmentManagementController supplyControllerUnderTest() {
        return new ApplicationAssessmentManagementController();
    }

    @Test
    public void testManageApplications() throws Exception {
        CompetitionResource competitionResource = newCompetitionResource()
                .withName("name")
                .withCompetitionStatus(IN_ASSESSMENT)
                .build();

        List<ApplicationCountSummaryResource> summaryResources = newApplicationCountSummaryResource()
                .withName("one", "two")
                .withLeadOrganisation("Lead Org 1", "Lead Org 2")
                .withAccepted(2L, 3L)
                .withAssessors(3L, 4L)
                .withSubmitted(1L, 2L).build(2);
        when(competitionService.getById(competitionResource.getId())).thenReturn(competitionResource);
        when(applicationCountSummaryRestService.getApplicationCountSummariesByCompetitionId(competitionResource.getId())).thenReturn(restSuccess(summaryResources));

        ManageApplicationsViewModel model = (ManageApplicationsViewModel) mockMvc.perform(get("/assessment/competition/{competitionId}", competitionResource.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("competition/manage-applications"))
                .andExpect(model().attributeExists("model"))
                .andReturn().getModelAndView().getModel().get("model");

        assertEquals(competitionResource.getId(), model.getCompetitionId());
        assertEquals(competitionResource.getName(), model.getCompetitionName());
        assertTrue(model.getInAssessment());
        assertEquals(2, model.getApplications().size());
        assertEquals(2L, model.getApplications().get(0).getAccepted());
        assertEquals(3L, model.getApplications().get(0).getAssessors());
        assertEquals(1L, model.getApplications().get(0).getCompleted());
        assertEquals("Lead Org 1", model.getApplications().get(0).getLeadOrganisation());

        assertEquals(3L, model.getApplications().get(1).getAccepted());
        assertEquals(4L, model.getApplications().get(1).getAssessors());
        assertEquals(2L, model.getApplications().get(1).getCompleted());
        assertEquals("Lead Org 2", model.getApplications().get(1).getLeadOrganisation());
    }
}
