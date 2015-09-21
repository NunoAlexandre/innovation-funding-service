package com.worth.ifs.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Calendar;

/**
 * Created by nunoalexandre on 15/09/15.
 */

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Enumerated(EnumType.STRING)
    protected ProcessEvent event;

    @Enumerated(EnumType.STRING)
    protected ProcessStatus status;

    @Version
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastModified;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "observations")
    private String observations;

    @Column(name="decision_reason")
    private String decisionReason;


    public Process() {
    }

    public Process(ProcessEvent event, ProcessStatus status) {
        this.event = event;
        this.status = status;
    }


    public Process(ProcessEvent event, ProcessStatus status, LocalDate startDate, LocalDate endDate) {
        this(event, status);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Process(ProcessEvent event, ProcessStatus status, LocalDate startDate, LocalDate endDate, String observations) {
        this(event, status, startDate, endDate);
        this.observations = observations;
    }


    /**
     * Getters
     **/
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Setters
     **/
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public ProcessEvent getEvent() {
        return event;
    }

    public void setEvent(ProcessEvent event) {
        this.event = event;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String reason) {
        this.decisionReason = reason;
    }

    @JsonIgnore
    public Calendar getVersion() {
        return lastModified;
    }
}
