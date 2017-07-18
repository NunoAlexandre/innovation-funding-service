package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.mapper.ApplicationCountSummaryPageMapper;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.repository.ApplicationStatisticsRepository;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl.SUBMITTED_STATES;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

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

    private static final Map<String, Sort> SORT_FIELD_TO_DB_SORT_FIELDS = new HashMap<String, Sort>() {{
        put("appTitle", new Sort(ASC, new String[]{"name", "id"}));
        put("leadOrg", new Sort(ASC, new String[]{"leadOrganisation", "id"}));
        put("assignedApps", new Sort(ASC, new String[]{"assessors", "id"}));
        put("completedApps", new Sort(ASC, new String[]{"submitted", "id"}));
    }};

    @Override
    public ServiceResult<ApplicationCountSummaryPageResource> getApplicationCountSummariesByCompetitionId(Long competitionId, int pageIndex, int pageSize, Optional<String> filter) {

        String filterStr = filter.map(String::trim).orElse("");
        Pageable pageable = new PageRequest(pageIndex, pageSize);
        Page<ApplicationStatistics> applicationStatistics = applicationStatisticsRepository.findByCompetitionAndApplicationProcessActivityStateStateIn(competitionId, SUBMITTED_STATES, filterStr, pageable);

        return find(applicationStatistics, notFoundError(Page.class)).andOnSuccessReturn(stats -> applicationCountSummaryPageMapper.mapToResource(stats));
    }

    @Override
    public ServiceResult<ApplicationCountSummaryPageResource> getApplicationCountSummariesByCompetitionIdAndInnovationArea(Long competitionId, int pageIndex, int pageSize, Optional<Long> innovationArea, String sortField) {
        Sort sort = getApplicationSummarySortField(sortField);
        Pageable pageable = new PageRequest(pageIndex, pageSize, sort);

        Page<ApplicationStatistics> applicationStatistics = innovationArea.map(i ->
                applicationStatisticsRepository.findByCompetitionAndInnovationAreaProcessActivityStateStateIn(competitionId, SUBMITTED_STATES, i, pageable))
                .orElse(applicationStatisticsRepository.findByCompetitionAndApplicationProcessActivityStateStateIn(competitionId, SUBMITTED_STATES, "", pageable));

        return find(applicationStatistics, notFoundError(Page.class)).andOnSuccessReturn(stats -> applicationCountSummaryPageMapper.mapToResource(stats));
    }

    private Sort getApplicationSummarySortField(String sortBy) {
        Sort result = SORT_FIELD_TO_DB_SORT_FIELDS.get(sortBy);
        return result != null ? result : new Sort(ASC, new String[]{"id"});
    }
}
