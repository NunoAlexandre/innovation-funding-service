package com.worth.ifs.invite.domain;


import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.invite.constant.InviteStatus;
import com.worth.ifs.user.domain.User;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

import java.util.Optional;

import static com.worth.ifs.invite.constant.InviteStatus.OPENED;
import static com.worth.ifs.invite.domain.ParticipantStatus.ACCEPTED;
import static com.worth.ifs.invite.domain.ParticipantStatus.REJECTED;

/**
 * A {@link Participant} in a {@link Competition}.
 */
@Entity
@Table(name = "competition_user")
public class CompetitionParticipant extends Participant<Competition, CompetitionInvite, CompetitionParticipantRole> {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id", referencedColumnName = "id")
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invite_id")
    private CompetitionInvite invite;

    @ManyToOne
    @JoinColumn(name = "rejection_reason_id")
    private RejectionReason rejectionReason;

    @Column(name = "rejection_comment")
    private String rejectionReasonComment;

    @Column(name = "competition_role_id")
    private CompetitionParticipantRole role;

    CompetitionParticipant() {
        // no-arg constructor
        this.competition = null;
    }

    public CompetitionParticipant(Competition competition, CompetitionInvite invite) {
        this(competition, null, invite);
    }

    public CompetitionParticipant(Competition competition, User user, CompetitionInvite invite) {
        super();
        if (competition == null) throw new NullPointerException("competition cannot be null");
        if (invite == null) throw new NullPointerException("invite cannot be null");

        this.competition = competition;
        this.user = user;
        this.invite = invite;
        this.role = CompetitionParticipantRole.ASSESSOR;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Competition getProcess() {
        return competition;
    }

    @Override
    public Optional<CompetitionInvite> getInvite() {
        return Optional.ofNullable(invite);
    }

    @Override
    public CompetitionParticipantRole getRole() {
        return role;
    }

    @Override
    public User getUser() {
        return user;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public String getRejectionReasonComment() {
        return rejectionReasonComment;
    }

    public CompetitionParticipant accept() {
        if (getInvite().get().getStatus() != OPENED)
            throw new IllegalStateException("Cannot accept a CompetitionParticipant that hasn't been opened");

        if (getStatus() == REJECTED)
            throw new IllegalStateException("Cannot accept a CompetitionParticipant that has been rejected");
        if (getStatus() == ACCEPTED)
            throw new IllegalStateException("CompetitionParticipant has already been accepted");

        setStatus(ACCEPTED);

        return this;
    }

    public CompetitionParticipant reject(RejectionReason rejectionReason, String rejectionComment) {
        if (rejectionReason == null) throw new NullPointerException("rejectionReason cannot be null");
        if (rejectionComment == null) throw new NullPointerException("rejectionComment cannot be null");
        if (rejectionComment.isEmpty()) throw new IllegalArgumentException("rejectionComment cannot be empty");

        if (getInvite().get().getStatus() != OPENED)
            throw new IllegalStateException("Cannot accept a CompetitionParticipant that hasn't been opened");

        if (getStatus() == ACCEPTED)
            throw new IllegalStateException("Cannot reject a CompetitionParticipant that has been accepted");
        if (getStatus() == REJECTED)
            throw new IllegalStateException("CompetitionParticipant has already been rejected");

        this.rejectionReason = rejectionReason;
        this.rejectionReasonComment = rejectionComment;
        setStatus(REJECTED);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CompetitionParticipant that = (CompetitionParticipant) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(id, that.id)
                .append(competition.getId(), that.competition.getId())
                .append(user, that.user)
                .append(invite, that.invite)
                .append(rejectionReason, that.rejectionReason)
                .append(rejectionReasonComment, that.rejectionReasonComment)
                .append(role, that.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(id)
                .append(competition.getId())
                .append(user)
                .append(invite)
                .append(rejectionReason)
                .append(rejectionReasonComment)
                .append(role)
                .toHashCode();
    }
}
