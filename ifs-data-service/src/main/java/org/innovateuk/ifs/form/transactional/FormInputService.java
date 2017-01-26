package org.innovateuk.ifs.form.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.resource.*;
import org.innovateuk.ifs.commons.security.NotSecured;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface FormInputService {

    @NotSecured(value = "Anyone can see a form input", mustBeSecuredByOtherServices = false)
    ServiceResult<FormInputResource> findFormInput(Long id);

    @NotSecured(value = "Anyone can see a form input", mustBeSecuredByOtherServices = false)
    ServiceResult<List<FormInputResource>> findByQuestionId(Long questionId);

    @NotSecured(value = "Anyone can see a form input", mustBeSecuredByOtherServices = false)
    ServiceResult<List<FormInputResource>> findByQuestionIdAndScope(Long questionId, FormInputScope scope);

    @NotSecured(value = "Anyone can see a form input", mustBeSecuredByOtherServices = false)
    ServiceResult<List<FormInputResource>> findByCompetitionId(Long competitionId);

    @NotSecured(value = "Anyone can see a form input", mustBeSecuredByOtherServices = false)
    ServiceResult<List<FormInputResource>> findByCompetitionIdAndScope(Long competitionId, FormInputScope scope);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<FormInputResponseResource>> findResponsesByApplication(Long applicationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<List<FormInputResponseResource>> findResponsesByFormInputIdAndApplicationId(Long formInputId, Long applicationId);

    // TODO we need to have separate methods for save and update
    @PreAuthorize("hasPermission(#formInputResponseCommand, 'SAVE')")
    ServiceResult<FormInputResponse> saveQuestionResponse(@P("formInputResponseCommand")FormInputResponseCommand formInputResponseCommand);

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    ServiceResult<FormInputResource> save(FormInputResource formInputResource);

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('comp_admin') || hasAuthority('project_finance')")
    ServiceResult<Void> delete(Long id);
}
