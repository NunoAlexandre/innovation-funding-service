package com.worth.ifs.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.InviteCollaboratorResource;
import com.worth.ifs.application.transactional.ApplicationService;
import com.worth.ifs.application.transactional.SectionService;
import com.worth.ifs.commons.controller.ServiceFailureToJsonResponseHandler;
import com.worth.ifs.commons.controller.SimpleServiceFailureToJsonResponseHandler;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.finance.handler.ApplicationFinanceHandler;
import com.worth.ifs.notifications.resource.Notification;
import com.worth.ifs.transactional.ServiceResult;
import com.worth.ifs.user.domain.UserRoleType;
import com.worth.ifs.util.JsonStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.worth.ifs.application.transactional.ApplicationServiceImpl.ServiceFailures.UNABLE_TO_SEND_NOTIFICATION;
import static com.worth.ifs.commons.controller.ControllerErrorHandlingUtil.handleServiceFailure;
import static com.worth.ifs.commons.controller.ControllerErrorHandlingUtil.handlingErrors;
import static com.worth.ifs.transactional.BaseTransactionalService.Failures.APPLICATION_NOT_FOUND;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.JsonStatusResponse.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@ExposesResourceFor(ApplicationResource.class)
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    ApplicationFinanceHandler applicationFinanceHandler;
    @Autowired
    ApplicationService applicationService;
    @Autowired
    SectionService sectionService;

    @Autowired
    ApplicationMapper applicationMapper;

    private List<ServiceFailureToJsonResponseHandler> serviceFailureHandlers = asList(
        new SimpleServiceFailureToJsonResponseHandler(singletonList(APPLICATION_NOT_FOUND), (serviceFailure, response) -> badRequest("Unable to find Application", response)),
        new SimpleServiceFailureToJsonResponseHandler(singletonList(UNABLE_TO_SEND_NOTIFICATION), (serviceFailure, response) -> internalServerError("Unable to send Notification", response))
    );

    @RequestMapping("/normal/{id}")
    public ApplicationResource getApplicationById(@PathVariable("id") final Long id) {
        Application application = applicationService.getApplicationById(id);
        ApplicationResource applicationResource = applicationMapper.mapApplicationToResource(application);
        return applicationResource;
    }


    @RequestMapping("/")
    public List<ApplicationResource> findAll() {
        return simpleMap(applicationService.findAll(), applicationMapper::mapApplicationToResource);
    }

    @RequestMapping("/findByUser/{userId}")
    public List<ApplicationResource> findByUserId(@PathVariable("userId") final Long userId) {
        return simpleMap(applicationService.findByUserId(userId), applicationMapper::mapApplicationToResource);
    }

    @RequestMapping("/saveApplicationDetails/{id}")
    public ResponseEntity<String> saveApplicationDetails(@PathVariable("id") final Long id,
                                                         @RequestBody ApplicationResource application) {
        return applicationService.saveApplicationDetails(id, application);
    }

    @RequestMapping("/getProgressPercentageByApplicationId/{applicationId}")
    public ObjectNode getProgressPercentageByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.getProgressPercentageNodeByApplicationId(applicationId);
    }

    @RequestMapping(value = "/updateApplicationStatus", method = RequestMethod.GET)
    public ResponseEntity<String> updateApplicationStatus(@RequestParam("applicationId") final Long id,
                                                          @RequestParam("statusId") final Long statusId) {

        return applicationService.updateApplicationStatus(id, statusId);
    }


    @RequestMapping("/applicationReadyForSubmit/{applicationId}")
    public ObjectNode applicationReadyForSubmit(@PathVariable("applicationId") final Long id){
        Application application = applicationService.getApplicationById(id);
        Competition competition = application.getCompetition();
        double progress = applicationService.getProgressPercentageByApplicationId(id);
        double researchParticipation = applicationFinanceHandler.getResearchParticipationPercentage(id).doubleValue();
        boolean allSectionsComplete = sectionService.childSectionsAreCompleteForAllOrganisations(null, id, null);

        boolean readyForSubmit = false;
        if(allSectionsComplete &&
                progress == 100 &&
                researchParticipation <= competition.getMaxResearchRatio()){

            readyForSubmit = true;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("readyForSubmit", readyForSubmit);
        node.put("progress", progress);
        node.put("researchParticipation",researchParticipation);
        node.put("researchParticipationValid", (researchParticipation <=competition.getMaxResearchRatio()) );
        node.put("allSectionComplete", allSectionsComplete);
        return node;
    }


    @RequestMapping("/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}")
    public List<ApplicationResource> getApplicationsByCompetitionIdAndUserId(@PathVariable("competitionId") final Long competitionId,
                                                                     @PathVariable("userId") final Long userId,
                                                                     @PathVariable("role") final UserRoleType role) {
        return simpleMap(applicationService.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role), applicationMapper::mapApplicationToResource);
    }

    @RequestMapping(value = "/createApplicationByName/{competitionId}/{userId}", method = RequestMethod.POST)
    public ApplicationResource createApplicationByApplicationNameForUserIdAndCompetitionId(
            @PathVariable("competitionId") final Long competitionId,
            @PathVariable("userId") final Long userId,
            @RequestBody JsonNode jsonObj) {
        return applicationMapper.mapApplicationToResource(applicationService.createApplicationByApplicationNameForUserIdAndCompetitionId(competitionId, userId, jsonObj));
    }

    @RequestMapping(value = "/{applicationId}/invitecollaborator", method = RequestMethod.POST)
    public JsonStatusResponse inviteCollaborator(
            @PathVariable("applicationId") final Long applicationId,
            @RequestBody InviteCollaboratorResource invite,
            HttpServletResponse response) {

        ServiceResult<Notification> inviteResult = handlingErrors(() -> applicationService.inviteCollaboratorToApplication(applicationId, invite));

        return inviteResult.mapLeftOrRight(
                failure -> handleServiceFailure(failure, serviceFailureHandlers, response).orElseGet(() -> internalServerError("Unable to send Notification to invitee", response)),
                success -> accepted("Notification sent successfully", response)
        );
    }

}