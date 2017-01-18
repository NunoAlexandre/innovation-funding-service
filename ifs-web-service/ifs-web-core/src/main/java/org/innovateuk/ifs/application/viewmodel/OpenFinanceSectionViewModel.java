package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.user.resource.UserResource;

/**
 * ViewModel for Finance open sections
 */
public class OpenFinanceSectionViewModel extends BaseSectionViewModel {

    private boolean fundingSectionLocked;
    private Long applicationDetailsQuestionId;
    private Long yourOrganisationSectionId;
    private String uploadedOverheadFileName;
    private Long uploadedOverheadFileId;
    private boolean notRequestingFunding;


    public OpenFinanceSectionViewModel(NavigationViewModel navigationViewModel, SectionResource currentSection,
                                       Boolean hasFinanceSection, Long financeSectionId, UserResource currentUser,
                                       Boolean subFinanceSection) {
        this.navigationViewModel = navigationViewModel;
        this.currentSection = currentSection;
        this.hasFinanceSection = hasFinanceSection;
        this.financeSectionId = financeSectionId;
        this.currentUser = currentUser;
        this.subFinanceSection = subFinanceSection;
    }

    public boolean isFundingSectionLocked() {
        return fundingSectionLocked;
    }

    public void setFundingSectionLocked(boolean fundingSectionLocked) {
        this.fundingSectionLocked = fundingSectionLocked;
    }

    public Long getApplicationDetailsQuestionId() {
        return applicationDetailsQuestionId;
    }

    public void setApplicationDetailsQuestionId(Long applicationDetailsQuestionId) {
        this.applicationDetailsQuestionId = applicationDetailsQuestionId;
    }

    public Long getYourOrganisationSectionId() {
        return yourOrganisationSectionId;
    }

    public void setYourOrganisationSectionId(Long yourOrganisationSectionId) {
        this.yourOrganisationSectionId = yourOrganisationSectionId;
    }

    public String getUploadedOverheadFileName() {
        return uploadedOverheadFileName;
    }

    public void setUploadedOverheadFileName(String uploadedOverheadFileName) {
        this.uploadedOverheadFileName = uploadedOverheadFileName;
    }

    public Long getUploadedOverheadFileId() {
        return uploadedOverheadFileId;
    }

    public void setUploadedOverheadFileId(Long uploadedOverheadFileId) {
        this.uploadedOverheadFileId = uploadedOverheadFileId;
    }

    public boolean isNotRequestingFunding() {
        return notRequestingFunding;
    }

    public void setNotRequestingFunding(boolean notRequestingFunding) {
        this.notRequestingFunding = notRequestingFunding;
    }

    /* Your finances display logic */
    public boolean showSectionAsNotRequired(SectionResource subSection) {
        return notRequestingFunding && (SectionType.ORGANISATION_FINANCES.equals(subSection.getType())
            || SectionType.FUNDING_FINANCES.equals(subSection.getType()));
    }

    public boolean showSectionAsLockedFunding(SectionResource subSection) {
        return !showSectionAsNotRequired(subSection) && SectionType.FUNDING_FINANCES.equals(subSection.getType())
                && fundingSectionLocked;
    }

    public boolean showSectionAsLink(SectionResource subSection) {
        return !showSectionAsLockedFunding(subSection);

    }

    public boolean showSectionStatus(SectionResource subSection) {
        return showSectionAsLink(subSection);
    }
}
