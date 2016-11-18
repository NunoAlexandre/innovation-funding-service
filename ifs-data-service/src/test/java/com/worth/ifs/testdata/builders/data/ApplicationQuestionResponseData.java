package com.worth.ifs.testdata.builders.data;

import com.worth.ifs.application.resource.ApplicationResource;

/**
 * Running data context for generating responses to application form questions
 */
public class ApplicationQuestionResponseData {

    private String questionName;
    private ApplicationResource application;

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setApplication(ApplicationResource application) {
        this.application = application;
    }

    public ApplicationResource getApplication() {
        return application;
    }
}
