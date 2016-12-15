package org.innovateuk.ifs.project.grantofferletter.send;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.file.builder.FileEntryResourceBuilder;
import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.grantofferletter.send.controller.ProjectGrantOfferLetterSendController;
import org.innovateuk.ifs.project.grantofferletter.send.form.ProjectGrantOfferLetterSendForm;
import org.innovateuk.ifs.project.grantofferletter.send.viewmodel.ProjectGrantOfferLetterSendViewModel;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.CompetitionSummaryResourceBuilder.newCompetitionSummaryResource;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.FILES_UNABLE_TO_CREATE_FILE;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_NOT_FOUND;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class GrantOfferLetterControllerTest extends BaseControllerMockMVCTest<ProjectGrantOfferLetterSendController> {
    @Test
    public void testView() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/send")).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel golViewModel = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");

        assertFalse(golViewModel.isSentToProjectTeam());
        assertEquals(null, golViewModel.getGrantOfferLetterFile());
        assertEquals(null, golViewModel.getAdditionalContractFile());
        assertEquals(null, golViewModel.getSignedGrantOfferLetterFile());
        assertFalse(golViewModel.getAdditionalContractFileContentAvailable());
        assertFalse(golViewModel.getGrantOfferLetterFileContentAvailable());

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(form.getAnnex(), null);
    }

    @Test
    public void testSendGOLSuccess() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        when(projectService.sendGrantOfferLetter(projectId)).thenReturn(serviceSuccess());

        // re-load model after sending GOL
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.TRUE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());


        MvcResult result = mockMvc.perform(post("/project/" + projectId + "/grant-offer-letter/send")).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel golViewModel = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");

        assertTrue(golViewModel.isSentToProjectTeam());
        assertEquals(null, golViewModel.getGrantOfferLetterFile());
        assertEquals(null, golViewModel.getAdditionalContractFile());
        assertEquals(null, golViewModel.getSignedGrantOfferLetterFile());
        assertFalse(golViewModel.getAdditionalContractFileContentAvailable());
        assertFalse(golViewModel.getGrantOfferLetterFileContentAvailable());
        assertEquals(Boolean.TRUE, golViewModel.isSentToProjectTeam());

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(form.getAnnex(), null);
    }

    @Test
    public void testSendGOLFailure() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        when(projectService.sendGrantOfferLetter(projectId)).thenReturn(serviceSuccess());

        // re-load model after sending GOL
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());


        MvcResult result = mockMvc.perform(post("/project/" + projectId + "/grant-offer-letter/send")).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel golViewModel = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");

        assertFalse(golViewModel.isSentToProjectTeam());
        assertEquals(null, golViewModel.getGrantOfferLetterFile());
        assertEquals(null, golViewModel.getAdditionalContractFile());
        assertEquals(null, golViewModel.getSignedGrantOfferLetterFile());
        assertFalse(golViewModel.getAdditionalContractFileContentAvailable());
        assertFalse(golViewModel.getGrantOfferLetterFileContentAvailable());
        assertEquals(Boolean.FALSE, golViewModel.isSentToProjectTeam());

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(form.getAnnex(), null);
    }

    @Test
    public void testDownloadGOLFileEntryNotPresent() throws Exception {

        Long projectId = 1L;

        when(projectService.getGeneratedGrantOfferFile(projectId)).thenReturn(Optional.empty());
        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/grant-offer-letter"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        // Assert that there is no content
        assertEquals("", response.getContentAsString());
        assertEquals(null, response.getHeader("Content-Disposition"));
        assertEquals(0, response.getContentLength());
    }

    @Test
    public void testDownloadGOLFileSuccess() throws Exception {

        Long projectId = 1L;

        FileEntryResource golFileEntryResource = FileEntryResourceBuilder.newFileEntryResource()
                .withName("gol-file.pdf")
                .build();
        byte[] content = "HelloWorld".getBytes();
        ByteArrayResource golByteArrayResource = new ByteArrayResource(content);

        when(projectService.getGeneratedGrantOfferFile(projectId)).thenReturn(Optional.of(golByteArrayResource));
        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.of(golFileEntryResource));

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/grant-offer-letter"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals("HelloWorld", response.getContentAsString());
        assertEquals("inline; filename=\"gol-file.pdf\"", response.getHeader("Content-Disposition"));
        assertEquals(10, response.getContentLength());
    }

    @Test
    public void uploadGrantOfferLetterFile() throws Exception {

        Long projectId = 123L;
        Long competitionId = 1L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();

        Optional<FileEntryResource> golFileEntryResource = Optional.of(FileEntryResourceBuilder.newFileEntryResource().withName("1").withMediaType("application/pdf").withFilesizeBytes(11).build());

        // when the model is re-loaded after uploading
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(golFileEntryResource);
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());


        FileEntryResource createdFileDetails = newFileEntryResource().withName("1").withMediaType("application/pdf").withFilesizeBytes(11).build();

        when(projectService.addGrantOfferLetter(123L, "application/pdf", 11, "grantOfferLetter.pdf", "My content!".getBytes())).
                thenReturn(serviceSuccess(createdFileDetails));

        MockMultipartFile uploadedFile = new MockMultipartFile("grantOfferLetter", "grantOfferLetter.pdf", "application/pdf", "My content!".getBytes());

        MvcResult result = mockMvc.perform(
                fileUpload("/project/"+ projectId  + "/grant-offer-letter/grant-offer-letter").
                        file(uploadedFile).param("uploadGrantOfferLetterClicked", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(uploadedFile, form.getGrantOfferLetter());

        ProjectGrantOfferLetterSendViewModel model = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(new FileDetailsViewModel(createdFileDetails), model.getGrantOfferLetterFile());
        assertEquals(null, model.getSignedGrantOfferLetterFile());
        assertTrue(model.getGrantOfferLetterFileContentAvailable());

    }

    @Test
    public void removeGrantOfferLetterFile() throws Exception {

        Long projectId = 123L;
        Long competitionId = 1L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();

        // when the model is re-loaded
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());


        when(projectService.removeGrantOfferLetter(123L)).
                thenReturn(serviceSuccess());

        MockMultipartFile fileToDelete = new MockMultipartFile("grantOfferLetter", "grantOfferLetter.pdf", "application/pdf", "My content!".getBytes());

        MvcResult result = mockMvc.perform(
                fileUpload("/project/"+ projectId  + "/grant-offer-letter/grant-offer-letter").
                        file(fileToDelete).param("removeGrantOfferLetterClicked", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel model = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");
        assertFalse(model.getGrantOfferLetterFileContentAvailable());

    }

    @Test
    public void testDownloadAnnexFileEntryNotPresent() throws Exception {

        Long projectId = 1L;

        when(projectService.getAdditionalContractFile(projectId)).thenReturn(Optional.empty());
        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/additional-contract"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        // Assert that there is no content
        assertEquals("", response.getContentAsString());
        assertEquals(null, response.getHeader("Content-Disposition"));
        assertEquals(0, response.getContentLength());
    }

    @Test
    public void testDownloadAnnexFileSuccess() throws Exception {

        Long projectId = 1L;

        FileEntryResource annexFileEntryResource = FileEntryResourceBuilder.newFileEntryResource()
                .withName("annex-file.pdf")
                .build();
        byte[] content = "HelloWorld".getBytes();
        ByteArrayResource annexByteArrayResource = new ByteArrayResource(content);

        when(projectService.getAdditionalContractFile(projectId)).thenReturn(Optional.of(annexByteArrayResource));
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.of(annexFileEntryResource));

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/additional-contract"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals("HelloWorld", response.getContentAsString());
        assertEquals("inline; filename=\"annex-file.pdf\"", response.getHeader("Content-Disposition"));
        assertEquals(10, response.getContentLength());
    }

    @Test
    public void uploadAnnexFile() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();
        Optional<FileEntryResource> annexFileEntryResource = Optional.of(FileEntryResourceBuilder.newFileEntryResource().withName("1").withMediaType("application/pdf").withFilesizeBytes(20).build());

        // when the model is re-loaded after uploading
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(annexFileEntryResource);
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        when(projectService.sendGrantOfferLetter(projectId)).thenReturn(serviceSuccess());

        MockMultipartFile uploadedFile = new MockMultipartFile("annex", "annex.pdf", "application/pdf", "My content!".getBytes());
        FileEntryResource createdFileDetails = newFileEntryResource().withName("1").withMediaType("application/pdf").withFilesizeBytes(20).build();

        when(projectService.addAdditionalContractFile(123L, "application/pdf", 11, "annex.pdf", "My content!".getBytes())).
                thenReturn(serviceSuccess(createdFileDetails));

        MvcResult result = mockMvc.perform(
                fileUpload("/project/"+ projectId  + "/grant-offer-letter/upload-annex").
                        file(uploadedFile).param("uploadAnnexClicked", "")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/grant-offer-letter/send")).
                andReturn();

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(uploadedFile, form.getAnnex());
    }

    @Test
    public void uploadAnnexFileFails() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        // when the model is re-loaded after uploading
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        when(projectService.sendGrantOfferLetter(projectId)).thenReturn(serviceSuccess());

        MockMultipartFile uploadedFile = new MockMultipartFile("annex", "annex.pdf", "application/pdf", "My content!".getBytes());

        when(projectService.addAdditionalContractFile(123L, "application/pdf", 11, "annex.pdf", "My content!".getBytes())).
                thenReturn(ServiceResult.serviceFailure(new Error(FILES_UNABLE_TO_CREATE_FILE, INTERNAL_SERVER_ERROR)));

        MvcResult result = mockMvc.perform(
                fileUpload("/project/"+ projectId  + "/grant-offer-letter/upload-annex").
                        file(uploadedFile).param("uploadAnnexClicked", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(uploadedFile, form.getAnnex());
        assertEquals(Boolean.FALSE, ((ProjectGrantOfferLetterSendViewModel)result.getModelAndView().getModel().get("model")).getAdditionalContractFileContentAvailable());
    }

    @Test
    public void testApproveSignedGOLSuccess() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        FileEntryResource signedGolFileEntryResource = FileEntryResourceBuilder.newFileEntryResource()
                .withName("signed-gol-file.pdf")
                .build();

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.of(signedGolFileEntryResource));

        when(projectService.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED)).thenReturn(serviceSuccess());

        // re-load model after sending GOL
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.TRUE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.TRUE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.of(signedGolFileEntryResource));


        MvcResult result = mockMvc.perform(post("/project/" + projectId + "/grant-offer-letter/signed/" + ApprovalType.APPROVED)).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel golViewModel = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");

        assertTrue(golViewModel.isSentToProjectTeam());
        assertEquals(null, golViewModel.getGrantOfferLetterFile());
        assertEquals(null, golViewModel.getAdditionalContractFile());
        assertEquals(new FileDetailsViewModel(signedGolFileEntryResource), golViewModel.getSignedGrantOfferLetterFile());
        assertFalse(golViewModel.getAdditionalContractFileContentAvailable());
        assertFalse(golViewModel.getGrantOfferLetterFileContentAvailable());
        assertEquals(Boolean.TRUE, golViewModel.isSentToProjectTeam());
        assertEquals(Boolean.TRUE, golViewModel.getSignedGrantOfferLetterApproved());
        assertEquals(Boolean.TRUE, golViewModel.getSignedGrantOfferLetterFileAvailable());

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(form.getAnnex(), null);
    }

    @Test
    public void testApproveSignedGOLFailure() throws Exception {
        Long competitionId = 1L;
        Long projectId = 123L;
        Long applicationId = 789L;

        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationResource).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        when(projectService.approveOrRejectSignedGrantOfferLetter(projectId, ApprovalType.APPROVED)).thenReturn(serviceFailure(GENERAL_NOT_FOUND));

        // re-load model after sending GOL
        when(projectService.getById(projectId)).thenReturn(projectResource);

        when(applicationService.getById(applicationId)).thenReturn(newApplicationResource().withId(applicationId).withCompetition(competitionId).build());

        competitionSummaryResource = newCompetitionSummaryResource().withId(competitionId).withCompetitionStatus(CompetitionStatus.OPEN).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(competitionId)).thenReturn(competitionSummaryResource);

        when(projectService.getGeneratedGrantOfferFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getAdditionalContractFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.isGrantOfferLetterAlreadySent(projectId)).thenReturn(serviceSuccess(Boolean.TRUE));
        when(projectService.isSignedGrantOfferLetterApproved(projectId)).thenReturn(serviceSuccess(Boolean.FALSE));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());


        MvcResult result = mockMvc.perform(post("/project/" + projectId + "/grant-offer-letter/signed/" + ApprovalType.APPROVED)).
                andExpect(view().name("project/grant-offer-letter-send")).
                andReturn();

        ProjectGrantOfferLetterSendViewModel golViewModel = (ProjectGrantOfferLetterSendViewModel) result.getModelAndView().getModel().get("model");

        assertTrue(golViewModel.isSentToProjectTeam());
        assertEquals(null, golViewModel.getGrantOfferLetterFile());
        assertEquals(null, golViewModel.getAdditionalContractFile());
        assertEquals(null, golViewModel.getSignedGrantOfferLetterFile());
        assertFalse(golViewModel.getAdditionalContractFileContentAvailable());
        assertFalse(golViewModel.getGrantOfferLetterFileContentAvailable());
        assertEquals(Boolean.TRUE, golViewModel.isSentToProjectTeam());
        assertEquals(Boolean.FALSE, golViewModel.getSignedGrantOfferLetterApproved());
        assertEquals(Boolean.FALSE, golViewModel.getSignedGrantOfferLetterFileAvailable());

        ProjectGrantOfferLetterSendForm form = (ProjectGrantOfferLetterSendForm) result.getModelAndView().getModel().get("form");
        assertEquals(form.getAnnex(), null);
    }

    @Test
    public void testDownloadSignedGrantOfferLetterSuccess() throws Exception {

        Long projectId = 1L;

        FileEntryResource annexFileEntryResource = FileEntryResourceBuilder.newFileEntryResource()
                .withName("annex-file.pdf")
                .build();
        byte[] content = "HelloWorld".getBytes();
        ByteArrayResource annexByteArrayResource = new ByteArrayResource(content);

        when(projectService.getSignedGrantOfferLetterFile(projectId)).thenReturn(Optional.of(annexByteArrayResource));
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.of(annexFileEntryResource));

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/signed-grant-offer-letter"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        assertEquals("HelloWorld", response.getContentAsString());
        assertEquals("inline; filename=\"annex-file.pdf\"", response.getHeader("Content-Disposition"));
        assertEquals(10, response.getContentLength());
    }

    @Test
    public void testDownloadSignedGrantOfferLetterFileEntryNotPresent() throws Exception {

        Long projectId = 1L;

        when(projectService.getSignedGrantOfferLetterFile(projectId)).thenReturn(Optional.empty());
        when(projectService.getSignedGrantOfferLetterFileDetails(projectId)).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/project/" + projectId + "/grant-offer-letter/signed-grant-offer-letter"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        // Assert that there is no content
        assertEquals("", response.getContentAsString());
        assertEquals(null, response.getHeader("Content-Disposition"));
        assertEquals(0, response.getContentLength());
    }

    @Override
    protected ProjectGrantOfferLetterSendController supplyControllerUnderTest() {
        return new ProjectGrantOfferLetterSendController();
    }
}
