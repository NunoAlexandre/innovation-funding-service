package org.innovateuk.ifs.assessment.viewmodel;

import org.innovateuk.ifs.invite.resource.CompetitionInviteResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ViewModel of a CompetitionInvite.
 */
public class CompetitionInviteViewModel {

    private final String competitionInviteHash;
    private final String competitionName;
    private final LocalDateTime acceptsDate;
    private final LocalDateTime deadlineDate;
    private final LocalDateTime briefingDate;
    private final BigDecimal assessorPay;
    private final boolean userLoggedIn;

    public CompetitionInviteViewModel(String competitionInviteHash, CompetitionInviteResource invite, boolean userLoggedIn) {
        this.competitionInviteHash = competitionInviteHash;
        this.competitionName = invite.getCompetitionName();
        this.acceptsDate = invite.getAcceptsDate();
        this.deadlineDate = invite.getDeadlineDate();
        this.briefingDate = invite.getBriefingDate();
        this.assessorPay = invite.getAssessorPay();
        this.userLoggedIn = userLoggedIn;
    }

    public String getCompetitionInviteHash() {
        return competitionInviteHash;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public LocalDateTime getAcceptsDate() {
        return acceptsDate;
    }

    public LocalDateTime getDeadlineDate() {
        return deadlineDate;
    }

    public LocalDateTime getBriefingDate() {
        return briefingDate;
    }

    public BigDecimal getAssessorPay() {
        return assessorPay;
    }

    public boolean isUserLoggedIn() {
        return userLoggedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompetitionInviteViewModel that = (CompetitionInviteViewModel) o;

        return new EqualsBuilder()
                .append(competitionInviteHash, that.competitionInviteHash)
                .append(competitionName, that.competitionName)
                .append(acceptsDate, that.acceptsDate)
                .append(deadlineDate, that.deadlineDate)
                .append(briefingDate, that.briefingDate)
                .append(assessorPay, that.assessorPay)
                .append(userLoggedIn, that.userLoggedIn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(competitionInviteHash)
                .append(competitionName)
                .append(acceptsDate)
                .append(deadlineDate)
                .append(briefingDate)
                .append(assessorPay)
                .append(userLoggedIn)
                .toHashCode();
    }
}
