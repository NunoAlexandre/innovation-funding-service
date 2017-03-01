package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.PageableMatcher;
import org.innovateuk.ifs.application.domain.ApplicationStatistics;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.Role;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.innovateuk.ifs.application.builder.ApplicationCountSummaryResourceBuilder.newApplicationCountSummaryResource;
import static org.innovateuk.ifs.application.builder.ApplicationStatisticsBuilder.newApplicationStatistics;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.resource.UserRoleType.APPLICANT;
import static org.innovateuk.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ApplicationCountSummaryServiceImpl}
 */
public class ApplicationCountSummaryServiceImplTest extends BaseServiceUnitTest<ApplicationCountSummaryService> {

    @Override
    protected ApplicationCountSummaryService supplyServiceUnderTest() {
        return new ApplicationCountSummaryServiceImpl();
    }

    @Test
    public void getApplicationCountSummariesByCompetitionId() {
        Long competitionId = 1L;
        Role leadApplicationRole = newRole()
                .withType(LEADAPPLICANT)
                .build();
        Role applicantRole = newRole()
                .withType(APPLICANT)
                .build();

        List<ApplicationStatistics> applicationStatistics = newApplicationStatistics()
                .withProcessRoles(
                        newProcessRole()
                                .withRole(applicantRole, leadApplicationRole)
                                .build(2),
                        newProcessRole()
                                .withRole(leadApplicationRole, applicantRole)
                                .build(2)
                )
                .build(2);

        Page<ApplicationStatistics> page = mock(Page.class);
        when(page.getContent()).thenReturn(applicationStatistics);

        ApplicationCountSummaryPageResource resource = mock(ApplicationCountSummaryPageResource.class);

        when(applicationStatisticsRepositoryMock.findByCompetition(eq(competitionId),eq("%filter%"),argThat(new PageableMatcher(0,20)))).thenReturn(page);
        when(applicationCountSummaryPageMapperMock.mapToResource(page)).thenReturn(resource);

        ServiceResult<ApplicationCountSummaryPageResource> result = service.getApplicationCountSummariesByCompetitionId(competitionId,0,20, ofNullable("filter"));

        assertTrue(result.isSuccess());
        assertEquals(resource, result.getSuccessObject());
    }
}
