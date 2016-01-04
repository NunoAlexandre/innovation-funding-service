package com.worth.ifs.application.transactional;

import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.application.resource.FormInputResponseFileEntryId;
import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.resource.FileEntryResourceAssembler;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.transactional.ServiceFailure;
import com.worth.ifs.transactional.ServiceSuccess;
import com.worth.ifs.user.domain.*;
import com.worth.ifs.util.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.application.transactional.ApplicationServiceImpl.ServiceFailures.*;
import static com.worth.ifs.transactional.BaseTransactionalService.Failures.*;
import static com.worth.ifs.transactional.ServiceFailure.error;
import static com.worth.ifs.util.Either.right;
import static com.worth.ifs.util.EntityLookupCallbacks.getOrFail;

/**
 * Transactional and secured service focused around the processing of Applications
 */
@Service
public class ApplicationServiceImpl extends BaseTransactionalService implements ApplicationService {

    public enum ServiceFailures {
        UNABLE_TO_CREATE_FILE, //
        UNABLE_TO_UPDATE_FILE, //
        UNABLE_TO_DELETE_FILE, //
        UNABLE_TO_FIND_FILE, //
    }

    @Autowired
    private FileService fileService;

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    public Application createApplicationByApplicationNameForUserIdAndCompetitionId(String applicationName, Long competitionId, Long userId) {

        User user = userRepository.findOne(userId);

        Application application = new Application();
        application.setName(applicationName);
        LocalDate currentDate = LocalDate.now();
        application.setStartDate(currentDate);

        String name = ApplicationStatusConstants.CREATED.getName();

        List<ApplicationStatus> applicationStatusList = applicationStatusRepository.findByName(name);
        ApplicationStatus applicationStatus = applicationStatusList.get(0);

        application.setApplicationStatus(applicationStatus);
        application.setDurationInMonths(3L);

        List<Role> roles = roleRepository.findByName(UserRoleType.LEADAPPLICANT.getName());
        Role role = roles.get(0);

        Organisation userOrganisation = user.getProcessRoles().get(0).getOrganisation();

        Competition competition = competitionRepository.findOne(competitionId);
        ProcessRole processRole = new ProcessRole(user, application, role, userOrganisation);

        List<ProcessRole> processRoles = new ArrayList<>();
        processRoles.add(processRole);

        application.setProcessRoles(processRoles);
        application.setCompetition(competition);

        applicationRepository.save(application);
        processRoleRepository.save(processRole);

        return application;
    }

