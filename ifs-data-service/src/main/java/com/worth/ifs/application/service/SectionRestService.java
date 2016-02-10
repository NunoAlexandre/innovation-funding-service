package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Section;
import com.worth.ifs.commons.rest.RestResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Interface for CRUD operations on {@link Section} related data.
 */
public interface SectionRestService {
    RestResult<Section> getById(Long sectionId);
    RestResult<Map<Long, Set<Long>>> getCompletedSectionsByOrganisation(Long applicationId);
    RestResult<List<Long>> getCompletedSectionIds(Long applicationId, Long organisationId);
    RestResult<List<Long>> getIncompletedSectionIds(Long applicationId);
    RestResult<Section> getSection(String name);
    RestResult<Boolean> allSectionsMarkedAsComplete(Long applicationId);
    Future<RestResult<Section>> getPreviousSection(Long sectionId);
    Future<RestResult<Section>> getNextSection(Long sectionId);
    RestResult<Section> getSectionByQuestionId(Long questionId);
}
