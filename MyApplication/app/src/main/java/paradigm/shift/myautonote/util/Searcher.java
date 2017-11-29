package paradigm.shift.myautonote.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.SearchResult;
import paradigm.shift.myautonote.data_util.DataReader;

/**
 * Utility class to search all our data for a given query.
 * Created by aravind on 11/29/17.
 */

public class Searcher {
    public static List<SearchResult> getSearchResults(String query, Context context) {

        // BFS.
        Directory topDir = DataReader.getInstance(context).getTopDir();
        Queue<List<DataItem>> searchQ = new LinkedList<>();
        List<DataItem> startList = new ArrayList<>();
        startList.add(topDir);
        searchQ.add(startList);
        List<SearchResult> results = new ArrayList<>();
        while(!searchQ.isEmpty()) {
            List<DataItem> curItemPath = searchQ.remove();
            DataItem curItem = curItemPath.get(curItemPath.size()-1);
            SearchResult r = curItem.search(curItemPath, query);
            if (r != null) {
                results.add(r);
            }
            if (curItem instanceof Directory) {
                Directory curDir = (Directory) curItem;
                for (String d : curDir.getSubdirectoryNames()) {
                    List<DataItem> subDirPath = new ArrayList<>(curItemPath);
                    subDirPath.add(curDir.getSubDirectory(d));
                    searchQ.add(subDirPath);
                }
                for (String f : curDir.getFileNames()) {
                    List<DataItem> filePath = new ArrayList<>(curItemPath);
                    filePath.add(curDir.getFile(f));
                    searchQ.add(filePath);
                }
            }
        }
        return results;
    }
}
