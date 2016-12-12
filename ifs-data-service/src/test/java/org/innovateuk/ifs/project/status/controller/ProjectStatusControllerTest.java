package org.innovateuk.ifs.project.status.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.status.resource.CompetitionProjectsStatusResource;
import org.junit.Test;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.project.builder.CompetitionProjectsStatusResourceBuilder.newCompetitionProjectsStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectStatusResourceBuilder.newProjectStatusResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectStatusControllerTest extends BaseControllerMockMVCTest<ProjectStatusController> {

    @Test
    public void testGetCompetitionStatus() throws Exception {
        final Long competitionId = 123L;
        final CompetitionProjectsStatusResource cpsr = newCompetitionProjectsStatusResource().
                withCompetitionName("ABC").
                withCompetitionNumber(competitionId).
                withProjectStatusResources(newProjectStatusResource().withProjectNumber().build(3)).build();
        ServiceResult<CompetitionProjectsStatusResource> expected = serviceSuccess(cpsr);
        when(projectStatusServiceMock.getCompetitionStatus(competitionId)).thenReturn(expected);

        mockMvc.perform(get("/project/competition/{competitionId}", 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(cpsr)));

        verify(projectStatusServiceMock).getCompetitionStatus(competitionId);
    }

    @Override
    protected ProjectStatusController supplyControllerUnderTest() {
        return new ProjectStatusController();
    }
}
