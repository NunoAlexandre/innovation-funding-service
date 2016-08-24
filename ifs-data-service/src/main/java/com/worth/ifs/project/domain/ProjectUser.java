package com.worth.ifs.project.domain;

import com.worth.ifs.invite.domain.Participant;
import com.worth.ifs.invite.domain.ProjectInvite;
import com.worth.ifs.invite.domain.ProjectParticipantRole;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.User;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Optional;

/**
 * ProjectUser defines a User's role on a Project and in relation to a particular Organisation.
 */
@Entity
public class ProjectUser extends Participant<Project, ProjectInvite, ProjectParticipantRole> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "projectId", referencedColumnName = "id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectParticipantRole role;

    @ManyToOne
    @JoinColumn(name = "organisationId", referencedColumnName = "id")
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "invite_id", referencedColumnName = "id")
    private ProjectInvite invite;

    public ProjectUser() {
        // no-arg constructor
    }

    public ProjectUser(Long id, User user, Project project, ProjectParticipantRole role, Organisation organisation) {
        this.id = id;
        this.user = user;
        this.project = project;
        this.role = role;
        this.organisation = organisation;
    }

    public ProjectUser(User user, Project project, ProjectParticipantRole role, Organisation organisation) {
        this.user = user;
        this.project = project;
        this.role = role;
        this.organisation = organisation;
    }

    @Override
    public Optional<ProjectInvite> getInvite() {
        return Optional.ofNullable(invite);
    }

    @Override
    public ProjectParticipantRole getRole() {
        return role;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Project getProcess() {
        return project;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public Long getId() {
        return id;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPartner() {
        return getRole().isPartner();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectUser that = (ProjectUser) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(user, that.user)
                .append(project, that.project)
                .append(role, that.role)
                .append(organisation, that.organisation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(user)
                .append(project)
                .append(role)
                .append(organisation)
                .toHashCode();
    }
}
