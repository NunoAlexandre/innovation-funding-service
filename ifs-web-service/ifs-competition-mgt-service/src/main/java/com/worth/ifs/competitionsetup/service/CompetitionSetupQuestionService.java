package com.worth.ifs.competitionsetup.service;

import com.worth.ifs.application.resource.CompetitionSetupQuestionResource;
import com.worth.ifs.commons.service.ServiceResult;

/**
 * Service for logic around handling the questions handled in the applicationForm section of competition setup.
 */
public interface CompetitionSetupQuestionService {

	ServiceResult<CompetitionSetupQuestionResource> getQuestion(Long questionId);

	ServiceResult<Void> updateQuestion(CompetitionSetupQuestionResource question);
}
