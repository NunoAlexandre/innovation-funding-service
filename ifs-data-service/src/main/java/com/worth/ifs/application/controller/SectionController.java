package com.worth.ifs.application.controller;

import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.application.transactional.SectionService;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.rest.ValidationMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SectionController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @RequestMapping("/{sectionId}")
    public RestResult<SectionResource> getById(@PathVariable("sectionId") final Long sectionId) {
        return sectionService.getById(sectionId).toGetResponse();
    }

    @RequestMapping("/getCompletedSectionsByOrganisation/{applicationId}")
    public RestResult<Map<Long, Set<Long>>> getCompletedSectionsMap(@PathVariable("applicationId") final Long applicationId) {
        return sectionService.getCompletedSections(applicationId).toGetResponse();
    }

    @RequestMapping("/getCompletedSections/{applicationId}/{organisationId}")
    public RestResult<Set<Long>> getCompletedSections(@PathVariable("applicationId") final Long applicationId,
                                          @PathVariable("organisationId") final Long organisationId) {

        return sectionService.getCompletedSections(applicationId, organisationId).toGetResponse();
    }

    @RequestMapping("/markAsComplete/{sectionId}/{applicationId}/{markedAsCompleteById}")
    public RestResult<List<ValidationMessages>> markAsComplete(@PathVariable("sectionId") final Long sectionId,
                                                               @PathVariable("applicationId") final Long applicationId,
                                                               @PathVariable("markedAsCompleteById") final Long markedAsCompleteById) {
        return sectionService.markSectionAsComplete(sectionId, applicationId, markedAsCompleteById).toGetResponse();
    }

    @RequestMapping("/markAsInComplete/{sectionId}/{applicationId}/{markedAsCompleteById}")
    public RestResult<Void> markAsInComplete(@PathVariable("sectionId") final Long sectionId,
                                           @PathVariable("applicationId") final Long applicationId,
                                           @PathVariable("markedAsCompleteById") final Long markedAsCompleteById) {
        return sectionService.markSectionAsInComplete(sectionId, applicationId, markedAsCompleteById).toPutResponse();
    }

    @RequestMapping("/allSectionsMarkedAsComplete/{applicationId}")
    public RestResult<Boolean> getCompletedSections(@PathVariable("applicationId") final Long applicationId) {
        return sectionService.childSectionsAreCompleteForAllOrganisations(null, applicationId, null).toGetResponse();
    }

    @RequestMapping("/getIncompleteSections/{applicationId}")
    public RestResult<List<Long>> getIncompleteSections(@PathVariable("applicationId") final Long applicationId) {
        return sectionService.getIncompleteSections(applicationId).toGetResponse();
    }

    @RequestMapping("findByName/{name}")
    public RestResult<SectionResource> findByName(@PathVariable("name") final String name) {
        return sectionService.findByName(name).toGetResponse();
    }

    @RequestMapping("/getNextSection/{sectionId}")
    public RestResult<SectionResource> getNextSection(@PathVariable("sectionId") final Long sectionId) {
        return sectionService.getNextSection(sectionId).toGetResponse();
    }

    @RequestMapping("/getPreviousSection/{sectionId}")
    public RestResult<SectionResource> getPreviousSection(@PathVariable("sectionId") final Long sectionId) {
        return sectionService.getPreviousSection(sectionId).toGetResponse();
    }

    @RequestMapping("/getSectionByQuestionId/{questionId}")
    public RestResult<SectionResource> getSectionByQuestionId(@PathVariable("questionId") final Long questionId) {
        return sectionService.getSectionByQuestionId(questionId).toGetResponse();
    }

    @RequestMapping("/getQuestionsForSectionAndSubsections/{sectionId}")
    public RestResult<Set<Long>> getQuestionsForSectionAndSubsections(@PathVariable("sectionId") final Long sectionId){
        return sectionService.getQuestionsForSectionAndSubsections(sectionId).toGetResponse();
    }
}
