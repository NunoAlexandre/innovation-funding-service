package org.innovateuk.ifs.application.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.builder.ApplicationBuilder;
import org.innovateuk.ifs.application.builder.QuestionBuilder;
import org.innovateuk.ifs.application.constant.ApplicationStatusConstants;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.ApplicationStatus;
import org.innovateuk.ifs.application.domain.Question;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryId;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.builder.CompetitionBuilder;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.resource.FileEntryResourceAssembler;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.FormInputResponse;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.user.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.Role;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserRoleType;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.name;
import static org.innovateuk.ifs.LambdaMatcher.lambdaMatches;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.ApplicationStatusBuilder.newApplicationStatus;
import static org.innovateuk.ifs.application.constant.ApplicationStatusConstants.CREATED;
import static org.innovateuk.ifs.commons.error.CommonErrors.internalServerErrorError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.file.builder.FileEntryBuilder.newFileEntry;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.form.builder.FormInputBuilder.newFormInput;
import static org.innovateuk.ifs.form.builder.FormInputResponseBuilder.newFormInputResponse;
import static org.innovateuk.ifs.form.resource.FormInputType.*;
import static org.innovateuk.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.RoleBuilder.newRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ApplicationServiceImpl}
 */
public class ApplicationServiceImplMockTest extends BaseServiceUnitTest<ApplicationServiceImpl> {

    private Application openApplication;

    @Override
    protected ApplicationServiceImpl supplyServiceUnderTest() {
        return new ApplicationServiceImpl();
    }

    @Mock
    private SystemNotificationSource systemNotificationSourceMock;

    private FormInput formInput;
    private FormInputType formInputType;
    private Question question;
    private FileEntryResource fileEntryResource;
    private FormInputResponseFileEntryResource formInputResponseFileEntryResource;
    private FileEntry existingFileEntry;
    private FormInputResponse existingFormInputResponse;
    private List<FormInputResponse> existingFormInputResponses;
    private FormInputResponse unlinkedFormInputFileEntry;

    @Before
    public void setUp() throws Exception {
        question = QuestionBuilder.newQuestion().build();

        formInputType = FormInputType.FILEUPLOAD;

        formInput = newFormInput().withType(formInputType).build();
        formInput.setId(123L);
        formInput.setQuestion(question);
        question.setFormInputs(singletonList(formInput));

        fileEntryResource = newFileEntryResource().with(id(999L)).build();
        formInputResponseFileEntryResource = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);

        existingFileEntry = newFileEntry().with(id(999L)).build();
        existingFormInputResponse = newFormInputResponse().withFileEntry(existingFileEntry).build();
        existingFormInputResponses = singletonList(existingFormInputResponse);
        unlinkedFormInputFileEntry = newFormInputResponse().with(id(existingFormInputResponse.getId())).withFileEntry(null).build();
        final Competition openCompetition = newCompetition().withCompetitionStatus(CompetitionStatus.OPEN).build();
        openApplication = newApplication().withCompetition(openCompetition).build();

