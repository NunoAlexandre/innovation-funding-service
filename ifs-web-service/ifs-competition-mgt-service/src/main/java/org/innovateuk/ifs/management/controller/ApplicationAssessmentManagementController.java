package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.application.service.ApplicationCountSummaryRestService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.management.controller.CompetitionManagementApplicationController.ApplicationOverviewOrigin;
import org.innovateuk.ifs.management.model.ManageApplicationsModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;

/**
 * Controller for the manage application dashboard
 */
@Controller
@RequestMapping("assessment/competition/{competitionId}")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class ApplicationAssessmentManagementController {

    @Autowired
    private ApplicationCountSummaryRestService  applicationCountSummaryRestService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ManageApplicationsModelPopulator manageApplicationsPopulator;


    @RequestMapping(method = RequestMethod.GET)
    public String manageApplications(Model model,
                                     @PathVariable("competitionId") long competitionId,
                                     @RequestParam MultiValueMap<String, String> queryParams) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);
        List<ApplicationCountSummaryResource> applicationCounts = applicationCountSummaryRestService.getApplicationCountSummariesByCompetitionId(competitionId)
                .getSuccessObjectOrThrowException();
        model.addAttribute("model", manageApplicationsPopulator.populateModel(competitionResource, applicationCounts));
        model.addAttribute("originQuery", buildOriginQueryString(ApplicationOverviewOrigin.MANAGE_APPLICATIONS, queryParams));

        return "competition/manage-applications";
    }

}
