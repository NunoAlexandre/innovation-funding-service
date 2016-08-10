package com.worth.ifs.competitionsetup.service.modelpopulator;

import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.resource.SectionType;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.application.service.SectionService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.model.Question;
import com.worth.ifs.form.resource.FormInputResource;
import com.worth.ifs.form.service.FormInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * populates the model for the initial details competition setup section.
 */
@Service
public class ApplicationFormModelPopulator implements CompetitionSetupSectionModelPopulator {

	@Autowired
	private CompetitionService competitionService;

	@Autowired
	private SectionService sectionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private FormInputService formInputService;

    private Long appendixTypeId = 4L;
    private Long scoreTypeId = 23L;

    @Override
	public CompetitionSetupSection sectionToPopulateModel() {
		return CompetitionSetupSection.APPLICATION_FORM;
	}

	@Override
	public void populateModel(Model model, CompetitionResource competitionResource) {
		List<SectionResource> sections = sectionService.getAllByCompetitionId(competitionResource.getId());
		List<QuestionResource> questionResources = questionService.findByCompetition(competitionResource.getId());

        List<SectionResource> generalSections = sections.stream().filter(sectionResource -> sectionResource.getType() == SectionType.GENERAL).collect(Collectors.toList());
        List<SectionResource> parentSections = generalSections.stream().filter(sectionResource -> sectionResource.getParentSection() == null).collect(Collectors.toList());

        model.addAttribute("generalSections", generalSections);
        model.addAttribute("generalParentSections", parentSections);
        model.addAttribute("questions", getSortedQuestions(questionResources, parentSections));
	}

	private List<Question> getSortedQuestions(List<QuestionResource> questionResources, List<SectionResource> parentSections) {
        List<Question> questions = new ArrayList();

        Optional<SectionResource> section = parentSections.stream().filter(sectionResource -> sectionResource.getName().equals("Application questions")).findFirst();

        if(!section.isPresent()) {
            return new ArrayList();
        }

        questionResources = questionResources.stream().filter(questionResource -> section.get().getQuestions().contains(questionResource.getId())).collect(Collectors.toList());
        questionResources.forEach(questionResource -> initQuestionForForm(questions, questionResource));

        return questions.stream().sorted((q1, q2) -> Integer.compare(Integer.parseInt(q1.getNumber()), Integer.parseInt(q2.getNumber()))).collect(Collectors.toList());
    }

    private void initQuestionForForm(List<Question> questions, QuestionResource questionResource) {
        List<FormInputResource> formInputs = formInputService.findApplicationInputsByQuestion(questionResource.getId());
        List<FormInputResource> formAssessmentInputs = formInputService.findAssessmentInputsByQuestion(questionResource.getId());

        Boolean appendix = inputsTypeMatching(formInputs, appendixTypeId);
        Boolean scored = inputsTypeMatching(formAssessmentInputs, scoreTypeId);

        formInputs.stream()
                .filter(formInputResource -> !formInputResource.getFormInputType().equals(appendixTypeId))
                .forEach(formInputResource -> questions.add(createQuestionObjectFromQuestionResource(questionResource, formInputResource, appendix, scored)));
    }

    private Boolean inputsTypeMatching(List<FormInputResource> formInputs, Long typeId) {
        return formInputs
                .stream()
                .anyMatch(formInputResource -> formInputResource.getFormInputType().equals(typeId));
    }

	private Question createQuestionObjectFromQuestionResource(QuestionResource questionResource, FormInputResource formInputResource, Boolean appendix, Boolean scored){
        Question question = new Question();
        question.setId(questionResource.getId());
        question.setNumber(questionResource.getQuestionNumber());
        question.setShortTitle(questionResource.getShortName());
        question.setTitle(questionResource.getName());
        question.setSubTitle(questionResource.getDescription());

        question.setGuidanceTitle(formInputResource.getGuidanceQuestion());
        question.setGuidance(formInputResource.getGuidanceAnswer());
        if(formInputResource.getWordCount() > 0) {
            question.setMaxWords(formInputResource.getWordCount());
        } else {
            question.setMaxWords(400);
        }

        question.setScored(scored);
        question.setAppendix(appendix);

	    return question;
    }

}
