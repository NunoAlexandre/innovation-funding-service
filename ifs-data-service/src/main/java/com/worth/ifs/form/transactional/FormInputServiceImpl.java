package com.worth.ifs.form.transactional;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.domain.FormInputType;
import com.worth.ifs.form.mapper.FormInputTypeMapper;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.form.repository.FormInputTypeRepository;
import com.worth.ifs.form.resource.FormInputTypeResource;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.ProcessRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.commons.error.CommonErrors.forbiddenError;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.user.domain.UserRoleType.COLLABORATOR;
import static com.worth.ifs.user.domain.UserRoleType.LEADAPPLICANT;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.Arrays.asList;

@Service
public class FormInputServiceImpl extends BaseTransactionalService implements FormInputService {

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private FormInputTypeRepository formInputTypeRepository;

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private FormInputTypeMapper mapper;


    @Override
    public ServiceResult<FormInput> findFormInput(Long id) {
        return find(formInputRepository.findOne(id), notFoundError(FormInput.class, id));
    }

    @Override
    public ServiceResult<FormInputTypeResource> findFormInputType(Long id) {
        return find(formInputTypeRepository.findOne(id), notFoundError(FormInputType.class, id)).
                andOnSuccessReturn(mapper::mapToResource);
    }

    @Override
    public ServiceResult<List<FormInputResponse>> findResponsesByApplication(final Long applicationId) {
        return find(formInputResponseRepository.findByApplicationId(applicationId), notFoundError(FormInputResponse.class, applicationId));
    }

    @Override
    public ServiceResult<FormInputResponse> saveQuestionResponse(Long userId, Long applicationId, Long formInputId, String htmlUnescapedValue) {

        // TODO DW - INFUND-1555 - this should come out and be part of the permissions-checking work
        ProcessRole userAppRole = processRoleRepository.findByUserIdAndApplicationId(userId, applicationId);
        if (userAppRole == null) {
            return serviceFailure(forbiddenError("Unable to update question response"));
        } else if (!asList(COLLABORATOR.getName(), LEADAPPLICANT.getName()).contains(userAppRole.getRole().getName())) {
            return serviceFailure(forbiddenError("Unable to update question response"));
        }

        return find(user(userId), application(applicationId), formInput(formInputId)).
                andOnSuccess((user, application, formInput) -> {

            return getOrCreateResponse(application, formInput, userAppRole).andOnSuccessReturn(response -> {

                if (!response.getValue().equals(htmlUnescapedValue)) {
                    response.setUpdateDate(LocalDateTime.now());
                    response.setUpdatedBy(userAppRole);
                }

                response.setValue(htmlUnescapedValue);
                formInputResponseRepository.save(response);
                return response;
            });
        });
    }

    private ServiceResult<FormInputResponse> getOrCreateResponse(Application application, FormInput formInput, ProcessRole userAppRole) {

        List<FormInputResponse> existingResponse = formInputResponseRepository.findByApplicationIdAndFormInputId(application.getId(), formInput.getId());

        return existingResponse != null && existingResponse.size() > 0 ?
                serviceSuccess(existingResponse.get(0)) :
                serviceSuccess(new FormInputResponse(LocalDateTime.now(), "", userAppRole, formInput, application));
    }

    private Supplier<ServiceResult<FormInput>> formInput(Long id) {
        return () -> findFormInput(id);
    }
}