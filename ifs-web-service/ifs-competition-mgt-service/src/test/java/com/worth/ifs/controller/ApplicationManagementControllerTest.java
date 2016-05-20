package com.worth.ifs.controller;

import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.controller.viewmodel.AssessorFeedbackViewModel;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.worth.ifs.application.service.Futures.settable;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.competition.resource.CompetitionResource.Status.ASSESSOR_FEEDBACK;
import static com.worth.ifs.competition.resource.CompetitionResource.Status.FUNDERS_PANEL;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationManagementControllerTest extends BaseControllerMockMVCTest<ApplicationManagementController>{

    @Test
    public void testDisplayApplicationForCompetitionAdministrator() throws Exception {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.loginDefaultUser();
        this.setupInvites();
        this.setupOrganisationTypes();

        assertApplicationOverviewExpectations(AssessorFeedbackViewModel.withNoFile(true));
    }

    @Test
    public void testDisplayApplicationForCompetitionAdministratorWithCorrectAssessorFeedbackReadonly() throws Exception {

        asList(CompetitionResource.Status.values()).forEach(status -> {

            this.setupCompetition();
            this.setupApplicationWithRoles();
            this.loginDefaultUser();
            this.setupInvites();
            this.setupOrganisationTypes();

            competitionResource.setCompetitionStatus(status);

            boolean expectedReadonlyState = !asList(FUNDERS_PANEL, ASSESSOR_FEEDBACK).contains(status);

            assertApplicationOverviewExpectations(AssessorFeedbackViewModel.withNoFile(expectedReadonlyState));
        });
    }

    @Test
    public void testDisplayApplicationForCompetitionAdministratorWithCorrectAssessorFeedbackFileEntry() throws Exception {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.loginDefaultUser();
        this.setupInvites();
        this.setupOrganisationTypes();

        applications.get(0).setAssessorFeedbackFileEntry(123L);

        FileEntryResource existingFileEntry = newFileEntryResource().withName("myfile").build();
        when(assessorFeedbackRestService.getAssessorFeedbackFileDetails(applications.get(0).getId())).thenReturn(restSuccess(existingFileEntry));
        assertApplicationOverviewExpectations(AssessorFeedbackViewModel.withExistingFile("myfile", true));
    }

    @Test
    public void testDownloadAssessorFeedbackFile() throws Exception {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.loginDefaultUser();
        this.setupInvites();
        this.setupOrganisationTypes();

        ByteArrayResource fileContents = new ByteArrayResource("The returned file data".getBytes());
        when(assessorFeedbackRestService.getAssessorFeedbackFile(applications.get(0).getId())).thenReturn(restSuccess(fileContents));

        mockMvc.perform(get("/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId() + "/assessorFeedback") )
                .andExpect(status().isOk())
                .andExpect(content().string("The returned file data"));
    }

    @Test
    public void testUploadAssessorFeedbackFile() throws Exception {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.loginDefaultUser();
        this.setupInvites();
        this.setupOrganisationTypes();

        MockMultipartFile uploadedFile = new MockMultipartFile("assessorFeedback", "filename.txt", "text/plain", "Content to upload".getBytes());

        FileEntryResource successfulCreationResult = newFileEntryResource().build();

        when(assessorFeedbackRestService.addAssessorFeedbackDocument(
                applications.get(0).getId(), "text/plain", 17L, "filename.txt", "Content to upload".getBytes())).
                thenReturn(restSuccess(successfulCreationResult));

        mockMvc.perform(fileUpload("/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId()).
                    file(uploadedFile).
                    param("uploadAssessorFeedback", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId()));

        verify(assessorFeedbackRestService).addAssessorFeedbackDocument(
                applications.get(0).getId(), "text/plain", 17L, "filename.txt", "Content to upload".getBytes());
    }

    @Test
    public void testRemoveAssessorFeedbackFile() throws Exception {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.loginDefaultUser();
        this.setupInvites();
        this.setupOrganisationTypes();

        when(assessorFeedbackRestService.removeAssessorFeedbackDocument(applications.get(0).getId())).thenReturn(restSuccess());

        mockMvc.perform(post("/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId()).
                    param("removeAssessorFeedback", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId()));

        verify(assessorFeedbackRestService).removeAssessorFeedbackDocument(applications.get(0).getId());
    }

    private void assertApplicationOverviewExpectations(AssessorFeedbackViewModel expectedAssessorFeedback) {
        Map<Long, FormInputResponseResource> mappedFormInputResponsesToFormInput = new HashMap<>();

        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));

        try {
            mockMvc.perform(get("/competition/" + competitionResource.getId() + "/application/" + applications.get(0).getId()) )
                    .andExpect(status().isOk())
                    .andExpect(view().name("competition-mgt-application-overview"))
                    .andExpect(model().attribute("applicationReadyForSubmit", false))
                    .andExpect(model().attribute("isCompManagementDownload", true))
                    .andExpect(model().attribute("responses", mappedFormInputResponsesToFormInput))
                    .andExpect(model().attribute("assessorFeedback", expectedAssessorFeedback));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ApplicationManagementController supplyControllerUnderTest() {
        return new ApplicationManagementController();
    }
}
