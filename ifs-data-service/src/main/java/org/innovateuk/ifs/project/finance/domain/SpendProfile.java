package org.innovateuk.ifs.project.finance.domain;

import org.innovateuk.ifs.project.domain.Project;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;

import javax.persistence.*;
import java.util.Calendar;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

/**
 * Entity representing the Spend Profile of a Partner Organisation on a Project
 */
@Entity
public class SpendProfile {

    public static final String ELIGIBLE_COSTS_DESCRIPTION = "Eligible costs for Partner Organisation";
    public static final String SPEND_PROFILE_DESCRIPTION = "Spend Profile figures for Partner Organisation";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    private Organisation organisation;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private CostCategoryType costCategoryType;

    @ManyToOne(cascade = ALL, optional = false)
    @JoinColumn(name = "eligible_costs_cost_group_id")
    private CostGroup eligibleCosts;

    @ManyToOne(cascade = ALL, optional = false)
    @JoinColumn(name = "spend_profile_figures_cost_group_id")
    private CostGroup spendProfileFigures;

    @ManyToOne(optional = false)
    private User generatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar generatedDate;

    private boolean markedAsComplete;

    @Enumerated(STRING)
    private ApprovalType approval;

    public SpendProfile() {
        // for ORM use
    }

    public SpendProfile(Organisation organisation, Project project, CostCategoryType costCategoryType, List<Cost> eligibleCosts, List<Cost> spendProfileFigures, User generatedBy, Calendar generatedDate, boolean markedAsComplete, ApprovalType approval) {
        this.organisation = organisation;
        this.project = project;
        this.costCategoryType = costCategoryType;
        this.eligibleCosts = new CostGroup(ELIGIBLE_COSTS_DESCRIPTION, eligibleCosts);
        this.spendProfileFigures = new CostGroup(SPEND_PROFILE_DESCRIPTION, spendProfileFigures);
        this.generatedBy = generatedBy;
        this.generatedDate = generatedDate;
        this.markedAsComplete = markedAsComplete;
        this.approval = approval;
    }

    public Long getId() {
        return id;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public Project getProject() {
        return project;
    }

    public CostCategoryType getCostCategoryType() {
        return costCategoryType;
    }

    public CostGroup getEligibleCosts() {
        return eligibleCosts;
    }

    public CostGroup getSpendProfileFigures() {
        return spendProfileFigures;
    }

    public boolean isMarkedAsComplete() {
        return markedAsComplete;
    }

    public void setMarkedAsComplete(boolean markedAsComplete) {
        this.markedAsComplete = markedAsComplete;
    }

    public User getGeneratedBy() {
        return generatedBy;
    }

    public Calendar getGeneratedDate() {
        return generatedDate;
    }

    public ApprovalType getApproval() {
        return approval;
    }

    public void setApproval(ApprovalType approval) {
        this.approval = approval;
    }
}