        when(applicationRepositoryMock.findOne(anyLong())).thenReturn(openApplication);
    }

    @Test
    public void testCreateApplicationByApplicationNameForUserIdAndCompetitionId() {

        Competition competition = newCompetition().build();
        User user = newUser().build();
        Organisation organisation = newOrganisation().with(name("testOrganisation")).build();
        Role leadApplicantRole = newRole().withType(LEADAPPLICANT).build();
        ProcessRole processRole = newProcessRole().withUser(user).withRole(leadApplicantRole).withOrganisationId(organisation.getId()).build();
        ApplicationStatus applicationStatus = newApplicationStatus().withName(CREATED).build();

        Application application = ApplicationBuilder.newApplication().
                withId(1L).
                withName("testApplication").
                withApplicationStatus(applicationStatus).
                withDurationInMonths(3L).
                withCompetition(competition).
                build();

        ApplicationResource applicationResource = newApplicationResource().build();

        when(applicationStatusRepositoryMock.findByName(CREATED.getName())).thenReturn(singletonList(applicationStatus));
        when(roleRepositoryMock.findOneByName(leadApplicantRole.getName())).thenReturn(leadApplicantRole);
        when(competitionRepositoryMock.findOne(competition.getId())).thenReturn(competition);
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);
        when(applicationRepositoryMock.save(any(Application.class))).thenReturn(application);
        when(processRoleRepositoryMock.findByUser(user)).thenReturn(singletonList(processRole));
        when(organisationRepositoryMock.findByUsers(user)).thenReturn(singletonList(organisation));
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        Supplier<Application> applicationExpectations = () -> argThat(lambdaMatches(created -> {
            assertEquals("testApplication", created.getName());
            assertEquals(applicationStatus.getId(), created.getApplicationStatus().getId());
            assertEquals(Long.valueOf(3), created.getDurationInMonths());
            assertEquals(competition.getId(), created.getCompetition().getId());
            assertNull(created.getStartDate());

            assertEquals(1, created.getProcessRoles().size());
            ProcessRole createdProcessRole = created.getProcessRoles().get(0);
            assertNull(createdProcessRole.getId());
            assertEquals(application.getId(), createdProcessRole.getApplicationId());
            assertEquals(organisation.getId(), createdProcessRole.getOrganisationId());
            assertEquals(leadApplicantRole.getId(), createdProcessRole.getRole().getId());
            assertEquals(user.getId(), createdProcessRole.getUser().getId());

            return true;
        }));

        when(applicationMapperMock.mapToResource(applicationExpectations.get())).thenReturn(applicationResource);

        ApplicationResource created =
                service.createApplicationByApplicationNameForUserIdAndCompetitionId("testApplication",
                        competition.getId(), user.getId()).getSuccessObject();

        verify(applicationRepositoryMock, times(2)).save(isA(Application.class));
        verify(processRoleRepositoryMock).save(isA(ProcessRole.class));
        assertEquals(applicationResource, created);
    }

    @Test
    public void testCreateFormInputResponseFileUpload() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        File fileFound = mock(File.class);
        FileEntry newFileEntry = newFileEntry().with(id(999L)).build();

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, newFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(null);
        when(processRoleRepositoryMock.findOne(789L)).thenReturn(newProcessRole().build());
        when(formInputRepositoryMock.findOne(123L)).thenReturn(newFormInput().build());
        when(applicationRepositoryMock.findOne(456L)).thenReturn(openApplication);

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isSuccess());
        FormInputResponseFileEntryResource resultParts = result.getSuccessObject();
        assertEquals(Long.valueOf(999), resultParts.getFileEntryResource().getId());

        verify(formInputResponseRepositoryMock).findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L);
    }

    @Test
    public void testCreateFormInputResponseFileUploadButReplaceIfFileAlreadyExistsForFormInputResponse() {

        FileEntryResource fileEntryResource = newFileEntryResource().with(id(987L)).build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntry alreadyExistingFileEntry = newFileEntry().with(id(987L)).build();
        FormInputResponse existingFormInputResponseWithLinkedFileEntry = newFormInputResponse().withFileEntry(alreadyExistingFileEntry).build();

        File fileFound = mock(File.class);

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));

        when(fileServiceMock.deleteFile(alreadyExistingFileEntry.getId())).thenReturn(serviceSuccess(alreadyExistingFileEntry));

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).thenReturn(serviceSuccess(Pair.of(fileFound, alreadyExistingFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponseWithLinkedFileEntry);
        when(processRoleRepositoryMock.findOne(789L)).thenReturn(newProcessRole().build());
        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);
        when(applicationRepositoryMock.findOne(456L)).thenReturn(openApplication);

        when(fileServiceMock.getFileByFileEntryId(987L)).thenReturn(serviceSuccess(inputStreamSupplier));

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        FormInputResponseFileEntryResource resultParts = result.getSuccessObject();
        assertEquals(Long.valueOf(987), resultParts.getFileEntryResource().getId());

        verify(formInputResponseRepositoryMock, times(3)).findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L);
    }

    @Test
    public void testCreateFormInputResponseFileUploadButFileServiceCallFails() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceFailure(internalServerErrorError()));

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void testCreateFormInputResponseFileUploadWithAlreadyExistingFormInputResponse() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        File fileFound = mock(File.class);
        FileEntry newFileEntry = newFileEntry().with(id(999L)).build();

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, newFileEntry)));

        FormInputResponse existingFormInputResponse = newFormInputResponse().build();
        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponse);

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isSuccess());
        FormInputResponseFileEntryResource resultParts = result.getSuccessObject();
        assertEquals(Long.valueOf(999), resultParts.getFileEntryResource().getId());

        assertEquals(newFileEntry, existingFormInputResponse.getFileEntry());
    }

    @Test
    public void testCreateFormInputResponseFileUploadButProcessRoleNotFound() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        File fileFound = mock(File.class);
        FileEntry newFileEntry = newFileEntry().with(id(999L)).build();

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, newFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(null);
        when(processRoleRepositoryMock.findOne(789L)).thenReturn(null);

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(ProcessRole.class, 789L)));
    }

    @Test
    public void testCreateFormInputResponseFileUploadButFormInputNotFound() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        File fileFound = mock(File.class);
        FileEntry newFileEntry = newFileEntry().with(id(999L)).build();

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, newFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(null);
        when(processRoleRepositoryMock.findOne(789L)).thenReturn(newProcessRole().build());
        when(formInputRepositoryMock.findOne(123L)).thenReturn(null);

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(FormInput.class, 123L)));
    }

    @Test
    public void testCreateFormInputResponseFileUploadButApplicationNotFound() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        File fileFound = mock(File.class);
        FileEntry newFileEntry = newFileEntry().with(id(999L)).build();

        when(fileServiceMock.createFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, newFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(null);
        when(processRoleRepositoryMock.findOne(789L)).thenReturn(newProcessRole().build());
        when(formInputRepositoryMock.findOne(123L)).thenReturn(newFormInput().build());
        when(applicationRepositoryMock.findOne(456L)).thenReturn(null);

        ServiceResult<FormInputResponseFileEntryResource> result =
                service.createFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Application.class, 456L)));
    }

    @Test
    public void testUpdateFormInputResponseFileUpload() {

        FileEntryResource fileEntryResource = newFileEntryResource().with(id(999L)).build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntry existingFileEntry = newFileEntry().with(id(999L)).build();

        FormInputResponse existingFormInputResponse = newFormInputResponse().withFileEntry(existingFileEntry).build();

        File fileFound = mock(File.class);

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));
        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);

        when(fileServiceMock.updateFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceSuccess(Pair.of(fileFound, existingFileEntry)));

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponse);
        when(fileServiceMock.getFileByFileEntryId(existingFileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));

        ServiceResult<Void> result =
                service.updateFormInputResponseFileUpload(fileEntry, inputStreamSupplier);

        assertTrue(result.isSuccess());

        verify(formInputResponseRepositoryMock).findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L);
    }

    @Test
    public void testUpdateFormInputResponseFileUploadButFileServiceCallFails() {

        FileEntryResource fileEntryResource = newFileEntryResource().build();
        FormInputResponseFileEntryResource formInputFileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);
        Supplier<InputStream> inputStreamSupplier = () -> null;

        FileEntry fileEntry = FileEntryResourceAssembler.valueOf(fileEntryResource);
        FormInputResponse existingFormInputResponse =
                newFormInputResponse().withFileEntry(fileEntry).build();

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));

        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);
        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponse);
        when(fileServiceMock.getFileByFileEntryId(fileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));

        when(fileServiceMock.updateFile(fileEntryResource, inputStreamSupplier)).
                thenReturn(serviceFailure(internalServerErrorError()));

        ServiceResult<Void> result =
                service.updateFormInputResponseFileUpload(formInputFileEntry, inputStreamSupplier);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void testDeleteFormInputResponseFileUpload() {

        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(456L, 123L)).thenReturn(existingFormInputResponses);
        when(fileServiceMock.getFileByFileEntryId(existingFileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));
        when(formInputResponseRepositoryMock.save(existingFormInputResponse)).thenReturn(unlinkedFormInputFileEntry);
        when(fileServiceMock.deleteFile(999L)).thenReturn(serviceSuccess(existingFileEntry));
        when(formInputRepositoryMock.findOne(formInputResponseFileEntryResource.getCompoundId().getFormInputId())).thenReturn(formInput);

        ServiceResult<FormInputResponse> result =
                service.deleteFormInputResponseFileUpload(formInputResponseFileEntryResource.getCompoundId());

        assertTrue(result.isSuccess());
        assertEquals(unlinkedFormInputFileEntry, result.getSuccessObject());
        assertNull(existingFormInputResponse.getFileEntry());
        verify(formInputResponseRepositoryMock, times(2)).findByApplicationIdAndFormInputId(456L, 123L);
        verify(formInputResponseRepositoryMock).save(existingFormInputResponse);
    }

    @Test
    public void testDeleteFormInputResponseFileUploadButFileServiceCallFails() {
        Supplier<InputStream> inputStreamSupplier = () -> null;

        //when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponse);
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(456L, 123L)).thenReturn(existingFormInputResponses);
        when(fileServiceMock.getFileByFileEntryId(existingFileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));
        when(fileServiceMock.deleteFile(999L)).thenReturn(serviceFailure(internalServerErrorError()));
        when(formInputRepositoryMock.findOne(formInputResponseFileEntryResource.getCompoundId().getFormInputId())).thenReturn(formInput);

        ServiceResult<FormInputResponse> result =
                service.deleteFormInputResponseFileUpload(formInputResponseFileEntryResource.getCompoundId());

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void testDeleteFormInputResponseFileUploadButUnableToFindFormInputResponse() {
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(456L, 123L)).thenReturn(existingFormInputResponses);
        when(formInputRepositoryMock.findOne(formInputResponseFileEntryResource.getCompoundId().getFormInputId())).thenReturn(null);
        ServiceResult<FormInputResponse> result =
                service.deleteFormInputResponseFileUpload(formInputResponseFileEntryResource.getCompoundId());

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(FormInput.class, 123L)));
    }

    @Test
    public void testDeleteFormInputResponseFileUploadButFileEntryNotFound() {

        FileEntryResource fileEntryResource = newFileEntryResource().with(id(999L)).build();
        FormInputResponseFileEntryResource fileEntry = new FormInputResponseFileEntryResource(fileEntryResource, 123L, 456L, 789L);

        FileEntry existingFileEntry = newFileEntry().with(id(999L)).build();
        FormInputResponse existingFormInputResponse = newFormInputResponse().withFileEntry(existingFileEntry).build();

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));

        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);
        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(existingFormInputResponse);
        when(fileServiceMock.getFileByFileEntryId(existingFileEntry.getId())).thenReturn(serviceFailure(notFoundError(File.class, 999L)));

        ServiceResult<FormInputResponse> result = service.deleteFormInputResponseFileUpload(fileEntry.getCompoundId());

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(File.class, 999L)));
    }

    @Test
    public void testGetFormInputResponseFileUpload() {

        FileEntry fileEntry = newFileEntry().with(id(321L)).build();
        FormInputResponse formInputResponse = newFormInputResponse().withFileEntry(fileEntry).build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));
        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(formInputResponse);
        when(fileServiceMock.getFileByFileEntryId(fileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));

        ServiceResult<FormInputResponseFileAndContents> result =
                service.getFormInputResponseFileUpload(new FormInputResponseFileEntryId(123L, 456L, 789L));

        assertTrue(result.isSuccess());
        assertEquals(inputStreamSupplier, result.getSuccessObject().getContentsSupplier());

        FileEntryResource fileEntryResource = newFileEntryResource().with(id(321L)).build();
        FormInputResponseFileEntryResource formInputResponseFile = result.getSuccessObject().getFormInputResponseFileEntry();

        assertEquals(fileEntryResource.getId(), formInputResponseFile.getFileEntryResource().getId());
        assertEquals(123L, formInputResponseFile.getCompoundId().getFormInputId());
        assertEquals(456L, formInputResponseFile.getCompoundId().getApplicationId());
        assertEquals(789L, formInputResponseFile.getCompoundId().getProcessRoleId());
    }

    @Test
    public void testGetFormInputResponseFileUploadButFileServiceCallFails() {

        FileEntry fileEntry = newFileEntry().build();
        FormInputResponse formInputResponse = newFormInputResponse().withFileEntry(fileEntry).build();

        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));
        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(formInputResponse);
        when(fileServiceMock.getFileByFileEntryId(fileEntry.getId())).thenReturn(serviceFailure(internalServerErrorError()));

        ServiceResult<FormInputResponseFileAndContents> result =
                service.getFormInputResponseFileUpload(new FormInputResponseFileEntryId(123L, 456L, 789L));

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(internalServerErrorError()));
    }

    @Test
    public void testGetFormInputResponseFileUploadButUnableToFindFormInputResponse() {
        Question question = QuestionBuilder.newQuestion().build();
        question.setMultipleStatuses(true);
        FormInput formInputLocal = newFormInput().withType(formInputType).build();
        formInputLocal.setId(123L);
        formInputLocal.setQuestion(question);
        question.setFormInputs(singletonList(formInputLocal));
        when(formInputRepositoryMock.findOne(123L)).thenReturn(formInputLocal);

        when(formInputResponseRepositoryMock.findByApplicationIdAndUpdatedByIdAndFormInputId(456L, 789L, 123L)).thenReturn(null);

        ServiceResult<FormInputResponseFileAndContents> result =
                service.getFormInputResponseFileUpload(new FormInputResponseFileEntryId(123L, 456L, 789L));

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(FormInputResponse.class, 456L, 789L, 123L)));
    }

    @Test
    public void applicationServiceShouldReturnApplicationByUserId() throws Exception {
        User testUser1 = new User(1L, "test", "User1",  "email1@email.nl", "testToken123abc", "my-uid");
        User testUser2 = new User(2L, "test", "User2",  "email2@email.nl", "testToken456def", "my-uid");

        Application testApplication1 = new Application(null, "testApplication1Name", null, null, 1L);
        Application testApplication2 = new Application(null, "testApplication2Name", null, null, 2L);
        Application testApplication3 = new Application(null, "testApplication3Name", null, null, 3L);

        ApplicationResource testApplication1Resource = newApplicationResource().with(id(1L)).withName("testApplication1Name").build();
        ApplicationResource testApplication2Resource = newApplicationResource().with(id(2L)).withName("testApplication2Name").build();
        ApplicationResource testApplication3Resource = newApplicationResource().with(id(3L)).withName("testApplication3Name").build();

        Organisation organisation1 = new Organisation(1L, "test organisation 1");
        Organisation organisation2 = new Organisation(2L, "test organisation 2");

        ProcessRole testProcessRole1 = new ProcessRole(0L, testUser1, testApplication1.getId(), new Role(), organisation1.getId());
        ProcessRole testProcessRole2 = new ProcessRole(1L, testUser1, testApplication2.getId(), new Role(), organisation1.getId());
        ProcessRole testProcessRole3 = new ProcessRole(2L, testUser2, testApplication2.getId(), new Role(), organisation2.getId());
        ProcessRole testProcessRole4 = new ProcessRole(3L, testUser2, testApplication3.getId(), new Role(), organisation2.getId());

        when(userRepositoryMock.findOne(1L)).thenReturn(testUser1);
        when(userRepositoryMock.findOne(2L)).thenReturn(testUser2);

        when(applicationRepositoryMock.findOne(testApplication1.getId())).thenReturn(testApplication1);
        when(applicationRepositoryMock.findOne(testApplication2.getId())).thenReturn(testApplication2);
        when(applicationRepositoryMock.findOne(testApplication3.getId())).thenReturn(testApplication3);

        when(processRoleRepositoryMock.findByUser(testUser1)).thenReturn(new ArrayList<ProcessRole>() {{
            add(testProcessRole1);
            add(testProcessRole2);
        }});

        when(processRoleRepositoryMock.findByUser(testUser2)).thenReturn(new ArrayList<ProcessRole>() {{
            add(testProcessRole3);
            add(testProcessRole4);
        }});

        when(applicationMapperMock.mapToResource(testApplication1)).thenReturn(testApplication1Resource);
        when(applicationMapperMock.mapToResource(testApplication2)).thenReturn(testApplication2Resource);
        when(applicationMapperMock.mapToResource(testApplication3)).thenReturn(testApplication3Resource);

        List<ApplicationResource> applicationsForUser1 = service.findByUserId(testUser1.getId()).getSuccessObject();
        assertEquals(2, applicationsForUser1.size());
        assertEquals(testApplication1Resource.getId(), applicationsForUser1.get(0).getId());
        assertEquals(testApplication2Resource.getId(), applicationsForUser1.get(1).getId());

        List<ApplicationResource> applicationsForUser2 = service.findByUserId(testUser2.getId()).getSuccessObject();
        assertEquals(2, applicationsForUser1.size());
        assertEquals(testApplication2Resource.getId(), applicationsForUser2.get(0).getId());
        assertEquals(testApplication3Resource.getId(), applicationsForUser2.get(1).getId());
    }

    @Test
    public void applicationControllerCanCreateApplication() throws Exception {
        Long competitionId = 1L;
        Long organisationId = 2L;
        Long userId = 3L;
        String roleName = UserRoleType.LEADAPPLICANT.getName();
        Competition competition = CompetitionBuilder.newCompetition().with(id(1L)).build();
        Role role = newRole().with(name(roleName)).build();
        Organisation organisation = newOrganisation().with(id(organisationId)).build();
        User user = newUser().with(id(userId)).build();
        ApplicationStatus applicationStatus = newApplicationStatus().withName(ApplicationStatusConstants.CREATED.getName()).build();

        String applicationName = "testApplication";

        Application application = ApplicationBuilder.newApplication().
                withId(1L).
                withName(applicationName).
                withApplicationStatus(applicationStatus).
                withCompetition(competition).
                build();

        ApplicationResource newApplication = newApplicationResource().build();

        when(applicationStatusRepositoryMock.findByName(applicationStatus.getName())).thenReturn(singletonList(applicationStatus));
        when(competitionRepositoryMock.findOne(competition.getId())).thenReturn(competition);
        when(roleRepositoryMock.findOneByName(role.getName())).thenReturn(role);
        when(userRepositoryMock.findOne(userId)).thenReturn(user);
        when(processRoleRepositoryMock.findByUser(user)).thenReturn(singletonList(
            newProcessRole().withUser(user).withOrganisationId(organisation.getId()).build()
        ));
        when(organisationRepositoryMock.findByUsers(user)).thenReturn(singletonList(organisation));
        when(applicationRepositoryMock.save(any(Application.class))).thenReturn(application);
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        Supplier<Application> applicationExpectations = () -> argThat(lambdaMatches(created -> {
            assertEquals(applicationName, created.getName());
            assertEquals(applicationStatus, created.getApplicationStatus());
            assertEquals(competitionId, created.getCompetition().getId());
            return true;
        }));

        when(applicationMapperMock.mapToResource(applicationExpectations.get())).thenReturn(newApplication);

        ApplicationResource created = service.createApplicationByApplicationNameForUserIdAndCompetitionId(applicationName, competitionId, userId).getSuccessObject();
        assertEquals(newApplication, created);
    }

    @Test
    public void test_ErrorCountTurnover() {
        // Should never happen but check that reasonable error codes get returned in the event that the database
        // becomes inconsistent

        // Turnover and count - these should always be in sync - but here we test when they are not.
        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(true).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(false).build();

        // Method under test
        ServiceResult<Boolean> shouldBeFailure = service.isIncludeGrowthTableByCountAndTurnover(staffCountFormInput, staffTurnoverFormInput);

        // Assertions
        assertTrue(shouldBeFailure.isFailure());
        assertEquals(1, shouldBeFailure.getErrors().size());
        assertEquals("include.growth.table.count.turnover.input.active.not.consistent", shouldBeFailure.getErrors().get(0).getErrorKey());
    }


    @Test
    public void test_ErrorFinance() {
        // Should never happen but check that reasonable error codes get returned in the event that the database
        // becomes inconsistent

        // Financial inputs - these should always be in sync - but here we test when they are not.
        FormInput yearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(true).build();
        List<FormInput> overviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(true, true, true, false /*Inconsistent*/).build(4);
        FormInput count = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(true).build();

        // Method under test
        ServiceResult<Boolean> shouldBeFailure = service.isIncludeGrowthTableByFinance(yearEnd, overviewRows, count);

        // Assertions
        assertTrue(shouldBeFailure.isFailure());
        assertEquals(1, shouldBeFailure.getErrors().size());
        assertEquals("include.growth.table.finance.input.active.not.consistent", shouldBeFailure.getErrors().get(0).getErrorKey());
    }

    @Test
    public void test_ErrorCountTurnoverFinance() {
        // Should never happen but check that reasonable error codes get returned in the event that the database
        // becomes inconsistent

        // Not consistent
        boolean byFinance = false;
        boolean byCountAndTurnover = true;
        // Method under test
        ServiceResult<Boolean> shouldBeFailure = service.isIncludeGrowthTableByCountTurnoverAndFinance(byFinance, byCountAndTurnover);

        // Assertions
        assertTrue(shouldBeFailure.isFailure());
        assertEquals(1, shouldBeFailure.getErrors().size());
        assertEquals("include.growth.table.count.turnover.finance.input.active.not.consistent", shouldBeFailure.getErrors().get(0).getErrorKey());
    }

    @Test
    public void test_GetTurnoverNonFinancial() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long turnoverFormInputId = 678L;
        boolean isIncludeGrowthTable = false;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse turnover = newFormInputResponse().withValue("2").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).withId(turnoverFormInputId).withResponses(asList(turnover)).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, turnoverFormInputId)).thenReturn(asList(turnover));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).withId(turnoverFormInputId).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));

        ServiceResult<Long> result = service.getTurnoverByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertEquals(2L, result.getSuccessObject().longValue());
    }

    @Test
    public void test_GetHeadcountNonFinancial() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long headcountFormInputId = 987L;
        boolean isIncludeGrowthTable = false;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse headcount = newFormInputResponse().withValue("1").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).withId(headcountFormInputId).withResponses(asList(headcount)).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, headcountFormInputId)).thenReturn(asList(headcount));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).withId(headcountFormInputId).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));

        ServiceResult<Long> result = service.getHeadcountByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertEquals(1L, result.getSuccessObject().longValue());
    }

    @Test
    public void test_GetTurnoverFinancial() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long turnoverFormInputId = 678L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse turnover = newFormInputResponse().withValue("2").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).withId(turnoverFormInputId).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).withId(turnoverFormInputId).withResponses(asList(turnover)).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, turnoverFormInputId)).thenReturn(asList(turnover));
        ServiceResult<Long> result = service.getTurnoverByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertEquals(2L, result.getSuccessObject().longValue());
    }

    @Test
    public void test_GetHeadcountFinancial() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long headcountFormInputId = 987L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse headcount = newFormInputResponse().withValue("1").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).withId(headcountFormInputId).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).withId(headcountFormInputId).withResponses(asList(headcount)).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, headcountFormInputId)).thenReturn(asList(headcount));

        ServiceResult<Long> result = service.getHeadcountByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertEquals(1L, result.getSuccessObject().longValue());
    }

    @Test
    public void test_GetHeadcountFinancialNoHeadcountResponse() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long headcountFormInputId = 987L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse headcount = newFormInputResponse().withValue("1").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).withId(headcountFormInputId).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).withId(headcountFormInputId).withResponses(asList(headcount)).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, headcountFormInputId)).thenReturn(emptyList());

        ServiceResult<Long> result = service.getHeadcountByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertNull(result.getSuccessObject());
    }

    @Test
    public void test_GetHeadcountFinancialNoHeadcountInput() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long headcountFormInputId = 987L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).withId(headcountFormInputId).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(emptyList());

        ServiceResult<Long> result = service.getHeadcountByApplicationId(applicationId);

        assertTrue(result.isFailure());
    }

    @Test
    public void test_GetTurnoverFinancialNoTurnoverResponse() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long turnoverFormInputId = 678L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInputResponse turnover = newFormInputResponse().withValue("2").build();

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).withId(turnoverFormInputId).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).withId(turnoverFormInputId).withResponses(asList(turnover)).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        FormInput financialCount = newFormInput().withType(FormInputType.FINANCIAL_STAFF_COUNT).withActive(isIncludeGrowthTable).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(asList(financialCount));
        when(formInputResponseRepositoryMock.findByApplicationIdAndFormInputId(applicationId, turnoverFormInputId)).thenReturn(emptyList());
        ServiceResult<Long> result = service.getTurnoverByApplicationId(applicationId);

        assertTrue(result.isSuccess());
        assertNull(result.getSuccessObject());
    }

    @Test
    public void test_GetTurnoverFinancialNoTurnoverInput() {
        Long applicationId = 123L;
        Long competitionId = 456L;
        Long turnoverFormInputId = 678L;
        boolean isIncludeGrowthTable = true;
        Competition comp = new Competition();
        comp.setId(competitionId);
        Application app = new Application();
        app.setId(applicationId);
        app.setCompetition(comp);
        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(app);

        FormInput staffCountFormInput = newFormInput().withType(STAFF_COUNT).withActive(!isIncludeGrowthTable).build();
        FormInput staffTurnoverFormInput = newFormInput().withType(STAFF_TURNOVER).withActive(!isIncludeGrowthTable).withId(turnoverFormInputId).build();
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_TURNOVER))).thenReturn(asList(staffTurnoverFormInput));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(STAFF_COUNT))).thenReturn(asList(staffCountFormInput));

        FormInput financialYearEnd = newFormInput().withType(FINANCIAL_YEAR_END).withActive(isIncludeGrowthTable).withId(turnoverFormInputId).build();
        List<FormInput> financialOverviewRows = newFormInput().withType(FINANCIAL_OVERVIEW_ROW).withActive(isIncludeGrowthTable).build(4);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_YEAR_END))).thenReturn(asList(financialYearEnd));
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_OVERVIEW_ROW))).thenReturn(financialOverviewRows);
        when(formInputRepositoryMock.findByCompetitionIdAndTypeIn(competitionId, asList(FINANCIAL_STAFF_COUNT))).thenReturn(emptyList());
        ServiceResult<Long> result = service.getTurnoverByApplicationId(applicationId);

        assertTrue(result.isFailure());
    }
}
