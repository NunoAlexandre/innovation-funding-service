package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.commons.error.CommonErrors;
import org.innovateuk.ifs.commons.error.exception.GeneralUnexpectedErrorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFundingDecisionServiceImplTest {
	@InjectMocks
	private ApplicationFundingDecisionServiceImpl service;
	
	@Mock
	private ApplicationFundingDecisionRestService applicationFundingDecisionRestService;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMakeFundingDecision() {
		Long competitionId = 123L;
		Map<Long, FundingDecision> applicationIdToFundingDecision = mock(Map.class);
		
		when(applicationFundingDecisionRestService.makeApplicationFundingDecision(competitionId, applicationIdToFundingDecision)).thenReturn(restSuccess());

		service.makeApplicationFundingDecision(competitionId, applicationIdToFundingDecision);
		
		verify(applicationFundingDecisionRestService).makeApplicationFundingDecision(competitionId,  applicationIdToFundingDecision);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = GeneralUnexpectedErrorException.class)
	public void testErrorMakingFundingDecision() {
		Long competitionId = 123L;
		Map<Long, FundingDecision> applicationIdToFundingDecision = mock(Map.class);
		
		when(applicationFundingDecisionRestService.makeApplicationFundingDecision(competitionId, applicationIdToFundingDecision)).thenReturn(restFailure(CommonErrors.internalServerErrorError()));
		service.makeApplicationFundingDecision(competitionId, applicationIdToFundingDecision);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSaveFundingDecisionData() {
		Long competitionId = 123L;
		Map<Long, FundingDecision> applicationIdToFundingDecision = mock(Map.class);
		
		when(applicationFundingDecisionRestService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision)).thenReturn(restSuccess());

		service.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision);
		
		verify(applicationFundingDecisionRestService).saveApplicationFundingDecisionData(competitionId,  applicationIdToFundingDecision);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = GeneralUnexpectedErrorException.class)
	public void testErrorSavingFundingDecisionData() {
		Long competitionId = 123L;
		Map<Long, FundingDecision> applicationIdToFundingDecision = mock(Map.class);
		
		when(applicationFundingDecisionRestService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision)).thenReturn(restFailure(CommonErrors.internalServerErrorError()));
		service.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision).getSuccessObjectOrThrowException();
	}
	
	@Test
	public void testVerifyAllApplicationsRepresented() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"), "2", val("N"));
		List<Long> applicationIds = asList(1L, 2L);
		
		boolean result = service.verifyAllApplicationsRepresented(parameterMap, applicationIds);

		assertTrue(result);
	}
	
	@Test
	public void testVerifyNotAllApplicationsRepresented() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"));
		List<Long> applicationIds = asList(1L, 2L);
		
		boolean result = service.verifyAllApplicationsRepresented(parameterMap, applicationIds);

		assertFalse(result);
	}
	
	@Test
	public void testVerifyAllApplicationsDecided() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"), "2", val("-"));
		List<Long> applicationIds = asList(1L, 2L);
		
		boolean result = service.verifyAllApplicationsRepresented(parameterMap, applicationIds);

		assertFalse(result);
	}
	
	@Test
	public void testIgnoreNonNumericParamNamesForVerifyNotAllApplicationsRepresented() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"), "somethingelse", val("Y"));
		List<Long> applicationIds = asList(1L, 2L);
		
		boolean result = service.verifyAllApplicationsRepresented(parameterMap, applicationIds);

		assertFalse(result);
	}
	
	@Test
	public void testIgnoreIrrelevantParamValuesForVerifyNotAllApplicationsRepresented() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"), "2", val("Q"));
		List<Long> applicationIds = asList(1L, 2L);
		
		boolean result = service.verifyAllApplicationsRepresented(parameterMap, applicationIds);

		assertFalse(result);
	}
	
	@Test
	public void testApplicationIdToFundingDecisionFromRequestParams() {
		Map<String, String[]> parameterMap = asMap("1", val("Y"), "2", val("N"), "3", val("-"), "4", val("Y"));
		List<Long> applicationIds = asList(1L, 2L, 3L);
		
		Map<Long, FundingDecision> result = service.applicationIdToFundingDecisionFromRequestParams(parameterMap, applicationIds);

		assertEquals(3, result.size());
		assertEquals(FundingDecision.FUNDED, result.get(1L));
		assertEquals(FundingDecision.UNFUNDED, result.get(2L));
		assertEquals(FundingDecision.UNDECIDED, result.get(3L));
	}
	
	@Test
	public void testFundingDecisionForStringFunded() {
		FundingDecision result = service.fundingDecisionForString("Y");
		assertEquals(FundingDecision.FUNDED, result);
	}
	
	@Test
	public void testFundingDecisionForStringUnfunded() {
		FundingDecision result = service.fundingDecisionForString("N");
		assertEquals(FundingDecision.UNFUNDED, result);
	}
	
	@Test
	public void testFundingDecisionForStringUndecided() {
		FundingDecision result = service.fundingDecisionForString("-");
		assertEquals(FundingDecision.UNDECIDED, result);
	}
	
	private String[] val(String val) {
		return new String[]{val};
	}
}
