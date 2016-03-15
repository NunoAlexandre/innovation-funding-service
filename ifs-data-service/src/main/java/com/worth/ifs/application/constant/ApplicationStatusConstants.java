package com.worth.ifs.application.constant;


/**
 * Java enumeration of the current available Application workflow statuses.
 * The value of these entries are used when saving to the database.
 */
public enum ApplicationStatusConstants {
    CREATED(1L, "created"), // initial state
    SUBMITTED(2L, "submitted"),
    APPROVED(3L, "approved"),
    REJECTED(4L, "rejected"),
    OPEN(5L, "open"); // state after first time opening application.

    private final Long id;
    private final String name;

    ApplicationStatusConstants(Long id, String name){
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
