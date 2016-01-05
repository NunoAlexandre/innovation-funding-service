package com.worth.ifs.file.transactional;

import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.repository.FileEntryRepository;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.resource.FileEntryResourceAssembler;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.transactional.ServiceFailure;
import com.worth.ifs.util.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Supplier;

import static com.worth.ifs.file.transactional.FileServiceImpl.ServiceFailures.*;
import static com.worth.ifs.util.Either.right;
import static com.worth.ifs.util.EntityLookupCallbacks.getOrFail;
import static com.worth.ifs.util.FileFunctions.pathElementsToFile;
import static com.worth.ifs.util.FileFunctions.pathElementsToPath;

/**
 * The class is an implementation of FileService that, based upon a given fileStorageStrategy, is able to
 * validate, store and retrieve files.
 */
@Service
public class FileServiceImpl extends BaseTransactionalService implements FileService {

    private static final Log log = LogFactory.getLog(FileServiceImpl.class);

    public enum ServiceFailures {
        UNABLE_TO_CREATE_FOLDERS, //
        UNABLE_TO_CREATE_FILE, //
        UNABLE_TO_UPDATE_FILE, //
        UNABLE_TO_DELETE_FILE, //
        DUPLICATE_FILE_CREATED, //
        UNABLE_TO_FIND_FILE, //
        INCORRECTLY_REPORTED_MEDIA_TYPE, //
        INCORRECTLY_REPORTED_FILESIZE, //
    }

    @Autowired
    private FileStorageStrategy fileStorageStrategy;

    @Autowired
    private FileEntryRepository fileEntryRepository;

    @Override
    public Either<ServiceFailure, Pair<File, FileEntry>> createFile(FileEntryResource resource, Supplier<InputStream> inputStreamSupplier) {

        return handlingErrors(UNABLE_TO_CREATE_FILE, () ->

            createTemporaryFileForValidation(inputStreamSupplier).map(validationFile -> {
                try {
                    return validateMediaType(validationFile, MediaType.parseMediaType(resource.getMediaType())).map(tempFile ->
                           validateContentLength(resource.getFilesizeBytes(), tempFile)).map(tempFile ->
                           saveFileEntry(resource).map(savedFileEntry ->
                           createFileForFileEntry(savedFileEntry, tempFile)).map(fileAndFileEntry ->
                           successResponse(fileAndFileEntry))
                    );
                } finally {
                    deleteFile(validationFile);
                }
            })
        );
    }

    @Override
    public Either<ServiceFailure, Supplier<InputStream>> getFileByFileEntryId(Long fileEntryId) {
        return handlingErrors(UNABLE_TO_FIND_FILE, () ->
                findFileEntry(fileEntryId).
                map(fileEntry -> findFile(fileEntry)).
                map(fileEntry -> getInputStreamSuppier(fileEntry)).
                map(inputStream -> successResponse(inputStream))
        );
    }

    @Override
    public Either<ServiceFailure, Pair<File, FileEntry>> updateFile(FileEntryResource updatedFile, Supplier<InputStream> inputStreamSupplier) {

        return handlingErrors(UNABLE_TO_UPDATE_FILE, () ->

                createTemporaryFileForValidation(inputStreamSupplier).map(validationFile -> {
                    try {
                        return validateMediaType(validationFile, MediaType.parseMediaType(updatedFile.getMediaType())).map(tempFile ->
                               validateContentLength(updatedFile.getFilesizeBytes(), tempFile)).map(tempFile ->
                               updateFileEntry(updatedFile).map(updatedFileEntry ->
                               updateFileForFileEntry(updatedFileEntry, tempFile).map(fileAndFileEntry ->
                               successResponse(fileAndFileEntry)
                        )));
                    } finally {
                        deleteFile(validationFile);
                    }
                })
        );
    }

    @Override
    public Either<ServiceFailure, FileEntry> deleteFile(long fileEntryId) {

        return handlingErrors(UNABLE_TO_DELETE_FILE, () ->
            findFileEntry(fileEntryId).
            map(fileEntry -> findFile(fileEntry).
            map(file -> {

                fileEntryRepository.delete(fileEntry);

                boolean fileDeletedSuccessfully = file.delete();

                if (fileDeletedSuccessfully) {
                    return successResponse(fileEntry);
                } else {
                    return errorResponse(UNABLE_TO_DELETE_FILE);
                }
            })));
    }

