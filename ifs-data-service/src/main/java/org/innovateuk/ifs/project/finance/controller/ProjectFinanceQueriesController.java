package org.innovateuk.ifs.project.finance.controller;

import org.innovateuk.ifs.project.finance.service.ProjectFinanceQueriesService;
import org.innovateuk.ifs.threads.controller.CommonThreadController;
import org.innovateuk.threads.resource.QueryResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project/finance/queries")
public class ProjectFinanceQueriesController extends CommonThreadController<QueryResource> {
    @Autowired
    public ProjectFinanceQueriesController(ProjectFinanceQueriesService service) {
        super(service);
    }
}