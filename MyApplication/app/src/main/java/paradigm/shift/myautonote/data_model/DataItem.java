package paradigm.shift.myautonote.data_model;

import java.util.List;

/**
 * A generic data item. This can represent a directory or a file.
 * Created by aravind on 11/18/17.
 */

public abstract class DataItem {

    protected final String myName;
    protected final List<String> myPathFromRoot;

    protected DataItem(final String name, final List<String> pathFromRoot) {
        myName = name;
        myPathFromRoot = pathFromRoot;
    }

    public String getName() {
        return myName;
    }

    public List<String> getPathFromRoot() {
        return myPathFromRoot;
    }
}
