package org.innovateuk.ifs.competitionsetup.service.modelpopulator;

import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.service.CategoryService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.form.service.FormInputService;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationLandingModelPopulatorTest {

	@InjectMocks
	private ApplicationLandingModelPopulator populator;

	@Mock
	private CategoryService categoryService;
	
	@Mock
	private CompetitionService competitionService;

	@Mock
	private QuestionService questionService;

	@Mock
	private FormInputService formInputService;

	@Mock
	private SectionService sectionService;
	
	@Test
	public void testSectionToPopulateModel() {
		CompetitionSetupSection result = populator.sectionToPopulateModel();
		
		assertEquals(CompetitionSetupSection.APPLICATION_FORM, result);
	}
	
	@Test
	public void testPopulateModel() {
		Model model = new ExtendedModelMap();
		CompetitionResource competition = newCompetitionResource()
				.withCompetitionCode("code")
				.withName("name")
				.withId(8L)
				.withResearchCategories(CollectionFunctions.asLinkedSet(2L, 3L))
				.build();
		Long questionId = 100L;

		List<SectionResource> sections = newSectionResource().build(1);
		when(sectionService.getAllByCompetitionId(competition.getId())).thenReturn(sections);
		List<QuestionResource> questionResources = asList(newQuestionResource().withId(questionId).build());
		when(questionService.findByCompetition(competition.getId())).thenReturn(questionResources);

		List<SectionResource> generalSections = sections.stream().filter(sectionResource -> sectionResource.getType() == SectionType.GENERAL).collect(Collectors.toList());
		List<SectionResource> parentSections = generalSections.stream().filter(sectionResource -> sectionResource.getParentSection() == null).collect(Collectors.toList());

		List<FormInputResource> formInputResources = asList(newFormInputResource().withScope(FormInputScope.APPLICATION));
		when(formInputService.findApplicationInputsByQuestion(questionId)).thenReturn(formInputResources);
		List<FormInputResource> formInputResourcesAssessment = asList(newFormInputResource().withScope(FormInputScope.ASSESSMENT));
		when(formInputService.findAssessmentInputsByQuestion(questionId)).thenReturn(formInputResourcesAssessment);

		populator.populateModel(model, competition);
		
		assertEquals(2, model.asMap().size());
		assertEquals(new ArrayList(), model.asMap().get("questions"));
		assertEquals(new ArrayList(), model.asMap().get("projectDetails"));
	}
}
