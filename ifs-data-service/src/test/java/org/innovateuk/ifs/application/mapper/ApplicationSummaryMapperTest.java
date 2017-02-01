package org.innovateuk.ifs.application.mapper;

import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatus;
import org.innovateuk.ifs.application.domain.FundingDecisionStatus;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.CompletedPercentageResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.application.transactional.ApplicationSummarisationService;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.repository.OrganisationRepository;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.clearUniqueIds;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryMapperTest {

	private static final long APPLICATION_ID = 123L;

	@InjectMocks
	private ApplicationSummaryMapperImpl mapper;
	
	@Mock
	private ApplicationService applicationService;
	
	@Mock
	private ApplicationSummarisationService applicationSummarisationService;
	
	@Mock
	private FundingDecisionMapper fundingDecisionMapper;

	@Mock
	protected OrganisationRepository organisationRepository;
	
	private Application source;
	
	@Before
	public void setUp() {
		clearUniqueIds();
		when(fundingDecisionMapper.mapToResource(FundingDecisionStatus.FUNDED)).thenReturn(FundingDecision.FUNDED);
		
		ApplicationStatus openStatus = new ApplicationStatus(ApplicationStatusConstants.OPEN.getId(), ApplicationStatusConstants.OPEN.getName());
		source = newApplication()
				.withId(APPLICATION_ID)
				.withName("appname")
				.withApplicationStatus(openStatus)
				.withDurationInMonths(7L)
				.withFundingDecision(FundingDecisionStatus.FUNDED)
				.build();
		
		Organisation org1 = newOrganisation().withId(1L).withName("leadorg").build();
		Organisation org2 = newOrganisation().withId(2L).withName("otherorg1").build();
		when(organisationRepository.findOne(1L)).thenReturn(org1);
		when(organisationRepository.findOne(2L)).thenReturn(org2);

		ProcessRole leadProcessRole = leadProcessRole(org1);
		source.addUserApplicationRole(leadProcessRole);
		
		ProcessRole collaboratorProcessRole1 = collaboratorProcessRole(org2);
		source.addUserApplicationRole(collaboratorProcessRole1);
		ProcessRole collaboratorProcessRole2 = collaboratorProcessRole(org2);
		source.addUserApplicationRole(collaboratorProcessRole2);
		ProcessRole collaboratorProcessRole3 = collaboratorProcessRole(org1);
		source.addUserApplicationRole(collaboratorProcessRole3);
		
		CompletedPercentageResource resource = new CompletedPercentageResource();
		resource.setCompletedPercentage(new BigDecimal("66.6"));
		when(applicationService.getProgressPercentageByApplicationId(APPLICATION_ID)).thenReturn(serviceSuccess(resource));
		
		when(applicationSummarisationService.getFundingSought(source)).thenReturn(serviceSuccess(new BigDecimal("1.23")));
		when(applicationSummarisationService.getTotalProjectCost(source)).thenReturn(serviceSuccess(new BigDecimal("9.87")));
	}
	
	@Test
	public void testMap() {
		
		ApplicationSummaryResource result = mapper.mapToResource(source);
		
		assertEquals(APPLICATION_ID, result.getId());
		assertEquals("appname", result.getName());
		assertEquals("In Progress", result.getStatus());
		assertEquals(66, result.getCompletedPercentage());
		assertEquals("leadorg", result.getLead());
		assertEquals("User 4", result.getLeadApplicant());
		assertEquals(2, result.getNumberOfPartners());
		assertEquals(new BigDecimal("1.23"), result.getGrantRequested());
		assertEquals(new BigDecimal("9.87"), result.getTotalProjectCost());
		assertEquals(7L, result.getDuration());
		assertTrue(result.isFunded());
		assertEquals(FundingDecision.FUNDED, result.getFundingDecision());
		
		verify(fundingDecisionMapper).mapToResource(FundingDecisionStatus.FUNDED);
	}
	
	@Test
	public void testMapFundedBecauseOfStatus() {
		ApplicationStatus approvedStatus = new ApplicationStatus(ApplicationStatusConstants.APPROVED.getId(), ApplicationStatusConstants.APPROVED.getName());
		source.setApplicationStatus(approvedStatus);
		source.setFundingDecision(null);
		
		ApplicationSummaryResource result = mapper.mapToResource(source);
		
		assertTrue(result.isFunded());
		assertEquals(FundingDecision.FUNDED, result.getFundingDecision());
	}
	
	@Test
	public void testMapFundedBecauseOfFundingDecision() {
		ApplicationStatus openStatus = new ApplicationStatus(ApplicationStatusConstants.OPEN.getId(), ApplicationStatusConstants.OPEN.getName());
		source.setApplicationStatus(openStatus);
		source.setFundingDecision(FundingDecisionStatus.FUNDED);
		
		ApplicationSummaryResource result = mapper.mapToResource(source);
		
		assertTrue(result.isFunded());
		assertEquals(FundingDecision.FUNDED, result.getFundingDecision());
	}

	private ProcessRole leadProcessRole(Organisation organisation) {
		ProcessRole leadProcessRole = new ProcessRole();
		
		leadProcessRole.setOrganisationId(organisation.getId());
		Role role = new Role();
		role.setName(UserRoleType.LEADAPPLICANT.getName());
        leadProcessRole.setUser(newUser().build());
		leadProcessRole.setRole(role);
		return leadProcessRole;
	}
	
	private ProcessRole collaboratorProcessRole(Organisation organisation) {
		ProcessRole collaboratorProcessRole = new ProcessRole();
		collaboratorProcessRole.setOrganisationId(organisation.getId());
		Role role = new Role();
		role.setName(UserRoleType.COLLABORATOR.getName());
				
		collaboratorProcessRole.setRole(role);
		return collaboratorProcessRole;
	}
}
