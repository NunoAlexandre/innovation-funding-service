package com.worth.ifs.form.service;

import com.worth.ifs.application.domain.Response;
import com.worth.ifs.application.service.ResponseRestService;
import com.worth.ifs.form.resource.FormInputResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class contains methods to retrieve and store {@link Response} related data,
 * through the RestService {@link ResponseRestService}.
 */
@Service
public class FormInputServiceImpl implements FormInputService {

    @Autowired
    private FormInputRestService formInputRestService;

    @Override
    public FormInputResource getOne(Long formInputId) {
        return formInputRestService.getById(formInputId).getSuccessObjectOrThrowException();
    }
}
