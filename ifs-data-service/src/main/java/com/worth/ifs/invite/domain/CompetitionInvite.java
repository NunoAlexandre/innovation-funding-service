package com.worth.ifs.invite.domain;

import com.worth.ifs.category.domain.Category;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.user.domain.User;

import javax.persistence.*;
import java.io.Serializable;

import static com.worth.ifs.category.resource.CategoryType.INNOVATION_AREA;
import static com.worth.ifs.invite.constant.InviteStatus.CREATED;

@Entity
@DiscriminatorValue("COMPETITION")
public class CompetitionInvite extends Invite<Competition, CompetitionInvite> implements Serializable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    private Competition competition;

    @ManyToOne
    @JoinColumn(name="innovation_category_id", referencedColumnName = "id")
    private Category innovationArea;

    public CompetitionInvite() {
        // no-arg constructor
    }

    /**
     * A new User invited to a Competition.
     */
    public CompetitionInvite(final String name, final String email, final String hash, final Competition competition, final Category innovationArea) {
        super(name, email, hash, CREATED);
        if (competition == null) {
            throw new NullPointerException("competition cannot be null");
        }
        if (innovationArea == null) {
            throw new NullPointerException("innovationArea cannot be null");
        }
        if (INNOVATION_AREA != innovationArea.getType()) {
            throw new IllegalArgumentException("innovationArea must be of type INNOVATION_AREA");
        }
        this.competition = competition;
        this.innovationArea = innovationArea;
    }

    /**
     * An existing User invited to a Competition.
     */
    public CompetitionInvite(final User existingUser, final String hash, Competition competition) {
        super(existingUser.getName(), existingUser.getEmail(), hash, CREATED);
        if (competition == null) {
            throw new NullPointerException("competition cannot be null");
        }
        this.competition = competition;
    }

    @Override
    public  Competition getTarget() {
        return competition;
    }

    @Override
    public void setTarget(Competition competition) {
        this.competition = competition;
    }

    public Category getInnovationArea() {
        return innovationArea;
    }
}
