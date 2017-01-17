package org.innovateuk.ifs.documentation;

import org.innovateuk.ifs.application.builder.ApplicationAssessorResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import static org.innovateuk.ifs.application.builder.ApplicationAssessorResourceBuilder.newApplicationAssessorResource;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.READY_TO_SUBMIT;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.REJECTED;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ApplicationAssessorResourceDocs {

    public static final FieldDescriptor[] applicationAssessorFields = {
            fieldWithPath("userId").description("Id of the user"),
            fieldWithPath("firstName").description("first name of the user"),
            fieldWithPath("lastName").description("last name of the user"),
            fieldWithPath("businessType").description("Assessor type (business or academic)"),
            fieldWithPath("skillAreas").description("Skills of the user"),
            fieldWithPath("rejectReason").description("The reason for rejecting the application"),
            fieldWithPath("rejectComment").description("Any other comments about the reason why this application is being rejected"),
            fieldWithPath("mostRecentAssessmentState").description("Assessment state of the most recent assessment for the user for the requested application"),
            fieldWithPath("totalApplicationsCount").description("Total count of applications assigned to this user for all competitions currently in assessment"),
            fieldWithPath("assignedCount").description("Count of applications assigned to this user for the requested competition including those already submitted"),
            fieldWithPath("submittedCount").description("Count of applications submitted by this user for the requested competition")
    };

    public static final ApplicationAssessorResourceBuilder applicationAssessorResourceBuilder = newApplicationAssessorResource()
            .withUserId(1L, 2L)
            .withFirstName("Oliver", "Irving")
            .withLastName("Romero", "Wolfe")
            .withBusinessType(ACADEMIC, BUSINESS)
            .withSkillAreas("Human computer interaction, Wearables, IoT", "Solar Power, Genetics, Recycling")
            .withRejectReason("Conflict of interest", "Not available")
            .withRejectComment("Member of board of directors", "I do like reviewing the applications to your competitions but please do not assign so many to me.")
            .withMostRecentAssessmentState(READY_TO_SUBMIT, REJECTED)
            .withTotalApplicationsCount(6, 8)
            .withAssignedCount(4, 6)
            .withSubmittedCount(1, 3);
}