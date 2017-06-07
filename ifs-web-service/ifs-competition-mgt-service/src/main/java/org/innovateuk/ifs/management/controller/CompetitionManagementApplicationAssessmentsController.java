package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.innovateuk.ifs.application.service.ApplicationCountSummaryRestService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionsRestService;
import org.innovateuk.ifs.management.service.CompetitionManagementApplicationServiceImpl.ApplicationOverviewOrigin;
import org.innovateuk.ifs.management.model.ManageApplicationsModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;

/**
 * Controller for the manage application dashboard
 */
@Controller
@RequestMapping("/assessment/competition/{competitionId}")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class CompetitionManagementApplicationAssessmentsController {

    @Autowired
    private ApplicationCountSummaryRestService applicationCountSummaryRestService;

    @Autowired
    private CompetitionsRestService competitionService;

    @Autowired
    private ManageApplicationsModelPopulator manageApplicationsPopulator;

    @GetMapping("/applications")
    public String manageApplications(Model model,
                                     @PathVariable("competitionId") long competitionId,
                                     @RequestParam MultiValueMap<String, String> queryParams,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "filterSearch", defaultValue = "") String filter) {
        CompetitionResource competitionResource = competitionService.getCompetitionById(competitionId).getSuccessObjectOrThrowException();

        ApplicationCountSummaryPageResource applicationCounts = applicationCountSummaryRestService
                .getApplicationCountSummariesByCompetitionId(competitionId, page, 20, filter)
                .getSuccessObjectOrThrowException();

        String originQuery = buildOriginQueryString(ApplicationOverviewOrigin.MANAGE_APPLICATIONS, queryParams);

        model.addAttribute("model", manageApplicationsPopulator.populateModel(competitionResource, applicationCounts, filter, originQuery));
        model.addAttribute("originQuery", originQuery);

        return "competition/manage-applications";
    }

}
