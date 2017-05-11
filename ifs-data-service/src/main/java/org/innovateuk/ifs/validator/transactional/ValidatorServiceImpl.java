package org.innovateuk.ifs.validator.transactional;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.Question;
import org.innovateuk.ifs.commons.rest.ValidationMessages;
import org.innovateuk.ifs.finance.handler.item.FinanceRowHandler;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.transactional.FinanceRowService;
import org.innovateuk.ifs.finance.transactional.ProjectFinanceRowService;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.repository.FormInputRepository;
import org.innovateuk.ifs.form.repository.FormInputResponseRepository;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.innovateuk.ifs.validator.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to validate several objects
 * TODO: INFUND-9548 adding unit tests for this class
 */
@Service
public class ValidatorServiceImpl extends BaseTransactionalService implements ValidatorService {

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;
    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private FinanceRowService financeRowService;

    @Autowired
    private ProjectFinanceRowService projectFinanceRowService;

    @Autowired
    private ValidationUtil validationUtil;

    @Override
    public List<BindingResult> validateFormInputResponse(Long applicationId, Long formInputId) {
        List<BindingResult> results = new ArrayList<>();
        List<FormInputResponse> responses = formInputResponseRepository.findByApplicationIdAndFormInputId(applicationId, formInputId);
        if (!responses.isEmpty()) {
            results.addAll(responses.stream().map(formInputResponse -> validationUtil.validateResponse(formInputResponse, false)).collect(Collectors.toList()));
        } else {
            FormInputResponse emptyResponse = new FormInputResponse();
            emptyResponse.setFormInput(formInputRepository.findOne(formInputId));
            results.add(validationUtil.validateResponse(emptyResponse, false));
        }

        FormInput formInput = formInputRepository.findOne(formInputId);
        if (formInput.getType().equals(FormInputType.APPLICATION_DETAILS)) {
            Application application = applicationRepository.findOne(applicationId);
            results.add(validationUtil.validationApplicationDetails(application));
        }

        return results;
    }

    @Override
    public BindingResult validateFormInputResponse(Long applicationId, Long formInputId, Long markedAsCompleteById) {
        FormInputResponse response = formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(applicationId, markedAsCompleteById, formInputId);
        BindingResult result = validationUtil.validateResponse(response, false);

        validateFileUploads(formInputId, response).forEach(objectError -> result.addError(objectError));

        return result;
    }


    @Override
    public List<ValidationMessages> validateCostItem(Long applicationId, Question question, Long markedAsCompleteById) {
        return getProcessRole(markedAsCompleteById).andOnSuccess(role ->
                financeRowService.financeDetails(applicationId, role.getOrganisationId()).andOnSuccess(financeDetails ->
                        financeRowService.getCostItems(financeDetails.getId(), question.getId()).andOnSuccessReturn(costItems ->
                                validationUtil.validateCostItem(costItems, question)
                        )
                )
        ).getSuccessObject();
    }

    @Override
    public FinanceRowHandler getCostHandler(FinanceRowItem costItem) {
        return financeRowService.getCostHandler(costItem.getId());
    }

    @Override
    public FinanceRowHandler getProjectCostHandler(FinanceRowItem costItem) {
        return projectFinanceRowService.getCostHandler(costItem);
    }

    private List<ObjectError> validateFileUploads(Long formInputId, FormInputResponse response) {
        List<ObjectError> errors = new ArrayList<>();
        FormInput formInput = formInputRepository.findOne(formInputId);

        if(FormInputType.FINANCE_UPLOAD == formInput.getType()) {
            if (response == null) {
                errors.add(new ObjectError("value", "validation.field.must.not.be.blank"));
            } else {
                errors.addAll(validationUtil.validationJesForm(response).getAllErrors());
            }
        }

        return errors;
    }
}
