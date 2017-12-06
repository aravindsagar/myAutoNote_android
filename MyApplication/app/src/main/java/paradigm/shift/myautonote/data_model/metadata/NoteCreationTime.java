package paradigm.shift.myautonote.data_model.metadata;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Entity to keep track of new note creation times inside a folder. This data will be used to
 * generate new note suggestions.
 *
 * Created by aravind on 12/5/17.
 */

@Entity
public class NoteCreationTime {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemFullName;
    // 1-7
    public int dayOfWeek;

    // Seconds from 00:00.
    public int timeOfDay;

    public NoteCreationTime(String itemFullName, int dayOfWeek, int timeOfDay) {
        this.itemFullName = itemFullName;
        this.dayOfWeek = dayOfWeek;
        this.timeOfDay = timeOfDay;
    }
}
