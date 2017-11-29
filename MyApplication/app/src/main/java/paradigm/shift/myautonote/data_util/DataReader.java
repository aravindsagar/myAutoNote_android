package paradigm.shift.myautonote.data_util;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.File;

import static paradigm.shift.myautonote.data_util.DataWriter.DATA_FILE;

/**
 * Utility class to parse a json data file and provide access to data in it.
 * Created by aravind on 11/18/17.
 */

public class DataReader {

    private static final String OUR_ROOT_DIR_NAME = "Home";

    private boolean myIsInvalidated = true;
    private Directory myTopDir;
    private Context myContext;
    private List<DataChangedListener> myListeners;

    private static DataReader myInstance;

    /**
     * Private constructer; use static initializer to get an instance.
     * This keeps the class a Singleton.
     * @param context
     */
    private DataReader(final Context context) {
        myContext = context;
        myListeners = new ArrayList<>(2);
    }

    /**
     * Gets an instance of DataReader. Note that this is a singleton class and the same instance
     * will be shared across all who request it.
     */
    public static DataReader getInstance(final Context context) {
        if (myInstance == null) {
            myInstance = new DataReader(context);
        }
        return myInstance;
    }

    /**
     * Get A Directory object representing the root directory of our data.
     * @return
     */
    public Directory getTopDir() {
        if (myIsInvalidated || myTopDir == null) {
            try {
                myTopDir = buildDirStructure();
                myIsInvalidated = false;
            } catch (IOException|JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return myTopDir;
    }

    public void registerListener(DataChangedListener listener) {
        myListeners.add(listener);
    }

    /**
     * Returns a JSONObject containing data inside our data file.
     */
    JSONObject readDataFile(java.io.File dataFile) throws IOException, JSONException {

        FileInputStream iStream = new FileInputStream(dataFile);
        int size = iStream.available();
        byte[] buffer = new byte[size];
        iStream.read(buffer);
        iStream.close();
        String jsonData = new String(buffer, "UTF-8");
        return new JSONObject(jsonData);
    }

    /**
     * Build the directory structure from our stored data.
     */
    private Directory buildDirStructure() throws IOException, JSONException {
        // Read the data file and parse it.
        java.io.File dataFile = new java.io.File(myContext.getFilesDir(), DATA_FILE);
        if (!dataFile.exists()) {
            // Data file not found, create a new one.
            Log.d("DataReader", "Data file not found, creating a new file.");
            DataWriter.getInstance(myContext).createDataFile();

            // The file should exist now.
            return buildDirStructure();
        }
        return buildDirectory(OUR_ROOT_DIR_NAME, readDataFile(dataFile).getJSONObject("files"), null);
    }

    /**
     * Returns the directory structure represented by given json object in the form of a Directory object.
     * @param jObj
     * @return
     */
    private static Directory buildDirectory(final String name, final JSONObject jObj, final Directory parent) {
        Map<String, Directory> childDirs = new HashMap<>();
        Map<String, File> childFiles = new HashMap<>();
        Directory curDir = new Directory(name, parent, childDirs, childFiles);

        Iterator<String> children = jObj.keys();
        while (children.hasNext()) {
            String child = children.next();
            JSONObject childObj = jObj.optJSONObject(child);

            if (childObj != null) {
                // The child is a directory, build it recursively.
                childDirs.put(child, buildDirectory(child, childObj, curDir));
            } else {
                // This child is a file. Add it to the file list.
                childFiles.put(child, new File(child, curDir, jObj.optString(child)));
            }
        }

        return curDir;
    }

    /**
     * Called by DataWriter when it has written some data.
     */
    void invalidateData() {
        myIsInvalidated = true;
        for (DataChangedListener l : myListeners) {
            if (l != null) {
                l.onDataChanged();
            }
        }
    }
}
