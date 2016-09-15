package com.worth.ifs.project.status;

import com.worth.ifs.project.resource.CompetitionProjectsStatusResource;
import com.worth.ifs.project.service.ProjectStatusRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectStatusServiceImpl implements ProjectStatusService {

    @Autowired
    private ProjectStatusRestService projectStatusRestService;

    @Override
    public CompetitionProjectsStatusResource getCompetitionStatus(Long competitionId) {
        return projectStatusRestService.getCompetitionStatus(competitionId).getSuccessObjectOrThrowException();
    }
}
