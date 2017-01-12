package org.innovateuk.ifs.invite.domain;

import org.innovateuk.ifs.user.domain.User;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A Participant in a {@link ProcessActivity}.
 */
@MappedSuperclass
public abstract class Participant<P extends ProcessActivity, I extends Invite<P,I>, R extends ParticipantRole<P>> {

    @Column(name = "participant_status_id")
    private ParticipantStatus status;

    protected Participant() {
        this.status = ParticipantStatus.PENDING;
    }

    public abstract Long getId();

    public ParticipantStatus getStatus() {
        return status;
    }

    public abstract P getProcess();

    public abstract I getInvite();

    public abstract R getRole();

    // TODO make this Optional<User>
    public abstract User getUser();

    protected void setStatus(ParticipantStatus newStatus) {
        switch (newStatus) {
            case PENDING:
                if (this.status != null) throw new IllegalStateException("cannot change an existing Participant to PENDING");
                break;
            case ACCEPTED:
            case REJECTED:
                if (this.status != ParticipantStatus.PENDING)
                    throw new IllegalStateException("cannot change a " + this.status + "  Participant to " + newStatus);
            break;
        }

        this.status = newStatus;
    }
}
