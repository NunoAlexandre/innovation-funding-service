package com.worth.ifs.application.service;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.ApplicationStatusResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.worth.ifs.application.service.ListenableFutures.adapt;
import static com.worth.ifs.application.service.ListenableFutures.call;

/**
 * This class contains methods to retrieve and store {@link ApplicationResource} related data,
 * through the RestService {@link ApplicationRestService}.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    ApplicationRestService applicationRestService;

    @Autowired
    ApplicationStatusRestService applicationStatusRestService;

    @Override
    public ApplicationResource getById(Long applicationId, Boolean... hateoas) {
        return applicationRestService.getApplicationById(applicationId);
    }

    @Override
    public List<ApplicationResource> getInProgress(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId);
        return applications.stream()
                .filter(a -> (fetchApplicationStatusFromId(a.getApplicationStatus()).getName().equals("created") || fetchApplicationStatusFromId(a.getApplicationStatus()).getName().equals("submitted")))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<ApplicationResource> getFinished(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId);
        return applications.stream()
                .filter(a -> (fetchApplicationStatusFromId(a.getApplicationStatus()).getName().equals("approved") || fetchApplicationStatusFromId(a.getApplicationStatus()).getName().equals("rejected")))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Map<Long, Integer> getProgress(Long userId) {
        List<ApplicationResource> applications = applicationRestService.getApplicationsByUserId(userId);
        Map<Long, Future<Integer>> applicationProgress = new HashMap<>();
        applications.stream()
                .filter(a -> fetchApplicationStatusFromId(a.getApplicationStatus()).getName().equals("created"))
                .map(ApplicationResource::getId)
                .forEach(id -> {
                    applicationProgress.put(id, adapt(applicationRestService.getCompleteQuestionsPercentage(id), Double::intValue));
                });
        return call(applicationProgress);
    }

    @Override
    public ApplicationResource createApplication(Long competitionId, Long userId, String applicationName) {
        ApplicationResource application = applicationRestService.createApplication(competitionId, userId, applicationName);

        return application;
    }

    @Override
    public Boolean isApplicationReadyForSubmit(Long applicationId) {
        return applicationRestService.isApplicationReadyForSubmit(applicationId);
    }

    @Override
    public void updateStatus(Long applicationId, Long statusId) {
        applicationRestService.updateApplicationStatus(applicationId, statusId);
    }

    @Override
    public ListenableFuture<Integer> getCompleteQuestionsPercentage(Long applicationId) {
        return adapt(applicationRestService.getCompleteQuestionsPercentage(applicationId), d -> d.intValue());
    }

    @Override
    public int getAssignedQuestionsCount(Long applicationId, Long processRoleId) {
        return applicationRestService.getAssignedQuestionsCount(applicationId, processRoleId).intValue();
    }

    @Override
    public void save(ApplicationResource application) {
        applicationRestService.saveApplication(application);
    }

    @Override
    public ApplicationResource findByProcessRoleId(Long id) {
        return applicationRestService.findByProcessRoleId(id);
    }

    private ApplicationStatusResource fetchApplicationStatusFromId(Long id) {
        return applicationStatusRestService.getApplicationStatusById(id);
    }

}
