package com.worth.ifs.competition.domain;

import com.worth.ifs.competition.resource.MilestoneType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Entity model to store the Competition Milestones
 */
@Entity
public class Milestone {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private MilestoneType type;
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="competitionId", referencedColumnName="id")
    private Competition competition;

    Milestone() {
        // default constructor
    }

    public Milestone(MilestoneType type, Competition competition) {
        if (type == null) throw new NullPointerException("type cannot be null");
        if (competition == null) throw new NullPointerException("competition cannot be null");
        if (type.isPresetDate()) throw new NullPointerException("MilestoneType '" + type.getMilestoneDescription() + "' must have a date");
        this.type = type;
        this.competition = competition;
    }

    public Milestone(MilestoneType type, LocalDateTime date, Competition competition) {
        if (type == null) throw new NullPointerException("type cannot be null");
        if (competition == null) throw new NullPointerException("competition cannot be null");
        if (date == null) throw new NullPointerException("date cannot be null");

        this.type = type;
        this.date = date;
        this.competition = competition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Competition getCompetition() {
        return competition;
    }

    public MilestoneType getType() {
        return type;
    }

    public void setType(MilestoneType type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }


    public boolean isSet() {
        assert date != null || !type.isPresetDate();
        return date != null;
    }

    public void ifSet(Consumer<LocalDateTime> consumer) {
        if (date != null) {
            consumer.accept(date);
        }
    }

    public boolean isReached(LocalDateTime now) {
        return date != null && !date.isAfter(now);
    }
}