    private Either<ServiceFailure, FileEntry> updateFileEntry(FileEntryResource updatedFileDetails) {
        FileEntry updated = fileEntryRepository.save(FileEntryResourceAssembler.valueOf(updatedFileDetails));
        return right(updated);
    }

    private <T> Either<ServiceFailure, Pair<File, FileEntry>> updateFileForFileEntry(FileEntry existingFileEntry, File tempFile) {

        Pair<List<String>, String> absoluteFilePathAndName = fileStorageStrategy.getAbsoluteFilePathAndName(existingFileEntry);
        List<String> pathElements = absoluteFilePathAndName.getLeft();
        String filename = absoluteFilePathAndName.getRight();

        return updateFileForFileEntry(pathElements, filename, tempFile).map(file -> right(Pair.of(file, existingFileEntry)));
    }

    private Either<ServiceFailure, File> createTemporaryFileForValidation(Supplier<InputStream> inputStreamSupplier) {

        return createTemporaryFile("filevalidation", UNABLE_TO_CREATE_FILE).
                map(tempFile -> updateFileWithContents(tempFile, inputStreamSupplier)).
                map(tempFile -> pathToFile(tempFile));
    }

    private Either<ServiceFailure, File> validateContentLength(long filesizeBytes, File tempFile) {

        if (tempFile.length() == filesizeBytes) {
            return right(tempFile);
        } else {
            log.error("Reported filesize was " + filesizeBytes + " bytes but actual file is " + tempFile.length() + " bytes");
            return errorResponse(INCORRECTLY_REPORTED_FILESIZE);
        }
    }

    private Either<ServiceFailure, File> validateMediaType(File file, MediaType mediaType) {
        final String detectedContentType;
        try {
            detectedContentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.error("Unable to probe file for Content Type", e);
            return errorResponse(INCORRECTLY_REPORTED_MEDIA_TYPE);
        }

        if (detectedContentType == null) {
            log.warn("Content Type of file " + file + " could not be determined - returning as valid because not explicitly detectable");
            return right(file);
        } else if (mediaType.toString().equals(detectedContentType)) {
            return right(file);
        } else {
            log.warn("Content Type of file has been detected as " + detectedContentType + " but was reported as being " + mediaType);
            return errorResponse(INCORRECTLY_REPORTED_MEDIA_TYPE);
        }
    }

    private Either<ServiceFailure, Path> createTemporaryFile(String prefix, Enum<?> errorMessage) {
        try {
            return right(Files.createTempFile(prefix, ""));
        } catch (IOException e) {
            log.error("Error creating temporary file for " + prefix, e);
            return errorResponse(errorMessage);
        }
    }

