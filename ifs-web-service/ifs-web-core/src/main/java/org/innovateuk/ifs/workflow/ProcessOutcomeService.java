package org.innovateuk.ifs.workflow;

import org.innovateuk.ifs.workflow.resource.ProcessOutcomeResource;

/**
 * Interface for CRUD operations on {@link ProcessOutcomeResource} related data.
 */
public interface ProcessOutcomeService {

    ProcessOutcomeResource getById(Long id);

    ProcessOutcomeResource getByProcessId(Long processId);

    ProcessOutcomeResource getByProcessIdAndOutcomeType(Long processId, String outcomeType);
}
