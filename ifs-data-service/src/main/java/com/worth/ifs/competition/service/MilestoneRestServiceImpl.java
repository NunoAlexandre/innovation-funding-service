package com.worth.ifs.competition.service;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.milestoneResourceListType;

import java.util.List;

import org.springframework.stereotype.Service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.competition.domain.Milestone;
import com.worth.ifs.competition.resource.MilestoneResource;

/**
 * MilestoneRestServiceImpl is a utility for CRUD operations on {@link Milestone}.
 * This class connects to the {@link com.worth.ifs.competition.controller.MilestoneController}
 * through a REST call.
 */
@Service
public class MilestoneRestServiceImpl extends BaseRestService implements MilestoneRestService {

    private String milestonesRestURL = "/milestone";

    @Override
    public RestResult<List<MilestoneResource>> getAllDatesByCompetitionId(Long competitionId) {
        return getWithRestResult(milestonesRestURL + "/" + competitionId, milestoneResourceListType());
    }

    @Override
    public RestResult<Void> update(List<MilestoneResource> milestones, Long competitionId) {
        return putWithRestResult(milestonesRestURL + "/" + competitionId, milestones, Void.class);
    }

    @Override
    public RestResult<MilestoneResource> create() {
        return postWithRestResult(milestonesRestURL + "", MilestoneResource.class);
    }
}
