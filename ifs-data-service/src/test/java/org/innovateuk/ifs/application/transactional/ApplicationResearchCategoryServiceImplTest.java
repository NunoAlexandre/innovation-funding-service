package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.category.domain.ResearchCategory;
import org.innovateuk.ifs.category.repository.ResearchCategoryRepository;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.junit.Test;
import org.mockito.Mock;

import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.category.builder.ResearchCategoryBuilder.newResearchCategory;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link  ApplicationResearchCategoryServiceImpl}
 */

public class ApplicationResearchCategoryServiceImplTest extends BaseServiceUnitTest<ApplicationResearchCategoryService> {

    @Mock
    private ResearchCategoryRepository researchCategoryRepository;

    @Override
    protected ApplicationResearchCategoryService supplyServiceUnderTest() {
        return new ApplicationResearchCategoryServiceImpl();
    }

    @Test
    public void setApplicationResearchCategory() throws Exception {
        Long applicationId = 1L;
        Long researchCategoryId = 1L;

        ResearchCategory researchCategory = newResearchCategory().withId(researchCategoryId).build();

        Competition competition = newCompetition().build();
        Application application = newApplication().withId(applicationId).withCompetition(competition).build();

        Application expectedApplication = newApplication().withId(applicationId).withResearchCategory(researchCategory).build();
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(application);
        when(applicationRepositoryMock.save(expectedApplication)).thenReturn(expectedApplication);
        when(researchCategoryRepository.findById(researchCategoryId)).thenReturn(researchCategory);

        ServiceResult<ApplicationResource> result = service.setResearchCategory(applicationId, researchCategoryId);

        assertTrue(result.isSuccess());

        verify(applicationRepositoryMock, times(1)).save(any(Application.class));
    }

    @Test
    public void setApplicationResearchCategory_nonExistingApplicationShouldResultInError() throws Exception {
        Long applicationId = 1L;
        Long researchCategoryId = 2L;

        ResearchCategory researchCategory = newResearchCategory().withId(researchCategoryId).build();

        Application expectedApplication = newApplication().withId(applicationId).withResearchCategory(researchCategory).build();
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(null);

        ServiceResult<ApplicationResource> result = service.setResearchCategory(applicationId, researchCategoryId);

        assertTrue(result.isFailure());

        verify(applicationRepositoryMock, times(0)).save(any(Application.class));
        assertEquals(CommonFailureKeys.GENERAL_NOT_FOUND.getErrorKey(), result.getFailure().getErrors().get(0).getErrorKey());
    }

    @Test
    public void setApplicationResearchCategory_nonExistingResearchCategoryShouldResultInError() throws Exception {
        Long applicationId = 1L;
        Long researchCategoryId = 2L;

        ResearchCategory researchCategory = newResearchCategory().withId(researchCategoryId).build();
        Application application = newApplication().withId(applicationId).build();

        Application expectedApplication = newApplication().withId(applicationId).withResearchCategory(researchCategory).build();
        when(researchCategoryRepository.findOne(researchCategoryId)).thenReturn(null);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(application);
        when(applicationRepositoryMock.save(expectedApplication)).thenReturn(expectedApplication);

        ServiceResult<ApplicationResource> result = service.setResearchCategory(applicationId, researchCategoryId);

        assertTrue(result.isFailure());
        verify(applicationRepositoryMock, times(0)).save(any(Application.class));
        assertEquals(CommonFailureKeys.GENERAL_NOT_FOUND.getErrorKey(), result.getFailure().getErrors().get(0).getErrorKey());
    }
}
