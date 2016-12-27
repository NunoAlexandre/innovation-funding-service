package org.innovateuk.ifs.project.gol.resource;

import org.innovateuk.ifs.workflow.resource.OutcomeType;

/**
 * Represents the events that can be triggered during the Grant Offer Letter process.
 */
public enum GOLOutcomes implements OutcomeType {

    PROJECT_CREATED("project-created"),
    GOL_REMOVED("gol-removed"),
    GOL_SENT("gol-sent"),
    GOL_SIGNED("gol-signed"),
    GOL_APPROVED("gol-approved"),
    GOL_REJECTED("gol-rejected");

    String event;

    GOLOutcomes(String event) {
        this.event = event;
    }

    @Override
    public String getType() {
        return event;
    }
}
