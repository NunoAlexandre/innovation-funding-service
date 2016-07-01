package com.worth.ifs.workflow.resource;

import javax.persistence.Entity;

/**
 * The process events should be represented by a named event.
 * These are used to progress through the workflow.
 */
public interface OutcomeType {
    String getType();
}
