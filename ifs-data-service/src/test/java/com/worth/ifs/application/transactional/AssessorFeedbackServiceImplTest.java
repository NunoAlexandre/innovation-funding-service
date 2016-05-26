package com.worth.ifs.application.transactional;

import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.file.domain.builders.FileEntryBuilder.newFileEntry;
import static com.worth.ifs.file.resource.builders.FileEntryResourceBuilder.newFileEntryResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.resource.FileEntryResource;

public class AssessorFeedbackServiceImplTest extends BaseServiceUnitTest<AssessorFeedbackServiceImpl> {

    @Test
    public void testCreateAssessorFeedbackFileEntry() {

        FileEntryResource fileEntryToCreate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        Application application = newApplication().withId(123L).build();
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        FileEntry createdFileEntry = newFileEntry().build();
        ServiceResult<Pair<File, FileEntry>> successfulFileCreationResult = serviceSuccess(Pair.of(new File("createdfile"), createdFileEntry));
        when(fileServiceMock.createFile(fileEntryToCreate, inputStreamSupplier)).thenReturn(successfulFileCreationResult);

        FileEntryResource createdFileEntryResource = newFileEntryResource().build();
        when(fileEntryMapperMock.mapToResource(createdFileEntry)).thenReturn(createdFileEntryResource);

        //
        // Call the method under test
        //
        ServiceResult<FileEntryResource> result = service.createAssessorFeedbackFileEntry(application.getId(), fileEntryToCreate, inputStreamSupplier);

        //
        // Assert that the result of our service call was successful and contains the resource returned from the mapper
        //
        assertTrue(result.isSuccess());
        assertEquals(createdFileEntryResource, result.getSuccessObject());

        // assert that the application entity got its Assessor Feedback file entry updated to match the FileEntry returned by
        // the FileService
        assertEquals(createdFileEntry, application.getAssessorFeedbackFileEntry());

        verify(applicationRepositoryMock).findOne(application.getId());
        verifyNoMoreInteractions(addressRepositoryMock);
    }

