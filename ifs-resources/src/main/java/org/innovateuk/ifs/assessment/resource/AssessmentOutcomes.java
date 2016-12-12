package org.innovateuk.ifs.assessment.resource;

import org.innovateuk.ifs.workflow.resource.OutcomeType;

public enum AssessmentOutcomes implements OutcomeType {
    ACCEPT("accept"),
    REJECT("reject"),
    FEEDBACK("feedback"),
    FUNDING_DECISION("funding-decision"),
    SUBMIT("submit");

    String event;

    AssessmentOutcomes(String event) {
        this.event = event;
    }

    @Override
    public String getType() {
        return event;
    }
}
