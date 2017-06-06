package org.innovateuk.ifs.application.populator;

import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.application.areas.populator.ApplicationResearchCategoryPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.areas.viewmodel.ResearchCategoryViewModel;
import org.innovateuk.ifs.category.resource.ResearchCategoryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.category.builder.ResearchCategoryResourceBuilder.newResearchCategoryResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ApplicationResearchCategoryPopulatorTest extends BaseUnitTest {

    @InjectMocks
    private ApplicationResearchCategoryPopulator populator;

    @Test
    public void populateWithApplicationFinances() throws Exception {

        Long questionId = 1L;
        Long applicationId = 2L;
        String competitionName = "COMP_NAME";

        List<ApplicationFinanceResource> applicationFinanceResource = newApplicationFinanceResource().withApplication(applicationId).withOrganisationSize(1L).build(3);
        List<ResearchCategoryResource> researchCategories = newResearchCategoryResource().withId(1L, 2L, 3L).build(3);
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId)
                .withCompetitionName(competitionName).withResearchCategory(researchCategories.get(0)).build();

        when(categoryRestServiceMock.getResearchCategories()).thenReturn(restSuccess(researchCategories));
        when(financeService.getApplicationFinanceDetails(applicationId)).thenReturn(applicationFinanceResource);

        ResearchCategoryViewModel researchCategoryViewModel = populator.populate(applicationResource, questionId);

        assertEquals(questionId, researchCategoryViewModel.getQuestionId());
        assertEquals(applicationId, researchCategoryViewModel.getApplicationId());
        assertEquals(researchCategoryViewModel.getCurrentCompetitionName(), competitionName);
        assertEquals(researchCategoryViewModel.getAvailableResearchCategories().size(), 3L);
        assertEquals(researchCategoryViewModel.getHasApplicationFinances(), true);
    }

    @Test
    public void populateWithoutApplicationFinancesAndResearchCategorySelected() throws Exception {

        Long questionId = 1L;
        Long applicationId = 2L;
        String competitionName = "COMP_NAME";
        List<ApplicationFinanceResource> applicationFinanceResource = newApplicationFinanceResource().withApplication(applicationId).build(3);

        List<ResearchCategoryResource> researchCategories = newResearchCategoryResource().withId(1L, 2L, 3L).build(3);
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId)
                .withCompetitionName(competitionName).withResearchCategory(researchCategories.get(0)).build();

        when(categoryRestServiceMock.getResearchCategories()).thenReturn(restSuccess(researchCategories));
        when(financeService.getApplicationFinanceDetails(applicationId)).thenReturn(applicationFinanceResource);

        ResearchCategoryViewModel researchCategoryViewModel = populator.populate(applicationResource, questionId);

        assertEquals(questionId, researchCategoryViewModel.getQuestionId());
        assertEquals(applicationId, researchCategoryViewModel.getApplicationId());
        assertEquals(researchCategoryViewModel.getCurrentCompetitionName(), competitionName);
        assertEquals(researchCategoryViewModel.getAvailableResearchCategories().size(), 3L);
        assertEquals(researchCategoryViewModel.getHasApplicationFinances(), false);
    }

    @Test
    public void populateWithoutApplicationFinancesAndNoResearchCategorySelected() throws Exception {

        Long questionId = 1L;
        Long applicationId = 2L;
        String competitionName = "COMP_NAME";

        List<ApplicationFinanceResource> applicationFinanceResource = newApplicationFinanceResource().withApplication(applicationId).build(3);

        List<ResearchCategoryResource> researchCategories = newResearchCategoryResource().withId(1L, 2L, 3L).build(3);
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId)
                .withCompetitionName(competitionName).withResearchCategory(researchCategories.get(0)).build();

        when(categoryRestServiceMock.getResearchCategories()).thenReturn(restSuccess(newResearchCategoryResource().build(3)));
        when(financeService.getApplicationFinanceDetails(applicationId)).thenReturn(applicationFinanceResource);

        ResearchCategoryViewModel researchCategoryViewModel = populator.populate(applicationResource, questionId);

        assertEquals(questionId, researchCategoryViewModel.getQuestionId());
        assertEquals(applicationId, researchCategoryViewModel.getApplicationId());
        assertEquals(researchCategoryViewModel.getCurrentCompetitionName(), competitionName);
        assertEquals(researchCategoryViewModel.getAvailableResearchCategories().size(), 3L);
        assertEquals(researchCategoryViewModel.getHasApplicationFinances(), false);
    }
}