    @Override
    public Either<ServiceFailure, ServiceSuccess<Pair<File, FormInputResponseFileEntryResource>>> createFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        return handlingErrors(UNABLE_TO_CREATE_FILE, () ->
                fileService.createFile(formInputResponseFile.getFileEntryResource(), inputStreamSupplier).map(successfulFile -> {

            long applicationId = formInputResponseFile.getCompoundId().getApplicationId();
            long processRoleId = formInputResponseFile.getCompoundId().getProcessRoleId();
            long formInputId = formInputResponseFile.getCompoundId().getFormInputId();

            FileEntry fileEntry = successfulFile.getResult().getValue();

            FormInputResponse existingResponse = formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(applicationId, processRoleId, formInputId);

            if (existingResponse != null) {
                existingResponse.setFileEntry(fileEntry);
                formInputResponseRepository.save(existingResponse);
                FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputResponseFile.getCompoundId());
                return successResponse(Pair.of(successfulFile.getResult().getKey(), fileEntryResource));
            } else {
                return getProcessRole(processRoleId).
                        map(processRole -> getFormInput(formInputId).
                        map(formInput -> getApplication(applicationId).
                        map(application -> {

                    FormInputResponse newFormInputResponse = new FormInputResponse(LocalDateTime.now(), fileEntry, processRole, formInput, application);
                    formInputResponseRepository.save(newFormInputResponse);
                    FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputId, applicationId, processRoleId);
                    return successResponse(Pair.of(successfulFile.getResult().getKey(), fileEntryResource));
                })));
            }
        }));
    }

    @Override
    public Either<ServiceFailure, ServiceSuccess<Pair<File, FormInputResponseFileEntryResource>>> updateFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        return handlingErrors(UNABLE_TO_UPDATE_FILE, () -> {

            Either<ServiceFailure, ServiceSuccess<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>>> existingFileResult =
                    getFormInputResponseFileUpload(formInputResponseFile.getCompoundId());

            return existingFileResult.map(existingFile -> {

                FormInputResponseFileEntryResource existingFormInputResource = existingFile.getResult().getKey();

                FileEntryResource existingFileResource = existingFormInputResource.getFileEntryResource();
                FileEntryResource updatedFileDetails = formInputResponseFile.getFileEntryResource();
                FileEntryResource updatedFileDetailsWithId = new FileEntryResource(existingFileResource.getId(), updatedFileDetails.getName(), updatedFileDetails.getMediaType(), updatedFileDetails.getFilesizeBytes());

                return fileService.updateFile(updatedFileDetailsWithId, inputStreamSupplier).map(updatedFile ->
                    successResponse(Pair.of(updatedFile.getResult().getKey(), existingFormInputResource))
                );
            });
        });
    }

    @Override
    public Either<ServiceFailure, ServiceSuccess<FormInputResponse>> deleteFormInputResponseFileUpload(FormInputResponseFileEntryId formInputResponseFileId) {

        return handlingErrors(UNABLE_TO_DELETE_FILE, () -> {

            Either<ServiceFailure, ServiceSuccess<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>>> existingFileResult =
                    getFormInputResponseFileUpload(formInputResponseFileId);

            return existingFileResult.map(existingFile -> {

                FormInputResponseFileEntryResource formInputFileEntryResource = existingFile.getResult().getKey();
                Long fileEntryId = formInputFileEntryResource.getFileEntryResource().getId();

                return fileService.deleteFile(fileEntryId).
                    map(deletedFile -> getFormInputResponse(formInputFileEntryResource.getCompoundId()).
                    map(formInputResponse -> unlinkFileEntryFromFormInputResponse(formInputResponse)).
                    map(unlinkedFileEntryResource -> successResponse(unlinkedFileEntryResource)
                ));
            });
        });
    }

    private Either<ServiceFailure, FormInputResponse> unlinkFileEntryFromFormInputResponse(FormInputResponse formInputResponse) {
        formInputResponse.setFileEntry(null);
        FormInputResponse unlinkedResponse = formInputResponseRepository.save(formInputResponse);
        return right(unlinkedResponse);
    }

    @Override
    public Either<ServiceFailure, ServiceSuccess<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>>> getFormInputResponseFileUpload(FormInputResponseFileEntryId fileEntryId) {
        return handlingErrors(UNABLE_TO_FIND_FILE, () -> getFormInputResponse(fileEntryId).
                map(formInputResponse -> fileService.getFileByFileEntryId(formInputResponse.getFileEntry().getId()).
                map(inputStreamSupplier -> successResponse(Pair.of(formInputResponseFileEntryResource(formInputResponse.getFileEntry(), fileEntryId), inputStreamSupplier.getResult()))
        )));
    }

    private Either<ServiceFailure, FormInput> getFormInput(long formInputId) {
        return getOrFail(() -> formInputRepository.findOne(formInputId), () -> error(FORM_INPUT_NOT_FOUND));
    }

    private Either<ServiceFailure, Application> getApplication(long applicationId) {
        return getOrFail(() -> applicationRepository.findOne(applicationId), () -> error(APPLICATION_NOT_FOUND));
    }

    private FormInputResponseFileEntryResource formInputResponseFileEntryResource(FileEntry fileEntry, FormInputResponseFileEntryId fileEntryId) {
        FileEntryResource fileEntryResource = FileEntryResourceAssembler.valueOf(fileEntry);
        return new FormInputResponseFileEntryResource(fileEntryResource, fileEntryId.getFormInputId(), fileEntryId.getApplicationId(), fileEntryId.getProcessRoleId());
    }

    private Either<ServiceFailure, FormInputResponse> getFormInputResponse(FormInputResponseFileEntryId fileEntry) {
        return getOrFail(() -> formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(fileEntry.getApplicationId(), fileEntry.getProcessRoleId(), fileEntry.getFormInputId()), () -> error(FORM_INPUT_RESPONSE_NOT_FOUND));
    }
}
