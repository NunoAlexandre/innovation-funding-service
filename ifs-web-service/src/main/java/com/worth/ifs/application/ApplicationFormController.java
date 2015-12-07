package com.worth.ifs.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.view.FinanceFormHandler;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.exception.AutosaveElementException;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.util.JsonStatusResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller will handle all requests that are related to the application form.
 */
@Controller
@RequestMapping("/application-form")
public class ApplicationFormController extends AbstractApplicationController {
    private final Log log = LogFactory.getLog(getClass());

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder, WebRequest webRequest) {
        dataBinder.registerCustomEditor(LocalDate.class, "application.startDate", new LocalDatePropertyEditor(webRequest));
    }

    @Autowired
    CostService costService;

    @RequestMapping("/{applicationId}")
    public String applicationForm(@ModelAttribute("form") ApplicationForm form, Model model, @PathVariable("applicationId") final Long applicationId,
                                  HttpServletRequest request) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        addApplicationAndFinanceDetails(applicationId, user.getId(), Optional.empty(), model, form);
        return "application-form";
    }

    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.GET)
    public String applicationFormWithOpenSection(@Valid @ModelAttribute("form") ApplicationForm form, BindingResult bindingResult, Model model,
                                                 @PathVariable("applicationId") final Long applicationId,
                                                 @PathVariable("sectionId") final Long sectionId,
                                                 HttpServletRequest request) {
        Application app = applicationService.getById(applicationId);
        User user = userAuthenticationService.getAuthenticatedUser(request);
        addApplicationAndFinanceDetails(applicationId, user.getId(), Optional.of(sectionId), model, form);

        form.bindingResult = bindingResult;
        form.objectErrors = bindingResult.getAllErrors();

        return "application-form";
    }

    @RequestMapping(value = "/deletecost/{applicationId}/{sectionId}/{costId}")
    public String deleteCost(Model model, @PathVariable("applicationId") final Long applicationId,
                             @PathVariable("sectionId") final Long sectionId,
                             @PathVariable("costId") final Long costId, HttpServletRequest request) {

        doDeleteCost(costId);
        return "redirect:/application-form/" + applicationId + "/section/" + sectionId;
    }

    @RequestMapping(value = "/deletecost/{applicationId}/{sectionId}/{costId}/{renderQuestionId}", params = "singleFragment=true", produces = "application/json")
    public @ResponseBody JsonStatusResponse deleteCostWithFragmentResponse(Model model, @PathVariable("applicationId") final Long applicationId,
                                          @PathVariable("sectionId") final Long sectionId,
                                          @PathVariable("costId") final Long costId,
                                          @PathVariable("renderQuestionId") final Long renderQuestionId,
                                          HttpServletRequest request) {

        doDeleteCost(costId);
        return JsonStatusResponse.ok();
    }

    private void doDeleteCost(@PathVariable("costId") Long costId) {
        costService.delete(costId);
    }

    @RequestMapping(value = "/addcost/{applicationId}/{sectionId}/{questionId}/{renderQuestionId}", params = "singleFragment=true")
    public String addAnotherWithFragmentResponse(@Valid @ModelAttribute("form") ApplicationForm form, Model model,
                                                 @PathVariable("applicationId") final Long applicationId,
                                                 @PathVariable("sectionId") final Long sectionId,
                                                 @PathVariable("questionId") final Long questionId,
                                                 @PathVariable("renderQuestionId") final Long renderQuestionId,
                                                 HttpServletRequest request) {
        addCost(applicationId, questionId, request);
        return renderSingleQuestionHtml(model, applicationId, sectionId, renderQuestionId, request, form);
    }

    private String renderSingleQuestionHtml(Model model, Long applicationId, Long sectionId, Long renderQuestionId, HttpServletRequest request, ApplicationForm form) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        Application application = addApplicationAndFinanceDetails(applicationId, user.getId(), Optional.of(sectionId), model, form);
        Optional<Section> currentSection = getSection(application.getCompetition().getSections(), Optional.of(sectionId), false);
        Question question = currentSection.get().getQuestions().stream().filter(q -> q.getId().equals(renderQuestionId)).collect(Collectors.toList()).get(0);
        model.addAttribute("question", question);
        return "single-question";
    }

    @RequestMapping(value = "/addcost/{applicationId}/{sectionId}/{questionId}")
    public String addAnother(@ModelAttribute("form") ApplicationForm form, Model model,
                             @PathVariable("applicationId") final Long applicationId,
                             @PathVariable("sectionId") final Long sectionId,
                             @PathVariable("questionId") final Long questionId,
                             HttpServletRequest request) {
        addCost(applicationId, questionId, request);
        return "redirect:/application-form/" + applicationId + "/section/" + sectionId;
    }

    private void addCost(Long applicationId, Long questionId, HttpServletRequest request) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationFinance applicationFinance = financeService.getApplicationFinance(user.getId(), applicationId);
        financeService.addCost(applicationFinance.getId(), questionId);
    }

    /**
     * Get the details of the current application, add this to the model so we can use it in the templates.
     */

