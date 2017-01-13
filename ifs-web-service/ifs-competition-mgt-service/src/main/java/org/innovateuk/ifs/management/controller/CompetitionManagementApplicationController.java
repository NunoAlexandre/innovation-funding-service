package org.innovateuk.ifs.management.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.BaseController;
import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationModelPopulator;
import org.innovateuk.ifs.application.populator.ApplicationPrintPopulator;
import org.innovateuk.ifs.application.populator.OpenFinanceSectionModelPopulator;
import org.innovateuk.ifs.application.resource.AppendixResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource;
import org.innovateuk.ifs.application.resource.SectionResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.AssessorFeedbackRestService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.application.viewmodel.OpenFinanceSectionViewModel;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.UserAuthenticationService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.file.controller.viewmodel.OptionalFileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.form.service.FormInputService;
import org.innovateuk.ifs.populator.OrganisationDetailsModelPopulator;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.ASSESSOR_FEEDBACK;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.FUNDERS_PANEL;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.toField;
import static org.innovateuk.ifs.controller.FileUploadControllerUtils.getMultipartFileBytes;
import static org.innovateuk.ifs.file.controller.FileDownloadControllerUtils.getFileResponseEntity;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/competition/{competitionId}/application")
public class CompetitionManagementApplicationController extends BaseController {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(CompetitionManagementApplicationController.class);

    @Autowired
    private FormInputResponseService formInputResponseService;

    @Autowired
    private FileEntryRestService fileEntryRestService;

    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;

    @Autowired
    private OpenFinanceSectionModelPopulator openFinanceSectionSectionModelPopulator;

    @Autowired
    private AssessorFeedbackRestService assessorFeedbackRestService;

    @Autowired
    private UserService userService;

    @Autowired
    protected ApplicationModelPopulator applicationModelPopulator;

    @Autowired
    protected CompetitionService competitionService;

    @Autowired
    protected SectionService sectionService;

    @Autowired
    protected ProcessRoleService processRoleService;

    @Autowired
    protected ApplicationPrintPopulator applicationPrintPopulator;

    @Autowired
    protected UserAuthenticationService userAuthenticationService;

    @Autowired
    protected FormInputService formInputService;

    @Autowired
    protected ApplicationService applicationService;

    @RequestMapping(value= "/{applicationId}", method = GET)
    public String displayApplicationForCompetitionAdministrator(@PathVariable("applicationId") final Long applicationId,
                                                                @PathVariable("competitionId") final Long competitionId,
                                                                @ModelAttribute("form") ApplicationForm form,
                                                                Model model,
                                                                HttpServletRequest request
    ){
        return validateApplicationAndCompetitionIds(applicationId, competitionId, (application) -> {
            UserResource user = getLoggedUser(request);
            form.setAdminMode(true);

            List<FormInputResponseResource> responses = formInputResponseService.getByApplication(applicationId);

            // so the mode is viewonly
            application.enableViewMode();

            CompetitionResource competition = competitionService.getById(application.getCompetition());
            applicationModelPopulator.addApplicationAndSections(application, competition, user.getId(), Optional.empty(), Optional.empty(), model, form);
            organisationDetailsModelPopulator.populateModel(model, application.getId());
            applicationModelPopulator.addOrganisationAndUserFinanceDetails(competition.getId(), applicationId, user, model, form);
            addAppendices(applicationId, responses, model);

            model.addAttribute("form", form);
            model.addAttribute("applicationReadyForSubmit", false);
            model.addAttribute("isCompManagementDownload", true);

            OptionalFileDetailsViewModel assessorFeedbackViewModel = getAssessorFeedbackViewModel(application, competition);
            model.addAttribute("assessorFeedback", assessorFeedbackViewModel);

            return "competition-mgt-application-overview";
        });
    }

    @RequestMapping(value = "/{applicationId}/assessorFeedback", method = GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadAssessorFeedbackFile(
            @PathVariable("applicationId") final Long applicationId) {

        final ByteArrayResource resource = assessorFeedbackRestService.getAssessorFeedbackFile(applicationId).getSuccessObjectOrThrowException();
        final FileEntryResource fileDetails = assessorFeedbackRestService.getAssessorFeedbackFileDetails(applicationId).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails);
    }

