package com.worth.ifs.invite.resource;

import com.worth.ifs.invite.constant.InviteStatusConstants;
import com.worth.ifs.invite.domain.Invite;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/*
* InviteResource is a DTO which enables to application to transfer Invite entities.
* */

public class InviteResource {
    private String leadOrganisation;
    private String leadApplicant;
    private String leadApplicantEmail;
    private Long id;
    private String name;
    private String email;
    private Long application;
    private Long competitionId;
    private String competitionName;
    private String applicationName;
    private Long inviteOrganisation;
    private String inviteOrganisationName;
    private String hash;
    private InviteStatusConstants status;

    public InviteResource() {
    }


    public InviteResource(Long id, String name, String email, Long application, Long inviteOrganisation, String hash, InviteStatusConstants status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.application = application;
        this.inviteOrganisation = inviteOrganisation;
        this.hash = hash;
        this.status = status;
    }

    public InviteResource(String name, String email, Long application) {
        this.name = name;
        this.email = email;
        this.application = application;
    }

    public InviteResource(Invite i) {
        this.id = i.getId();
        this.name = i.getName();
        this.email = i.getEmail();
        this.application = i.getApplication().getId();
        this.applicationName = i.getApplication().getName();
        this.competitionId = i.getApplication().getCompetition().getId();
        this.competitionName = i.getApplication().getCompetition().getName();
        this.leadOrganisation = i.getApplication().getLeadOrganisation().getName();
        this.leadApplicant = i.getApplication().getLeadApplicant().getName();
        this.inviteOrganisation = i.getInviteOrganisation().getId();
        this.inviteOrganisationName = i.getInviteOrganisation().getOrganisationName();
        this.hash = i.getHash();
        this.status = i.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getApplication() {
        return application;
    }

    public void setApplication(Long application) {
        this.application = application;
    }

    public Long getInviteOrganisation() {
        return inviteOrganisation;
    }

    public void setInviteOrganisation(Long inviteOrganisation) {
        this.inviteOrganisation = inviteOrganisation;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public InviteStatusConstants getStatus() {
        return status;
    }

    public void setStatus(InviteStatusConstants status) {
        this.status = status;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getLeadOrganisation() {
        return leadOrganisation;
    }

    public void setLeadOrganisation(String leadOrganisation) {
        this.leadOrganisation = leadOrganisation;
    }

    public String getLeadApplicant() {
        return leadApplicant;
    }

    public void setLeadApplicant(String leadApplicant) {
        this.leadApplicant = leadApplicant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InviteResource that = (InviteResource) o;

        return new EqualsBuilder()
                .append(leadOrganisation, that.leadOrganisation)
                .append(leadApplicant, that.leadApplicant)
                .append(id, that.id)
                .append(name, that.name)
                .append(email, that.email)
                .append(application, that.application)
                .append(competitionId, that.competitionId)
                .append(competitionName, that.competitionName)
                .append(applicationName, that.applicationName)
                .append(inviteOrganisation, that.inviteOrganisation)
                .append(hash, that.hash)
                .append(status, that.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(leadOrganisation)
                .append(leadApplicant)
                .append(id)
                .append(name)
                .append(email)
                .append(application)
                .append(competitionId)
                .append(competitionName)
                .append(applicationName)
                .append(inviteOrganisation)
                .append(hash)
                .append(status)
                .toHashCode();
    }

    public String getInviteOrganisationName() {
        return inviteOrganisationName;
    }

    public void setInviteOrganisationName(String inviteOrganisationName) {
        this.inviteOrganisationName = inviteOrganisationName;
    }

    public String getLeadApplicantEmail() {
        return leadApplicantEmail;
    }

    public void setLeadApplicantEmail(String leadApplicantEmail) {
        this.leadApplicantEmail = leadApplicantEmail;
    }
}
