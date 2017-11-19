package paradigm.shift.myautonote.data_model;

import java.util.List;

/**
 * Created by aravind on 11/18/17.
 */

public class File extends DataItem {

    private final String myFileContents;

    public File(final String name, final List<String> pathFromRoot, String fileContents) {
        super(name, pathFromRoot);
        this.myFileContents = fileContents;
    }

    public String getMyFileContents() {
        return myFileContents;
    }
}
