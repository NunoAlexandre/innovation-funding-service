package com.worth.ifs.application.controller;

import com.worth.ifs.application.resource.ApplicationSummaryPageResource;
import com.worth.ifs.application.resource.ApplicationSummaryResource;
import com.worth.ifs.application.transactional.ApplicationSummaryService;
import com.worth.ifs.commons.rest.RestResult;
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
    @Autowired
    ApplicationSummaryService applicationSummaryService;

    @RequestMapping("/findByCompetition/{competitionId}")
    public RestResult<ApplicationSummaryPageResource> getApplicationSummaryByCompetitionId(@PathVariable("competitionId") Long competitionId, @RequestParam(value="page", defaultValue="0") int pageIndex, @RequestParam(value="sort", required=false) String sortBy) {
        return applicationSummaryService.getApplicationSummariesByCompetitionId(competitionId, pageIndex, sortBy).toGetResponse();
    }

    @RequestMapping("/{id}")
    public RestResult<ApplicationSummaryResource> getApplicationSummaryById(@PathVariable("id") Long id){
        return applicationSummaryService.getApplicationSummaryById(id).toGetResponse();
    }
}
