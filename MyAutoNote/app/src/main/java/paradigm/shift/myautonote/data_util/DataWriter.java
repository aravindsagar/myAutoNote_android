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
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.metadata.NoteCreationTime;
import paradigm.shift.myautonote.data_model.metadata.TrashEntry;
import paradigm.shift.myautonote.util.MiscUtils;

/**
 * Class to write data into our data file in json format.
 * Created by aravind on 11/21/17.
 */

public class DataWriter {
    static final String DATA_FILE = "notes";

    private static DataWriter ourWriter;
    private final WeakReference<Context> myContext;
    private final Executor myExecutor = Executors.newSingleThreadExecutor();

    private DataWriter(Context context) {
        myContext = new WeakReference<>(context.getApplicationContext());
    }

    public static DataWriter getInstance(Context context) {
        if (ourWriter == null || ourWriter.myContext.get() == null) {
            ourWriter = new DataWriter(context);
        }
        return ourWriter;
    }

    /**
     * Initializes our data file. Does nothing if the file already exists.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void createDataFile() throws IOException {

        // Initialize with dummy data for testing.
        // TODO: make this an empty json file.
        InputStream iStream = myContext.get().getAssets().open("test_data.json");
        int size = iStream.available();
        byte[] buffer = new byte[size];
        iStream.read(buffer);
        iStream.close();
        String jsonData = new String(buffer, "UTF-8");

        File outFile = new File(myContext.get().getFilesDir(), DATA_FILE);
        if (outFile.createNewFile()) {
            FileOutputStream outputStream = new FileOutputStream(outFile);
            outputStream.write(jsonData.getBytes());
            outputStream.close();
        }

        // No need to notify data reader in this case, since it'll automatically read
        // once this method returns.
    }

    public void addFolder(final List<Directory> dir, final String newDirName) throws IOException, JSONException {
        addFolder(dir, newDirName, new JSONObject());
    }

    /**
     * Creates a new folder in the given destination.
     */
    public void addFolder(final List<Directory> dir, final String newDirName, final JSONObject contents) throws IOException, JSONException {
        Log.d("DataWriter", "Creating new folder " + newDirName);
        JSONObject jsonObject = DataReader.getInstance(myContext.get()).readDataFile();
        JSONObject jDir = traverse(jsonObject, dir);
        if (jDir == null) {
            throw new FileNotFoundException();
        }
        jDir.put(newDirName, contents);
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Renames the requested folder. If destination is null, deletes the folder.
     */
    public void editFolder(final List<Directory> dir, final String source, final String destination) throws IOException, JSONException {
        Log.d("DataWriter", "Renaming folder " + source);
        JSONObject jsonObject = DataReader.getInstance(myContext.get()).readDataFile();
        JSONObject jDir = jsonObject.getJSONObject("files");
        final StringBuilder dirStr = new StringBuilder(dir.get(0).getName() + "/");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
            dirStr.append(dir.get(i).getName());
            dirStr.append("/");
        }
        dirStr.append(source);

        final String srcDataStr = jDir.getString(source);

        if (destination != null) {
            jDir.put(destination, jDir.getJSONObject(source));
        }
        if (!source.equals(destination)) {
            if (destination == null) {
                // This is item deletion. Add it to trash.
                myExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        MetadataDb.getInstance(myContext.get()).metadataDao().insertTrashEntry(
                                new TrashEntry(dirStr.toString(), srcDataStr)
                        );
                    }
                });
            }
            jDir.remove(source);
        }
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Moves a data item from one folder to another.
     */
    public void moveItem(final String[] srcDir, final String[] dstDir, final String itemName) throws IOException, JSONException {
        Log.d("DataWriter", "Moving item" + itemName);
        if(Arrays.equals(srcDir, dstDir)) {
            return;
        }

        JSONObject jsonObject = DataReader.getInstance(myContext.get()).readDataFile();
        JSONObject jSrcDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < srcDir.length; i++) {
            jSrcDir = jSrcDir.getJSONObject(srcDir[i]);
            if (jSrcDir == null) {
                return;
            }
        }
        JSONObject jDstDir = jsonObject.getJSONObject("files");
        for (int i = 1; i < dstDir.length; i++) {
            jDstDir = jDstDir.getJSONObject(dstDir[i]);
            if (jDstDir == null) {
                return;
            }
        }

        JSONObject moveObj = null;
        String moveObjStr = null;
        try {
            moveObj = jSrcDir.getJSONObject(itemName);
        } catch (JSONException e) {
            moveObjStr = jSrcDir.getString(itemName);
        }
        if (moveObj == null && moveObjStr == null) {
            return;
        }

        if (moveObj != null) {
            jDstDir.put(itemName, moveObj);
        } else {
            jDstDir.put(itemName, moveObjStr);
        }

        jSrcDir.remove(itemName);
        writeData(jsonObject.toString());
        notifyDataReader();
    }

    /**
     * Creates a new file in the given destination.
     */
    public void addFile(final List<Directory> dir, final String newFileName, final String contents) throws IOException, JSONException {
        Log.d("DataWriter", "Creating new folder " + newFileName);
        JSONObject jsonObject = DataReader.getInstance(myContext.get()).readDataFile();
        JSONObject jDir = traverse(jsonObject, dir);
        if (jDir == null) {
            throw new FileNotFoundException();
        }
        jDir.put(newFileName, contents);
        writeData(jsonObject.toString());

        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                addMetaData(dir);
            }
        });

        notifyDataReader();
    }

    private void addMetaData(List<Directory> dir) {
        MetadataDb db = MetadataDb.getInstance(myContext.get());
        db.metadataDao().insertNoteCreationTime(new NoteCreationTime(
                MiscUtils.constructFullName(dir),
                MiscUtils.getDayOfTheWeek(),
                MiscUtils.getSecondsSinceMidnight()
        ));
    }

    /**
     * Edits the given file. If destination is not null and is different from source, the file will
     * be renamed.
     */
    public void editFile(final List<Directory> dir, final String source, final String destination, final String contents)
            throws IOException, JSONException {
        Log.d("DataWriter", "Editing file " + source);
        JSONObject jsonObject = DataReader.getInstance(myContext.get()).readDataFile();
        JSONObject jDir = traverse(jsonObject, dir);
        if (jDir == null) {
            throw new FileNotFoundException();
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
        File outFile = new File(myContext.get().getFilesDir(), DATA_FILE);
        FileOutputStream outputStream = new FileOutputStream(outFile);
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    private JSONObject traverse(JSONObject jObj, List<Directory> dir) throws JSONException {
        JSONObject jDir = jObj.getJSONObject("files");
        for (int i = 1; i < dir.size(); i++) {
            jDir = jDir.getJSONObject(dir.get(i).getName());
            if (jDir == null) {
                return null;
            }
        }
        return jDir;
    }

    private void notifyDataReader() {
        DataReader.getInstance(myContext.get()).invalidateData();
    }
}
