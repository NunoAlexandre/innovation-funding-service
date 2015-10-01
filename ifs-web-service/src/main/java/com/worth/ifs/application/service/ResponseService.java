package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Response;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

public interface ResponseService {
    public List<Response> getByApplication(Long applicationId);
    public HashMap<Long, Response> mapResponsesToQuestion(List<Response> responses);
    public Boolean save(Long userId, Long applicationId, Long questionId, String value);
    public Boolean saveQuestionResponseAssessorScore(Long assessorUserId, Long responseId, Integer score);
    public Boolean saveQuestionResponseAssessorConfirmationAnswer(Long assessorUserId, Long responseId, Boolean confirmation);
    public Boolean saveQuestionResponseAssessorFeedback(Long assessorUserId, Long responseId, String feedback);
}
