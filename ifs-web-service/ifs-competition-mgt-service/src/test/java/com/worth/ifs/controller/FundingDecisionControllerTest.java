package com.worth.ifs.controller;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.resource.ApplicationSummaryPageResource;
import com.worth.ifs.application.resource.ApplicationSummaryResource;
import com.worth.ifs.application.resource.FundingDecision;
import com.worth.ifs.application.service.ApplicationFundingDecisionService;
import com.worth.ifs.competition.controller.FundingDecisionController;
import com.worth.ifs.filter.CookieFlashMessageFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FundingDecisionControllerTest extends BaseControllerMockMVCTest<FundingDecisionController> {

	@Override
	protected FundingDecisionController supplyControllerUnderTest() {
		return new FundingDecisionController();
	}
	
    @Mock
    private ApplicationFundingDecisionService applicationFundingDecisionService;
    
    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;
    
    @Before
    public void setup() {
        
        ApplicationSummaryPageResource applicationSummaries = new ApplicationSummaryPageResource();
        ApplicationSummaryResource app8 = app(8L);
        ApplicationSummaryResource app9 = app(9L);
        ApplicationSummaryResource app10 = app(10L);
        applicationSummaries.setContent(asList(app8, app9, app10));
        when(applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(123L, null, 0, Integer.MAX_VALUE)).thenReturn(applicationSummaries);
    }
    
    private ApplicationSummaryResource app(Long id) {
		ApplicationSummaryResource app = new ApplicationSummaryResource();
		app.setId(id);
		return app;
	}

	@SuppressWarnings("unchecked")
	@Test
    public void submitFundingDecisionFailVerification() throws Exception {
    	
		when(applicationFundingDecisionService.verifyAllApplicationsRepresented(isA(Map.class), isA(List.class))).thenReturn(false);
		
    	mockMvc.perform(
    				post("/competition/123/fundingdecisionsubmit")
    				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    				.param("8", "Y")
    				.param("9", "N")
    			)
                .andExpect(redirectedUrl("/competition/123/applications"));
    	
    	verify(applicationFundingDecisionService, never()).makeApplicationFundingDecision(any(Long.class), any(Map.class));
    	verify(cookieFlashMessageFilter).setFlashMessage(isA(HttpServletResponse.class), eq("fundingNotDecidedForAllApplications"));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void submitFundingDecision() throws Exception {
    	
		when(applicationFundingDecisionService.verifyAllApplicationsRepresented(isA(Map.class), isA(List.class))).thenReturn(true);

		Map<Long, FundingDecision> fundingDecisons = mock(Map.class);
		when(applicationFundingDecisionService.applicationIdToFundingDecisionFromRequestParams(isA(Map.class), isA(List.class))).thenReturn(fundingDecisons);
    	
		mockMvc.perform(
    				post("/competition/123/fundingdecisionsubmit")
    				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    				.param("8", "Y")
    				.param("9", "N")
    				.param("10", "Y")
    			)
                .andExpect(redirectedUrl("/competition/123/applications"));
    	
    	verifyNoMoreInteractions(cookieFlashMessageFilter);
    	
    	verify(applicationFundingDecisionService).makeApplicationFundingDecision(123L, fundingDecisons);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void fundingDecisionFailVerification() throws Exception {
    	
		when(applicationFundingDecisionService.verifyAllApplicationsRepresented(isA(Map.class), isA(List.class))).thenReturn(false);
		
    	mockMvc.perform(
    				post("/competition/123/fundingdecision")
    				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    				.param("8", "Y")
    				.param("9", "N")
    				.param("action", "notify")
    			)
                .andExpect(redirectedUrl("/competition/123/applications"));
    	
    	verify(applicationFundingDecisionService, never()).makeApplicationFundingDecision(any(Long.class), any(Map.class));
    	verify(cookieFlashMessageFilter).setFlashMessage(isA(HttpServletResponse.class), eq("fundingNotDecidedForAllApplications"));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void fundingDecision() throws Exception {
    	
		when(applicationFundingDecisionService.verifyAllApplicationsRepresented(isA(Map.class), isA(List.class))).thenReturn(true);

		Map<Long, FundingDecision> fundingDecisons = mock(Map.class);
		when(applicationFundingDecisionService.applicationIdToFundingDecisionFromRequestParams(isA(Map.class), isA(List.class))).thenReturn(fundingDecisons);
    	
    	mockMvc.perform(
    				post("/competition/123/fundingdecision")
    				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    				.param("8", "Y")
    				.param("9", "N")
    				.param("10", "Y")
    				.param("action", "notify")
    			)
                .andExpect(view().name("funding-decision-confirmation"))
                .andExpect(model().attribute("competitionId", 123L))
                .andExpect(model().attribute("applicationFundingDecisions", fundingDecisons));

    	verifyNoMoreInteractions(cookieFlashMessageFilter);
    	verify(applicationFundingDecisionService, never()).makeApplicationFundingDecision(any(Long.class), any(Map.class));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void saveFundingDecisionData() throws Exception {
    	
		Map<Long, FundingDecision> fundingDecisons = mock(Map.class);
		when(applicationFundingDecisionService.applicationIdToFundingDecisionFromRequestParams(isA(Map.class), isA(List.class))).thenReturn(fundingDecisons);
    
    	mockMvc.perform(
    				post("/competition/123/fundingdecision")
    				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
    				.param("8", "Y")
    				.param("9", "N")
    				.param("action", "save")
    			)
                .andExpect(redirectedUrl("/competition/123/applications"));
    	
    	verify(applicationFundingDecisionService).saveApplicationFundingDecisionData(123L, fundingDecisons);
    }

}
