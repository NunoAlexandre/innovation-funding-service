package com.worth.ifs.project.otherdocuments.controller;

import com.worth.ifs.commons.error.exception.ObjectNotFoundException;
import com.worth.ifs.file.controller.viewmodel.FileDetailsViewModel;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.project.ProjectService;
import com.worth.ifs.project.otherdocuments.form.ProjectPartnerDocumentsForm;
import com.worth.ifs.project.otherdocuments.viewmodel.ProjectPartnerDocumentsViewModel;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.user.resource.OrganisationResource;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.worth.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static com.worth.ifs.user.resource.UserRoleType.PROJECT_MANAGER;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;
import static java.util.Collections.singletonList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller backing the Other Documents page
 */
@Controller
@RequestMapping("/project/{projectId}/partner/documents")
public class ProjectOtherDocumentsController {

    private static final String FORM_ATTR = "form";

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = GET)
    public String viewOtherDocumentsPage(Model model, @ModelAttribute(FORM_ATTR) ProjectPartnerDocumentsForm form,
                                         @PathVariable("projectId") Long projectId,
                                         @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        return doViewOtherDocumentsPage(model, form, projectId, loggedInUser);
    }

    @RequestMapping(method = POST)
    public String acceptOrRejectOtherDocuments(Model model, @ModelAttribute(FORM_ATTR) ProjectPartnerDocumentsForm form,
                                               @PathVariable("projectId") Long projectId,
                                               @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        //TODO Needs further work -  INFUND-4621 INFUND-4620
        return doViewOtherDocumentsPage(model, form, projectId, loggedInUser);
    }

    @RequestMapping(value = "/collaboration-agreement", method = GET)
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadCollaborationAgreementFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectService.getCollaborationAgreementFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectService.getCollaborationAgreementFileDetails(projectId);

        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    @RequestMapping(value = "/exploitation-plan", method = GET)
    public
    @ResponseBody
    ResponseEntity<ByteArrayResource> downloadExploitationPlanFile(
            @PathVariable("projectId") final Long projectId) {

        final Optional<ByteArrayResource> content = projectService.getExploitationPlanFile(projectId);
        final Optional<FileEntryResource> fileDetails = projectService.getExploitationPlanFileDetails(projectId);
        return returnFileIfFoundOrThrowNotFoundException(projectId, content, fileDetails);
    }

    private String doViewOtherDocumentsPage(Model model, ProjectPartnerDocumentsForm form, Long projectId, UserResource loggedInUser) {

        ProjectPartnerDocumentsViewModel viewModel = getOtherDocumentsViewModel(form, projectId, loggedInUser);
        model.addAttribute("model", viewModel);
        return "project/other-documents";
    }

    private ProjectPartnerDocumentsViewModel getOtherDocumentsViewModel(ProjectPartnerDocumentsForm form, Long projectId, UserResource loggedInUser) {

        ProjectResource project = projectService.getById(projectId);
        Optional<FileEntryResource> collaborationAgreement = projectService.getCollaborationAgreementFileDetails(projectId);
        Optional<FileEntryResource> exploitationPlan = projectService.getExploitationPlanFileDetails(projectId);

        String leadPartnerOrganisationName = projectService.getLeadOrganisation(projectId).getName();

        Optional<ProjectUserResource> projectManager = getProjectManagerResource(project);
        String projectManagerName = projectManager.map(ProjectUserResource::getUserName).orElse("");
        String projectManagerTelephone = projectManager.map(ProjectUserResource::getPhoneNumber).orElse("");
        String projectManagerEmail = projectManager.map(ProjectUserResource::getEmail).orElse("");

        List<String> partnerOrganisationNames = projectService.getPartnerOrganisationsForProject(projectId).stream()
                .map(OrganisationResource::getName)
                .filter(s -> s != leadPartnerOrganisationName)
                .collect(Collectors.toList());

        return new ProjectPartnerDocumentsViewModel(projectId, project.getName(), leadPartnerOrganisationName,
                projectManagerName, projectManagerTelephone, projectManagerEmail,
                collaborationAgreement.map(FileDetailsViewModel::new).orElse(null),
                exploitationPlan.map(FileDetailsViewModel::new).orElse(null),
                partnerOrganisationNames, form != null && form.isApproved(),
                form != null && form.isRejected(), form != null ? form.getRejectionReason() : null);
    }

    private ResponseEntity<ByteArrayResource> returnFileIfFoundOrThrowNotFoundException(Long projectId, Optional<ByteArrayResource> content, Optional<FileEntryResource> fileDetails) {
        if (content.isPresent() && fileDetails.isPresent()) {
            return getFileResponseEntity(content.get(), fileDetails.get());
        } else {
            throw new ObjectNotFoundException("Could not find Collaboration Agreement for project " + projectId, singletonList(projectId));
        }
    }

    private Optional<ProjectUserResource> getProjectManagerResource(ProjectResource project) {
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(project.getId());
        Optional<ProjectUserResource> projectManager = simpleFindFirst(projectUsers, pu -> PROJECT_MANAGER.getName().equals(pu.getRoleName()));

        return projectManager;
    }
}
