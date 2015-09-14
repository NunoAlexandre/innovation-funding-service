package com.worth.ifs.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.finance.FinanceFormHandler;
import com.worth.ifs.application.finance.service.FinanceServiceImpl;
import com.worth.ifs.application.helper.ApplicationHelper;
import com.worth.ifs.application.service.*;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.security.UserAuthenticationService;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.domain.UserApplicationRole;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This controller will handle all requests that are related to the application form.
 */
@Controller
@RequestMapping("/application-form")
public class ApplicationFormController {
    private final Log log = LogFactory.getLog(getClass());
    public final String LABOUR = "Labour";
    public final String WORKING_DAYS_PER_YEAR = "Working days per year";


    @Autowired
    ApplicationService applicationService;
    @Autowired
    ResponseService responseService;
    @Autowired
    UserService userService;
    @Autowired
    ProcessRoleService processRoleService;
    @Autowired
    SectionService sectionService;
    @Autowired
    FinanceServiceImpl financeService;
    @Autowired
    FinanceFormHandler financeFormHandler;
    
    @Autowired
    UserAuthenticationService userAuthenticationService;


    @RequestMapping("/{applicationId}")
    public String applicationForm(Model model,@PathVariable("applicationId") final Long applicationId,
                                  HttpServletRequest request){
        User user = userAuthenticationService.getAuthenticatedUser(request);
        this.addApplicationDetails(applicationId, user.getId(), 0L, model);
        return "application-form";
    }

    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.GET)
    public String applicationFormWithOpenSection(Model model,
                                     @PathVariable("applicationId") final Long applicationId,
                                     @PathVariable("sectionId") final Long sectionId,
                                     HttpServletRequest request){
        Application app = applicationService.getById(applicationId);
        User user = userAuthenticationService.getAuthenticatedUser(request);
        this.addApplicationDetails(applicationId, user.getId(), sectionId, model);

        return "application-form";
    }


    @RequestMapping(value = "/addcost/{applicationId}/{sectionId}/{questionId}")
    public String addAnother(Model model,
                             @PathVariable("applicationId") final Long applicationId,
                             @PathVariable("sectionId") final Long sectionId,
                             @PathVariable("questionId") final Long questionId,
                             HttpServletRequest request) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationFinance applicationFinance = financeService.getApplicationFinance(applicationId, user.getId());
        financeService.addCost(applicationFinance.getId(), questionId);
        this.addApplicationDetails(applicationId, user.getId(), sectionId, model);
        return "redirect:/application-form/"+applicationId + "/section/" + sectionId;
    }

    /**
     * Get the details of the current application, add this to the model so we can use it in the templates.
     */
    private void addApplicationDetails(Long applicationId, Long userId, Long currentSectionId, Model model){
        ApplicationHelper applicationHelper = new ApplicationHelper();
        Application application = applicationService.getById(applicationId);
        model.addAttribute("currentApplication", application);

        Competition competition = application.getCompetition();
        model.addAttribute("currentCompetition", competition);

        model.addAttribute("applicationOrganisations", applicationHelper.getApplicationOrganisations(application));
        model.addAttribute("assignableUsers", userService.getAssignable(application.getId()));
        model.addAttribute("leadOrganisation", applicationHelper.getApplicationLeadOrganisation(application).orElseGet(() -> null));

        List<Section> sectionsList = sectionService.getParentSections(competition.getSections());
        Map<Long, Section> sections =
                sectionsList.stream().collect(Collectors.toMap(Section::getId,
                        Function.identity()));
        model.addAttribute("sections", sections);
        model.addAttribute("completedSections", sectionService.getCompleted(applicationId));

        List<Response> responses = responseService.getByApplication(applicationId);
        model.addAttribute("responses", responseService.mapResponsesToQuestion(responses));

        addFinanceDetails(model, applicationId, userId);

        Section currentSection = getSection(application, currentSectionId);
        model.addAttribute("currentSectionId", currentSectionId);
        model.addAttribute("currentSection", currentSection);

        int todayDay =  LocalDateTime.now().getDayOfYear();
        model.addAttribute("todayDay", todayDay);
        model.addAttribute("yesterdayDay", todayDay-1);
    }


    private Section getSection(Application application, Long sectionId) {
        Competition comp = application.getCompetition();
        List<Section> sections = comp.getSections();

        // get the section that we want to show, so we can use this on to show the correct questions.
        Optional<Section> section = sections.stream().
                filter(x -> x.getId().equals(sectionId))
                .findFirst();

        return section.isPresent() ? section.get() : null;
    }

    private void addFinanceDetails(Model model, Long applicationId, Long userId) {
        ApplicationFinance applicationFinance = financeService.getApplicationFinance(userId, applicationId);
        if(applicationFinance==null) {
            applicationFinance = financeService.addApplicationFinance(userId, applicationId);
        }
        model.addAttribute("organisationFinance", financeService.getFinances(applicationFinance.getId()));
        model.addAttribute("financeTotal", financeService.getTotal(applicationFinance.getId()));
        model.addAttribute("financeSection", sectionService.getByName("Your finances"));
        model.addAttribute("organisationFinances", financeService.getFinances(applicationFinance.getId()));
    }

    /**
     * This method is for the post request when the users clicks the input[type=submit] button.
     * This is also used when the user clicks the 'mark-as-complete' button.
     */
    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.POST)
    public String applicationFormSubmit(Model model,
                                                 @PathVariable("applicationId") final Long applicationId,
                                                 @PathVariable("sectionId") final Long sectionId,
                                                 HttpServletRequest request){
        User user = userAuthenticationService.getAuthenticatedUser(request);
        Application application = applicationService.getById(applicationId);
        Competition comp = application.getCompetition();
        List<Section> sections = comp.getSections();

        // get the section that we want, so we can use this on to store the correct questions.
        Section section = sections.stream().filter(x -> x.getId().equals(sectionId)).findFirst().get();
        saveQuestionResponses(request, section.getQuestions(), user.getId(), applicationId);

        // save application details if they are in the request
        Map<String, String[]> params = request.getParameterMap();
        params.forEach((key, value) -> log.info("key " + key));

        setApplicationDetails(application, params);
        markQuestion(request, params, applicationId, user.getId());
        assignQuestion(request, params, applicationId, user.getId());

        applicationService.save(application);
        financeFormHandler.handle(request);

        addApplicationDetails(applicationId, user.getId(), sectionId, model);
        model.addAttribute("applicationSaved", true);
        return "application-form";
    }

    private void markQuestion(HttpServletRequest request, Map<String, String[]> params, Long applicationId, Long userId) {
        if(params.containsKey("mark_as_complete")){
            Long questionId = Long.valueOf(request.getParameter("mark_as_complete"));
            responseService.markQuestionAsComplete(applicationId, questionId,userId);
        }else if(params.containsKey("mark_as_incomplete")){
            Long questionId = Long.valueOf(request.getParameter("mark_as_incomplete"));
            responseService.markQuestionAsInComplete(applicationId, questionId, userId);
        }
    }

    private void assignQuestion(HttpServletRequest request, Map<String, String[]> params, Long applicationId, Long userId) {
        if(params.containsKey("assign_question")){
            String assign = request.getParameter("assign_question");
            Long questionId = Long.valueOf(assign.split("_")[0]);
            Long assigneeId = Long.valueOf(assign.split("_")[1]);

            responseService.assignQuestion(applicationId, questionId, userId, assigneeId);
        }
    }

    private void saveQuestionResponses(HttpServletRequest request, List<Question> questions, Long userId, Long applicationId) {
        // saving questions from section
        for (Question question : questions) {
            if(request.getParameterMap().containsKey("question[" + question.getId() + "]")){
                String value = request.getParameter("question[" + question.getId() + "]");
                Boolean saved = responseService.save(userId, applicationId, question.getId(), value);
                if(!saved){
                    log.error("save failed. " + question.getId());
                }
            }
        }
    }

    private void setApplicationDetails(Application application, Map<String, String[]> applicationDetailParams) {
        if(applicationDetailParams.containsKey("question[application_details-title]")){
            String title = applicationDetailParams.get("question[application_details-title]")[0];
            application.setName(title);
        }
        if(applicationDetailParams.containsKey("question[application_details-startdate][year]")){
            int year = Integer.valueOf(applicationDetailParams.get("question[application_details-startdate][year]")[0]);
            int month = Integer.valueOf(applicationDetailParams.get("question[application_details-startdate][month]")[0]);
            int day = Integer.valueOf(applicationDetailParams.get("question[application_details-startdate][day]")[0]);
            LocalDate date = LocalDate.of(year, month, day);
            application.setStartDate(date);
        }
        if(applicationDetailParams.containsKey("question[application_details-duration]")){
            Long duration = Long.valueOf(applicationDetailParams.get("question[application_details-duration]")[0]);
            application.setDurationInMonths(duration);
        }
    }

    /**
     * This method is for supporting ajax saving from the application form.
     */
    @RequestMapping(value = "/saveFormElement", method = RequestMethod.POST)
    public @ResponseBody JsonNode saveFormElement(@RequestParam("questionId") String inputIdentifier,
                                                  @RequestParam("value") String value,
                                                  @RequestParam("applicationId") Long applicationId,
                                                  HttpServletRequest request) {

        User user = userAuthenticationService.getAuthenticatedUser(request);

        if(inputIdentifier.equals("application_details-title")){
            Application application = applicationService.getById(applicationId);
            application.setName(value);
            applicationService.save(application);
        } else if(inputIdentifier.equals("application_details-duration")){
            Application application = applicationService.getById(applicationId);
            application.setDurationInMonths(Long.valueOf(value));
            applicationService.save(application);
        } else if(inputIdentifier.startsWith("application_details-startdate")){
            Application application = applicationService.getById(applicationId);
            LocalDate startDate = application.getStartDate();

            if(startDate == null){
                startDate = LocalDate.now();
            }
            if (inputIdentifier.endsWith("_day")) {
                startDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), Integer.parseInt(value));
            }else if (inputIdentifier.endsWith("_month")) {
                startDate = LocalDate.of(startDate.getYear(), Integer.parseInt(value), startDate.getDayOfMonth());
            }else if (inputIdentifier.endsWith("_year")) {
                startDate = LocalDate.of(Integer.parseInt(value), startDate.getMonth(), startDate.getDayOfMonth());
            }
            application.setStartDate(startDate);
            applicationService.save(application);
        } else if(inputIdentifier.startsWith("cost-")) {
            String fieldName = request.getParameter("fieldName");
            if(fieldName != null && value != null) {
                financeFormHandler.handle(fieldName, value);
            }
        } else {
            Long questionId = Long.valueOf(inputIdentifier);
            responseService.save(user.getId(), applicationId, questionId, value);
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", "true");
        return node;
    }

}
