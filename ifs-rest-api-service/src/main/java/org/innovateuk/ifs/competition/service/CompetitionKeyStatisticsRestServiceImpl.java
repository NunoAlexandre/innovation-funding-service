package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.competition.resource.*;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

/**
 * Interface for retrieving Competition Key Statistics Resources}
 */
@Service
public class CompetitionKeyStatisticsRestServiceImpl extends BaseRestService implements CompetitionKeyStatisticsRestService {

    private String competitionKeyStatisticsRestURL = "/competitionStatistics";

    @Override
    public RestResult<CompetitionReadyToOpenKeyStatisticsResource> getReadyToOpenKeyStatisticsByCompetition(long competitionId) {
        return getWithRestResult(format("%s/%s/%s",competitionKeyStatisticsRestURL,competitionId,"readyToOpen"), CompetitionReadyToOpenKeyStatisticsResource.class);

    }

    @Override
    public RestResult<CompetitionOpenKeyStatisticsResource> getOpenKeyStatisticsByCompetition(long competitionId) {
        return getWithRestResult(format("%s/%s/%s",competitionKeyStatisticsRestURL,competitionId,"open"), CompetitionOpenKeyStatisticsResource.class);

    }

    @Override
    public RestResult<CompetitionClosedKeyStatisticsResource> getClosedKeyStatisticsByCompetition(long competitionId) {
        return getWithRestResult(format("%s/%s/%s",competitionKeyStatisticsRestURL,competitionId,"closed"), CompetitionClosedKeyStatisticsResource.class);

    }

    @Override
    public RestResult<CompetitionInAssessmentKeyStatisticsResource> getInAssessmentKeyStatisticsByCompetition(long competitionId) {
        return getWithRestResult(format("%s/%s/%s",competitionKeyStatisticsRestURL,competitionId,"inAssessment"), CompetitionInAssessmentKeyStatisticsResource.class);

    }
}
