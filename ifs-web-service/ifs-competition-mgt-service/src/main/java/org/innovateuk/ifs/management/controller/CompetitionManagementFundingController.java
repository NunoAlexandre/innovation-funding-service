package org.innovateuk.ifs.management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.form.*;
import org.innovateuk.ifs.competition.service.ApplicationSummarySortFieldService;
import org.innovateuk.ifs.management.service.CompetitionManagementApplicationServiceImpl;
import org.innovateuk.ifs.management.viewmodel.PaginationViewModel;
import org.innovateuk.ifs.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;
import static org.innovateuk.ifs.util.JsonUtil.getObjectFromJson;
import static org.innovateuk.ifs.util.JsonUtil.getSerializedObject;

/**
 * Handles the Competition Management Funding decision views and submission of funding decision.
 */
@Controller
@RequestMapping("/competition/{competitionId}/funding")
@PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
public class CompetitionManagementFundingController {

    private static final Log log = LogFactory.getLog(CompetitionManagementFundingController.class);

    public static final Collection<String> FILTERED_PARAMS = asList(
            "applicationIds",
            "fundingDecision",
            "_csrf");

    private static final int PAGE_SIZE = 20;

    private static final String SELECTION_FORM = "fundingDecisionSelectionForm";

    @Autowired
    private ApplicationSummarySortFieldService applicationSummarySortFieldService;

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Autowired
    private ApplicationFundingDecisionService applicationFundingDecisionService;

    @Autowired
    @Qualifier("mvcValidator")
    private Validator validator;

    @Autowired
    private CookieUtil cookieUtil;

    @GetMapping
    public String applications(Model model,
                               @PathVariable("competitionId") long competitionId,
                               @RequestParam(name = "clearFilters", required = false) boolean clearFilters,
                               @ModelAttribute @Valid FundingDecisionPaginationForm paginationForm,
                               @ModelAttribute FundingDecisionFilterForm filterForm,
                               @ModelAttribute FundingDecisionSelectionForm selectionForm,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return "redirect:/competition/" + competitionId + "/funding";
        }



        CompetitionSummaryResource competitionSummary = applicationSummaryRestService
                .getCompetitionSummary(competitionId)
                .getSuccessObjectOrThrowException();

        model.addAttribute("competitionSummary", competitionSummary);
        String originQuery = buildOriginQueryString(CompetitionManagementApplicationServiceImpl.ApplicationOverviewOrigin.FUNDING_APPLICATIONS, mapFormFilterParametersToMultiValueMap(filterForm));
        model.addAttribute("originQuery", originQuery);

        FundingDecisionSelectionCookie selectionCookieForm = new FundingDecisionSelectionCookie();

        try {
            selectionCookieForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            selectionForm = selectionCookieForm.getFundingDecisionSelectionForm();

            FundingDecisionSelectionForm trimmedSelectionForm = updateApplicationSelection(selectionForm, filterForm, competitionId);
            selectionForm.setApplicationIds(trimmedSelectionForm.getApplicationIds());

            boolean noFilterGetParameterIsPresent = !filterForm.getFundingFilter().isPresent() && !filterForm.getStringFilter().isPresent();
            boolean filterCookieParameterIsPresent = selectionCookieForm.getFundingDecisionFilterForm().getFundingFilter().isPresent()
                    || selectionCookieForm.getFundingDecisionFilterForm().getStringFilter().isPresent();
            boolean selectionHasBeenMade = selectionCookieForm.getFundingDecisionSelectionForm().isAllSelected() != false ||
                    selectionCookieForm.getFundingDecisionSelectionForm().getApplicationIds().size() > 0;

            if(clearFilters) {
                filterForm = new FundingDecisionFilterForm();
            }
            else if (noFilterGetParameterIsPresent && filterCookieParameterIsPresent && selectionHasBeenMade) {
                filterForm.setFundingFilter(selectionCookieForm.getFundingDecisionFilterForm().getFundingFilter());
                filterForm.setStringFilter(selectionCookieForm.getFundingDecisionFilterForm().getStringFilter());
            }
        } catch (Exception e) {
            log.error(e);
        }


        selectionCookieForm.setFundingDecisionFilterForm(filterForm);

        cookieUtil.saveToCookie(response, format("%s_comp%s", SELECTION_FORM, competitionId), getSerializedObject(selectionCookieForm));

