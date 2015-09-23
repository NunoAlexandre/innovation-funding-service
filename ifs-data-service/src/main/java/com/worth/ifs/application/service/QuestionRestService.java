package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Question;

import java.util.List;

public interface QuestionRestService {
    public void markAsComplete(Long questionId, Long markedAsCompleteById);
    public void markAsInComplete(Long questionId, Long markedAsInCompleteById);
    public void assign(Long questionId, Long assigneeId, Long assignedById);
    public List<Question> findByCompetition(Long competitionId);
    public void updateNotification(Long questionStatusId, Boolean notify);
}
