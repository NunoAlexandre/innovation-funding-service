package com.worth.ifs.validator;

import com.worth.ifs.form.domain.FormInputResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

public class ValidatedResponse {
    private FormInputResponse response;
    private List<String> allErrors;
    private int errorCount;

    public ValidatedResponse() {
    	// no-arg constructor
    }

    public ValidatedResponse(BindingResult result, FormInputResponse response) {
        errorCount = result.getErrorCount();
        allErrors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        this.response = response;
    }

    public FormInputResponse getResponse() {
        return response;
    }

    public void setResponse(FormInputResponse response) {
        this.response = response;
    }

    public List<String> getAllErrors() {
        return allErrors;
    }

    public void setAllErrors(List<String> allErrors) {
        this.allErrors = allErrors;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
