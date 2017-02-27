package org.innovateuk.ifs.application;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.form.ApplicantInviteForm;
import org.innovateuk.ifs.application.form.ApplicationTeamAddOrganisationForm;
import org.innovateuk.ifs.application.populator.ApplicationTeamAddOrganisationModelPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.viewmodel.ApplicationTeamAddOrganisationViewModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationTeamAddOrganisationControllerTest extends BaseControllerMockMVCTest<ApplicationTeamAddOrganisationController> {

    @Spy
    @InjectMocks
    private ApplicationTeamAddOrganisationModelPopulator applicationTeamAddOrganisationModelPopulator;

    @Override
    protected ApplicationTeamAddOrganisationController supplyControllerUnderTest() {
        return new ApplicationTeamAddOrganisationController();
    }

    @Test
    public void getAddOrganisation() throws Exception {
        ApplicationResource applicationResource = setupApplicationResource();

        ApplicationTeamAddOrganisationForm expectedForm = new ApplicationTeamAddOrganisationForm();

        ApplicationTeamAddOrganisationViewModel expectedViewModel = new ApplicationTeamAddOrganisationViewModel(
                applicationResource.getId(),
                "Application name"
        );

        mockMvc.perform(get("/application/{applicationId}/team/addOrganisation", applicationResource.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("application-team/add-organisation"))
                .andReturn();

        verify(applicationService, only()).getById(applicationResource.getId());
        verifyZeroInteractions(inviteRestService);
    }

    @Test
    public void submitAddOrganisation() throws Exception {

    }

    @Test
    public void addApplicant() throws Exception {
        ApplicationResource applicationResource = setupApplicationResource();

        ApplicationTeamAddOrganisationViewModel expectedViewModel = new ApplicationTeamAddOrganisationViewModel(
                applicationResource.getId(),
                applicationResource.getName()
        );

        MvcResult result = mockMvc.perform(post("/application/{applicationId}/team/addOrganisation", applicationResource.getId())
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("addApplicant", "")
                .param("organisationName", "Ludlow")
                .param("applicants[0].name", "Jessica Doe")
                .param("applicants[0].email", "jessica.doe@ludlow.co.uk"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("application-team/add-organisation"))
                .andReturn();

        ApplicationTeamAddOrganisationForm form = (ApplicationTeamAddOrganisationForm) result.getModelAndView().getModel().get("form");

        assertEquals("Ludlow", form.getOrganisationName());
        assertEquals("The applicant rows should contain the existing applicant as well as a blank one", asList(new ApplicantInviteForm("Jessica Doe", "jessica.doe@ludlow.co.uk"), new ApplicantInviteForm()), form.getApplicants());

        verify(applicationService, only()).getById(applicationResource.getId());
        verifyZeroInteractions(inviteRestService);
    }

    @Test
    public void removeApplicant() throws Exception {
        ApplicationResource applicationResource = setupApplicationResource();

        ApplicationTeamAddOrganisationViewModel expectedViewModel = new ApplicationTeamAddOrganisationViewModel(
                applicationResource.getId(),
                applicationResource.getName()
        );

        MvcResult result = mockMvc.perform(post("/application/{applicationId}/team/addOrganisation", applicationResource.getId())
                .contentType(APPLICATION_FORM_URLENCODED)
                // Remove the row at index 0
                .param("removeApplicant", "0")
                .param("organisationName", "Ludlow")
                .param("applicants[0].name", "Jessica Doe")
                .param("applicants[0].email", "jessica.doe@ludlow.co.uk"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("application-team/add-organisation"))
                .andReturn();

        ApplicationTeamAddOrganisationForm form = (ApplicationTeamAddOrganisationForm) result.getModelAndView().getModel().get("form");

        assertEquals("Ludlow", form.getOrganisationName());
        assertTrue("The list of applicants should be empty", form.getApplicants().isEmpty());

        verify(applicationService, only()).getById(applicationResource.getId());
        verifyZeroInteractions(inviteRestService);
    }

    private ApplicationResource setupApplicationResource() {
        ApplicationResource applicationResource = newApplicationResource()
                .withName("Application name")
                .build();

        when(applicationService.getById(applicationResource.getId())).thenReturn(applicationResource);
        return applicationResource;
    }
}