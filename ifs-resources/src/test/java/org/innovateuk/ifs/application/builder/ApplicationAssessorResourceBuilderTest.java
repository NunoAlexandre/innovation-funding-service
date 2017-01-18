package org.innovateuk.ifs.application.builder;

import org.innovateuk.ifs.application.resource.ApplicationAssessorResource;
import org.innovateuk.ifs.assessment.resource.AssessmentStates;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.application.builder.ApplicationAssessorResourceBuilder.newApplicationAssessorResource;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.ACCEPTED;
import static org.innovateuk.ifs.assessment.resource.AssessmentStates.OPEN;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.junit.Assert.assertEquals;

public class ApplicationAssessorResourceBuilderTest {

    @Test
    public void buildOne() {
        Long expectedUserId = 1L;
        String expectedFirstName = "firstName";
        String expectedLastName = "lastName";
        BusinessType expectedBusinessType = ACADEMIC;
        List<InnovationAreaResource> expectedInnovationAreas = newInnovationAreaResource()
                .withName("Early Stage Manufacturing", "Bioscience")
                .build(2);
        String expectedSkillAreas = "skillAreas";
        String expectedRejectReason = "rejectReason";
        String expectedRejectComment = "rejectComment";
        boolean expectedAvailable = true;
        AssessmentStates expectedMostRecentAssessmentState = ACCEPTED;
        long expectedTotalApplicationsCount = 10;
        long expectedAssignedCount = 20;
        long expectedSubmittedCount = 30;

        ApplicationAssessorResource applicationAssessorResource = newApplicationAssessorResource()
                .withUserId(expectedUserId)
                .withFirstName(expectedFirstName)
                .withLastName(expectedLastName)
                .withBusinessType(expectedBusinessType)
                .withInnovationAreas(expectedInnovationAreas)
                .withSkillAreas(expectedSkillAreas)
                .withRejectReason(expectedRejectReason)
                .withRejectComment(expectedRejectComment)
                .withAvailable(expectedAvailable)
                .withMostRecentAssessmentState(expectedMostRecentAssessmentState)
                .withTotalApplicationsCount(expectedTotalApplicationsCount)
                .withAssignedCount(expectedAssignedCount)
                .withSubmittedCount(expectedSubmittedCount)
                .build();

        assertEquals(expectedUserId, applicationAssessorResource.getUserId());
        assertEquals(expectedFirstName, applicationAssessorResource.getFirstName());
        assertEquals(expectedLastName, applicationAssessorResource.getLastName());
        assertEquals(expectedBusinessType, applicationAssessorResource.getBusinessType());
        assertEquals(expectedInnovationAreas, applicationAssessorResource.getInnovationAreas());
        assertEquals(expectedSkillAreas, applicationAssessorResource.getSkillAreas());
        assertEquals(expectedRejectReason, applicationAssessorResource.getRejectReason());
        assertEquals(expectedRejectComment, applicationAssessorResource.getRejectComment());
        assertEquals(expectedAvailable, applicationAssessorResource.isAvailable());
        assertEquals(expectedMostRecentAssessmentState, applicationAssessorResource.getMostRecentAssessmentState());
        assertEquals(expectedTotalApplicationsCount, applicationAssessorResource.getTotalApplicationsCount());
        assertEquals(expectedAssignedCount, applicationAssessorResource.getAssignedCount());
        assertEquals(expectedSubmittedCount, applicationAssessorResource.getSubmittedCount());
    }

