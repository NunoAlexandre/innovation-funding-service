package com.worth.ifs.application.domain;

import com.worth.ifs.application.constant.ApplicationStatusConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * ApplicationStatus defines database relations and a model to use client side and server side.
 */
@Entity
public class ApplicationStatus {

    public ApplicationStatus() {
    	// no-arg constructor
    }

    public ApplicationStatus(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

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

    public boolean isApproved() {
        return ApplicationStatusConstants.APPROVED.getId().equals(getId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
