package com.worth.ifs.application.transactional;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.FormInputResponseFileEntryId;
import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.security.NotSecured;
import com.worth.ifs.user.domain.UserRoleType;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Transactional and secure service for Application processing work
 */
public interface ApplicationService {

    @PreAuthorize("hasAuthority('applicant')")
    ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(String applicationName, final Long competitionId, final Long userId);

    @PreAuthorize("hasPermission(#fileEntry, 'UPDATE')")
    ServiceResult<Pair<File, FormInputResponseFileEntryResource>> createFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryResource fileEntry, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#fileEntry, 'UPDATE')")
    ServiceResult<Pair<File, FormInputResponseFileEntryResource>> updateFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryResource fileEntry, Supplier<InputStream> inputStreamSupplier);

    @PreAuthorize("hasPermission(#fileEntry, 'com.worth.ifs.application.resource.FormInputResponseFileEntryResource', 'UPDATE')")
    ServiceResult<FormInputResponse> deleteFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryId fileEntryId);

    @PreAuthorize("hasPermission(#fileEntry, 'com.worth.ifs.application.resource.FormInputResponseFileEntryResource', 'READ')")
    ServiceResult<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> getFormInputResponseFileUpload(@P("fileEntry") FormInputResponseFileEntryId fileEntryId);

    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<ApplicationResource> getApplicationById(@P("applicationId") final Long applicationId);

    @PostFilter("hasPermission(#application, 'READ')")
    ServiceResult<List<ApplicationResource>> findAll();

    @NotSecured("TODO")
    ServiceResult<List<ApplicationResource>> findByUserId(final Long userId);

    /**
     * This method saves only a few application attributes that
     * the user is able to modify on the application form.
     */
    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'UPDATE')")
    ServiceResult<ApplicationResource> saveApplicationDetails(@P("applicationId") final Long id, ApplicationResource application);

    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'READ')")
    ServiceResult<ObjectNode> getProgressPercentageNodeByApplicationId(@P("applicationId") final Long applicationId);

    @PreAuthorize("hasPermission(#applicationId, 'com.worth.ifs.application.resource.ApplicationResource', 'UPDATE')")
    ServiceResult<ApplicationResource> updateApplicationStatus(@P("applicationId") final Long id,
                                                               final Long statusId);

    @PostFilter("hasPermission(#application, 'READ')")
    ServiceResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(final Long competitionId,
                                                                                     final Long userId,
                                                                                     final UserRoleType role);

    @PreAuthorize("hasAuthority('applicant')")
    ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            final Long competitionId,
            final Long userId,
            String applicationName);

    @NotSecured("TODO DW - INFUND-1555 - secure")
    ServiceResult<ApplicationResource> findByProcessRole(Long id);

    @NotSecured("TODO DW - INFUND-1555 - secure")
    ServiceResult<ObjectNode> applicationReadyForSubmit(final Long id);
}