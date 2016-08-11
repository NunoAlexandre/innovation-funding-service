package com.worth.ifs.bankdetails.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BankDetailsReviewViewModel {
    private String projectNumber;
    private String projectName;
    private String financeContactName;
    private String financeContactEmail;
    private String financeContactPhoneNumber;
    private String organisationName;
    private String registrationNumber;
    private String bankAccountNumber;
    private String sortCode;
    private String organisationAddress;
    private Boolean verified;
    private Short companyNameScore;
    private Boolean registrationNumberMatched;
    private Short addressScore;
    private Boolean approved;
    private Boolean approvedManually;

    public BankDetailsReviewViewModel(String projectNumber, String projectName, String financeContactName, String financeContactEmail, String financeContactPhoneNumber, String organisationName, String registrationNumber, String bankAccountNumber, String sortCode, String organisationAddress, Boolean verified, Short companyNameScore, Boolean registrationNumberMatched, Short addressScore, Boolean approved, Boolean approvedManually) {
        this.projectNumber = projectNumber;
        this.projectName = projectName;
        this.financeContactName = financeContactName;
        this.financeContactEmail = financeContactEmail;
        this.financeContactPhoneNumber = financeContactPhoneNumber;
        this.organisationName = organisationName;
        this.registrationNumber = registrationNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.sortCode = sortCode;
        this.organisationAddress = organisationAddress;
        this.verified = verified;
        this.companyNameScore = companyNameScore;
        this.registrationNumberMatched = registrationNumberMatched;
        this.addressScore = addressScore;
        this.approved = approved;
        this.approvedManually = approvedManually;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getFinanceContactName() {
        return financeContactName;
    }

    public void setFinanceContactName(String financeContactName) {
        this.financeContactName = financeContactName;
    }

    public String getFinanceContactEmail() {
        return financeContactEmail;
    }

    public void setFinanceContactEmail(String financeContactEmail) {
        this.financeContactEmail = financeContactEmail;
    }

    public String getFinanceContactPhoneNumber() {
        return financeContactPhoneNumber;
    }

    public void setFinanceContactPhoneNumber(String financeContactPhoneNumber) {
        this.financeContactPhoneNumber = financeContactPhoneNumber;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }

    public String getOrganisationAddress() {
        return organisationAddress;
    }

    public void setOrganisationAddress(String organisationAddress) {
        this.organisationAddress = organisationAddress;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Short getCompanyNameScore() {
        return companyNameScore;
    }

    public void setCompanyNameScore(Short companyNameScore) {
        this.companyNameScore = companyNameScore;
    }

    public Boolean getRegistrationNumberMatched() {
        return registrationNumberMatched;
    }

    public void setRegistrationNumberMatched(Boolean registrationNumberMatched) {
        this.registrationNumberMatched = registrationNumberMatched;
    }

    public Short getAddressScore() {
        return addressScore;
    }

    public void setAddressScore(Short addressScore) {
        this.addressScore = addressScore;
    }

    public Boolean getApprovedManually() {
        return approvedManually;
    }

    public void setApprovedManually(Boolean approvedManually) {
        this.approvedManually = approvedManually;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BankDetailsReviewViewModel that = (BankDetailsReviewViewModel) o;

        return new EqualsBuilder()
                .append(financeContactName, that.financeContactName)
                .append(financeContactEmail, that.financeContactEmail)
                .append(financeContactPhoneNumber, that.financeContactPhoneNumber)
                .append(organisationName, that.organisationName)
                .append(registrationNumber, that.registrationNumber)
                .append(bankAccountNumber, that.bankAccountNumber)
                .append(sortCode, that.sortCode)
                .append(organisationAddress, that.organisationAddress)
                .append(verified, that.verified)
                .append(companyNameScore, that.companyNameScore)
                .append(registrationNumberMatched, that.registrationNumberMatched)
                .append(addressScore, that.addressScore)
                .append(approved, that.approved)
                .append(approvedManually, that.approvedManually)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(financeContactName)
                .append(financeContactEmail)
                .append(financeContactPhoneNumber)
                .append(organisationName)
                .append(registrationNumber)
                .append(bankAccountNumber)
                .append(sortCode)
                .append(organisationAddress)
                .append(verified)
                .append(companyNameScore)
                .append(registrationNumberMatched)
                .append(addressScore)
                .append(approved)
                .append(approvedManually)
                .toHashCode();
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }
}
