package org.innovateuk.ifs.project.resource;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ProjectResourceTest {
    Long id;
    String name;
    ApplicationResource applicationResource;
    List<Long> projectUsers;
    AddressResource addressResource;
    Long duration;
    LocalDate targetStartDate;
    ProjectResource projectResource;

    @Before
    public void setup(){
        id = 1L;
        name = "Test Project 1";
        applicationResource = newApplicationResource().build();
        projectUsers = asList(1L, 2L, 3L);
        addressResource = newAddressResource().build();
        duration = 3L;
        targetStartDate = LocalDate.now();
        projectResource = newProjectResource().withId(id)
                .withName(name)
                .withApplication(applicationResource)
                .withProjectUsers(projectUsers)
                .withAddress(addressResource)
                .withDuration(duration)
                .withTargetStartDate(targetStartDate)
                .build();
    }

    @Test
    public void projectResourceShouldReturnCorrectPropertyValues(){
        assertEquals(projectResource.getId(), id);
        assertEquals(projectResource.getAddress(), addressResource);
        assertEquals(projectResource.getName(), name);
        assertEquals(projectResource.getDurationInMonths(), duration);
        assertEquals(projectResource.getApplication(), applicationResource.getId());
        assertEquals(projectResource.getProjectUsers(), projectUsers);
    }

    public void equalsShouldReturnFalseOnNull() {
        assertFalse(applicationResource.equals(null));
    }
}
