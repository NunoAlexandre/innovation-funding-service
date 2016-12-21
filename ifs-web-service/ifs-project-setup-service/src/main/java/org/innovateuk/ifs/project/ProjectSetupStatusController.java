package org.innovateuk.ifs.project;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.project.constant.ProjectActivityStates;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.sections.ProjectSetupSectionPartnerAccessor;
import org.innovateuk.ifs.project.sections.ProjectSetupSectionStatus;
import org.innovateuk.ifs.project.sections.SectionAccess;
import org.innovateuk.ifs.project.sections.SectionStatus;
import org.innovateuk.ifs.project.viewmodel.ProjectSetupStatusViewModel;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.project.constant.ProjectActivityStates.*;
import static org.innovateuk.ifs.project.sections.SectionAccess.ACCESSIBLE;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;
import static java.util.Arrays.asList;

/**
 * This controller will handle all requests that are related to a project.
 */
@Controller
@RequestMapping("/project")
public class ProjectSetupStatusController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompetitionService competitionService;

    @RequestMapping(value = "/{projectId}", method = RequestMethod.GET)
    public String viewProjectSetupStatus(Model model, @PathVariable("projectId") final Long projectId,
                                         @ModelAttribute("loggedInUser") UserResource loggedInUser,
                                         NativeWebRequest springRequest) {

        HttpServletRequest request = springRequest.getNativeRequest(HttpServletRequest.class);
        String dashboardUrl = request.getScheme() + "://" +
            request.getServerName() +
            ":" + request.getServerPort() +
            "/applicant/dashboard";


        model.addAttribute("model", getProjectSetupStatusViewModel(projectId, loggedInUser));
        model.addAttribute("url", dashboardUrl);
        return "project/setup-status";
    }

    private ProjectSetupStatusViewModel getProjectSetupStatusViewModel(Long projectId, UserResource loggedInUser) {

        ProjectResource project = projectService.getById(projectId);
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectId);

        ApplicationResource applicationResource = applicationService.getById(project.getApplication());
        CompetitionResource competition = competitionService.getById(applicationResource.getCompetition());

        Optional<MonitoringOfficerResource> monitoringOfficer = projectService.getMonitoringOfficerForProject(projectId);

        OrganisationResource organisation = projectService.getOrganisationByProjectAndUser(projectId, loggedInUser.getId());

        ProjectTeamStatusResource teamStatus = projectService.getProjectTeamStatus(projectId, Optional.empty());
        ProjectPartnerStatusResource ownOrganisation = teamStatus.getPartnerStatusForOrganisation(organisation.getId()).get();

        ProjectActivityStates spendProfileState = ownOrganisation.getSpendProfileStatus();
        ProjectActivityStates grantOfferLetterState = teamStatus.getLeadPartnerStatus().getGrantOfferLetterStatus();

        ProjectSetupSectionPartnerAccessor statusAccessor = new ProjectSetupSectionPartnerAccessor(teamStatus);
        ProjectSetupSectionStatus sectionStatus = new ProjectSetupSectionStatus();
        boolean allFinanceChecksApproved = checkAllFinanceChecksApproved(teamStatus);
        //boolean allBankDetailsApprovedOrNotRequired = checkAllBankDetailsApprovedOrNotRequired(teamStatus);

        ProjectUserResource loggedInUserPartner = simpleFindFirst(projectUsers, pu ->
                pu.getUser().equals(loggedInUser.getId()) &&
                pu.getRoleName().equals(UserRoleType.PARTNER.getName())).get();

        boolean leadPartner = teamStatus.getLeadPartnerStatus().getOrganisationId().equals(loggedInUserPartner.getOrganisation());

        boolean projectDetailsSubmitted = COMPLETE.equals(teamStatus.getLeadPartnerStatus().getProjectDetailsStatus());

        boolean projectDetailsProcessCompleted;
        boolean awaitingProjectDetailsActionFromOtherPartners = false;
        if (leadPartner) {
            projectDetailsProcessCompleted = checkLeadPartnerProjectDetailsProcessCompleted(teamStatus);
            awaitingProjectDetailsActionFromOtherPartners = awaitingProjectDetailsActionFromOtherPartners(teamStatus);

        } else {
            projectDetailsProcessCompleted = statusAccessor.isFinanceContactSubmitted(organisation);
        }

        ProjectActivityStates bankDetailsState = ownOrganisation.getBankDetailsStatus();

        SectionAccess companiesHouseAccess = statusAccessor.canAccessCompaniesHouseSection(organisation);
        SectionAccess projectDetailsAccess = statusAccessor.canAccessProjectDetailsSection(organisation);
        SectionAccess monitoringOfficerAccess = statusAccessor.canAccessMonitoringOfficerSection(organisation);
        SectionAccess bankDetailsAccess = statusAccessor.canAccessBankDetailsSection(organisation);
        SectionAccess financeChecksAccess = statusAccessor.canAccessFinanceChecksSection(organisation);
        SectionAccess spendProfileAccess = statusAccessor.canAccessSpendProfileSection(organisation);
        SectionAccess otherDocumentsAccess = statusAccessor.canAccessOtherDocumentsSection(organisation);
        SectionAccess grantOfferAccess = statusAccessor.canAccessGrantOfferLetterSection(organisation);

        SectionStatus projectDetailsStatus = sectionStatus.projectDetailsSectionStatus(projectDetailsProcessCompleted, awaitingProjectDetailsActionFromOtherPartners, leadPartner);
        SectionStatus monitoringOfficerStatus = sectionStatus.monitoringOfficerSectionStatus(monitoringOfficer.isPresent(), projectDetailsSubmitted);
        SectionStatus bankDetailsStatus = sectionStatus.bankDetailsSectionStatus(bankDetailsState);
        SectionStatus financeChecksStatus = sectionStatus.financeChecksSectionStatus(bankDetailsState, allFinanceChecksApproved);
        SectionStatus spendProfileStatus= sectionStatus.spendProfileSectionStatus(spendProfileState);
        SectionStatus otherDocumentsStatus = sectionStatus.otherDocumentsSectionStatus(project, leadPartner);
        SectionStatus grantOfferStatus = sectionStatus.grantOfferLetterSectionStatus(grantOfferLetterState, leadPartner);

        return new ProjectSetupStatusViewModel(project, competition, monitoringOfficer, organisation.getId(), leadPartner,
                companiesHouseAccess, projectDetailsAccess, monitoringOfficerAccess, bankDetailsAccess, financeChecksAccess, spendProfileAccess, otherDocumentsAccess, grantOfferAccess,
                projectDetailsStatus, monitoringOfficerStatus, bankDetailsStatus, financeChecksStatus, spendProfileStatus, otherDocumentsStatus, grantOfferStatus);
    }

    private boolean checkAllFinanceChecksApproved(ProjectTeamStatusResource teamStatus) {
        return teamStatus.checkForAllPartners(status -> COMPLETE.equals(status.getFinanceChecksStatus()));
    }

    private boolean checkAllBankDetailsApprovedOrNotRequired(ProjectTeamStatusResource teamStatus) {
        return teamStatus.checkForAllPartners(status ->
                asList(NOT_REQUIRED, COMPLETE).contains(status.getBankDetailsStatus()));
    }

    private boolean checkLeadPartnerProjectDetailsProcessCompleted(ProjectTeamStatusResource teamStatus) {

        ProjectPartnerStatusResource leadPartnerStatus = teamStatus.getLeadPartnerStatus();

        return COMPLETE.equals(leadPartnerStatus.getProjectDetailsStatus())
                && COMPLETE.equals(leadPartnerStatus.getFinanceContactStatus())
                && allOtherPartnersFinanceContactStatusComplete(teamStatus);
    }

    private boolean awaitingProjectDetailsActionFromOtherPartners(ProjectTeamStatusResource teamStatus) {

        ProjectPartnerStatusResource leadPartnerStatus = teamStatus.getLeadPartnerStatus();

        return COMPLETE.equals(leadPartnerStatus.getProjectDetailsStatus())
                && COMPLETE.equals(leadPartnerStatus.getFinanceContactStatus())
                && !allOtherPartnersFinanceContactStatusComplete(teamStatus);
    }

    private boolean allOtherPartnersFinanceContactStatusComplete(ProjectTeamStatusResource teamStatus) {
        return teamStatus.checkForOtherPartners(status -> COMPLETE.equals(status.getFinanceContactStatus()));
    }
}
