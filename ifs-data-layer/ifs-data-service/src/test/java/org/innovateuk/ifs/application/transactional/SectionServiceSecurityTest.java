package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.application.domain.Section;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.security.ApplicationLookupStrategy;
import org.innovateuk.ifs.application.security.SectionPermissionRules;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.application.transactional.SectionServiceSecurityTest.TestSectionService.ARRAY_SIZE_FOR_POST_FILTER_TESTS;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Module: innovation-funding-service
 **/
public class SectionServiceSecurityTest extends BaseServiceSecurityTest<SectionService> {

    private ApplicationLookupStrategy applicationLookupStrategy;
    private SectionPermissionRules sectionPermissionRules;

    @Before
    public void lookupPermissionRules() {
        sectionPermissionRules = getMockPermissionRulesBean(SectionPermissionRules.class);
        applicationLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationLookupStrategy.class);
    }

    @Test
    public void testPermissionForMarkSectionAsComplete() {
        Long sectionId = 1L;
        Long applicationId = 2L;
        Long markedAsCompleteById = 3L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().build());
        assertAccessDenied(
                () -> classUnderTest.markSectionAsComplete(sectionId, applicationId, markedAsCompleteById),
                () -> verify(sectionPermissionRules).onlyMemberOfProjectTeamCanMarkSectionAsComplete(isA(ApplicationResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testPermissionForMarkSectionAsInComplete() {
        Long sectionId = 1L;
        Long applicationId = 2L;
        Long markedAsCompleteById = 3L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().build());
        assertAccessDenied(
                () -> classUnderTest.markSectionAsInComplete(sectionId, applicationId, markedAsCompleteById),
                () -> verify(sectionPermissionRules).onlyMemberOfProjectTeamCanMarkSectionAsInComplete(isA(ApplicationResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testPermissionForMarkSectionAsNotRequired() {
        Long sectionId = 1L;
        Long applicationId = 2L;
        Long markedAsCompleteById = 3L;
        when(applicationLookupStrategy.getApplicationResource(applicationId)).thenReturn(newApplicationResource().build());
        assertAccessDenied(
                () -> classUnderTest.markSectionAsNotRequired(sectionId, applicationId, markedAsCompleteById),
                () -> verify(sectionPermissionRules).onlyMemberOfProjectTeamCanMarkSectionAsNotRequired(isA(ApplicationResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void testPermissionForGetByCompetitionIdVisibleForAssessment() {
        classUnderTest.getByCompetitionIdVisibleForAssessment(1L);
        verify(sectionPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).userCanReadSection(isA(SectionResource.class), isA(UserResource.class));
    }

    @Override
    protected Class<? extends SectionService> getClassUnderTest() {
        return TestSectionService.class;
    }

    public static class TestSectionService implements SectionService {
        static final int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

        @Override
        public ServiceResult<SectionResource> getById(Long sectionId) {
            return null;
        }

        @Override
        public ServiceResult<Map<Long, Set<Long>>> getCompletedSections(Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<Set<Long>> getCompletedSections(long applicationId, long organisationId) {
            return null;
        }

        @Override
        public ServiceResult<Set<Long>> getQuestionsForSectionAndSubsections(Long sectionId) {
            return null;
        }

        @Override
        public ServiceResult<List<ValidationMessages>> markSectionAsComplete(Long sectionId, Long applicationId, Long markedAsCompleteById) {
            return null;
        }

        @Override
        public ServiceResult<Void> markSectionAsNotRequired(Long sectionId, Long applicationId, Long markedAsCompleteById) {
            return null;
        }

        @Override
        public ServiceResult<Void> markSectionAsInComplete(Long sectionId, Long applicationId, Long markedAsInCompleteById) {
            return null;
        }

        @Override
        public ServiceResult<List<Long>> getIncompleteSections(Long applicationId) {
            return null;
        }

        @Override
        public ServiceResult<List<SectionResource>> getSectionsByCompetitionIdAndType(Long competitionId, SectionType type) {
            return null;
        }

        @Override
        public ServiceResult<Boolean> childSectionsAreCompleteForAllOrganisations(Section parentSection, Long applicationId, Section excludedSection) {
            return null;
        }

        @Override
        public ServiceResult<SectionResource> getNextSection(Long sectionId) {
            return null;
        }

        @Override
        public ServiceResult<SectionResource> getNextSection(SectionResource section) {
            return null;
        }

        @Override
        public ServiceResult<SectionResource> getPreviousSection(Long sectionId) {
            return null;
        }

        @Override
        public ServiceResult<SectionResource> getPreviousSection(SectionResource section) {
            return null;
        }

        @Override
        public ServiceResult<SectionResource> getSectionByQuestionId(Long questionId) {
            return null;
        }

        @Override
        public ServiceResult<List<SectionResource>> getByCompetitionId(Long CompetitionId) {
            return null;
        }

        @Override
        public ServiceResult<List<SectionResource>> getByCompetitionIdVisibleForAssessment(Long competitionId) {
            return serviceSuccess(newSectionResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }
    }
}
