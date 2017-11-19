package paradigm.shift.myautonote.data_utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import paradigm.shift.myautonote.data_model.Directory;

/**
 * Utility class to parse a json data file and provide access to data in it.
 * Created by aravind on 11/18/17.
 */

public class DataReader {

    private static final String ourRootDirName = "Home";

    private boolean myIsInvalidated = true;
    private Directory myTopDir;
    private Context myContext;

    public DataReader(final Context context) {
        myContext = context;
    }

    public Directory getMyTopDir() {
        if (myIsInvalidated || myTopDir == null) {
            try {
                buildDirStructure();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return myTopDir;
    }

    private Directory buildDirStructure() throws IOException {
        // Read the file and parse it.
        InputStream iStream = myContext.getAssets().open("test_data.json"); // TODO: replace with stored file.
        int size = iStream.available();
        byte[] buffer = new byte[size];
        iStream.read(buffer);
        iStream.close();
        String jsonData = new String(buffer, "UTF-8");

        // TODO: parse the data into dir structure.
        return new Directory(ourRootDirName);
    }
}
