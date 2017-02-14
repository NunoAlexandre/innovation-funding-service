package org.innovateuk.ifs.competition.controller;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.security.UserAuthenticationService;
import org.innovateuk.ifs.competition.populator.CompetitionOverviewPopulator;
import org.innovateuk.ifs.competition.populator.publiccontent.AbstractPublicContentSectionViewModelPopulator;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionType;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;

/**
 * This controller will handle all requests that are related to a competition.
 */

@Controller
@RequestMapping("/competition")
@PreAuthorize("permitAll")
public class CompetitionController {
    public static final String TEMPLATE_PATH = "competition/";
    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private CompetitionOverviewPopulator overviewPopulator;

    private Map<PublicContentSectionType, AbstractPublicContentSectionViewModelPopulator> sectionModelPopulators;

    @Autowired
    public void setSectionPopulator(Collection<AbstractPublicContentSectionViewModelPopulator> populators) {
        sectionModelPopulators = populators.stream().collect(Collectors.toMap(p -> p.getType(), Function.identity()));
    }

    @RequestMapping(value = {"/{competitionId}/overview", "/{competitionId}/overview/{section}"})
    public String competitionOverview(Model model,
                                      @PathVariable("competitionId") final Long competitionId,
                                      @PathVariable(name = "section", required = false) final Optional<String> section,
                                     HttpServletRequest request) {

        Optional<PublicContentSectionType> selectedSection = PublicContentSectionType.fromPath(section.orElse(null));
        PublicContentItemResource publicContentItem = competitionService
                .getPublicContentOfCompetition(competitionId)
                .getSuccessObjectOrThrowException();

        model.addAttribute("model", overviewPopulator.populateViewModel(
                publicContentItem,
                getPopulator(selectedSection.orElse(PublicContentSectionType.SUMMARY)).populate(publicContentItem.getPublicContentResource())));
        return TEMPLATE_PATH + "overview";
    }

    @RequestMapping(value = "/{competitionId}/download/{contentGroupId}", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> getFileDetails(Model model,
                                                            @PathVariable("competitionId") Long competitionId,
                                                            @PathVariable("contentGroupId") Long contentGroupId) {
        final ByteArrayResource resource = competitionService.downloadPublicContentAttachment(contentGroupId).getSuccessObjectOrThrowException();
        FileEntryResource fileDetails = competitionService.getPublicContentFileDetails(contentGroupId).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails);
    }

    @RequestMapping("/{competitionId}/details")
    public String competitionDetails(Model model, @PathVariable("competitionId") final Long competitionId,
                                     HttpServletRequest request) {
        addUserToModel(model, request);
        addCompetitionToModel(model, competitionId);
        return TEMPLATE_PATH + "details";
    }

    @RequestMapping("/{competitionId}/info/{templateName}")
    public String getInfoPage(Model model, @PathVariable("competitionId") final Long competitionId,
                              HttpServletRequest request, @PathVariable("templateName") String templateName) {
        addUserToModel(model, request);
        addCompetitionToModel(model, competitionId);
        return TEMPLATE_PATH+"info/"+ templateName;
    }

    private void addUserToModel(Model model, HttpServletRequest request) {
        boolean userIsLoggedIn = userIsLoggedIn(request);
        model.addAttribute("userIsLoggedIn", userIsLoggedIn);
    }

    private void addCompetitionToModel(Model model, Long competitionId) {
        model.addAttribute("currentCompetition", competitionService.getById(competitionId));
    }

    private boolean userIsLoggedIn(HttpServletRequest request) {
        Authentication authentication = userAuthenticationService.getAuthentication(request);

        if(authentication != null) {
            return true;
        } else {
            return false;
        }
    }

    private AbstractPublicContentSectionViewModelPopulator getPopulator(PublicContentSectionType sectionType) {
        if(PublicContentSectionType.SEARCH.equals(sectionType)) {
            return null;
        }
        return sectionModelPopulators.getOrDefault(sectionType, null);
    }
}

