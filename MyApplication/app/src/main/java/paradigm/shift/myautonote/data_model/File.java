package paradigm.shift.myautonote.data_model;

import java.util.List;

/**
 * Created by aravind on 11/18/17.
 */

public class File extends DataItem {

    private final String myFileContents;
    private final static int MAX_SEARCH_LINE_LEN = 51;

    public File(final String name, final Directory parent, final String fileContents) {
        super(name, parent);
        this.myFileContents = fileContents;
    }

    public String getFileContents() {
        return myFileContents;
    }

    @Override
    public String toString() {
        return getFileContents();
    }

    public String toUnformattedString() {
        return getFileContents().replaceAll("</?(p|img)>", " ");
    }
    @Override
    public SearchResult search(List<DataItem> path, String query) {
        String contents = toUnformattedString();
        String lowerContents = contents.toLowerCase();
        if (lowerContents.contains(query)) {
            int idx = lowerContents.indexOf(query);
            int offset = (50 - query.length())/2;
            int sidx = 0, eidx = lowerContents.length();
            if (idx - offset > 0) {
                sidx = idx - offset;
            }
            if (idx + query.length() + offset <= lowerContents.length()) {
                eidx = idx + query.length() + offset;
            }
            return new SearchResult(path, contents.substring(sidx, eidx), false);
        }
        if (getName().toLowerCase().contains(query)) {
            return new SearchResult(path, null, false);
        }
        return null;
    }
}
