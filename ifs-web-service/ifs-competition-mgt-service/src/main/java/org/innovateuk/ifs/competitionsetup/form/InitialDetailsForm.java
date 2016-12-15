package org.innovateuk.ifs.competitionsetup.form;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Form for the initial details competition setup section.
 */
public class InitialDetailsForm extends CompetitionSetupForm {

    @NotNull(message = "{validation.initialdetailsform.executiveuserid.required}")
    private Long executiveUserId;

    private LocalDateTime openingDate;

    @NotNull(message = "{validation.initialdetailsform.openingdateday.required}")
    @Range(min=1, max=31, message= "{validation.initialdetailsform.openingdateday.range}")
    private Integer openingDateDay;

    @NotNull(message = "{validation.initialdetailsform.openingdatemonth.required}")
    @Range(min=1, max=12, message= "{validation.initialdetailsform.openingdatemonth.range}")
    private Integer openingDateMonth;

    @NotNull(message = "{validation.initialdetailsform.openingdateyear.required}")
    @Range(min=1900, max=9999, message= "{validation.initialdetailsform.openingdateyear.range}")
    private Integer openingDateYear;

    @NotEmpty(message = "{validation.standard.title.required}")
    private String title;

    @NotNull(message = "{validation.initialdetailsform.innovationsectorcategoryid.required}")
    private Long innovationSectorCategoryId;

    @NotEmpty(message = "{validation.initialdetailsform.innovationareacategoryid.required}")
    private List<Long> innovationAreaCategoryIds;

    @NotNull(message = "{validation.initialdetailsform.competitiontypeid.required}")
    private Long competitionTypeId;

    @NotNull(message = "{validation.initialdetailsform.leadtechnologistuserid.required}")
    private Long leadTechnologistUserId;

    public Long getExecutiveUserId() {
        return executiveUserId;
    }

    public void setExecutiveUserId(Long executiveUserId) {
        this.executiveUserId = executiveUserId;
    }

    public LocalDateTime getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDateTime openingDate) {
        this.openingDate = openingDate;
    }

    public Integer getOpeningDateDay() {
        return openingDateDay;
    }

    public void setOpeningDateDay(Integer openingDateDay) {
        this.openingDateDay = openingDateDay;
    }

    public Integer getOpeningDateMonth() {
        return openingDateMonth;
    }

    public void setOpeningDateMonth(Integer openingDateMonth) {
        this.openingDateMonth = openingDateMonth;
    }

    public Integer getOpeningDateYear() {
        return openingDateYear;
    }

    public void setOpeningDateYear(Integer openingDateYear) {
        this.openingDateYear = openingDateYear;
    }

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

    public List<Long> getInnovationAreaCategoryIds() {
        return innovationAreaCategoryIds;
    }

    public void setInnovationAreaCategoryIds(List<Long> innovationAreaCategoryIds) {
        this.innovationAreaCategoryIds = innovationAreaCategoryIds;
    }

    public Long getCompetitionTypeId() {
        return competitionTypeId;
    }

    public void setCompetitionTypeId(Long competitionTypeId) {
        this.competitionTypeId = competitionTypeId;
    }

    public Long getLeadTechnologistUserId() {
        return leadTechnologistUserId;
    }

    public void setLeadTechnologistUserId(Long leadTechnologistUserId) {
        this.leadTechnologistUserId = leadTechnologistUserId;
    }


}
