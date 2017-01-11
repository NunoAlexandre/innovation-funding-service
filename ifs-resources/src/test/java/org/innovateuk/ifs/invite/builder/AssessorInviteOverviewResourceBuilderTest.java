package org.innovateuk.ifs.invite.builder;

import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.invite.resource.AssessorInviteOverviewResource;
import org.innovateuk.ifs.user.resource.BusinessType;
import org.junit.Test;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.invite.builder.AssessorInviteOverviewResourceBuilder.newAssessorInviteOverviewResource;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.junit.Assert.assertEquals;

public class AssessorInviteOverviewResourceBuilderTest {

    @Test
    public void buildOne() {
        String expectedName = "name";
        CategoryResource expectedInnovationArea = newInnovationAreaResource().build();
        Boolean expectedCompliant = FALSE;
        BusinessType expectedBusinessType = ACADEMIC;
        String expectedStatus = "status";
        String expectedDetails = "details";

        AssessorInviteOverviewResource assessorInviteOverviewResource = newAssessorInviteOverviewResource()
                .withName(expectedName)
                .withInnovationArea(expectedInnovationArea)
                .withCompliant(expectedCompliant)
                .withBusinessType(expectedBusinessType)
                .withStatus(expectedStatus)
                .withDetails(expectedDetails)
                .build();

        assertEquals(expectedName, assessorInviteOverviewResource.getName());
        assertEquals(expectedInnovationArea, assessorInviteOverviewResource.getInnovationArea());
        assertEquals(expectedCompliant, assessorInviteOverviewResource.isCompliant());
        assertEquals(expectedBusinessType, assessorInviteOverviewResource.getBusinessType());
        assertEquals(expectedStatus, assessorInviteOverviewResource.getStatus());
        assertEquals(expectedDetails, assessorInviteOverviewResource.getDetails());
    }

    @Test
    public void buildMany() {
        String[] expectedNames = {"name1", "name2"};
        CategoryResource[] expectedInnovationAreas = newInnovationAreaResource().buildArray(2, InnovationAreaResource.class);
        Boolean[] expectedCompliants = {TRUE, FALSE};
        BusinessType[] expectedBusinessTypes = {ACADEMIC, BUSINESS};
        String[] expectedStatuses = {"status1", "status2"};
        String[] expectedDetails = {"details1", "details2"};

        List<AssessorInviteOverviewResource> assessorCreatedInviteResources = newAssessorInviteOverviewResource()
                .withName(expectedNames)
                .withInnovationArea(expectedInnovationAreas)
                .withCompliant(expectedCompliants)
                .withBusinessType(expectedBusinessTypes)
                .withStatus(expectedStatuses)
                .withDetails(expectedDetails)
                .build(2);

        AssessorInviteOverviewResource first = assessorCreatedInviteResources.get(0);
        assertEquals(expectedNames[0], first.getName());
        assertEquals(expectedInnovationAreas[0], first.getInnovationArea());
        assertEquals(expectedCompliants[0], first.isCompliant());
        assertEquals(expectedBusinessTypes[0], first.getBusinessType());
        assertEquals(expectedStatuses[0], first.getStatus());
        assertEquals(expectedDetails[0], first.getDetails());

        AssessorInviteOverviewResource second = assessorCreatedInviteResources.get(1);
        assertEquals(expectedNames[1], second.getName());
        assertEquals(expectedInnovationAreas[1], second.getInnovationArea());
        assertEquals(expectedCompliants[1], second.isCompliant());
        assertEquals(expectedBusinessTypes[1], second.getBusinessType());
        assertEquals(expectedStatuses[1], second.getStatus());
        assertEquals(expectedDetails[1], second.getDetails());
    }
}