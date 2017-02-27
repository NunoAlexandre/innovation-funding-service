package org.innovateuk.ifs.project.financecheck.controller;

import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.controller.FileDownloadControllerUtils;
import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.project.PartnerOrganisationService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.finance.ProjectFinanceService;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckResource;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckSummaryResource;
import org.innovateuk.ifs.project.finance.workflow.financechecks.resource.FinanceCheckProcessResource;
import org.innovateuk.ifs.project.financecheck.FinanceCheckService;
import org.innovateuk.ifs.project.financecheck.form.CostFormField;
import org.innovateuk.ifs.project.financecheck.form.FinanceCheckForm;
import org.innovateuk.ifs.project.financecheck.form.FinanceCheckSummaryForm;
import org.innovateuk.ifs.project.financecheck.viewmodel.FinanceCheckViewModel;
import org.innovateuk.ifs.project.financecheck.viewmodel.ProjectFinanceCheckSummaryViewModel;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.innovateuk.ifs.application.resource.ApplicationResource.formatter;
import static org.innovateuk.ifs.project.finance.resource.FinanceCheckState.APPROVED;
import static org.innovateuk.ifs.project.util.ControllersUtil.isLeadPartner;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * This controller is for allowing internal users to view and update application finances entered by applicants
 * It is only used by the workaround in place for adding finance check eligible totals currently.  It will in future
 * be replaced by different controller.
 */
