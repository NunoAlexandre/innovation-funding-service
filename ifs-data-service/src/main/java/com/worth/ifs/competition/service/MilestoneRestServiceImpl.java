package com.worth.ifs.competition.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.competition.domain.Milestone;
import com.worth.ifs.competition.resource.MilestoneResource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.milestoneResourceListType;

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
}
