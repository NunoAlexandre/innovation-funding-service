package org.innovateuk.ifs.competitionsetup.form;

import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Range;

import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Milestone Form Entry for the Milestones form.
 */
public class MilestoneRowForm {
    @Range(min = 1, max = 31)
    private Integer day;
    @Range(min = 1, max = 12)
    private Integer month;
    @Range(min = 2016, max = 9000)
    private Integer year;
    private MilestoneTime time;

    private MilestoneType milestoneType;
    private String dayOfWeek;
    private boolean editable;
    private LocalDateTime date;

    private static final Log LOG = LogFactory.getLog(MilestoneRowForm.class);

    public MilestoneRowForm() {

    }

    public MilestoneRowForm(MilestoneType milestoneType, LocalDateTime dateTime) {
        this.setMilestoneType(milestoneType);
        if(dateTime != null) {
            this.setDay(dateTime.getDayOfMonth());
            this.setMonth(dateTime.getMonth().getValue());
            this.setYear(dateTime.getYear());
            this.setTime(MilestoneTime.fromLocalDateTime(dateTime));
            this.setDate(dateTime);
            this.editable = LocalDateTime.now().isBefore(dateTime);
        } else {
            this.editable = true;
            this.setTime(MilestoneTime.TWELVE_PM);
        }
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

    public String getDayOfWeek() {
        return getNameOfDay();
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public MilestoneType getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(MilestoneType milestoneType) {
        this.milestoneType = milestoneType;
    }

    public String getMilestoneNameType() {
        return milestoneType.name();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public MilestoneTime getTime() {
        return time;
    }

    public void setTime(MilestoneTime time) {
        this.time = time;
    }

    private String getNameOfDay() {
        String dayName =  getMilestoneDate(day, month, year);
        if(dayName == null) {
            dayOfWeek = "-";
        }
        else {
            try {
                dayOfWeek = dayName.substring(0, 1) + dayName.substring(1, 3).toLowerCase();
            } catch (Exception ex) {
                LOG.error(ex);
            }
        }
        return dayOfWeek;
    }

    private String getMilestoneDate (Integer day, Integer month, Integer year) {
        if (day != null && month != null && year != null) {
            try {
                return LocalDateTime.of(year, month, day, 0, 0).getDayOfWeek().name();
            } catch (DateTimeException ex) {
                LOG.error("Invalid date");
                LOG.debug(ex.getMessage());
            }
        }

        return null;
    }

    public LocalDateTime getMilestoneAsDateTime(){
        if (day != null && month != null && year != null){
            if ( time != null) {
                return LocalDateTime.of(year, month, day, time.getHour(), 0);
            } else {
                return LocalDateTime.of(year, month, day, 0, 0);
            }
        } else {
            return null;
        }
    }
}
