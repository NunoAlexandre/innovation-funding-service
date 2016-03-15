package com.worth.ifs.user.resource;

import java.util.ArrayList;
import java.util.List;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.Response;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;

public class ProcessRoleResource {
    private Long id;
    private User user;
    private Long application;
    private Long role;
    private Long organisation;
    private List<Response> responses = new ArrayList<>();

    public ProcessRoleResource(){}

    public ProcessRoleResource(Long id, User user, Application application, Role role, Organisation organisation
    ) {
        this.id = id;
        this.user = user;
        this.application = application.getId();
        this.role = role.getId();
        this.organisation = organisation.getId();
    }

    public Long getId(){return id;}

    public User getUser() {
        return user;
    }

    public Long getRole() {
        return role;
    }

    public Long getOrganisation() {
        return organisation;
    }

    public Long getApplication() {
        return this.application;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setApplication(Long application) {
        this.application = application;
    }

    public void setRole(Long role) {
        this.role = role;
    }

    public void setOrganisation(Long organisation) {
        this.organisation = organisation;
    }

    public List<Response> getResponses() {
        return this.responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
