package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.commons.service.ServiceResult;

import java.util.List;
import java.util.Map;

/**
 * Service for making the decision of which applications will receive funding and which will not for a given competition.
 */
public interface ApplicationFundingDecisionService {

	/**
	 * make the decision of what applications will receive funding and which will not for a given competition.
	 * @param competitionId the id of the competition
	 * @param applicationIdToFundingDecision map of application ids to funding decisions
	 */
	ServiceResult<Void> makeApplicationFundingDecision(Long competitionId, Map<Long, FundingDecision> applicationIdToFundingDecision);
	
	/**
	 * verifies all submitted applications represented.
	 * @param parameterMap the submitted parameters
	 * @param applicationIds the ids of submitted applications for the competition
	 * @return boolean to indicate if all applications represented
	 */
	boolean verifyAllApplicationsRepresented(Map<String, String[]> parameterMap, List<Long> applicationIds);
	
	/**
	 * constructs map of application ids to funding decisions from the parameter map and the list of submitted application ids.
	 * @param parameterMap the submitted parameters
	 * @param applicationIds the ids of submitted applications for the competition
	 * @return map of application ids to funding decisions from the parameter map and the list of submitted application ids
	 */
	Map<Long, FundingDecision> applicationIdToFundingDecisionFromRequestParams(Map<String, String[]> parameterMap, List<Long> applicationIds);

	/**
	 * save the funding decision data
	 * @param competitionId the id of the competition
	 * @param applicationIdToFundingDecision map of application ids to funding decisions
	 */
	ServiceResult<Void> saveApplicationFundingDecisionData(Long competitionId, Map<Long, FundingDecision> applicationIdToFundingDecision);
	
	/**
	 * returns the appropriate FundingDecision enum value for a String used in the decision form.
	 * @param val either "Y", "N" or "-"
	 * @return the appropriate FundingDecision enum value
	 */
	FundingDecision fundingDecisionForString(String val);
}
