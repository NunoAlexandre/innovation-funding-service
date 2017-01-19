package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class FinanceCheckDocs {

    public static final FieldDescriptor[] financeCheckApprovalStatusFields = {
            fieldWithPath("canApprove").description("Is the Finance Check currently in a state where it can be approved"),
            fieldWithPath("currentState").description("The current state of the Finance Check process"),
            fieldWithPath("participant").description("The latest ProjectUser to interact with the Finance Check process"),
            fieldWithPath("internalParticipant").description("The latest internal User to interact with the Finance Check process"),
            fieldWithPath("modifiedDate").description("The latest time that the Finance Check process was updated")
    };

    public static final FieldDescriptor[] financeCheckSummaryResourceFields = {
            fieldWithPath("projectId").description("Id or project that the status is from"),
            fieldWithPath("projectName").description("Name of project that the status is from"),
            fieldWithPath("competitionId").description("Id of competition that the project is from"),
            fieldWithPath("competitionName").description("Name of competition that the project is from"),
            fieldWithPath("projectStartDate").description("Expected start date of project"),
            fieldWithPath("durationInMonths").description("Total expected project duration in months"),
            fieldWithPath("totalProjectCost").description("Total cost of project (a sum of costs of all partners)"),
            fieldWithPath("grantAppliedFor").description("Total grant applied for, excluding any other sources of funding"),
            fieldWithPath("otherPublicSectorFunding").description("Other public sector funding available for the project"),
            fieldWithPath("totalPercentageGrant").description("Total percentage of grant through IFS"),
            fieldWithPath("spendProfilesGenerated").description("Flag to signify if spend profiles have already been generated using these finance checks"),
            fieldWithPath("financeChecksAllApproved").description("Flag to signify if all finance checks have been approved"),
            fieldWithPath("spendProfileGeneratedBy").description("Name of internal IFS user who generated spend profile"),
            fieldWithPath("spendProfileGeneratedDate").description("Date when internal user generated spend profile from finance checks"),
            fieldWithPath("partnerStatusResources").description("List of statuses for all partners")
    };

    public static final FieldDescriptor[] financeCheckEligibilityResourceFields = {
            fieldWithPath("projectId").description("Id or project that the eligibility is from"),
            fieldWithPath("organisationId").description("Id of organisation that the eligibility is from"),
            fieldWithPath("durationInMonths").description("Total expected project duration in months"),
            fieldWithPath("totalCost").description("Total cost of project for the organisation"),
            fieldWithPath("percentageGrant").description("The percentage of funding of the project through grant for the organisation"),
            fieldWithPath("fundingSought").description("The funding sought for the organisation for the project"),
            fieldWithPath("otherPublicSectorFunding").description("Other public sector funding available for the organisation for the project"),
            fieldWithPath("contributionToProject").description("The contribution that the organisation is  making to the project")
    };

    public static final FieldDescriptor[] financeCheckResourceFields = {
            fieldWithPath("id").description("Finance check entity unique id"),
            fieldWithPath("project").description("Project id to which the finance check belongs"),
            fieldWithPath("organisation").description("Organisation of partner organisation"),
            fieldWithPath("costGroup").description("Cost group that holds updated cost values for finance check.  These are then used to generate spend profile for each partner")
    };
}
