package org.walkmod.writers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Summary {

    private static Summary instance = null;

    private List<File> writtenFiles;

    private Summary() {
        writtenFiles = new LinkedList<File>();
    }

    public static Summary getInstance() {
        if (instance == null) {
            instance = new Summary();
        }
        return instance;
    }

    public void clear() {
        writtenFiles.clear();
    }

    public List<File> getWrittenFiles() {
        return writtenFiles;
    }

    public void addFile(File file) {
        writtenFiles.add(file);
    }
}
