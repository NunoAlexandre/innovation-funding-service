package org.innovateuk.ifs.application.populator;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.application.UserApplicationRole;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.service.*;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.application.viewmodel.QuestionOrganisationDetailsViewModel;
import org.innovateuk.ifs.application.viewmodel.QuestionViewModel;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.form.service.FormInputService;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static org.innovateuk.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.category.builder.ResearchCategoryResourceBuilder.newResearchCategoryResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuestionModelPopulatorTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private QuestionModelPopulator populator;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private UserService userService;

    @Mock
    private QuestionService questionService;

    @Mock
    private InviteRestService inviteRestService;

    @Mock
    private SectionService sectionService;

    @Mock
    private FormInputService formInputService;

    @Mock
    private FormInputResponseService formInputResponseService;

    @Mock
    private ApplicationNavigationPopulator applicationNavigationPopulator;

    @Mock
    private Model model;

    @Mock
    private CategoryService categoryService;

    private Long questionId;
    private Long applicationId;
    private Long competitionId;
    private Long organisationId;
    private Long researchCategoryId;

    private CompetitionResource competition;
    private ApplicationResource application;
    private UserResource user;
    private OrganisationResource organisation;
    private ApplicationForm form;
    private QuestionOrganisationDetailsViewModel organisationDetailsViewModel;
    private QuestionResource question;
    private List<FormInputResource> formInputs;
    private List<ProcessRoleResource> userApplicationRoles;
    private List<ResearchCategoryResource> researchCategories;

    @Before
    public void setUp() {
        super.setUp();

        questionId = 18L;
        applicationId = 23L;
        competitionId = 56L;
        organisationId = 3L;
        researchCategoryId = 1L;
        question = newQuestionResource().withId(questionId).build();
        researchCategories = newResearchCategoryResource().build(1);
        application = newApplicationResource().withId(applicationId).withCompetition(competitionId).withResearchCategories(researchCategories.stream().collect(Collectors.toSet())).build();
        competition = newCompetitionResource().withId(competitionId).build();
        organisation = newOrganisationResource().withId(organisationId).build();
        user = newUserResource().build();
        form = new ApplicationForm();
        formInputs = newFormInputResource().build(10);
        organisationDetailsViewModel = new QuestionOrganisationDetailsViewModel();

        userApplicationRoles = newProcessRoleResource()
            .withApplication(applicationId)
            .withRole(newRoleResource().withName(UserApplicationRole.LEAD_APPLICANT.getRoleName()).build(),
                    newRoleResource().withName(UserApplicationRole.COLLABORATOR.getRoleName()).build(),
                    newRoleResource().withName(UserApplicationRole.LEAD_APPLICANT.getRoleName()).build())
            .withOrganisation(3L, 4L, 5L)
            .withUser(user)
            .build(3);
    }

    @Test
    public void testPopulateModelWithValidObjects() throws Exception {
        setupSuccess();

        QuestionViewModel viewModel = populator.populateModel(questionId, applicationId, user, model, form, organisationDetailsViewModel);
        assertNotEquals(null, viewModel);

        assertEquals(user, viewModel.getCurrentUser());
        assertEquals(application, viewModel.getApplication().getCurrentApplication());
        assertEquals(competition, viewModel.getApplication().getCurrentCompetition());
        assertEquals(question, viewModel.getCurrentQuestion());
        assertEquals(organisation, viewModel.getApplication().getUserOrganisation());
        assertEquals(organisation, viewModel.getApplication().getLeadOrganisation());
        assertEquals(user, viewModel.getLeadApplicant());
        assertEquals(Boolean.TRUE, viewModel.getUserIsLeadApplicant());
        assertEquals(researchCategories, viewModel.getApplication().getResearchCategories());
    }

    private void setupSuccess(){

        when(applicationNavigationPopulator.addNavigation(any(QuestionResource.class), anyLong())).thenReturn(new NavigationViewModel());
        when(questionService.getById(questionId)).thenReturn(question);
        when(formInputService.findApplicationInputsByQuestion(questionId)).thenReturn(formInputs);
        when(applicationService.getById(applicationId)).thenReturn(application);
        when(competitionService.getById(application.getCompetition())).thenReturn(competition);
        when(processRoleService.findProcessRolesByApplicationId(applicationId)).thenReturn(userApplicationRoles);
        when(organisationService.getOrganisationById(anyLong())).thenReturn(organisation);
        when(userService.isLeadApplicant(user.getId(), application)).thenReturn(Boolean.TRUE);
        when(userService.getLeadApplicantProcessRoleOrNull(application)).thenReturn(newProcessRoleResource().withUser(user).build());
        when(userService.findById(user.getId())).thenReturn(user);
        when(categoryService.getResearchCategories()).thenReturn(researchCategories);
    }
}