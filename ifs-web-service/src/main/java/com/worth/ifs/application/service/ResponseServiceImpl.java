package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ResponseServiceImpl implements ResponseService {

    @Autowired
    ResponseRestService responseRestService;

    @Override
    public List<Response> getByApplication(Long applicationId) {
        return responseRestService.getResponsesByApplicationId(applicationId);
    }

    @Override
    public void assignQuestion(Long applicationId, Long questionId, Long userId, Long assigneeId) {
        responseRestService.assignQuestion(applicationId, questionId, userId, assigneeId);
    }

    @Override
    public HashMap<Long, Response> mapResponsesToQuestion(List<Response> responses) {
        HashMap<Long, Response> responseMap = new HashMap<>();
        for (Response response : responses) {
            responseMap.put(response.getQuestion().getId(), response);
        }
        return responseMap;
    }

    @Override
    public void markQuestionAsComplete(Long applicationId, Long questionId, Long userId) {
        responseRestService.markQuestionAsComplete(applicationId, questionId, userId, true);
    }

    @Override
    public void markQuestionAsInComplete(Long applicationId, Long questionId, Long userId) {
        responseRestService.markQuestionAsComplete(applicationId, questionId, userId, false);
    }

    @Override
    public Boolean save(Long userId, Long applicationId, Long questionId, String value) {
        return responseRestService.saveQuestionResponse(userId, applicationId, questionId, value);
    }
}
