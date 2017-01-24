package org.innovateuk.ifs.application.finance.view;

import org.innovateuk.ifs.application.finance.viewmodel.BaseFinanceOverviewViewModel;
import org.springframework.ui.Model;

public interface FinanceOverviewModelManager {
    void addFinanceDetails(Model model, Long competitionId, Long applicationId);
    BaseFinanceOverviewViewModel getFinanceDetailsViewModel(Long competitionId, Long applicationId);
}
