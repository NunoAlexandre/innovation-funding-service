package org.innovateuk.ifs.application;

import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationModelPopulator;
import org.innovateuk.ifs.application.populator.ApplicationOverviewModelPopulator;
import org.innovateuk.ifs.application.populator.ApplicationPrintPopulator;
import org.innovateuk.ifs.application.populator.AssessorQuestionFeedbackPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.service.*;
import org.innovateuk.ifs.assessment.service.AssessmentRestService;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.controller.viewmodel.OptionalFileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.populator.OrganisationDetailsModelPopulator;
import org.innovateuk.ifs.profiling.ProfileExecution;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.innovateuk.ifs.application.resource.ApplicationState.SUBMITTED;
import static org.innovateuk.ifs.commons.rest.ValidationMessages.collectValidationMessages;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.PROJECT_SETUP;
import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * This controller will handle all requests that are related to the application overview.
 * Application overview is the page that contains the most basic information about the current application and
 * the basic information about the competition the application is related to.
 */

@Controller
@RequestMapping("/application")
@PreAuthorize("hasAuthority('applicant')")
public class ApplicationController {

    public static final String ASSIGN_QUESTION_PARAM = "assign_question";
    public static final String MARK_AS_COMPLETE = "mark_as_complete";

    @Autowired
    private ApplicationOverviewModelPopulator applicationOverviewModelPopulator;

    @Autowired
    private AssessorFeedbackRestService assessorFeedbackRestService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ApplicationModelPopulator applicationModelPopulator;

    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Autowired
    private ApplicationPrintPopulator applicationPrintPopulator;

    @Autowired
    private FormInputResponseService formInputResponseService;

    @Autowired
    private FormInputResponseRestService formInputResponseRestService;

    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;

    @Autowired
    private AssessorQuestionFeedbackPopulator assessorQuestionFeedbackPopulator;

    @Autowired
    private UserRestService userRestService;

    @Autowired
    private AssessorFormInputResponseRestService assessorFormInputResponseRestService;

    @Autowired
    private AssessmentRestService assessmentRestService;

    public static String redirectToApplication(ApplicationResource application) {
        return "redirect:/application/" + application.getId();
    }

    @ProfileExecution
    @GetMapping("/{applicationId}")
    public String applicationDetails(ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                     @ModelAttribute("loggedInUser") UserResource user) {

        Long userId = user.getId();
        applicationOverviewModelPopulator.populateModel(applicationId, userId, form, model);
        return "application-details";
    }

    @ProfileExecution
    @PostMapping(value = "/{applicationId}")
    public String applicationDetails(@PathVariable("applicationId") long applicationId,
                                     @ModelAttribute("loggedInUser") UserResource user,
                                     HttpServletRequest request) {

        ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);

