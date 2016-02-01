package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.QuestionStatus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for CRUD operations on {@link Question} related data.
 */
public interface QuestionService {
    public void assign(Long questionId, Long applicationId, Long assigneeId, Long assignedById);
    public void markAsComplete(Long questionId, Long applicationId, Long markedAsCompleteById);
    public void markAsInComplete(Long questionId, Long applicationId, Long markedAsInCompleteById);
    public List<Question> findByCompetition(Long competitionId);
    public List<QuestionStatus> getNotificationsForUser(Collection<QuestionStatus> questionStatuses, Long userId);
    public void removeNotifications(List<QuestionStatus> questionStatuses);
    public Set<Long> getMarkedAsComplete(Long applicationId, Long organisationId);
    public Question getById(Long questionId);
    public Question getNextQuestion(Long questionId);
    public Question getPreviousQuestion(Long questionId);
    public Question getPreviousQuestionBySection(Long sectionId);
    public Question getNextQuestionBySection(Long sectionId);
    public Map<Long, QuestionStatus> getQuestionStatusesForApplicationAndOrganisation(Long applicationId, Long userOrganisationId);
    public QuestionStatus getByQuestionIdAndApplicationIdAndOrganisationId(Long questionId, Long applicationId, Long organisationId);
    public List<QuestionStatus> getByQuestionIds(List<Long> questionIds, Long applicationId, Long organisationId);
}