    @RequestMapping(value = "/{applicationId}", params = "uploadAssessorFeedback", method = POST)
    public String uploadAssessorFeedbackFile(
            @PathVariable("competitionId") final Long competitionId,
            @PathVariable("applicationId") final Long applicationId,
            @ModelAttribute("form") ApplicationForm applicationForm,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            HttpServletRequest request) {

        Supplier<String> failureView = () -> displayApplicationForCompetitionAdministrator(applicationId, competitionId, applicationForm, model, request);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            MultipartFile file = applicationForm.getAssessorFeedback();

            RestResult<FileEntryResource> uploadFileResult = assessorFeedbackRestService.addAssessorFeedbackDocument(applicationId,
                    file.getContentType(), file.getSize(), file.getOriginalFilename(), getMultipartFileBytes(file));

            return validationHandler.
                    addAnyErrors(uploadFileResult, toField("assessorFeedback")).
                    failNowOrSucceedWith(failureView, () -> redirectToApplicationOverview(competitionId, applicationId));
        });
    }

    @RequestMapping(value = "/{applicationId}", params = "removeAssessorFeedback", method = POST)
    public String removeAssessorFeedbackFile(@PathVariable("competitionId") final Long competitionId,
                                             @PathVariable("applicationId") final Long applicationId,
                                             Model model,
                                             @ModelAttribute("form") ApplicationForm applicationForm,
                                             @SuppressWarnings("unused") BindingResult bindingResult,
                                             ValidationHandler validationHandler,
                                             HttpServletRequest request) {

        Supplier<String> failureView = () -> displayApplicationForCompetitionAdministrator(applicationId, competitionId, applicationForm, model, request);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            RestResult<Void> removeFileResult = assessorFeedbackRestService.removeAssessorFeedbackDocument(applicationId);

            return validationHandler.
                    addAnyErrors(removeFileResult, toField("assessorFeedback")).
                    failNowOrSucceedWith(failureView, () -> redirectToApplicationOverview(competitionId, applicationId));
        });
    }

    @RequestMapping(value = "/{applicationId}/finances/{organisationId}", method = RequestMethod.GET)
    public String displayApplicationForCompetitionAdministrator(@PathVariable("applicationId") final Long applicationId,
                                                                @PathVariable("competitionId") final Long competitionId,
                                                                @PathVariable("organisationId") final Long organisationId,
                                                                @ModelAttribute("form") ApplicationForm form,
                                                                Model model,
                                                                BindingResult bindingResult
    ) throws ExecutionException, InterruptedException {

        return validateApplicationAndCompetitionIds(applicationId, competitionId, (application) -> {
            SectionResource financeSection = sectionService.getFinanceSection(application.getCompetition());
            List<SectionResource> allSections = sectionService.getAllByCompetitionId(application.getCompetition());
            List<FormInputResponseResource> responses = formInputResponseService.getByApplication(applicationId);
            UserResource impersonatingUser;
            try {
                impersonatingUser = getImpersonateUserByOrganisationId(organisationId, form, applicationId);
            } catch(ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            // so the mode is viewonly
            form.setAdminMode(true);
            application.enableViewMode();
            model.addAttribute("responses", formInputResponseService.mapFormInputResponsesToFormInput(responses));
            model.addAttribute("applicationReadyForSubmit", false);

            //TODO - INFUND-7498 - ViewModel is changed so template should be changed as well
            OpenFinanceSectionViewModel openFinanceSectionViewModel = (OpenFinanceSectionViewModel) openFinanceSectionSectionModelPopulator.populateModel(form, model, application, financeSection, impersonatingUser, bindingResult, allSections, organisationId, false, false);
            model.addAttribute("model", openFinanceSectionViewModel);

            return "comp-mgt-application-finances";
        });
    }

    private UserResource getImpersonateUserByOrganisationId(@PathVariable("organisationId") Long organisationId, @ModelAttribute("form") ApplicationForm form, Long applicationId) throws InterruptedException, ExecutionException {
        UserResource user;
        form.setImpersonateOrganisationId(Long.valueOf(organisationId));
        List<ProcessRoleResource> processRoles = processRoleService.findProcessRolesByApplicationId(applicationId);
        Optional<Long> userId = processRoles.stream()
                .filter(p -> p.getOrganisation().equals(Long.valueOf(organisationId)))
                .map(p -> p.getUser())
                .findAny();

        if (!userId.isPresent()) {
            LOG.error("Found no user to impersonate.");
            return null;
        }
        user = userService.retrieveUserById(userId.get()).getSuccessObject();
        return user;
    }

    @RequestMapping(value = "/{applicationId}/forminput/{formInputId}/download", method = GET)
    public @ResponseBody ResponseEntity<ByteArrayResource> downloadQuestionFile(
            @PathVariable("applicationId") final Long applicationId,
            @PathVariable("formInputId") final Long formInputId,
            HttpServletRequest request) throws ExecutionException, InterruptedException {
        final UserResource user = userAuthenticationService.getAuthenticatedUser(request);
        ProcessRoleResource processRole;
        if(user.hasRole(UserRoleType.COMP_ADMIN)){
            long processRoleId = formInputResponseService.getByFormInputIdAndApplication(formInputId, applicationId).getSuccessObjectOrThrowException().get(0).getUpdatedBy();
            processRole = processRoleService.getById(processRoleId).get();
        } else {
            processRole = processRoleService.findProcessRole(user.getId(), applicationId);
        }

        final ByteArrayResource resource = formInputResponseService.getFile(formInputId, applicationId, processRole.getId()).getSuccessObjectOrThrowException();
        final FormInputResponseFileEntryResource fileDetails = formInputResponseService.getFileDetails(formInputId, applicationId, processRole.getId()).getSuccessObjectOrThrowException();
        return getFileResponseEntity(resource, fileDetails.getFileEntryResource());
    }


    /**
     * Printable version of the application
     */
    @RequestMapping(value="/{applicationId}/print")
    public String printManagementApplication(@PathVariable("applicationId") Long applicationId,
                                             @PathVariable("competitionId") Long competitionId,
                                             Model model, HttpServletRequest request) {

        return validateApplicationAndCompetitionIds(applicationId, competitionId, (application) -> applicationPrintPopulator.print(applicationId, model, request));
    }

    private void addAppendices(Long applicationId, List<FormInputResponseResource> responses, Model model) {
        final List<AppendixResource> appendices = responses.stream().filter(fir -> fir.getFileEntry() != null).
                map(fir -> {
                    FormInputResource formInputResource = formInputService.getOne(fir.getFormInput());
                    FileEntryResource fileEntryResource = fileEntryRestService.findOne(fir.getFileEntry()).getSuccessObject();
                    String title = formInputResource.getDescription() != null ? formInputResource.getDescription() : fileEntryResource.getName();
                    return new AppendixResource(applicationId, formInputResource.getId(), title, fileEntryResource);
                }).
                collect(Collectors.toList());
        model.addAttribute("appendices", appendices);
    }

    private OptionalFileDetailsViewModel getAssessorFeedbackViewModel(ApplicationResource application, CompetitionResource competition) {

        boolean readonly = !asList(FUNDERS_PANEL, ASSESSOR_FEEDBACK).contains(competition.getCompetitionStatus());

        Long assessorFeedbackFileEntry = application.getAssessorFeedbackFileEntry();

        if (assessorFeedbackFileEntry != null) {
            RestResult<FileEntryResource> fileEntry = assessorFeedbackRestService.getAssessorFeedbackFileDetails(application.getId());
            return OptionalFileDetailsViewModel.withExistingFile(fileEntry.getSuccessObjectOrThrowException(), readonly);
        } else {
            return OptionalFileDetailsViewModel.withNoFile(readonly);
        }
    }

    private String redirectToApplicationOverview(Long competitionId, Long applicationId) {
        return "redirect:/competition/" + competitionId + "/application/" + applicationId;
    }


    private String validateApplicationAndCompetitionIds(Long applicationId, Long competitionId, Function<ApplicationResource, String> success) {
        ApplicationResource application = applicationService.getById(applicationId);
        if (application.getCompetition().equals(competitionId)) {
            return success.apply(application);
        } else {
            throw new ObjectNotFoundException();
        }
    }
}
