package com.worth.ifs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.domain.*;
import com.worth.ifs.security.TokenAuthenticationService;
import com.worth.ifs.service.ApplicationService;
import com.worth.ifs.service.ResponseService;
import com.worth.ifs.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * This controller will handle all requests that are related to the application form.
 */
@Controller
@RequestMapping("/application-form")
public class ApplicationFormController {
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    ApplicationService applicationService;
    @Autowired
    ResponseService responseService;

    @Autowired
    UserService userService;

    @Autowired
    TokenAuthenticationService tokenAuthenticationService;

    /**
     * Get the details of the current application, add this to the model so we can use it in the templates.
     */
    private void addApplicationDetails(Long applicationId, Model model){
        Application application = applicationService.getApplicationById(applicationId);
        model.addAttribute("currentApplication", application);

        Competition competition = application.getCompetition();
        model.addAttribute("currentCompetition", competition);

        List<Section> sections = competition.getSections();
        model.addAttribute("sections", sections);


        List<Response> responses = responseService.getResponsesByApplicationId(applicationId);
        HashMap<Long, Response> responseMap = new HashMap<>();
        for (Response response : responses) {
            responseMap.put(response.getQuestion().getId(), response);
        }
        model.addAttribute("responses", responseMap);
    }

    @RequestMapping("/{applicationId}")
    public String applicationForm(Model model,@PathVariable("applicationId") final Long applicationId){
        this.addApplicationDetails(applicationId, model);
        return "application-form";
    }

    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.GET)
    public String applicationFormWithOpenSection(Model model,
                                     @PathVariable("applicationId") final Long applicationId,
                                     @PathVariable("sectionId") final Long sectionId){
        Application app = applicationService.getApplicationById(applicationId);
        Competition comp = app.getCompetition();
        List<Section> sections = comp.getSections();

        // get the section that we want to show, so we can use this on to show the correct questions.
        Section section = sections.stream().filter(x -> x.getId() == sectionId).findFirst().get();

        this.addApplicationDetails(applicationId, model);
        model.addAttribute("currentSectionId", sectionId);
        model.addAttribute("currentSection", section);

        return "application-form";
    }
    @RequestMapping(value = "/{applicationId}/section/{sectionId}", method = RequestMethod.POST)
    public String applicationFormSubmit(Model model,
                                                 @PathVariable("applicationId") final Long applicationId,
                                                 @PathVariable("sectionId") final Long sectionId,
                                                 HttpServletRequest request){

        log.warn("Got form submit;");
        User user = (User)tokenAuthenticationService.getAuthentication(request).getDetails();


        Application app = applicationService.getApplicationById(applicationId);
        Competition comp = app.getCompetition();
        List<Section> sections = comp.getSections();

        // get the section that we want to show, so we can use this on to show the correct questions.
        Section section = sections.stream().filter(x -> x.getId().equals(sectionId)).findFirst().get();

        List<Question> questions = section.getQuestions();
        for (Question question : questions) {
            if(request.getParameterMap().containsKey("question[" + question.getId() + "]")){
                String value = request.getParameter("question[" + question.getId() + "]");
                log.warn("Question/response in post: " + question.getId() + ": " + value);
                responseService.saveQuestionResponse(user.getId(), applicationId, question.getId(), value);
            }
        }


        this.addApplicationDetails(applicationId, model);
        model.addAttribute("currentSectionId", sectionId);
        model.addAttribute("currentSection", section);
        model.addAttribute("applicationSaved", true);
        return "application-form";
    }


    @RequestMapping(value = "/saveFormElement", method = RequestMethod.POST)
    public @ResponseBody JsonNode saveFormElement(@RequestParam("questionId") Long questionId,
                                                  @RequestParam("value") String value,
                                                  @RequestParam("applicationId") Long applicationId,
                                                  HttpServletRequest request) {

        User user = (User)tokenAuthenticationService.getAuthentication(request).getDetails();

        log.info("Save Form element: applicationId " + applicationId);
        log.info("Save Form element: questionId " + questionId);
        log.info("Save Form element: value " + value);

        responseService.saveQuestionResponse(user.getId(), applicationId, questionId, value);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", "true");
        return node;

    }
}
