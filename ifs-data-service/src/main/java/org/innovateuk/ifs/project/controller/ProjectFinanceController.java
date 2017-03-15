package org.innovateuk.ifs.project.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.finance.transactional.ProjectFinanceRowService;
import org.innovateuk.ifs.project.finance.resource.*;
import org.innovateuk.ifs.project.finance.transactional.ProjectFinanceService;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.transactional.ProjectGrantOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * ProjectFinanceController exposes Project finance data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
public class ProjectFinanceController {
    @Autowired
    private ProjectFinanceRowService financeRowService;

    @Autowired
    private ProjectFinanceService projectFinanceService;

    @Autowired
    private ProjectGrantOfferService projectGrantOfferService;

    @RequestMapping(value = "/{projectId}/spend-profile/generate", method = POST)
    public RestResult<Void> generateSpendProfile(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.generateSpendProfile(projectId).toPostCreateResponse();
    }

    /**
     * This method was written to recreate Spend Profile for one of the partner organisations on Production.
     * This method assumes that all the necessary stuff is in the database before the Spend Profile can be generated.
     */
    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/user/{userId}/spend-profile/generate", method = POST)
    public RestResult<Void> generateSpendProfileForPartnerOrganisation(@PathVariable("projectId") final Long projectId,
                                                                       @PathVariable("organisationId") final Long organisationId,
                                                                       @PathVariable("userId") final Long userId) {
        return projectFinanceService.generateSpendProfileForPartnerOrganisation(projectId, organisationId, userId).toPostCreateResponse();
    }

    @RequestMapping(value = "/{projectId}/spend-profile/approval/{approvalType}", method = POST)
    public RestResult<Void> approveOrRejectSpendProfile(@PathVariable("projectId") final Long projectId,
                                                        @PathVariable("approvalType") final ApprovalType approvalType) {
        return projectFinanceService.approveOrRejectSpendProfile(projectId, approvalType).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/spend-profile/approval", method = GET)
    public RestResult<ApprovalType> getSpendProfileStatusByProjectId(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.getSpendProfileStatusByProjectId(projectId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile-table")
    public RestResult<SpendProfileTableResource> getSpendProfileTable(@PathVariable("projectId") final Long projectId,
                                                                      @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        return projectFinanceService.getSpendProfileTable(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile-csv")
    public RestResult<SpendProfileCSVResource> getSpendProfileCSV(@PathVariable("projectId") final Long projectId,
                                                                  @PathVariable("organisationId") final Long organisationId) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getSpendProfileCSV(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile")
    public RestResult<SpendProfileResource> getSpendProfile(@PathVariable("projectId") final Long projectId,
                                                            @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getSpendProfile(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/spend-profile", method = POST)
    public RestResult<Void> saveSpendProfile(@PathVariable("projectId") final Long projectId,
                                             @PathVariable("organisationId") final Long organisationId,
                                             @RequestBody SpendProfileTableResource table) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.saveSpendProfile(projectOrganisationCompositeId, table).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/spend-profile/complete", method = POST)
    public RestResult<Void> markSpendProfileComplete(@PathVariable("projectId") final Long projectId,
                                                    @PathVariable("organisationId") final Long organisationId) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.markSpendProfileComplete(projectOrganisationCompositeId).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/spend-profile/incomplete", method = POST)
    public RestResult<Void> markSpendProfileIncomplete(@PathVariable("projectId") final Long projectId,
                                                    @PathVariable("organisationId") final Long organisationId) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.markSpendProfileIncomplete(projectOrganisationCompositeId).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/complete-spend-profiles-review", method = POST)
    public RestResult<Void> completeSpendProfilesReview(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.completeSpendProfilesReview(projectId).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/project-finances", method = GET)
    public RestResult<List<ProjectFinanceResource>> getProjectFinances(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.getProjectFinances(projectId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/viability")
    public RestResult<ViabilityResource> getViability(@PathVariable("projectId") final Long projectId,
                                                      @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getViability(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/viability/{viability}/{viabilityRagStatus}", method = POST)
    public RestResult<Void> saveViability(@PathVariable("projectId") final Long projectId,
                                          @PathVariable("organisationId") final Long organisationId,
                                          @PathVariable("viability") final Viability viability,
                                          @PathVariable("viabilityRagStatus") final ViabilityRagStatus viabilityRagStatus) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.saveViability(projectOrganisationCompositeId, viability, viabilityRagStatus).toPostResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/eligibility")
    public RestResult<EligibilityResource> getEligibility(@PathVariable("projectId") final Long projectId,
                                                          @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getEligibility(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/eligibility/{eligibility}/{eligibilityRagStatus}", method = POST)
    public RestResult<Void> saveEligibility(@PathVariable("projectId") final Long projectId,
                                            @PathVariable("organisationId") final Long organisationId,
                                            @PathVariable("eligibility") final Eligibility eligibility,
                                            @PathVariable("eligibilityRagStatus") final EligibilityRagStatus eligibilityRagStatus) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.saveEligibility(projectOrganisationCompositeId, eligibility, eligibilityRagStatus).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/credit-report/{reportPresent}", method = POST)
    public RestResult<Void> saveCreditReport(@PathVariable("projectId") Long projectId, @PathVariable("organisationId") Long organisationId, @PathVariable("reportPresent") Boolean reportPresent) {
        return projectFinanceService.saveCreditReport(projectId, organisationId, reportPresent).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/credit-report", method = GET)
    public RestResult<Boolean> getCreditReport(@PathVariable("projectId") Long projectId, @PathVariable("organisationId") Long organisationId) {
        return projectFinanceService.getCreditReport(projectId, organisationId).toGetResponse();
    }

    @RequestMapping("/{projectId}/organisation/{organisationId}/financeDetails")
    public RestResult<ProjectFinanceResource> financeDetails(@PathVariable("projectId") final Long projectId, @PathVariable("organisationId") final Long organisationId) {
        return financeRowService.financeChecksDetails(projectId, organisationId).toGetResponse();
    }
}