    @Test
    public void buildMany() {
        Long[] expectedUserIds = {1L, 2L};
        String[] expectedFirstNames = {"firstName1", "firstName2"};
        String[] expectedLastNames = {"lastName1", "lastName2"};
        BusinessType[] expectedBusinessTypes = {BUSINESS, ACADEMIC};
        @SuppressWarnings("unchecked") List<InnovationAreaResource>[] expectedInnovationAreas = new List[]{
                newInnovationAreaResource()
                        .withName("Creative economy", "Offshore Renewable Energy")
                        .build(2),
                newInnovationAreaResource()
                        .withName("Urban Living", "Advanced Therapies")
                        .build(2)
        };
        String[] expectedSkillAreas = {"skillAreas1", "skillAreas2"};
        String[] expectedRejectReasons = {"rejectReason1", "rejectReason2"};
        String[] expectedRejectComments = {"rejectComment1", "rejectComment2"};
        Boolean[] expectedAvailable = {true, false};
        AssessmentStates[] expectedMostRecentAssessmentStates = {ACCEPTED, OPEN};
        Long[] expectedTotalApplicationsCount = {10L, 11L};
        Long[] expectedAssignedCount = {20L, 21L};
        Long[] expectedSubmittedCount = {30L, 31L};

        List<ApplicationAssessorResource> applicationAssessorResources = newApplicationAssessorResource()
                .withUserId(expectedUserIds)
                .withFirstName(expectedFirstNames)
                .withLastName(expectedLastNames)
                .withBusinessType(expectedBusinessTypes)
                .withInnovationAreas(expectedInnovationAreas)
                .withSkillAreas(expectedSkillAreas)
                .withRejectReason(expectedRejectReasons)
                .withRejectComment(expectedRejectComments)
                .withAvailable(expectedAvailable)
                .withMostRecentAssessmentState(expectedMostRecentAssessmentStates)
                .withTotalApplicationsCount(expectedTotalApplicationsCount)
                .withAssignedCount(expectedAssignedCount)
                .withSubmittedCount(expectedSubmittedCount)
                .build(2);

        ApplicationAssessorResource first = applicationAssessorResources.get(0);
        assertEquals(expectedUserIds[0], first.getUserId());
        assertEquals(expectedFirstNames[0], first.getFirstName());
        assertEquals(expectedLastNames[0], first.getLastName());
        assertEquals(expectedBusinessTypes[0], first.getBusinessType());
        assertEquals(expectedInnovationAreas[0], first.getInnovationAreas());
        assertEquals(expectedSkillAreas[0], first.getSkillAreas());
        assertEquals(expectedRejectReasons[0], first.getRejectReason());
        assertEquals(expectedRejectComments[0], first.getRejectComment());
        assertEquals(expectedAvailable[0], first.isAvailable());
        assertEquals(expectedMostRecentAssessmentStates[0], first.getMostRecentAssessmentState());
        assertEquals(expectedTotalApplicationsCount[0].intValue(), first.getTotalApplicationsCount());
        assertEquals(expectedAssignedCount[0].intValue(), first.getAssignedCount());
        assertEquals(expectedSubmittedCount[0].intValue(), first.getSubmittedCount());

        ApplicationAssessorResource second = applicationAssessorResources.get(1);
        assertEquals(expectedUserIds[1], second.getUserId());
        assertEquals(expectedFirstNames[1], second.getFirstName());
        assertEquals(expectedLastNames[1], second.getLastName());
        assertEquals(expectedBusinessTypes[1], second.getBusinessType());
        assertEquals(expectedInnovationAreas[1], second.getInnovationAreas());
        assertEquals(expectedSkillAreas[1], second.getSkillAreas());
        assertEquals(expectedRejectReasons[1], second.getRejectReason());
        assertEquals(expectedRejectComments[1], second.getRejectComment());
        assertEquals(expectedAvailable[1], second.isAvailable());
        assertEquals(expectedMostRecentAssessmentStates[1], second.getMostRecentAssessmentState());
        assertEquals(expectedTotalApplicationsCount[1].intValue(), second.getTotalApplicationsCount());
        assertEquals(expectedAssignedCount[1].intValue(), second.getAssignedCount());
        assertEquals(expectedSubmittedCount[1].intValue(), second.getSubmittedCount());
    }
}