        questionService.assignQuestion(applicationId, request, assignedBy);
        return "redirect:/application/" + applicationId;
    }

    @ProfileExecution
    @GetMapping("/{applicationId}/section/{sectionId}")
    public String applicationDetailsOpenSection(ApplicationForm form, Model model,
                                                @PathVariable("applicationId") long applicationId,
                                                @PathVariable("sectionId") long sectionId,
                                                @ModelAttribute("loggedInUser") UserResource user) {
        ApplicationResource application = applicationService.getById(applicationId);
        SectionResource section = sectionService.getById(sectionId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(applicationId);
        ProcessRoleResource userApplicationRole = simpleFindFirst(userApplicationRoles, role -> role.getUser().equals(user.getId())).get();

        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, Optional.ofNullable(section), Optional.empty(), model, form, userApplicationRoles);
        applicationModelPopulator.addOrganisationAndUserFinanceDetails(competition.getId(), applicationId, user, model, form, userApplicationRole.getOrganisationId());
        model.addAttribute("ableToSubmitApplication", ableToSubmitApplication(userApplicationRole, application));
        return "application-details";
    }

    private boolean ableToSubmitApplication(ProcessRoleResource userApplicationRole, ApplicationResource application) {
        return userIsLeadApplicant(userApplicationRole) && application.isSubmittable();
    }

    private boolean userIsLeadApplicant(ProcessRoleResource userApplicationRole) {
        return userApplicationRole.getRoleName().equals(UserRoleType.LEADAPPLICANT.getName());
    }

    @ProfileExecution
    @GetMapping("/{applicationId}/summary")
    public String applicationSummary(@ModelAttribute("form") ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                     @ModelAttribute("loggedInUser") UserResource user) {

        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(applicationId);

        List<FormInputResponseResource> responses = formInputResponseRestService.getResponsesByApplicationId(applicationId).getSuccessObjectOrThrowException();
        model.addAttribute("incompletedSections", sectionService.getInCompleted(applicationId));
        model.addAttribute("responses", formInputResponseService.mapFormInputResponsesToFormInput(responses));

        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles);
        ProcessRoleResource userApplicationRole = userRestService.findProcessRole(user.getId(), applicationId).getSuccessObjectOrThrowException();

        applicationModelPopulator.addOrganisationAndUserFinanceDetails(competition.getId(), applicationId, user, model, form, userApplicationRole.getOrganisationId());

        model.addAttribute("applicationReadyForSubmit", applicationService.isApplicationReadyForSubmit(application.getId()));

        if (PROJECT_SETUP.equals(competition.getCompetitionStatus())) {
            OptionalFileDetailsViewModel assessorFeedbackViewModel = getAssessorFeedbackViewModel(application);
            model.addAttribute("assessorFeedback", assessorFeedbackViewModel);
        }

        if (competition.getCompetitionStatus().isFeedbackReleased()) {
            model.addAttribute("scores", assessorFormInputResponseRestService.getApplicationAssessmentAggregate(applicationId).getSuccessObjectOrThrowException());
            model.addAttribute("feedback", assessmentRestService.getApplicationFeedback(applicationId)
                    .getSuccessObjectOrThrowException()
                    .getFeedback()
            );

            return "application-feedback-summary";
        } else {
            return "application-summary";
        }
    }

    @GetMapping(value = "/{applicationId}/question/{questionId}/feedback")
    public String applicationAssessorQuestionFeedback(Model model, @PathVariable("applicationId") long applicationId,
                                                      @PathVariable("questionId") long questionId) {
        ApplicationResource applicationResource = applicationService.getById(applicationId);
        if (!applicationResource.getCompetitionStatus().isFeedbackReleased()) {
            return "redirect:/application/" + applicationId + "/summary";
        }
        model.addAttribute("model", assessorQuestionFeedbackPopulator.populate(applicationResource, questionId));
        return "application-assessor-feedback";

    }

    @ProfileExecution
    @PostMapping("/{applicationId}/summary")
    public String applicationSummarySubmit(@PathVariable("applicationId") long applicationId,
                                           @ModelAttribute("loggedInUser") UserResource user,
                                           HttpServletRequest request) {

        Map<String, String[]> params = request.getParameterMap();

        if (params.containsKey(ASSIGN_QUESTION_PARAM)) {
            ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);
            questionService.assignQuestion(applicationId, request, assignedBy);
        } else if (params.containsKey(MARK_AS_COMPLETE)) {
            Long markQuestionCompleteId = Long.valueOf(request.getParameter(MARK_AS_COMPLETE));
            if (markQuestionCompleteId != null) {
                ProcessRoleResource processRole = processRoleService.findProcessRole(user.getId(), applicationId);
                List<ValidationMessages> markAsCompleteErrors = questionService.markAsComplete(markQuestionCompleteId, applicationId, processRole.getId());

                if (collectValidationMessages(markAsCompleteErrors).hasErrors()) {
                    questionService.markAsInComplete(markQuestionCompleteId, applicationId, processRole.getId());
                }
            }
        }

        return "redirect:/application/" + applicationId + "/summary";
    }

    @ProfileExecution
    @GetMapping("/{applicationId}/confirm-submit")
    public String applicationConfirmSubmit(ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                           @ModelAttribute("loggedInUser") UserResource user) {
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(applicationId);
        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles);
        return "application-confirm-submit";
    }

    @PostMapping("/{applicationId}/submit")
    public String applicationSubmit(ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                    @ModelAttribute("loggedInUser") UserResource user,
                                    HttpServletResponse response) {
        ApplicationResource application = applicationService.getById(applicationId);
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(applicationId);
        ProcessRoleResource userApplicationRole = simpleFindFirst(userApplicationRoles, role -> role.getUser().equals(user.getId())).get();

        if (!ableToSubmitApplication(userApplicationRole, application)) {
            cookieFlashMessageFilter.setFlashMessage(response, "cannotSubmit");
            return "redirect:/application/" + applicationId + "/confirm-submit";
        }

        applicationService.updateState(applicationId, SUBMITTED);
        application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles);
        return "application-submitted";
    }

    @ProfileExecution
    @GetMapping("/{applicationId}/track")
    public String applicationTrack(ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                   @ModelAttribute("loggedInUser") UserResource user) {
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(applicationId);
        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles);
        return "application-track";
    }

    @ProfileExecution
    @GetMapping("/create/{competitionId}")
    public String applicationCreatePage() {
        return "application-create";
    }

    @ProfileExecution
    @PostMapping("/create/{competitionId}")
    public String applicationCreate(Model model,
                                    @PathVariable("competitionId") long competitionId,
                                    @RequestParam(value = "application_name", required = true) String applicationName,
                                    @ModelAttribute("loggedInUser") UserResource user) {
        Long userId = user.getId();

        String applicationNameWithoutWhiteSpace = applicationName.replaceAll("\\s", "");

        if (applicationNameWithoutWhiteSpace.length() > 0) {
            ApplicationResource application = applicationService.createApplication(competitionId, userId, applicationName);
            return "redirect:/application/" + application.getId();
        } else {
            model.addAttribute("applicationNameEmpty", true);
            return "application-create";
        }
    }

    @ProfileExecution
    @GetMapping(value = "/create-confirm-competition")
    public String competitionCreateApplication() {
        return "application-create-confirm-competition";
    }

    @GetMapping("/terms-and-conditions")
    public String termsAndConditions() {
        return "application-terms-and-conditions";
    }

    @GetMapping("/{applicationId}/assessorFeedback")
    public @ResponseBody
    ResponseEntity<ByteArrayResource> downloadAssessorFeedbackFile(
            @PathVariable("applicationId") long applicationId) {

        ByteArrayResource resource = assessorFeedbackRestService.getAssessorFeedbackFile(applicationId).getSuccessObjectOrThrowException();
        FileEntryResource fileDetails = assessorFeedbackRestService.getAssessorFeedbackFileDetails(applicationId).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails);
    }

    /**
     * Printable version of the application
     */
    @GetMapping(value = "/{applicationId}/print")
    public String printApplication(@PathVariable("applicationId") long applicationId,
                                   Model model,
                                   @ModelAttribute("loggedInUser") UserResource user) {
        return applicationPrintPopulator.print(applicationId, model, user);
    }

    /**
     * Assign a question to a user
     *
     * @param applicationId the application for which the user is assigned
     * @param sectionId     section id for showing details
     * @param request       request parameters
     * @return
     */
    @ProfileExecution
    @PostMapping("/{applicationId}/section/{sectionId}")
    public String assignQuestion(@PathVariable("applicationId") long applicationId,
                                 @PathVariable("sectionId") long sectionId,
                                 @ModelAttribute("loggedInUser") UserResource user,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        doAssignQuestion(applicationId, user, request, response);

        return "redirect:/application/" + applicationId + "/section/" + sectionId;
    }

    private OptionalFileDetailsViewModel getAssessorFeedbackViewModel(ApplicationResource application) {

        boolean readonly = true;

        Long assessorFeedbackFileEntry = application.getAssessorFeedbackFileEntry();

        if (assessorFeedbackFileEntry != null) {
            RestResult<FileEntryResource> fileEntry = assessorFeedbackRestService.getAssessorFeedbackFileDetails(application.getId());
            return OptionalFileDetailsViewModel.withExistingFile(fileEntry.getSuccessObjectOrThrowException(), readonly);
        } else {
            return OptionalFileDetailsViewModel.withNoFile(readonly);
        }
    }

    private void doAssignQuestion(Long applicationId, UserResource user, HttpServletRequest request, HttpServletResponse response) {
        ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);

        questionService.assignQuestion(applicationId, request, assignedBy);
        cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
    }

    private void addApplicationAndSectionsInternalWithOrgDetails(final ApplicationResource application, final CompetitionResource competition,
                                                                 final UserResource user, final Model model, final ApplicationForm form,
                                                                 List<ProcessRoleResource> userApplicationRoles) {
        addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, Optional.empty(), Optional.empty(), model, form, userApplicationRoles);
    }

    private void addApplicationAndSectionsInternalWithOrgDetails(final ApplicationResource application, final CompetitionResource competition,
                                                                 final UserResource user, Optional<SectionResource> section, Optional<Long> currentQuestionId,
                                                                 final Model model, final ApplicationForm form, List<ProcessRoleResource> userApplicationRoles) {

        organisationDetailsModelPopulator.populateModel(model, application.getId(), userApplicationRoles);
        applicationModelPopulator.addApplicationAndSections(application, competition, user, section, currentQuestionId, model, form, userApplicationRoles);
    }
}
