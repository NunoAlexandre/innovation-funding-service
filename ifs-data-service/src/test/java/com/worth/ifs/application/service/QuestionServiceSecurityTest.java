package com.worth.ifs.application.service;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.resource.QuestionApplicationCompositeId;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.application.resource.QuestionType;
import com.worth.ifs.application.security.QuestionPermissionRules;
import com.worth.ifs.application.transactional.QuestionService;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.QuestionResourceBuilder.newQuestionResource;
import static com.worth.ifs.application.service.QuestionServiceSecurityTest.TestQuestionService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing how the secured methods in QuestionService interact with Spring Security
 */
public class QuestionServiceSecurityTest extends BaseServiceSecurityTest<QuestionService> {

    private QuestionPermissionRules questionPermissionRules;


    @Before
    public void lookupPermissionRules() {
        questionPermissionRules = getMockPermissionRulesBean(QuestionPermissionRules.class);
    }

    @Test
    public void testFindByCompetition() {
        final Long competitionId = 1L;
        service.findByCompetition(competitionId);
        verify(questionPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class));
    }

    @Test
    public void testGetNextQuestion() {
        final Long questionId = 1L;
        assertAccessDenied(
                () -> service.getNextQuestion(questionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetPreviousQuestionBySection() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> service.getPreviousQuestionBySection(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetNextQuestionBySection() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> service.getNextQuestionBySection(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetPreviousQuestion() {
        final Long sectionId = 1L;
        assertAccessDenied(
                () -> service.getPreviousQuestion(sectionId),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testQuestionResourceByFormInputType() {
        final String formInputTypeTitle = "test";
        assertAccessDenied(
                () -> service.getQuestionResourceByFormInputType(formInputTypeTitle),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testGetQuestionByFormInputType() {
        final String formInputTypeTitle = "test";
        assertAccessDenied(
                () -> service.getQuestionByFormInputType(formInputTypeTitle),
                () -> verify(questionPermissionRules).loggedInUsersCanSeeAllQuestions(isA(Question.class), isA(UserResource.class))
        );
    }
    
    @Test
    public void testGetQuestionsBySectionIdAndType() {
         service.getQuestionsBySectionIdAndType(1L, QuestionType.GENERAL);
         verify(questionPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).loggedInUsersCanSeeAllQuestions(isA(QuestionResource.class), isA(UserResource.class));
    }

    @Override
    protected Class<TestQuestionService> getServiceClass() {
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
        public ServiceResult<QuestionResource> getQuestionResourceByFormInputType(String formInputTypeTitle) {
            return serviceSuccess(newQuestionResource().build());
        }

        @Override
        public ServiceResult<Question> getQuestionByFormInputType(String formInputTypeTitle) {
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
    }
}

