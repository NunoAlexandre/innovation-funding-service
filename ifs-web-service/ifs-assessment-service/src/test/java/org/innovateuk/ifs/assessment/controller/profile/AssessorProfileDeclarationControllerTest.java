package org.innovateuk.ifs.assessment.controller.profile;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileAppointmentForm;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileDeclarationForm;
import org.innovateuk.ifs.assessment.form.profile.AssessorProfileFamilyAffiliationForm;
import org.innovateuk.ifs.assessment.form.profile.populator.AssessorProfileDeclarationFormPopulator;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileDeclarationModelPopulator;
import org.innovateuk.ifs.assessment.model.profile.AssessorProfileEditDeclarationModelPopulator;
import org.innovateuk.ifs.assessment.viewmodel.profile.AssessorProfileDeclarationViewModel;
import org.innovateuk.ifs.assessment.viewmodel.profile.AssessorProfileEditDeclarationViewModel;
import org.innovateuk.ifs.user.resource.AffiliationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.AffiliationResourceBuilder.newAffiliationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.AffiliationType.*;
import static org.innovateuk.ifs.util.CollectionFunctions.combineLists;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessorProfileDeclarationControllerTest extends BaseControllerMockMVCTest<AssessorProfileDeclarationController> {
    @Spy
    @InjectMocks
    private AssessorProfileEditDeclarationModelPopulator assessorProfileEditDeclarationModelPopulator;

    @Spy
    @InjectMocks
    private AssessorProfileDeclarationModelPopulator assessorProfileDeclarationModelPopulator;

    @Spy
    @InjectMocks
    private AssessorProfileDeclarationFormPopulator assessorProfileDeclarationFormPopulator;

    @Override
    @Before
    public void setUp() {
        super.setUp();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ReflectionTestUtils.setField(controller, "validator", validator);
    }

    @Override
    protected AssessorProfileDeclarationController supplyControllerUnderTest() {
        return new AssessorProfileDeclarationController();
    }

    @Test
    public void getDeclaration() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String expectedPrincipalEmployer = "Big Name Corporation";
        String expectedRole = "Financial Accountant";
        String expectedProfessionalAffiliations = "Professional affiliations...";
        String expectedFinancialInterests = "Other financial interests...";
        String expectedFamilyFinancialInterests = "Other family financial interests...";

        List<AffiliationResource> expectedAppointments = newAffiliationResource()
                .withAffiliationType(PERSONAL)
                .withOrganisation("Org 1", "Org 2")
                .withPosition("Pos 1", "Post 2")
                .withExists(TRUE)
                .build(2);
        List<AffiliationResource> expectedFamilyAffiliations = newAffiliationResource()
                .withAffiliationType(FAMILY)
                .withRelation("Relation 1", "Relation 2")
                .withOrganisation("Org 1", "Org 2")
                .withExists(TRUE)
                .build(2);
        AffiliationResource principalEmployer = newAffiliationResource()
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(expectedPrincipalEmployer)
                .withPosition(expectedRole)
                .build();
        AffiliationResource professionalAffiliations = newAffiliationResource()
                .withAffiliationType(PROFESSIONAL)
                .withExists(TRUE)
                .withDescription(expectedProfessionalAffiliations)
                .build();
        AffiliationResource financialInterests = newAffiliationResource()
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(TRUE)
                .withDescription(expectedFinancialInterests)
                .build();
        AffiliationResource familyFinancialInterests = newAffiliationResource()
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(TRUE)
                .withDescription(expectedFamilyFinancialInterests)
                .build();

        AssessorProfileDeclarationViewModel expectedViewModel = new AssessorProfileDeclarationViewModel(
                expectedPrincipalEmployer,
                expectedRole,
                expectedProfessionalAffiliations,
                expectedAppointments,
                expectedFinancialInterests,
                expectedFamilyAffiliations,
                expectedFamilyFinancialInterests
        );

        when(userService.getUserAffiliations(user.getId())).thenReturn(combineLists(
                combineLists(
                        expectedAppointments,
                        expectedFamilyAffiliations
                ),
                principalEmployer,
                professionalAffiliations,
                financialInterests,
                familyFinancialInterests
                )
        );

        mockMvc.perform(get("/profile/declaration"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/declaration-of-interest"));

        verify(userService).getUserAffiliations(user.getId());
    }

    @Test
    public void getEditDeclaration() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String expectedPrincipalEmployer = "Big Name Corporation";
        String expectedRole = "Financial Accountant";
        String expectedProfessionalAffiliations = "Professional affiliations...";

        List<AssessorProfileAppointmentForm> expectedAppointments = asList(
                new AssessorProfileAppointmentForm("Org 1", "Pos 1"),
                new AssessorProfileAppointmentForm("Org 2", "Pos 2")
        );

        String expectedFinancialInterests = "Other financial interests...";

        List<AssessorProfileFamilyAffiliationForm> expectedFamilyAffiliations = asList(
                new AssessorProfileFamilyAffiliationForm("Relation 1", "Org 1", "Pos 1"),
                new AssessorProfileFamilyAffiliationForm("Relation 2", "Org 2", "Pos 2")
        );

        String expectedFamilyFinancialInterests = "Other family financial interests...";

        AffiliationResource principalEmployer = newAffiliationResource()
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(expectedPrincipalEmployer)
                .withPosition(expectedRole)
                .build();

        AffiliationResource professionalAffiliations = newAffiliationResource()
                .withAffiliationType(PROFESSIONAL)
                .withExists(TRUE)
                .withDescription(expectedProfessionalAffiliations)
                .build();

        List<AffiliationResource> appointments = newAffiliationResource()
                .withAffiliationType(PERSONAL)
                .withExists(TRUE)
                .withOrganisation("Org 1", "Org 2")
                .withPosition("Pos 1", "Pos 2")
                .build(2);

        AffiliationResource financialInterests = newAffiliationResource()
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(TRUE)
                .withDescription(expectedFinancialInterests)
                .build();

        List<AffiliationResource> familyAffiliations = newAffiliationResource()
                .withAffiliationType(FAMILY)
                .withExists(TRUE)
                .withRelation("Relation 1", "Relation 2")
                .withOrganisation("Org 1", "Org 2")
                .withPosition("Pos 1", "Pos 2")
                .build(2);

        AffiliationResource familyFinancialInterests = newAffiliationResource()
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(TRUE)
                .withDescription(expectedFamilyFinancialInterests)
                .build();

        when(userService.getUserAffiliations(user.getId())).thenReturn(combineLists(
                combineLists(
                        appointments,
                        familyAffiliations
                ),
                principalEmployer,
                professionalAffiliations,
                financialInterests,
                familyFinancialInterests
                )
        );

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);
        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();
        expectedForm.setPrincipalEmployer(expectedPrincipalEmployer);
        expectedForm.setRole(expectedRole);
        expectedForm.setProfessionalAffiliations(expectedProfessionalAffiliations);
        expectedForm.setHasAppointments(TRUE);
        expectedForm.setAppointments(expectedAppointments);
        expectedForm.setHasFinancialInterests(TRUE);
        expectedForm.setFinancialInterests(expectedFinancialInterests);
        expectedForm.setHasFamilyAffiliations(TRUE);
        expectedForm.setFamilyAffiliations(expectedFamilyAffiliations);
        expectedForm.setHasFamilyFinancialInterests(TRUE);
        expectedForm.setFamilyFinancialInterests(expectedFamilyFinancialInterests);

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(view().name("profile/declaration-of-interest-edit"));

        verify(userService).getUserAffiliations(user.getId());
    }


    @Test
    public void getEditDeclaration_noAffiliations() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        // The form should have no fields populated
        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm));
    }

    @Test
    public void getEditDeclaration_noAppointments() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        List<AffiliationResource> appointments = newAffiliationResource()
                .withAffiliationType(PERSONAL)
                .withExists(FALSE)
                .build(1);

        when(userService.getUserAffiliations(user.getId())).thenReturn(appointments);

        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();
        expectedForm.setHasAppointments(FALSE);

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm));
    }

    @Test
    public void getEditDeclaration_noFinancialInterests() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        List<AffiliationResource> financialInterests = newAffiliationResource()
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(FALSE)
                .build(1);

        when(userService.getUserAffiliations(user.getId())).thenReturn(financialInterests);

        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();
        expectedForm.setHasFinancialInterests(FALSE);

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm));
    }

    @Test
    public void getEditDeclaration_noFamilyInterests() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        List<AffiliationResource> appointments = newAffiliationResource()
                .withAffiliationType(FAMILY)
                .withExists(FALSE)
                .build(1);

        when(userService.getUserAffiliations(user.getId())).thenReturn(appointments);

        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();
        expectedForm.setHasFamilyAffiliations(FALSE);

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm));
    }

    @Test
    public void getEditDeclaration_noFamilyFinancialInterests() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        List<AffiliationResource> familyFinancialInterests = newAffiliationResource()
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(FALSE)
                .build(1);

        when(userService.getUserAffiliations(user.getId())).thenReturn(familyFinancialInterests);

        AssessorProfileDeclarationForm expectedForm = new AssessorProfileDeclarationForm();
        expectedForm.setHasFamilyFinancialInterests(FALSE);

        mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm));
    }

    @Test
    public void getEditDeclaration_beforeFinancialYearEndInCurrentYear() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        // Set the clock such that the financial year end has yet to be reached this year
        setClockToStartOfDay(getFinancialYearEndDate(year).minusDays(1));
        // Expect the declaration date to be the financial year end of the current year
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        MvcResult result = mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andReturn();

        AssessorProfileEditDeclarationViewModel viewModel = (AssessorProfileEditDeclarationViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(expectedDeclarationDate, viewModel.getDeclarationDate());
    }

    @Test
    public void getEditDeclaration_onFinancialYearEndInCurrentYear() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        // Set the clock such that the financial year end has been reached this year
        setClockToStartOfDay(getFinancialYearEndDate(year));
        // Expect the declaration date to be the financial year end of the next year
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year).plusYears(1);

        MvcResult result = mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andReturn();

        AssessorProfileEditDeclarationViewModel viewModel = (AssessorProfileEditDeclarationViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(expectedDeclarationDate, viewModel.getDeclarationDate());
    }

    @Test
    public void getEditDeclaration_afterFinancialYearEndInCurrentYear() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        // Set the clock such that the financial year end has been passed this year
        setClockToStartOfDay(getFinancialYearEndDate(year).plusDays(1));
        // Expect the declaration date to be the financial year end of the next year
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year).plusYears(1);

        MvcResult result = mockMvc.perform(get("/profile/declaration/edit"))
                .andExpect(status().isOk())
                .andReturn();

        AssessorProfileEditDeclarationViewModel viewModel = (AssessorProfileEditDeclarationViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(expectedDeclarationDate, viewModel.getDeclarationDate());
    }

    @Test
    public void submitDeclaration() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "true";
        List<String[]> appointments = asList(new String[]{"Org 1", "Pos 1"}, new String[]{"Org 2", "Pos 2"});
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "true";
        List<String[]> familyAffiliations = asList(new String[]{"Relation 1", "Org 1", "Pos 1"}, new String[]{"Relation 2", "Org 2", "Pos 2"});
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";
        String accurateAccount = "true";

        AffiliationResource expectedPrincipalEmployer = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(principalEmployer)
                .withPosition(role)
                .build();

        AffiliationResource expectedProfessionalAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PROFESSIONAL)
                .withExists(TRUE)
                .withDescription(professionalAffiliations)
                .build();

        List<AffiliationResource> expectedAppointments = appointments.stream().map(appointment -> newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL)
                .withExists(TRUE)
                .withOrganisation(appointment[0])
                .withPosition(appointment[1])
                .build()).collect(toList());

        AffiliationResource expectedFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(TRUE)
                .withDescription(financialInterests)
                .build();

        List<AffiliationResource> expectedFamilyAffiliations = familyAffiliations.stream().map(familyAffiliation -> newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY)
                .withExists(TRUE)
                .withRelation(familyAffiliation[0])
                .withOrganisation(familyAffiliation[1])
                .withPosition(familyAffiliation[2])
                .build()).collect(toList());

        AffiliationResource expectedFamilyFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(TRUE)
                .withDescription(familyFinancialInterests)
                .build();

        when(userService.updateUserAffiliations(user.getId(), combineLists(
                combineLists(expectedAppointments,
                        expectedFamilyAffiliations
                ),
                expectedPrincipalEmployer,
                expectedProfessionalAffiliations,
                expectedFinancialInterests,
                expectedFamilyFinancialInterests))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("appointments[0].organisation", appointments.get(0)[0])
                .param("appointments[0].position", appointments.get(0)[1])
                .param("appointments[1].organisation", appointments.get(1)[0])
                .param("appointments[1].position", appointments.get(1)[1])
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("familyAffiliations[0].relation", familyAffiliations.get(0)[0])
                .param("familyAffiliations[0].organisation", familyAffiliations.get(0)[1])
                .param("familyAffiliations[0].position", familyAffiliations.get(0)[2])
                .param("familyAffiliations[1].relation", familyAffiliations.get(1)[0])
                .param("familyAffiliations[1].organisation", familyAffiliations.get(1)[1])
                .param("familyAffiliations[1].position", familyAffiliations.get(1)[2])
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests)
                .param("accurateAccount", accurateAccount))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));
    }

    @Test
    public void submitDeclaration_withNoAffiliations() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String hasAppointments = "false";
        String hasFinancialInterests = "false";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "false";
        String accurateAccount = "true";

        AffiliationResource expectedPrincipalEmployer = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(principalEmployer)
                .withPosition(role)
                .build();

        AffiliationResource expectedProfessionalAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PROFESSIONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedAppointments = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(FALSE)
                .build();

        when(userService.updateUserAffiliations(user.getId(), combineLists(
                expectedAppointments,
                expectedFamilyAffiliations,
                expectedPrincipalEmployer,
                expectedProfessionalAffiliations,
                expectedFinancialInterests,
                expectedFamilyFinancialInterests))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", accurateAccount))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));
    }

    @Test
    public void submitDeclaration_withYesAnswerToAppointmentsButNoAppointments() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "true";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "appointments"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertTrue(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("appointments"));
        assertEquals("Please enter your appointments, directorships or consultancies.", bindingResult.getFieldError("appointments").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_withYesAnswerToAppointmentsButInvalidAppointments() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String hasAppointments = "true";
        String hasFinancialInterests = "false";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "false";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("hasAppointments", hasAppointments)
                .param("appointments[0].organisation", "Org 1")
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "appointments[0].position"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertNull(form.getProfessionalAffiliations());
        assertTrue(form.getHasAppointments());
        AssessorProfileAppointmentForm appointmentForm = new AssessorProfileAppointmentForm();
        appointmentForm.setOrganisation("Org 1");
        assertEquals(asList(appointmentForm), form.getAppointments());
        assertFalse(form.getHasFinancialInterests());
        assertNull(form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertFalse(form.getHasFamilyFinancialInterests());
        assertNull(form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("appointments[0].position"));
        assertEquals("Please enter a position.", bindingResult.getFieldError("appointments[0].position").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_withNoAnswerToAppointmentsButHasAppointments() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String hasAppointments = "false";
        String hasFinancialInterests = "false";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "false";

        AffiliationResource expectedPrincipalEmployer = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(principalEmployer)
                .withPosition(role)
                .build();

        AffiliationResource expectedProfessionalAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PROFESSIONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedAppointments = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(FALSE)
                .build();

        when(userService.updateUserAffiliations(user.getId(), combineLists(
                expectedAppointments,
                expectedFamilyAffiliations,
                expectedPrincipalEmployer,
                expectedProfessionalAffiliations,
                expectedFinancialInterests,
                expectedFamilyFinancialInterests))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("hasAppointments", hasAppointments)
               /*
                  Test the scenario where a user selects "Yes" to the radio button in the UI, partially enters appointment row(s), and then changes their decision to "No".
                  The row(s) are hidden by the UI rather than destroyed in case the user makes the change by accident.
                  The rows are still submitted but should not be persisted or cause any validation warning.
                */
                .param("appointments[0].organisation", "Org 1")
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));
    }

    @Test
    public void submitDeclaration_withYesAnswerToFinancialInterestsButNoFinancialInterests() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "false";
        String hasFinancialInterests = "true";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "financialInterests"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertNull(form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("financialInterests"));
        assertEquals("Please enter your financial interests.", bindingResult.getFieldError("financialInterests").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_withYesAnswerToFamilyAffiliationsButNoFamilyAffiliations() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "false";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "true";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "familyAffiliations"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertTrue(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("familyAffiliations"));
        assertEquals("Please enter the appointments, directorships or consultancies of your close family members.", bindingResult.getFieldError("familyAffiliations").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_withYesAnswerToFamilyAffiliationsButInvalidFamilyAffiliations() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String hasAppointments = "false";
        String hasFinancialInterests = "false";
        String hasFamilyAffiliations = "true";
        String hasFamilyFinancialInterests = "false";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("familyAffiliations[0].relation", "Relation")
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "familyAffiliations[0].organisation"))
                .andExpect(model().attributeHasFieldErrors("form", "familyAffiliations[0].position"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertNull(form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertFalse(form.getHasFinancialInterests());
        assertNull(form.getFinancialInterests());
        assertTrue(form.getHasFamilyAffiliations());
        AssessorProfileFamilyAffiliationForm familyAffiliationForm = new AssessorProfileFamilyAffiliationForm();
        familyAffiliationForm.setRelation("Relation");
        assertEquals(asList(familyAffiliationForm), form.getFamilyAffiliations());
        assertFalse(form.getHasFamilyFinancialInterests());
        assertNull(form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(2, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("familyAffiliations[0].organisation"));
        assertEquals("Please enter an organisation.", bindingResult.getFieldError("familyAffiliations[0].organisation").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("familyAffiliations[0].position"));
        assertEquals("Please enter a position.", bindingResult.getFieldError("familyAffiliations[0].position").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_withNoAnswerToFamilyAffiliationsButHasFamilyAffiliations() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String hasAppointments = "false";
        String hasFinancialInterests = "false";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "false";

        AffiliationResource expectedPrincipalEmployer = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(EMPLOYER)
                .withExists(TRUE)
                .withOrganisation(principalEmployer)
                .withPosition(role)
                .build();

        AffiliationResource expectedProfessionalAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PROFESSIONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedAppointments = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(PERSONAL_FINANCIAL)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyAffiliations = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY)
                .withExists(FALSE)
                .build();

        AffiliationResource expectedFamilyFinancialInterests = newAffiliationResource()
                .with(id(null))
                .withAffiliationType(FAMILY_FINANCIAL)
                .withExists(FALSE)
                .build();

        when(userService.updateUserAffiliations(user.getId(), combineLists(
                expectedAppointments,
                expectedFamilyAffiliations,
                expectedPrincipalEmployer,
                expectedProfessionalAffiliations,
                expectedFinancialInterests,
                expectedFamilyFinancialInterests))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
               /*
                  Test the scenario where a user selects "Yes" to the radio button in the UI, partially enters appointment row(s), and then changes their decision to "No".
                  The row(s) are hidden by the UI rather than destroyed in case the user makes the change by accident.
                  The rows are still submitted but should not be persisted or cause any validation warning.
                */
                .param("familyAffiliations[0].relation", "Relation")
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessor/dashboard"));
    }

    @Test
    public void submitDeclaration_withYesAnswerToFamilyFinancialInterestsButNoFamilyFinancialInterests() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "false";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "true";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("accurateAccount", "true"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeHasFieldErrors("form", "familyFinancialInterests"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertNull(form.getFamilyFinancialInterests());

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("familyFinancialInterests"));
        assertEquals("Please enter your family''s financial interests.", bindingResult.getFieldError("familyFinancialInterests").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void submitDeclaration_invalidForm() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("accurateAccount", "false"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "principalEmployer"))
                .andExpect(model().attributeHasFieldErrors("form", "role"))
                .andExpect(model().attributeHasFieldErrors("form", "hasAppointments"))
                .andExpect(model().attributeHasFieldErrors("form", "hasFinancialInterests"))
                .andExpect(model().attributeHasFieldErrors("form", "hasFamilyAffiliations"))
                .andExpect(model().attributeHasFieldErrors("form", "hasFamilyFinancialInterests"))
                .andExpect(model().attributeHasFieldErrors("form", "accurateAccount"))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();

        assertTrue(bindingResult.hasErrors());
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(7, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("principalEmployer"));
        assertEquals("Please enter a principal employer.", bindingResult.getFieldError("principalEmployer").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("role"));
        assertEquals("Please enter your role with your principal employer.", bindingResult.getFieldError("role").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("hasAppointments"));
        assertEquals("Please tell us if you have any appointments, directorships or consultancies.", bindingResult.getFieldError("hasAppointments").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("hasFinancialInterests"));
        assertEquals("Please tell us if you have any other financial interests.", bindingResult.getFieldError("hasFinancialInterests").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("hasFamilyAffiliations"));
        assertEquals("Please tell us if any of your close family members have any appointments, directorships or consultancies.", bindingResult.getFieldError("hasFamilyAffiliations").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("hasFamilyFinancialInterests"));
        assertEquals("Please tell us if any of your close family members have any other financial interests.", bindingResult.getFieldError("hasFamilyFinancialInterests").getDefaultMessage());
        assertTrue(bindingResult.hasFieldErrors("accurateAccount"));
        assertEquals("You must agree that your account is accurate.", bindingResult.getFieldError("accurateAccount").getDefaultMessage());

        verifyZeroInteractions(userService);
    }

    @Test
    public void addAppointment() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "true";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("addAppointment", "")
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("appointments[0].organisation", "Org")
                .param("appointments[0].position", "Pos")
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertTrue(form.getHasAppointments());
        assertEquals("The appointment rows should contain the existing appointment as well as a blank one", asList(new AssessorProfileAppointmentForm("Org", "Pos"), new AssessorProfileAppointmentForm()), form.getAppointments());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        verifyZeroInteractions(userService);
    }

    @Test
    public void removeAppointment() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "true";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "false";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                // Remove the row at index 0
                .param("removeAppointment", "0")
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("appointments[0].organisation", "Org")
                .param("appointments[0].position", "Pos")
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertTrue(form.getHasAppointments());
        assertTrue("The list of appointments should be empty", form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertFalse(form.getHasFamilyAffiliations());
        assertTrue(form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        verifyZeroInteractions(userService);
    }

    @Test
    public void addFamilyMemberAffiliation() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "false";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "true";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("addFamilyMemberAffiliation", "")
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("familyAffiliations[0].relation", "Relation")
                .param("familyAffiliations[0].organisation", "Org")
                .param("familyAffiliations[0].position", "Pos")
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertTrue(form.getHasFamilyAffiliations());
        assertEquals("The family affiliation rows should contain the existing appointment as well as a blank one", asList(new AssessorProfileFamilyAffiliationForm("Relation", "Org", "Pos"), new AssessorProfileFamilyAffiliationForm()), form.getFamilyAffiliations());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        verifyZeroInteractions(userService);
    }

    @Test
    public void removeFamilyMemberAffiliation() throws Exception {
        UserResource user = newUserResource().build();
        setLoggedInUser(user);

        int year = 2016;
        setClockToStartOfDay(LocalDate.of(year, JANUARY, 1));
        LocalDate expectedDeclarationDate = getFinancialYearEndDate(year);

        String principalEmployer = "Big Name Corporation";
        String role = "Financial Accountant";
        String professionalAffiliations = "Professional affiliations...";
        String hasAppointments = "false";
        String hasFinancialInterests = "true";
        String financialInterests = "Other financial interests...";
        String hasFamilyAffiliations = "true";
        String hasFamilyFinancialInterests = "true";
        String familyFinancialInterests = "Other family financial interests...";

        AssessorProfileEditDeclarationViewModel expectedViewModel = new AssessorProfileEditDeclarationViewModel(expectedDeclarationDate);

        MvcResult result = mockMvc.perform(post("/profile/declaration/edit")
                .contentType(APPLICATION_FORM_URLENCODED)
                .param("removeFamilyMemberAffiliation", "0")
                .param("principalEmployer", principalEmployer)
                .param("role", role)
                .param("professionalAffiliations", professionalAffiliations)
                .param("hasAppointments", hasAppointments)
                .param("hasFinancialInterests", hasFinancialInterests)
                .param("financialInterests", financialInterests)
                .param("hasFamilyAffiliations", hasFamilyAffiliations)
                .param("familyAffiliations[0].relation", "Relation")
                .param("familyAffiliations[0].organisation", "Org")
                .param("familyAffiliations[0].position", "Pos")
                .param("hasFamilyFinancialInterests", hasFamilyFinancialInterests)
                .param("familyFinancialInterests", familyFinancialInterests))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("profile/declaration-of-interest-edit"))
                .andReturn();

        AssessorProfileDeclarationForm form = (AssessorProfileDeclarationForm) result.getModelAndView().getModel().get("form");

        assertEquals(principalEmployer, form.getPrincipalEmployer());
        assertEquals(role, form.getRole());
        assertEquals(professionalAffiliations, form.getProfessionalAffiliations());
        assertFalse(form.getHasAppointments());
        assertTrue(form.getAppointments().isEmpty());
        assertTrue(form.getHasFinancialInterests());
        assertEquals(financialInterests, form.getFinancialInterests());
        assertTrue(form.getHasFamilyAffiliations());
        assertTrue("The list of family affiliation rows should be empty", form.getFamilyAffiliations().isEmpty());
        assertTrue(form.getHasFamilyFinancialInterests());
        assertEquals(familyFinancialInterests, form.getFamilyFinancialInterests());

        verifyZeroInteractions(userService);
    }

    private LocalDate getFinancialYearEndDate(int year) {
        return LocalDate.of(year, MARCH, 31);
    }

    private void setClockToStartOfDay(LocalDate date) {
        Clock clock = Clock.fixed(date.atStartOfDay(systemDefault()).toInstant(), systemDefault());
        AssessorProfileEditDeclarationModelPopulator assessorProfileEditDeclarationModelPopulator =
                (AssessorProfileEditDeclarationModelPopulator) ReflectionTestUtils.getField(
                        controller,
                        AssessorProfileDeclarationController.class,
                        "assessorProfileEditDeclarationModelPopulator"
                );

        ReflectionTestUtils.setField(assessorProfileEditDeclarationModelPopulator, "clock", clock, Clock.class);
    }
}
