package org.innovateuk.ifs.project;

import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.rest.LocalDateResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.project.finance.ProjectFinanceService;
import org.innovateuk.ifs.project.form.SpendProfileForm;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.util.DateUtil;
import org.innovateuk.ifs.project.util.FinancialYearDate;
import org.innovateuk.ifs.project.util.SpendProfileTableCalculator;
import org.innovateuk.ifs.commons.validation.SpendProfileCostValidator;
import org.innovateuk.ifs.project.viewmodel.ProjectSpendProfileProjectManagerViewModel;
import org.innovateuk.ifs.project.viewmodel.ProjectSpendProfileViewModel;
import org.innovateuk.ifs.project.viewmodel.SpendProfileSummaryModel;
import org.innovateuk.ifs.project.viewmodel.SpendProfileSummaryYearModel;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE;
import static org.innovateuk.ifs.project.constant.ProjectActivityStates.COMPLETE;
import static org.innovateuk.ifs.project.util.ControllersUtil.isLeadPartner;
import static org.innovateuk.ifs.user.resource.UserRoleType.PARTNER;
import static org.innovateuk.ifs.user.resource.UserRoleType.PROJECT_MANAGER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * This controller will handle all requests that are related to spend profile.
 */
@Controller
@RequestMapping("/" + ProjectSpendProfileController.BASE_DIR + "/{projectId}/partner-organisation/{organisationId}/spend-profile")
public class ProjectSpendProfileController {

    private static final String FORM_ATTR_NAME = "form";
    public static final String BASE_DIR = "project";
    public static final String REVIEW_TEMPLATE_NAME = "spend-profile-review";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProjectFinanceService projectFinanceService;

    @Autowired
    private SpendProfileTableCalculator spendProfileTableCalculator;

    @Autowired
    @Qualifier("spendProfileCostValidator")
    private SpendProfileCostValidator spendProfileCostValidator;

