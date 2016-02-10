package com.worth.ifs.form.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.BaseControllerIntegrationTest;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.form.domain.FormInputResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static com.worth.ifs.commons.error.Errors.forbiddenError;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FormInputResponseControllerIntegrationTest extends BaseControllerIntegrationTest<FormInputResponseController> {

    @Override
    @Autowired
    protected void setControllerUnderTest(FormInputResponseController controller) {
        this.controller = controller;
    }

    @Test
    @Rollback
    public void test_findResponsesByApplication(){
        Long applicationId = 1L;
        Long formInputId = 1L;
        List<FormInputResponse> responses = controller.findResponsesByApplication(applicationId).getSuccessObject();

        assertThat(responses, hasSize(16));

        Optional<FormInputResponse> response = responses.stream().filter(r -> r.getFormInput().getId().equals(formInputId)).findFirst();

        assertTrue(response.isPresent());
        assertThat(response.get().getValue(), containsString("Within the Industry one issue has caused"));
    }


    @Test
    @Rollback
    public void test_saveNotAllowed() {

        Long applicationId = 1L;
        Long formInputId = 1L;
        String inputValue = "NOT ALLOWED";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("userId", Long.MAX_VALUE);
        jsonObj.put("applicationId", applicationId);
        jsonObj.put("formInputId", formInputId);
        jsonObj.put("value", inputValue);

        RestResult<List<String>> result = controller.saveQuestionResponse(jsonObj);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(forbiddenError("Unable to update question response")));

        List<FormInputResponse> responses = controller.findResponsesByApplication(applicationId).getSuccessObject();
        Optional<FormInputResponse> response = responses.stream().filter(r -> r.getFormInput().getId().equals(formInputId)).findFirst();

        assertTrue(response.isPresent());
        assertNotEquals(inputValue, response.get().getValue());
        assertThat(response.get().getValue(), containsString("Within the Industry one issue has caused"));
    }

    @Test
    @Rollback
    public void test_saveInvalidQuestionResponse() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("userId", 1);
        jsonObj.put("applicationId", 1);
        jsonObj.put("formInputId", 1);
        jsonObj.put("value", "");

        List<String> errors = controller.saveQuestionResponse(jsonObj).getSuccessObject();
        assertThat(errors, hasSize(1));
        assertThat(errors, hasItem("Please enter some text"));
    }

    @Test
    @Rollback
    public void test_saveWordCountExceedingQuestionResponse() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("userId", 1);
        jsonObj.put("applicationId", 1);
        jsonObj.put("formInputId", 1);

        String value = "";
        for(int i=0; i<=501; i++) {
            value+=" word";
        }

        jsonObj.put("value", value);

        List<String> errors = controller.saveQuestionResponse(jsonObj).getSuccessObject();
        assertThat(errors, hasSize(1));
        assertThat(errors, hasItem("Maximum word count exceeded"));
    }

    @Test
    @Rollback
    public void test_saveQuestionResponse() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("userId", 1);
        jsonObj.put("applicationId", 1);
        jsonObj.put("formInputId", 1);
        jsonObj.put("value", "Some text value...");

        List<String> errors = controller.saveQuestionResponse(jsonObj).getSuccessObject();
        assertThat(errors, hasSize(0));
    }
}
