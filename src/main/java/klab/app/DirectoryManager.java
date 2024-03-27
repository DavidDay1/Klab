package klab.app;

import java.io.File;

public class DirectoryManager {
    private File dir;


    public synchronized File getDir() {
        return dir;
    }

    public synchronized void setDir(File dir) {
        this.dir = dir;
    }
}
