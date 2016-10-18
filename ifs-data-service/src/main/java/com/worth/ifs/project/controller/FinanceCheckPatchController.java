package com.worth.ifs.project.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.project.transactional.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * ProjectController exposes Project data and operations through a REST API.
 */
@RestController
@RequestMapping("/project/patch")
public class FinanceCheckPatchController {
    @Autowired
    ProjectService projectService;

    @RequestMapping(value = "/generateFinanceChecksForAllProjects", method = POST)
    public RestResult<Void> generateFinanceChecksForAllProjects(){
        return projectService.generateFinanceChecksForAllProjects().toGetResponse();
    }
}