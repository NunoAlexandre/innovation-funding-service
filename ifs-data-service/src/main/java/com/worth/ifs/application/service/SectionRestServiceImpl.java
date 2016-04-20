package com.worth.ifs.application.service;

import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.longsListType;
import static com.worth.ifs.commons.service.ParameterizedTypeReferences.longsSetType;

/**
 * SectionRestServiceImpl is a utility for CRUD operations on {@link Section}.
 * This class connects to the {@link com.worth.ifs.application.controller.SectionController}
 * through a REST call.
 */
@Service
public class SectionRestServiceImpl extends BaseRestService implements SectionRestService {

    @Value("${ifs.data.service.rest.section}")
    String sectionRestURL;

    @Override
    public RestResult<SectionResource> getById(Long sectionId) {
        return getWithRestResult(sectionRestURL + "/" + sectionId, SectionResource.class);
    }

    @Override
    public RestResult<Map<Long, Set<Long>>> getCompletedSectionsByOrganisation(Long applicationId) {
        return getWithRestResult(sectionRestURL + "/getCompletedSectionsByOrganisation/" + applicationId, new ParameterizedTypeReference<Map<Long, Set<Long>>>() {});
    }

    @Override
    public RestResult<List<Long>> getCompletedSectionIds(Long applicationId, Long organisationId) {
        return getWithRestResult(sectionRestURL + "/getCompletedSections/" + applicationId + "/" + organisationId, longsListType());
    }

    @Override
    public RestResult<List<Long>> getIncompletedSectionIds(Long applicationId) {
        return getWithRestResult(sectionRestURL + "/getIncompleteSections/" + applicationId, longsListType());
    }

    @Override
    public RestResult<Boolean> allSectionsMarkedAsComplete(Long applicationId) {
        return getWithRestResult(sectionRestURL + "/allSectionsMarkedAsComplete/" + applicationId, Boolean.class);
    }

    @Override
    public Future<RestResult<SectionResource>> getPreviousSection(Long sectionId) {
        return getWithRestResultAsync(sectionRestURL + "/getPreviousSection/" + sectionId, SectionResource.class);
    }

    @Override
    public Future<RestResult<SectionResource>> getNextSection(Long sectionId) {
        return getWithRestResultAsync(sectionRestURL + "/getNextSection/" + sectionId, SectionResource.class);
    }

    @Override
    public RestResult<SectionResource> getSectionByQuestionId(Long questionId) {
        return getWithRestResult(sectionRestURL + "/getSectionByQuestionId/" + questionId, SectionResource.class);
    }

    @Override
    public RestResult<Set<Long>> getQuestionsForSectionAndSubsections(Long sectionId) {
        return getWithRestResult(sectionRestURL + "/getQuestionsForSectionAndSubsections/" + sectionId, longsSetType());
    }

	@Override
	public RestResult<SectionResource> getFinanceSectionForCompetition(Long competitionId) {
		return getWithRestResult(sectionRestURL + "/getFinanceSectionByCompetitionId/" + competitionId, SectionResource.class);
	}
}
