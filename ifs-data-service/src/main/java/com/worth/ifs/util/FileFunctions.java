package com.worth.ifs.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * A collection of helpful methods for dealing with Files and Filesystem concerns
 */
public final class FileFunctions {

	private FileFunctions(){}
	
    static String separator = File.separator;
    
    /**
     * Given a list of path parts, this method will construct a path separated by the file separator.
     *
     * E.g. given ("path", "to", "file"), this will return "path/to/file".
     *
     * @param pathElements
     * @return
     */
    public static final String pathElementsToPathString(List<String> pathElements) {

        if (pathElements == null || pathElements.isEmpty()) {
            return "";
        }

        return pathElements.stream().reduce("",
                (pathSoFar, nextPathSegment) -> pathSoFar + (!pathSoFar.isEmpty() ? separator : "") + nextPathSegment);
    }

    /**
     * Given a list of path parts, this method will construct a path separated by the file separator, and ensures that the
     * path is absolute.
     *
     * E.g. given ("path", "to", "file"), this will return "/path/to/file".  Note the leading "/".
     * E.g. given ("/path", "to", "file"), this will return "/path/to/file".  Note the leading "/" is not duplicated.
     *
     * @param pathElements
     * @return
     */
    public static final String pathElementsToAbsolutePathString(List<String> pathElements, String absolutePathPrefix) {
        String path = pathElementsToPathString(pathElements);
        return path.startsWith(absolutePathPrefix) ? path : absolutePathPrefix + path;
    }

    /**
     * Given a list of path parts, this method will construct a path separated by the file separator.
     *
     * E.g. given ("path", "to", "file"), this will return "path/to/file".
     *
     * @param pathElements
     * @return
     */
    public static final File pathElementsToFile(List<String> pathElements) {
        return new File(pathElementsToPathString(pathElements));
    }

    /**
     * Given a list of path parts, this method will construct a path separated by the file separator.
     *
     * E.g. given ("path", "to", "file"), this will return "path/to/file".
     *
     * @param pathElements
     * @return
     */
    public static final Path pathElementsToPath(List<String> pathElements) {
        return pathElementsToFile(pathElements).toPath();
    }
}
