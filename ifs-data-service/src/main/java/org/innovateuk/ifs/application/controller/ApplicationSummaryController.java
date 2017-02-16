package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.transactional.ApplicationSummaryService;
import org.innovateuk.ifs.application.transactional.CompetitionSummaryService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApplicationSummaryController exposes application summary data and operations through a REST API.
 * It is mainly used at present for getting summaries of applications for showing in the competition manager views.
 */
@RestController
@RequestMapping("/applicationSummary")
public class ApplicationSummaryController {
    private static final String DEFAULT_PAGE_SIZE = "20";

    @Autowired
    private ApplicationSummaryService applicationSummaryService;

    @Autowired
    private CompetitionSummaryService competitionSummaryService;

    @RequestMapping("/findByCompetition/{competitionId}")
    public RestResult<ApplicationSummaryPageResource> getApplicationSummaryByCompetitionId(
            @PathVariable("competitionId") long competitionId,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "filter", required = false) String filter) {
        return applicationSummaryService.getApplicationSummariesByCompetitionId(competitionId, sortBy, pageIndex, pageSize, filter).toGetResponse();
    }

    @RequestMapping("/getCompetitionSummary/{id}")
    public RestResult<CompetitionSummaryResource> getCompetitionSummary(@PathVariable("id") Long id) {
        return competitionSummaryService.getCompetitionSummaryByCompetitionId(id).toGetResponse();
    }

    @RequestMapping("/findByCompetition/{competitionId}/submitted")
    public RestResult<ApplicationSummaryPageResource> getSubmittedApplicationSummariesByCompetitionId(
            @PathVariable("competitionId") long competitionId,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "filter", required = false) String filter) {
        return applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(competitionId, sortBy, pageIndex, pageSize, filter).toGetResponse();
    }

    @RequestMapping("/findByCompetition/{competitionId}/not-submitted")
    public RestResult<ApplicationSummaryPageResource> getNotSubmittedApplicationSummariesByCompetitionId(
            @PathVariable("competitionId") long competitionId,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize
    ) {
        return applicationSummaryService.getNotSubmittedApplicationSummariesByCompetitionId(competitionId, sortBy, pageIndex, pageSize).toGetResponse();
    }

    @RequestMapping("/findByCompetition/{competitionId}/feedback-required")
    public RestResult<ApplicationSummaryPageResource> getFeedbackRequiredApplicationSummariesByCompetitionId(
            @PathVariable("competitionId") long competitionId,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize
    ) {
        return applicationSummaryService.getFeedbackRequiredApplicationSummariesByCompetitionId(competitionId, sortBy, pageIndex, pageSize).toGetResponse();
    }

}
