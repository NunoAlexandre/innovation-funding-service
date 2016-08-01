package com.worth.ifs.application.service;

import com.google.common.collect.Lists;
import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.competition.builder.CompetitionResourceBuilder;
import com.worth.ifs.competition.resource.CompetitionCountResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competition.resource.CompetitionTypeResource;
import com.worth.ifs.competition.service.CompetitionsRestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static com.worth.ifs.competition.builder.CompetitionTypeResourceBuilder.newCompetitionTypeResource;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;


/**
 * Test Class for all functionality in {@link CompetitionServiceImpl}
 */
public class CompetitionServiceImplTest extends BaseServiceUnitTest<CompetitionService> {

    @Mock
    private CompetitionsRestService competitionsRestService;

    @Override
    protected CompetitionService supplyServiceUnderTest() {
        return new CompetitionServiceImpl();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void test_getById() throws Exception {
        CompetitionResource competitionResource = newCompetitionResource().withId(1L).build();

        when(competitionsRestService.getCompetitionById(1L)).thenReturn(restSuccess(competitionResource));

        final CompetitionResource found = service.getById(1L);
        assertEquals(Long.valueOf(1L), found.getId());
    }

    @Test
    public void test_create() throws Exception {
        CompetitionResource competitionResource = newCompetitionResource().withId(1L).build();

        when(competitionsRestService.create()).thenReturn(restSuccess(competitionResource));

        final CompetitionResource found = service.create();
        assertEquals(Long.valueOf(1L), found.getId());
    }

    @Test
    public void test_getAllCompetitions() throws Exception {
        CompetitionResource comp1 = newCompetitionResource().withName("Competition 1").withId(1L).build();

        CompetitionResource comp2 = newCompetitionResource().withName("Competition 2").withId(2L).build();

        final List<CompetitionResource> expected = new ArrayList<>(asList(comp1, comp2));
        when(competitionsRestService.getAll()).thenReturn(restSuccess(expected));

        final List<CompetitionResource> found = service.getAllCompetitions();
        assertEquals(2, found.size());
        assertEquals(Long.valueOf(1L), found.get(0).getId());
        assertEquals(Long.valueOf(2L), found.get(1).getId());
    }

    @Test
    public void test_getAllCompetitionsNotInSetup() throws Exception {
        CompetitionResource comp1 = newCompetitionResource().withName("Competition 1").withId(1L).withCompetitionStatus(CompetitionResource.Status.COMPETITION_SETUP).build();

        CompetitionResource comp2 = newCompetitionResource().withName("Competition 2").withId(2L).build();

        final List<CompetitionResource> expected = new ArrayList<>(asList(comp1, comp2));
        when(competitionsRestService.getAll()).thenReturn(restSuccess(expected));

        final List<CompetitionResource> found = service.getAllCompetitionsNotInSetup();
        assertEquals(1, found.size());
        assertEquals(Long.valueOf(2L), found.get(0).getId());
    }

    @Test
    public void test_getCompletedCompetitionSetupSectionStatusesByCompetitionId() throws Exception {
    	CompetitionResource comp = newCompetitionResource().withId(1L).build();
    	comp.getSectionSetupStatus().put(CompetitionSetupSection.INITIAL_DETAILS, true);
    	comp.getSectionSetupStatus().put(CompetitionSetupSection.ADDITIONAL_INFO, false);
    	comp.getSectionSetupStatus().put(CompetitionSetupSection.ELIGIBILITY, true);
    	
    	when(competitionsRestService.getCompetitionById(1L)).thenReturn(restSuccess(comp));
    	
    	List<CompetitionSetupSection> result = service.getCompletedCompetitionSetupSectionStatusesByCompetitionId(1L);
    	
    	assertEquals(2, result.size());
    	assertEquals(CompetitionSetupSection.INITIAL_DETAILS, result.get(0));
    	assertEquals(CompetitionSetupSection.ELIGIBILITY, result.get(1));
    }


    @Test
    public void test_getAllCompetitionTypes() throws Exception {
        CompetitionTypeResource type1 = newCompetitionTypeResource().withStateAid(false).withName("Type 1").withId(1L).build();

        CompetitionTypeResource type2 = newCompetitionTypeResource().withStateAid(false).withName("Type 2").withId(2L).build();

        final List<CompetitionTypeResource> expected = new ArrayList<>(asList(type1, type2));

        when(competitionsRestService.getCompetitionTypes()).thenReturn(restSuccess(expected));

        final List<CompetitionTypeResource> found = service.getAllCompetitionTypes();
        assertEquals(2, found.size());
        assertEquals(Long.valueOf(1L), found.get(0).getId());
        assertEquals(Long.valueOf(2L), found.get(1).getId());
    }

    @Test
    public void test_generateCompetitionCode() throws Exception {
        final String expected = "201606-01";
        when(competitionsRestService.generateCompetitionCode(1L, LocalDateTime.of(2016, 06, 16, 0, 0, 0))).thenReturn(restSuccess(expected));

        final String found = service.generateCompetitionCode(1L, LocalDateTime.of(2016, 06, 16, 0, 0, 0));
        assertEquals(expected, found);
    }

    @Test
    public void test_getLiveCompetitions() throws Exception {
        CompetitionResource resource1 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.OPEN).build();
        CompetitionResource resource2 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.OPEN).build();
        CompetitionResource resource3 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.IN_ASSESSMENT).build();
        when(competitionsRestService.findLiveCompetitions()).thenReturn(restSuccess(Lists.newArrayList(resource1, resource2, resource3)));

        Map<CompetitionResource.Status, List<CompetitionResource>> result = service.getLiveCompetitions();

        assertTrue(result.get(CompetitionResource.Status.OPEN).contains(resource1));
        assertTrue(result.get(CompetitionResource.Status.OPEN).contains(resource2));
        assertTrue(result.get(CompetitionResource.Status.IN_ASSESSMENT).contains(resource3));
        assertEquals(result.get(CompetitionResource.Status.ASSESSOR_FEEDBACK), null);
    }

    @Test
    public void test_getProjectSetupCompetitions() throws Exception {
        CompetitionResource resource1 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.PROJECT_SETUP).build();
        CompetitionResource resource2 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.PROJECT_SETUP).build();
        when(competitionsRestService.findProjectSetupCompetitions()).thenReturn(restSuccess(Lists.newArrayList(resource1, resource2)));

        Map<CompetitionResource.Status, List<CompetitionResource>> result = service.getProjectSetupCompetitions();

        assertTrue(result.get(CompetitionResource.Status.PROJECT_SETUP).contains(resource1));
        assertTrue(result.get(CompetitionResource.Status.PROJECT_SETUP).contains(resource2));
        assertEquals(result.get(CompetitionResource.Status.ASSESSOR_FEEDBACK), null);
    }

    @Test
    public void test_getUpcomingCompetitions() throws Exception {
        CompetitionResource resource1 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.COMPETITION_SETUP).build();
        CompetitionResource resource2 = CompetitionResourceBuilder.newCompetitionResource().withCompetitionStatus(CompetitionResource.Status.NOT_STARTED).build();
        when(competitionsRestService.findUpcomingCompetitions()).thenReturn(restSuccess(Lists.newArrayList(resource1, resource2)));

        Map<CompetitionResource.Status, List<CompetitionResource>> result = service.getUpcomingCompetitions();

        assertTrue(result.get(CompetitionResource.Status.COMPETITION_SETUP).contains(resource1));
        assertTrue(result.get(CompetitionResource.Status.NOT_STARTED).contains(resource2));
        assertEquals(result.get(CompetitionResource.Status.ASSESSOR_FEEDBACK), null);
    }

    @Test
    public void test_getCompetitionCounts() throws Exception {
        CompetitionCountResource resource = new CompetitionCountResource();
        when(competitionsRestService.countCompetitions()).thenReturn(restSuccess(resource));

        CompetitionCountResource result = service.getCompetitionCounts();

        assertEquals(result, resource);
    }
}