package org.innovateuk.ifs.competition.viewmodel;

import org.innovateuk.ifs.competition.viewmodel.publiccontent.AbstractPublicSectionContentViewModel;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.SectionViewModel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * View model for the competition overview with the public content data
 */
public class CompetitionOverviewViewModel {
    private String competitionTitle;
    private LocalDateTime competitionOpenDate;
    private LocalDateTime competitionCloseDate;
    private Long competitionId;
    private String shortDescription;
    private String nonIfsUrl;
    private Boolean nonIfs;
    private List<SectionViewModel> allContentSections;
    private AbstractPublicSectionContentViewModel currentSection;

    public String getCompetitionTitle() {
        return competitionTitle;
    }

    public void setCompetitionTitle(String competitionTitle) {
        this.competitionTitle = competitionTitle;
    }

    public LocalDateTime getCompetitionOpenDate() {
        return competitionOpenDate;
    }

    public void setCompetitionOpenDate(LocalDateTime competitionOpenDate) {
        this.competitionOpenDate = competitionOpenDate;
    }

    public LocalDateTime getCompetitionCloseDate() {
        return competitionCloseDate;
    }

    public void setCompetitionCloseDate(LocalDateTime competitionCloseDate) {
        this.competitionCloseDate = competitionCloseDate;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public List<SectionViewModel> getAllContentSections() {
        return allContentSections;
    }

    public void setAllContentSections(List<SectionViewModel> allContentSections) {
        this.allContentSections = allContentSections;
    }

    public void setCurrentSection(AbstractPublicSectionContentViewModel currentSection) {
        this.currentSection = currentSection;
    }

    public AbstractPublicSectionContentViewModel getCurrentSection() {
        return currentSection;
    }

    public String getNonIfsUrl() {
        return nonIfsUrl;
    }

    public void setNonIfsUrl(String nonIfsUrl) {
        this.nonIfsUrl = nonIfsUrl;
    }

    public Boolean getNonIfs() {
        return nonIfs;
    }

    public void setNonIfs(Boolean nonIfs) {
        this.nonIfs = nonIfs;
    }
}
