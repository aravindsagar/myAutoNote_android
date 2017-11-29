package paradigm.shift.myautonote.data_model;

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

    public Directory(final String name, final Directory parent,
                     final Map<String, Directory> subDirs, final Map<String, File> files) {
        super(name, parent);
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

    public Directory getSubDirectory(final String dirName) {
        return mySubDirs.get(dirName);
    }

    public File getFile(final String fileName) {
        return myFiles.get(fileName);
    }

    @Override
    public String toString() {
        return toString(0);
    }
    
    public String toString(final int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        sb.append(myName);
        sb.append('\n');
        for (Directory d : mySubDirs.values()) {
            sb.append(d.toString(indent+2));
            sb.append('\n');
        }
        for (File f : myFiles.values()) {
            sb.append(" (f) ");
            sb.append(f.getName());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public SearchResult search(final List<DataItem> path, final String query) {
        if (getName().toLowerCase().contains(query)) {
            return new SearchResult(path, null, true);
        }
        return null;
    }
}
