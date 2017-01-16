package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.application.resource.SectionType;
import org.innovateuk.ifs.application.transactional.sectionupdater.ApplicationFinanceUpdater;

import java.util.Optional;

/**
 * Service for getting the right updaters, and future populators
 */
public interface ApplicationFinanceService {
    Optional<ApplicationFinanceUpdater> getApplicationFinanceSaver(SectionType sectionType);
}
