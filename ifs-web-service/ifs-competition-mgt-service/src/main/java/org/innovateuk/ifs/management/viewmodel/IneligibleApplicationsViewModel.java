package org.innovateuk.ifs.management.viewmodel;

import java.util.List;
import java.util.Optional;

/**
 * View model for the Ineligible Competition Management Applications page
 */
public class IneligibleApplicationsViewModel extends BaseApplicationsViewModel<IneligibleApplicationsRowViewModel> {

    public IneligibleApplicationsViewModel(long competitionId,
                                           String competitionName,
                                           String sorting,
                                           String filter,
                                           List<IneligibleApplicationsRowViewModel> applications,
                                           PaginationViewModel pagination) {
        super(competitionId, competitionName, applications, pagination, sorting, filter);
    }
}
