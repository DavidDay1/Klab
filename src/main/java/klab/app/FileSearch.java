package klab.app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Objects;

/**
 * Class for searching files in a directory
 * @version 1.0
 */

public class FileSearch implements FilenameFilter {

    String name;

    /**
     * Constructor for FileSearch
     * @param name name of file to search for
     */

    public FileSearch(String name) {
        this.name = name;
    }

    /**
     * Method for accepting files
     * @param dir directory to search
     * @param name name of file to search for
     * @return true if file is found, false otherwise
     */

    public boolean accept(File dir, String name) {
        if (name.equals("")){
            return false;
        }
        return name.contains(this.name);
    }

    /**
     * Method for searching files
     * @param dir directory to search
     * @param name name of file to search for
     * @return list of files found
     */

    public static List<File> search(File dir, String name) {
        FileSearch filter = new FileSearch(name);

        return List.of(Objects.requireNonNull(dir.listFiles(filter)));
    }
}
