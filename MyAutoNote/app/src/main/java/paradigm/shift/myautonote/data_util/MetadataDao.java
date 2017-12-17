package paradigm.shift.myautonote.data_util;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import paradigm.shift.myautonote.data_model.metadata.NoteCreationTime;
import paradigm.shift.myautonote.data_model.metadata.TrashEntry;

/**
 * Data access object for Metadata database.
 *
 * Created by aravind on 12/5/17.
 */

@SuppressWarnings("UnusedReturnValue")
@Dao
public interface MetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNoteCreationTime(NoteCreationTime noteCreationTime);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTrashEntry(TrashEntry trashEntry);

    @Query("SELECT * FROM NoteCreationTime")
    NoteCreationTime[] loadAllNoteCreationTimes();

    @Query("SELECT * FROM TrashEntry")
    TrashEntry[] loadAllTrashEntries();

    @Query("SELECT * FROM TrashEntry WHERE fullName = (:fullName) LIMIT 1")
    TrashEntry loadTrashEntry(String fullName);

    @Delete
    int deleteTrashEntry(TrashEntry trashEntry);
}
