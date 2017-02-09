package org.innovateuk.ifs.publiccontent.form.section.subform;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.innovateuk.ifs.publiccontent.form.section.DatesForm;

import javax.validation.constraints.NotNull;

/**
 * The repeating date that is being used in the {@link DatesForm}
 */
public class Date {
    private Long id;

    @Range(min = 1, max = 31)
    @NotNull(message = "{validation.publiccontent.datesform.date.required}")
    private Integer day;

    @Range(min = 1, max = 12)
    @NotNull(message = "{validation.publiccontent.datesform.date.required}")
    private Integer month;

    @Range(min = 0, max = Integer.MAX_VALUE)
    @NotNull(message = "{validation.publiccontent.datesform.date.required}")
    private Integer year;

    @NotEmpty(message = "{validation.publiccontent.datesform.content.required}")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