        switch (competitionSummary.getCompetitionStatus()) {
            case FUNDERS_PANEL:
            case ASSESSOR_FEEDBACK:
                return populateSubmittedModel(model, competitionId, paginationForm, filterForm, selectionForm, originQuery);
            default:
                return "redirect:/login";
        }
    }

    @PostMapping
    public String makeDecision(Model model,
                               @PathVariable("competitionId") long competitionId,
                               @ModelAttribute @Valid FundingDecisionPaginationForm paginationForm,
                               @ModelAttribute @Valid FundingDecisionSelectionForm fundingDecisionSelectionForm,
                               @ModelAttribute @Valid FundingDecisionChoiceForm fundingDecisionChoiceForm,
                               @ModelAttribute FundingDecisionFilterForm filterForm,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "redirect:/competition/" + competitionId + "/funding";
        }

        try {
            FundingDecisionSelectionCookie selectionForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            fundingDecisionSelectionForm = selectionForm.getFundingDecisionSelectionForm();
        } catch (Exception e) {
            log.error(e);
        }

        CompetitionSummaryResource competitionSummary = applicationSummaryRestService
                .getCompetitionSummary(competitionId)
                .getSuccessObjectOrThrowException();

        model.addAttribute("competitionSummary", competitionSummary);
        String originQuery = buildOriginQueryString(CompetitionManagementApplicationServiceImpl.ApplicationOverviewOrigin.FUNDING_APPLICATIONS, mapFormFilterParametersToMultiValueMap(filterForm));
        model.addAttribute("originQuery", originQuery);

        switch (competitionSummary.getCompetitionStatus()) {
            case FUNDERS_PANEL:
            case ASSESSOR_FEEDBACK:
                return fundersPanelCompetition(model, competitionId, fundingDecisionSelectionForm, paginationForm,fundingDecisionChoiceForm, filterForm, originQuery, bindingResult);
            default:
                return "redirect:/login";
        }
    }

    @PostMapping(params = {"addAll"})
    public @ResponseBody
    JsonNode addAllApplicationsToFundingDecisionSelectionList(@PathVariable("competitionId") long competitionId,
                                                     @RequestParam("addAll") boolean addAll,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        try {
            FundingDecisionSelectionCookie selectionForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());

            if (addAll) {
                selectionForm.getFundingDecisionSelectionForm().setApplicationIds(getAllApplicationIdsByFilters(competitionId, selectionForm.getFundingDecisionFilterForm()));
                selectionForm.getFundingDecisionSelectionForm().setAllSelected(true);
            } else {
                selectionForm.getFundingDecisionSelectionForm().setApplicationIds(Arrays.asList());
                selectionForm.getFundingDecisionSelectionForm().setAllSelected(false);
            }

            cookieUtil.saveToCookie(response, format("%s_comp%s", SELECTION_FORM, competitionId), getSerializedObject(selectionForm));
            return createJsonObjectNode(selectionForm.getFundingDecisionSelectionForm().getApplicationIds().size());
        } catch (Exception e) {
            log.error(e);
            return createJsonObjectNode(-1);
        }
    }

    @PostMapping(params = {"assessor", "isSelected"})
    public @ResponseBody JsonNode addSelectedApplicationsToFundingDecisionList(@PathVariable("competitionId") long competitionId,
                                                               @RequestParam("assessor") long applicationId,
                                                               @RequestParam("isSelected") boolean isSelected,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
        try {
            FundingDecisionSelectionCookie cookieForm = getApplicationSelectionFormFromCookie(request, competitionId).orElse(new FundingDecisionSelectionCookie());
            FundingDecisionSelectionForm selectionForm = cookieForm.getFundingDecisionSelectionForm();
            if (isSelected) {
                List<Long> applicationIds = selectionForm.getApplicationIds();

                if(!applicationIds.contains(applicationId)) {
                    selectionForm.getApplicationIds().add(applicationId);
                    List<Long> filteredApplicationList = getAllApplicationIdsByFilters(competitionId, cookieForm.getFundingDecisionFilterForm());
                    if(applicationIds.containsAll(filteredApplicationList)) {
                        selectionForm.setAllSelected(true);
                    }
                }
            } else {
                selectionForm.getApplicationIds().remove(applicationId);
                selectionForm.setAllSelected(false);
            }

            cookieForm.setFundingDecisionSelectionForm(selectionForm);

            cookieUtil.saveToCookie(response, format("%s_comp%s", SELECTION_FORM, competitionId), getSerializedObject(cookieForm));
            return createJsonObjectNode(selectionForm.getApplicationIds().size());
        } catch (Exception e) {
            log.error(e);
            return createJsonObjectNode(-1);
        }
    }

    private Optional<FundingDecisionSelectionCookie> getApplicationSelectionFormFromCookie(HttpServletRequest request, long competitionId) {
        String assessorFormJson = cookieUtil.getCookieValue(request, format("%s_comp%s", SELECTION_FORM, competitionId));
        if (isNotBlank(assessorFormJson)) {
            return Optional.ofNullable(getObjectFromJson(assessorFormJson, FundingDecisionSelectionCookie.class));
        } else {
            return Optional.empty();
        }
    }

    private List<Long> getAllApplicationIdsByFilters(Long competitionId, FundingDecisionFilterForm filterForm) {
        RestResult<List<ApplicationSummaryResource>> restResult = applicationSummaryRestService.getAllSubmittedApplications(competitionId, filterForm.getStringFilter(), filterForm.getFundingFilter());

        return restResult.getSuccessObjectOrThrowException().stream().map(p -> p.getId()).collect(Collectors.toList());
    }

    private FundingDecisionSelectionForm updateApplicationSelection(FundingDecisionSelectionForm selectionForm, FundingDecisionFilterForm filterForm, Long competitionId) {
        List<Long> filteredApplicationIds = getAllApplicationIdsByFilters(competitionId, filterForm);

        FundingDecisionSelectionForm updatedSelectionForm = new FundingDecisionSelectionForm();

        if(selectionForm.isAllSelected()) {
            updatedSelectionForm.setApplicationIds(filteredApplicationIds);
        }
        else {
            selectionForm.getApplicationIds().retainAll(filteredApplicationIds);
            updatedSelectionForm.setApplicationIds(selectionForm.getApplicationIds());
        }

        return updatedSelectionForm;
    }

    private ObjectNode createJsonObjectNode(int selectionCount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("selectionCount", selectionCount);

        return node;
    }

    MultiValueMap<String, String> mapFormFilterParametersToMultiValueMap(FundingDecisionFilterForm fundingDecisionFilterForm) {
        MultiValueMap<String, String> filterMap = new LinkedMultiValueMap<>();
        if(fundingDecisionFilterForm.getFundingFilter().isPresent()) {
            filterMap.put("fundingFilter", Arrays.asList(fundingDecisionFilterForm.getFundingFilter().get().getName()));
        }
        if(fundingDecisionFilterForm.getStringFilter().isPresent()) {
            filterMap.put("stringFilter",Arrays.asList(fundingDecisionFilterForm.getStringFilter().get()));
        }

        return filterMap;
    }

    private String fundersPanelCompetition(Model model,
                                           Long competitionId,
                                           FundingDecisionSelectionForm fundingDecisionSelectionForm,
                                           FundingDecisionPaginationForm fundingDecisionPaginationForm,
                                           FundingDecisionChoiceForm fundingDecisionChoiceForm,
                                           FundingDecisionFilterForm fundingDecisionFilterForm,
                                           String originQuery,
                                           BindingResult bindingResult) {
        if (fundingDecisionChoiceForm.getFundingDecision() != null) {
            validator.validate(fundingDecisionSelectionForm, bindingResult);
            if (!bindingResult.hasErrors()) {
                Optional<FundingDecision> fundingDecision = applicationFundingDecisionService.getFundingDecisionForString(fundingDecisionChoiceForm.getFundingDecision());
                if (fundingDecision.isPresent()) {
                    applicationFundingDecisionService.saveApplicationFundingDecisionData(competitionId, fundingDecision.get(), fundingDecisionSelectionForm.getApplicationIds());
                }
            }
        }

        return populateSubmittedModel(model, competitionId, fundingDecisionPaginationForm, fundingDecisionFilterForm, fundingDecisionSelectionForm, originQuery);
    }

    private ApplicationSummaryPageResource getApplicationsByFilters(Long competitionId, FundingDecisionPaginationForm paginationForm, FundingDecisionFilterForm fundingDecisionFilterForm) {
        return applicationSummaryRestService.getSubmittedApplications(
                competitionId,
                "id",
                paginationForm.getPage(),
                PAGE_SIZE,
                fundingDecisionFilterForm.getStringFilter(),
                fundingDecisionFilterForm.getFundingFilter())
                .getSuccessObjectOrThrowException();
    }

    private String populateSubmittedModel(Model model, Long competitionId, FundingDecisionPaginationForm paginationForm, FundingDecisionFilterForm fundingDecisionFilterForm, FundingDecisionSelectionForm fundingDecisionSelectionForm, String originQuery) {
        ApplicationSummaryPageResource results = getApplicationsByFilters(competitionId, paginationForm, fundingDecisionFilterForm);

        model.addAttribute("pagination", new PaginationViewModel(results, originQuery));
        model.addAttribute("results", results);
        model.addAttribute("selectionForm", fundingDecisionSelectionForm);

        return "comp-mgt-funders-panel";
    }
}
