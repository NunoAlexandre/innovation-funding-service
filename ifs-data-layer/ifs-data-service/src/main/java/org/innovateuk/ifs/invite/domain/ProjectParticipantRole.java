package org.innovateuk.ifs.invite.domain;

import org.innovateuk.ifs.project.domain.Project;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * The role of {@link org.innovateuk.ifs.project.domain.ProjectUser}.
 */
public enum ProjectParticipantRole implements ParticipantRole<Project> {
    PROJECT_PARTNER(10, "partner"),
    PROJECT_MANAGER(11, "project_manager"),
    PROJECT_FINANCE_CONTACT(9, "finance_contact");

    private final long id;
    private final String name;

    private static final Map<Long, ProjectParticipantRole> idMap =
            stream(values()).collect(toMap(ProjectParticipantRole::getId, identity()));

    ProjectParticipantRole(final long id, final String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public boolean isPartner() {
        return this == PROJECT_PARTNER;
    }

    public boolean isProjectManager() {
        return this == PROJECT_MANAGER;
    }

    public boolean isFinanceContact() {
        return this == PROJECT_FINANCE_CONTACT;
    }

    public static ProjectParticipantRole getById(Long id) {
        return idMap.get(id);
    }
}
