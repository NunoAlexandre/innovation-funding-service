package com.worth.ifs.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.service.MilestoneRestService;
import com.worth.ifs.competition.resource.MilestoneResource.MilestoneName;

import com.worth.ifs.commons.error.Error;

/**
 * This class contains methods to retrieve and store {@link MilestoneResource} related data,
 * through the RestService {@link MilestoneRestService}.
 */
@Service
public class MilestoneServiceImpl implements MilestoneService{

    @Autowired
    private MilestoneRestService milestoneRestService;

    @Override
    public List<MilestoneResource> getAllDatesByCompetitionId(Long competitionId) {
        return milestoneRestService.getAllDatesByCompetitionId(competitionId).getSuccessObjectOrThrowException();
    }

    @Override
    public List<Error> update(List<MilestoneResource> milestones, Long competitionId) {
       RestResult<Void> result = milestoneRestService.update(milestones, competitionId);
       if(result.isFailure()) {
    	   return result.getFailure().getErrors();
       }
       return new ArrayList<>();
    }

    @Override
    public MilestoneResource create(MilestoneName name, Long competitionId)
    {
        return milestoneRestService.create(name, competitionId).getSuccessObjectOrThrowException();
    }
}
