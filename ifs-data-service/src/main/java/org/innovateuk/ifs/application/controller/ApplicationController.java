package org.innovateuk.ifs.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompletedPercentageResource;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @RequestMapping("/{id}")
    public RestResult<ApplicationResource> getApplicationById(@PathVariable("id") final Long id) {
            return applicationService.getApplicationById(id).toGetResponse();
    }

    @RequestMapping("/")
    public RestResult<List<ApplicationResource>> findAll() {
        return applicationService.findAll().toGetResponse();
    }

    @RequestMapping("/findByUser/{userId}")
    public RestResult<List<ApplicationResource>> findByUserId(@PathVariable("userId") final Long userId) {
        return applicationService.findByUserId(userId).toGetResponse();
    }

    @RequestMapping("/saveApplicationDetails/{id}")
    public RestResult<Void> saveApplicationDetails(@PathVariable("id") final Long id,
                                                   @RequestBody ApplicationResource application) {

        return applicationService.saveApplicationDetails(id, application).toPostResponse();
    }

    @RequestMapping("/getProgressPercentageByApplicationId/{applicationId}")
    public RestResult<CompletedPercentageResource> getProgressPercentageByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.getProgressPercentageByApplicationId(applicationId).toGetResponse();
    }

    @PutMapping("/updateApplicationStatus")
    public RestResult<Void> updateApplicationStatus(@RequestParam("applicationId") final Long id,
                                                          @RequestParam("statusId") final Long statusId) {
        ServiceResult<ApplicationResource> updateStatusResult = applicationService.updateApplicationStatus(id, statusId);

        if(updateStatusResult.isSuccess() && ApplicationStatusConstants.SUBMITTED.getId().equals(statusId)){
            applicationService.saveApplicationSubmitDateTime(id, LocalDateTime.now());
            applicationService.sendNotificationApplicationSubmitted(id);
        }

        return updateStatusResult.toPutResponse();
    }


    @RequestMapping("/applicationReadyForSubmit/{applicationId}")
    public RestResult<ObjectNode> applicationReadyForSubmit(@PathVariable("applicationId") final Long id){
        return applicationService.applicationReadyForSubmit(id).toGetResponse();
    }


    @RequestMapping("/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}")
    public RestResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(@PathVariable("competitionId") final Long competitionId,
                                                                     @PathVariable("userId") final Long userId,
                                                                     @PathVariable("role") final UserRoleType role) {

        return applicationService.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role).toGetResponse();
    }

    @PostMapping("/createApplicationByName/{competitionId}/{userId}")
    public RestResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            @PathVariable("competitionId") final Long competitionId,
            @PathVariable("userId") final Long userId,
            @RequestBody JsonNode jsonObj) {

        String name = jsonObj.get("name").textValue();
        ServiceResult<ApplicationResource> applicationResult =
                applicationService.createApplicationByApplicationNameForUserIdAndCompetitionId(name, competitionId, userId);
        return applicationResult.toPostCreateResponse();
    }
}
