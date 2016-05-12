package com.worth.ifs;

import com.worth.ifs.application.UserApplicationRole;
import com.worth.ifs.application.builder.QuestionResourceBuilder;
import com.worth.ifs.application.builder.SectionResourceBuilder;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.resource.ApplicationStatusResource;
import com.worth.ifs.application.resource.SectionType;
import com.worth.ifs.application.finance.model.UserRole;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.service.FinanceService;
import com.worth.ifs.application.finance.view.*;
import com.worth.ifs.application.resource.*;
import com.worth.ifs.application.service.*;
import com.worth.ifs.assessment.builder.AssessmentBuilder;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.resource.Score;
import com.worth.ifs.assessment.service.AssessmentRestService;
import com.worth.ifs.commons.security.UserAuthentication;
import com.worth.ifs.commons.security.UserAuthenticationService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.service.CompetitionsRestService;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.finance.service.ApplicationFinanceRestService;
import com.worth.ifs.finance.service.CostRestService;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.resource.FormInputResponseResource;
import com.worth.ifs.form.resource.FormInputTypeResource;
import com.worth.ifs.form.service.FormInputResponseService;
import com.worth.ifs.form.service.FormInputService;
import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.resource.InviteOrganisationResource;
import com.worth.ifs.invite.resource.InviteResource;
import com.worth.ifs.invite.service.InviteOrganisationRestService;
import com.worth.ifs.invite.service.InviteRestService;
import com.worth.ifs.user.builder.OrganisationBuilder;
import com.worth.ifs.user.builder.ProcessRoleBuilder;
import com.worth.ifs.user.resource.*;
import com.worth.ifs.user.service.OrganisationRestService;
import com.worth.ifs.user.service.OrganisationTypeRestService;
import com.worth.ifs.user.service.ProcessRoleService;
import com.worth.ifs.user.service.UserService;
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
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.worth.ifs.BuilderAmendFunctions.*;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static com.worth.ifs.application.builder.ApplicationStatusBuilder.newApplicationStatus;
import static com.worth.ifs.application.builder.ApplicationStatusResourceBuilder.newApplicationStatusResource;
import static com.worth.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static com.worth.ifs.application.builder.ResponseResourceBuilder.newResponseResource;
import static com.worth.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static com.worth.ifs.application.service.Futures.settable;
import static com.worth.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.rest.RestResult.restFailure;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.form.builder.FormInputBuilder.newFormInput;
import static com.worth.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static com.worth.ifs.form.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.user.builder.OrganisationTypeBuilder.newOrganisationType;
import static com.worth.ifs.user.builder.OrganisationTypeResourceBuilder.newOrganisationTypeResource;
import static com.worth.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
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
    public UserResource loggedInUser;
    public UserResource assessor;
    public UserResource applicant;

    public UserResource assessorUser;
    public UserResource applicantUser;

    public UserAuthentication loggedInUserAuthentication;

    protected final Log log = LogFactory.getLog(getClass());

    @Mock
    public ApplicationFinanceRestService applicationFinanceRestService;
    @Mock
    public InviteOrganisationRestService inviteOrganisationRestService;
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
    public OrganisationRestService organisationRestService;
    @Mock
    public OrganisationTypeRestService organisationTypeRestService;
    @Mock
    public SectionService sectionService;
    @Mock
    public CompetitionService competitionService;
    @Mock
    public InviteRestService inviteRestService;
    @Mock
    public FinanceModelManager financeModelManager;
    @Mock
    public DefaultFinanceModelManager defaultFinanceModelManager;
    @Mock
    public DefaultFinanceFormHandler defaultFinanceFormHandler;
    @Mock
    public FinanceHandler financeHandler;
    @Mock
    public FinanceOverviewModelManager financeOverviewModelManager;
    @Mock
    public FinanceFormHandler financeFormHandler;

    @Mock
    public Environment env;

    @Mock
    public MessageSource messageSource;

    public List<ApplicationResource> applications;
    public List<SectionResource> sectionResources;
    public Map<Long, QuestionResource> questionResources;

    public Map<Long, FormInputResponseResource> formInputsToFormInputResponses;
    public List<CompetitionResource> competitionResources;
    public CompetitionResource competitionResource;
    public List<UserResource> users;
    public List<OrganisationResource> organisations;
    TreeSet<OrganisationResource> organisationSet;
    public List<AssessmentResource> assessments;
    // TODO BO - remove assessorProcessRolesTemporary when Assessment is converted to DTO
    public List<ProcessRoleResource> assessorProcessRoleResources;
    public List<ProcessRoleResource> applicantRoles;
    public List<AssessmentResource> submittedAssessments;
    public ApplicationFinanceResource applicationFinanceResource;
    public ApplicationStatusResource submittedApplicationStatus;
    public ApplicationStatusResource createdApplicationStatus;
    public ApplicationStatusResource approvedApplicationStatus;
    public ApplicationStatusResource rejectedApplicationStatus;
    public ApplicationStatusResource openApplicationStatus;
    public List<ProcessRoleResource> processRoles;

    public List<ProcessRoleResource> application1ProcessRoles;
    public List<ProcessRoleResource> application2ProcessRoles;
    public List<ProcessRoleResource> application3ProcessRoles;
    public List<ProcessRoleResource> application4ProcessRoles;

    public List<OrganisationResource> application1Organisations;
    public List<OrganisationResource> application2Organisations;
    public List<OrganisationResource> application3Organisations;
    public List<OrganisationResource> application4Organisations;


    private Random randomGenerator;
    private FormInputResource formInput;
    private FormInputTypeResource formInputType;
    public OrganisationTypeResource businessOrganisationTypeResource;
    public OrganisationTypeResource researchOrganisationTypeResource;
    public OrganisationTypeResource businessOrganisationType;
    public OrganisationTypeResource researchOrganisationType;
    public InviteResource invite;
    public InviteResource acceptedInvite;
    public InviteResource existingUserInvite;

    public static final String INVITE_HASH = "b157879c18511630f220325b7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String INVITE_HASH_EXISTING_USER = "cccccccccc630f220325b7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String INVALID_INVITE_HASH = "aaaaaaa7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    public static final String ACCEPTED_INVITE_HASH = "BBBBBBBBB7a64cf3eb782759326d3cbb85e546e0d03e663ec711ec7ca65827a96";
    ;


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
        applications = new ArrayList<>();
        questionResources = new HashMap<>();
        organisations = new ArrayList<>();
        randomGenerator = new Random();

        setupUsers();
        setupOrganisationTypes();
        setupUserRoles();
    }

    private void setupUsers() {
        applicant = newUserResource().withId(1L)
                .withFirstName("James")
                .withLastName("Watts")
                .withEmail("james.watts@email.co.uk")
                .withUID("2aerg234-aegaeb-23aer").build();
        loggedInUser = applicant;

        UserResource user2 = newUserResource().withId(2L)
                .withFirstName("John")
                .withLastName("Patricks")
                .withEmail("john.patricks@email.co.uk")
                .withUID("6573ag-aeg32aeb-23aerr").build();

        assessor = newUserResource().withId(3L)
                .withFirstName("Clark")
                .withLastName("Baker")
                .withEmail("clark.baker@email.co.uk")
                .withUID("2522-34y34ah-hrt4420").build();
        users = asList(loggedInUser, user2);

        applicantUser = newUserResource().withId(1L).withFirstName("James").withLastName("Watts").withEmail("james.watts@email.co.uk").withUID("6573ag-aeg32aeb-23aerr").build();
        assessorUser = newUserResource().withId(3L).withFirstName("Clark").withLastName("Baker").withEmail("clark.baker@email.co.uk").withUID("2522-34y34ah-hrt4420").build();

        loggedInUserAuthentication = new UserAuthentication(loggedInUser);
    }

    public void setupOrganisationTypes() {

        businessOrganisationTypeResource = newOrganisationTypeResource().with(id(1L)).with(name("Business")).build();
        researchOrganisationTypeResource = newOrganisationTypeResource().with(id(2L)).with(name("Research")).build();

        // TODO DW - INFUND-1604 - remove when process roles are converted to DTOs
        businessOrganisationType = newOrganisationTypeResource().with(id(1L)).with(name("Business")).build();
        researchOrganisationType = newOrganisationTypeResource().with(id(2L)).with(name("Research")).build();

        ArrayList<OrganisationTypeResource> organisationTypes = new ArrayList<>();
        organisationTypes.add(businessOrganisationTypeResource);
        organisationTypes.add(researchOrganisationTypeResource);
        organisationTypes.add(new OrganisationTypeResource(3L, "Public Sector", null));
        organisationTypes.add(new OrganisationTypeResource(4L, "Charity", null));
        organisationTypes.add(new OrganisationTypeResource(5L, "University (HEI)", 2L));
        organisationTypes.add(new OrganisationTypeResource(6L, "Research & technology organisation (RTO)", 2L));
        organisationTypes.add(new OrganisationTypeResource(7L, "Catapult", 2L));
        organisationTypes.add(new OrganisationTypeResource(8L, "Public sector research establishment", 2L));
        organisationTypes.add(new OrganisationTypeResource(9L, "Research council institute", 2L));

        when(organisationTypeRestService.getAll()).thenReturn(restSuccess(organisationTypes));
        when(organisationTypeRestService.findOne(anyLong())).thenReturn(restSuccess(new OrganisationTypeResource(99L, "Unknown organisation type", null)));
        when(organisationTypeRestService.findOne(1L)).thenReturn(restSuccess(businessOrganisationTypeResource));
        when(organisationTypeRestService.findOne(2L)).thenReturn(restSuccess(researchOrganisationTypeResource));

    }

    public void loginDefaultUser(){
        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(loggedInUserAuthentication);
        when(userAuthenticationService.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(loggedInUser);
    }
    public void loginUser(UserResource user){
        UserAuthentication userAuthentication = new UserAuthentication(user);
        when(userAuthenticationService.getAuthentication(any(HttpServletRequest.class))).thenReturn(userAuthentication);
        when(userAuthenticationService.getAuthenticatedUser(any(HttpServletRequest.class))).thenReturn(user);
    }

    public void setupCompetition(){
        formInput = newFormInputResource().build();
        formInputType = new FormInputTypeResource(1L, "textarea");
        formInput.setFormInputType(formInputType.getId());

        competitionResource = newCompetitionResource().with(id(1L)).with(name("Competition x")).with(description("Description afds")).
                withStartDate(LocalDateTime.now().minusDays(2)).withEndDate(LocalDateTime.now().plusDays(5)).
                build();

        QuestionResourceBuilder questionResourceBuilder = newQuestionResource().withCompetition(competitionResource.getId());

        SectionResourceBuilder sectionResourceBuilder = newSectionResource().withCompetition(competitionResource.getId());

        QuestionResource q01Resource = questionResourceBuilder.with(id(1L)).with(name("Application details")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(3), FormInputResource::getId)).
                        build();
        when(questionService.getById(q01Resource.getId())).thenReturn(q01Resource);

        SectionResource sectionResource1 = sectionResourceBuilder.
                with(id(1L)).
                with(name("Application details")).
                withQuestions(simpleMap(singletonList(q01Resource), QuestionResource::getId)).
                build();

        QuestionResource q10Resource = questionResourceBuilder.with(id(10L)).with(name("How does your project align with the scope of this competition?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).
                        build();
        when(questionService.getById(q10Resource.getId())).thenReturn(q10Resource);

        SectionResource sectionResource2 = sectionResourceBuilder.
                with(id(2L)).
                with(name("Scope (Gateway question)")).
                withQuestions(simpleMap(singletonList(q10Resource), QuestionResource::getId)).
                build();

        QuestionResource q20Resource = questionResourceBuilder.with(id(20L)).with(name("1. What is the business opportunity that this project addresses?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).
                        build();
        when(questionService.getById(q20Resource.getId())).thenReturn(q20Resource);

        QuestionResource q21Resource = questionResourceBuilder.with(id(21L)).with(name("2. What is the size of the market opportunity that this project might open up?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q21Resource.getId())).thenReturn(q21Resource);

        QuestionResource q22Resource = questionResourceBuilder.with(id(22L)).with(name("3. How will the results of the project be exploited and disseminated?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q22Resource.getId())).thenReturn(q22Resource);

        QuestionResource q23Resource = questionResourceBuilder.with(id(23L)).with(name("4. What economic, social and environmental benefits is the project expected to deliver?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q23Resource.getId())).thenReturn(q23Resource);

        SectionResource sectionResource3 = sectionResourceBuilder.
                with(id(3L)).
                with(name("Business proposition (Q1 - Q4)")).
                withQuestions(simpleMap(asList(q20Resource, q21Resource, q22Resource, q23Resource), QuestionResource::getId)).
                build();


        QuestionResource q30Resource = questionResourceBuilder.with(id(30L)).with(name("5. What technical approach will be adopted and how will the project be managed?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q30Resource.getId())).thenReturn(q30Resource);

        QuestionResource q31Resource = questionResourceBuilder.with(id(31L)).with(name("6. What is innovative about this project?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q31Resource.getId())).thenReturn(q31Resource);

        QuestionResource q32Resource = questionResourceBuilder.with(id(32L)).with(name("7. What are the risks (technical, commercial and environmental) to project success? What is the project's risk management strategy?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q32Resource.getId())).thenReturn(q32Resource);

        QuestionResource q33Resource = questionResourceBuilder.with(id(33L)).with(name("8. Does the project team have the right skills and experience and access to facilities to deliver the identified benefits?")).
                withFormInputs(simpleMap(newFormInputResource().with(incrementingIds(1)).withFormInputType(formInputType.getId()).build(1), FormInputResource::getId)).build();
        when(questionService.getById(q33Resource.getId())).thenReturn(q33Resource);

        SectionResource sectionResource4 = sectionResourceBuilder.
                with(id(4L)).
                with(name("Project approach (Q5 - Q8)")).
                withQuestions(simpleMap(asList(q30Resource, q31Resource, q32Resource, q33Resource), QuestionResource::getId)).
                build();

        SectionResource sectionResource5 = sectionResourceBuilder.with(id(5L)).with(name("Funding (Q9 - Q10)")).build();
        SectionResource sectionResource6 = sectionResourceBuilder.with(id(6L)).with(name("Finances")).build();
        SectionResource sectionResource7 = sectionResourceBuilder.with(id(7L)).with(name("Your finances")).build();

        sectionResource6.setChildSections(Arrays.asList(sectionResource7.getId()));


        sectionResources = asList(sectionResource1, sectionResource2, sectionResource3, sectionResource4, sectionResource5, sectionResource6, sectionResource7);
        sectionResources.forEach(s -> {
                    s.setQuestionGroup(false);
                    s.setChildSections(new ArrayList<>());
                    when(sectionService.getById(s.getId())).thenReturn(s);
                }
        );
        when(sectionService.getSectionsForCompetitionByType(1L,SectionType.FINANCE)).thenReturn(Arrays.asList(sectionResource7));
        when(sectionService.getSectionsForCompetitionByType(1L,SectionType.ORGANISATION_FINANCES)).thenReturn(Arrays.asList(sectionResource6));

        ArrayList<QuestionResource> questionList = new ArrayList<>();
        for (SectionResource section : sectionResources) {
            section.setQuestionGroup(false);
            List<Long> sectionQuestions = section.getQuestions();
            section.setQuestionGroup(false);
            if(sectionQuestions != null){
                Map<Long, QuestionResource> questionsMap =
                        sectionQuestions.stream().collect(
                                toMap(identity(), q -> questionService.getById(q)));
                questionList.addAll(questionsMap.values());
                questionResources.putAll(questionsMap);

                when(sectionService.getQuestionsForSectionAndSubsections(eq(section.getId())))
                        .thenReturn(new HashSet<>(questionList.stream().map(QuestionResource::getId).collect(Collectors.toList())));
            }
        }

        sectionResource7.setQuestionGroup(true);

        questionResources.forEach((id, question) -> {
            when(questionService.getById(id)).thenReturn(question);
        });

        when(questionService.getPreviousQuestionBySection(any())).thenReturn(Optional.empty());
        when(questionService.getNextQuestionBySection(any())).thenReturn(Optional.empty());
        when(questionService.getNextQuestion(any())).thenReturn(Optional.empty());
        when(questionService.getPreviousQuestion(any())).thenReturn(Optional.empty());

        when(questionService.getNextQuestion(eq(q01Resource.getId()))).thenReturn(Optional.of(q10Resource));
        when(questionService.getPreviousQuestion(eq(q10Resource.getId()))).thenReturn(Optional.of(q01Resource));

        when(questionService.getNextQuestion(eq(q10Resource.getId()))).thenReturn(Optional.of(q20Resource));
        when(questionService.getPreviousQuestion(eq(q20Resource.getId()))).thenReturn(Optional.of(q10Resource));

        when(questionService.getNextQuestion(eq(q20Resource.getId()))).thenReturn(Optional.of(q21Resource));
        when(questionService.getPreviousQuestion(eq(q21Resource.getId()))).thenReturn(Optional.of(q20Resource));

        when(questionService.getNextQuestion(eq(q21Resource.getId()))).thenReturn(Optional.of(q22Resource));
        when(questionService.getPreviousQuestion(eq(q22Resource.getId()))).thenReturn(Optional.of(q21Resource));

        when(sectionService.getSectionByQuestionId(eq(q01Resource.getId()))).thenReturn(sectionResource1);
        when(sectionService.getSectionByQuestionId(eq(q10Resource.getId()))).thenReturn(sectionResource2);
        when(sectionService.getSectionByQuestionId(eq(q20Resource.getId()))).thenReturn(sectionResource3);
        when(sectionService.getSectionByQuestionId(eq(q21Resource.getId()))).thenReturn(sectionResource3);
        when(sectionService.getSectionByQuestionId(eq(q22Resource.getId()))).thenReturn(sectionResource3);

        competitionResource.setSections(sectionResources.stream().map(s -> s.getId()).collect(toList()));
        when(sectionService.filterParentSections(anyList())).thenReturn(sectionResources);
        competitionResources = singletonList(competitionResource);
        when(questionService.findByCompetition(competitionResource.getId())).thenReturn(questionList);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(competitionRestService.getAll()).thenReturn(restSuccess(competitionResources));
        when(competitionService.getById(any(Long.class))).thenReturn(competitionResource);
    }

    public void setupUserRoles() {
        RoleResource assessorRole = new RoleResource(3L, UserRole.ASSESSOR.getRoleName(), null);
        assessorRole.setUrl("assessor/dashboard");
        RoleResource applicantRole = new RoleResource(4L, UserRole.APPLICANT.getRoleName(), null);
        applicantRole.setUrl("applicant/dashboard");
        applicant.setRoles(singletonList(applicantRole));
        assessor.setRoles(singletonList(assessorRole));
    }

    public void setupApplicationWithRoles(){
        openApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.OPEN.getId())).withName(ApplicationStatusConstants.OPEN.getName()).build();
        createdApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.CREATED.getId())).withName(ApplicationStatusConstants.CREATED.getName()).build();
        submittedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.SUBMITTED.getId())).withName(ApplicationStatusConstants.SUBMITTED.getName()).build();
        approvedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.APPROVED.getId())).withName(ApplicationStatusConstants.APPROVED.getName()).build();
        rejectedApplicationStatus = newApplicationStatusResource().with(status -> status.setId(ApplicationStatusConstants.REJECTED.getId())).withName(ApplicationStatusConstants.REJECTED.getName()).build();

        // Build the backing applications.

        List<ApplicationResource> applicationResources = asList(
                newApplicationResource().with(id(1L)).with(name("Rovel Additive Manufacturing Process")).withStartDate(LocalDate.now().plusMonths(3)).withApplicationStatus(ApplicationStatusConstants.CREATED).build(),
                newApplicationResource().with(id(2L)).with(name("Providing sustainable childcare")).withStartDate(LocalDate.now().plusMonths(4)).withApplicationStatus(ApplicationStatusConstants.SUBMITTED).build(),
                newApplicationResource().with(id(3L)).with(name("Mobile Phone Data for Logistics Analytics")).withStartDate(LocalDate.now().plusMonths(5)).withApplicationStatus(ApplicationStatusConstants.APPROVED).build(),
                newApplicationResource().with(id(4L)).with(name("Using natural gas to heat homes")).withStartDate(LocalDate.now().plusMonths(6)).withApplicationStatus(ApplicationStatusConstants.REJECTED).build()
        );

        Map<Long, ApplicationResource> idsToApplicationResources = applicationResources.stream().collect(toMap(a -> a.getId(), a -> a));

        RoleResource role1 = newRoleResource().withId(1L).withName(UserApplicationRole.LEAD_APPLICANT.getRoleName()).build();
        RoleResource role2 = newRoleResource().withId(2L).withName(UserApplicationRole.COLLABORATOR.getRoleName()).build();
        RoleResource assessorRole = newRoleResource().withId(3L).withName(UserRole.ASSESSOR.getRoleName()).build();

        OrganisationResource organisation1 = newOrganisationResource().withId(1L).withOrganisationType(businessOrganisationTypeResource.getId()).withName("Empire Ltd").build();
        OrganisationResource organisation2 = newOrganisationResource().withId(2L).withOrganisationType(researchOrganisationTypeResource.getId()).withName("Ludlow").build();
        organisations = asList(organisation1, organisation2);
        Comparator<OrganisationResource> compareById = Comparator.comparingLong(OrganisationResource::getId);
        organisationSet = new TreeSet<>(compareById);
        organisationSet.addAll(organisations);

        ProcessRoleResource processRole1 = newProcessRoleResource().with(id(1L)).withApplicationId(applicationResources.get(0).getId()).withUser(applicantUser).withRole(role1).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole2 = newProcessRoleResource().with(id(2L)).withApplicationId(applicationResources.get(0).getId()).withUser(applicantUser).withRole(role1).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole3 = newProcessRoleResource().with(id(3L)).withApplicationId(applicationResources.get(2).getId()).withUser(applicantUser).withRole(role1).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole4 = newProcessRoleResource().with(id(4L)).withApplicationId(applicationResources.get(3).getId()).withUser(applicantUser).withRole(role1).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole5 = newProcessRoleResource().with(id(5L)).withApplicationId(applicationResources.get(0).getId()).withUser(applicantUser).withRole(role2).withOrganisation(organisation2.getId()).build();
        ProcessRoleResource processRole6 = newProcessRoleResource().with(id(6L)).withApplicationId(applicationResources.get(1).getId()).withUser(assessorUser).withRole(assessorRole).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole7 = newProcessRoleResource().with(id(7L)).withApplicationId(applicationResources.get(2).getId()).withUser(assessorUser).withRole(assessorRole).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole8 = newProcessRoleResource().with(id(8L)).withApplicationId(applicationResources.get(0).getId()).withUser(assessorUser).withRole(assessorRole).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole9 = newProcessRoleResource().with(id(9L)).withApplicationId(applicationResources.get(3).getId()).withUser(assessorUser).withRole(assessorRole).withOrganisation(organisation1.getId()).build();
        ProcessRoleResource processRole10 = newProcessRoleResource().with(id(10L)).withApplicationId(applicationResources.get(1).getId()).withUser(applicantUser).withRole(role1).withOrganisation(organisation2.getId()).build();

        assessorProcessRoleResources = asList(processRole6, processRole7, processRole8, processRole9);
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

        organisation1.setProcessRoles(simpleMap(asList(processRole1, processRole2, processRole3, processRole4, processRole7, processRole8, processRole8), ProcessRoleResource::getId));
        organisation2.setProcessRoles(simpleMap(singletonList(processRole5), ProcessRoleResource::getId));

        competitionResource.setApplications(simpleMap(applicationResources, ApplicationResource::getId));

        applicationResources.get(0).setCompetition(competitionResource.getId());
        applicationResources.get(0).setProcessRoles(asList(processRole1.getId(), processRole5.getId()));
        applicationResources.get(1).setCompetition(competitionResource.getId());
        applicationResources.get(1).setProcessRoles(singletonList(processRole2.getId()));
        applicationResources.get(2).setCompetition(competitionResource.getId());
        applicationResources.get(2).setProcessRoles(asList(processRole3.getId(), processRole7.getId(), processRole8.getId()));
        applicationResources.get(3).setCompetition(competitionResource.getId());
        applicationResources.get(3).setProcessRoles(singletonList(processRole4.getId()));

        loggedInUser.setProcessRoles(asList(processRole1.getId(), processRole2.getId(),processRole3.getId(), processRole4.getId()));
        users.get(0).setProcessRoles(asList(processRole5.getId()));
        applications = applicationResources;

        when(sectionService.filterParentSections(sectionResources)).thenReturn(sectionResources);
        when(sectionService.getCompleted(applicationResources.get(0).getId(), organisation1.getId())).thenReturn(asList(1L, 2L));
        when(sectionService.getInCompleted(applicationResources.get(0).getId())).thenReturn(asList(3L, 4L));
        when(processRoleService.findProcessRole(applicant.getId(), applicationResources.get(0).getId())).thenReturn(processRole1);
        when(processRoleService.findProcessRole(applicant.getId(), applicationResources.get(1).getId())).thenReturn(processRole2);
        when(processRoleService.findProcessRole(applicant.getId(), applicationResources.get(2).getId())).thenReturn(processRole3);
        when(processRoleService.findProcessRole(applicant.getId(), applicationResources.get(3).getId())).thenReturn(processRole4);
        when(processRoleService.findProcessRole(users.get(0).getId(), applicationResources.get(0).getId())).thenReturn(processRole5);
        when(processRoleService.findProcessRole(assessor.getId(), applicationResources.get(1).getId())).thenReturn(processRole6);
        when(processRoleService.findProcessRole(assessor.getId(), applicationResources.get(2).getId())).thenReturn(processRole7);
        when(processRoleService.findProcessRole(assessor.getId(), applicationResources.get(0).getId())).thenReturn(processRole8);
        when(processRoleService.findProcessRole(assessor.getId(), applicationResources.get(3).getId())).thenReturn(processRole9);

        when(processRoleService.findProcessRolesByApplicationId(applicationResources.get(0).getId())).thenReturn(application1ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationResources.get(1).getId())).thenReturn(application2ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationResources.get(2).getId())).thenReturn(application3ProcessRoles);
        when(processRoleService.findProcessRolesByApplicationId(applicationResources.get(3).getId())).thenReturn(application4ProcessRoles);

		Map<Long, Set<Long>> completedMap = new HashMap<>();
        completedMap.put(organisation1.getId(), new TreeSet<>());
        completedMap.put(organisation2.getId(), new TreeSet<>());
        when(sectionService.getCompletedSectionsByOrganisation(applicationResources.get(0).getId())).thenReturn(completedMap);
        when(sectionService.getCompletedSectionsByOrganisation(applicationResources.get(1).getId())).thenReturn(completedMap);
        when(sectionService.getCompletedSectionsByOrganisation(applicationResources.get(2).getId())).thenReturn(completedMap);

        processRoles.forEach(pr -> when(applicationService.findByProcessRoleId(pr.getId())).thenReturn(restSuccess(idsToApplicationResources.get(pr.getApplication()))));

        when(applicationRestService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(restSuccess(applications));
        when(applicationService.getById(applications.get(0).getId())).thenReturn(applications.get(0));
        when(applicationService.getById(applications.get(1).getId())).thenReturn(applications.get(1));
        when(applicationService.getById(applications.get(2).getId())).thenReturn(applications.get(2));
        when(applicationService.getById(applications.get(3).getId())).thenReturn(applications.get(3));
        when(organisationService.getOrganisationById(organisationSet.first().getId())).thenReturn(organisationSet.first());
        when(organisationService.getOrganisationByIdForAnonymousUserFlow(organisationSet.first().getId())).thenReturn(organisationSet.first());
        when(organisationService.getOrganisationType(loggedInUser.getId(), applications.get(0).getId())).thenReturn("Business");
        when(userService.isLeadApplicant(loggedInUser.getId(), applications.get(0))).thenReturn(true);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(0))).thenReturn(processRole1);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(1))).thenReturn(processRole2);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(2))).thenReturn(processRole3);
        when(userService.getLeadApplicantProcessRoleOrNull(applications.get(3))).thenReturn(processRole4);
        when(userService.findById(loggedInUser.getId())).thenReturn(loggedInUser);

        processRoles.forEach(processRole -> when(processRoleService.getById(processRole.getId())).thenReturn(settable(processRole)));

        when(sectionService.getById(1L)).thenReturn(sectionResources.get(0));
        when(sectionService.getById(3L)).thenReturn(sectionResources.get(2));

        organisations.forEach(organisation -> when(organisationRestService.getOrganisationById(organisation.getId())).thenReturn(restSuccess(organisation)));
        organisations.forEach(organisation -> when(organisationRestService.getOrganisationByIdForAnonymousUserFlow(organisation.getId())).thenReturn(restSuccess(organisation)));
    }

    public void setupApplicationResponses(){
        ApplicationResource application = applications.get(0);
        ApplicationResource app = newApplicationResource().build();

        Long userApplicationRoleId = loggedInUser.getProcessRoles().get(0);
        ProcessRoleResource userApplicationRole = processRoles.stream().filter(p -> p.getId().equals(userApplicationRoleId)).findFirst().get();
        ResponseResource responseResource = newResponseResource().withId(1L).withUpdatedBy(userApplicationRole.getId()).withQuestion(newQuestionResource().withId(20L)).withApplication(app).build();
        ResponseResource responseResource2 = newResponseResource().withId(2L).withUpdatedBy(userApplicationRole.getId()).withQuestion(newQuestionResource().withId(21L)).withApplication(app).build();

        List<ResponseResource> responseResources = asList(responseResource, responseResource2);

        questionResources.get(20L).setResponses(Arrays.asList(responseResource.getId()));
        questionResources.get(21L).setResponses(Arrays.asList(responseResource2.getId()));

        when(responseService.getByApplication(application.getId())).thenReturn(responseResources);

        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);

        when(formInputService.getOne(anyLong())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return newFormInputResource().with(id((Long) args[0])).build();
        });

        List<Long> formInputIds =  questionResources.get(1L).getFormInputs();
        List<FormInputResponseResource> formInputResponses = newFormInputResponseResource().withFormInputs(formInputIds).
                with(idBasedValues("Value "))
                .build(formInputIds.size());

        when(formInputResponseService.getByApplication(application.getId())).thenReturn(formInputResponses);
        formInputsToFormInputResponses = formInputResponses.stream().collect(toMap(formInputResponseResource -> formInputResponseResource.getFormInput(), identity()));
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

    public void setupInvites() {
        when(inviteRestService.getInvitesByApplication(isA(Long.class))).thenReturn(restSuccess(emptyList()));
        InviteOrganisationResource inviteOrganisation = new InviteOrganisationResource(2L, "Invited Organisation Ltd", null, null);

        invite = new InviteResource();
        invite.setStatus(InviteStatusConstants.SEND);
        invite.setApplication(1L);
        invite.setName("Some Invitee");
        invite.setHash(INVITE_HASH);
        String email = "invited@email.com";
        invite.setEmail(email);
        invite.setInviteOrganisation(inviteOrganisation.getId());
        inviteOrganisation.setInviteResources(Arrays.asList(invite));

        when(inviteRestService.getInviteByHash(eq(INVITE_HASH))).thenReturn(restSuccess(invite));
        when(inviteOrganisationRestService.findOne(eq(invite.getInviteOrganisation()))).thenReturn(restSuccess(inviteOrganisation));
        when(inviteOrganisationRestService.put(any())).thenReturn(restSuccess());
        when(inviteRestService.checkExistingUser(eq(INVITE_HASH))).thenReturn(restFailure(notFoundError(UserResource.class, INVITE_HASH)));
        when(inviteRestService.checkExistingUser(eq(INVALID_INVITE_HASH))).thenReturn(restFailure(notFoundError(UserResource.class, email)));
        when(inviteRestService.getInviteByHash(eq(INVALID_INVITE_HASH))).thenReturn(restFailure(emptyList()));
        when(inviteRestService.getInviteOrganisationByHash(INVITE_HASH)).thenReturn(restSuccess(new InviteOrganisationResource()));

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
        when(inviteRestService.checkExistingUser(eq(INVITE_HASH_EXISTING_USER))).thenReturn(restSuccess());
        when(inviteRestService.getInviteByHash(eq(INVITE_HASH_EXISTING_USER))).thenReturn(restSuccess(existingUserInvite));

        when(inviteRestService.getInvitesByApplication(isA(Long.class))).thenReturn(restSuccess(emptyList()));
        when(inviteRestService.getInviteOrganisationByHash(INVITE_HASH_EXISTING_USER)).thenReturn(restSuccess(new InviteOrganisationResource()));

    }

    @Bean(name = "messageSource")
    public MessageSource testMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }
}
