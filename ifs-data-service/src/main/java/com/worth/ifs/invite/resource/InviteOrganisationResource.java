package com.worth.ifs.invite.resource;

import com.worth.ifs.invite.domain.InviteOrganisation;
import com.worth.ifs.user.domain.Organisation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.stream.Collectors;

/*
* InviteOrganisationResource is a DTO which enables the application to transfer InviteOrganisation entities.
* */

public class InviteOrganisationResource {
    private Long id;
    private String organisationName;
    private Long organisation;

    List<InviteResource> inviteResources;

    public InviteOrganisationResource() {

    }

    public InviteOrganisationResource(Long id, String organisationName, Organisation organisation, List<InviteResource> inviteResources) {
        this.id = id;
        this.organisationName = organisationName;
        this.organisation = organisation.getId();
        this.inviteResources = inviteResources;
    }

    public InviteOrganisationResource(InviteOrganisation invite) {
        this.id = invite.getId();
        this.organisationName = invite.getOrganisationName();
        if(invite.getOrganisation() != null && invite.getOrganisation().getId() != null){
            this.organisation = invite.getOrganisation().getId();
        }
        this.setInviteResources(invite.getInvites().stream().map(i -> new InviteResource(i)).collect(Collectors.toList()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }
    public List<InviteResource> getInviteResources() {
        return inviteResources;
    }

    public void setInviteResources(List<InviteResource> inviteResources) {
        this.inviteResources = inviteResources;
    }

    public Long getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Long organisation) {
        this.organisation = organisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InviteOrganisationResource that = (InviteOrganisationResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(organisationName, that.organisationName)
                .append(organisation, that.organisation)
                .append(inviteResources, that.inviteResources)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(organisationName)
                .append(organisation)
                .append(inviteResources)
                .toHashCode();
    }
}
