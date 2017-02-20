package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.mapper.ApplicationCountSummaryMapper;
import org.innovateuk.ifs.application.mapper.ApplicationCountSummaryPageMapper;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.repository.ApplicationStatisticsRepository;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

@Service
public class ApplicationCountSummaryServiceImpl extends BaseTransactionalService implements ApplicationCountSummaryService {

    @Autowired
    private ApplicationCountSummaryPageMapper applicationCountSummaryPageMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OrganisationRepository processRoleRepository;

    @Autowired
    private ApplicationStatisticsRepository applicationStatisticsRepository;

    @Override
    public ServiceResult<ApplicationCountSummaryPageResource> getApplicationCountSummariesByCompetitionId(Long competitionId, int pageIndex, int pageSize, String filter) {
        String filterStr = (filter != null) ? filter : "";
        Pageable pageable = new PageRequest(pageIndex, pageSize);
        Page<ApplicationStatistics> applicationStatistics = applicationStatisticsRepository.findByCompetition(competitionId, filterStr, pageable);

        List<Organisation> organisations = organisationRepository.findAll(simpleMap(applicationStatistics.getContent(), ApplicationStatistics::getLeadOrganisationId));

        return find(applicationStatistics, notFoundError(Page.class)).andOnSuccessReturn(applicationCountSummaryPageMapper::mapToResource);

        /* TODO Figure out how to get the organisation name mapped correctly.
        ApplicationCountSummaryPageResource pagedStatistics = applicationCountSummaryPageMapper.mapToResource(applicationStatistics);
        return serviceSuccess(simpleMap(applicationStatistics.getContent(), applicationStats -> {
            ApplicationCountSummaryResource summaryResource = applicationCountSummaryMapper.mapToResource(applicationStats);
            summaryResource.setLeadOrganisation(
                    organisations.stream()
                            .filter(organisation -> organisation.getId().equals(applicationStats.getLeadOrganisationId()))
                            .findFirst()
                            .map(Organisation::getName)
                            .orElse("")
            );

            return summaryResource;
        }));
         */
    }
}
