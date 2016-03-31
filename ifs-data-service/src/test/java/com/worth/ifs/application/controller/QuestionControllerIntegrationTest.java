package com.worth.ifs.application.controller;

import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.QuestionStatus;
import com.worth.ifs.application.repository.QuestionStatusRepository;
import com.worth.ifs.application.transactional.QuestionService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@Rollback
public class QuestionControllerIntegrationTest extends BaseControllerIntegrationTest<QuestionController> {


    @Autowired
    QuestionStatusRepository questionStatusRepository;
    @Autowired
    QuestionService questionService;


    private final Long userId = 1L;
    private final Long applicationId = 1L;
    private final Long questionId = 13L;
    private Question question;
    private Long newAssigneeProcessRoleId = 5L;
    private Long organisationId = 3L;
    private Long questionStatusId = 2L;
    private Long competitionId = 1L;
    private Long sectionId = 2L;
    public static final long QUESTION_ID_WITH_MULTIPLE = 35L;


    @Before
    public void setup(){
        question = controller.getQuestionById(questionId).getSuccessObject();
    }


    @Override
    @Autowired
    protected void setControllerUnderTest(QuestionController controller) {
        this.controller = controller;
    }

    @Test
    public void testGetQuestionById() throws Exception {
        question= controller.getQuestionById(questionId).getSuccessObject();

        assertNotNull(question);
        assertEquals("How does your project align with the scope of this competition?", question.getName());
    }

    @Test
    public void testMarkAsComplete() throws Exception {
        controller.markAsComplete(questionId, applicationId, userId);


        List<QuestionStatus> statuses = questionStatusRepository.findByQuestionIdAndApplicationId(questionId, applicationId);

        statuses.forEach(
                s -> assertTrue(s.getMarkedAsComplete())
        );
    }

    @Test
    public void testMarkAsInComplete() throws Exception {
        controller.markAsInComplete(questionId, applicationId, userId);

        List<QuestionStatus> statuses = questionStatusRepository.findByQuestionIdAndApplicationId(questionId, applicationId);

        statuses.forEach(
                s -> assertFalse(s.getMarkedAsComplete())
        );

    }

     @Test
     public void testAssign() throws Exception {
        controller.assign(questionId, applicationId, newAssigneeProcessRoleId, userId);

        List<QuestionStatus> statuses = questionStatusRepository.findByQuestionIdAndApplicationId(questionId, applicationId);

        statuses.forEach(
                s -> assertEquals(newAssigneeProcessRoleId, s.getAssignee().getId())
        );
    }

    @Ignore
    @Test
    public void testAssignMultiple() throws Exception {
        //Todo: don't know how to implement this, can we assign questions that are in the finance form for example?
    }

    @Test
    public void testGetMarkedAsComplete() throws Exception {
        // Start with zero completed
        Set<Long> markedAsComplete = controller.getMarkedAsComplete(applicationId, organisationId).getSuccessObject();
        assertNotNull(markedAsComplete);
        assertEquals(7, markedAsComplete.size());

        // Complete one section
        controller.markAsComplete(questionId, applicationId, userId);
        markedAsComplete = controller.getMarkedAsComplete(applicationId, organisationId).getSuccessObject();
        assertNotNull(markedAsComplete);
        assertEquals(8, markedAsComplete.size());

        // Mark section as incomplete again.
        controller.markAsInComplete(questionId, applicationId, userId);
        markedAsComplete = controller.getMarkedAsComplete(applicationId, organisationId).getSuccessObject();
        assertNotNull(markedAsComplete);
        assertEquals(7, markedAsComplete.size());
    }

    @Test
    public void testUpdateNotification() throws Exception {
        QuestionStatus questionStatus = questionStatusRepository.findByQuestionIdAndApplicationIdAndAssigneeId(questionId, applicationId, userId);

        controller.updateNotification(questionStatus.getId(), true);

        questionStatus = questionStatusRepository.findByQuestionIdAndApplicationIdAndAssigneeId(questionId, applicationId, userId);
        assertTrue(questionStatus.getNotified());

        controller.updateNotification(questionStatus.getId(), false);

        questionStatus = questionStatusRepository.findByQuestionIdAndApplicationIdAndAssigneeId(questionId, applicationId, userId);
        assertFalse(questionStatus.getNotified());
    }

    @Test
    public void testFindByCompetition() throws Exception {
        List<Question> questions = controller.findByCompetition(competitionId).getSuccessObject();

        assertNotNull(questions);
        assertTrue(questions.size() > 5);
    }

    @Test
    public void testGetNextQuestion() throws Exception {
        Question nextQuestion = controller.getNextQuestion(9L).getSuccessObject();
        assertNotNull(nextQuestion);
        assertEquals(new Long(11L), nextQuestion.getId());
    }

    @Test
    public void testGetPreviousQuestion() throws Exception {
        Question previousQuestion = controller.getPreviousQuestion(11L).getSuccessObject();

        assertNotNull(previousQuestion);
        assertEquals(new Long(9L), previousQuestion.getId());
    }

    @Test
    public void testGetPreviousQuestionBySection() throws Exception {
        Question previousQuestion = controller.getPreviousQuestionBySection(10L).getSuccessObject();
        assertNotNull(previousQuestion);
        assertNotNull(previousQuestion.getId());
        assertEquals(16L , previousQuestion.getId().longValue());
    }

    @Test
    public void testGetNextQuestionBySection() throws Exception {
        Question nextQuestion = controller.getNextQuestionBySection(10L).getSuccessObject();
        assertNotNull(nextQuestion);
        assertNotNull(nextQuestion.getId());
        assertEquals(41L, nextQuestion.getId().longValue());
    }

    @Test
    public void testIsMarkedAsComplete() throws Exception {
        assertFalse(questionService.isMarkedAsComplete(question, applicationId, organisationId).getSuccessObject());

        controller.markAsComplete(questionId, applicationId, userId);

        assertTrue(questionService.isMarkedAsComplete(question, applicationId, organisationId).getSuccessObject());
    }

    @Test
    public void testIsMarkedAsCompleteMultiple() throws Exception {
        question = controller.getQuestionById(QUESTION_ID_WITH_MULTIPLE).getSuccessObject();

        assertFalse(questionService.isMarkedAsComplete(question, applicationId, organisationId).getSuccessObject());

        controller.markAsComplete(QUESTION_ID_WITH_MULTIPLE, applicationId, userId);
        controller.markAsComplete(QUESTION_ID_WITH_MULTIPLE, applicationId, 2L);
        controller.markAsComplete(QUESTION_ID_WITH_MULTIPLE, applicationId, 8L);
        controller.markAsComplete(QUESTION_ID_WITH_MULTIPLE, applicationId, 9L);

        assertTrue(questionService.isMarkedAsComplete(question, applicationId, organisationId).getSuccessObject());
    }
}