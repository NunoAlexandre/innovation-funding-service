package org.innovateuk.ifs.application.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ApplicationStatisticsRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<ApplicationStatisticsRepository> {

    public static final Collection<Long> SUBMITTED_STATUS_IDS = asList(
            ApplicationStatusConstants.APPROVED.getId(),
            ApplicationStatusConstants.REJECTED.getId(),
            ApplicationStatusConstants.SUBMITTED.getId());

    @Autowired
    @Override
    protected void setRepository(ApplicationStatisticsRepository repository) {
        this.repository = repository;
    }

    @Test
    public void findByCompetition() throws Exception {
        Long competitionId = 1L;

        List<ApplicationStatistics> statisticsList = repository.findByCompetitionAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS);
        assertEquals(5, statisticsList.size());

    }

    @Test
    public void findByCompetitionPaged() throws Exception {
        Long competitionId = 1L;

        Pageable pageable = new PageRequest(1, 3);

        Page<ApplicationStatistics> statisticsPage = repository.findByCompetitionAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS, "", pageable);
        assertEquals(5, statisticsPage.getTotalElements());
        assertEquals(3, statisticsPage.getSize());
        assertEquals(1, statisticsPage.getNumber());
    }

    @Test
    public void findByCompetitionFilterd() throws Exception {
        Long competitionId = 1L;

        Pageable pageable = new PageRequest(0, 20);

        Page<ApplicationStatistics> statisticsPage = repository.findByCompetitionAndApplicationStatusIdIn(competitionId, SUBMITTED_STATUS_IDS,"4", pageable);
        assertEquals(1, statisticsPage.getTotalElements());
        assertEquals(20, statisticsPage.getSize());
        assertEquals(0, statisticsPage.getNumber());
    }
}
