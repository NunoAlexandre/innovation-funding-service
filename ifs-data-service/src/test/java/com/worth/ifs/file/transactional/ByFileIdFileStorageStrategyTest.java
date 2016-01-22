package com.worth.ifs.file.transactional;

import com.worth.ifs.file.domain.FileEntry;
import org.junit.Test;

import java.util.List;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.file.domain.builders.FileEntryBuilder.newFileEntry;
import static com.worth.ifs.util.CollectionFunctions.combineLists;
import static com.worth.ifs.util.CollectionFunctions.simpleJoiner;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * Test the storage strategy of ByFileIdFileStorageStrategy
 */
public class ByFileIdFileStorageStrategyTest {

    private List<String> tempFolderPathSegments = asList("path", "to");
    private List<String> tempFolderPathSegmentsWithBaseFolder = asList("path", "to", "BaseFolder");
    private String fullPathToTempFolder = simpleJoiner(tempFolderPathSegments, separator);

    @Test
    public void testGetFilePathAndName() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy(fullPathToTempFolder, "BaseFolder");

        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "000000000_999999999", "000000_999999", "000_999"), strategy.getFilePathAndName(0L).getKey());
        assertEquals("0", strategy.getFilePathAndName(0L).getValue());
    }

    @Test
    public void testGetAbsoluteFilePathAndName() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy(fullPathToTempFolder, "BaseFolder");

        FileEntry fileEntry = newFileEntry().with(id(123L)).build();
        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "000000000_999999999", "000000_999999", "000_999"), strategy.getAbsoluteFilePathAndName(fileEntry).getKey());
        assertEquals("123", strategy.getAbsoluteFilePathAndName(fileEntry).getValue());
    }

    @Test
    public void testGetFilePathAndNameForMoreComplexNumber() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy(fullPathToTempFolder, "BaseFolder");

        // test a number that is within the middle of the deepest set of partitions
        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "000000000_999999999", "000000_999999", "5000_5999"), strategy.getFilePathAndName(5123L).getKey());
        assertEquals("5123", strategy.getFilePathAndName(5123L).getValue());

        // test a number that is within the middle of the deepest and next deepest set of partitions
        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "000000000_999999999", "5000000_5999999", "5123000_5123999"), strategy.getFilePathAndName(5123123L).getKey());
        assertEquals("5123123", strategy.getFilePathAndName(5123123L).getValue());

        // test a number that is within the middle of the deepest, next deepest and least deepest set of partitions
        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "5000000000_5999999999", "5526000000_5526999999", "5526359000_5526359999"), strategy.getFilePathAndName(5526359849L).getKey());
        assertEquals("5526359849", strategy.getFilePathAndName(5526359849L).getValue());
    }

    @Test
    public void testEachPartitionLevelCanOnlyContainMaximumOf1000Entries() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy(fullPathToTempFolder, "BaseFolder");

        List<String> folderPaths = strategy.getFilePathAndName(5526359849L).getKey();
        assertEquals(combineLists(tempFolderPathSegmentsWithBaseFolder, "5000000000_5999999999", "5526000000_5526999999", "5526359000_5526359999"), folderPaths);

        //
        // Check that the parent partitions can only hold a maximum of 1000 child entreis underneath them.
        // Do this by looking at the size of one of the partition's child folders, and then seeing the
        // id range that that child folder has, and then seeing how many of those folder sizes that
        // could be fit underneath it - this'll be based on the current partition's id range of course
        //
        assertEquals("BaseFolder", folderPaths.get(tempFolderPathSegments.size()));
        for (int depth = tempFolderPathSegments.size() + 1; depth < folderPaths.size() - 1; depth++) {

            String[] currentPartitionFromAndToRange = folderPaths.get(depth).split("_");
            long currentPartitionFrom = Long.valueOf(currentPartitionFromAndToRange[0]);
            long currentPartitionTo = Long.valueOf(currentPartitionFromAndToRange[1]);
            long currentPartitionsIdRange = currentPartitionTo - currentPartitionFrom + 1;

            String[] childPartitionFromAndToRange = folderPaths.get(depth + 1).split("_");
            long childPartitionFrom = Long.valueOf(childPartitionFromAndToRange[0]);
            long childPartitionTo = Long.valueOf(childPartitionFromAndToRange[1]);
            long childPartitionsIdRange = childPartitionTo - childPartitionFrom + 1;

            assertEquals(1000, currentPartitionsIdRange / childPartitionsIdRange);
        }
    }

    @Test
    public void testGetFullPathToFileUploadFolderWithUnixSeparator() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy("/tmp/path/to/containing/folder", "BaseFolder");

        List<String> fullPathToFileUploadFolder = strategy.getAbsolutePathToFileUploadFolder("/");
        assertEquals(asList("/tmp", "path", "to", "containing", "folder", "BaseFolder"), fullPathToFileUploadFolder);
    }

    @Test
    public void testGetFullPathToFileUploadFolderWithWindowsSeparator() {

        ByFileIdFileStorageStrategy strategy = new ByFileIdFileStorageStrategy("c:\\tmp\\path\\to\\containing\\folder", "BaseFolder");

        List<String> fullPathToFileUploadFolder = strategy.getAbsolutePathToFileUploadFolder("\\");
        assertEquals(asList("c:", "tmp", "path", "to", "containing", "folder", "BaseFolder"), fullPathToFileUploadFolder);
    }
}
