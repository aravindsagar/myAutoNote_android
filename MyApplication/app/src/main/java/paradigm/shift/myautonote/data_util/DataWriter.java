package paradigm.shift.myautonote.data_util;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import paradigm.shift.myautonote.data_model.Directory;

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

    /**
     * Creates a new folder in the given destination.
     */
    public void addFolder(final List<Directory> dir, final String newDirName) throws IOException, JSONException {
        Log.d("DataWriter", "Creating new folder " + newDirName);
        File dataFile = new File(myContext.getFilesDir(), DATA_FILE);
        JSONObject jsonObject = DataReader.getInstance(myContext).readDataFile(dataFile);
        JSONObject jDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
        }
        jDir.put(newDirName, new JSONObject());
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Renames the requested folder. If destination is null, deletes the folder.
     */
    public void editFolder(final List<Directory> dir, final String source, final String destination) throws IOException, JSONException {
        Log.d("DataWriter", "Renaming folder " + source);
        File dataFile = new File(myContext.getFilesDir(), DATA_FILE);
        JSONObject jsonObject = DataReader.getInstance(myContext).readDataFile(dataFile);
        JSONObject jDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
        }

        if (destination != null) {
            jDir.put(destination, jDir.getJSONObject(source));
        }
        if (!source.equals(destination)) {
            jDir.remove(source);
        }
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Creates a new file in the given destination.
     */
    public void addFile(final List<Directory> dir, final String newFileName, final String contents) throws IOException, JSONException {
        Log.d("DataWriter", "Creating new folder " + newFileName);
        File dataFile = new File(myContext.getFilesDir(), DATA_FILE);
        JSONObject jsonObject = DataReader.getInstance(myContext).readDataFile(dataFile);
        JSONObject jDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
        }
        jDir.put(newFileName, contents);
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Edits the given file. If destination is not null and is different from source, the file will
     * be renamed.
     */
    public void editFile(final List<Directory> dir, final String source, final String destination, final String contents)
            throws IOException, JSONException {
        Log.d("DataWriter", "Renaming folder " + source);
        File dataFile = new File(myContext.getFilesDir(), DATA_FILE);
        JSONObject jsonObject = DataReader.getInstance(myContext).readDataFile(dataFile);
        JSONObject jDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
        }

        if (destination != null) {
            jDir.put(destination, contents);
            if (!source.equals(destination)) {
                jDir.remove(source);
            }
        } else {
            jDir.put(source, contents);
        }
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    private void writeData(String data) throws IOException {
        File outFile = new File(myContext.getFilesDir(), DATA_FILE);
        FileOutputStream outputStream = new FileOutputStream(outFile);
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    private void notifyDataReader() {
        DataReader.getInstance(myContext).invalidateData();
    }
}
