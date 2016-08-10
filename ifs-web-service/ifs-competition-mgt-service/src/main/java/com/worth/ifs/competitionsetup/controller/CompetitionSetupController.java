package com.worth.ifs.competitionsetup.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CharMatcher;
import com.worth.ifs.application.service.CategoryService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.category.resource.CategoryResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionResource.Status;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competitionsetup.form.AdditionalInfoForm;
import com.worth.ifs.competitionsetup.form.ApplicationFormForm;
import com.worth.ifs.competitionsetup.form.AssessorsForm;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.EligibilityForm;
import com.worth.ifs.competitionsetup.form.FinanceForm;
import com.worth.ifs.competitionsetup.form.InitialDetailsForm;
import com.worth.ifs.competitionsetup.form.MilestonesForm;
import com.worth.ifs.competitionsetup.service.CompetitionSetupService;
import com.worth.ifs.competitionsetup.model.Question;
import com.worth.ifs.competitionsetup.service.CompetitionSetupQuestionService;
import com.worth.ifs.competitionsetup.service.CompetitionSetupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


/**
 * Controller for showing and handling the different competition setup sections
 */
@Controller
@RequestMapping("/competition/setup")
public class CompetitionSetupController {

    private static final Log LOG = LogFactory.getLog(CompetitionSetupController.class);
    private static final String BINDING_RESULT_COMPETITION_SETUP_FORM = "org.springframework.validation.BindingResult.applicationFormForm";

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionSetupService competitionSetupService;

    @Autowired
    private CompetitionSetupQuestionService competitionSetupQuestionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private Validator validator;

    @RequestMapping(value = "/{competitionId}", method = RequestMethod.GET)
    public String initCompetitionSetupSection(Model model, @PathVariable("competitionId") Long competitionId) {

        CompetitionResource competition = competitionService.getById(competitionId);

        if(competition == null || !Status.COMPETITION_SETUP.equals(competition.getCompetitionStatus())) {
        	LOG.error("Competition is not found in setup state");
            return "redirect:/dashboard";
        }

        CompetitionSetupSection section = CompetitionSetupSection.fromPath("home");
        competitionSetupService.populateCompetitionSectionModelAttributes(model, competition, section);
        return "competition/setup";
    }

    @RequestMapping(value = "/{competitionId}/home", method = RequestMethod.GET)
    public String competitionSetupHome(Model model, @PathVariable("competitionId") Long competitionId) {
          return "competition/setup-home";
    }


    @RequestMapping(value = "/{competitionId}/section/{sectionPath}/edit", method = RequestMethod.POST)
    public String setSectionAsIncomplete(@PathVariable("competitionId") Long competitionId, @PathVariable("sectionPath") String sectionPath) {

    	CompetitionSetupSection section = CompetitionSetupSection.fromPath(sectionPath);
    	if(section == null) {
    		LOG.error("Invalid section path specified: " + sectionPath);
            return "redirect:/dashboard";
    	}

        competitionService.setSetupSectionMarkedAsIncomplete(competitionId, section);

        return "redirect:/competition/setup/" + competitionId + "/section/" + section.getPath();
    }

    @RequestMapping(value = "/{competitionId}/section/application/question/{questionId}", method = RequestMethod.GET)
    public String editCompetitionSetupApplication(@PathVariable("competitionId") Long competitionId,
                                              @PathVariable("questionId") Long questionId,
                                              Model model) {

    	CompetitionSetupSection section = CompetitionSetupSection.APPLICATION_FORM;
        CompetitionResource competition = competitionService.getById(competitionId);

        //TODO do competition check

        competitionSetupService.populateCompetitionSectionModelAttributes(model, competition, section);
        ApplicationFormForm competitionSetupForm = (ApplicationFormForm) competitionSetupService.getSectionFormData(competition, section);

        addQuestionToUpdate(questionId, model, competitionSetupForm);
        model.addAttribute("competitionSetupForm", competitionSetupForm);

        return "competition/setup";
    }

    @RequestMapping(value = "/{competitionId}/section/{sectionPath}", method = RequestMethod.GET)
    public String editCompetitionSetupSection(@PathVariable("competitionId") Long competitionId,
                                              @PathVariable("sectionPath") String sectionPath,
                                              Model model) {

        CompetitionSetupSection section = CompetitionSetupSection.fromPath(sectionPath);
        if(section == null) {
            LOG.error("Invalid section path specified: " + sectionPath);
            return "redirect:/dashboard";
        }

        CompetitionResource competition = competitionService.getById(competitionId);

        if(competition == null || !Status.COMPETITION_SETUP.equals(competition.getCompetitionStatus())) {
            LOG.error("Competition is not found in setup state");
            return "redirect:/dashboard";
        }

        competitionSetupService.populateCompetitionSectionModelAttributes(model, competition, section);
        model.addAttribute("competitionSetupForm", competitionSetupService.getSectionFormData(competition, section));

        return "competition/setup";
    }

