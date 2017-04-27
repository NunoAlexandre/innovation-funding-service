package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.user.resource.OrganisationResource;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * View model extending the {@link BaseSectionViewModel} for open sections (not finance, but used by finances overview)
 */
public class OpenSectionViewModel extends BaseSectionViewModel {
    private Set<OrganisationResource> academicOrganisations;
    private Set<OrganisationResource> applicationOrganisations;
    private List<String> pendingOrganisationNames;
    private OrganisationResource leadOrganisation;

    private Map<Long, Set<Long>> completedSectionsByOrganisation;
    private Long eachCollaboratorFinanceSectionId;

    private Integer completedQuestionsPercentage;

    public OpenSectionViewModel() {
        subFinanceSection = Boolean.FALSE;
    }

    public Set<OrganisationResource> getAcademicOrganisations() {
        return academicOrganisations;
    }

    public void setAcademicOrganisations(Set<OrganisationResource> academicOrganisations) {
        this.academicOrganisations = academicOrganisations;
    }

    public Set<OrganisationResource> getApplicationOrganisations() {
        return applicationOrganisations;
    }

    public void setApplicationOrganisations(Set<OrganisationResource> applicationOrganisations) {
        this.applicationOrganisations = applicationOrganisations;
    }

    public List<String> getPendingOrganisationNames() {
        return pendingOrganisationNames;
    }

    public void setPendingOrganisationNames(List<String> pendingOrganisationNames) {
        this.pendingOrganisationNames = pendingOrganisationNames;
    }

    public OrganisationResource getLeadOrganisation() {
        return leadOrganisation;
    }

    public void setLeadOrganisation(OrganisationResource leadOrganisation) {
        this.leadOrganisation = leadOrganisation;
    }

    public Map<Long, Set<Long>> getCompletedSectionsByOrganisation() {
        return completedSectionsByOrganisation;
    }

    public void setCompletedSectionsByOrganisation(Map<Long, Set<Long>> completedSectionsByOrganisation) {
        this.completedSectionsByOrganisation = completedSectionsByOrganisation;
    }

    public Set<Long> getSectionsMarkedAsComplete() {
        return sectionsMarkedAsComplete;
    }

    public void setSectionsMarkedAsComplete(Set<Long> sectionsMarkedAsComplete) {
        this.sectionsMarkedAsComplete = sectionsMarkedAsComplete;
    }

    public Long getEachCollaboratorFinanceSectionId() {
        return eachCollaboratorFinanceSectionId;
    }

    public void setEachCollaboratorFinanceSectionId(Long eachCollaboratorFinanceSectionId) {
        this.eachCollaboratorFinanceSectionId = eachCollaboratorFinanceSectionId;
    }

    public Integer getCompletedQuestionsPercentage() {
        return completedQuestionsPercentage;
    }

    public void setCompletedQuestionsPercentage(Integer completedQuestionsPercentage) {
        this.completedQuestionsPercentage = completedQuestionsPercentage;
    }
}
