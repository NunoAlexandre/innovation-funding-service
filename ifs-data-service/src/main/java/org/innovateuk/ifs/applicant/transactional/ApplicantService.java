package org.innovateuk.ifs.applicant.transactional;

import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.service.ServiceResult;

/**
 * Service interface for retrieving rich resources for application sections and questions.
 */
public interface ApplicantService {

    @NotSecured("Service should only be calling other services to receive data and should be using their permission rules.")
    ServiceResult<ApplicantQuestionResource> getQuestion(Long userId, Long questionId, Long applicationId);

    @NotSecured("Service should only be calling other services to receive data and should be using their permission rules.")
    ServiceResult<ApplicantSectionResource> getSection(Long userId, Long sectionId, Long applicationId);
}
