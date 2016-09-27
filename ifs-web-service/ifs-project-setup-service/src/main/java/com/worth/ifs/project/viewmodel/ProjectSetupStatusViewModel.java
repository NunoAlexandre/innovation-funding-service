package com.worth.ifs.project.viewmodel;

import com.worth.ifs.bankdetails.resource.BankDetailsResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.project.resource.MonitoringOfficerResource;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.sections.SectionAccess;

import java.util.Optional;

/**
 * A view model that backs the Project Status page
 */
public class ProjectSetupStatusViewModel implements BasicProjectDetailsViewModel {

    private Long projectId;
    private String projectName;
    private Long applicationId;
    private String competitionName;
    private boolean projectDetailsSubmitted;
    private boolean partnerDocumentsSubmitted;
    private boolean monitoringOfficerAssigned;
    private String monitoringOfficerName;
    private BankDetailsResource bankDetails;
    private Long organisationId;
    private boolean leadPartner;
    private SectionAccess companiesHouseSection;
    private SectionAccess projectDetailsSection;
    private SectionAccess monitoringOfficerSection;
    private SectionAccess bankDetailsSection;
    private SectionAccess financeChecksSection;
    private SectionAccess spendProfileSection;
    private SectionAccess otherDocumentsSection;
    private SectionAccess grantOfferLetterSection;

    public ProjectSetupStatusViewModel(ProjectResource project, CompetitionResource competition,
                                       Optional<MonitoringOfficerResource> monitoringOfficerResource,
                                       Optional<BankDetailsResource> bankDetails, Long organisationId,
                                       boolean projectDetailsSubmitted, boolean leadPartner,
                                       SectionAccess companiesHouseSection, SectionAccess projectDetailsSection,
                                       SectionAccess monitoringOfficerSection, SectionAccess bankDetailsSection,
                                       SectionAccess financeChecksSection, SectionAccess spendProfileSection,
                                       SectionAccess otherDocumentsSection, SectionAccess grantOfferLetterSection) {
        this.projectId = project.getId();
        this.projectName = project.getName();
        this.applicationId = project.getApplication();
        this.competitionName = competition.getName();
        this.projectDetailsSubmitted = projectDetailsSubmitted;
        this.partnerDocumentsSubmitted = project.isPartnerDocumentsSubmitted();
        this.monitoringOfficerAssigned = monitoringOfficerResource.isPresent();
        this.monitoringOfficerName = monitoringOfficerResource.map(mo -> mo.getFullName()).orElse("");
        this.bankDetails = bankDetails.orElse(null);
        this.organisationId = organisationId;
        this.leadPartner = leadPartner;
        this.companiesHouseSection = companiesHouseSection;
        this.projectDetailsSection = projectDetailsSection;
        this.monitoringOfficerSection = monitoringOfficerSection;
        this.bankDetailsSection = bankDetailsSection;
        this.financeChecksSection = financeChecksSection;
        this.spendProfileSection = spendProfileSection;
        this.otherDocumentsSection = otherDocumentsSection;
        this.grantOfferLetterSection = grantOfferLetterSection;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public boolean isProjectDetailsSubmitted() {
        return projectDetailsSubmitted;
    }

    public boolean isPartnerDocumentsSubmitted() {
        return partnerDocumentsSubmitted;
    }

    public boolean isMonitoringOfficerAssigned() {
        return monitoringOfficerAssigned;
    }

    public String getMonitoringOfficerName() {
        return monitoringOfficerName;
    }

    public BankDetailsResource getBankDetails() {
        return bankDetails;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public boolean isLeadPartner() {
        return leadPartner;
    }

    public boolean isNonLeadPartner() {
        return !isLeadPartner();
    }

    public SectionAccess getCompaniesHouseSection() {
        return companiesHouseSection;
    }

    public SectionAccess getProjectDetailsSection() {
        return projectDetailsSection;
    }

    public SectionAccess getMonitoringOfficerSection() {
        return monitoringOfficerSection;
    }

    public SectionAccess getBankDetailsSection() {
        return bankDetailsSection;
    }

    public SectionAccess getFinanceChecksSection() {
        return financeChecksSection;
    }

    public SectionAccess getSpendProfileSection() {
        return spendProfileSection;
    }

    public SectionAccess getOtherDocumentsSection() {
        return otherDocumentsSection;
    }

    public SectionAccess getGrantOfferLetterSection() {
        return grantOfferLetterSection;
    }
}
