package org.innovateuk.ifs.project.grantofferletter.controller;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.commons.service.FailingOrSucceedingResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.CaseInsensitiveConverter;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.controller.viewmodel.FileDetailsViewModel;
import org.innovateuk.ifs.project.grantofferletter.resource.GOLState;
import org.innovateuk.ifs.project.grantofferletter.ProjectGrantOfferService;
import org.innovateuk.ifs.project.grantofferletter.form.ProjectGrantOfferSendLetterForm;
import org.innovateuk.ifs.project.grantofferletter.viewmodel.ProjectGrantOfferSendLetterModel;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;
import java.util.Optional;

import static org.innovateuk.ifs.controller.FileUploadControllerUtils.getMultipartFileBytes;
import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
/**
 * This Controller handles Grant Offer Letter activity for the Internal Competition team members
 */
@Controller
@RequestMapping("/project/{projectId}/grant-offer-letter")
public class ProjectGrantOfferSendLetterController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Autowired
    private ProjectGrantOfferService projectGrantOfferService;

    private static final String FORM_ATTR = "form";

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ApprovalType.class, new CaseInsensitiveConverter<>(ApprovalType.class));
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @GetMapping("/send")
    public String viewGrantOfferLetterSend(@PathVariable Long projectId, Model model, @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        ProjectGrantOfferSendLetterForm form = new ProjectGrantOfferSendLetterForm();
        return doViewGrantOfferLetterSend(projectId, model, form);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @PostMapping("/send")
    public String sendGrantOfferLetter(@PathVariable Long projectId,
                                       @ModelAttribute(FORM_ATTR) ProjectGrantOfferSendLetterForm form,
                                       Model model,
                                       @SuppressWarnings("unused") BindingResult bindingResult,
                                       ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> doViewGrantOfferLetterSend(projectId, model, form);
        ServiceResult<Void> generateResult = projectGrantOfferService.sendGrantOfferLetter(projectId);

        return validationHandler.addAnyErrors(generateResult).failNowOrSucceedWith(failureView, () -> {return doViewGrantOfferLetterSend(projectId, model, form);}
        );
    }

    private String doViewGrantOfferLetterSend(Long projectId, Model model, ProjectGrantOfferSendLetterForm form) {
        ProjectGrantOfferSendLetterModel viewModel = populateGrantOfferLetterSendViewModel(projectId);

        model.addAttribute("model", viewModel);
        model.addAttribute("form", form);

        return "project/grant-offer-letter-send";
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @PostMapping(value = "/grant-offer-letter", params = "uploadGrantOfferLetterClicked")
    public String uploadGrantOfferLetterFile(@PathVariable("projectId") final Long projectId,
                                             @ModelAttribute(FORM_ATTR) ProjectGrantOfferSendLetterForm form,
                                             @SuppressWarnings("unused") BindingResult bindingResult,
                                             ValidationHandler validationHandler,
                                             Model model)
    {
        return performActionOrBindErrorsToField(projectId, validationHandler, model, "grantOfferLetter", form, () -> {

            MultipartFile file = form.getGrantOfferLetter();

            return projectGrantOfferService.addGrantOfferLetter(projectId, file.getContentType(), file.getSize(),
                    file.getOriginalFilename(), getMultipartFileBytes(file));
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @PostMapping(value = "/grant-offer-letter", params = "removeGrantOfferLetterClicked")
    public String removeGrantOfferLetterFile(@PathVariable("projectId") final Long projectId,
                                             @ModelAttribute(FORM_ATTR) ProjectGrantOfferSendLetterForm form,
                                             @SuppressWarnings("unused") BindingResult bindingResult,
                                             ValidationHandler validationHandler,
                                             Model model) {

        projectGrantOfferService.removeGrantOfferLetter(projectId);

        return doViewGrantOfferLetterSend(projectId, model, form);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @PostMapping("/signed/{approvalType}")
    public String signedGrantOfferLetterApproval(
            @PathVariable("projectId") final Long projectId,
            @PathVariable("approvalType") final ApprovalType approvalType,
            @ModelAttribute(FORM_ATTR) ProjectGrantOfferSendLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model) {
        projectGrantOfferService.approveOrRejectSignedGrantOfferLetter(projectId, approvalType).toPostResponse();
        return doViewGrantOfferLetterSend(projectId, model, form);
    }

    private String performActionOrBindErrorsToField(Long projectId, ValidationHandler validationHandler, Model model, String fieldName, ProjectGrantOfferSendLetterForm form, Supplier<FailingOrSucceedingResult<?, ?>> actionFn) {

        Supplier<String> successView = () -> redirectToGrantOfferLetterPage(projectId);
        Supplier<String> failureView = () -> doViewGrantOfferLetterSend(projectId, model, form);

        return validationHandler.performActionOrBindErrorsToField(fieldName, failureView, successView, actionFn);
    }

    private String redirectToGrantOfferLetterPage(Long projectId) {
        return "redirect:/project/" + projectId + "/grant-offer-letter/send";
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @GetMapping("/additional-contract")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadAdditionalContractFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectGrantOfferService.getAdditionalContractFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectGrantOfferService.getAdditionalContractFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @GetMapping("/grant-offer-letter")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadGeneratedGrantOfferLetterFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectGrantOfferService.getGrantOfferFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectGrantOfferService.getGrantOfferFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @GetMapping("/signed-grant-offer-letter")
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadSignedGrantOfferLetterFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectGrantOfferService.getSignedGrantOfferLetterFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectGrantOfferService.getSignedGrantOfferLetterFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(content, fileDetails);
    }

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_GRANT_OFFER_LETTER_SEND_SECTION')")
    @PostMapping(params = "uploadAnnexClicked", value = "/upload-annex")
    public String uploadAnnexFile(
            @PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) ProjectGrantOfferSendLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, "annex", form, () -> {

            MultipartFile file = form.getAnnex();

            return projectGrantOfferService.addAdditionalContractFile(projectId, file.getContentType(), file.getSize(),
                    file.getOriginalFilename(), getMultipartFileBytes(file));
        });
    }

    private ProjectGrantOfferSendLetterModel populateGrantOfferLetterSendViewModel(Long projectId) {
        ProjectResource project = projectService.getById(projectId);
        ApplicationResource application = applicationService.getById(project.getApplication());
        CompetitionSummaryResource competitionSummary = applicationSummaryRestService.getCompetitionSummary(application.getCompetition()).getSuccessObjectOrThrowException();

        Optional<FileEntryResource> grantOfferFileDetails = projectGrantOfferService.getGrantOfferFileDetails(projectId);

        Optional<FileEntryResource> additionalContractFile = projectGrantOfferService.getAdditionalContractFileDetails(projectId);

        Optional<FileEntryResource> signedGrantOfferLetterFile = projectGrantOfferService.getSignedGrantOfferLetterFileDetails(projectId);

        GOLState golState = projectGrantOfferService.getGrantOfferLetterWorkflowState(projectId).getSuccessObject();

        return new ProjectGrantOfferSendLetterModel(competitionSummary,
                grantOfferFileDetails.isPresent() ? grantOfferFileDetails.map(FileDetailsViewModel::new).orElse(null) : null,
                additionalContractFile.isPresent() ? additionalContractFile.map(FileDetailsViewModel::new).orElse(null) : null,
                !GOLState.PENDING.equals(golState),
                projectId,
                project.getName(),
                application.getId(),
                grantOfferFileDetails.isPresent() ? grantOfferFileDetails.isPresent() : Boolean.FALSE,
                additionalContractFile.isPresent() ? additionalContractFile.isPresent() : Boolean.FALSE,
                GOLState.APPROVED.equals(golState),
                GOLState.READY_TO_APPROVE.equals(golState) || GOLState.APPROVED.equals(golState),
                signedGrantOfferLetterFile.isPresent() ? signedGrantOfferLetterFile.map(FileDetailsViewModel::new).orElse(null) : null
        );
    }

    private ResponseEntity<ByteArrayResource> returnFileIfFoundOrThrowNotFoundException(Optional<ByteArrayResource> content, Optional<FileEntryResource> fileDetails) {
        if (content.isPresent() && fileDetails.isPresent()) {
            return getFileResponseEntity(content.get(), fileDetails.get());
        } else {
            return new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT);
        }
    }

}
