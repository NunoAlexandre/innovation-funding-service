package com.worth.ifs.assessment.security;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.assessment.resource.AssessmentResource;
import com.worth.ifs.assessment.transactional.AssessmentService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.workflow.resource.ProcessOutcomeResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.util.List;

import static com.worth.ifs.BaseBuilderAmendFunctions.id;
import static com.worth.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder.newProcessOutcomeResource;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;


public class AssessmentServiceSecurityTest extends BaseServiceSecurityTest<AssessmentService> {

    private AssessmentPermissionRules assessmentPermissionRules;
    private AssessmentLookupStrategy assessmentLookupStrategy;

    @Override
    protected Class<? extends AssessmentService> getServiceClass() {
        return TestAssessmentService.class;
    }

    @Before
    public void setUp() throws Exception {
        assessmentPermissionRules = getMockPermissionRulesBean(AssessmentPermissionRules.class);
        assessmentLookupStrategy = getMockPermissionEntityLookupStrategiesBean(AssessmentLookupStrategy.class);
    }

    private static Long ID_TO_FIND = 1L;
    private static int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

    @Test
    public void findById() {
        AssessmentResource assessmentResource = newAssessmentResource().with(id(ID_TO_FIND)).build();

        assertAccessDenied(
                () -> service.findById(ID_TO_FIND),
                () -> verify(assessmentPermissionRules).userCanReadAssessment(eq(assessmentResource), isA(UserResource.class))
        );
    }


    @Test
    public void findByUserId() {
        long userId = 3L;

        service.findByUserId(userId);
        verify(assessmentPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).userCanReadAssessment(isA(AssessmentResource.class), isA(UserResource.class));
    }

    @Test
    public void recommend() {
        final Long assessmentId = 1L;
        ProcessOutcomeResource outcome = newProcessOutcomeResource().build();
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> service.recommend(assessmentId, outcome),
                () -> verify(assessmentPermissionRules).userCanUpdateAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void rejectInvitation() {
        final Long assessmentId = 1L;
        ProcessOutcomeResource outcome = newProcessOutcomeResource().build();
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> service.rejectInvitation(assessmentId, outcome),
                () -> verify(assessmentPermissionRules).userCanUpdateAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    public static class TestAssessmentService implements AssessmentService {
        @Override
        public ServiceResult<AssessmentResource> findById(Long id) {
            return serviceSuccess(newAssessmentResource().with(id(ID_TO_FIND)).build());
        }

        @Override
        public ServiceResult<List<AssessmentResource>> findByUserId(Long userId) {
            return serviceSuccess(newAssessmentResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<Void> recommend(@P("assessmentId") Long assessmentId, ProcessOutcomeResource processOutcome) {
            return null;
        }

        @Override
        public ServiceResult<Void> rejectInvitation(@P("assessmentId") Long assessmentId, ProcessOutcomeResource processOutcome) {
            return null;
        }
    }
}