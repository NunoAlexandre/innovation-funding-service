package com.worth.ifs.file.domain.builders;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.file.domain.FileEntry;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class FileEntryBuilder extends BaseBuilder<FileEntry, FileEntryBuilder> {

    private FileEntryBuilder(List<BiConsumer<Integer, FileEntry>> multiActions) {
        super(multiActions);
    }

    public static FileEntryBuilder newFileEntry() {
        return new FileEntryBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected FileEntryBuilder createNewBuilderWithActions(List<BiConsumer<Integer, FileEntry>> actions) {
        return new FileEntryBuilder(actions);
    }

    @Override
    protected FileEntry createInitial() {
        return new FileEntry();
    }
}