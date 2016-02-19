package com.worth.ifs.finance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.OrganisationSize;

import javax.persistence.*;

/**
 * ApplicationFinance defines database relations and a model to use client side and server side.
 */
@Entity
public class ApplicationFinance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name="organisationId", referencedColumnName="id")
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name="applicationId", referencedColumnName="id")
    private Application application;

    @Enumerated(EnumType.STRING)
    private OrganisationSize organisationSize;

    public ApplicationFinance() {
    }

    public ApplicationFinance(Application application, Organisation organisation) {
        this.application = application;
        this.organisation = organisation;
    }

    public ApplicationFinance(long id, Application application, Organisation organisation) {
        this.id = id;
        this.application = application;
        this.organisation = organisation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    @JsonIgnore
    public Application getApplication() {
        return application;
    }

    public OrganisationSize getOrganisationSize() {
        return organisationSize;
    }

    public void setOrganisationSize(OrganisationSize organisationSize) {
        this.organisationSize = organisationSize;
    }

    public void merge(ApplicationFinanceResource applicationFinance) {
        this.setOrganisationSize(applicationFinance.getOrganisationSize());
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
