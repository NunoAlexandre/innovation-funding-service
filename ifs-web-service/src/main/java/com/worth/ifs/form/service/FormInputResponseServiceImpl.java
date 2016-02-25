package com.worth.ifs.form.service;

import com.worth.ifs.application.domain.Response;
import com.worth.ifs.application.service.ResponseRestService;
import com.worth.ifs.form.domain.FormInputResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.worth.ifs.util.CollectionFunctions.simpleToMap;

/**
 * This class contains methods to retrieve and store {@link Response} related data,
 * through the RestService {@link ResponseRestService}.
 */
// TODO DW - INFUND-1555 - handle rest results
@Service
public class FormInputResponseServiceImpl implements FormInputResponseService {

    @Autowired
    private FormInputResponseRestService responseRestService;

    @Override
    public List<FormInputResponse> getByApplication(Long applicationId) {
        return responseRestService.getResponsesByApplicationId(applicationId).getSuccessObjectOrNull();
    }

    @Override
    public Map<Long, FormInputResponse> mapFormInputResponsesToFormInput(List<FormInputResponse> responses) {
        return simpleToMap(
            responses,
            response -> response.getFormInput().getId(),
            response -> response
        );
    }

    @Override
    public List<String> save(Long userId, Long applicationId, Long formInputId, String value) {
        return responseRestService.saveQuestionResponse(userId, applicationId, formInputId, value).getSuccessObjectOrThrowException();
    }
}
