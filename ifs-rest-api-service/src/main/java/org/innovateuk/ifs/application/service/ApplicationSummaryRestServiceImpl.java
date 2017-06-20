package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.application.resource.*;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.competitionSummaryResourceListType;

@Service
public class ApplicationSummaryRestServiceImpl extends BaseRestService implements ApplicationSummaryRestService {

    private String applicationSummaryRestUrl = "/applicationSummary";

	private String applicationRestUrl = "/application";

	@Override
	public RestResult<ApplicationSummaryPageResource> getAllApplications(long competitionId, String sortField, int pageNumber, int pageSize, Optional<String> filter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId;
		return getApplicationSummaryPage(baseUrl, pageNumber, pageSize, sortField, filter);
	}

	@Override
	public RestResult<List<ApplicationSummaryResource>> getAllSubmittedApplications(long competitionId, Optional<String> filter, Optional<FundingDecision> fundingFilter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId + "/all-submitted";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		filter.ifPresent(f -> params.put("filter", singletonList(f)));
		fundingFilter.ifPresent(f -> params.put("fundingFilter", singletonList(f.toString())));

		String uri = UriComponentsBuilder.fromPath(baseUrl).queryParams(params).build().encode().toUriString();

		return getWithRestResult(uri, competitionSummaryResourceListType());
	}

	@Override
	public RestResult<ApplicationSummaryPageResource> getSubmittedApplications(long competitionId, String sortField, int pageNumber, int pageSize, Optional<String> filter, Optional<FundingDecision> fundingFilter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId + "/submitted";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		filter.ifPresent(f -> params.put("filter", singletonList(f)));
		fundingFilter.ifPresent(f -> params.put("fundingFilter", singletonList(f.toString())));

		String uriWithParams = buildPaginationUri(baseUrl, pageNumber, pageSize, sortField, params);
		return getWithRestResult(uriWithParams, ApplicationSummaryPageResource.class);
	}

	@Override
	public RestResult<ApplicationSummaryPageResource> getNonSubmittedApplications(long competitionId, String sortField, int pageNumber, int pageSize, Optional<String> filter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId + "/not-submitted";
		return getApplicationSummaryPage(baseUrl, pageNumber, pageSize, sortField, filter);
	}

	@Override
	public RestResult<ApplicationSummaryPageResource> getIneligibleApplications(long competitionId, String sortField, int pageNumber, int pageSize, Optional<String> filter, Optional<Boolean> informFilter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId + "/ineligible";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		filter.ifPresent(f -> params.put("filter", singletonList(f)));
		informFilter.ifPresent(f -> params.put("informFilter", singletonList(f.toString())));
		String uriWithParams = buildPaginationUri(baseUrl, pageNumber, pageSize, sortField, params);
		return getWithRestResult(uriWithParams, ApplicationSummaryPageResource.class);
	}

	@Override
	public RestResult<ApplicationSummaryPageResource> getWithFundingDecisionApplications(Long competitionId,
																						 String sortField,
																						 int pageNumber,
																						 int pageSize,
																						 Optional<String> filter,
																						 Optional<Boolean> sendFilter,
																						 Optional<FundingDecision> fundingFilter) {
		String baseUrl = applicationSummaryRestUrl + "/findByCompetition/" + competitionId + "/with-funding-decision";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		filter.ifPresent(f -> params.put("filter", singletonList(f)));
		sendFilter.ifPresent(f -> params.put("sendFilter", singletonList(f.toString())));
		fundingFilter.ifPresent(f -> params.put("fundingFilter", singletonList(f.toString())));
		String uriWithParams = buildPaginationUri(baseUrl, pageNumber, pageSize, sortField, params);
		return getWithRestResult(uriWithParams, ApplicationSummaryPageResource.class);
	}
	
	private RestResult<ApplicationSummaryPageResource> getApplicationSummaryPage(String url, int pageNumber, int pageSize, String sortField, Optional<String> filter) {

		String urlWithParams = buildUri(url, sortField, pageNumber, pageSize, filter);
		return getWithRestResult(urlWithParams, ApplicationSummaryPageResource.class);
	}

	@Override
	public RestResult<ByteArrayResource> downloadByCompetition(long competitionId) {
		String url = applicationRestUrl + "/download/downloadByCompetition/" + competitionId;
		return getWithRestResult(url, ByteArrayResource.class);
	}

	@Override
	public RestResult<CompetitionSummaryResource> getCompetitionSummary(long competitionId) {
		return getWithRestResult(applicationSummaryRestUrl + "/getCompetitionSummary/" + competitionId, CompetitionSummaryResource.class);
	}

	@Override
	public RestResult<ApplicationTeamResource> getApplicationTeam(long applicationId) {
		return getWithRestResult(applicationSummaryRestUrl + "/applicationTeam/" + applicationId, ApplicationTeamResource.class);
	}

	public void setApplicationSummaryRestUrl(String applicationSummaryRestUrl) {
		this.applicationSummaryRestUrl = applicationSummaryRestUrl;
	}

	protected String buildUri(String url, String sortField, int pageNumber, int pageSize, Optional<String> filter) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		filter.ifPresent(f -> params.put("filter", singletonList(f)));
		return buildPaginationUri(url, pageNumber, pageSize, sortField, params);
	}

}