    @Test
    public void testCreateAssessorFeedbackFileEntryButApplicationDoesntExist() {

        FileEntryResource fileEntryToCreate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(applicationRepositoryMock.findOne(123L)).thenReturn(null);

        //
        // Call the method under test
        //
        ServiceResult<FileEntryResource> result = service.createAssessorFeedbackFileEntry(123L, fileEntryToCreate, inputStreamSupplier);

        //
        // Assert that the result of our service call was successful and contains the resource returned from the mapper
        //
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Application.class, 123L)));
    }

    @Test
    public void testGetAssessorFeedbackFileEntryDetails() {

        FileEntry existingFileEntry = newFileEntry().build();
        Application application = newApplication().withId(123L).withAssessorFeedbackFileEntry(existingFileEntry).build();
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        FileEntryResource retrievedFileEntryResource = newFileEntryResource().build();
        when(fileEntryMapperMock.mapToResource(existingFileEntry)).thenReturn(retrievedFileEntryResource);

        //
        // Call the method under test
        //
        ServiceResult<FileEntryResource> result = service.getAssessorFeedbackFileEntryDetails(application.getId());

        //
        // Assert that the result of our service call was successful and contains the resource returned from the mapper
        //
        assertTrue(result.isSuccess());
        assertEquals(retrievedFileEntryResource, result.getSuccessObject());

        verify(applicationRepositoryMock).findOne(application.getId());
        verifyNoMoreInteractions(addressRepositoryMock);
    }

    @Test
    public void testGetAssessorFeedbackFileEntryContents() {

        FileEntry existingFileEntry = newFileEntry().build();
        Application application = newApplication().withId(123L).withAssessorFeedbackFileEntry(existingFileEntry).build();
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        FileEntryResource retrievedFileEntryResource = newFileEntryResource().build();
        when(fileEntryMapperMock.mapToResource(existingFileEntry)).thenReturn(retrievedFileEntryResource);

        Supplier<InputStream> inputStreamSupplier = () -> null;
        when(fileServiceMock.getFileByFileEntryId(existingFileEntry.getId())).thenReturn(serviceSuccess(inputStreamSupplier));

        //
        // Call the method under test
        //
        ServiceResult<Pair<FileEntryResource, Supplier<InputStream>>> result = service.getAssessorFeedbackFileEntryContents(application.getId());

        //
        // Assert that the result of our service call was successful and contains the resource returned from the mapper
        //
        assertTrue(result.isSuccess());
        assertEquals(retrievedFileEntryResource, result.getSuccessObject().getKey());
        assertEquals(inputStreamSupplier, result.getSuccessObject().getValue());

        verify(applicationRepositoryMock).findOne(application.getId());
        verifyNoMoreInteractions(addressRepositoryMock);
    }

    @Test
    public void testUpdateAssessorFeedbackFileEntry() {

        FileEntryResource fileEntryToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        Application application = newApplication().withId(123L).build();
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        FileEntry updatedFileEntry = newFileEntry().build();
        ServiceResult<Pair<File, FileEntry>> successfulFileUpdateResult = serviceSuccess(Pair.of(new File("updatedfile"), updatedFileEntry));
        when(fileServiceMock.updateFile(fileEntryToUpdate, inputStreamSupplier)).thenReturn(successfulFileUpdateResult);

        FileEntryResource updatedFileEntryResource = newFileEntryResource().build();
        when(fileEntryMapperMock.mapToResource(updatedFileEntry)).thenReturn(updatedFileEntryResource);

        //
        // Call the method under test
        //
        ServiceResult<FileEntryResource> result = service.updateAssessorFeedbackFileEntry(application.getId(), fileEntryToUpdate, inputStreamSupplier);

        //
        // Assert that the result of our service call was successful and contains the resource returned from the mapper
        //
        assertTrue(result.isSuccess());
        assertEquals(updatedFileEntryResource, result.getSuccessObject());

        // assert that the application entity got its Assessor Feedback file entry updated to match the FileEntry returned by
        // the FileService
        assertEquals(updatedFileEntry, application.getAssessorFeedbackFileEntry());

        verify(applicationRepositoryMock).findOne(application.getId());
        verifyNoMoreInteractions(addressRepositoryMock);
    }

    @Test
    public void testDeleteAssessorFeedbackFileEntry() {

        FileEntry fileEntryToDelete = newFileEntry().build();

        Application application = newApplication().withId(123L).withAssessorFeedbackFileEntry(fileEntryToDelete).build();
        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);

        when(fileServiceMock.deleteFile(fileEntryToDelete.getId())).thenReturn(serviceSuccess(fileEntryToDelete));

        //
        // Call the method under test
        //
        ServiceResult<Void> result = service.deleteAssessorFeedbackFileEntry(application.getId());

        //
        // Assert that the result of our service call was successful
        //
        assertTrue(result.isSuccess());

        // assert that the application entity got its Assessor Feedback file entry deleted
        assertNull(application.getAssessorFeedbackFileEntry());

        verify(applicationRepositoryMock).findOne(application.getId());
        verifyNoMoreInteractions(addressRepositoryMock);
    }
    
    @Test
    public void testFeedbackUploadedNotUploaded() {
    	
    	when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(123L, Arrays.asList(3L, 4L, 2L))).thenReturn(5L);
    	
    	ServiceResult<Boolean> result = service.assessorFeedbackUploaded(123L);
    	
    	assertTrue(result.isSuccess());
    	assertFalse(result.getSuccessObject());
    }
    
    @Test
    public void testFeedbackUploadedIsUploaded() {
    	
    	when(applicationRepositoryMock.countByCompetitionIdAndApplicationStatusIdInAndAssessorFeedbackFileEntryIsNull(123L, Arrays.asList(3L, 4L, 2L))).thenReturn(0L);
    	
    	ServiceResult<Boolean> result = service.assessorFeedbackUploaded(123L);
    	
    	assertTrue(result.isSuccess());
    	assertTrue(result.getSuccessObject());
    }

    @Override
    protected AssessorFeedbackServiceImpl supplyServiceUnderTest() {
        return new AssessorFeedbackServiceImpl();
    }
}
