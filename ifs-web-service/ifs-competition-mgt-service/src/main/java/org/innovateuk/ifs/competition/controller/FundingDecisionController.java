package org.innovateuk.ifs.competition.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionService;
import org.innovateuk.ifs.application.service.ApplicationSummaryService;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;

/**
 * This controller gets the decision to fund or not fund the applications for a given competition.
 */
@Controller
@PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
public class FundingDecisionController {
	
	@Autowired
	private ApplicationSummaryService applicationSummaryService;
	
	@Autowired
	private ApplicationFundingDecisionService applicationFundingDecisionService;
	
	@Autowired
	private CookieFlashMessageFilter cookieFlashMessageFilter;
	
	@RequestMapping(method = RequestMethod.POST, value = "/competition/{competitionId}/fundingdecision")
    public String fundingDecisionCheck(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable("competitionId") Long competitionId, @RequestParam("action") String action){
		
		List<Long> applicationIds = submittedApplicationIdsForCompetition(competitionId);

		Map<Long, FundingDecision> applicationIdToFundingDecision = applicationFundingDecisionService.applicationIdToFundingDecisionFromRequestParams(request.getParameterMap(), applicationIds);

		applicationFundingDecisionService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision);
		
		if(!"notify".equals(action)) {
			return "redirect:/competition/" + competitionId + "/applications";
		}
		
		if(!applicationFundingDecisionService.verifyAllApplicationsRepresented(request.getParameterMap(), applicationIds)) {
			cookieFlashMessageFilter.setFlashMessage(response, "fundingNotDecidedForAllApplications");
			return "redirect:/competition/" + competitionId + "/applications";
		}
		
		model.addAttribute("competitionId", competitionId);
		model.addAttribute("applicationFundingDecisions", applicationIdToFundingDecision);
		
		return "funding-decision-confirmation";
    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/competition/{competitionId}/fundingdecisionsubmit")
    public String submitFundingDecision(HttpServletRequest request, HttpServletResponse response, @PathVariable("competitionId") Long competitionId){
		
		List<Long> applicationIds = submittedApplicationIdsForCompetition(competitionId);

		Map<Long, FundingDecision> applicationIdToFundingDecision = applicationFundingDecisionService.applicationIdToFundingDecisionFromRequestParams(request.getParameterMap(), applicationIds);

		applicationFundingDecisionService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision);
		
		if(!applicationFundingDecisionService.verifyAllApplicationsRepresented(request.getParameterMap(), applicationIds)) {
			cookieFlashMessageFilter.setFlashMessage(response, "fundingNotDecidedForAllApplications");
			return "redirect:/competition/" + competitionId + "/applications";
		}
		
		applicationFundingDecisionService.makeApplicationFundingDecision(competitionId, applicationIdToFundingDecision);

		return "redirect:/competition/" + competitionId + "/applications";
    }
	
	private List<Long> submittedApplicationIdsForCompetition(Long competitionId) {
		return applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(competitionId, null, 0, Integer.MAX_VALUE, null).getContent()
				.stream().map(e -> e.getId()).collect(Collectors.toList());
	}
	
}
