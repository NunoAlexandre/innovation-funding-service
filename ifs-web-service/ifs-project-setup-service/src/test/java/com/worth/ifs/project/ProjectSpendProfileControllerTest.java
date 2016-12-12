package com.worth.ifs.project;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.commons.rest.LocalDateResource;
import com.worth.ifs.project.builder.ProjectLeadStatusResourceBuilder;
import com.worth.ifs.project.constant.ProjectActivityStates;
import com.worth.ifs.project.form.SpendProfileForm;
import com.worth.ifs.project.resource.*;
import com.worth.ifs.project.util.SpendProfileTableCalculator;
import com.worth.ifs.project.validation.SpendProfileCostValidator;
import com.worth.ifs.project.viewmodel.ProjectSpendProfileProjectManagerViewModel;
import com.worth.ifs.project.viewmodel.ProjectSpendProfileViewModel;
import com.worth.ifs.project.viewmodel.SpendProfileSummaryModel;
import com.worth.ifs.project.viewmodel.SpendProfileSummaryYearModel;
import com.worth.ifs.user.builder.OrganisationResourceBuilder;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.RoleResource;
import com.worth.ifs.user.resource.UserRoleType;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static com.worth.ifs.commons.error.CommonFailureKeys.GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION;
import static com.worth.ifs.commons.error.CommonFailureKeys.SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE;
import static com.worth.ifs.commons.error.CommonFailureKeys.SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static com.worth.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static com.worth.ifs.project.builder.SpendProfileResourceBuilder.newSpendProfileResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static com.worth.ifs.user.resource.UserRoleType.PARTNER;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.MapFunctions.asMap;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectSpendProfileControllerTest extends BaseControllerMockMVCTest<ProjectSpendProfileController> {
    @Mock
    public SpendProfileCostValidator spendProfileCostValidator;
    @Spy
    public SpendProfileTableCalculator spendProfileTableCalculator;

    @Override
    protected ProjectSpendProfileController supplyControllerUnderTest() {
        return new ProjectSpendProfileController();
    }

    @Test
    public void viewSpendProfileWhenProjectDetailsNotInDB() throws Exception {

        Long organisationId = 1L;

        ProjectResource projectResource = newProjectResource().build();

        when(projectService.getById(projectResource.getId())).
                thenThrow(new ObjectNotFoundException("Project not found", null));

        mockMvc.perform(get("/project/{projectId}/partner-organisation/{organisationId}/spend-profile", projectResource.getId(), organisationId))
                .andExpect(status().isNotFound())
                .andExpect(model().attributeDoesNotExist("model"));

        verify(projectFinanceService, never()).getSpendProfileTable(projectResource.getId(), organisationId);
    }

    @Test
    public void viewSpendProfileWhenSpendProfileDetailsNotInDB() throws Exception {

        Long organisationId = 1L;

        ProjectResource projectResource = newProjectResource().build();

        when(projectService.getById(projectResource.getId())).
                thenReturn(projectResource);

        when(projectFinanceService.getSpendProfileTable(projectResource.getId(), organisationId)).
                thenThrow(new ObjectNotFoundException("SpendProfile not found", null));


        mockMvc.perform(get("/project/{projectId}/partner-organisation/{organisationId}/spend-profile", projectResource.getId(), organisationId))
                .andExpect(status().isNotFound())
                .andExpect(model().attributeDoesNotExist("model"));
    }

    @Test
    public void viewSpendProfileSuccessfulViewModelPopulation() throws Exception {

        Long organisationId = 1L;
        Long projectId = 1L;

        ProjectResource projectResource = newProjectResource()
                .withName("projectName1")
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .withDuration(3L)
                .withId(projectId)
                .build();

        SpendProfileTableResource expectedTable = buildSpendProfileTableResource(projectResource);
        ProjectTeamStatusResource teamStatus = buildProjectTeamStatusResource();

        PartnerOrganisationResource partnerOrganisationResource = new PartnerOrganisationResource();
        partnerOrganisationResource.setOrganisation(organisationId);
        partnerOrganisationResource.setLeadOrganisation(false);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectResource.getId())).thenReturn(serviceSuccess(Collections.singletonList(partnerOrganisationResource)));


        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);

        when(projectFinanceService.getSpendProfileTable(projectResource.getId(), organisationId)).thenReturn(expectedTable);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectResource.getId())).thenReturn(serviceSuccess(Collections.emptyList()));
        when(projectService.getProjectTeamStatus(projectResource.getId(), Optional.empty())).thenReturn(teamStatus);


        ProjectSpendProfileViewModel expectedViewModel = buildExpectedProjectSpendProfileViewModel(organisationId, projectResource, expectedTable);

        mockMvc.perform(get("/project/{projectId}/partner-organisation/{organisationId}/spend-profile", projectResource.getId(), organisationId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("project/spend-profile"));

        verify(partnerOrganisationServiceMock).getPartnerOrganisations(eq(projectResource.getId()));

    }

    @Test
    public void testProjectManagerViewSpendProfile() throws Exception {
        long organisationId = 1L;

        ProjectResource projectResource = newProjectResource().withId(123L).withApplication(456L).build();
        List<ProjectUserResource> projectUserResources = newProjectUserResource()
                .withUser(1L)
                .withRoleName(UserRoleType.PROJECT_MANAGER)
                .withOrganisation(organisationId)
                .build(1);
        List<OrganisationResource> partnerOrganisations = newOrganisationResource()
                .withId(1L)
                .withName("abc")
                .build(1);
        SpendProfileResource spendProfileResource = newSpendProfileResource().build();
        List<RoleResource> roleResources = newRoleResource().withType(UserRoleType.PROJECT_MANAGER).build(1);
        ProjectTeamStatusResource teamStatus = buildProjectTeamStatusResource();

        loggedInUser.setRoles(roleResources);
        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);
        when(projectService.getProjectUsersForProject(projectResource.getId())).thenReturn(projectUserResources);
        when(projectService.getPartnerOrganisationsForProject(projectResource.getId())).thenReturn(partnerOrganisations);
        when(projectService.getProjectTeamStatus(projectResource.getId(), Optional.empty())).thenReturn(teamStatus);


        when(projectFinanceService.getSpendProfile(projectResource.getId(), organisationId)).thenReturn(Optional.of(spendProfileResource));

        ProjectSpendProfileProjectManagerViewModel expectedViewModel = buildExpectedProjectSpendProfileProjectManagerViewModel(projectResource, partnerOrganisations);

        mockMvc.perform(get("/project/{id}/partner-organisation/{organisationId}/spend-profile", projectResource.getId(), organisationId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/spend-profile-review"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andReturn();
    }

    @Test
    public void saveSpendProfileWhenErrorWhilstSaving() throws Exception {

        Long projectId = 1L;
        Long organisationId = 1L;

        SpendProfileTableResource table = new SpendProfileTableResource();

        when(projectFinanceService.getSpendProfileTable(projectId, organisationId)).thenReturn(table);

        List<Error> incorrectCosts = new ArrayList<>();
        incorrectCosts.add(new Error(SPEND_PROFILE_CONTAINS_FRACTIONS_IN_COST_FOR_SPECIFIED_CATEGORY_AND_MONTH, asList("Labour", 1), HttpStatus.BAD_REQUEST));

        when(projectFinanceService.saveSpendProfile(projectId, organisationId, table)).thenReturn(serviceFailure(incorrectCosts));

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/edit", projectId, organisationId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("table.markedAsComplete", "true")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile/edit"));
    }

    @Test
    public void saveSpendProfileSuccess() throws Exception {

        Long projectId = 1L;
        Long organisationId = 1L;

        SpendProfileTableResource table = new SpendProfileTableResource();

        when(projectFinanceService.getSpendProfileTable(projectId, organisationId)).thenReturn(table);

        when(projectFinanceService.saveSpendProfile(projectId, organisationId, table)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/edit", projectId, organisationId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("table.markedAsComplete", "true")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile"));
    }

    @Test
    public void markAsCompleteSpendProfileWhenSpendHigherThanEligible() throws Exception {

        Long organisationId = 1L;
        Long projectId = 1L;

        ProjectResource projectResource = newProjectResource()
                .withName("projectName1")
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .withDuration(3L)
                .withId(projectId)
                .build();

        SpendProfileTableResource table = buildSpendProfileTableResource(projectResource);
        ProjectTeamStatusResource teamStatus = buildProjectTeamStatusResource();

        PartnerOrganisationResource partnerOrganisationResource = new PartnerOrganisationResource();
        partnerOrganisationResource.setOrganisation(organisationId);
        partnerOrganisationResource.setLeadOrganisation(false);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectId)).thenReturn(serviceSuccess(Collections.singletonList(partnerOrganisationResource)));

        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);

        when(projectFinanceService.getSpendProfileTable(projectResource.getId(), organisationId)).thenReturn(table);

        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectResource.getId())).thenReturn(serviceSuccess(Collections.emptyList()));
        when(projectService.getProjectTeamStatus(projectResource.getId(), Optional.empty())).thenReturn(teamStatus);

        when(projectFinanceService.markSpendProfileComplete(projectResource.getId(), organisationId)).thenReturn(serviceFailure(SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE));

        ProjectSpendProfileViewModel expectedViewModel = buildExpectedProjectSpendProfileViewModel(organisationId, projectResource, table);

        expectedViewModel.setObjectErrors(Collections.singletonList(new ObjectError(SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE.getErrorKey(), "Cannot mark as complete, because totals more than eligible")));

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/complete", projectResource.getId(), organisationId)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("project/spend-profile"));

        verify(partnerOrganisationServiceMock).getPartnerOrganisations(eq(projectResource.getId()));
    }

    @Test
    public void markAsCompleteSpendProfileSuccess() throws Exception {

        Long projectId = 1L;
        Long organisationId = 1L;

        when(projectFinanceService.markSpendProfileComplete(projectId, organisationId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/complete", projectId, organisationId)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile"));
    }

    @Test
    public void markAsIncompleteSpendProfileSuccess() throws Exception {

        Long projectId = 1L;
        Long organisationId = 1L;

        when(projectFinanceService.markSpendProfileIncomplete(projectId, organisationId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/incomplete", projectId, organisationId)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile"));
    }

    @Test
    public void markAsIncompleteSpendProfileFailure() throws Exception {

        Long projectId = 1L;
        Long organisationId = 1L;

        when(projectFinanceService.markSpendProfileIncomplete(projectId, organisationId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/incomplete", projectId, organisationId)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile"));
    }

    @Test
    public void editSpendProfileSuccess() throws Exception {

        Long organisationId = 1L;
        Long projectId = 1L;

        ProjectResource projectResource = newProjectResource()
                .withName("projectName1")
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .withDuration(3L)
                .withId(projectId)
                .build();

        SpendProfileTableResource table = buildSpendProfileTableResource(projectResource);
        ProjectTeamStatusResource teamStatus = buildProjectTeamStatusResource();

        PartnerOrganisationResource partnerOrganisationResource = new PartnerOrganisationResource();
        partnerOrganisationResource.setOrganisation(organisationId);
        partnerOrganisationResource.setLeadOrganisation(false);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectId)).thenReturn(serviceSuccess(Collections.singletonList(partnerOrganisationResource)));

        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);

        when(projectFinanceService.getSpendProfileTable(projectResource.getId(), organisationId)).thenReturn(table);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectResource.getId())).thenReturn(serviceSuccess(Collections.emptyList()));
        when(projectService.getProjectTeamStatus(projectResource.getId(), Optional.empty())).thenReturn(teamStatus);

        ProjectSpendProfileViewModel expectedViewModel = buildExpectedProjectSpendProfileViewModel(organisationId, projectResource, table);

        SpendProfileForm expectedForm = new SpendProfileForm();
        expectedForm.setTable(table);

        mockMvc.perform(get("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/edit", projectResource.getId(), organisationId)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(view().name("project/spend-profile"));

        verify(partnerOrganisationServiceMock).getPartnerOrganisations(eq(projectResource.getId()));
    }

    private ProjectTeamStatusResource buildProjectTeamStatusResource() {

        List<ProjectPartnerStatusResource> partnerStatuses = newProjectPartnerStatusResource().build(2);
        ProjectPartnerStatusResource leadProjectPartnerStatusResource = ProjectLeadStatusResourceBuilder.newProjectLeadStatusResource()
                .withSpendProfileStatus(ProjectActivityStates.ACTION_REQUIRED)
                .build();
        partnerStatuses.add(leadProjectPartnerStatusResource);

        return newProjectTeamStatusResource().withPartnerStatuses(partnerStatuses).build();
    }

    @Test
    public void partnerCannotEditAfterSubmission() throws Exception {
        Long organisationId = 1L;
        Long projectId = 1L;

        ProjectResource projectResource = newProjectResource()
                .withName("projectName1")
                .withTargetStartDate(LocalDate.of(2018, 3, 1))
                .withDuration(3L)
                .withId(projectId)
                .build();

        OrganisationResource organisationResource = newOrganisationResource().build();
        ProjectTeamStatusResource teamStatus = buildProjectTeamStatusResource();

        SpendProfileTableResource table = buildSpendProfileTableResource(projectResource);
        table.setMarkedAsComplete(true);

        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);

        when(projectFinanceService.getSpendProfileTable(projectResource.getId(), organisationId)).thenReturn(table);
        when(organisationService.getOrganisationById(organisationId)).thenReturn(organisationResource);

        PartnerOrganisationResource partnerOrganisationResource = new PartnerOrganisationResource();
        partnerOrganisationResource.setOrganisation(organisationId);
        partnerOrganisationResource.setLeadOrganisation(false);
        when(partnerOrganisationServiceMock.getPartnerOrganisations(projectId)).thenReturn(serviceSuccess(Collections.singletonList(partnerOrganisationResource)));

        when(projectService.getProjectTeamStatus(projectResource.getId(), Optional.empty())).thenReturn(teamStatus);
        when(projectFinanceService.markSpendProfileIncomplete(projectId, organisationId)).thenReturn(serviceFailure(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION));

        SpendProfileForm expectedForm = new SpendProfileForm();
        expectedForm.setTable(table);

        mockMvc.perform(get("/project/{projectId}/partner-organisation/{organisationId}/spend-profile/edit", projectResource.getId(), organisationId)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(view().name("project/spend-profile"));

        verify(partnerOrganisationServiceMock).getPartnerOrganisations(eq(projectResource.getId()));

    }

    private SpendProfileTableResource buildSpendProfileTableResource(ProjectResource projectResource) {

        SpendProfileTableResource expectedTable = new SpendProfileTableResource();

        expectedTable.setMarkedAsComplete(false);

        expectedTable.setMonths(asList(
                new LocalDateResource(2018, 3, 1),
                new LocalDateResource(2018, 4, 1),
                new LocalDateResource(2018, 5, 1)
        ));

        expectedTable.setEligibleCostPerCategoryMap(asMap(
                1L, new BigDecimal("100"),
                2L, new BigDecimal("150"),
                3L, new BigDecimal("55")));

        expectedTable.setMonthlyCostsPerCategoryMap(asMap(
                1L, asList(new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("40")),
                2L, asList(new BigDecimal("70"), new BigDecimal("50"), new BigDecimal("60")),
                3L, asList(new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("0"))));


        List<LocalDate> months = IntStream.range(0, projectResource.getDurationInMonths().intValue()).mapToObj(projectResource.getTargetStartDate()::plusMonths).collect(toList());
        List<LocalDateResource> monthResources = simpleMap(months, LocalDateResource::new);

        expectedTable.setMonths(monthResources);

        return expectedTable;
    }

    private ProjectSpendProfileProjectManagerViewModel buildExpectedProjectSpendProfileProjectManagerViewModel(ProjectResource projectResource, List<OrganisationResource> partnerOrganisations) {

        Map<String, Boolean> partnersSpendProfileProgress = new HashMap<>();
        partnersSpendProfileProgress.put("abc", false);

        Map<String, Boolean> editablePartners = new HashMap<>();
        editablePartners.put("abc", false);

        return new ProjectSpendProfileProjectManagerViewModel(projectResource.getId(),
                projectResource.getApplication(), projectResource.getName(),
                partnersSpendProfileProgress,
                partnerOrganisations,
                projectResource.getSpendProfileSubmittedDate() != null,
                editablePartners,
                false);
    }

    private ProjectSpendProfileViewModel buildExpectedProjectSpendProfileViewModel(Long organisationId, ProjectResource projectResource, SpendProfileTableResource expectedTable) {

        OrganisationResource organisationResource = OrganisationResourceBuilder.newOrganisationResource()
                .withId(organisationId)
                .withName("Org1")
                .build();

        List<ProjectUserResource> projectUsers = newProjectUserResource()
                .withUser(1L)
                .withOrganisation(1L)
                .withRoleName(PARTNER)
                .build(1);

        when(organisationService.getOrganisationById(organisationId)).thenReturn(organisationResource);
        when(projectService.getProjectUsersForProject(projectResource.getId())).thenReturn(projectUsers);

        List<SpendProfileSummaryYearModel> years = createSpendProfileSummaryYears();

        SpendProfileSummaryModel summary = new SpendProfileSummaryModel(years);

        // Build the expectedCategoryToActualTotal map based on the input
        Map<Long, BigDecimal> expectedCategoryToActualTotal = new LinkedHashMap<>();
        expectedCategoryToActualTotal.put(1L, new BigDecimal("100"));
        expectedCategoryToActualTotal.put(2L, new BigDecimal("180"));
        expectedCategoryToActualTotal.put(3L, new BigDecimal("55"));

        // Expected total for each month based on the input
        List<BigDecimal> expectedTotalForEachMonth = asList(new BigDecimal("150"), new BigDecimal("85"), new BigDecimal("100"));

        // Assert that the total of totals is correct for Actual Costs and Eligible Costs based on the input
        BigDecimal expectedTotalOfAllActualTotals = new BigDecimal("335");
        BigDecimal expectedTotalOfAllEligibleTotals = new BigDecimal("305");

        // Assert that the view model is populated with the correct values
        return new ProjectSpendProfileViewModel(projectResource, organisationResource, expectedTable,
                summary, false, expectedCategoryToActualTotal, expectedTotalForEachMonth,
                expectedTotalOfAllActualTotals, expectedTotalOfAllEligibleTotals, false, null, null ,false, true, false, false, false);
    }

    private List<SpendProfileSummaryYearModel> createSpendProfileSummaryYears() {
        return asList(new SpendProfileSummaryYearModel(2017, "150"), new SpendProfileSummaryYearModel(2018, "185"));
    }
}