@Controller
@RequestMapping("/project/{projectId}/finance-check")
public class FinanceCheckController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectFinanceService projectFinanceService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FinanceCheckService financeCheckService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private PartnerOrganisationService partnerOrganisationService;

    @RequestMapping(value = "/organisation/{organisationId}", method = GET)
    @PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
    public String view(@PathVariable("projectId") final Long projectId, @PathVariable("organisationId") Long organisationId,
                       @ModelAttribute(FORM_ATTR_NAME) FinanceCheckForm form,
                       @ModelAttribute("loggedInUser") UserResource loggedInUser,
                       Model model){
        FinanceCheckResource financeCheckResource = getFinanceCheckResource(projectId, organisationId);
        populateExistingFinanceCheckDetailsInForm(financeCheckResource, form);
        return doViewFinanceCheckForm(projectId, organisationId, model);
    }

    @RequestMapping(value = "/organisation/{organisationId}", method = POST)
    @PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
    public String update(@PathVariable("projectId") Long projectId,
                         @PathVariable("organisationId") Long organisationId,
                         @ModelAttribute(FORM_ATTR_NAME) @Valid FinanceCheckForm form,
                         @SuppressWarnings("unused") BindingResult bindingResult,
                         ValidationHandler validationHandler) {

        return validationHandler.failNowOrSucceedWith(
                () -> redirectToFinanceCheckForm(projectId, organisationId),
                () -> updateFinanceCheck(getFinanceCheckResource(projectId, organisationId), form, validationHandler));
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_SECTION')")
    @RequestMapping(method = GET)
    public String viewFinanceCheckSummary(@PathVariable Long projectId, Model model,
                                          @ModelAttribute FinanceCheckSummaryForm form) {
        return doViewFinanceCheckSummary(projectId, model);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_FINANCE_CHECKS_SECTION')")
    @RequestMapping(value = "/generate", method = POST)
    public String generateSpendProfile(@PathVariable Long projectId, Model model,
                                       @ModelAttribute FinanceCheckSummaryForm form,
                                       @SuppressWarnings("unused") BindingResult bindingResult,
                                       ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> doViewFinanceCheckSummary(projectId, model);
        ServiceResult<Void> generateResult = projectFinanceService.generateSpendProfile(projectId);

        return validationHandler.addAnyErrors(generateResult).failNowOrSucceedWith(failureView, () ->
                redirectToViewFinanceCheckSummary(projectId)
        );
    }

    @PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
    @RequestMapping(value = "/organisation/{organisationId}", params = "approve", method = POST)
    public String approveFinanceCheck(@PathVariable Long projectId, @PathVariable Long organisationId, Model model,
                                       @ModelAttribute FinanceCheckForm form,
                                       @SuppressWarnings("unused") BindingResult bindingResult,
                                       ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> doViewFinanceCheckForm(projectId, organisationId, model);

        ServiceResult<Void> updateResult = doUpdateFinanceCheck(getFinanceCheckResource(projectId, organisationId), form);
        ServiceResult<Void> approveResult = updateResult.andOnSuccess(() -> financeCheckService.approveFinanceCheck(projectId, organisationId));

        return validationHandler.addAnyErrors(approveResult).failNowOrSucceedWith(failureView, () ->
                redirectToFinanceCheckForm(projectId, organisationId)
        );
    }

    @PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
    @RequestMapping(value = "/organisation/{organisationId}/jes-file", method = GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadJesFile(@PathVariable("projectId") final Long projectId,
                                                                           @PathVariable("organisationId") Long organisationId) {

        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());

        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceByApplicationIdAndOrganisationId(application.getId(), organisationId);

        if (applicationFinanceResource.getFinanceFileEntry() != null) {
            FileEntryResource jesFileEntryResource = financeService.getFinanceEntry(applicationFinanceResource.getFinanceFileEntry()).getSuccessObject();
            ByteArrayResource jesByteArrayResource = financeService.getFinanceDocumentByApplicationFinance(applicationFinanceResource.getId()).getSuccessObject();
            return FileDownloadControllerUtils.getFileResponseEntity(jesByteArrayResource, jesFileEntryResource);
        }

        return new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT);

    }

    private String redirectToFinanceCheckForm(Long projectId, Long organisationId){
        return "redirect:/project/" + projectId + "/finance-check/organisation/" + organisationId;
    }

    private void populateExistingFinanceCheckDetailsInForm(FinanceCheckResource financeCheckResource, FinanceCheckForm form){
        form.setCosts(simpleMap(financeCheckResource.getCostGroup().getCosts(), c -> {
            CostFormField cf = new CostFormField();
            cf.setId(c.getId());
            cf.setValue(c.getValue());
            return cf;
        }));
    }

    private String doViewFinanceCheckForm(Long projectId, Long organisationId, Model model){
        populateFinanceCheckModel(projectId, organisationId, model);
        return "project/financecheck/partner-project-eligibility";
    }

    private void populateFinanceCheckModel(Long projectId, Long organisationId, Model model){
        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());
        String competitionName = application.getCompetitionName();
        String formattedCompId = formatter.format(application.getCompetition());

        OrganisationResource organisationResource = organisationService.getOrganisationById(organisationId);
        boolean isResearch = OrganisationTypeEnum.isResearch(organisationResource.getOrganisationType());
        Optional<ProjectUserResource> financeContact = getFinanceContact(projectId, organisationId);

        FinanceCheckProcessResource financeCheckStatus = financeCheckService.getFinanceCheckApprovalStatus(projectId, organisationId);
        boolean financeChecksApproved = APPROVED.equals(financeCheckStatus.getCurrentState());
        String approverName = financeCheckStatus.getInternalParticipant() != null ? financeCheckStatus.getInternalParticipant().getName() : null;
        LocalDate approvalDate = financeCheckStatus.getModifiedDate().toLocalDate();

        boolean isLeadPartner = isLeadPartner(partnerOrganisationService, projectId, organisationId);

        FileDetailsViewModel jesFileDetailsViewModel = null;
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceByApplicationIdAndOrganisationId(application.getId(), organisationId);
        if (applicationFinanceResource.getFinanceFileEntry() != null) {
            FileEntryResource jesFileEntryResource = financeService.getFinanceEntry(applicationFinanceResource.getFinanceFileEntry()).getSuccessObject();
            jesFileDetailsViewModel = new FileDetailsViewModel(jesFileEntryResource);
        }

        FinanceCheckViewModel financeCheckViewModel = new FinanceCheckViewModel(formattedCompId, competitionName, organisationResource.getName(),
                isLeadPartner, projectId, organisationId, isResearch, financeChecksApproved, approverName, approvalDate, jesFileDetailsViewModel);

        if (financeContact.isPresent()) { // Internal users may still view finance contact page without finance contact being set.  They will see a message warning about this on template.
            financeCheckViewModel.setFinanceContactName(financeContact.get().getUserName());
            financeCheckViewModel.setFinanceContactEmail(financeContact.get().getEmail());
        }

        model.addAttribute("model", financeCheckViewModel);
    }

    private Optional<ProjectUserResource> getFinanceContact(Long projectId, Long organisationId){
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);
        return simpleFindFirst(projectUsers, pr -> pr.isFinanceContact() && organisationId.equals(pr.getOrganisation()));
    }

    private FinanceCheckResource getFinanceCheckResource(Long projectId, Long organisationId){
        ProjectOrganisationCompositeId key = new ProjectOrganisationCompositeId(projectId, organisationId);
        return financeCheckService.getByProjectAndOrganisation(key);
    }

    private String updateFinanceCheck(FinanceCheckResource currentFinanceCheckResource, FinanceCheckForm financeCheckForm, ValidationHandler validationHandler){

        Supplier<String> failureView = () -> redirectToFinanceCheckForm(currentFinanceCheckResource.getProject(), currentFinanceCheckResource.getOrganisation());
        Supplier<String> successView = () -> redirectToViewFinanceCheckSummary(currentFinanceCheckResource.getProject());

        ServiceResult<Void> updateResult = doUpdateFinanceCheck(currentFinanceCheckResource, financeCheckForm);
        return validationHandler.addAnyErrors(updateResult).failNowOrSucceedWith(failureView, successView);
    }

    private ServiceResult<Void> doUpdateFinanceCheck(FinanceCheckResource currentFinanceCheckResource, FinanceCheckForm financeCheckForm) {
        for (int i = 0; i < financeCheckForm.getCosts().size(); i++) {
            currentFinanceCheckResource.getCostGroup().getCosts().get(i).setValue(financeCheckForm.getCosts().get(i).getValue());
        }

        return financeCheckService.update(currentFinanceCheckResource);
    }

    private String doViewFinanceCheckSummary(Long projectId, Model model) {
        FinanceCheckSummaryResource financeCheckSummaryResource = financeCheckService.getFinanceCheckSummary(projectId).getSuccessObjectOrThrowException();
        ProjectFinanceCheckSummaryViewModel projectFinanceCheckSummaryViewModel = new ProjectFinanceCheckSummaryViewModel(financeCheckSummaryResource);
        model.addAttribute("model", projectFinanceCheckSummaryViewModel);
        return "project/financecheck/summary";
    }

    private String redirectToViewFinanceCheckSummary(Long projectId) {
        return "redirect:/project/" + projectId + "/finance-check";
    }

}
