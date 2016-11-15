package com.worth.ifs.user.resource;

public enum Gender {
    FEMALE(1L, "Female"),
    MALE(2L, "Male"),
    NOT_STATED(3L, "Prefer not to say");

    private String displayName;
    private Long id;

    Gender(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getId() {
        return id;
    }
}
