package org.innovateuk.ifs.project.finance.service;

import org.innovateuk.thread.service.ThreadRestService;
import org.innovateuk.threads.resource.QueryResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectFinanceQueriesRestService extends ThreadRestService<QueryResource> {

    public ProjectFinanceQueriesRestService() {
        super("/project/finance/queries", new ParameterizedTypeReference<List<QueryResource>>(){});
    }

}
