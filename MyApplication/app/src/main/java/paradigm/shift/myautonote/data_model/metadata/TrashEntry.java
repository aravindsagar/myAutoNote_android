package paradigm.shift.myautonote.data_model.metadata;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by aravind on 12/6/17.
 */

@Entity
public class TrashEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;

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
