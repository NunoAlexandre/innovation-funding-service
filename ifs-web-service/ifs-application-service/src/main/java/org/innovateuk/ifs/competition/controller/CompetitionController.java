package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.populator.CompetitionOverviewPopulator;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;

/**
 * This controller will handle all requests that are related to a competition.
 */
@Controller
@RequestMapping("/competition/{competitionId}")
@PreAuthorize("permitAll")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionOverviewPopulator overviewPopulator;

    @GetMapping("overview")
    public String competitionOverview(final Model model,
                                      @PathVariable("competitionId") final long competitionId,
                                      @ModelAttribute(name = "loggedInUser", binding = false) UserResource loggedInUser) {
        final PublicContentItemResource publicContentItem = competitionService.getPublicContentOfCompetition(competitionId);
        model.addAttribute("model", overviewPopulator.populateViewModel(publicContentItem, loggedInUser != null));
        return "competition/overview";
    }

    @GetMapping("download/{contentGroupId}")
    public ResponseEntity<ByteArrayResource> getFileDetails(@PathVariable("competitionId") final long competitionId,
                                                            @PathVariable("contentGroupId") final long contentGroupId) {
        final ByteArrayResource resource = competitionService.downloadPublicContentAttachment(contentGroupId);
        final FileEntryResource fileDetails = competitionService.getPublicContentFileDetails(contentGroupId);
        return getFileResponseEntity(resource, fileDetails);
    }

    @GetMapping("details")
    public String competitionDetails(final Model model, @PathVariable("competitionId") final long competitionId) {
        addCompetitionToModel(model, competitionId);
        return "competition/details";
    }

    @GetMapping("info/before-you-apply")
    public String beforeYouApply(final Model model, @PathVariable("competitionId") final long competitionId) {
        addCompetitionToModel(model, competitionId);
        return "competition/info/before-you-apply";
    }

    @GetMapping("info/eligibility")
    public String eligibility(final Model model, @PathVariable("competitionId") final long competitionId) {
        addCompetitionToModel(model, competitionId);
        return "competition/info/eligibility";
    }

    @GetMapping("info/terms-and-conditions")
    public String termsAndConditions(@PathVariable("competitionId") final long competitionId) {
        return "competition/info/terms-and-conditions";
    }

    @GetMapping("info/what-we-ask-you")
    public String whatWeAskYou(final Model model, @PathVariable("competitionId") final long competitionId) {
        addCompetitionToModel(model, competitionId);
        return "competition/info/what-we-ask-you";
    }

    private void addCompetitionToModel(final Model model, final long competitionId) {
        model.addAttribute("currentCompetition", competitionService.getPublishedById(competitionId));
    }
}