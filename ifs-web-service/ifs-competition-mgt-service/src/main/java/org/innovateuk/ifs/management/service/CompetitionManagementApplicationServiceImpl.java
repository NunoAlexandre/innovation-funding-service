package org.innovateuk.ifs.management.service;

import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationModelPopulator;
import org.innovateuk.ifs.application.resource.AppendixResource;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.AssessorFeedbackRestService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.error.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.controller.viewmodel.OptionalFileDetailsViewModel;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.FormInputResponseResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.innovateuk.ifs.populator.OrganisationDetailsModelPopulator;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.ASSESSOR_FEEDBACK;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.FUNDERS_PANEL;
import static org.innovateuk.ifs.util.MapFunctions.asMap;

/**
 * Implementation of {@link CompetitionManagementApplicationService}
 */
@Service
public class CompetitionManagementApplicationServiceImpl implements CompetitionManagementApplicationService {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationModelPopulator applicationModelPopulator;

    @Autowired
    private OrganisationDetailsModelPopulator organisationDetailsModelPopulator;

    @Autowired
    private FormInputResponseRestService formInputResponseRestService;

    @Autowired
    private FormInputRestService formInputRestService;

    @Autowired
    private FileEntryRestService fileEntryRestService;

    @Autowired
    private AssessorFeedbackRestService assessorFeedbackRestService;

    public enum ApplicationOverviewOrigin {
        ALL_APPLICATIONS("/competition/{competitionId}/applications/all"),
        SUBMITTED_APPLICATIONS("/competition/{competitionId}/applications/submitted"),
        INELIGIBLE_APPLICATIONS("/competition/{competitionId}/applications/ineligible"),
        MANAGE_APPLICATIONS("/assessment/competition/{competitionId}"),
        FUNDING_APPLICATIONS("/competition/{competitionId}/funding"),
        APPLICATION_PROGRESS("/competition/{competitionId}/application/{applicationId}/assessors");

        private String baseOriginUrl;

        ApplicationOverviewOrigin(String baseOriginUrl) {
            this.baseOriginUrl = baseOriginUrl;
        }

        public String getBaseOriginUrl() {
            return baseOriginUrl;
        }
    }

    @Override
    public String displayApplicationOverview(UserResource user, long applicationId, long competitionId, ApplicationForm form, String origin, MultiValueMap<String, String> queryParams, Model model, ApplicationResource application) {
        form.setAdminMode(true);

        List<FormInputResponseResource> responses = formInputResponseRestService.getResponsesByApplicationId(applicationId).getSuccessObjectOrThrowException();

        // so the mode is viewonly
        application.enableViewMode();

        CompetitionResource competition = competitionService.getById(application.getCompetition());
        applicationModelPopulator.addApplicationAndSections(application, competition, user.getId(), Optional.empty(), Optional.empty(), model, form);
        organisationDetailsModelPopulator.populateModel(model, application.getId());

        // Having to pass getImpersonateOrganisationId here because look at the horrible code inside addOrganisationAndUserFinanceDetails with impersonation org id :(
        applicationModelPopulator.addOrganisationAndUserFinanceDetails(competition.getId(), applicationId, user, model, form, form.getImpersonateOrganisationId());
        addAppendices(applicationId, responses, model);

        model.addAttribute("form", form);
        model.addAttribute("applicationReadyForSubmit", false);
        model.addAttribute("isCompManagementDownload", true);

        OptionalFileDetailsViewModel assessorFeedbackViewModel = getAssessorFeedbackViewModel(application, competition);
        model.addAttribute("assessorFeedback", assessorFeedbackViewModel);

        model.addAttribute("backUrl", buildBackUrl(origin, applicationId, competitionId, queryParams));

        return "competition-mgt-application-overview";
    }

    @Override
    public String markApplicationAsIneligible(long applicationId,
                                              long competitionId,
                                              String origin,
                                              MultiValueMap<String, String> queryParams,
                                              ApplicationForm applicationForm,
                                              UserResource user,
                                              Model model) {
        ServiceResult<Void> result = applicationService.markAsIneligible(applicationId, applicationForm.getIneligibleReason());

        if (result != null && result.isSuccess()) {
            return "redirect:/competition/" + competitionId + "/applications/submitted";
        } else {
            return displayApplicationOverview(user,
                    applicationId,
                    competitionId,
                    applicationForm,
                    origin,
                    queryParams,
                    model,
                    applicationService.getById(applicationId));
        }
    }

    @Override
    public String validateApplicationAndCompetitionIds(Long applicationId, Long competitionId, Function<ApplicationResource, String> success) {
        ApplicationResource application = applicationService.getById(applicationId);
        if (application.getCompetition().equals(competitionId)) {
            return success.apply(application);
        } else {
            throw new ObjectNotFoundException();
        }
    }

    private String buildBackUrl(String origin, Long applicationId, Long competitionId, MultiValueMap<String, String> queryParams) {
        String baseUrl = ApplicationOverviewOrigin.valueOf(origin).getBaseOriginUrl();

        queryParams.remove("origin");

        return UriComponentsBuilder.fromPath(baseUrl)
                .queryParams(queryParams)
                .buildAndExpand(asMap(
                        "competitionId", competitionId,
                        "applicationId", applicationId
                ))
                .encode()
                .toUriString();
    }

    private void addAppendices(Long applicationId, List<FormInputResponseResource> responses, Model model) {
        final List<AppendixResource> appendices = responses.stream().filter(fir -> fir.getFileEntry() != null).
                map(fir -> {
                    FormInputResource formInputResource = formInputRestService.getById(fir.getFormInput()).getSuccessObjectOrThrowException();
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

    public enum ApplicationOverviewOrigin {
        ALL_APPLICATIONS("/competition/{competitionId}/applications/all"),
        SUBMITTED_APPLICATIONS("/competition/{competitionId}/applications/submitted"),
        MANAGE_APPLICATIONS("/assessment/competition/{competitionId}"),
        FUNDING_APPLICATIONS("/competition/{competitionId}/funding"),
        APPLICATION_PROGRESS("/competition/{competitionId}/application/{applicationId}/assessors");

        private String baseOriginUrl;

        ApplicationOverviewOrigin(String baseOriginUrl) {
            this.baseOriginUrl = baseOriginUrl;
        }

        public String getBaseOriginUrl() {
            return baseOriginUrl;
        }
    }
}
