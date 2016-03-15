package com.worth.ifs.competition.controller;

import java.util.List;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.transactional.CompetitionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/competition")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @RequestMapping("/{id}")
    public RestResult<CompetitionResource> getApplicationById(@PathVariable("id") final Long id) {
        return competitionService.getCompetitionById(id).toGetResponse();
    }

    @RequestMapping("/findAll")
    public RestResult<List<CompetitionResource>> findAll() {
        return competitionService.findAll().toGetResponse();
    }
}
