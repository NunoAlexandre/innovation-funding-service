package org.innovateuk.ifs.finance.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.project.finance.resource.EligibilityRagStatus;
import org.innovateuk.ifs.project.finance.resource.ViabilityRagStatus;
import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.OrganisationSize;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

/**
 * Entity object similar to ApplicationFinance for storing values in finance_row tables which can be edited by
 * internal project finance users.  It also holds organisation size because internal users will be allowed to edit
 * organisation size as well.
 */
@Entity
public class ProjectFinance extends Finance {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="projectId", referencedColumnName="id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="viabilityApprovalUserId", referencedColumnName="id")
    private User viabilityApprovalUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="eligibilityApprovalUserId", referencedColumnName="id")
    private User eligibilityApprovalUser;

    private boolean creditReportConfirmed = false;

    @Enumerated(EnumType.STRING)
    private ViabilityRagStatus viabilityStatus = ViabilityRagStatus.UNSET;

    @Enumerated(EnumType.STRING)
    private EligibilityRagStatus eligibilityStatus = EligibilityRagStatus.UNSET;

    private LocalDate viabilityApprovalDate;

    private LocalDate eligibilityApprovalDate;

    public ProjectFinance() {
    }

    public ProjectFinance(Organisation organisation, OrganisationSize organisationSize, Project project) {
        super(organisation, organisationSize);
        this.project = project;
    }

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getViabilityApprovalUser() {
        return viabilityApprovalUser;
    }

    public void setViabilityApprovalUser(User viabilityApprovalUser) {
        this.viabilityApprovalUser = viabilityApprovalUser;
    }

    public boolean getCreditReportConfirmed() { return creditReportConfirmed; }

    public void setCreditReportConfirmed(boolean creditReportConfirmed) { this.creditReportConfirmed = creditReportConfirmed; }

    public ViabilityRagStatus getViabilityStatus() {
        return viabilityStatus;
    }

    public void setViabilityStatus(ViabilityRagStatus viabilityStatus) {
        this.viabilityStatus = viabilityStatus;
    }

    public LocalDate getViabilityApprovalDate() {
        return viabilityApprovalDate;
    }

    public void setViabilityApprovalDate(LocalDate viabilityApprovalDate) {
        this.viabilityApprovalDate = viabilityApprovalDate;
    }

    public User getEligibilityApprovalUser() {
        return eligibilityApprovalUser;
    }

    public void setEligibilityApprovalUser(User eligibilityApprovalUser) {
        this.eligibilityApprovalUser = eligibilityApprovalUser;
    }

    public EligibilityRagStatus getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(EligibilityRagStatus eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public LocalDate getEligibilityApprovalDate() {
        return eligibilityApprovalDate;
    }

    public void setEligibilityApprovalDate(LocalDate eligibilityApprovalDate) {
        this.eligibilityApprovalDate = eligibilityApprovalDate;
    }
}
