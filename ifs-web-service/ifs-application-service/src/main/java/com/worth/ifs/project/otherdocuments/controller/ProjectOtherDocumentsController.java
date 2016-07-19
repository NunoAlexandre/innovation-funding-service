package com.worth.ifs.project.otherdocuments.controller;

import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.commons.service.FailingOrSucceedingResult;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.file.controller.viewmodel.FileDetailsViewModel;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.project.ProjectService;
import com.worth.ifs.project.otherdocuments.form.ProjectOtherDocumentsForm;
import com.worth.ifs.project.otherdocuments.viewmodel.ProjectOtherDocumentsViewModel;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.worth.ifs.controller.FileUploadControllerUtils.getMultipartFileBytes;
import static com.worth.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller backing the Other Documents page
 */
@Controller
@RequestMapping("/project/{projectId}/other-documents")
public class ProjectOtherDocumentsController {

    private static final String FORM_ATTR = "form";

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = GET)
    public String viewOtherDocumentsPage(@PathVariable("projectId") Long projectId, Model model,
                                         @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        ProjectOtherDocumentsForm form = new ProjectOtherDocumentsForm();
        return doViewOtherDocumentsPage(projectId, model, loggedInUser, form);
    }

    private String doViewOtherDocumentsPage(@PathVariable("projectId") Long projectId, Model model, @ModelAttribute("loggedInUser") UserResource loggedInUser, ProjectOtherDocumentsForm form) {
        ProjectOtherDocumentsViewModel viewModel = getOtherDocumentsViewModel(projectId, loggedInUser);
        model.addAttribute("model", viewModel);
        model.addAttribute("form", form);
        return "project/other-documents";
    }

    @RequestMapping(value = "/collaboration-agreement", method = GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadCollaborationAgreementFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectService.getCollaborationAgreementFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectService.getCollaborationAgreementFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @RequestMapping(params = "uploadCollaborationAgreementClicked", method = POST)
    public String uploadCollaborationAgreementFile(
            @PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) ProjectOtherDocumentsForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "collaborationAgreement", form, () -> {

            MultipartFile file = form.getCollaborationAgreement();

            return projectService.addCollaborationAgreementDocument(projectId, file.getContentType(), file.getSize(),
                    file.getOriginalFilename(), getMultipartFileBytes(file));
        });
    }

    @RequestMapping(params = "removeCollaborationAgreementClicked", method = POST)
    public String removeCollaborationAgreementFile(@PathVariable("projectId") final Long projectId,
                                             @ModelAttribute(FORM_ATTR) ProjectOtherDocumentsForm form,
                                             @SuppressWarnings("unused") BindingResult bindingResult,
                                             ValidationHandler validationHandler,
                                             Model model,
                                             @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "collaborationAgreement", form,
                () -> projectService.removeCollaborationAgreementDocument(projectId));
    }

    @RequestMapping(value = "/exploitation-plan", method = GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadExploitationPlanFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectService.getExploitationPlanFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectService.getExploitationPlanFileDetails(projectId);
        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @RequestMapping(params = "uploadExploitationPlanClicked", method = POST)
    public String uploadExploitationPlanFile(
            @PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) ProjectOtherDocumentsForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "exploitationPlan", form, () -> {

            MultipartFile file = form.getExploitationPlan();

            return projectService.addExploitationPlanDocument(projectId, file.getContentType(), file.getSize(),
                    file.getOriginalFilename(), getMultipartFileBytes(file));
        });
    }

    @RequestMapping(params = "removeExploitationPlanClicked", method = POST)
    public String removeExploitationPlanFile(@PathVariable("projectId") final Long projectId,
                                             @ModelAttribute(FORM_ATTR) ProjectOtherDocumentsForm form,
                                             @SuppressWarnings("unused") BindingResult bindingResult,
                                             ValidationHandler validationHandler,
                                             Model model,
                                             @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "exploitationPlan", form,
                () -> projectService.removeExploitationPlanDocument(projectId));
    }

    private String performActionOrBindErrorsToField(Long projectId, ValidationHandler validationHandler, Model model, UserResource loggedInUser, String fieldName, ProjectOtherDocumentsForm form, Supplier<FailingOrSucceedingResult<?, ?>> actionFn) {

        Supplier<String> successView = () -> redirectToOtherDocumentsPage(projectId);
        Supplier<String> failureView = () -> doViewOtherDocumentsPage(projectId, model, loggedInUser, form);

        return validationHandler.performActionOrBindErrorsToField(fieldName, failureView, successView, actionFn);
    }

    private ProjectOtherDocumentsViewModel getOtherDocumentsViewModel(Long projectId, UserResource loggedInUser) {

        ProjectResource project = projectService.getById(projectId);
        Optional<FileEntryResource> collaborationAgreement = projectService.getCollaborationAgreementFileDetails(projectId);
        Optional<FileEntryResource> exploitationPlan = projectService.getExploitationPlanFileDetails(projectId);
        List<OrganisationResource> partnerOrganisations = projectService.getPartnerOrganisationsForProject(projectId);

        List<String> partnerOrganisationNames = simpleMap(partnerOrganisations, OrganisationResource::getName);

        boolean leadPartner = projectService.isUserLeadPartner(projectId, loggedInUser.getId());

        // TODO DW - these rejection messages to be covered in other stories
        List<String> rejectionReasons = emptyList();

        // TODO DW - these flags to be covered in other stories
        boolean otherDocumentsSubmitted = false;
        boolean otherDocumentsApproved = false;

        return new ProjectOtherDocumentsViewModel(projectId, project.getName(),
                collaborationAgreement.map(FileDetailsViewModel::new).orElse(null),
                exploitationPlan.map(FileDetailsViewModel::new).orElse(null),
                partnerOrganisationNames, rejectionReasons,
                leadPartner, otherDocumentsSubmitted, otherDocumentsApproved
        );
    }

    private ResponseEntity<ByteArrayResource> returnFileIfFoundOrThrowNotFoundException(@PathVariable("projectId") Long projectId, Optional<ByteArrayResource> content, Optional<FileEntryResource> fileDetails) {
        if (content.isPresent() && fileDetails.isPresent()) {
            return getFileResponseEntity(content.get(), fileDetails.get());
        } else {
            throw new ObjectNotFoundException("Could not find Collaboration Agreement for project " + projectId, singletonList(projectId));
        }
    }

    private String redirectToOtherDocumentsPage(Long projectId) {
        return "redirect:/project/" + projectId + "/other-documents";
    }
}
