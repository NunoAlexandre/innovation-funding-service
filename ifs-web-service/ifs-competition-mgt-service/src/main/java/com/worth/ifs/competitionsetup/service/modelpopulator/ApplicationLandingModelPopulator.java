package com.worth.ifs.competitionsetup.service.modelpopulator;

import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.resource.SectionType;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.application.service.SectionService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupQuestionType;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * populates the model for the Application Questions landing page of the competition setup section.
 */
@Service
public class ApplicationLandingModelPopulator implements CompetitionSetupSectionModelPopulator {

    private static final String QUESTIONS_KEY = "questions";
    private static final String PROJECT_DETAILS_KEY = "projectDetails";

	@Autowired
	private SectionService sectionService;

    @Autowired
    private QuestionService questionService;

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
        model.addAttribute(QUESTIONS_KEY, getSortedQuestions(questionResources, parentSections));
        model.addAttribute(PROJECT_DETAILS_KEY, getSortedProjectDetails(questionResources, parentSections));
   }

	private List<QuestionResource> getSortedQuestions(List<QuestionResource> questionResources, List<SectionResource> parentSections) {
        Optional<SectionResource> section = parentSections.stream().filter(sectionResource -> sectionResource.getName().equals("Application questions")).findFirst();
        return section.isPresent() ? questionResources.stream().filter(questionResource -> section.get().getQuestions().contains(questionResource.getId())).collect(Collectors.toList())
                : new ArrayList<>();
    }

    private List<QuestionResource> getSortedProjectDetails(List<QuestionResource> questionResources, List<SectionResource> parentSections) {
        Optional<SectionResource> section = parentSections.stream().filter(sectionResource -> sectionResource.getName().equals("Project details")).findFirst();
        return section.isPresent() ? questionResources.stream()
                .filter(questionResource ->  section.get().getQuestions().contains(questionResource.getId()))
                .filter(questionResource -> !questionResource.getShortName().equals(CompetitionSetupQuestionType.APPLICATION_DETAILS.getShortName()))
                .collect(Collectors.toList())
                : new ArrayList<>();
    }
}