    @RequestMapping(value = "/{competitionId}/section/initial", method = RequestMethod.POST)
    public String submitInitialSectionDetails(@Valid @ModelAttribute("competitionSetupForm") InitialDetailsForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.INITIAL_DETAILS, model);
    }

    @RequestMapping(value = "/{competitionId}/section/additional", method = RequestMethod.POST)
    public String submitAdditionalSectionDetails(@Valid @ModelAttribute("competitionSetupForm") AdditionalInfoForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.ADDITIONAL_INFO, model);
    }

    @RequestMapping(value = "/{competitionId}/section/eligibility", method = RequestMethod.POST)
    public String submitEligibilitySectionDetails(@Valid @ModelAttribute("competitionSetupForm") EligibilityForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

    	if("yes".equals(competitionSetupForm.getMultipleStream()) && StringUtils.isEmpty(competitionSetupForm.getStreamName())){
    		bindingResult.addError(new FieldError("competitionSetupForm", "streamName", "A stream name is required"));
    	}
        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.ELIGIBILITY, model);
    }

    @RequestMapping(value = "/{competitionId}/section/milestones", method = RequestMethod.POST)
    public String submitMilestonesSectionDetails(@Valid @ModelAttribute("competitionSetupForm") MilestonesForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.MILESTONES, model);
    }

    @RequestMapping(value = "/{competitionId}/section/assessors", method = RequestMethod.POST)
    public String submitMilestonesSectionDetails(@Valid @ModelAttribute("competitionSetupForm") AssessorsForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.ASSESSORS, model);
    }

    @RequestMapping(value = "/{competitionId}/section/application", method = RequestMethod.POST)
    public String submitApplicationFormSectionDetails(@Valid @ModelAttribute("competitionSetupForm") ApplicationFormForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {


        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.APPLICATION_FORM, model);
    }

    @RequestMapping(value = "/{competitionId}/section/application/question/{questionId}", method = RequestMethod.POST)
    public String submitApplicationQuestion(@Valid @ModelAttribute("competitionSetupForm") ApplicationFormForm competitionSetupForm,
                                            BindingResult bindingResult,
                                            @PathVariable("competitionId") Long competitionId,
                                            @PathVariable("questionId") Long questionId,
                                            Model model) {

        competitionSetupQuestionService.updateQuestion(competitionSetupForm.getQuestionToUpdate());

        if(!bindingResult.hasErrors()) {
            return "redirect:/competition/setup/" + competitionId + "/section/application";
        } else {
            competitionSetupService.populateCompetitionSectionModelAttributes(model, competitionService.getById(competitionId), CompetitionSetupSection.APPLICATION_FORM);
            addQuestionToUpdate(questionId, model, competitionSetupForm);
            model.addAttribute("competitionSetupForm", competitionSetupForm);
            return "competition/setup";
        }
    }

    @RequestMapping(value = "/{competitionId}/section/finance", method = RequestMethod.POST)
    public String submitFinanceSectionDetails(@Valid @ModelAttribute("competitionSetupForm") FinanceForm competitionSetupForm,
                                              BindingResult bindingResult,
                                              @PathVariable("competitionId") Long competitionId,
                                              Model model) {

        return genericCompetitionSetupSection(competitionSetupForm, bindingResult, competitionId, CompetitionSetupSection.FINANCE, model);
    }

    /* AJAX Function */
    @RequestMapping(value = "/getInnovationArea/{innovationSectorId}", method = RequestMethod.GET)
    @ResponseBody
    public List<CategoryResource> getInnovationAreas(@PathVariable("innovationSectorId") Long innovationSectorId) {

        return categoryService.getCategoryByParentId(innovationSectorId);
    }

    /* AJAX Function */
    @RequestMapping(value = "/{competitionId}/generateCompetitionCode", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode generateCompetitionCode(@PathVariable("competitionId") Long competitionId, HttpServletRequest request) {

      CompetitionResource competition = competitionService.getById(competitionId);
      if (competition.getStartDate() != null) {
        return this.createJsonObjectNode(true, competitionService.generateCompetitionCode(competitionId, competition.getStartDate()));
      }
      else {
        return this.createJsonObjectNode(false, "Please set a start date for your competition before generating the competition code, you can do this in the Initial Details section");
      }
    }


    private String genericCompetitionSetupSection(CompetitionSetupForm competitionSetupForm, BindingResult bindingResult, Long competitionId, CompetitionSetupSection section, Model model) {
        CompetitionResource competition = competitionService.getById(competitionId);

        if(competition == null || !Status.COMPETITION_SETUP.equals(competition.getCompetitionStatus())) {
        	LOG.error("Competition is not found in setup state");
            return "redirect:/dashboard";
        }

        if (!bindingResult.hasErrors()) {
        	List<Error> saveSectionResult = competitionSetupService.saveCompetitionSetupSection(competitionSetupForm, competition, section);
        	if(saveSectionResult != null && !saveSectionResult.isEmpty()) {
        		saveSectionResult.forEach(e -> {
        			ObjectError error = new ObjectError("currentSection", e.getErrorMessage());
        			bindingResult.addError(error);
        		});
        	}
            //@TODO Fix redirection / error handling
            return null;
//        	competitionSetupService.saveCompetitionSetupSection(competitionSetupForm, competition, section);
//            return "redirect:/competition/setup/" + competitionId + "/section/" + section.getPath();
        } else {
            LOG.debug("Form errors");
            competitionSetupService.populateCompetitionSectionModelAttributes(model, competition, section);
            return "competition/setup";
        }
    }

    private ObjectNode createJsonObjectNode(boolean success, String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("success", success ? "true" : "false");
        node.put("message", CharMatcher.is('\"').trimFrom(message));
        return node;
    }

    private void addQuestionToUpdate(Long questionId, Model model, ApplicationFormForm applicationFormForm) {
        if(model.containsAttribute("questions")) {
            List<Question> questions = (List<Question>) model.asMap().get("questions");
            Optional<Question> question = questions.stream().filter(questionObject -> questionObject.getId().equals(questionId)).findFirst();
            if(question.isPresent()) {
                applicationFormForm.setQuestionToUpdate(question.get());
            } else {
                LOG.error("Question(" + questionId + ") not found");
            }
        }

    }

}
