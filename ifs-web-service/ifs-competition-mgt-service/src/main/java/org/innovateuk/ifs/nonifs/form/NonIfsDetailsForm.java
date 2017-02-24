package org.innovateuk.ifs.nonifs.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.competitionsetup.form.MilestoneRowForm;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Form for the Non-IFS competition details page.
 */
public class NonIfsDetailsForm {

    @NotNull
    private String title;
    @NotNull
    private Long innovationSectorCategoryId;
    @NotNull
    private Long innovationAreaCategoryId;
    @Valid
    @NotNull
    private MilestoneRowForm openDate;
    @Valid
    @NotNull
    private MilestoneRowForm closeDate;
    @Valid
    @NotNull
    private MilestoneRowForm applicantNotifiedDate;
    @NotNull
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getInnovationSectorCategoryId() {
        return innovationSectorCategoryId;
    }

    public void setInnovationSectorCategoryId(Long innovationSectorCategoryId) {
        this.innovationSectorCategoryId = innovationSectorCategoryId;
    }

    public Long getInnovationAreaCategoryId() {
        return innovationAreaCategoryId;
    }

    public void setInnovationAreaCategoryId(Long innovationAreaCategoryId) {
        this.innovationAreaCategoryId = innovationAreaCategoryId;
    }

    public MilestoneRowForm getOpenDate() {
        return openDate;
    }

    public void setOpenDate(MilestoneRowForm openDate) {
        this.openDate = openDate;
    }

    public MilestoneRowForm getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(MilestoneRowForm closeDate) {
        this.closeDate = closeDate;
    }

    public MilestoneRowForm getApplicantNotifiedDate() {
        return applicantNotifiedDate;
    }

    public void setApplicantNotifiedDate(MilestoneRowForm applicantNotifiedDate) {
        this.applicantNotifiedDate = applicantNotifiedDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NonIfsDetailsForm that = (NonIfsDetailsForm) o;

        return new EqualsBuilder()
                .append(title, that.title)
                .append(innovationSectorCategoryId, that.innovationSectorCategoryId)
                .append(innovationAreaCategoryId, that.innovationAreaCategoryId)
                .append(openDate, that.openDate)
                .append(closeDate, that.closeDate)
                .append(applicantNotifiedDate, that.applicantNotifiedDate)
                .append(url, that.url)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(title)
                .append(innovationSectorCategoryId)
                .append(innovationAreaCategoryId)
                .append(openDate)
                .append(closeDate)
                .append(applicantNotifiedDate)
                .append(url)
                .toHashCode();
    }
}
