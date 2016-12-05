package com.worth.ifs.competitionsetup.service.formpopulator;

import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;
import com.worth.ifs.competitionsetup.form.InitialDetailsForm;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.worth.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InitialDetailsFormPopulatorTest {

	private InitialDetailsFormPopulator service;
	
	@Before
	public void setUp() {
		service = new InitialDetailsFormPopulator();
	}
	
	@Test
	public void testSectionToFill() {
		CompetitionSetupSection result = service.sectionToFill();
		assertEquals(CompetitionSetupSection.INITIAL_DETAILS, result);
	}
				
	@Test
	public void testGetSectionFormDataInitialDetails() {
		CompetitionResource competition = newCompetitionResource()
				.withCompetitionType(4L)
				.withExecutive(5L)
				.withInnovationAreas(singleton(6L))  //TODO: INFUND-6479
				.withLeadTechnologist(7L)
				.withStartDate(LocalDateTime.of(2000, 1, 2, 3, 4))
				.withCompetitionCode("code")
				.withPafCode("paf")
				.withName("name")
				.withBudgetCode("budgetcode")
				.withId(8L).build();

		CompetitionSetupForm result = service.populateForm(competition);
		
		assertTrue(result instanceof InitialDetailsForm);
		InitialDetailsForm form = (InitialDetailsForm) result;
		assertEquals(Long.valueOf(4L), form.getCompetitionTypeId());
		assertEquals(Long.valueOf(5L), form.getExecutiveUserId());
		assertEquals(Long.valueOf(6L), form.getInnovationAreaCategoryId());
		assertEquals(Long.valueOf(7L), form.getLeadTechnologistUserId());
		assertEquals(Integer.valueOf(2), form.getOpeningDateDay());
		assertEquals(Integer.valueOf(1), form.getOpeningDateMonth());
		assertEquals(Integer.valueOf(2000), form.getOpeningDateYear());
		assertEquals("name", form.getTitle());
	}
}
