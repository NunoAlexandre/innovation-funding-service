package com.worth.ifs.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.finance.service.CostService;
import com.worth.ifs.application.finance.service.FinanceService;
import com.worth.ifs.application.form.ApplicationForm;
import com.worth.ifs.application.form.validation.ApplicationStartDateValidator;
import com.worth.ifs.application.model.OpenFinanceSectionSectionModelPopulator;
import com.worth.ifs.application.model.OpenSectionModelPopulator;
import com.worth.ifs.application.model.QuestionModelPopulator;
import com.worth.ifs.application.resource.*;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.exception.AutosaveElementException;
import com.worth.ifs.exception.BigDecimalNumberFormatException;
import com.worth.ifs.exception.IntegerNumberFormatException;
import com.worth.ifs.exception.UnableToReadUploadedFile;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.service.FormInputService;
import com.worth.ifs.model.OrganisationDetailsModelPopulator;
import com.worth.ifs.profiling.ProfileExecution;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.util.AjaxResult;
import com.worth.ifs.util.MessageUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.multipart.support.StringMultipartFileEditor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.worth.ifs.application.resource.SectionType.FINANCE;
import static com.worth.ifs.commons.error.Error.fieldError;
import static com.worth.ifs.commons.rest.ValidationMessages.collectValidationMessages;
import static com.worth.ifs.commons.rest.ValidationMessages.fromBindingResult;
import static com.worth.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This controller will handle all requests that are related to the application form.
 */
@Controller
@RequestMapping(ApplicationFormController.APPLICATION_BASE_URL+"{applicationId}/form")
public class ApplicationFormController extends AbstractApplicationController {


    private static final Log LOG = LogFactory.getLog(ApplicationFormController.class);

    @Autowired
    private CostService costService;

    @Autowired
    private FormInputService formInputService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    MessageSource messageSource;

    @Autowired
    private QuestionModelPopulator questionModelPopulator;

    @Autowired
    private OpenSectionModelPopulator openSectionModel;

    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;

