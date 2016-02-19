package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Response;
import com.worth.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class contains methods to retrieve and store {@link Response} related data,
 * through the RestService {@link ResponseRestService}.
 */
// TODO DW - INFUND-1555 - handle rest results
@Service
public class ResponseServiceImpl implements ResponseService {

    @Autowired
    ResponseRestService responseRestService;

    @Override
    public List<Response> getByApplication(Long applicationId) {
        return responseRestService.getResponsesByApplicationId(applicationId).getSuccessObjectOrNull();
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
    public RestResult<Void> saveQuestionResponseAssessorFeedback(Long assessorUserId, Long responseId, Optional<String> feedbackValue, Optional<String> feedbackText) {
        return responseRestService.saveQuestionResponseAssessorFeedback(assessorUserId, responseId, feedbackValue, feedbackText);
    }
}
