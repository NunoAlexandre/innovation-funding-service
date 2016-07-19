package com.worth.ifs.application.transactional;

import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.service.FileAndContents;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * An object containing Form Input Response file details as well as the standard FileEntry details
 */
public class FormInputResponseFileAndContents implements FileAndContents {

    private FormInputResponseFileEntryResource fileEntry;
    private Supplier<InputStream> contentsSupplier;

    public FormInputResponseFileAndContents(FormInputResponseFileEntryResource fileEntry, Supplier<InputStream> contentsSupplier) {
        this.fileEntry = fileEntry;
        this.contentsSupplier = contentsSupplier;
    }

    public FormInputResponseFileEntryResource getFormInputResponseFileEntry() {
        return fileEntry;
    }

    @Override
    public FileEntryResource getFileEntry() {
        return fileEntry.getFileEntryResource();
    }

    @Override
    public Supplier<InputStream> getContentsSupplier() {
        return contentsSupplier;
    }
}
