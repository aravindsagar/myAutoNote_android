package paradigm.shift.myautonote.data_model;

import java.util.List;

/**
 * A generic data item. This can represent a directory or a file.
 * Created by aravind on 11/18/17.
 */

public abstract class DataItem {

    protected final String myName;

    protected final Directory myParent;

    protected DataItem(final String name, final Directory parent) {
        myName = name;
        myParent = parent;
    }

    public String getName() {
        return myName;
    }

    public Directory getParent() {
        return myParent;
    }
}
