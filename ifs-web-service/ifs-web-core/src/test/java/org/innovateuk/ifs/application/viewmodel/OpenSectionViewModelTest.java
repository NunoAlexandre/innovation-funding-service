package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OpenSectionViewModelTest {

    private OpenSectionViewModel viewModel;

    private Long sectionId = 23456L;
    private SectionResource currentSection;
    private List<String> pendingNames;
    private UserResource currentUser;
    private OrganisationResource organisation;
    private SectionAssignableViewModel sectionAssignableViewModel;
    private NavigationViewModel navigationViewModel;
    private SectionApplicationViewModel applicationViewModel;

    private SortedSet<OrganisationResource> academicOrganisations;
    private SortedSet<OrganisationResource> applicationOrganisations;


    @Before
    public void setup() {
        currentSection = newSectionResource().withId(sectionId).build();
        navigationViewModel = new NavigationViewModel();
        currentUser = newUserResource().build();
        pendingNames = new ArrayList<>();
        pendingNames.add("PendingNameOne");
        pendingNames.add("PendingNameTwo");
        organisation = newOrganisationResource().build();

        academicOrganisations = new TreeSet<>(Comparator.comparingLong(OrganisationResource::getId));
        academicOrganisations.add(newOrganisationResource().build());

        applicationOrganisations = new TreeSet<>(Comparator.comparingLong(OrganisationResource::getId));
        applicationOrganisations.add(newOrganisationResource().build());

        viewModel = new OpenSectionViewModel();

        viewModel.setTitle("Title");
        viewModel.setCurrentSection(currentSection);
        viewModel.setResponses(Collections.emptyMap());
        viewModel.setUserIsLeadApplicant(Boolean.TRUE);
        viewModel.setCompletedSections(asList(2L));
        viewModel.setSections(asMap(sectionId, currentSection));
        viewModel.setQuestionFormInputs(asMap());
        viewModel.setSubSections(asMap());
        viewModel.setSectionQuestions(asMap());
        viewModel.setSubSectionQuestionFormInputs(asMap());
        viewModel.setSectionAssignableViewModel(sectionAssignableViewModel);
        viewModel.setSectionApplicationViewModel(applicationViewModel);
        viewModel.setNavigationViewModel(navigationViewModel);
        viewModel.setCurrentSection(currentSection);
        viewModel.setHasFinanceSection(Boolean.FALSE);
        viewModel.setFinanceSectionId(sectionId);
        viewModel.setCurrentUser(currentUser);

        viewModel.setAcademicOrganisations(academicOrganisations);
        viewModel.setAllQuestionsCompleted(Boolean.FALSE);
        viewModel.setApplicationOrganisations(applicationOrganisations);
        viewModel.setCompletedQuestionsPercentage(BigDecimal.TEN);
        viewModel.setCompletedSectionsByOrganisation(asMap());
        viewModel.setEachCollaboratorFinanceSectionId(sectionId);
        viewModel.setLeadOrganisation(organisation);
        viewModel.setPendingOrganisationNames(pendingNames);
        viewModel.setSectionsMarkedAsComplete(new HashSet<>(asList(2L)));
    }

    @Test
    public void testNormalGetters() {
        assertEquals("Title", viewModel.getTitle());
        assertEquals(currentSection, viewModel.getCurrentSection());
        assertEquals(Boolean.FALSE, viewModel.getHasFinanceSection());
        assertEquals(sectionId, viewModel.getFinanceSectionId());
        assertEquals(Boolean.TRUE, viewModel.getUserIsLeadApplicant());
        assertEquals(asList(2L), viewModel.getCompletedSections());
        assertEquals(asMap(sectionId, currentSection), viewModel.getSections());
        assertEquals(sectionAssignableViewModel, viewModel.getAssignable());
        assertEquals(navigationViewModel, viewModel.getNavigation());
        assertEquals(currentUser, viewModel.getCurrentUser());
        assertEquals(applicationViewModel, viewModel.getApplication());

        assertEquals(academicOrganisations, viewModel.getAcademicOrganisations());
        assertEquals(Boolean.FALSE, viewModel.getAllQuestionsCompleted());
        assertEquals(applicationOrganisations, viewModel.getApplicationOrganisations());
        assertEquals(BigDecimal.TEN, viewModel.getCompletedQuestionsPercentage());
        assertEquals(asMap(), viewModel.getCompletedSectionsByOrganisation());
        assertEquals(sectionId, viewModel.getEachCollaboratorFinanceSectionId());
        assertEquals(organisation, viewModel.getLeadOrganisation());
        assertEquals(pendingNames, viewModel.getPendingOrganisationNames());
        assertEquals(new HashSet<>(asList(2L)), viewModel.getSectionsMarkedAsComplete());
    }

    @Test
    public void testGetCurrentQuestionFormInputs() {
        assertEquals(null, viewModel.getCurrentQuestionFormInputs());
    }

    @Test
    public void testGetIsYourFinancesAndIsNotCompleted() {
        currentSection.setName("Your finances");
        viewModel.setCurrentSection(currentSection);
        viewModel.setCompletedSections(asList());

        assertEquals(Boolean.TRUE, viewModel.getIsYourFinancesAndIsNotCompleted());
    }

    @Test
    public void testGetIsYourFinances() {
        currentSection.setName("NOT Your finances");
        viewModel.setCurrentSection(currentSection);

        assertEquals(Boolean.FALSE, viewModel.getIsYourFinances());
    }

    @Test
    public void getIsFinanceOverview() {
        currentSection.setName("Finances overview");
        viewModel.setCurrentSection(currentSection);

        assertEquals(Boolean.TRUE, viewModel.getIsFinanceOverview());
    }

    @Test
    public void testGetIsSection() {
        assertEquals(Boolean.TRUE, viewModel.getIsSection());
    }

}