//    private Application addApplicationDetails(Long applicationId, Long userId, Long currentSectionId, Model model) {
//        Application application = applicationService.getById(applicationId);
//        model.addAttribute("currentApplication", application);
//        Competition competition = application.getCompetition();
//        model.addAttribute("currentCompetition", competition);
//
//        Organisation userOrganisation = organisationService.getUserOrganisation(application, userId).get();
//
//        addOrganisationDetails(model, application, Optional.of(userOrganisation));
//        addQuestionsDetails(model, application, Optional.of(userOrganisation), userId);
//        addOrganisationFinanceDetails(model, application, userId);
//        addFinanceDetails(model, application);
//        addMappedSectionsDetails(model, application, Optional.of(currentSectionId), Optional.of(userOrganisation), false);
//        addUserDetails(model, application, userId);
//
//        return application;
//    }

    private BindingResult saveApplicationForm(ApplicationForm form, Model model,
                                                        @PathVariable("applicationId") final Long applicationId,
                                                        @PathVariable("sectionId") final Long sectionId,
                                                        HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult) {
        User user = userAuthenticationService.getAuthenticatedUser(request);
        Application application = applicationService.getById(applicationId);
        Competition comp = application.getCompetition();
        List<Section> sections = comp.getSections();

        // get the section that we want, so we can use this on to store the correct questions.
        Section section = sections.stream().filter(x -> x.getId().equals(sectionId)).findFirst().get();

        Map<Long, List<String>> errors = saveQuestionResponses(request, section.getQuestions(), user.getId(), applicationId);
        errors.forEach((k, errorsList) -> {
            errorsList.forEach(e -> {
                bindingResult.rejectValue("formInput["+ k +"]", e, e);
            });
        });

        // save application details if they are in the request
        Map<String, String[]> params = request.getParameterMap();
        params.forEach((key, value) -> log.info("key " + key));

        setApplicationDetails(application, form.getApplication());
        boolean marked = markQuestion(request, params, applicationId, user.getId(), errors);

        applicationService.save(application);
        // if a question is marked as complete, don't show the field saved message.
        if(!marked){
            cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");
        }

        FinanceFormHandler financeFormHandler = new FinanceFormHandler(costService);
        if(financeFormHandler.handle(request)){
            cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");
        }

        addApplicationAndFinanceDetails(applicationId, user.getId(), Optional.of(sectionId), model, form);

        return bindingResult;
    }

    /**
     * This method is for the post request when the users clicks the input[type=submit] button.
     * This is also used when the user clicks the 'mark-as-complete' button or reassigns a question to another user.
     */
    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.POST)
    public String applicationFormSubmit(@Valid @ModelAttribute("form") ApplicationForm form,
                                        BindingResult bindingResult,
                                        Model model,
                                        @PathVariable("applicationId") final Long applicationId,
                                        @PathVariable("sectionId") final Long sectionId,
                                        HttpServletRequest request,
                                        HttpServletResponse response){
        Map<String, String[]> params = request.getParameterMap();

        bindingResult.getAllErrors().forEach((e) -> log.info("Validations on application : " + e.getObjectName() + " v: " + e.getDefaultMessage()));
        bindingResult = saveApplicationForm(form, model, applicationId, sectionId, request, response, bindingResult);
        bindingResult.getAllErrors().forEach((e) -> log.info("Remote validation: " + e.getObjectName() + " v: " + e.getDefaultMessage()));

        if (params.containsKey("assign_question")) {
            assignQuestion(model, applicationId, sectionId, request);
            cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
        }

        form.bindingResult = bindingResult;
        form.objectErrors = bindingResult.getAllErrors();


        if(bindingResult.hasErrors()){
            return "application-form";
        }else{
            // add redirect, to make sure the user cannot resubmit the form by refreshing the page.
            return "redirect:/application-form/"+applicationId + "/section/" + sectionId;
        }
    }

    private boolean markQuestion(HttpServletRequest request, Map<String, String[]> params, Long applicationId, Long userId, Map<Long, List<String>> errors) {
        ProcessRole processRole = processRoleService.findProcessRole(userId, applicationId);
        if (processRole == null) {
            return false;
        }
        boolean success = false;
        if (params.containsKey("mark_as_complete")) {
            Long questionId = Long.valueOf(request.getParameter("mark_as_complete"));

            if(errors.containsKey(questionId) && errors.get(questionId).size() > 0){
                List<String> fieldErrors = errors.get(questionId);
                fieldErrors.add("Please enter valid data before marking a question as complete.");
            }else{
                questionService.markAsComplete(questionId, applicationId, processRole.getId());
                success= true;
            }
        }
        if (params.containsKey("mark_as_incomplete")) {
            Long questionId = Long.valueOf(request.getParameter("mark_as_incomplete"));
            questionService.markAsInComplete(questionId, applicationId, processRole.getId());
            success= true;

        }
        return success;
    }

    private Map<Long, List<String>> saveQuestionResponses(HttpServletRequest request, List<Question> questions, Long userId, Long applicationId) {
        Map<Long, List<String>> errorMap = new HashMap<>();
        questions.forEach(question -> question.getFormInputs().forEach(formInput -> {

            if(request.getParameterMap().containsKey("formInput[" + formInput.getId() + "]")) {
                String value = request.getParameter("formInput[" + formInput.getId() + "]");
                List<String> errors = formInputResponseService.save(userId, applicationId, formInput.getId(), value);
                if (errors.size() != 0) {
                    log.error("save failed. " + question.getId());
                    errorMap.put(question.getId(), new ArrayList<>(errors));
                }
            }
        }));
        return errorMap;
    }

    /**
     * Set the submitted values, if not null. If they are null, then probably the form field was not in the current html form.
     */
    private void setApplicationDetails(Application application, Application updatedApplication) {
        if(updatedApplication == null){
            return;
        }

        if(updatedApplication.getName() != null){
            log.error("setApplicationDetails: "+ updatedApplication.getName());
            application.setName(updatedApplication.getName());
        }
        if(updatedApplication.getStartDate() != null) {
            log.error("setApplicationDetails: "+ updatedApplication.getStartDate());
            application.setStartDate(updatedApplication.getStartDate());
        }
        if(updatedApplication.getDurationInMonths() != null){
            log.error("setApplicationDetails: " + updatedApplication.getDurationInMonths());
            application.setDurationInMonths(updatedApplication.getDurationInMonths());
        }
    }

    /**
     * This method is for supporting ajax saving from the application form.
     */
    @RequestMapping(value = "/saveFormElement", method = RequestMethod.POST)
    public @ResponseBody JsonNode saveFormElement(@RequestParam("formInputId") String inputIdentifier,
                             @RequestParam("value") String value,
                             @RequestParam("applicationId") Long applicationId,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        List<String> errors = new ArrayList<>();
        try {
            String fieldName = request.getParameter("fieldName");

            User user = userAuthenticationService.getAuthenticatedUser(request);
            log.debug("INPUT ID: " + inputIdentifier);
            if (fieldName.startsWith("application.")) {
                errors = this.saveApplicationDetails(applicationId, fieldName, value, errors);
            } else if (inputIdentifier.startsWith("cost-") || fieldName.startsWith("cost-")) {
                FinanceFormHandler financeFormHandler = new FinanceFormHandler(costService);
                if (fieldName != null && value != null) {
                    String cleanedFieldName = fieldName;
                    if (fieldName.startsWith("cost-")) {
                        cleanedFieldName = fieldName.replace("cost-", "");
                    }
                    log.debug("FIELDNAME: " + cleanedFieldName + " VALUE: " + value);
                    financeFormHandler.storeField(cleanedFieldName, value);
                }
            } else {
                Long formInputId = Long.valueOf(inputIdentifier);
                errors = formInputResponseService.save(user.getId(), applicationId, formInputId, value);
            }

            if (errors.size() > 0) {
                return this.createJsonObjectNode(false, errors);
            } else {
                return this.createJsonObjectNode(true, null);
            }
        } catch (Exception e) {
            log.error("Exception on autosave: ");
            log.error(e.getMessage());
            e.printStackTrace();
            AutosaveElementException ex = new AutosaveElementException(inputIdentifier, value, applicationId, e);

            errors.add(ex.getErrorMessage());
            return this.createJsonObjectNode(false, errors);
        }
    }

    private ObjectNode createJsonObjectNode(boolean success, List<String> errors) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", (success ? "true" : "false"));
        if(!success){
            ArrayNode errorsNode = mapper.createArrayNode();
            errors.stream().forEach(e -> errorsNode.add(e));
            node.set("validation_errors", errorsNode);
        }
        return node;
    }

    private List<String> saveApplicationDetails(Long applicationId, String fieldName, String value, List<String> errors) {
        Application application = applicationService.getById(applicationId);

        if (fieldName.equals("application.name")) {
            value = value.trim();
            if(StringUtils.isEmpty(value)){
                errors.add("Please enter the full title of the project.");
            }else{

                application.setName(value);
                applicationService.save(application);
            }
        } else if (fieldName.startsWith("application.durationInMonths")) {
            Long durationInMonth = Long.valueOf(value);
            if(durationInMonth == null || durationInMonth < 1L){
                errors.add("Please enter a valid duration.");
            }else{
                application.setDurationInMonths(durationInMonth);
                applicationService.save(application);
            }
        } else if (fieldName.startsWith("application.startDate")) {
            errors = this.saveApplicationStartDate(application, fieldName, value, errors);

        }
        return errors;
    }

    private List<String> saveApplicationStartDate(Application application, String fieldName, String value, List<String> errors) {
        LocalDate startDate = application.getStartDate();

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        try{
            if (fieldName.endsWith(".dayOfMonth")) {
                startDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), Integer.parseInt(value));
            } else if (fieldName.endsWith(".monthValue")) {
                startDate = LocalDate.of(startDate.getYear(), Integer.parseInt(value), startDate.getDayOfMonth());
            } else if (fieldName.endsWith(".year")) {
                startDate = LocalDate.of(Integer.parseInt(value), startDate.getMonth(), startDate.getDayOfMonth());
            }
            if(startDate.isBefore(LocalDate.now())){
                errors.add("Please enter a future date.");
            }

            application.setStartDate(startDate);
            applicationService.save(application);
        }catch(DateTimeException e){
            errors.add("Please enter a valid date.");
        }catch(NumberFormatException e){
            errors.add("Please enter a valid date.");
        }
        return errors;
    }

    public void assignQuestion(Model model,
                               @PathVariable("applicationId") final Long applicationId,
                               @PathVariable("sectionId") final Long sectionId,
                               HttpServletRequest request) {
        assignQuestion(request, applicationId);
    }

    protected Application addApplicationAndFinanceDetails(Long applicationId, Long userId, Optional<Long> currentSectionId, Model model, ApplicationForm form) {
        Application application = super.addApplicationDetails(applicationId, userId, currentSectionId, model, true, form);
        form.application = application;
        addOrganisationFinanceDetails(model, application, userId, form);
        addFinanceDetails(model, application);
        return application;
    }
}