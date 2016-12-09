package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.google.common.collect.Lists;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.resource.CompetitionFunderResource;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competitionsetup.form.AdditionalInfoForm;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import org.hamcrest.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalInfoSectionSaverTest {

	@InjectMocks
	private AdditionalInfoSectionSaver service;
	
	@Mock
	private CompetitionService competitionService;
	
	@Test
	public void testSaveCompetitionSetupSection() {
		AdditionalInfoForm competitionSetupForm = new AdditionalInfoForm("Activity", "Innovate", "BudgetCode", Collections.emptyList());

		CompetitionResource competition = newCompetitionResource()
				.withId(1L).build();

		service.saveSection(competition, competitionSetupForm);

		assertEquals("Activity", competition.getActivityCode());
		assertEquals("Innovate", competition.getInnovateBudget());
		assertEquals("BudgetCode", competition.getBudgetCode());

		verify(competitionService).update(competition);
	}

	@Test
	public void testAutoSaveFunders() {
		CompetitionResource competition = newCompetitionResource().build();
		int expectedFunders = competition.getFunders().size() + 3;
		int lastIndex = expectedFunders - 1;
		String validBudget = "199122.02";
		AdditionalInfoForm form = new AdditionalInfoForm();
        when(competitionService.update(competition)).thenReturn(serviceSuccess());

		//Test that auto save will fill in the blank funders.
		ServiceResult<Void> result = service.autoSaveSectionField(competition, form,
				"funder["+ lastIndex +"].funderBudget", validBudget, Optional.empty());

		assertThat(competition.getFunders().size(), CoreMatchers.equalTo(expectedFunders));
		assertThat(competition.getFunders().get(lastIndex).getFunderBudget(), CoreMatchers.equalTo(new BigDecimal(validBudget)));
		assertTrue(result.isSuccess());

	}


	@Test
	public void testAutoSaveRemoveFunders() {
		CompetitionResource competition = newCompetitionResource().withFunders(Lists.newArrayList(
				new CompetitionFunderResource(),
				new CompetitionFunderResource(),
				new CompetitionFunderResource()
		)).build();
		AdditionalInfoForm form = new AdditionalInfoForm();

		when(competitionService.update(competition)).thenReturn(serviceSuccess());

		assertThat(competition.getFunders().size(), CoreMatchers.equalTo(3));

		//Test that out of range request to remove funders will leave the competition unchanged.
		ServiceResult<Void> result = service.autoSaveSectionField(competition, form,
				"removeFunder", "4", Optional.empty());

		assertThat(competition.getFunders().size(), CoreMatchers.equalTo(3));
		assertTrue(result.isSuccess());

		//Test that a valid index can be removed.
		result = service.autoSaveSectionField(competition, form,
				"removeFunder", "2", Optional.empty());

		assertThat(competition.getFunders().size(), CoreMatchers.equalTo(2));
		assertTrue(result.isSuccess());

		//Test trying to remove 0th funder will fail with error.
		result = service.autoSaveSectionField(competition, form,
				"removeFunder", "0", Optional.empty());

		assertThat(competition.getFunders().size(), CoreMatchers.equalTo(2));
		assertFalse(result.isSuccess());
	}

	@Test
	public void testsSupportsForm() {
		assertTrue(service.supportsForm(AdditionalInfoForm.class));
		assertFalse(service.supportsForm(CompetitionSetupForm.class));
	}

}
