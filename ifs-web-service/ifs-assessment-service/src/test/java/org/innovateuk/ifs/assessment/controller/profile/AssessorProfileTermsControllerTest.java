package org.innovateuk.ifs.assessment.controller.profile;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.controller.profile.AssessorProfileTermsController.ContractAnnexParameter;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileTermsForm;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileTermsAnnexModelPopulator;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileTermsModelPopulator;
import org.innovateuk.ifs.assessment.viewmodel.profile.AssessorProfileTermsAnnexViewModel;
import org.innovateuk.ifs.assessment.viewmodel.profile.AssessorProfileTermsViewModel;
import org.innovateuk.ifs.user.resource.ContractResource;
import org.innovateuk.ifs.user.resource.ProfileContractResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.innovateuk.ifs.assessment.controller.profile.AssessorProfileTermsController.ContractAnnexParameter.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.ContractResourceBuilder.newContractResource;
import static org.innovateuk.ifs.user.builder.ProfileContractResourceBuilder.newProfileContractResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.CollectionFunctions.asListOfPairs;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AssessorProfileTermsControllerTest extends BaseControllerMockMVCTest<AssessorProfileTermsController> {

    @Spy
    @InjectMocks
    private AssessorProfileTermsModelPopulator assessorProfileTermsModelPopulator;

    @Spy
    @InjectMocks
    private AssessorProfileTermsAnnexModelPopulator assessorProfileTermsAnnexModelPopulator;

    @Override
    protected AssessorProfileTermsController supplyControllerUnderTest() {
        return new AssessorProfileTermsController();
    }

    @Test
    public void getTerms() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        LocalDateTime expectedContractSignedDate = LocalDateTime.now();
        String expectedText = "Contract text...";

        ProfileContractResource profileContract = newProfileContractResource()
                .withContractSignedDate(expectedContractSignedDate)
                .withCurrentAgreement(true)
                .withContract(newContractResource()
                        .withText(expectedText)
                        .build())
                .build();

        when(userService.getProfileContract(user.getId())).thenReturn(profileContract);

        AssessorProfileTermsViewModel expectedViewModel = new AssessorProfileTermsViewModel();
        expectedViewModel.setCurrentAgreement(true);
        expectedViewModel.setContractSignedDate(expectedContractSignedDate);
        expectedViewModel.setText(expectedText);

        AssessorProfileTermsForm expectedForm = new AssessorProfileTermsForm();
        expectedForm.setAgreesToTerms(TRUE);

        mockMvc.perform(get("/profile/terms"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(view().name("profile/terms"));

        verify(userService, only()).getProfileContract(user.getId());
    }

    @Test
    public void getAnnex() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String expectedAnnexA = "Annex A...";
        String expectedAnnexB = "Annex B...";
        String expectedAnnexC = "Annex C...";

        ContractResource contract = newContractResource()
                .withAnnexA(expectedAnnexA)
                .withAnnexB(expectedAnnexB)
                .withAnnexC(expectedAnnexC)
                .build();

        when(contractService.getCurrentContract()).thenReturn(contract);

        // Check that each of the possible params returns the correct annex text
        List<Pair<ContractAnnexParameter, String>> params = asListOfPairs(A, expectedAnnexA, B, expectedAnnexB, C, expectedAnnexC);
        params.stream().forEach(paramAndExpected -> {
            try {
                assertProfileTermsAnnexView(paramAndExpected.getLeft(), paramAndExpected.getRight());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        verify(contractService, times(params.size())).getCurrentContract();
    }

    @Test
    public void submitTerms() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        when(userService.updateProfileContract(user.getId())).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/terms")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("agreesToTerms", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));

        verify(userService, only()).updateProfileContract(user.getId());
    }

    @Test
    public void submitTerms_invalidForm() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        LocalDateTime expectedContractSignedDate = LocalDateTime.now();
        String expectedText = "Contract text...";

        ProfileContractResource profileContract = newProfileContractResource()
                .withContractSignedDate(expectedContractSignedDate)
                .withCurrentAgreement(true)
                .withContract(newContractResource()
                        .withText(expectedText)
                        .build())
                .build();

        when(userService.getProfileContract(user.getId())).thenReturn(profileContract);

        AssessorProfileTermsViewModel expectedViewModel = new AssessorProfileTermsViewModel();
        expectedViewModel.setCurrentAgreement(true);
        expectedViewModel.setContractSignedDate(expectedContractSignedDate);
        expectedViewModel.setText(expectedText);

        MvcResult result = mockMvc.perform(post("/profile/terms")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("agreesToTerms", "false"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeHasFieldErrors("form", "agreesToTerms"))
                .andExpect(view().name("profile/terms"))
                .andReturn();

        AssessorProfileTermsForm form = (AssessorProfileTermsForm) result.getModelAndView().getModel().get("form");

        assertEquals(FALSE, form.getAgreesToTerms());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("agreesToTerms"));
        assertEquals("Please agree to the terms and conditions.", bindingResult.getFieldError("agreesToTerms").getDefaultMessage());

        verify(userService, only()).getProfileContract(user.getId());
    }

    private void assertProfileTermsAnnexView(ContractAnnexParameter annexParameter, String expectedText) throws Exception {
        AssessorProfileTermsAnnexViewModel expectedViewModel = new AssessorProfileTermsAnnexViewModel(annexParameter, expectedText);
        mockMvc.perform(get("/profile/terms/annex/{annex}", annexParameter))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/annex"));
    }
}
