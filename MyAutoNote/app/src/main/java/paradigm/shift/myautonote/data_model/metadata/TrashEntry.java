package paradigm.shift.myautonote.data_model.metadata;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Entity representing an item that has been moved to trash.
 *
 * Created by aravind on 12/6/17.
 */

@Entity
public class TrashEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Full path to the item that has been deleted.
     */
    public String fullName;

    /**
     * JSON string containing the data inside the deleted entry. Can represent items inside a
     * folder, or note contents itself.
     */
    public String data;

    public TrashEntry(String fullName, String data) {
        this.fullName = fullName;
        this.data = data;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
