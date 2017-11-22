package paradigm.shift.myautonote.data_util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 11/21/17.
 */

public class DataWriter {
    public static final String DATA_FILE = "notes";

    private static DataWriter ourWriter;
    private final Context myContext;

    private DataWriter(Context context) {
        myContext = context;
    }

    public static DataWriter getInstance(Context context) {
        if (ourWriter == null) {
            ourWriter = new DataWriter(context);
        }
        return ourWriter;
    }

    /**
     * Initializes our data file. Does nothing if the file already exists.
     * @return
     */
    public void createDataFile() throws IOException {

        // Initialize with dummy data for testing.
        // TODO: make this an empty json file.
        InputStream iStream = myContext.getAssets().open("test_data.json");
        int size = iStream.available();
        byte[] buffer = new byte[size];
        iStream.read(buffer);
        iStream.close();
        String jsonData = new String(buffer, "UTF-8");

        File outFile = new File(myContext.getFilesDir(), DATA_FILE);
        if (outFile.createNewFile()) {
            FileOutputStream outputStream = new FileOutputStream(outFile);
            outputStream.write(jsonData.getBytes());
            outputStream.close();
        }

        // No need to notify data reader in this case, since it'll automatically read
        // once this method returns.
    }

    private void notifyDataReader() {
        DataReader.getInstance(myContext).invalidateData();
    }
}
