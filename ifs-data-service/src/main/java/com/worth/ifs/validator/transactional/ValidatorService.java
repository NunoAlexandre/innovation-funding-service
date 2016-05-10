package com.worth.ifs.validator.transactional;

import com.worth.ifs.application.domain.Question;
import com.worth.ifs.commons.rest.ValidationMessages;
import com.worth.ifs.security.NotSecured;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface ValidatorService {
    // @NotSecured(value = "This service is used to validate existing data")
    @NotSecured(value = "TODO - what does this mean", mustBeSecuredByOtherServices = false)
    List<BindingResult> validateFormInputResponse(Long applicationId, Long formInputId);

    // @NotSecured(value = "This service is used to validate existing data")
    @NotSecured(value = "TODO - what does this mean", mustBeSecuredByOtherServices = false)
    BindingResult validateFormInputResponse(Long applicationId, Long formInputId, Long markedAsCompleteById);

    // @NotSecured("This service is used to validate existing data")
    @NotSecured(value = "TODO - what does this mean", mustBeSecuredByOtherServices = false)
    List<ValidationMessages> validateCostItem(Long applicationId, Question question, Long markedAsCompleteById);
}
