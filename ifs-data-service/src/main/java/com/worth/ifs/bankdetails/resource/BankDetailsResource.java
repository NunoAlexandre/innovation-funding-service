package com.worth.ifs.bankdetails.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class BankDetailsResource {

    private Long id;

    @NotBlank (message = "Sort code is mandatory")
    @Pattern(regexp="\\d{6}", message = "Please enter a valid 6 digit sort code")
    private String sortCode;

    @NotBlank (message = "Account number is mandatory")
    @Pattern(regexp="\\d{8}", message = "Please enter a valid 8 digit account number")
    private String accountNumber;

    @NotNull(message = "Project id is mandatory")
    private Long project;

    @NotNull(message = "Organisation Address is mandatory")
    private OrganisationAddressResource organisationAddress;

    @NotNull(message = "Organisation id is mandatory")
    private Long organisation;

    private String organisationTypeName;

    private String companyName;

    private String registrationNumber;

    private short companyNameScore;

    private boolean registrationNumberMatched;

    private short addressScore;

    private boolean manualApproval;

    private boolean verified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }

    public OrganisationAddressResource getOrganisationAddress() {
        return organisationAddress;
    }

    public void setOrganisationAddress(OrganisationAddressResource organisationAddressResource) {
        this.organisationAddress = organisationAddressResource;
    }

    public Long getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Long organisation) {
        this.organisation = organisation;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @JsonIgnore
    public boolean isApproved(){
        return manualApproval || (verified && registrationNumberMatched && companyNameScore > 6 && addressScore > 6);
    }

    public String getOrganisationTypeName() {
        return organisationTypeName;
    }

    public void setOrganisationTypeName(String organisationTypeName) {
        this.organisationTypeName = organisationTypeName;
    }

    public short getCompanyNameScore() {
        return companyNameScore;
    }

    public void setCompanyNameScore(short companyNameScore) {
        this.companyNameScore = companyNameScore;
    }

    public boolean getRegistrationNumberMatched() {
        return registrationNumberMatched;
    }

    public void setRegistrationNumberMatched(boolean registrationNumberMatched) {
        this.registrationNumberMatched = registrationNumberMatched;
    }

    public short getAddressScore() {
        return addressScore;
    }

    public void setAddressScore(short addressScore) {
        this.addressScore = addressScore;
    }

    public boolean isManualApproval() {
        return manualApproval;
    }

    public void setManualApproval(boolean manualApproval) {
        this.manualApproval = manualApproval;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BankDetailsResource that = (BankDetailsResource) o;

        return new EqualsBuilder()
                .append(companyNameScore, that.companyNameScore)
                .append(registrationNumberMatched, that.registrationNumberMatched)
                .append(addressScore, that.addressScore)
                .append(manualApproval, that.manualApproval)
                .append(verified, that.verified)
                .append(id, that.id)
                .append(sortCode, that.sortCode)
                .append(accountNumber, that.accountNumber)
                .append(project, that.project)
                .append(organisationAddress, that.organisationAddress)
                .append(organisation, that.organisation)
                .append(organisationTypeName, that.organisationTypeName)
                .append(companyName, that.companyName)
                .append(registrationNumber, that.registrationNumber)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(sortCode)
                .append(accountNumber)
                .append(project)
                .append(organisationAddress)
                .append(organisation)
                .append(organisationTypeName)
                .append(companyName)
                .append(registrationNumber)
                .append(companyNameScore)
                .append(registrationNumberMatched)
                .append(addressScore)
                .append(manualApproval)
                .append(verified)
                .toHashCode();
    }
}
