package org.innovateuk.ifs.project.monitoringofficer.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.project.monitoringofficer.resource.MonitoringOfficerResource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

public class MonitoringOfficerRestServiceImplTest extends BaseRestServiceUnitTest<MonitoringOfficerRestServiceImpl> {
    private static final String projectRestURL = "/project";

    @Override
    protected MonitoringOfficerRestServiceImpl registerRestServiceUnderTest() {
        MonitoringOfficerRestServiceImpl monitoringOfficerRestService = new MonitoringOfficerRestServiceImpl();
        ReflectionTestUtils.setField(monitoringOfficerRestService, "projectRestURL", projectRestURL);
        return monitoringOfficerRestService;
    }

    @Test
    public void testUpdateMonitoringOfficer() {

        Long projectId = 1L;

        MonitoringOfficerResource monitoringOfficerResource = new MonitoringOfficerResource();
        monitoringOfficerResource.setId(null);
        monitoringOfficerResource.setProject(projectId);
        monitoringOfficerResource.setFirstName("abc");
        monitoringOfficerResource.setLastName("xyz");
        monitoringOfficerResource.setEmail("abc.xyz@gmail.com");
        monitoringOfficerResource.setPhoneNumber("078323455");

        setupPutWithRestResultExpectations(projectRestURL + "/" + projectId + "/monitoring-officer", monitoringOfficerResource, OK);

        RestResult<Void> result = service.updateMonitoringOfficer(projectId, "abc", "xyz", "abc.xyz@gmail.com", "078323455");

        assertTrue(result.isSuccess());

    }

    @Test
    public void testGetMonitoringOfficerForProject() {

        MonitoringOfficerResource expectedMonitoringOfficerResource = new MonitoringOfficerResource();
        expectedMonitoringOfficerResource.setProject(1L);
        expectedMonitoringOfficerResource.setFirstName("abc");
        expectedMonitoringOfficerResource.setLastName("xyz");
        expectedMonitoringOfficerResource.setEmail("abc.xyz@gmail.com");
        expectedMonitoringOfficerResource.setPhoneNumber("078323455");

        setupGetWithRestResultExpectations(projectRestURL + "/1/monitoring-officer", MonitoringOfficerResource.class, expectedMonitoringOfficerResource);

        RestResult<MonitoringOfficerResource> result = service.getMonitoringOfficerForProject(1L);

        assertTrue(result.isSuccess());

        Assert.assertEquals(expectedMonitoringOfficerResource, result.getSuccessObject());

    }
}
