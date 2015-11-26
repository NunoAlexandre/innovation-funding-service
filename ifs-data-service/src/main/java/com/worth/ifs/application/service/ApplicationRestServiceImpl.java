package com.worth.ifs.application.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.controller.ApplicationController;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.user.domain.UserRoleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * ApplicationRestServiceImpl is a utility for CRUD operations on {@link Application}.
 * This class connects to the {@link ApplicationController}
 * through a REST call.
 */
@Service
public class ApplicationRestServiceImpl extends BaseRestService implements ApplicationRestService {
    @Value("${ifs.data.service.rest.application}")
    String applicationRestURL;

    private final Log log = LogFactory.getLog(getClass());

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Application getApplicationById(Long applicationId) {
        ParameterizedTypeReference<ApplicationResource> responseType = new ParameterizedTypeReference<ApplicationResource>() {};
        ResponseEntity<ApplicationResource> applicationResourceEntities = restTemplate.exchange(URI.create(dataRestServiceURL + applicationRestURL + "/" + applicationId), HttpMethod.GET, null, responseType);
        ApplicationResource applicationResource =applicationResourceEntities.getBody();
        return applicationResource.toApplication();

    }

    @Override
    public List<Application> getApplicationsByUserId(Long userId) {
        ParameterizedTypeReference<Resources<ApplicationResource>> responseType = new ParameterizedTypeReference<Resources<ApplicationResource>>() {};
        ResponseEntity<Resources<ApplicationResource>> applicationResourceEntities = restTemplate.exchange(dataRestServiceURL + applicationRestURL + "/findByUser/" + userId, HttpMethod.GET, null, responseType);
        Resources<ApplicationResource> applicationResources =applicationResourceEntities.getBody();

        Collection<ApplicationResource> resources = applicationResources.getContent();
        List<Application> applications = new ArrayList<>(resources.size());

        for (ApplicationResource resource : resources) {
            applications.add(resource.toApplication());
        }
        return applications;
    }

    @Override
    public void saveApplication(Application application) {
        log.debug("ApplicationRestRestService.saveApplication " + application.getId());

        ResponseEntity<String> response =
                restPostWithEntity(applicationRestURL + "/saveApplicationDetails/" + application.getId(), application, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("ApplicationRestRestService, save == ok : " + response.getBody());
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            //  bad credentials?
            log.info("Unauthorized save request.");
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.info("Status code not_found .....");
        }

    }

    @Override
    public void updateApplicationStatus(Long applicationId, Long statusId) {

        ResponseEntity<String> response =
                restGetEntity(applicationRestURL + "/updateApplicationStatus?applicationId=" + applicationId + "&statusId=" + statusId, String.class);

        log.debug("ApplicationRestRestService.updateApplicationStatus sending for applicationId " + applicationId);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("ApplicationRestRestService, save == ok : " + response.getBody());
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            //  bad credentials?
            log.info("Unauthorized request.");
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.info("Status code not_found .....");
        }
    }

    @Override
    public Double getCompleteQuestionsPercentage(Long applicationId) {
        if (applicationId == null) {
            log.error("No application and/org organisation id!!");
        }

        ObjectNode jsonResponse = restGet(applicationRestURL + "/getProgressPercentageByApplicationId/" + applicationId, ObjectNode.class);
        return jsonResponse.get("completedPercentage").asDouble();
    }

    @Override
    public List<Application> getApplicationsByCompetitionIdAndUserId(Long competitionID, Long userID, UserRoleType role) {
        return asList(restGet(applicationRestURL + "/getApplicationsByCompetitionIdAndUserId/" + competitionID + "/" + userID + "/" + role, Application[].class));
    }

    @Override
    public Integer getAssignedQuestionsCount(Long applicationId, Long assigneeId) {
//        http://localhost:8090/questionStatuses/search/countByApplicationIdAndAssigneeId?applicationId=1&assigneeId=9

        String count = restGet("/questionStatuses/search/countByApplicationIdAndAssigneeId?applicationId=" + applicationId + "&assigneeId=" + assigneeId, String.class);
        return Integer.valueOf(count);
    }

    @Override
    public Application createApplication(Long competitionId, Long userId, String applicationName) {
        Application application = new Application();
        application.setName(applicationName);

        String url = applicationRestURL + "/createApplicationByName/" + competitionId + "/" + userId;
        ResponseEntity<Application> creationResponse = restPut(url, application, Application.class);

        Application newApplication = creationResponse.getBody();

        return newApplication;
    }

}
