package com.worth.ifs.application.service;

import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.resource.QuestionStatusResource;
import com.worth.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * This class contains methods to retrieve and store {@link QuestionResource} related data,
 * through the RestService {@link QuestionRestService}.
 */
// TODO DW - INFUND-1555 - handle rest results
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    QuestionRestService questionRestService;

    @Autowired
    QuestionStatusRestService questionStatusRestService;

    @Override
    public void assign(Long questionId, Long applicationId, Long assigneeId, Long assignedById) {
        questionRestService.assign(questionId, applicationId, assigneeId, assignedById);
    }

    @Override
    public void markAsComplete(Long questionId, Long applicationId, Long markedAsCompleteById) {
        questionRestService.markAsComplete(questionId, applicationId, markedAsCompleteById);
        questionRestService.assign(questionId, applicationId, 0L, 0L);
    }

    @Override
    public void markAsInComplete(Long questionId, Long applicationId, Long markedAsInCompleteById) {
        questionRestService.markAsInComplete(questionId, applicationId, markedAsInCompleteById);
    }

    @Override
    public List<QuestionResource> findByCompetition(Long competitionId) {
        return questionRestService.findByCompetition(competitionId).getSuccessObjectOrThrowException();
    }

    @Override
    public Map<Long, QuestionStatusResource> getQuestionStatusesForApplicationAndOrganisation(Long applicationId, Long userOrganisationId) {
        return mapToQuestionIds(questionStatusRestService.findByApplicationAndOrganisation(applicationId, userOrganisationId).getSuccessObjectOrThrowException());
    }

    private Map<Long, QuestionStatusResource> mapToQuestionIds(final List<QuestionStatusResource> questionStatusResources){

        final Map questionAssignees = new HashMap<Long, QuestionStatusResource>();

        for(QuestionStatusResource questionStatusResource : questionStatusResources){
            questionAssignees.put(questionStatusResource.getQuestion(), questionStatusResource);
        }

        return questionAssignees;
    }

    @Override
    public List<QuestionStatusResource> getNotificationsForUser(Collection<QuestionStatusResource> questionStatuses, Long userId) {
        return questionStatuses.stream().
                filter(qs ->  userId.equals(qs.getAssigneeUserId()) && (qs.getNotified() != null && qs.getNotified().equals(Boolean.FALSE)))
                .collect(Collectors.toList());
    }

    @Override
    public void removeNotifications(List<QuestionStatusResource> questionStatuses) {
        questionStatuses.stream().forEach(qs -> questionRestService.updateNotification(qs.getId(), true));
    }

    @Override
    public Future<Set<Long>> getMarkedAsComplete(Long applicationId, Long organisationId) {
        return questionRestService.getMarkedAsComplete(applicationId, organisationId);
    }

    @Override
    public QuestionResource getById(Long questionId) {
        return questionRestService.findById(questionId).getSuccessObjectOrThrowException();
    }

    @Override
    public Optional<QuestionResource> getNextQuestion(Long questionId) {
        return questionRestService.getNextQuestion(questionId).getOptionalSuccessObject();
    }

    @Override
    public Optional<QuestionResource> getPreviousQuestion(Long questionId) {
        return questionRestService.getPreviousQuestion(questionId).getOptionalSuccessObject();
    }

    @Override
    public Optional<QuestionResource> getPreviousQuestionBySection(Long sectionId) {
        return questionRestService.getPreviousQuestionBySection(sectionId).getOptionalSuccessObject();
    }

    @Override
    public Optional<QuestionResource> getNextQuestionBySection(Long sectionId) {
        return questionRestService.getNextQuestionBySection(sectionId).getOptionalSuccessObject();
    }

    @Override
    public RestResult<QuestionResource> getQuestionByFormInputType(String formInputType) {
        return questionRestService.getQuestionByFormInputType(formInputType);
    }

    @Override
    public QuestionStatusResource getByQuestionIdAndApplicationIdAndOrganisationId(Long questionId, Long applicationId, Long organisationId){
        List<QuestionStatusResource> questionStatuses = questionStatusRestService.getByQuestionIdAndApplicationIdAndOrganisationId(questionId, applicationId, organisationId).getSuccessObjectOrThrowException();
        if(questionStatuses == null || questionStatuses.isEmpty()){
            return null;
        }
        return questionStatuses.get(0);
    }

    @Override
    public Map<Long, QuestionStatusResource> getQuestionStatusesByQuestionIdsAndApplicationIdAndOrganisationId(List<Long> questionIds, Long applicationId, Long organisationId){
        return mapToQuestionIds(questionStatusRestService.getQuestionStatusesByQuestionIdsAndApplicationIdAndOrganisationId(questionIds, applicationId, organisationId).getSuccessObjectOrThrowException());
    }

    public List<QuestionStatusResource> findQuestionStatusesByQuestionAndApplicationId(Long questionId, Long applicationId) {
        return questionStatusRestService.findQuestionStatusesByQuestionAndApplicationId(questionId, applicationId).getSuccessObjectOrThrowException();
    }
}
