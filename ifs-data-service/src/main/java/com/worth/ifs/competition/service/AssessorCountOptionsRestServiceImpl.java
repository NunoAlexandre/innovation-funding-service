package com.worth.ifs.competition.service;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.competition.domain.AssessorCountOption;
import com.worth.ifs.competition.resource.AssessorCountOptionResource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.worth.ifs.commons.service.ParameterizedTypeReferences.competitionTypeAssessorOptionResourceListType;

/**
 * AssessorCountOptionsRestServiceImpl is a utility for CRUD operations on {@link AssessorCountOption}.
 */
@Service
public class AssessorCountOptionsRestServiceImpl extends BaseRestService implements AssessorCountOptionsRestService {

    @Override
    public RestResult<List<AssessorCountOptionResource>> findAllByCompetitionType(Long competitionTypeId) {
        return getWithRestResult("/assessor-count-options/" + competitionTypeId, competitionTypeAssessorOptionResourceListType());
    }
}