    @Autowired
    private OpenFinanceSectionSectionModelPopulator openFinanceSectionModel;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder, WebRequest webRequest) {
        dataBinder.registerCustomEditor(String.class, new StringMultipartFileEditor());
    }

    @ProfileExecution
    @RequestMapping(value = {QUESTION_URL + "{"+QUESTION_ID+"}", QUESTION_URL + "edit/{"+QUESTION_ID+"}"}, method = RequestMethod.GET)
    public String showQuestion(@ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form,
                               @SuppressWarnings("unused") BindingResult bindingResult,
                               @SuppressWarnings("unused") ValidationHandler validationHandler,
                               Model model,
                               @PathVariable(APPLICATION_ID) final Long applicationId,
                               @PathVariable(QUESTION_ID) final Long questionId,
                               HttpServletRequest request) {
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        questionModelPopulator.populateModel(questionId, applicationId, user, model, form);
        organisationDetailsModelPopulator.populateModel(model, applicationId);
        return APPLICATION_FORM;
    }

    @ProfileExecution
    @RequestMapping(value = QUESTION_URL + "{"+QUESTION_ID+"}/forminput/{formInputId}/download", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadApplicationFinanceFile(
                                @PathVariable(APPLICATION_ID) final Long applicationId,
                                @PathVariable("formInputId") final Long formInputId,
                                HttpServletRequest request) {
        final UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ProcessRoleResource processRole = processRoleService.findProcessRole(user.getId(), applicationId);
        final ByteArrayResource resource = formInputResponseService.getFile(formInputId, applicationId, processRole.getId()).getSuccessObjectOrThrowException();
        final FormInputResponseFileEntryResource fileDetails = formInputResponseService.getFileDetails(formInputId, applicationId, processRole.getId()).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails.getFileEntryResource());
    }

    @RequestMapping(value = "/{applicationFinanceId}/finance-download", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadApplicationFinanceFile(
            @PathVariable("applicationFinanceId") final Long applicationFinanceId) {

        final ByteArrayResource resource = financeService.getFinanceDocumentByApplicationFinance(applicationFinanceId).getSuccessObjectOrThrowException();
        final FileEntryResource fileDetails = financeService.getFinanceEntryByApplicationFinanceId(applicationFinanceId).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails);
    }

    @ProfileExecution
    @RequestMapping(value = SECTION_URL + "{sectionId}", method = RequestMethod.GET)
    public String applicationFormWithOpenSection(@Valid @ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form, BindingResult bindingResult, Model model,
                                                 @PathVariable(APPLICATION_ID) final Long applicationId,
                                                 @PathVariable("sectionId") final Long sectionId,
                                                 HttpServletRequest request) {
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationResource application = applicationService.getById(applicationId);
        List<SectionResource> allSections = sectionService.getAllByCompetitionId(application.getCompetition());
        SectionResource section = simpleFilter(allSections, s -> sectionId.equals(s.getId())).get(0);

        if (FINANCE.equals(section.getType())) {
            openFinanceSectionModel.populateModel(form, model, application, section, user, bindingResult, allSections);
        } else {
            openSectionModel.populateModel(form, model, application, section, user, bindingResult, allSections);
        }

        return APPLICATION_FORM;
    }

    private void addFormAttributes(ApplicationResource application,
                                   CompetitionResource competition,
                                   Optional<SectionResource> section,
                                   UserResource user, Model model,
                                   ApplicationForm form, Optional<QuestionResource> question,
                                   Optional<List<FormInputResource>> formInputs,
                                   List<ProcessRoleResource> userApplicationRoles){
        addApplicationDetails(application, competition, user.getId(), section, question.map(q -> q.getId()), model, form, userApplicationRoles);
        organisationDetailsModelPopulator.populateModel(model, application.getId(), userApplicationRoles);
        addNavigation(question.orElse(null), application.getId(), model);
        Map<Long, List<FormInputResource>> questionFormInputs = new HashMap<>();

        if(question.isPresent()) {
            questionFormInputs.put(question.get().getId(), formInputs.orElse(null));
        }
        model.addAttribute("currentQuestion", question.orElse(null));
        model.addAttribute("questionFormInputs", questionFormInputs);
        model.addAttribute("currentUser", user);
        model.addAttribute("form", form);
        if(question.isPresent()) {
            model.addAttribute("title", question.get().getShortName());
        }
    }

    @ProfileExecution
    @RequestMapping(value = {QUESTION_URL + "{"+QUESTION_ID+"}", QUESTION_URL + "edit/{"+QUESTION_ID+"}"}, method = RequestMethod.POST)
    public String questionFormSubmit(@Valid @ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form,
                                     @SuppressWarnings("unused") BindingResult bindingResult,
                                     @SuppressWarnings("unused") ValidationHandler validationHandler,
                                     Model model,
                                     @PathVariable(APPLICATION_ID) final Long applicationId,
                                     @PathVariable(QUESTION_ID) final Long questionId,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);

        Map<String, String[]> params = request.getParameterMap();

        // Check if the request is to just open edit view or to save
        if(params.containsKey(EDIT_QUESTION)){
            ProcessRoleResource processRole = processRoleService.findProcessRole(user.getId(), applicationId);
            if (processRole != null) {
                questionService.markAsInComplete(questionId, applicationId, processRole.getId());
            } else {
                LOG.error("Not able to find process role for user " + user.getName() + " for application id " + applicationId);
            }
            return showQuestion(form, bindingResult, validationHandler, model, applicationId, questionId, request);
        } else {
            QuestionResource question = questionService.getById(questionId);
            SectionResource section = sectionService.getSectionByQuestionId(questionId);
            ApplicationResource application = applicationService.getById(applicationId);
            CompetitionResource competition = competitionService.getById(application.getCompetition());
            List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());
            List<FormInputResource> formInputs = formInputService.findApplicationInputsByQuestion(questionId);


            if (params.containsKey(ASSIGN_QUESTION_PARAM)) {
                assignQuestion(applicationId, request);
                cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
            }

            ValidationMessages errors = new ValidationMessages();

            if (isAllowedToUpdateQuestion(questionId, applicationId, user.getId()) || isMarkQuestionRequest(params)) {
                /* Start save action */
                errors.addAll(saveApplicationForm(application, competition, form, applicationId, null, question, request, response));
            }

            model.addAttribute("form", form);

            /* End save action */

            if (errors.hasErrors()) {

                validationHandler.addAnyErrors(errors);

                this.addFormAttributes(application, competition, Optional.ofNullable(section), user, model, form,
                        Optional.ofNullable(question), Optional.ofNullable(formInputs), userApplicationRoles);
                model.addAttribute("currentUser", user);
                addUserDetails(model, application, user.getId());
                addNavigation(question, applicationId, model);
                return APPLICATION_FORM;
            } else {
                return getRedirectUrl(request, applicationId);
            }
        }
    }

    private Boolean isAllowedToUpdateQuestion(Long questionId, Long applicationId, Long userId) {
        List<QuestionStatusResource> questionStatuses = questionService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId);
        return questionStatuses.isEmpty() || questionStatuses.stream()
                .anyMatch(questionStatusResource -> (
                        questionStatusResource.getAssignee() == null || questionStatusResource.getAssigneeUserId().equals(userId))
                        && (questionStatusResource.getMarkedAsComplete() == null || !questionStatusResource.getMarkedAsComplete()));
    }

    private BindingResult removeDuplicateFieldErrors(BindingResult bindingResult) {
        BindingResult br = new BeanPropertyBindingResult(this, "form");
        bindingResult.getFieldErrors().stream().distinct()
                .filter(e -> {
                    for (FieldError fieldError : br.getFieldErrors(e.getField())) {
                        if(fieldError.getDefaultMessage().equals(e.getDefaultMessage())){
                            return false;
                        }else{
                            return true;
                        }
                    }
                    return true;
                })
                .forEach(e -> br.addError(e));
        bindingResult.getGlobalErrors().stream().forEach(e -> br.addError(e));
        bindingResult = br;
        return bindingResult;
    }

    private String getRedirectUrl(HttpServletRequest request, Long applicationId) {
        if (request.getParameter("submit-section") == null
                && (request.getParameter(ASSIGN_QUESTION_PARAM) != null ||
                request.getParameter(MARK_AS_INCOMPLETE) != null ||
                request.getParameter(MARK_SECTION_AS_INCOMPLETE) != null ||
                request.getParameter(ADD_COST) != null ||
                request.getParameter(REMOVE_COST) != null ||
                request.getParameter(MARK_AS_COMPLETE) != null ||
                request.getParameter(REMOVE_UPLOADED_FILE) != null ||
                request.getParameter(UPLOAD_FILE) != null ||
                request.getParameter(EDIT_QUESTION) != null)) {
            // user did a action, just display the same page.
            LOG.debug("redirect: " + request.getRequestURI());
            return "redirect:" + request.getRequestURI();
        } else {
            // add redirect, to make sure the user cannot resubmit the form by refreshing the page.
            LOG.debug("default redirect: ");
            return "redirect:" + APPLICATION_BASE_URL + applicationId;
        }
    }

    @RequestMapping(value = "/add_cost/{"+QUESTION_ID+"}")
    public String addCostRow(@ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form,
                             BindingResult bindingResult,
                             Model model,
                             @PathVariable(APPLICATION_ID) final Long applicationId,
                             @PathVariable(QUESTION_ID) final Long questionId,
                             HttpServletRequest request) {
        CostItem costItem = addCost(applicationId, questionId, request);
        String type = costItem.getCostType().getType();
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);

        Set<Long> markedAsComplete = new TreeSet<>();
        model.addAttribute("markedAsComplete", markedAsComplete);
        String organisationType = organisationService.getOrganisationType(user.getId(), applicationId);
        financeHandler.getFinanceModelManager(organisationType).addCost(model, costItem, applicationId, user.getId(), questionId, type);

        form.setBindingResult(bindingResult);
        return String.format("finance/finance :: %s_row", type);
    }

    @RequestMapping(value = "/remove_cost/{costId}")
    public @ResponseBody String removeCostRow(@PathVariable("costId") final Long costId) throws JsonProcessingException {
        costService.delete(costId);
        AjaxResult ajaxResult = new AjaxResult(HttpStatus.OK, "true");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ajaxResult);
    }

    private CostItem addCost(Long applicationId, Long questionId, HttpServletRequest request) {
        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        String organisationType = organisationService.getOrganisationType(user.getId(), applicationId);
        return financeHandler.getFinanceFormHandler(organisationType).addCost(applicationId, user.getId(), questionId);
    }

    private ValidationMessages saveApplicationForm(ApplicationResource application,
                                      CompetitionResource competition,
                                      ApplicationForm form,
                                      Long applicationId, Long sectionId, QuestionResource question,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ProcessRoleResource processRole = processRoleService.findProcessRole(user.getId(), applicationId);

        // Check if action is mark as complete.  Check empty values if so, ignore otherwise. (INFUND-1222)
        Map<String, String[]> params = request.getParameterMap();

        logSaveApplicationDetails(params);

        boolean ignoreEmpty = (!params.containsKey(MARK_AS_COMPLETE)) && (!params.containsKey(MARK_SECTION_AS_COMPLETE));

//        Map<Long, List<String>> errors = new HashMap<>();

        ValidationMessages errors = new ValidationMessages();

        // Prevent saving question when it's a unmark question request (INFUND-2936)
        if(!isMarkQuestionAsInCompleteRequest(params)) {
            if (question != null) {
                errors.addAll(saveQuestionResponses(request, singletonList(question), user.getId(), processRole.getId(), application.getId(), ignoreEmpty));
            } else {
                SectionResource selectedSection = getSelectedSection(competition.getSections(), sectionId);
                List<QuestionResource> questions = simpleMap(selectedSection.getQuestions(), questionService::getById);
                errors.addAll(saveQuestionResponses(request, questions, user.getId(), processRole.getId(), application.getId(), ignoreEmpty));
            }
        }

        errors.addAll(validationApplicationStartDate(request));

        setApplicationDetails(application, form.getApplication());

        if(userIsLeadApplicant(application, user.getId())) {
            applicationService.save(application);
        }

        if(!isMarkSectionAsIncompleteRequest(params)) {
            String organisationType = organisationService.getOrganisationType(user.getId(), applicationId);
            errors.addAll(financeHandler.getFinanceFormHandler(organisationType).update(request, user.getId(), applicationId));
        }

        if(isMarkQuestionRequest(params)) {
            errors.addAll(handleApplicationDetailsMarkCompletedRequest(application, request, response, processRole, errors));

        } else if(isMarkSectionRequest(params)){
            errors.addAll(handleMarkSectionRequest(application, competition, sectionId, request, response, processRole, errors));
        }

        cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");

        return errors;
    }

    private void logSaveApplicationDetails(Map<String, String[]> params) {
        params.forEach((key, value) -> LOG.debug(String.format("saveApplicationForm key %s => value %s", key, value[0])));
    }

    private ValidationMessages validationApplicationStartDate(HttpServletRequest request) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "");
        new ApplicationStartDateValidator().validate(request, bindingResult);
        return fromBindingResult(bindingResult);
    }

    private ValidationMessages handleApplicationDetailsMarkCompletedRequest(ApplicationResource application, HttpServletRequest request, HttpServletResponse response, ProcessRoleResource processRole, ValidationMessages errorsSoFar) {

        if (errorsSoFar.hasErrors()) {
            return new ValidationMessages(fieldError("formInput[application]", "application.validation.MarkAsCompleteFailed"));
        } else {

            ValidationMessages messages = new ValidationMessages();

            List<ValidationMessages> applicationMessages = markApplicationQuestions(application, processRole.getId(), request, response, errorsSoFar);

            if (collectValidationMessages(applicationMessages).hasErrors()) {
                messages.addError(fieldError("formInput[application]", "application.validation.MarkAsCompleteFailed"));
                messages.addAll(handleApplicationDetailsValidationMessages(applicationMessages, application));
            }

            return messages;
        }
    }

    private ValidationMessages handleApplicationDetailsValidationMessages(List<ValidationMessages> applicationMessages, ApplicationResource application) {

        ValidationMessages toFieldErrors = new ValidationMessages();

        applicationMessages.forEach(validationMessage ->
            validationMessage.getErrors().stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.hasText(e.getErrorMessage()))
                .forEach(e -> {
                            e.toString();
                            if (validationMessage.getObjectName().equals("target")) {
                                if (StringUtils.hasText(e.getErrorKey())) {
                                    toFieldErrors.addError(fieldError("formInput[application." + validationMessage.getObjectId() + "-" + e.getFieldName() + "]", e.getErrorMessage()));
                                    if (e.getErrorKey().equals("durationInMonths")) {
                                        application.setDurationInMonths(null);
                                    }
                                }
                            }
                        }));

        return toFieldErrors;
    }

    private ValidationMessages handleMarkSectionRequest(ApplicationResource application, CompetitionResource competition, Long sectionId, HttpServletRequest request, HttpServletResponse response, ProcessRoleResource processRole, ValidationMessages errorsSoFar) {

        ValidationMessages messages = new ValidationMessages();

        if (errorsSoFar.hasErrors()) {
            messages.addError(fieldError("formInput[cost]", "application.validation.MarkAsCompleteFailed"));
        } else {
            SectionResource selectedSection = getSelectedSection(competition.getSections(), sectionId);
            List<ValidationMessages> financeErrorsMark = markAllQuestionsInSection(application, selectedSection, processRole.getId(), request);

            if (collectValidationMessages(financeErrorsMark).hasErrors()) {
                messages.addError(fieldError("formInput[cost]", "application.validation.MarkAsCompleteFailed"));
                messages.addAll(handleMarkSectionValidationMessages(financeErrorsMark));
            }
        }

        return messages;
    }

    private ValidationMessages handleMarkSectionValidationMessages(List<ValidationMessages> financeErrorsMark) {

        ValidationMessages toFieldErrors = new ValidationMessages();

        financeErrorsMark.forEach(validationMessage ->
            validationMessage.getErrors().stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.hasText(e.getErrorMessage()))
                .forEach(e -> {
                    if (validationMessage.getObjectName().equals("costItem")) {
                        if (StringUtils.hasText(e.getErrorKey())) {
                            toFieldErrors.addError(fieldError("formInput[cost-" + validationMessage.getObjectId() + "-" + e.getFieldName() + "]", e.getErrorMessage()));
                        } else {
                            toFieldErrors.addError(fieldError("formInput[cost-" + validationMessage.getObjectId() + "]", e.getErrorMessage()));
                        }
                    } else {
                        toFieldErrors.addError(fieldError("formInput[" + validationMessage.getObjectId() + "]", e.getErrorMessage()));
                    }
                })
        );

        return toFieldErrors;
    }

    private List<ValidationMessages> markAllQuestionsInSection(ApplicationResource application,
                                                               SectionResource selectedSection,
                                                               Long processRoleId,
                                                               HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        String action = params.containsKey(MARK_SECTION_AS_COMPLETE) ? MARK_AS_COMPLETE : MARK_AS_INCOMPLETE;

        if(action.equals(MARK_AS_COMPLETE)){
            return sectionService.markAsComplete(selectedSection.getId(), application.getId(), processRoleId);
        }else{
            sectionService.markAsInComplete(selectedSection.getId(), application.getId(), processRoleId);
        }

        return emptyList();
    }

    private boolean isMarkQuestionRequest(@NotNull Map<String, String[]> params){
        return params.containsKey(MARK_AS_COMPLETE) || params.containsKey(MARK_AS_INCOMPLETE);
    }

    private boolean isMarkQuestionAsInCompleteRequest(@NotNull Map<String, String[]> params){
        return params.containsKey(MARK_AS_INCOMPLETE);
    }

    private boolean isMarkSectionRequest(@NotNull Map<String, String[]> params){
        return params.containsKey(MARK_SECTION_AS_COMPLETE) || params.containsKey(MARK_SECTION_AS_INCOMPLETE);
    }

    private boolean isMarkSectionAsIncompleteRequest(@NotNull Map<String, String[]> params){
        return params.containsKey(MARK_SECTION_AS_INCOMPLETE);
    }

    private SectionResource getSelectedSection(List<Long> sectionIds, Long sectionId) {
        return sectionIds.stream()
                .map(sectionService::getById)
                .filter(x -> x.getId().equals(sectionId))
                .findFirst()
                .get();
    }

    private List<ValidationMessages> markApplicationQuestions(ApplicationResource application, Long processRoleId, HttpServletRequest request, HttpServletResponse response, ValidationMessages errorsSoFar) {

        if (processRoleId == null) {
            return emptyList();
        }

        Map<String, String[]> params = request.getParameterMap();

        if (params.containsKey(MARK_AS_COMPLETE)) {

            Long questionId = Long.valueOf(request.getParameter(MARK_AS_COMPLETE));

            List<ValidationMessages> markAsCompleteErrors = questionService.markAsComplete(questionId, application.getId(), processRoleId);

            if (collectValidationMessages(markAsCompleteErrors).hasErrors()) {
                questionService.markAsInComplete(questionId, application.getId(), processRoleId);
            }
            else {
                cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");
            }

            if (errorsSoFar.hasFieldErrors(questionId + "")) {
                markAsCompleteErrors.add(new ValidationMessages(fieldError(questionId + "", "Please enter valid data before marking a question as complete.")));
            }

            return markAsCompleteErrors;

        } else if (params.containsKey(MARK_AS_INCOMPLETE)) {
            Long questionId = Long.valueOf(request.getParameter(MARK_AS_INCOMPLETE));
            questionService.markAsInComplete(questionId, application.getId(), processRoleId);
        }

        return emptyList();
    }

    /**
     * This method is for the post request when the users clicks the input[type=submit] button.
     * This is also used when the user clicks the 'mark-as-complete' button or reassigns a question to another user.
     */
    @ProfileExecution
    @RequestMapping(value = SECTION_URL + "{sectionId}", method = RequestMethod.POST)
    public String applicationFormSubmit(@Valid @ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form,
                                        BindingResult bindingResult, ValidationHandler validationHandler,
                                        Model model,
                                        @PathVariable(APPLICATION_ID) final Long applicationId,
                                        @PathVariable("sectionId") final Long sectionId,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        logSaveApplicationBindingErrors(validationHandler);

        UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());

        Map<String, String[]> params = request.getParameterMap();

        ValidationMessages saveApplicationErrors = saveApplicationForm(application, competition, form, applicationId, sectionId, null, request, response);
        logSaveApplicationErrors(bindingResult);

        if (params.containsKey(ASSIGN_QUESTION_PARAM)) {
            assignQuestion(applicationId, request);
            cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
        }

        model.addAttribute("form", form);

        if(saveApplicationErrors.hasErrors()){

            validationHandler.addAnyErrors(saveApplicationErrors);

            SectionResource section = sectionService.getById(sectionId);
            addApplicationAndSectionsInternalWithOrgDetails(application, competition, user.getId(), Optional.ofNullable(section), model, form);
            addOrganisationAndUserFinanceDetails(competition.getId(), application.getId(), user, model, form);
            addNavigation(section, applicationId, model);
            List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());
            Optional<OrganisationResource> userOrganisation = getUserOrganisation(user.getId(), userApplicationRoles);
            addCompletedDetails(model, application, userOrganisation);
            return APPLICATION_FORM;
        } else {
            return getRedirectUrl(request, applicationId);
        }
    }

    private void logSaveApplicationBindingErrors(ValidationHandler validationHandler) {
        if(LOG.isDebugEnabled())
            validationHandler.getAllErrors().forEach(e -> LOG.debug("Validations on application : " + e.getObjectName() + " v: " + e.getDefaultMessage()));
    }

    private void logSaveApplicationErrors(BindingResult bindingResult) {
        if(LOG.isDebugEnabled()){
            bindingResult.getFieldErrors().forEach(e -> LOG.debug("Remote validation field: " + e.getObjectName() + " v: " + e.getField() + " v: " + e.getDefaultMessage()));
            bindingResult.getGlobalErrors().forEach(e -> LOG.debug("Remote validation global: " + e.getObjectName()+ " v: " + e.getCode() + " v: " + e.getDefaultMessage()));
        }
    }


    private ValidationMessages saveQuestionResponses(HttpServletRequest request,
                                                          List<QuestionResource> questions,
                                                          Long userId,
                                                          Long processRoleId,
                                                          Long applicationId,
                                                          boolean ignoreEmpty) {
        final Map<String, String[]> params = request.getParameterMap();

        Map<Long, List<String>> errorMap = new HashMap<>();

        errorMap.putAll(saveNonFileUploadQuestions(questions, params, request, userId, applicationId, ignoreEmpty));

        errorMap.putAll(saveFileUploadQuestionsIfAny(questions, params, request, applicationId, processRoleId));

        ValidationMessages toFieldErrors = new ValidationMessages();
        errorMap.forEach((k, errorsList) -> errorsList.forEach(e -> toFieldErrors.addError(fieldError("formInput[" + k + "]", e, e))));
        return toFieldErrors;
    }

    // TODO DW - ideally this would return a ValidationMessages with fieldErrors for the given Long
    private Map<Long, List<String>> saveNonFileUploadQuestions(List<QuestionResource> questions,
                                                               Map<String, String[]> params,
                                                               HttpServletRequest request,
                                                               Long userId,
                                                               Long applicationId,
                                                               boolean ignoreEmpty) {

        Map<Long, List<String>> errorMap = new HashMap<>();
        questions.stream()
                .forEach(question ->
                        {
                            List<FormInputResource> formInputs = formInputService.findApplicationInputsByQuestion(question.getId());
                            formInputs
                                    .stream()
                                    .filter(formInput1 -> !"fileupload".equals(formInput1.getFormInputTypeTitle()))
                                    .forEach(formInput -> {
                                                if (params.containsKey("formInput[" + formInput.getId() + "]")) {
                                                    String value = request.getParameter("formInput[" + formInput.getId() + "]");
                                                    ValidationMessages errors = formInputResponseService.save(userId, applicationId, formInput.getId(), value, ignoreEmpty);
                                                    if (errors.hasErrors()) {
                                                        LOG.info("save failed. " + question.getId());
                                                        errorMap.put(question.getId(), simpleMap(errors.getErrors(), Error::getErrorMessage));
                                                    }
                                                }
                                            }
                                    );
                        }
                );
        return errorMap;
    }

    // TODO DW - ideally this would return a ValidationMessages with fieldErrors for the given Long
    private Map<Long, List<String>> saveFileUploadQuestionsIfAny(List<QuestionResource> questions,
                                                                 final Map<String, String[]> params,
                                                                 HttpServletRequest request,
                                                                 Long applicationId,
                                                                 Long processRoleId) {
        Map<Long, List<String>> errorMap = new HashMap<>();
        questions.stream()
                .forEach(question -> {
                    List<FormInputResource> formInputs = formInputService.findApplicationInputsByQuestion(question.getId());
                    formInputs
                            .stream()
                            .filter(formInput1 -> "fileupload".equals(formInput1.getFormInputTypeTitle()) && request instanceof StandardMultipartHttpServletRequest)
                            .forEach(formInput -> processFormInput(formInput.getId(), params, applicationId, processRoleId, request, errorMap));
                });
        return errorMap;
    }

    private void processFormInput(Long formInputId, Map<String, String[]> params, Long applicationId, Long processRoleId, HttpServletRequest request, Map<Long, List<String>> errorMap){
        if (params.containsKey(REMOVE_UPLOADED_FILE)) {
            formInputResponseService.removeFile(formInputId, applicationId, processRoleId).getSuccessObjectOrThrowException();
        } else {
            final Map<String, MultipartFile> fileMap = ((StandardMultipartHttpServletRequest) request).getFileMap();
            final MultipartFile file = fileMap.get("formInput[" + formInputId + "]");
            if (file != null && !file.isEmpty()) {
                try {
                    RestResult<FileEntryResource> result = formInputResponseService.createFile(formInputId,
                            applicationId,
                            processRoleId,
                            file.getContentType(),
                            file.getSize(),
                            file.getOriginalFilename(),
                            file.getBytes());
                    if (result.isFailure()) {
                        errorMap.put(formInputId,
                                result.getFailure().getErrors().stream()
                                        .map(e -> MessageUtil.getFromMessageBundle(messageSource, e.getErrorKey(), "Unknown error on file upload", request.getLocale())).collect(Collectors.toList()));
                    }
                } catch (IOException e) {
                	LOG.error(e);
                    throw new UnableToReadUploadedFile();
                }
            }
        }
    }

    /**
     * Set the submitted values, if not null. If they are null, then probably the form field was not in the current html form.
     * @param application
     * @param updatedApplication
     */
    private void setApplicationDetails(ApplicationResource application, ApplicationResource updatedApplication) {
        if (updatedApplication == null) {
            return;
        }

        if (updatedApplication.getName() != null) {
            LOG.debug("setApplicationDetails: " + updatedApplication.getName());
            application.setName(updatedApplication.getName());
        }

        if (updatedApplication.getStartDate() != null) {
            LOG.debug("setApplicationDetails date 123: " + updatedApplication.getStartDate().toString());
            if (updatedApplication.getStartDate().isEqual(LocalDate.MIN)
                    || updatedApplication.getStartDate().isBefore(LocalDate.now())) {
                // user submitted a empty date field or date before today
                application.setStartDate(null);
            }else{
                application.setStartDate(updatedApplication.getStartDate());
            }
        }else{
            application.setStartDate(null);
        }
        if (updatedApplication.getDurationInMonths() != null) {
            LOG.debug("setApplicationDetails: " + updatedApplication.getDurationInMonths());
            application.setDurationInMonths(updatedApplication.getDurationInMonths());
        }
        else {
            application.setDurationInMonths(null);
        }
    }

    /**
     * This method is for supporting ajax saving from the application form.
     */
    @ProfileExecution
    @RequestMapping(value = "/saveFormElement", method = RequestMethod.POST)
    public @ResponseBody JsonNode saveFormElement(@RequestParam("formInputId") String inputIdentifier,
                                                  @RequestParam("value") String value,
                                                  @PathVariable(APPLICATION_ID) Long applicationId,
                                                  HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        try {
            String fieldName = request.getParameter("fieldName");
            LOG.info(String.format("saveFormElement: %s / %s", fieldName, value));

            UserResource user = userAuthenticationService.getAuthenticatedUser(request);
            errors = storeField(applicationId, user.getId(), fieldName, inputIdentifier, value);

            if (!errors.isEmpty()) {
                return this.createJsonObjectNode(false, errors);
            } else {
                return this.createJsonObjectNode(true, null);
            }
        } catch (Exception e) {
            AutosaveElementException ex = new AutosaveElementException(inputIdentifier, value, applicationId, e);
            handleAutosaveException(errors, e, ex);
            return this.createJsonObjectNode(false, errors);
        }
    }

    private void handleAutosaveException(List<String> errors, Exception e, AutosaveElementException ex) {
        List<Object> args = new ArrayList<>();
        args.add(ex.getErrorMessage());
        if(e.getClass().equals(IntegerNumberFormatException.class) || e.getClass().equals(BigDecimalNumberFormatException.class)){
            errors.add(messageSource.getMessage(e.getMessage(), args.toArray(), Locale.UK));
        }else{
            LOG.error("Got a exception on autosave : "+ e.getMessage());
            LOG.debug("Autosave exception: ", e);
            errors.add(ex.getErrorMessage());
        }
    }

    // TODO DW - ideally this would return a ValidationMessages with global errors in it
    private List<String> storeField(Long applicationId, Long userId, String fieldName, String inputIdentifier, String value) {

        String organisationType = organisationService.getOrganisationType(userId, applicationId);

        if (fieldName.startsWith("application.")) {
            return this.saveApplicationDetails(applicationId, fieldName, value);
        } else if (inputIdentifier.startsWith("financePosition-") || fieldName.startsWith("financePosition-")) {
            financeHandler.getFinanceFormHandler(organisationType).updateFinancePosition(userId, applicationId, fieldName, value);
            return emptyList();
        } else if (inputIdentifier.startsWith("cost-") || fieldName.startsWith("cost-")) {
            ValidationMessages validationMessages = financeHandler.getFinanceFormHandler(organisationType).storeCost(userId, applicationId, fieldName, value);
            if(validationMessages == null || validationMessages.getErrors() == null || validationMessages.getErrors().isEmpty()){
                LOG.debug("no errors");
                return emptyList();
            } else {
                String[] fieldNameParts = fieldName.split("-");
                // fieldname = other_costs-description-34-219
                return validationMessages.getErrors()
                        .stream()
                        .peek(e -> LOG.debug(String.format("Compare: %s => %s ", fieldName.toLowerCase(), e.getFieldName().toLowerCase())))
                        .filter(e -> fieldNameParts[1].toLowerCase().contains(e.getFieldName().toLowerCase())) // filter out the messages that are related to other fields.
                        .map(e -> e.getErrorMessage())
                        .collect(Collectors.toList());
            }
        } else {
            Long formInputId = Long.valueOf(inputIdentifier);
            ValidationMessages saveErrors = formInputResponseService.save(userId, applicationId, formInputId, value, false);
            return simpleMap(saveErrors.getErrors(), Error::getErrorMessage);
        }
    }

    private ObjectNode createJsonObjectNode(boolean success, List<String> errors) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", success ? "true" : "false");
        if (!success) {
            ArrayNode errorsNode = mapper.createArrayNode();
            errors.stream().forEach(errorsNode::add);
            node.set("validation_errors", errorsNode);
        }
        return node;
    }

    private List<String> saveApplicationDetails(Long applicationId, String fieldName, String value) {

        List<String> errors = new ArrayList<>();
        ApplicationResource application = applicationService.getById(applicationId);

        if ("application.name".equals(fieldName)) {
            String trimmedValue = value.trim();
            if (StringUtils.isEmpty(trimmedValue)) {
                errors.add("Please enter the full title of the project");
            } else {

                application.setName(trimmedValue);
                applicationService.save(application);
            }
        } else if (fieldName.startsWith("application.durationInMonths")) {
            Long durationInMonth = Long.valueOf(value);
            if (durationInMonth < 1L || durationInMonth > 36L) {
                errors.add("Please enter a valid duration between 1 and 36 months");
                application.setDurationInMonths(durationInMonth);
            } else {
                application.setDurationInMonths(durationInMonth);
                applicationService.save(application);
            }
        } else if (fieldName.startsWith(APPLICATION_START_DATE)) {
            errors = this.saveApplicationStartDate(application, fieldName, value, errors);

        }
        return errors;
    }

    private List<String> saveApplicationStartDate(ApplicationResource application, String fieldName, String value, List<String> errors) {
        LocalDate startDate = application.getStartDate();
            if (fieldName.endsWith(".dayOfMonth")) {
                startDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), Integer.parseInt(value));
            } else if (fieldName.endsWith(".monthValue")) {
                startDate = LocalDate.of(startDate.getYear(), Integer.parseInt(value), startDate.getDayOfMonth());
            } else if (fieldName.endsWith(".year")) {
                startDate = LocalDate.of(Integer.parseInt(value), startDate.getMonth(), startDate.getDayOfMonth());
            } else if ("application.startDate".equals(fieldName)){
                String[] parts = value.split("-");
                startDate = LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
            if (startDate.isBefore(LocalDate.now())) {
                errors.add("Please enter a future date");
                startDate = null;
            }else{
                LOG.debug("Save startdate: "+ startDate.toString());
            }
            application.setStartDate(startDate);
            applicationService.save(application);
        return errors;
    }


    private void assignQuestion(@PathVariable(APPLICATION_ID) final Long applicationId,
                               HttpServletRequest request) {
        assignQuestion(request, applicationId);
    }

    private void addApplicationAndSectionsInternalWithOrgDetails(final ApplicationResource application, final CompetitionResource competition, final Long userId, Optional<SectionResource> section, final Model model, final ApplicationForm form) {
        organisationDetailsModelPopulator.populateModel(model, application.getId());
        addApplicationAndSections(application, competition, userId, section, Optional.empty(), model, form);
    }
}
