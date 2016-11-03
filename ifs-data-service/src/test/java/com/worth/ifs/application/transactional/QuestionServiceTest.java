package com.worth.ifs.application.transactional;

import com.worth.ifs.BaseUnitTestMocksTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.QuestionType;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.assessment.domain.Assessment;
import com.worth.ifs.user.domain.ProcessRole;

import static com.worth.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static com.worth.ifs.application.builder.SectionBuilder.newSection;
import static com.worth.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static com.worth.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class QuestionServiceTest extends BaseUnitTestMocksTest {

    @InjectMocks
    protected QuestionService questionService = new QuestionServiceImpl();

    @Mock
    SectionService sectionService;

    @Test
    public void getNextQuestionTest() throws Exception {
        Question question = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 1).build();
        Question nextQuestion = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 2).build();
        QuestionResource nextQuestionResource = newQuestionResource().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 2).build();

        when(questionRepositoryMock.findOne(question.getId())).thenReturn(question);
        when(questionRepositoryMock.findFirstByCompetitionIdAndSectionIdAndPriorityGreaterThanOrderByPriorityAsc(
                question.getCompetition().getId(), question.getSection().getId(), question.getPriority()))
                .thenReturn(nextQuestion);
        when(questionMapperMock.mapToResource(nextQuestion)).thenReturn(nextQuestionResource);

        // Method under test
        assertEquals(nextQuestionResource, questionService.getNextQuestion(question.getId()).getSuccessObject());
    }

    @Test
    public void getPreviousQuestionTest() throws Exception {
        Question question = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 2).build();
        Question previousQuestion = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 1).build();
        QuestionResource previousQuestionResource = newQuestionResource().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 1).build();

        when(questionRepositoryMock.findOne(question.getId())).thenReturn(question);
        when(questionRepositoryMock.findFirstByCompetitionIdAndSectionIdAndPriorityLessThanOrderByPriorityDesc(
                question.getCompetition().getId(), question.getSection().getId(), question.getPriority()))
                .thenReturn(previousQuestion);
        when(questionMapperMock.mapToResource(previousQuestion)).thenReturn(previousQuestionResource);

        // Method under test
        assertEquals(previousQuestionResource, questionService.getPreviousQuestion(question.getId()).getSuccessObject());
    }

    @Test
    public void getNextQuestionFromOtherSectionTest() throws Exception {
        Section nextSection = newSection().build();
        SectionResource nextSectionResource = newSectionResource().build();
        Question question = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), newSection().build(), 1).build();
        Question nextQuestion = newQuestion().withCompetitionAndSectionAndPriority(newCompetition().build(), nextSection, 2).build();
        QuestionResource nextQuestionResource = newQuestionResource().withCompetitionAndSectionAndPriority(newCompetition().build(), nextSection, 2).build();

        when(questionRepositoryMock.findOne(question.getId())).thenReturn(question);
        when(sectionService.getNextSection(any(SectionResource.class))).thenReturn(serviceSuccess(nextSectionResource));
        when(questionRepositoryMock.findFirstByCompetitionIdAndSectionIdAndPriorityGreaterThanOrderByPriorityAsc(
                question.getCompetition().getId(), question.getSection().getId(), question.getPriority())).thenReturn(nextQuestion);
        when(questionMapperMock.mapToResource(nextQuestion)).thenReturn(nextQuestionResource);

        // Method under test
        assertEquals(nextQuestionResource, questionService.getNextQuestion(question.getId()).getSuccessObject());
    }

    @Test
    public void getPreviousQuestionFromOtherSectionTest() throws Exception {
        Section previousSection = newSection().build();
        SectionResource previousSectionResource = newSectionResource().build();
        Competition competition = newCompetition().build();
        Question question = newQuestion().withCompetitionAndSectionAndPriority(competition, newSection().build(), 2).build();
        Question previousQuestion = newQuestion().withCompetitionAndSectionAndPriority(competition, previousSection, 1).build();
        QuestionResource previousQuestionResource = newQuestionResource().withCompetitionAndSectionAndPriority(competition, previousSection, 1).build();

        when(questionRepositoryMock.findOne(question.getId())).thenReturn(question);
        when(sectionService.getPreviousSection(any(SectionResource.class)))
                .thenReturn(serviceSuccess(previousSectionResource));
        when(questionRepositoryMock.findFirstByCompetitionIdAndSectionIdOrderByPriorityDesc(
                question.getCompetition().getId(), previousQuestion.getSection().getId()))
                .thenReturn(previousQuestion);
        when(questionRepositoryMock.findFirstByCompetitionIdAndSectionIdAndPriorityLessThanOrderByPriorityDesc(
                question.getCompetition().getId(), question.getSection().getId(), question.getPriority()))
                .thenReturn(previousQuestion);
        when(questionMapperMock.mapToResource(previousQuestion)).thenReturn(previousQuestionResource);

        // Method under test
        assertEquals(previousQuestionResource, questionService.getPreviousQuestion(question.getId()).getSuccessObject());

    }

    @Test
    public void getPreviousQuestionBySectionTest() throws Exception {
        Section currentSection = newSection().withCompetitionAndPriorityAndParent(newCompetition().build(), 1, newSection().build()).build();
        SectionResource currentSectionResource = newSectionResource().withCompetitionAndPriorityAndParent(newCompetition().build().getId(), 1, newSection().build().getId()).build();
        Question previousSectionQuestion = newQuestion().build();
        QuestionResource previousSectionQuestionResource = newQuestionResource().build();
        SectionResource previousSectionResource = newSectionResource().withQuestions(Arrays.asList(previousSectionQuestion.getId())).build();
        when(sectionService.getById(currentSection.getId())).thenReturn(serviceSuccess(currentSectionResource));
        when(sectionService.getPreviousSection(currentSectionResource)).thenReturn(serviceSuccess(previousSectionResource));
        when(questionRepositoryMock.findOne(anyLong())).thenReturn(previousSectionQuestion);
        // Method under test
        when(questionMapperMock.mapToResource(previousSectionQuestion)).thenReturn(previousSectionQuestionResource);

        assertEquals(previousSectionQuestionResource, questionService.getPreviousQuestionBySection(currentSection.getId()).getSuccessObject());
    }

    @Test
    public void getQuestionsBySectionIdAndTypeTest() {

        Question child1CostQuestion = newQuestion().withQuestionType(QuestionType.COST).build();
        Question child1OtherQuestion = newQuestion().withQuestionType(QuestionType.GENERAL).build();
        Section childSection1 = newSection().withQuestions(asList(child1CostQuestion, child1OtherQuestion)).build();

        Question child2CostQuestion = newQuestion().withQuestionType(QuestionType.COST).build();
        Question child2OtherQuestion = newQuestion().withQuestionType(QuestionType.GENERAL).build();
        Section childSection2 = newSection().withQuestions(asList(child2CostQuestion, child2OtherQuestion)).build();

        Question parentCostQuestion = newQuestion().withQuestionType(QuestionType.COST).build();
        Question parentOtherQuestion = newQuestion().withQuestionType(QuestionType.GENERAL).build();

        Section parentSection = newSection()
                .withQuestions(asList(parentCostQuestion, parentOtherQuestion))
                .withChildSections(asList(childSection1, childSection2))
                .build();

        when(sectionRepositoryMock.findOne(1L)).thenReturn(parentSection);

        QuestionResource questionResource1 = newQuestionResource().build();
        QuestionResource questionResource2 = newQuestionResource().build();
        QuestionResource questionResource3 = newQuestionResource().build();

        when(questionMapperMock.mapToResource(child1CostQuestion)).thenReturn(questionResource1);
        when(questionMapperMock.mapToResource(child2CostQuestion)).thenReturn(questionResource2);
        when(questionMapperMock.mapToResource(parentCostQuestion)).thenReturn(questionResource3);


        ServiceResult<List<QuestionResource>> result = questionService.getQuestionsBySectionIdAndType(1L, QuestionType.COST);

        assertTrue(result.isSuccess());
        assertEquals(3, result.getSuccessObject().size());
        assertTrue(result.getSuccessObject().contains(questionResource1));
        assertTrue(result.getSuccessObject().contains(questionResource2));
        assertTrue(result.getSuccessObject().contains(questionResource3));
    }

    @Test
    public void saveQuestionResource() throws Exception {
        QuestionResource questionResource = newQuestionResource().build();
        Question question = newQuestion().build();

        when(questionMapperMock.mapToDomain(questionResource)).thenReturn(question);
        when(questionMapperMock.mapToResource(question)).thenReturn(questionResource);
        when(questionRepositoryMock.save(question)).thenReturn(question);

        ServiceResult<QuestionResource> result = questionService.save(questionResource);

        assertTrue(result.isSuccess());
        assertEquals(questionResource, result.getSuccessObject());
    }

    @Test
    public void getQuestionsByAssessmentIdTest() {
        Long assessmentId = 1L;
        Long competitionId = 2L;

        Competition competition = newCompetition()
                .with(id(competitionId))
                .build();

        Application application = newApplication()
                .withCompetition(competition)
                .build();

        ProcessRole processRole = newProcessRole()
                .withApplication(application)
                .build();

        Assessment assessment = newAssessment()
                .with(id(assessmentId))
                .withParticipant(processRole)
                .build();

        List<Question> questionsForSection1 = newQuestion().build(2);
        List<QuestionResource> questionResourcesForSection1 = newQuestionResource().build(2);

        List<Question> questionsForSection2 = newQuestion().build(2);
        List<QuestionResource> questionResourcesForSection2 = newQuestionResource().build(2);

        List<Section> sections = newSection()
                .withQuestions(questionsForSection1, questionsForSection2)
                .withDisplayInAssessmentApplicationSummary(true, true)
                .build(2);

        List<SectionResource> sectionsResources = newSectionResource().build(2);

        when(assessmentRepositoryMock.findOne(assessmentId)).thenReturn(assessment);
        when(sectionService.getByCompetitionIdVisibleForAssessment(competitionId)).thenReturn(serviceSuccess(sectionsResources));
        when(sectionMapperMock.mapToDomain(same(sectionsResources.get(0)))).thenReturn(sections.get(0));
        when(sectionMapperMock.mapToDomain(same(sectionsResources.get(1)))).thenReturn(sections.get(1));
        when(questionMapperMock.mapToResource(same(questionsForSection1.get(0)))).thenReturn(questionResourcesForSection1.get(0));
        when(questionMapperMock.mapToResource(same(questionsForSection1.get(1)))).thenReturn(questionResourcesForSection1.get(1));
        when(questionMapperMock.mapToResource(same(questionsForSection2.get(0)))).thenReturn(questionResourcesForSection2.get(0));
        when(questionMapperMock.mapToResource(same(questionsForSection2.get(1)))).thenReturn(questionResourcesForSection2.get(1));

        ServiceResult<List<QuestionResource>> result = questionService.getQuestionsByAssessmentId(assessmentId);

        List<QuestionResource> expectedQuestions = concat(questionResourcesForSection1.stream(), questionResourcesForSection2.stream()).collect(toList());

        assertTrue(result.isSuccess());
        assertEquals(expectedQuestions, result.getSuccessObject());
    }
}
