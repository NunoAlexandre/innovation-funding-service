package com.worth.ifs.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.request.WebRequest;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Map;

/**
 * This class is used to convert our custom implementation of the date fields, to the LocalDate object.
 * This way, the submitted fields, can be cast / injected into a domain object.
 * We use this to save form fields, into the ApplicationForm object, for example properties of the domain.Application object.
 *
 * One other way to remove this class, would be to merge the day/month/year value into 1 form-input element.
 */
public class LocalDatePropertyEditor extends PropertyEditorSupport {
    private static final Log LOG = LogFactory.getLog(LocalDatePropertyEditor.class);
    private WebRequest webRequest;

    public LocalDatePropertyEditor(WebRequest webRequest) {
        this.webRequest = webRequest;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Map<String, String[]> parameterMap = webRequest.getParameterMap();

        // should validate these...
        Integer year = returnZeroWhenNotValid(parameterMap, "application.startDate.year", ChronoField.YEAR);
        Integer month = returnZeroWhenNotValid(parameterMap, "application.startDate.monthValue", ChronoField.MONTH_OF_YEAR);
        Integer day = returnZeroWhenNotValid(parameterMap, "application.startDate.dayOfMonth", ChronoField.DAY_OF_MONTH);

        try {
            setValue(LocalDate.of(year, month, day));
        } catch (Exception ex) {
            LOG.error(ex);
            setValue(null);
        }
    }

    private Integer returnZeroWhenNotValid(Map<String, String[]> parameterMap, String parameterName, ChronoField chronoField) {
        try {
            return chronoField.checkValidIntValue(Long.valueOf(parameterMap.get(parameterName)[0]));
        } catch (Exception e){
            LOG.error(e);
            return (int)chronoField.range().getMinimum();
        }
    }
}