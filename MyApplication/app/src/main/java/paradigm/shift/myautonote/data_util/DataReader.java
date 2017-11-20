package paradigm.shift.myautonote.data_util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.File;

/**
 * Utility class to parse a json data file and provide access to data in it.
 * Created by aravind on 11/18/17.
 */

public class DataReader implements DataChangedListener {

    private static final String OUR_ROOT_DIR_NAME = "Home";

    private boolean myIsInvalidated = true;
    private Directory myTopDir;
    private Context myContext;

    private static DataReader myInstance;

    /**
     * Private constructer; use static initializer to get an instance.
     * This keeps the class a Singleton.
     * @param context
     */
    private DataReader(final Context context) {
        myContext = context;
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

    /**
     * Build the directory structure from our stored data.
     */
    private Directory buildDirStructure() throws IOException, JSONException {
        // Read the file and parse it.
        InputStream iStream = myContext.getAssets().open("test_data.json"); // TODO: replace with stored file.
        int size = iStream.available();
        byte[] buffer = new byte[size];
        iStream.read(buffer);
        iStream.close();
        String jsonData = new String(buffer, "UTF-8");

        // TODO: parse the data into dir structure.
        JSONObject jFiles = new JSONObject(jsonData).getJSONObject("files");
        return buildDirectory(OUR_ROOT_DIR_NAME, jFiles, null);
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

    @Override
    public void onDataChanged() {
        myIsInvalidated = true;
    }
}
