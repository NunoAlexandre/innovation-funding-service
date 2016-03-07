package com.worth.ifs;

import com.worth.ifs.application.builder.QuestionBuilder;
import com.worth.ifs.application.builder.SectionBuilder;
import com.worth.ifs.application.builder.SectionResourceBuilder;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.*;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.service.FinanceService;
import com.worth.ifs.application.finance.view.DefaultFinanceFormHandler;
import com.worth.ifs.application.finance.view.DefaultFinanceModelManager;
import com.worth.ifs.application.finance.view.FinanceHandler;
import com.worth.ifs.application.finance.view.FinanceOverviewModelManager;
import com.worth.ifs.application.model.UserApplicationRole;
import com.worth.ifs.application.model.UserRole;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.ApplicationStatusResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.service.*;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.assessment.domain.AssessmentStates;
import com.worth.ifs.assessment.dto.Score;
import com.worth.ifs.assessment.service.AssessmentRestService;
import com.worth.ifs.commons.security.TokenAuthenticationService;
import com.worth.ifs.commons.security.UserAuthentication;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.service.CompetitionsRestService;
import com.worth.ifs.exception.ErrorController;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.service.ApplicationFinanceRestService;
import com.worth.ifs.finance.service.CostRestService;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.domain.FormInputType;
import com.worth.ifs.form.service.FormInputResponseService;
import com.worth.ifs.form.service.FormInputService;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.user.resource.OrganisationTypeResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.service.OrganisationTypeRestService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.worth.ifs.BuilderAmendFunctions.*;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.application.builder.ApplicationStatusBuilder.newApplicationStatus;
import static com.worth.ifs.application.builder.ApplicationStatusResourceBuilder.newApplicationStatusResource;
import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.SectionBuilder.newSection;
import static com.worth.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static com.worth.ifs.application.service.Futures.settable;
import static com.worth.ifs.commons.rest.RestResult.restFailure;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.form.builder.FormInputBuilder.newFormInput;
import static com.worth.ifs.form.builder.FormInputResponseBuilder.newFormInputResponse;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class BaseUnitTest {

    public MockMvc mockMvc;
    public User loggedInUser;
    public User assessor;
    public User applicant;

    public UserAuthentication loggedInUserAuthentication;

    protected final Log log = LogFactory.getLog(getClass());

    @Mock
    public ApplicationFinanceRestService applicationFinanceRestService;
    @Mock
    public UserAuthenticationService userAuthenticationService;
    @Mock
    public ResponseService responseService;
    @Mock
    public FormInputResponseService formInputResponseService;
    @Mock
    public FormInputService formInputService;
    @Mock
    public ApplicationService applicationService;
    @Mock
    public ApplicationStatusRestService applicationStatusService;
    @Mock
    public CompetitionsRestService competitionRestService;
    @Mock
    public AssessmentRestService assessmentRestService;
    @Mock
    public ProcessRoleService processRoleService;
    @Mock
    public UserService userService;
    @Mock
    public FinanceService financeService;
    @Mock
    public CostService costService;
    @Mock
    public CostRestService costRestService;
    @Mock
    public ApplicationRestService applicationRestService;
    @Mock
    public QuestionService questionService;
    @Mock
    public OrganisationService organisationService;
    @Mock
    public OrganisationTypeRestService organisationTypeRestService;
    @Mock
    public SectionService sectionService;
    @Mock
    public CompetitionService competitionService;
    @Mock
    public InviteRestService inviteRestService;
    @Mock
    public TokenAuthenticationService tokenAuthenticationService;
    @Mock
    public DefaultFinanceModelManager defaultFinanceModelManager;
    @Mock
    public DefaultFinanceFormHandler defaultFinanceFormHandler;
    @Mock
    public FinanceHandler financeHandler;
    @Mock
    public FinanceOverviewModelManager financeOverviewModelManager;

    @Mock
    public Environment env;

    @Mock
    public MessageSource messageSource;

    public List<ApplicationResource> applications;
    public List<Section> sections;
    public List<SectionResource> sectionResources;
    public Map<Long, Question> questions;
    public Map<Long, FormInputResponse> formInputsToFormInputResponses;
    public List<Competition> competitions;
    public Competition competition;
    public List<CompetitionResource> competitionResources;
    public CompetitionResource competitionResource;
    public List<User> users;
    public List<Organisation> organisations;
    TreeSet<Organisation> organisationSet;
    public List<Assessment> assessments;
    public List<ProcessRole> assessorProcessRoles;
    public List<ProcessRole> applicantRoles;
    public List<Assessment> submittedAssessments;
    public ApplicationFinanceResource applicationFinanceResource;
    public ApplicationStatusResource submittedApplicationStatus;
    public ApplicationStatusResource createdApplicationStatus;
    public ApplicationStatusResource approvedApplicationStatus;
    public ApplicationStatusResource rejectedApplicationStatus;
    public List<ProcessRole> processRoles;

    public List<ProcessRole> application1ProcessRoles;
    public List<ProcessRole> application2ProcessRoles;
    public List<ProcessRole> application3ProcessRoles;
    public List<ProcessRole> application4ProcessRoles;

    public List<Organisation> application1Organisations;
    public List<Organisation> application2Organisations;
    public List<Organisation> application3Organisations;
    public List<Organisation> application4Organisations;


    private Random randomGenerator;
    private FormInput formInput;
    private FormInputType formInputType;
    public OrganisationTypeResource organisationTypeResource;
    public InviteResource invite;
    public InviteResource acceptedInvite;
    public InviteResource existingUserInvite;

    public static final String INVITE_HASH = "b157879c18511630f220325b7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String INVITE_HASH_EXISTING_USER = "cccccccccc630f220325b7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String INVALID_INVITE_HASH = "aaaaaaa7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String ACCEPTED_INVITE_HASH = "BBBBBBBBB7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";


    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/resources");
        viewResolver.setSuffix(".html");
        return viewResolver;
    }

    public <T> T attributeFromMvcResultModel(MvcResult result, String key){
        return (T)result.getModelAndView().getModel().entrySet().stream()
                .filter(entry -> entry.getKey().equals(key))
                .map(entry -> entry.getValue())
                .findFirst().orElse(null);
    }

    public void setup(){
        loggedInUser = new User(1L, "Nico Bijl", "email@email.nl", "test", "tokenABC", "image", new ArrayList());
        applicant = loggedInUser;
        User user2 = new User(2L, "Brent de Kok", "email@email.nl", "test", "tokenBCD", "image", new ArrayList());
        assessor = new User(3L, "Assessor", "email@assessor.nl", "test", "tokenDEF", "image", new ArrayList<>());
        users = asList(loggedInUser, user2);

        loggedInUserAuthentication = new UserAuthentication(loggedInUser);

        applications = new ArrayList<>();
        sections = new ArrayList<>();
        questions = new HashMap<>();
        organisations = new ArrayList<>();
        randomGenerator = new Random();
        
    }

    public void setupOrganisationTypes() {
        ArrayList<OrganisationTypeResource> organisationTypes = new ArrayList<>();
        organisationTypeResource = new OrganisationTypeResource(1L, "Business", null);
        organisationTypes.add(organisationTypeResource);
        OrganisationTypeResource research = new OrganisationTypeResource(2L, "Research", null);
        organisationTypes.add(research);
        organisationTypes.add(new OrganisationTypeResource(3L, "Public Sector", null));
        organisationTypes.add(new OrganisationTypeResource(4L, "Charity", null));
        organisationTypes.add(new OrganisationTypeResource(5L, "University (HEI)", 2L));
        organisationTypes.add(new OrganisationTypeResource(6L, "Research & technology organisation (RTO)", 2L));
        organisationTypes.add(new OrganisationTypeResource(7L, "Catapult", 2L));
        organisationTypes.add(new OrganisationTypeResource(8L, "Public sector research establishment", 2L));
        organisationTypes.add(new OrganisationTypeResource(9L, "Research council institute", 2L));

        when(organisationTypeRestService.getAll()).thenReturn(restSuccess(organisationTypes));
        when(organisationTypeRestService.findOne(anyLong())).thenReturn(restSuccess(new OrganisationTypeResource(99L, "Unknown organisation type", null)));
        when(organisationTypeRestService.findOne(1L)).thenReturn(restSuccess(organisationTypeResource));
        when(organisationTypeRestService.findOne(2L)).thenReturn(restSuccess(research));

    }

    public void loginDefaultUser(){
        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(loggedInUserAuthentication);
        when(userAuthenticationService.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(loggedInUser);
    }
    public void loginUser(User user){
        UserAuthentication userAuthentication = new UserAuthentication(user);
        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(userAuthentication);
        when(userAuthenticationService.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);
    }

    public void setupCompetition(){
        formInput = newFormInput().build();
        formInputType = new FormInputType(1L, "textarea");
        formInput.setFormInputType(formInputType);

        competition = newCompetition().with(id(1L)).with(name("Competition x")).with(description("Description afds")).
                withStartDate(LocalDateTime.now().minusDays(2)).withEndDate(LocalDateTime.now().plusDays(5)).
                build();

        competitionResource = newCompetitionResource().with(id(1L)).with(name("Competition x")).with(description("Description afds")).
                withStartDate(LocalDateTime.now().minusDays(2)).withEndDate(LocalDateTime.now().plusDays(5)).
                build();

        QuestionBuilder questionBuilder = newQuestion().with(competition(competition));
        SectionBuilder sectionBuilder = newSection().with(competition(competition));
        SectionResourceBuilder sectionResourceBuilder = newSectionResource().with(competition(competition.getId()));

        Question q01 = questionBuilder.with(id(1L)).with(name("Application details")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(3)).
                build();

        Section section1 = sectionBuilder.
                with(id(1L)).
                with(name("Application details")).
                withQuestions(singletonList(q01)).
                build();

        SectionResource sectionResource1 = sectionResourceBuilder.
                with(id(1L)).
                with(name("Application details")).
                withQuestions(simpleMap(singletonList(q01), Question::getId)).
                build();

        Question q10 = questionBuilder.with(id(10L)).with(name("How does your project align with the scope of this competition?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).
                build();

        Section section2 = sectionBuilder.
                with(id(2L)).
                with(name("Scope (Gateway question)")).
                withQuestions(singletonList(q10)).
                build();
        SectionResource sectionResource2 = sectionResourceBuilder.
                with(id(2L)).
                with(name("Scope (Gateway question)")).
                withQuestions(simpleMap(singletonList(q10), Question::getId)).
                build();

        Question q20 = questionBuilder.with(id(20L)).with(name("1. What is the business opportunity that this project addresses?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).
                build();

        Question q21 = questionBuilder.with(id(21L)).with(name("2. What is the size of the market opportunity that this project might open up?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();
        Question q22 = questionBuilder.with(id(22L)).with(name("3. How will the results of the project be exploited and disseminated?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();
        Question q23 = questionBuilder.with(id(23L)).with(name("4. What economic, social and environmental benefits is the project expected to deliver?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();

        Section section3 = sectionBuilder.
                with(id(3L)).
                with(name("Business proposition (Q1 - Q4)")).
                withQuestions(asList(q20, q21, q22, q23)).
                build();
        SectionResource sectionResource3 = sectionResourceBuilder.
                with(id(3L)).
                with(name("Business proposition (Q1 - Q4)")).
                withQuestions(simpleMap(asList(q20, q21, q22, q23), Question::getId)).
                build();


        Question q30 = questionBuilder.with(id(30L)).with(name("5. What technical approach will be adopted and how will the project be managed?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();
        Question q31 = questionBuilder.with(id(31L)).with(name("6. What is innovative about this project?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();
        Question q32 = questionBuilder.with(id(32L)).with(name("7. What are the risks (technical, commercial and environmental) to project success? What is the project's risk management strategy?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();
        Question q33 = questionBuilder.with(id(33L)).with(name("8. Does the project team have the right skills and experience and access to facilities to deliver the identified benefits?")).
                withFormInputs(newFormInput().with(incrementingIds(1)).withFormInputType(formInputType).build(1)).build();

        Section section4 = sectionBuilder.
                with(id(4L)).
                with(name("Project approach (Q5 - Q8)")).
                withQuestions(asList(q30, q31, q32, q33)).
                build();
        SectionResource sectionResource4 = sectionResourceBuilder.
                with(id(4L)).
                with(name("Project approach (Q5 - Q8)")).
                withQuestions(simpleMap(asList(q30, q31, q32, q33), Question::getId)).
                build();

        Section section5 = sectionBuilder.with(id(5L)).with(name("Funding (Q9 - Q10)")).build();
        Section section6 = sectionBuilder.with(id(6L)).with(name("Finances")).build();
        Section section7 = sectionBuilder.with(id(7L)).with(name("Your finances")).build();
        section6.setChildSections(Arrays.asList(section7));
        SectionResource sectionResource5 = sectionResourceBuilder.with(id(5L)).with(name("Funding (Q9 - Q10)")).build();
        SectionResource sectionResource6 = sectionResourceBuilder.with(id(6L)).with(name("Finances")).build();
        SectionResource sectionResource7 = sectionResourceBuilder.with(id(7L)).with(name("Your finances")).build();
        sectionResource6.setChildSections(Arrays.asList(sectionResource7.getId()));


        sections = asList(section1, section2, section3, section4, section5, section6, section7);
        sectionResources = asList(sectionResource1, sectionResource2, sectionResource3, sectionResource4, sectionResource5, sectionResource6, sectionResource7);
        sectionResources.forEach(s -> {
                    s.setChildSections(new ArrayList<>());
                    when(sectionService.getById(s.getId())).thenReturn(s);
                    when(sectionService.getByName(s.getName())).thenReturn(s);
                }
        );

        ArrayList<Question> questionList = new ArrayList<>();
        for (Section section : sections) {
            List<Question> sectionQuestions = section.getQuestions();
            if(sectionQuestions != null){
                Map<Long, Question> questionsMap =
                        sectionQuestions.stream().collect(toMap(Question::getId,
                                identity()));
                questionList.addAll(sectionQuestions);
                questions.putAll(questionsMap);
            }
        }

        questions.forEach((id, question) -> {
            when(questionService.getById(id)).thenReturn(question);
        });

        competition.setSections(sections);
        competitionResource.setSections(sections.stream().map(s -> s.getId()).collect(toList()));
        when(sectionService.getParentSections(anyList())).thenReturn(sectionResources);
        competitions = singletonList(competition);
        when(questionService.findByCompetition(competition.getId())).thenReturn(questionList);
        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competitionResource));
        when(competitionRestService.getAll()).thenReturn(restSuccess(competitionResources));
        when(competitionService.getById(any(Long.class))).thenReturn(competitionResource);
    }

    public void setupUserRoles() {
        Role assessorRole = new Role(3L, UserRole.ASSESSOR.getRoleName(), null);
        Role applicantRole = new Role(4L, UserRole.APPLICANT.getRoleName(), null);
        loggedInUser.setRoles(singletonList(applicantRole));
        assessor.setRoles(singletonList(assessorRole));
    }

    public void setupApplicationWithRoles(){
        createdApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.CREATED.getId())).withName(ApplicationStatusConstants.CREATED.getName()).build();
        submittedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.SUBMITTED.getId())).withName(ApplicationStatusConstants.SUBMITTED.getName()).build();
        approvedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.APPROVED.getId())).withName(ApplicationStatusConstants.APPROVED.getName()).build();
        rejectedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.REJECTED.getId())).withName(ApplicationStatusConstants.REJECTED.getName()).build();

        ApplicationStatus createdApplicationStatus2 = newApplicationStatus().with(status -> status.setId(ApplicationStatusConstants.CREATED.getId())).withName(ApplicationStatusConstants.CREATED.getName()).build();
        ApplicationStatus submittedApplicationStatus2 = newApplicationStatus().with(status -> status.setId(ApplicationStatusConstants.SUBMITTED.getId())).withName(ApplicationStatusConstants.SUBMITTED.getName()).build();
        ApplicationStatus approvedApplicationStatus2 = newApplicationStatus().with(status -> status.setId(ApplicationStatusConstants.APPROVED.getId())).withName(ApplicationStatusConstants.APPROVED.getName()).build();
        ApplicationStatus rejectedApplicationStatus2 = newApplicationStatus().with(status -> status.setId(ApplicationStatusConstants.REJECTED.getId())).withName(ApplicationStatusConstants.REJECTED.getName()).build();

        // Build the backing applications.

        List<ApplicationResource> applicationResources = asList(
                newApplicationResource().with(id(1L)).with(name("Rovel Additive Manufacturing Process")).withStartDate(LocalDate.now().plusMonths(3)).withApplicationStatus(createdApplicationStatus).build(),
                newApplicationResource().with(id(2L)).with(name("Providing sustainable childcare")).withStartDate(LocalDate.now().plusMonths(4)).withApplicationStatus(submittedApplicationStatus).build(),
                newApplicationResource().with(id(3L)).with(name("Mobile Phone Data for Logistics Analytics")).withStartDate(LocalDate.now().plusMonths(5)).withApplicationStatus(approvedApplicationStatus).build(),
                newApplicationResource().with(id(4L)).with(name("Using natural gas to heat homes")).withStartDate(LocalDate.now().plusMonths(6)).withApplicationStatus(rejectedApplicationStatus).build()
        );

        List<Application> applicationList = asList(
            newApplication().with(id(1L)).with(name("Rovel Additive Manufacturing Process")).withStartDate(LocalDate.now().plusMonths(3)).withApplicationStatus(createdApplicationStatus2).build(),
            newApplication().with(id(2L)).with(name("Providing sustainable childcare")).withStartDate(LocalDate.now().plusMonths(4)).withApplicationStatus(submittedApplicationStatus2).build(),
            newApplication().with(id(3L)).with(name("Mobile Phone Data for Logistics Analytics")).withStartDate(LocalDate.now().plusMonths(5)).withApplicationStatus(approvedApplicationStatus2).build(),
            newApplication().with(id(4L)).with(name("Using natural gas to heat homes")).withStartDate(LocalDate.now().plusMonths(6)).withApplicationStatus(rejectedApplicationStatus2).build()
        );

        Map<Long, ApplicationResource> idsToApplicationResources = applicationResources.stream().collect(toMap(a -> a.getId(), a -> a));

        Role role1 = new Role(1L, UserApplicationRole.LEAD_APPLICANT.getRoleName(), null);
        Role role2 = new Role(2L, UserApplicationRole.COLLABORATOR.getRoleName(), null);
        Role assessorRole = new Role(3L, UserRole.ASSESSOR.getRoleName(), null);

        Organisation organisation1 = new Organisation(1L, "Empire Ltd");
        organisation1.setOrganisationType(new OrganisationType("Business", null));
        Organisation organisation2 = new Organisation(2L, "Ludlow");
        organisation2.setOrganisationType(new OrganisationType("Research", null));
        organisations = asList(organisation1, organisation2);
        Comparator<Organisation> compareById = Comparator.comparingLong(Organisation::getId);
        organisationSet = new TreeSet<>(compareById);
        organisationSet.addAll(organisations);

        ProcessRole processRole1 = newProcessRole().with(id(1L)).withApplication(applicationList.get(0)).withUser(loggedInUser).withRole(role1).withOrganisation(organisation1).build();
        ProcessRole processRole2 = newProcessRole().with(id(2L)).withApplication(applicationList.get(0)).withUser(loggedInUser).withRole(role1).withOrganisation(organisation1).build();
        ProcessRole processRole3 = newProcessRole().with(id(3L)).withApplication(applicationList.get(2)).withUser(loggedInUser).withRole(role1).withOrganisation(organisation1).build();
        ProcessRole processRole4 = newProcessRole().with(id(4L)).withApplication(applicationList.get(3)).withUser(loggedInUser).withRole(role1).withOrganisation(organisation1).build();
        ProcessRole processRole5 = newProcessRole().with(id(5L)).withApplication(applicationList.get(0)).withUser(loggedInUser).withRole(role2).withOrganisation(organisation2).build();
        ProcessRole processRole6 = newProcessRole().with(id(6L)).withApplication(applicationList.get(1)).withUser(assessor).withRole(assessorRole).withOrganisation(organisation1).build();
        ProcessRole processRole7 = newProcessRole().with(id(7L)).withApplication(applicationList.get(2)).withUser(assessor).withRole(assessorRole).withOrganisation(organisation1).build();
        ProcessRole processRole8 = newProcessRole().with(id(8L)).withApplication(applicationList.get(0)).withUser(assessor).withRole(assessorRole).withOrganisation(organisation1).build();
        ProcessRole processRole9 = newProcessRole().with(id(9L)).withApplication(applicationList.get(3)).withUser(assessor).withRole(assessorRole).withOrganisation(organisation1).build();
        ProcessRole processRole10 = newProcessRole().with(id(10L)).withApplication(applicationList.get(1)).withUser(loggedInUser).withRole(role1).withOrganisation(organisation2).build();

        assessorProcessRoles = asList(processRole6, processRole7, processRole8, processRole9);
        processRoles = asList(processRole1,processRole2, processRole3, processRole4, processRole5, processRole6, processRole7, processRole8, processRole9);
        applicantRoles = asList(processRole1, processRole2, processRole3, processRole4, processRole5);
        application1ProcessRoles = asList(processRole1, processRole2, processRole5);
        application2ProcessRoles = asList(processRole6, processRole10);
        application3ProcessRoles = asList(processRole3, processRole7);
        application4ProcessRoles = asList(processRole4, processRole9);

        application1Organisations = asList(organisation1, organisation2);
        application2Organisations = asList(organisation1, organisation2);
        application3Organisations = asList(organisation1);
        application4Organisations = asList(organisation1);

        organisation1.setProcessRoles(asList(processRole1, processRole2, processRole3, processRole4, processRole7, processRole8, processRole8));
        organisation2.setProcessRoles(singletonList(processRole5));
        applicationList.forEach(competition::addApplication);


        applicationList.get(0).setCompetition(competition);
        applicationList.get(0).setProcessRoles(asList(processRole1, processRole5));
        applicationList.get(1).setCompetition(competition);
        applicationList.get(1).setProcessRoles(singletonList(processRole2));
        applicationList.get(2).setCompetition(competition);
        applicationList.get(2).setProcessRoles(asList(processRole3, processRole7, processRole8));
        applicationList.get(3).setCompetition(competition);
        applicationList.get(3).setProcessRoles(singletonList(processRole4));

        applicationResources.get(0).setCompetition(competition.getId());
        applicationResources.get(0).setProcessRoles(asList(processRole1.getId(), processRole5.getId()));
        applicationResources.get(1).setCompetition(competition.getId());
        applicationResources.get(1).setProcessRoles(singletonList(processRole2.getId()));
        applicationResources.get(2).setCompetition(competition.getId());
        applicationResources.get(2).setProcessRoles(asList(processRole3.getId(), processRole7.getId(), processRole8.getId()));
        applicationResources.get(3).setCompetition(competition.getId());
        applicationResources.get(3).setProcessRoles(singletonList(processRole4.getId()));

        loggedInUser.addUserApplicationRole(processRole1, processRole2, processRole3, processRole4);
        users.get(0).addUserApplicationRole(processRole5);
        applications = applicationResources;

        when(sectionService.getParentSections(simpleMap(competition.getSections(), Section::getId))).thenReturn(sectionResources);
        when(sectionService.getCompleted(applicationList.get(0).getId(), organisation1.getId())).thenReturn(asList(1L, 2L));
        when(sectionService.getInCompleted(applicationList.get(0).getId())).thenReturn(asList(3L, 4L));
        when(processRoleService.findProcessRole(applicant.getId(), applicationList.get(0).getId())).thenReturn(processRole1);
        when(processRoleService.findProcessRole(applicant.getId(), applicationList.get(1).getId())).thenReturn(processRole2);
        when(processRoleService.findProcessRole(applicant.getId(), applicationList.get(2).getId())).thenReturn(processRole3);
        when(processRoleService.findProcessRole(applicant.getId(), applicationList.get(3).getId())).thenReturn(processRole4);
        when(processRoleService.findProcessRole(users.get(0).getId(), applicationList.get(0).getId())).thenReturn(processRole5);
        when(processRoleService.findProcessRole(assessor.getId(), applicationList.get(1).getId())).thenReturn(processRole6);
        when(processRoleService.findProcessRole(assessor.getId(), applicationList.get(2).getId())).thenReturn(processRole7);
        when(processRoleService.findProcessRole(assessor.getId(), applicationList.get(0).getId())).thenReturn(processRole8);
        when(processRoleService.findProcessRole(assessor.getId(), applicationList.get(3).getId())).thenReturn(processRole9);

        when(processRoleService.findProcessRolesByApplicationId(applicationList.get(0).getId())).thenReturn(application1ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationList.get(1).getId())).thenReturn(application2ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationList.get(2).getId())).thenReturn(application3ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationList.get(3).getId())).thenReturn(application4ProcessRoles);

		Map<Long, Set<Long>> completedMap = new HashMap<>();
        completedMap.put(organisation1.getId(), new TreeSet<>());
        completedMap.put(organisation2.getId(), new TreeSet<>());
        when(sectionService.getCompletedSectionsByOrganisation(applicationList.get(0).getId())).thenReturn(completedMap);
        when(sectionService.getCompletedSectionsByOrganisation(applicationList.get(1).getId())).thenReturn(completedMap);
        when(sectionService.getCompletedSectionsByOrganisation(applicationList.get(2).getId())).thenReturn(completedMap);

        processRoles.forEach(pr -> when(applicationService.findByProcessRoleId(pr.getId())).thenReturn(restSuccess(idsToApplicationResources.get(pr.getApplication().getId()))));

        when(applicationRestService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(restSuccess(applications));

        when(applicationService.getById(applications.get(0).getId())).thenReturn(applications.get(0));
        when(applicationService.getById(applications.get(1).getId())).thenReturn(applications.get(1));
        when(applicationService.getById(applications.get(2).getId())).thenReturn(applications.get(2));
        when(applicationService.getById(applications.get(3).getId())).thenReturn(applications.get(3));
        when(organisationService.getOrganisationById(organisationSet.first().getId())).thenReturn(organisationSet.first());
        when(organisationService.getUserOrganisation(applications.get(0), loggedInUser.getId())).thenReturn(Optional.of(organisation1));
        when(organisationService.getApplicationLeadOrganisation(applications.get(0))).thenReturn(Optional.of(organisation1));
        when(organisationService.getApplicationOrganisations(applications.get(0))).thenReturn(organisationSet);
        when(organisationService.getApplicationOrganisations(applications.get(1))).thenReturn(organisationSet);
        when(organisationService.getApplicationOrganisations(applications.get(2))).thenReturn(organisationSet);
        when(organisationService.getApplicationOrganisations(applications.get(3))).thenReturn(organisationSet);
        when(organisationService.getOrganisationType(loggedInUser.getId(), applications.get(0).getId())).thenReturn("Business");
        when(userService.isLeadApplicant(loggedInUser.getId(), applications.get(0))).thenReturn(true);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(0))).thenReturn(processRole1);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(1))).thenReturn(processRole2);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(2))).thenReturn(processRole3);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(3))).thenReturn(processRole4);
        when(organisationService.getApplicationLeadOrganisation(applications.get(0))).thenReturn(Optional.of(organisation1));
        when(organisationService.getApplicationLeadOrganisation(applications.get(1))).thenReturn(Optional.of(organisation1));
        when(organisationService.getApplicationLeadOrganisation(applications.get(2))).thenReturn(Optional.of(organisation1));
        processRoles.forEach(processRole -> when(processRoleService.getById(processRole.getId())).thenReturn(settable(processRole)));

        when(sectionService.getById(1L)).thenReturn(sectionResources.get(0));
        when(sectionService.getById(3L)).thenReturn(sectionResources.get(2));
    }

    public void setupApplicationResponses(){
        ApplicationResource application = applications.get(0);
        Application app = newApplication().build();

        ProcessRole userApplicationRole = loggedInUser.getProcessRoles().get(0);

        Response response = new Response(1L, LocalDateTime.now(), userApplicationRole, questions.get(20L), app);
        Response response2 = new Response(2L, LocalDateTime.now(), userApplicationRole, questions.get(21L), app);

        List<Response> responses = asList(response, response2);
        userApplicationRole.setResponses(responses);

        questions.get(20L).setResponses(singletonList(response));
        questions.get(21L).setResponses(singletonList(response2));

        when(responseService.getByApplication(application.getId())).thenReturn(responses);

        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);

        when(formInputService.getOne(anyLong())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return newFormInput().with(id((Long) args[0])).build();
        });

        List<FormInput> formInputs = questions.get(01L).getFormInputs();
        List<FormInputResponse> formInputResponses = newFormInputResponse().withFormInputs(formInputs).
                with(idBasedValues("Value ")).build(formInputs.size());

        when(formInputResponseService.getByApplication(application.getId())).thenReturn(formInputResponses);
        formInputResponses.stream().map(FormInputResponse::getFormInput).map(FormInput::getId).forEach(log::debug);
        formInputsToFormInputResponses = formInputResponses.stream().collect(toMap(formInputResponse -> formInputResponse.getFormInput().getId(), identity()));
        when(formInputResponseService.mapFormInputResponsesToFormInput(formInputResponses)).thenReturn(formInputsToFormInputResponses);
    }

    public void setupFinances() {
        ApplicationResource application = applications.get(0);
        applicationFinanceResource = new ApplicationFinanceResource(1L, application.getId(), organisations.get(0).getId(), OrganisationSize.LARGE);
        when(financeService.getApplicationFinanceDetails(loggedInUser.getId(), application.getId())).thenReturn(applicationFinanceResource);
        when(financeService.getApplicationFinance(loggedInUser.getId(), application.getId())).thenReturn(applicationFinanceResource);
        when(applicationFinanceRestService.getResearchParticipationPercentage(anyLong())).thenReturn(restSuccess(0.0));
        when(financeHandler.getFinanceFormHandler("Business")).thenReturn(defaultFinanceFormHandler);
        when(financeHandler.getFinanceModelManager("Business")).thenReturn(defaultFinanceModelManager);
    }

    public void setupAssessment(){

        Assessment assessment1 = new Assessment(assessorProcessRoles.get(2));
        assessment1.setId(1L);
        Assessment assessment2 = new Assessment(assessorProcessRoles.get(0));
        assessment2.setId(2L);
        Assessment assessment3 = new Assessment(assessorProcessRoles.get(1));
        assessment3.setId(3L);
        Assessment assessment4 = new Assessment(assessorProcessRoles.get(3));
        assessment4.setId(4L);

        when(assessmentRestService.getTotalAssignedByAssessorAndCompetition(assessor.getId(), competition.getId())).thenReturn(restSuccess(3));
        when(assessmentRestService.getTotalSubmittedByAssessorAndCompetition(assessor.getId(), competition.getId())).thenReturn(restSuccess(1));

        assessment1.setProcessStatus(AssessmentStates.REJECTED.getState());
        assessment2.setProcessStatus(AssessmentStates.PENDING.getState());
        assessment3.setProcessStatus(AssessmentStates.SUBMITTED.getState());
        assessment4.setProcessStatus(AssessmentStates.ASSESSED.getState());

        submittedAssessments = singletonList(assessment3);
        assessments = asList(assessment1, assessment2, assessment3, assessment4);
        when(assessmentRestService.getAllByAssessorAndCompetition(assessor.getId(), competition.getId())).thenReturn(restSuccess(assessments));

        when(assessmentRestService.getOneByProcessRole(assessment1.getProcessRole().getId())).thenReturn(restSuccess(assessment1));
        when(assessmentRestService.getOneByProcessRole(assessment1.getProcessRole().getId())).thenReturn(restSuccess(assessment2));
        when(assessmentRestService.getOneByProcessRole(assessment1.getProcessRole().getId())).thenReturn(restSuccess(assessment3));
        when(assessmentRestService.getOneByProcessRole(assessment1.getProcessRole().getId())).thenReturn(restSuccess(assessment4));

        when(organisationService.getUserOrganisation(applications.get(0), assessor.getId())).thenReturn(Optional.of(organisations.get(0)));
        when(organisationService.getUserOrganisation(applications.get(1), assessor.getId())).thenReturn(Optional.of(organisations.get(0)));
        when(organisationService.getUserOrganisation(applications.get(2), assessor.getId())).thenReturn(Optional.of(organisations.get(0)));

        assessments.forEach(assessment -> when(assessmentRestService.getScore(assessment.getId())).thenReturn(restSuccess(new Score())));
    }

    public void setupInvites() {
        when(inviteRestService.getInvitesByApplication(isA(Long.class))).thenReturn(restSuccess(emptyList()));


        invite = new InviteResource();
        invite.setStatus(InviteStatusConstants.SEND);
        invite.setApplication(1L);
        invite.setName("Some Invitee");
        invite.setHash(INVITE_HASH);
        String email = "invited@email.com";
        invite.setEmail(email);
        when(inviteRestService.getInviteByHash(eq(INVITE_HASH))).thenReturn(restSuccess(invite));
        when(userService.findUserByEmail(eq(email))).thenReturn(restSuccess(emptyList()));
        when(inviteRestService.getInviteByHash(eq(INVALID_INVITE_HASH))).thenReturn(restFailure(emptyList()));

        acceptedInvite = new InviteResource();
        acceptedInvite.setStatus(InviteStatusConstants.ACCEPTED);
        acceptedInvite.setApplication(1L);
        acceptedInvite.setName("Some Invitee");
        acceptedInvite.setHash(ACCEPTED_INVITE_HASH);
        acceptedInvite.setEmail(email);
        when(inviteRestService.getInviteByHash(eq(ACCEPTED_INVITE_HASH))).thenReturn(restSuccess(acceptedInvite));

        existingUserInvite = new InviteResource();
        existingUserInvite.setStatus(InviteStatusConstants.SEND);
        existingUserInvite.setApplication(1L);
        existingUserInvite.setName("Some Invitee");
        existingUserInvite.setHash(INVITE_HASH_EXISTING_USER);
        existingUserInvite.setEmail("existing@email.com");
        when(userService.findUserByEmail(eq("existing@email.com"))).thenReturn(restSuccess(asList(new UserResource())));
        when(inviteRestService.getInviteByHash(eq(INVITE_HASH_EXISTING_USER))).thenReturn(restSuccess(existingUserInvite));

        when(inviteRestService.getInvitesByApplication(isA(Long.class))).thenReturn(restSuccess(emptyList()));
        when(inviteRestService.getInviteOrganisationByHash(INVITE_HASH)).thenReturn(restSuccess(new InviteOrganisationResource()));

    }

    public ExceptionHandlerExceptionResolver createExceptionResolver() {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(ErrorController.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new ErrorController(env, messageSource), method);
            }
        };
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }

    @Bean(name = "messageSource")
    public MessageSource testMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }
}
