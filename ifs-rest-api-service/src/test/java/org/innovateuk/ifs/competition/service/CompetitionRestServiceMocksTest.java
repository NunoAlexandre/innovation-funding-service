
package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationPageResource;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.*;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;

/**
 *
 */
public class CompetitionRestServiceMocksTest extends BaseRestServiceUnitTest<CompetitionsRestServiceImpl> {

    private static final String competitionsRestURL = "/competition";

    @Override
    protected CompetitionsRestServiceImpl registerRestServiceUnderTest() {
        return new CompetitionsRestServiceImpl();
    }

    @Test
    public void getAll() {

        List<CompetitionResource> returnedResponse = newCompetitionResource().build(3);
        setupGetWithRestResultExpectations(competitionsRestURL + "/findAll", competitionResourceListType(), returnedResponse);
        List<CompetitionResource> responses = service.getAll().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void getCompetitionById() {

        CompetitionResource returnedResponse = new CompetitionResource();

        setupGetWithRestResultExpectations(competitionsRestURL + "/123", CompetitionResource.class, returnedResponse);

        CompetitionResource response = service.getCompetitionById(123L).getSuccessObject();
        assertNotNull(response);
        Assert.assertEquals(returnedResponse, response);
    }

    @Test
    public void findInnovationLeads() {

        List<UserResource> returnedResponse = asList(new UserResource(), new UserResource());

        setupGetWithRestResultExpectations(competitionsRestURL + "/123" + "/innovation-leads", userListType(), returnedResponse);

        List<UserResource> response = service.findInnovationLeads(123L).getSuccessObject();
        assertNotNull(response);
        Assert.assertEquals(returnedResponse, response);
    }

    @Test
    public void addInnovationLead() {

        setupPostWithRestResultExpectations(competitionsRestURL + "/123" + "/add-innovation-lead" + "/234", HttpStatus.OK);

        RestResult<Void> response = service.addInnovationLead(123L, 234L);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void removeInnovationLead() {

        setupPostWithRestResultExpectations(competitionsRestURL + "/123" + "/remove-innovation-lead" + "/234", HttpStatus.OK);

        RestResult<Void> response = service.removeInnovationLead(123L, 234L);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    public void getCompetitionsByUserId() {
        final Long userId = 123L;
        List<CompetitionResource> returnedResponse = newCompetitionResource().build(2);

        setupGetWithRestResultExpectations(competitionsRestURL + "/getCompetitionsByUserId/" + userId, competitionResourceListType(), returnedResponse);

        List<CompetitionResource> responses = service.getCompetitionsByUserId(userId).getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void getCompetitionTypes() {
        List<CompetitionTypeResource> returnedResponse = asList(new CompetitionTypeResource(), new CompetitionTypeResource());

        setupGetWithRestResultExpectations("/competition-type/findAll", competitionTypeResourceListType(), returnedResponse);

        List<CompetitionTypeResource> response = service.getCompetitionTypes().getSuccessObject();
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(returnedResponse, response);
    }

    @Test
    public void create() {
        CompetitionResource competition = new CompetitionResource();

        setupPostWithRestResultExpectations(competitionsRestURL + "", CompetitionResource.class, null, competition, HttpStatus.CREATED);

        CompetitionResource response = service.create().getSuccessObject();
        assertNotNull(response);
        Assert.assertEquals(competition, response);
    }

    @Test
    public void createNonIfs() {
        CompetitionResource competition = new CompetitionResource();

        setupPostWithRestResultExpectations(competitionsRestURL + "/non-ifs", CompetitionResource.class, null, competition, HttpStatus.CREATED);

        CompetitionResource response = service.createNonIfs().getSuccessObject();
        assertNotNull(response);
        Assert.assertEquals(competition, response);
    }


    @Test
    public void update() {
        CompetitionResource competition = new CompetitionResource();
        competition.setId(1L);

        setupPutWithRestResultExpectations(competitionsRestURL + "/" + competition.getId(), Void.class, competition, null, HttpStatus.OK);

        service.update(competition).getSuccessObject();
    }

    @Test
    public void updateCompetitionInitialDetails() {

        CompetitionResource competition = new CompetitionResource();
        competition.setId(1L);

        setupPutWithRestResultExpectations(competitionsRestURL + "/" + competition.getId() + "/update-competition-initial-details", Void.class, competition, null, HttpStatus.OK);

        RestResult<Void> response = service.updateCompetitionInitialDetails(competition);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void generateCompetitionCode() {
        ZonedDateTime openingDate = ZonedDateTime.of(2016, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        Long competitionId = Long.MAX_VALUE;
        String competitionCode = "1602-1";
        setupPostWithRestResultExpectations(String.format("%s/generateCompetitionCode/%s", competitionsRestURL, competitionId), String.class, openingDate, competitionCode, HttpStatus.OK);

        String response = service.generateCompetitionCode(competitionId, openingDate).getSuccessObject();
        assertNotNull(response);
        assertEquals(competitionCode, response);
    }

    @Test
    public void findLiveCompetitions() {
        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", singleton(""), 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/live", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findLiveCompetitions().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findProjectSetupCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", singleton(""), 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/project-setup", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findProjectSetupCompetitions().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findUpcomingCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", Collections.EMPTY_SET, 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/upcoming", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findUpcomingCompetitions().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findNonIfsCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", Collections.EMPTY_SET, 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/non-ifs", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findNonIfsCompetitions().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findUnsuccessfulApplications() {

        int pageNumber = 0;
        int pageSize = 20;
        String sortField = "id";

        ApplicationPageResource applicationPage = new ApplicationPageResource();

        setupGetWithRestResultExpectations(competitionsRestURL + "/123" + "/unsuccessful-applications?page=0&size=20&sort=id", ApplicationPageResource.class, applicationPage);

        ApplicationPageResource result = service.findUnsuccessfulApplications(123L, pageNumber, pageSize, sortField).getSuccessObject();
        assertNotNull(result);
        Assert.assertEquals(applicationPage, result);
    }

    @Test
    public void countCompetitions() {
        CompetitionCountResource returnedResponse = new CompetitionCountResource();

        setupGetWithRestResultExpectations(competitionsRestURL + "/count", CompetitionCountResource.class, returnedResponse);

        CompetitionCountResource responses = service.countCompetitions().getSuccessObject();
        assertNotNull(responses);
        Assert.assertEquals(returnedResponse, responses);
    }

    @Test
    public void searchCompetitions() {
        CompetitionSearchResult returnedResponse = new CompetitionSearchResult();
        String searchQuery = "SearchQuery";
        int page = 1;
        int size = 20;

        setupGetWithRestResultExpectations(competitionsRestURL + "/search/" + page + "/" + size + "?searchQuery=" + searchQuery, CompetitionSearchResult.class, returnedResponse);

        CompetitionSearchResult responses = service.searchCompetitions(searchQuery, page, size).getSuccessObject();
        assertNotNull(responses);
        Assert.assertEquals(returnedResponse, responses);
    }

    @Test
    public void markAsSetup() {
        long competitionId = 1L;
        setupPostWithRestResultExpectations(competitionsRestURL + "/" + competitionId + "/mark-as-setup", HttpStatus.OK);

        RestResult<Void> result = service.markAsSetup(competitionId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void returnToSetup() {
        long competitionId = 1L;
        setupPostWithRestResultExpectations(competitionsRestURL + "/" + competitionId + "/return-to-setup", HttpStatus.OK);

        RestResult<Void> result = service.returnToSetup(competitionId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void closeAssessment() {
        long competitionId = 1L;
        setupPutWithRestResultExpectations(competitionsRestURL + "/" + competitionId + "/close-assessment",HttpStatus.OK);

        RestResult<Void> result = service.closeAssessment(competitionId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void notifyAssessors() {
        long competitionId = 1L;
        setupPutWithRestResultExpectations(competitionsRestURL + "/" + competitionId + "/notify-assessors", HttpStatus.OK);

        RestResult<Void> result = service.notifyAssessors(competitionId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void releaseFeedback() {
        long competitionId = 1L;
        setupPutWithRestResultExpectations(competitionsRestURL + "/" + competitionId + "/release-feedback", HttpStatus.OK);

        RestResult<Void> result = service.releaseFeedback(competitionId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void findFeedbackReleasedCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", Collections.EMPTY_SET, 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/feedback-released", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findFeedbackReleasedCompetitions().getSuccessObject();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }
}
