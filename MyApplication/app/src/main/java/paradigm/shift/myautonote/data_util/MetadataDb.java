package paradigm.shift.myautonote.data_util;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import paradigm.shift.myautonote.data_model.metadata.NoteCreationTime;
import paradigm.shift.myautonote.data_model.metadata.TrashEntry;

/**
 * Created by aravind on 12/5/17.
 */

@Database(entities = {NoteCreationTime.class, TrashEntry.class}, version = 1)
public abstract class MetadataDb extends RoomDatabase {
    public static final String DB_NAME = "metadata";

    public static MetadataDb getInstance(Context context) {
        return Room.databaseBuilder(context, MetadataDb.class, DB_NAME).build();
    }

    public abstract MetadataDao metadataDao();
}
