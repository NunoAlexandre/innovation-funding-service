package org.innovateuk.ifs.project.monitoringofficer.controller;

import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.monitoringofficer.viewmodel.ProjectMonitoringOfficerViewModel;
import org.innovateuk.ifs.project.resource.MonitoringOfficerResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Controller for the Partners' assigned Monitoring Officer page
 */
@Controller
@RequestMapping("/project/{projectId}/monitoring-officer")
public class ProjectMonitoringOfficerController {

    @Autowired
    private ProjectService projectService;

    @PreAuthorize("hasPermission(#projectId, 'ACCESS_MONITORING_OFFICER_SECTION')")
    @RequestMapping(method = GET)
    public String viewMonitoringOfficer(@PathVariable("projectId") Long projectId, Model model) {

        ProjectMonitoringOfficerViewModel viewModel = getMonitoringOfficerViewModel(projectId);
        model.addAttribute("model", viewModel);
        return "project/monitoring-officer";
    }

    private ProjectMonitoringOfficerViewModel getMonitoringOfficerViewModel(Long projectId) {
        ProjectResource project = projectService.getById(projectId);
        Optional<MonitoringOfficerResource> monitoringOfficer = projectService.getMonitoringOfficerForProject(projectId);
        return new ProjectMonitoringOfficerViewModel(project, monitoringOfficer);
    }
}