    @Autowired
    private PartnerOrganisationService partnerOrganisationService;

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION')")
    @RequestMapping(method = GET)
    public String viewSpendProfile(Model model,
                                   @PathVariable("projectId") final Long projectId,
                                   @PathVariable("organisationId") final Long organisationId,
                                   @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        if (userHasProjectManagerRole(loggedInUser, projectId)) {
            return viewProjectManagerSpendProfile(model, projectId, loggedInUser);
        }
        return reviewSpendProfilePage(model, projectId, organisationId, loggedInUser);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION')")
    @RequestMapping(value = "/review", method = GET)
    public String reviewSpendProfilePage(Model model,
                                         @PathVariable("projectId") final Long projectId,
                                         @PathVariable("organisationId") final Long organisationId,
                                         @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        model.addAttribute("model", buildSpendProfileViewModel(projectId, organisationId, loggedInUser));

        return BASE_DIR + "/spend-profile";
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION')")
    @RequestMapping(value = "/edit", method = GET)
    public String editSpendProfile(Model model,
                                   HttpServletRequest request,
                                   @ModelAttribute(FORM_ATTR_NAME) SpendProfileForm form,
                                   @SuppressWarnings("unused") BindingResult bindingResult,
                                   ValidationHandler validationHandler,
                                   @PathVariable("projectId") final Long projectId,
                                   @PathVariable("organisationId") final Long organisationId,
                                   @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        String failureView = "redirect:/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile";

        ProjectResource projectResource = projectService.getById(projectId);
        SpendProfileTableResource spendProfileTableResource = projectFinanceService.getSpendProfileTable(projectId, organisationId);
        form.setTable(spendProfileTableResource);

        if (!spendProfileTableResource.getMarkedAsComplete()) {
            model.addAttribute("model", buildSpendProfileViewModel(projectResource, organisationId, spendProfileTableResource, loggedInUser));

            return validationHandler.failNowOrSucceedWith(() -> BASE_DIR + "/spend-profile", () -> BASE_DIR + "/spend-profile");
        } else {
            ServiceResult<Void> result = markSpendProfileIncomplete(projectId, organisationId);
            return validationHandler.addAnyErrors(result).failNowOrSucceedWith(() -> failureView, () -> {

                model.addAttribute("model", buildSpendProfileViewModel(projectResource, organisationId, spendProfileTableResource, loggedInUser));
                return BASE_DIR + "/spend-profile";
            });
        }
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION')")
    @RequestMapping(value = "/edit", method = POST)
    public String saveSpendProfile(Model model,
                                   @ModelAttribute(FORM_ATTR_NAME) SpendProfileForm form,
                                   @SuppressWarnings("unused") BindingResult bindingResult,
                                   ValidationHandler validationHandler,
                                   @PathVariable("projectId") final Long projectId,
                                   @PathVariable("organisationId") final Long organisationId,
                                   @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        Supplier<String> failureView = () -> {

            SpendProfileTableResource updatedTable = form.getTable();
            SpendProfileTableResource originalTableWithUpdatedCosts = projectFinanceService.getSpendProfileTable(projectId, organisationId);
            originalTableWithUpdatedCosts.setMonthlyCostsPerCategoryMap(updatedTable.getMonthlyCostsPerCategoryMap());

            ProjectResource project = projectService.getById(projectId);

            return doEditSpendProfile(model, form, organisationId, loggedInUser, project, originalTableWithUpdatedCosts);
        };

        String successView = "redirect:/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile";

        spendProfileCostValidator.validate(form.getTable(), bindingResult);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            SpendProfileTableResource spendProfileTableResource = projectFinanceService.getSpendProfileTable(projectId, organisationId);
            spendProfileTableResource.setMonthlyCostsPerCategoryMap(form.getTable().getMonthlyCostsPerCategoryMap()); // update existing resource with user entered fields

            ServiceResult<Void> result = projectFinanceService.saveSpendProfile(projectId, organisationId, spendProfileTableResource);

            return validationHandler.addAnyErrors(result).failNowOrSucceedWith(failureView, () -> successView);
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION') && hasPermission(#projectId, 'MARK_SPEND_PROFILE_INCOMPLETE')")
    @RequestMapping(value = "/incomplete", method = POST)
    public String markAsActionRequiredSpendProfile(Model model,
                                                   @ModelAttribute(FORM_ATTR_NAME) SpendProfileForm form,
                                                   BindingResult bindingResult,
                                                   ValidationHandler validationHandler,
                                                   @PathVariable("projectId") final Long projectId,
                                                   @PathVariable("organisationId") final Long organisationId,
                                                   @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        Supplier<String> failureView = () -> reviewSpendProfilePage(model, projectId, organisationId, loggedInUser);
        String successView = "redirect:/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile";

        ServiceResult<Void> result = markSpendProfileIncomplete(projectId, organisationId);

        return validationHandler.addAnyErrors(result).failNowOrSucceedWith(failureView, () -> successView);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_SPEND_PROFILE_SECTION')")
    @RequestMapping(value = "/complete", method = POST)
    public String markAsCompleteSpendProfile(Model model,
                                             @PathVariable("projectId") final Long projectId,
                                             @PathVariable("organisationId") final Long organisationId,
                                             @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        return markSpendProfileComplete(model, projectId, organisationId, "redirect:/project/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile", loggedInUser);
    }

    private String doEditSpendProfile(Model model, SpendProfileForm form, Long organisationId, UserResource loggedInUser,
                                      ProjectResource project, SpendProfileTableResource spendProfileTableResource) {

        form.setTable(spendProfileTableResource);

        model.addAttribute("model", buildSpendProfileViewModel(project, organisationId, spendProfileTableResource, loggedInUser));

        return BASE_DIR + "/spend-profile";
    }

    private String viewProjectManagerSpendProfile(Model model, Long projectId, UserResource loggedInUser) {
        model.addAttribute("model", populateSpendProfileProjectManagerViewModel(projectId, loggedInUser));
        return BASE_DIR + "/" + REVIEW_TEMPLATE_NAME;
    }

    private Map<String, Boolean> getPartnersSpendProfileProgress(Long projectId, List<OrganisationResource> partnerOrganisations) {
        HashMap<String, Boolean> partnerProgressMap = new HashMap<>();
        partnerOrganisations.stream().forEach(organisation -> {
            Optional<SpendProfileResource> spendProfile = projectFinanceService.getSpendProfile(projectId, organisation.getId());
            partnerProgressMap.put(organisation.getName(), spendProfile.get().isMarkedAsComplete());
        });
        return partnerProgressMap;
    }

    private String markSpendProfileComplete(Model model,
                                            Long projectId,
                                            Long organisationId,
                                            String successView,
                                            UserResource loggedInUser) {
        ServiceResult<Void> result = projectFinanceService.markSpendProfileComplete(projectId, organisationId);
        if (result.isFailure()) {
            ProjectSpendProfileViewModel spendProfileViewModel = buildSpendProfileViewModel(projectId, organisationId, loggedInUser);
            spendProfileViewModel.setObjectErrors(Collections.singletonList(new ObjectError(SPEND_PROFILE_CANNOT_MARK_AS_COMPLETE_BECAUSE_SPEND_HIGHER_THAN_ELIGIBLE.getErrorKey(), "Cannot mark as complete, because totals more than eligible")));
            model.addAttribute("model", spendProfileViewModel);
            return BASE_DIR + "/spend-profile";
        } else {
            return successView;
        }
    }

    private ServiceResult<Void> markSpendProfileIncomplete(Long projectId,
                                                           Long organisationId) {

        return projectFinanceService.markSpendProfileIncomplete(projectId, organisationId);
    }

    private ProjectSpendProfileViewModel buildSpendProfileViewModel(final ProjectResource projectResource, final Long organisationId,
                                                                    final SpendProfileTableResource spendProfileTableResource, final UserResource loggedInUser) {
        SpendProfileSummaryModel summary = spendProfileTableCalculator.createSpendProfileSummary(projectResource, spendProfileTableResource.getMonthlyCostsPerCategoryMap(), spendProfileTableResource.getMonths());

        OrganisationResource organisationResource = organisationService.getOrganisationById(organisationId);

        boolean isResearch = OrganisationTypeEnum.isResearch(organisationResource.getOrganisationType());
        Map<Long, BigDecimal> categoryToActualTotal = spendProfileTableCalculator.calculateRowTotal(spendProfileTableResource.getMonthlyCostsPerCategoryMap());
        List<BigDecimal> totalForEachMonth = spendProfileTableCalculator.calculateMonthlyTotals(spendProfileTableResource.getMonthlyCostsPerCategoryMap(), spendProfileTableResource.getMonths().size());

        BigDecimal totalOfAllActualTotals = spendProfileTableCalculator.calculateTotalOfAllActualTotals(spendProfileTableResource.getMonthlyCostsPerCategoryMap());
        BigDecimal totalOfAllEligibleTotals = spendProfileTableCalculator.calculateTotalOfAllEligibleTotals(spendProfileTableResource.getEligibleCostPerCategoryMap());

        boolean isUserPartOfThisOrganisation = isUserPartOfThisOrganisation(projectResource.getId(), organisationId, loggedInUser);

        boolean leadPartner = isLeadPartner(partnerOrganisationService, projectResource.getId(), organisationId);

        return new ProjectSpendProfileViewModel(projectResource, organisationResource, spendProfileTableResource, summary,
                spendProfileTableResource.getMarkedAsComplete(), categoryToActualTotal, totalForEachMonth,
                totalOfAllActualTotals, totalOfAllEligibleTotals, projectResource.getSpendProfileSubmittedDate() != null, spendProfileTableResource.getCostCategoryGroupMap(),
                spendProfileTableResource.getCostCategoryResourceMap(), isResearch, isUserPartOfThisOrganisation, userHasProjectManagerRole(loggedInUser, projectResource.getId()),
                isApproved(projectResource.getId()), leadPartner);
    }

    private ProjectSpendProfileViewModel buildSpendProfileViewModel(Long projectId, Long organisationId, final UserResource loggedInUser) {
        ProjectResource projectResource = projectService.getById(projectId);
        SpendProfileTableResource spendProfileTableResource = projectFinanceService.getSpendProfileTable(projectId, organisationId);
        return buildSpendProfileViewModel(projectResource, organisationId, spendProfileTableResource, loggedInUser);
    }

    private ProjectSpendProfileProjectManagerViewModel populateSpendProfileProjectManagerViewModel(final Long projectId,
                                                                                                   final UserResource loggedInUser) {
        ProjectResource projectResource = projectService.getById(projectId);

        List<OrganisationResource> partnerOrganisations = projectService.getPartnerOrganisationsForProject(projectId);

        Map<String, Boolean> partnersSpendProfileProgress = getPartnersSpendProfileProgress(projectId, partnerOrganisations);

        Map<String, Boolean> editablePartners = determineEditablePartners(projectId, partnerOrganisations, loggedInUser);

        return new ProjectSpendProfileProjectManagerViewModel(projectId,
                projectResource.getApplication(), projectResource.getName(),
                partnersSpendProfileProgress,
                partnerOrganisations,
                projectResource.getSpendProfileSubmittedDate() != null,
                editablePartners,
                isApproved(projectId));
    }

    private boolean isApproved(final Long projectId) {
        ProjectTeamStatusResource teamStatus = projectService.getProjectTeamStatus(projectId, Optional.empty());
        return COMPLETE.equals(teamStatus.getLeadPartnerStatus().getSpendProfileStatus());
    }

    private boolean userHasProjectManagerRole(UserResource user, Long projectId) {
        Optional<ProjectUserResource> existingProjectManager = getProjectManager(projectId);
        return existingProjectManager.isPresent() && existingProjectManager.get().getUser().equals(user.getId());
    }

    private Optional<ProjectUserResource> getProjectManager(Long projectId) {
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pu -> PROJECT_MANAGER.getName().equals(pu.getRoleName()));
    }

    private Map<String, Boolean> determineEditablePartners(Long projectId, List<OrganisationResource> partnerOrganisations, final UserResource loggedInUser) {
        Map<String, Boolean> editablePartnersMap = new HashMap<>();
        partnerOrganisations.stream().forEach(organisation -> {
            boolean isUserPartOfThisOrganisation = isUserPartOfThisOrganisation(projectId, organisation.getId(), loggedInUser);
            editablePartnersMap.put(organisation.getName(), isUserPartOfThisOrganisation);
        });
        return editablePartnersMap;
    }

    private boolean isUserPartOfThisOrganisation(final Long projectId, final Long organisationId, final UserResource loggedInUser) {

        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        Optional<ProjectUserResource> returnedProjectUser = simpleFindFirst(projectUsers, projectUserResource -> projectUserResource.getUser().equals(loggedInUser.getId())
                && projectUserResource.getOrganisation().equals(organisationId)
                && PARTNER.getName().equals(projectUserResource.getRoleName())
        );

        return returnedProjectUser.isPresent();
    }
}
