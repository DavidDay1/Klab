package klab.app;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static klab.app.Node.logger;

/**
 * Class for searching files in a directory
 * @version 1.0
 */

public class FileSearch {

    private String name;
    private String fileID;

    /**
     * Constructor for FileSearch
     * @param name name of file to search for
     */

    public FileSearch(String name, String fileID) {
        this.name = name;
        this.fileID = fileID;
    }


    /**
     * Method for accepting files
     * @param dir directory to search
     * @param name name of file to search for
     * @return true if file is found, false otherwise
     */

    public boolean acceptByName(File dir, String name) {
        if (name.equals("")){
            return false;
        }
        return name.contains(this.name);
    }

    public boolean acceptByID(File dir, String id) {
        if (fileID.equals("")){
            return false;
        }
        return id.equals(this.fileID);
    }


    /**
     * Method for searching files
     * @param dir directory to search
     * @param name name of file to search for
     * @return list of files found
     */

    public static List<File> searchByName(File dir, String name) {
        logger.info("Searching for: " + name);
        logger.info("In directory: " + dir.getName());
        FileSearch filter = new FileSearch(name, null);
        return search(dir, filter::acceptByName);
    }

    public static File searchByID(File dir, String fileID) {
        logger.info("Searching for: " + fileID);
        logger.info("In directory: " + dir.getName());
        FileSearch filter = new FileSearch(null, fileID);
        File[] file = dir.listFiles(filter::acceptByID);
        if (file.length == 0) {
            return null;
        }
        return file[0];
    }

    private static List<File> search(File dir, FilenameFilter filter) {
        List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles(filter)));

        files.sort(Comparator.comparing(File::length));

        return files;
    }
}
