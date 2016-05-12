package com.worth.ifs.application.transactional;

import static com.worth.ifs.user.builder.RoleResourceBuilder.newRoleResource;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.resource.UserRoleType.COMP_ADMIN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.resource.UserRoleType;
import com.worth.ifs.user.resource.RoleResource;

public class ApplicationFundingServiceSecurityTest extends BaseServiceSecurityTest<ApplicationFundingService> {

	@Test
	public void testFundingDecisionAllowedIfGlobalCompAdminRole() {

		RoleResource compAdminRole = newRoleResource().withType(COMP_ADMIN).build();
		setLoggedInUser(newUserResource().withRolesGlobal(singletonList(compAdminRole)).build());
		service.makeFundingDecision(123L, new HashMap<Long, FundingDecision>());
	}

	@Test
	public void testFundingDecisionDeniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.makeFundingDecision(123L, new HashMap<Long, FundingDecision>());
			fail("Should not have been able to make funding decision without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testFundingDecisionDeniedIfNoGlobalRolesAtAll() {

		try {
			service.makeFundingDecision(123L, new HashMap<Long, FundingDecision>());
			fail("Should not have been able to make funding decision without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testFundingDecisionDeniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(
					newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
			try {
				service.makeFundingDecision(123L, new HashMap<Long, FundingDecision>());
				fail("Should not have been able to make funding decision without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}
	
	@Test
	public void testSaveFundingDecisionDataAllowedIfGlobalCompAdminRole() {

		RoleResource compAdminRole = newRoleResource().withType(COMP_ADMIN).build();
		setLoggedInUser(newUserResource().withRolesGlobal(singletonList(compAdminRole)).build());
		service.saveFundingDecisionData(123L, new HashMap<Long, FundingDecision>());
	}

	@Test
	public void testSaveFundingDecisionDataDeniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.saveFundingDecisionData(123L, new HashMap<Long, FundingDecision>());
			fail("Should not have been able to save funding decision data without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testSaveFundingDecisionDataDeniedIfNoGlobalRolesAtAll() {

		try {
			service.saveFundingDecisionData(123L, new HashMap<Long, FundingDecision>());
			fail("Should not have been able to save funding decision data without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testSaveFundingDecisionDataDeniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(
					newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
			try {
				service.saveFundingDecisionData(123L, new HashMap<Long, FundingDecision>());
				fail("Should not have been able to save funding decision data without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}
	
	@Test
	public void testGetFundingDecisionDataAllowedIfGlobalCompAdminRole() {

		RoleResource compAdminRole = newRoleResource().withType(COMP_ADMIN).build();
		setLoggedInUser(newUserResource().withRolesGlobal(singletonList(compAdminRole)).build());
		service.getFundingDecisionData(123L);
	}

	@Test
	public void testGetFundingDecisionDataDeniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.getFundingDecisionData(123L);
			fail("Should not have been able to get funding decision data without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testGetFundingDecisionDataDeniedIfNoGlobalRolesAtAll() {

		try {
			service.getFundingDecisionData(123L);
			fail("Should not have been able to get funding decision data without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void testGetFundingDecisionDataDeniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(
					newUserResource().withRolesGlobal(singletonList(newRoleResource().withType(role).build())).build());
			try {
				service.getFundingDecisionData(123L);
				fail("Should not have been able to get funding decision data without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}

	@Override
	protected Class<? extends ApplicationFundingService> getServiceClass() {
		return TestApplicationFundingService.class;
	}

	public static class TestApplicationFundingService implements ApplicationFundingService {

		@Override
		public ServiceResult<Void> makeFundingDecision(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {
			return null;
		}

		@Override
		public ServiceResult<Void> notifyLeadApplicantsOfFundingDecisions(Long competitionId, Map<Long, FundingDecision> applicationFundingDecisions) {
			return null;
		}

		@Override
		public ServiceResult<Void> saveFundingDecisionData(Long competitionId, Map<Long, FundingDecision> decision) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceResult<Map<Long, FundingDecision>> getFundingDecisionData(Long competitionId) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
