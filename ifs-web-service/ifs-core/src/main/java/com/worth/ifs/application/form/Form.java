package com.worth.ifs.application.form;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.math.NumberUtils.isNumber;

/**
 * This class is used to setup and submit the form input values. On submit the values are converted into an Form object.
 * http://stackoverflow.com/a/4511716
 */
public class Form {
    private Map<String, String> formInput;
    private List<ObjectError> objectErrors;
    private BindingResult bindingResult;

    public Form() {
        this.formInput = new HashMap<>();
        this.objectErrors = new ArrayList<>();
    }

    public Map<String, String> getFormInput() {
        return formInput;
    }

    public void setFormInput(Map<String, String> values) {
        this.formInput = values;
    }

    public void addFormInput(String key, String value){
        this.formInput.put(key, value);
    }

    public void addFormInput(String key, LocalDate value){
        this.formInput.put(key + "_day", value != null ? value.getDayOfMonth() + "" : "");
        this.formInput.put(key + "_month", value != null ? value.getMonthValue() + "" : "");
        this.formInput.put(key + "_year", value != null ? value.getYear() + "" : "");
    }

    public String getFormInput(String key){
        return this.formInput.get(key);
    }

    public LocalDate getFormInputLocalDate(String key){

        String day = this.formInput.get(key + "_day");
        String month = this.formInput.get(key + "_month");
        String year = this.formInput.get(key + "_year");

        if (isNumber(day) && isNumber(month) && isNumber(year)) {
            return LocalDate.of(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day));
        }

        return null;
    }

    public List<ObjectError> getObjectErrors() {
		return objectErrors;
	}
    public void setObjectErrors(List<ObjectError> objectErrors) {
		this.objectErrors = objectErrors;
	}
    public BindingResult getBindingResult() {
		return bindingResult;
	}
    public void setBindingResult(BindingResult bindingResult) {
		this.bindingResult = bindingResult;
	}
}
