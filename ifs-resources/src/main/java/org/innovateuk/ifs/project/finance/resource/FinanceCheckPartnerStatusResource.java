package org.innovateuk.ifs.project.finance.resource;

/**
 * A resource object to return finance check status for a partner organisation
 */
public class FinanceCheckPartnerStatusResource {
    private Long id;
    private String name;
    private Viability viability;
    private ViabilityRagStatus viabilityRagStatus;
    private Eligibility eligibility;
    private boolean awaitingResponse;

    public FinanceCheckPartnerStatusResource() {
    }

    public FinanceCheckPartnerStatusResource(Long id, String name, Viability viability, ViabilityRagStatus viabilityRagStatus, Eligibility eligibility, boolean awaitingResponse) {
        this.id = id;
        this.name = name;
        this.viability = viability;
        this.viabilityRagStatus = viabilityRagStatus;
        this.eligibility = eligibility;
        this.awaitingResponse = awaitingResponse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Eligibility getEligibility() {
        return eligibility;
    }

    public void setEligibility(Eligibility eligibility) {
        this.eligibility = eligibility;
    }

    public Viability getViability() {
        return viability;
    }

    public void setViability(Viability viability) {
        this.viability = viability;
    }

    public ViabilityRagStatus getViabilityRagStatus() {
        return viabilityRagStatus;
    }

    public void setViabilityRagStatus(ViabilityRagStatus viabilityRagStatus) {
        this.viabilityRagStatus = viabilityRagStatus;
    }

    public boolean isAwaitingResponse() {
        return awaitingResponse;
    }

    public void setAwaitingResponse(boolean awaitingResponse) {
        this.awaitingResponse = awaitingResponse;
    }

    public enum Eligibility {
        REVIEW,
        APPROVED,
    }

}