    private Either<ServiceFailure, Supplier<InputStream>> getInputStreamSuppier(File file) {
        return right(() -> {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("Unable to supply FileInputStream for file " + file, e);
                throw new IllegalStateException("Unable to supply FileInputStream for file " + file, e);
            }
        });
    }

    private Either<ServiceFailure, Pair<File, FileEntry>> createFileForFileEntry(FileEntry savedFileEntry, File tempFile) {

        Pair<List<String>, String> absoluteFilePathAndName = fileStorageStrategy.getAbsoluteFilePathAndName(savedFileEntry);
        List<String> pathElements = absoluteFilePathAndName.getLeft();
        String filename = absoluteFilePathAndName.getRight();
        return createFileForFileEntry(pathElements, filename, tempFile).map(file -> right(Pair.of(file, savedFileEntry)));
    }

    private Either<ServiceFailure, FileEntry> saveFileEntry(FileEntryResource resource) {
        FileEntry fileEntry = FileEntryResourceAssembler.valueOf(resource);
        return right(fileEntryRepository.save(fileEntry));
    }

    private Either<ServiceFailure, FileEntry> findFileEntry(Long fileEntryId) {
        return getOrFail(() -> fileEntryRepository.findOne(fileEntryId), () -> {
            log.error("Could not find FileEntry for id " + fileEntryId);
            return ServiceFailure.error(UNABLE_TO_FIND_FILE);
        });
    }

    private Either<ServiceFailure, File> findFile(FileEntry fileEntry) {
        return getOrFail(() -> {

            Pair<List<String>, String> filePathAndName = fileStorageStrategy.getAbsoluteFilePathAndName(fileEntry);
            List<String> pathElements = filePathAndName.getLeft();
            String filename = filePathAndName.getRight();
            File expectedFile = new File(pathElementsToFile(pathElements), filename);
            return expectedFile.exists() ? expectedFile : null;

        }, () -> {
            log.error("Could not find File for FileEntry with id " + fileEntry.getId());
            return ServiceFailure.error(UNABLE_TO_FIND_FILE);
        });
    }

    private Either<ServiceFailure, File> createFileForFileEntry(List<String> absolutePathElements, String filename, File tempFile) {

        Path foldersPath = pathElementsToPath(absolutePathElements);

        return createFolders(foldersPath).
                map(createdFolders -> copyTempFileToTargetFile(createdFolders, filename, tempFile));
    }

    private Either<ServiceFailure, File> updateFileForFileEntry(List<String> absolutePathElements, String filename, File tempFile) {

        Path foldersPath = pathElementsToPath(absolutePathElements);
        return updateExistingFileWithTempFile(foldersPath, filename, tempFile);
    }

    private Either<ServiceFailure, File> copyTempFileToTargetFile(Path targetFolder, String targetFilename, File tempFile) {
        try {
            File fileToCreate = new File(targetFolder.toString(), targetFilename);

            if (fileToCreate.exists()) {
                log.error("File " + targetFilename + " already existed in target path " + targetFolder + ".  Cannot create a new one here.");
                return errorResponse(DUPLICATE_FILE_CREATED);
            }

            Path targetFile = Files.copy(tempFile.toPath(), Paths.get(targetFolder.toString(), targetFilename));
            return right(targetFile.toFile());
        } catch (IOException e) {
            log.error("Unable to copy temporary file " + tempFile + " to target folder " + targetFolder + " and file " + targetFilename, e);
            return errorResponse(UNABLE_TO_CREATE_FILE);
        }
    }

    private Either<ServiceFailure, File> updateExistingFileWithTempFile(Path targetFolder, String targetFilename, File tempFile) {
        try {
            File fileToCreate = new File(targetFolder.toString(), targetFilename);

            if (!fileToCreate.exists()) {
                log.error("File " + targetFilename + " doesn't exist in target path " + targetFolder + ".  Cannot update one here.");
                return errorResponse(UNABLE_TO_FIND_FILE);
            }

            Path targetFile = Files.copy(tempFile.toPath(), Paths.get(targetFolder.toString(), targetFilename), StandardCopyOption.REPLACE_EXISTING);
            return right(targetFile.toFile());
        } catch (IOException e) {
            log.error("Unable to copy temporary file " + tempFile + " to target folder " + targetFolder + " and file " + targetFilename, e);
            return errorResponse(UNABLE_TO_UPDATE_FILE);
        }
    }

    private Either<ServiceFailure, Path> createFolders(Path path) {
        try {
            return right(Files.createDirectories(path));
        } catch (IOException e) {
            log.error("Error creating folders " + path, e);
            return errorResponse(UNABLE_TO_CREATE_FOLDERS, e);
        }
    }

    private Either<ServiceFailure, File> pathToFile(Path path) {
        return right(path.toFile());
    }

    private Either<ServiceFailure, Path> updateFileWithContents(Path file, Supplier<InputStream> inputStreamSupplier) {

        try {
            try (InputStream sourceInputStream = inputStreamSupplier.get()) {
                try {
                    Files.copy(sourceInputStream, file, StandardCopyOption.REPLACE_EXISTING);
                    return right(file);
                } catch (IOException e) {
                    log.error("Could not write data to file " + file, e);
                    return errorResponse(UNABLE_TO_CREATE_FILE, e);
                }
            }
        } catch (IOException e) {
            log.error("Error closing file stream for file " + file, e);
            return errorResponse(UNABLE_TO_CREATE_FILE, e);
        }
    }

    private void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.error("Error deleting file", e);
        }
    }
}
