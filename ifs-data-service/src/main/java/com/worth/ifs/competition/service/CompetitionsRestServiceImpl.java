package com.worth.ifs.competition.service;

import java.util.List;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.competition.resource.CompetitionResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.competitionResourceListType;

/**
 * CompetitionsRestServiceImpl is a utility for CRUD operations on {@link Competition}.
 * This class connects to the {@link com.worth.ifs.competition.controller.CompetitionController}
 * through a REST call.
 */
@Service
public class CompetitionsRestServiceImpl extends BaseRestService implements CompetitionsRestService {

    @Value("${ifs.data.service.rest.competition}")
    String competitionsRestURL;

    @SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(getClass());

    public RestResult<List<CompetitionResource>> getAll() {
        return getWithRestResult(competitionsRestURL + "/findAll", competitionResourceListType());
    }

    public RestResult<CompetitionResource> getCompetitionById(Long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId, CompetitionResource.class);
    }
}
