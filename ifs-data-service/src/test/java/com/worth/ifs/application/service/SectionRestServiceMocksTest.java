package com.worth.ifs.application.service;

import java.util.List;

import com.worth.ifs.BaseRestServiceUnitTest;
import com.worth.ifs.application.resource.SectionResource;

import org.junit.Test;

import static com.worth.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.longsListType;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Tests to check the ApplicationRestService's interaction with the RestTemplate and the processing of its results
 */
public class SectionRestServiceMocksTest extends BaseRestServiceUnitTest<SectionRestServiceImpl> {

    private static final String sectionRestUrl = "/section";

    @Override
    protected SectionRestServiceImpl registerRestServiceUnderTest() {
        SectionRestServiceImpl sectionRestService = new SectionRestServiceImpl();
        sectionRestService.sectionRestURL = sectionRestUrl;
        return sectionRestService;
    }

    @Test
    public void test_getCompletedSectionIds() {

        String expectedUrl = sectionRestUrl + "/getCompletedSections/123/456";
        List<Long> returnedResponse = asList(1L, 2L, 3L);

        setupGetWithRestResultExpectations(expectedUrl, longsListType(), returnedResponse);

        // now run the method under test
        List<Long> response = service.getCompletedSectionIds(123L, 456L).getSuccessObject();
        assertEquals(returnedResponse, response);
    }

    @Test
    public void test_getIncompleteSectionIds() {

        String expectedUrl = sectionRestUrl + "/getIncompleteSections/123";
        List<Long> returnedResponse = asList(1L, 2L, 3L);

        setupGetWithRestResultExpectations(expectedUrl, longsListType(), returnedResponse);

        // now run the method under test
        List<Long> response = service.getIncompletedSectionIds(123L).getSuccessObject();
        assertEquals(returnedResponse, response);
    }

    @Test
    public void test_getSection() {

        String expectedUrl = sectionRestUrl + "/findByName/Section 1";
        SectionResource returnedResponse = newSectionResource().build();

        setupGetWithRestResultExpectations(expectedUrl, SectionResource.class, returnedResponse);

        // now run the method under test
        SectionResource response = service.getSection("Section 1").getSuccessObject();
        assertEquals(returnedResponse, response);
    }
}
