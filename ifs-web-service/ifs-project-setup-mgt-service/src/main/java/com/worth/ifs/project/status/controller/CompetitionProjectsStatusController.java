package com.worth.ifs.project.status.controller;

import com.worth.ifs.bankdetails.BankDetailsService;
import com.worth.ifs.commons.security.SecuredBySpring;
import com.worth.ifs.project.sections.ProjectSetupSectionInternalUser;
import com.worth.ifs.project.status.ProjectStatusService;
import com.worth.ifs.project.status.resource.CompetitionProjectsStatusResource;
import com.worth.ifs.project.status.viewmodel.CompetitionProjectStatusViewModel;
import com.worth.ifs.user.resource.UserResource;
import com.worth.ifs.user.resource.UserRoleType;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/competition/{competitionId}/status")
public class CompetitionProjectsStatusController {
    @Autowired
    private ProjectStatusService projectStatusService;

    @Autowired
    private BankDetailsService bankDetailsService;

    @RequestMapping(method = GET)
    public String viewCompetitionStatus(
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser,
            @PathVariable Long competitionId) {

        CompetitionProjectStatusViewModel viewModel = populateCompetitionProjectStatusViewModel(competitionId, loggedInUser);

        model.addAttribute("model", viewModel);
        return "project/competition-status";
    }

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "EXPORT_BANK_DETAILS", description = "Project finance users should be able export bank details")
    @RequestMapping(value = "/bank-details/export", method = GET)
    public void exportBankDetails(
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser,
            @PathVariable Long competitionId,
            HttpServletResponse response) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String filename = String.format("Bank_details_%s_%s.csv", competitionId, LocalDateTime.now().format(formatter));
        response.setContentType("text/csv");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Content-Disposition", "attachment;filename="+filename);
        final ByteArrayResource resource = bankDetailsService.downloadByCompetition(competitionId);
        IOUtils.copy(resource.getInputStream(), response.getOutputStream());
        response.flushBuffer();
    }

    private CompetitionProjectStatusViewModel populateCompetitionProjectStatusViewModel(Long competitionId, UserResource userResource) {
        CompetitionProjectsStatusResource competitionProjectsStatusResource = projectStatusService.getCompetitionStatus(competitionId);

        Map<Long, ProjectStatusPermission> projectStatusPermissionMap = new HashMap<>();

        boolean canExportBankDetails = userResource.hasRole(UserRoleType.PROJECT_FINANCE);

        if(null == competitionProjectsStatusResource.getProjectStatusResources()) {
            return new CompetitionProjectStatusViewModel(
                    competitionProjectsStatusResource, canExportBankDetails, projectStatusPermissionMap
            );
        }

        competitionProjectsStatusResource.getProjectStatusResources().forEach(projectStatusResource -> {
            ProjectSetupSectionInternalUser internalUser = new ProjectSetupSectionInternalUser(projectStatusResource);

            ProjectStatusPermission projectStatusPermission = new ProjectStatusPermission(
                    internalUser.canAccessCompaniesHouseSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessProjectDetailsSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessMonitoringOfficerSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessBankDetailsSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessFinanceChecksSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessSpendProfileSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessOtherDocumentsSection(userResource).isAccessibleOrNotRequired(),
                    internalUser.canAccessGrantOfferLetterSection(userResource).isAccessibleOrNotRequired());

            projectStatusPermissionMap.put(projectStatusResource.getApplicationNumber(), projectStatusPermission);
        });

        return new CompetitionProjectStatusViewModel(
                competitionProjectsStatusResource, canExportBankDetails, projectStatusPermissionMap
        );
    }
}
