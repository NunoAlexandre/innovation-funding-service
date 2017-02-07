package org.innovateuk.ifs.file.service;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.transactional.FileEntryService;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.security.ApplicationFinanceLookupStrategy;
import org.innovateuk.ifs.finance.security.ApplicationFinancePermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing how the secured methods in FileEntryService interact with Spring Security
 */
public class FileEntryServiceSecurityTest extends BaseServiceSecurityTest<FileEntryService> {

    private ApplicationFinancePermissionRules applicationFinanceRules;
    private ApplicationFinanceLookupStrategy applicationFinanceLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        applicationFinanceRules = getMockPermissionRulesBean(ApplicationFinancePermissionRules.class);
        applicationFinanceLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ApplicationFinanceLookupStrategy.class);
    }


    @Test
    public void testGetFileEntryByApplicationFinanceId() {
        final Long applicationFinanceId = 1L;
        when(applicationFinanceLookupStrategy.getApplicationFinance(applicationFinanceId)).thenReturn(newApplicationFinanceResource().build());
        assertAccessDenied(
                () -> classUnderTest.getFileEntryByApplicationFinanceId(applicationFinanceId),
                () -> {
                    verify(applicationFinanceRules).consortiumMemberCanGetFileEntryResourceByFinanceIdOfACollaborator(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                    verify(applicationFinanceRules).compAdminCanGetFileEntryResourceForFinanceIdOfACollaborator(isA(ApplicationFinanceResource.class), isA(UserResource.class));
                });
    }

    @Override
    protected Class<? extends FileEntryService> getClassUnderTest() {
        return TestFileEntryService.class;
    }

    public static class TestFileEntryService implements FileEntryService {

        @Override
        public ServiceResult<FileEntryResource> findOne(Long id) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> getFileEntryByApplicationFinanceId(@P("applicationFinanceResourceId") Long applicationFinanceId) {
            return null;
        }

        @Override
        public ServiceResult<FileEntryResource> saveFile(@P("file") FileEntryResource file) {
            return ServiceResult.serviceSuccess(file);
        }


    }
}

