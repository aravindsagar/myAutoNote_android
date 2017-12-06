package paradigm.shift.myautonote.data_util;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import paradigm.shift.myautonote.data_model.metadata.NoteCreationTime;

/**
 * Created by aravind on 12/5/17.
 */

@Dao
public interface MetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNoteCreationTime(NoteCreationTime noteCreationTime);

    @Query("SELECT * FROM NoteCreationTime")
    NoteCreationTime[] loadAllNoteCreationTimes();
}
