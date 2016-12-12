package org.innovateuk.ifs.invite.resource;

import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link org.innovateuk.ifs.invite.domain.CompetitionInvite}s.
 */
public class CompetitionInviteResource extends InviteResource {

    private Long id;

    private String competitionName;

    private LocalDateTime acceptsDate;

    private LocalDateTime deadlineDate;

    private LocalDateTime briefingDate;

    private BigDecimal assessorPay;

    private String email;

    private InviteStatus status;

    private CategoryResource innovationArea;

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public LocalDateTime getAcceptsDate() {
        return acceptsDate;
    }

    public void setAcceptsDate(LocalDateTime acceptsDate) {
        this.acceptsDate = acceptsDate;
    }

    public LocalDateTime getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDateTime deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public LocalDateTime getBriefingDate() {
        return briefingDate;
    }

    public void setBriefingDate(LocalDateTime briefingDate) {
        this.briefingDate = briefingDate;
    }

    public BigDecimal getAssessorPay() {
        return assessorPay;
    }

    public void setAssessorPay(BigDecimal assessorPay) {
        this.assessorPay = assessorPay;
    }

    public CategoryResource getInnovationArea() {
        return innovationArea;
    }

    public void setInnovationArea(CategoryResource innovationArea) {
        this.innovationArea = innovationArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CompetitionInviteResource that = (CompetitionInviteResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(competitionName, that.competitionName)
                .append(email, that.email)
                .append(status, that.status)
                .append(acceptsDate, that.acceptsDate)
                .append(deadlineDate, that.deadlineDate)
                .append(briefingDate, that.briefingDate)
                .append(assessorPay, that.assessorPay)
                .append(innovationArea, that.innovationArea)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(competitionName)
                .append(email)
                .append(status)
                .append(acceptsDate)
                .append(deadlineDate)
                .append(briefingDate)
                .append(assessorPay)
                .append(innovationArea)
                .toHashCode();
    }
}
