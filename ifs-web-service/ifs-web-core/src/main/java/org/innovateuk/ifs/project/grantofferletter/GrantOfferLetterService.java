package org.innovateuk.ifs.project.grantofferletter;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.gol.resource.GOLState;
import org.innovateuk.ifs.project.resource.ApprovalType;

/**
 * Created by arif on 20/04/2017.
 */
public interface GrantOfferLetterService {

    ServiceResult<Boolean> isSendGrantOfferLetterAllowed(Long projectId);

    ServiceResult<Void> sendGrantOfferLetter(Long projectId);

    ServiceResult<Boolean> isGrantOfferLetterAlreadySent(Long projectId);

    ServiceResult<Void> approveOrRejectSignedGrantOfferLetter(Long projectId, ApprovalType approvalType);

    ServiceResult<Boolean> isSignedGrantOfferLetterApproved(Long projectId);

    ServiceResult<GOLState> getGrantOfferLetterWorkflowState(Long projectId);
}
