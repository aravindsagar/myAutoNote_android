package paradigm.shift.myautonote.data_model;

import java.util.List;

/**
 * Created by aravind on 11/29/17.
 */

public class SearchResult {
    private final List<DataItem> myItemPath;
    private final String myMatchingText;
    private final boolean myIsResultDir;

    public SearchResult(List<DataItem> itemPath, String matchingText, boolean isResultDir) {
        this.myItemPath = itemPath;
        this.myMatchingText = matchingText;
        this.myIsResultDir = isResultDir;
    }

    public List<DataItem> getItemPath() {
        return myItemPath;
    }

    public String getMatchingText() {
        return myMatchingText;
    }

    public boolean isResultDir() {
        return myIsResultDir;
    }
}
