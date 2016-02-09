package com.worth.ifs.application.controller;

import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.transactional.SectionService;
import com.worth.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.worth.ifs.commons.rest.RestResultBuilder.newRestHandler;

/**
 * SectionController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @RequestMapping("/getById/{sectionId}")
    public RestResult<Section> getById(@PathVariable("sectionId") final Long sectionId) {
        return newRestHandler().perform(() -> sectionService.getById(sectionId));
    }

    @RequestMapping("/getCompletedSectionsByOrganisation/{applicationId}")
    public RestResult<Map<Long, Set<Long>>> getCompletedSectionsMap(@PathVariable("applicationId") final Long applicationId) {
        return newRestHandler().perform(() -> sectionService.getCompletedSections(applicationId));
    }

    @RequestMapping("/getCompletedSections/{applicationId}/{organisationId}")
    public RestResult<Set<Long>> getCompletedSections(@PathVariable("applicationId") final Long applicationId,
                                          @PathVariable("organisationId") final Long organisationId) {

        return newRestHandler().perform(() -> sectionService.getCompletedSections(applicationId, organisationId));
    }

    @RequestMapping("/allSectionsMarkedAsComplete/{applicationId}")
    public RestResult<Boolean> getCompletedSections(@PathVariable("applicationId") final Long applicationId) {
        return newRestHandler().perform(() -> sectionService.childSectionsAreCompleteForAllOrganisations(null, applicationId, null));
    }

    @RequestMapping("/getIncompleteSections/{applicationId}")
    public RestResult<List<Long>> getIncompleteSections(@PathVariable("applicationId") final Long applicationId) {
        return newRestHandler().perform(() -> sectionService.getIncompleteSections(applicationId));
    }

    @RequestMapping("findByName/{name}")
    public RestResult<Section> findByName(@PathVariable("name") final String name) {
        return newRestHandler().perform(() -> sectionService.findByName(name));
    }

    @RequestMapping("/getNextSection/{sectionId}")
    public RestResult<Section> getNextSection(@PathVariable("sectionId") final Long sectionId) {
        return newRestHandler().perform(() -> sectionService.getNextSection(sectionId));
    }

    @RequestMapping("/getPreviousSection/{sectionId}")
    public RestResult<Section> getPreviousSection(@PathVariable("sectionId") final Long sectionId) {
        return newRestHandler().perform(() -> sectionService.getPreviousSection(sectionId));
    }

    @RequestMapping("/getSectionByQuestionId/{questionId}")
    public RestResult<Section> getSectionByQuestionId(@PathVariable("questionId") final Long questionId) {
        return newRestHandler().perform(() -> sectionService.getSectionByQuestionId(questionId));
    }
}
