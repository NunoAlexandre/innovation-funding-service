package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.application.domain.Question;
import org.innovateuk.ifs.application.resource.QuestionApplicationCompositeId;
import org.innovateuk.ifs.application.resource.QuestionResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.resource.QuestionType;
import org.innovateuk.ifs.application.security.QuestionPermissionRules;
import org.innovateuk.ifs.application.security.QuestionResourceLookupStrategy;
import org.innovateuk.ifs.application.transactional.QuestionService;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.security.AssessmentLookupStrategy;
import org.innovateuk.ifs.assessment.security.AssessmentPermissionRules;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.innovateuk.ifs.application.builder.QuestionBuilder.newQuestion;
import static org.innovateuk.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.application.service.QuestionServiceSecurityTest.TestQuestionService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.innovateuk.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Testing how the secured methods in QuestionService interact with Spring Security
 */
public class QuestionServiceSecurityTest extends BaseServiceSecurityTest<QuestionService> {

    private QuestionPermissionRules questionPermissionRules;
    private QuestionResourceLookupStrategy questionResourceLookupStrategy;

    private AssessmentPermissionRules assessmentPermissionRules;
    private AssessmentLookupStrategy assessmentLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        questionPermissionRules = getMockPermissionRulesBean(QuestionPermissionRules.class);
        questionResourceLookupStrategy = getMockPermissionEntityLookupStrategiesBean(QuestionResourceLookupStrategy.class);

        assessmentPermissionRules = getMockPermissionRulesBean(AssessmentPermissionRules.class);
        assessmentLookupStrategy = getMockPermissionEntityLookupStrategiesBean(AssessmentLookupStrategy.class);
    }

    @Test
    public void testFindByCompetition() {
        final Long competitionId = 1L;
        classUnderTest.findByCompetition(competitionId);
        verify(questionPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class));
    }

    @Test
    public void testGetQuestionById() {
        final Long questionId = 1L;
        when(questionResourceLookupStrategy.findResourceById(questionId)).thenReturn(newQuestionResource().build());
        assertAccessDenied(
                () -> classUnderTest.getQuestionById(questionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetNextQuestion() {
        final Long questionId = 1L;
        assertAccessDenied(
                () -> classUnderTest.getNextQuestion(questionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetPreviousQuestionBySection() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> classUnderTest.getPreviousQuestionBySection(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetNextQuestionBySection() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> classUnderTest.getNextQuestionBySection(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetPreviousQuestion() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> classUnderTest.getPreviousQuestion(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testQuestionResourceByCompetitionIdAndFormInputType() {
        assertAccessDenied(
                () -> classUnderTest.getQuestionResourceByCompetitionIdAndFormInputType(null, null),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetQuestionByCompetitionIdAndFormInputType() {
        assertAccessDenied(
                () -> classUnderTest.getQuestionByCompetitionIdAndFormInputType(null, null),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(Question.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetQuestionsBySectionIdAndType() {
        classUnderTest.getQuestionsBySectionIdAndType(1L, QuestionType.GENERAL);
        verify(questionPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class));
    }

    @Test
    public void testGetQuestionByIdAndAssessmentId() {
        Long questionId = 1L;
        Long assessmentId = 2L;

        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().build());
        assertAccessDenied(
                () -> classUnderTest.getQuestionByIdAndAssessmentId(questionId, assessmentId),
                () -> verify(assessmentPermissionRules).userCanReadAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetQuestionsByAssessmentId() {
        Long assessmentId = 1L;

        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().build());
        assertAccessDenied(
                () -> classUnderTest.getQuestionsByAssessmentId(assessmentId),
                () -> verify(assessmentPermissionRules).userCanReadAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Override
    protected Class<TestQuestionService> getClassUnderTest() {
        return TestQuestionService.class;
    }

    public static class TestQuestionService implements QuestionService {


        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<QuestionResource> getQuestionById(Long id) {
            return null;
        }

        @Override
        public ServiceResult<List<ValidationMessages>> markAsComplete(QuestionApplicationCompositeId ids, Long markedAsCompleteById) {
            return null;
        }

        @Override
        public ServiceResult<List<ValidationMessages>> markAsInComplete(QuestionApplicationCompositeId ids, Long markedAsInCompleteById) {
            return null;
        }

        @Override
        public ServiceResult<Void> assign(QuestionApplicationCompositeId ids, Long assigneeId, Long assignedById) {
            return null;
        }

        @Override
        public ServiceResult<Set<Long>> getMarkedAsComplete(Long applicationId, Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<Void> updateNotification(Long questionStatusId, Boolean notify) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionResource>> findByCompetition(Long competitionId) {
            return serviceSuccess(newQuestionResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<QuestionResource> getNextQuestion(Long questionId) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<QuestionResource> getPreviousQuestionBySection(Long sectionId) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<QuestionResource> getNextQuestionBySection(Long sectionId) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<QuestionResource> getPreviousQuestion(Long questionId) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<Boolean> isMarkedAsComplete(Question question, Long applicationId, Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<QuestionResource> getQuestionResourceByCompetitionIdAndFormInputType(Long competitionId, FormInputType formInputType) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<Question> getQuestionByCompetitionIdAndFormInputType(Long competitionId, FormInputType formInputType) {
            return serviceSuccess(newQuestion().build());
        }

        @Override
        public ServiceResult<List<QuestionStatusResource>> getQuestionStatusByQuestionIdAndApplicationId(Long questionId, Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionStatusResource>> getQuestionStatusByApplicationIdAndAssigneeIdAndOrganisationId(Long questionId, Long applicationId, Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionStatusResource>> getQuestionStatusByQuestionIdsAndApplicationIdAndOrganisationId(Long[] questionIds, Long applicationId, Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionStatusResource>> findByApplicationAndOrganisation(Long applicationId, Long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<QuestionStatusResource> getQuestionStatusResourceById(Long id) {
            return null;
        }

        @Override
        public ServiceResult<Integer> getCountByApplicationIdAndAssigneeId(Long applicationId, Long assigneeId) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionResource>> getQuestionsBySectionIdAndType(Long sectionId, QuestionType type) {
            return serviceSuccess(newQuestionResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<QuestionResource> save(QuestionResource questionResource) {
            return null;
        }

        @Override
        public ServiceResult<QuestionResource> getQuestionByIdAndAssessmentId(Long questionId, Long assessmentId) {
            return null;
        }

        @Override
        public ServiceResult<List<QuestionResource>> getQuestionsByAssessmentId(Long assessmentId) {
            return null;
        }
    }
}

