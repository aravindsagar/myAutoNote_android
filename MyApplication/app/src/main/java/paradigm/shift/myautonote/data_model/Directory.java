package paradigm.shift.myautonote.data_model;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by aravind on 11/18/17.
 */

public class Directory extends DataItem {

    // Items inside the directory can be stored as a map from name to item.
    private final Map<String, Directory> mySubDirs;
    private final Map<String, File> myFiles;

    public Directory(final String name, final List<String> pathFromRoot,
                     final Map<String, Directory> subDirs, final Map<String, File> files) {
        super(name, pathFromRoot);
        mySubDirs = subDirs;
        myFiles = files;
    }

    public List<String> getSubdirectoryNames() {
        List<String> dirNames = new ArrayList<>(mySubDirs.keySet());
        Collections.sort(dirNames);
        return dirNames;
    }

    public List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>(myFiles.keySet());
        Collections.sort(fileNames);
        return fileNames;
    }

    public Directory getSubDirectory(final String dirName) throws FileNotFoundException {
        if (mySubDirs.containsKey(dirName)) {
            return mySubDirs.get(dirName);
        } else {
            throw new FileNotFoundException();
        }
    }

    public File getFile(final String fileName) throws FileNotFoundException {
        if (myFiles.containsKey(fileName)) {
            return myFiles.get(fileName);
        } else {
            throw new FileNotFoundException();
        }
    }
}
