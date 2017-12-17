package paradigm.shift.myautonote.adapter;

import java.util.List;

import paradigm.shift.myautonote.data_model.Directory;

/**
 * An interface for listening to item clicks on the cur path recycler view.
 * Created by aravind on 11/21/17.
 */

public interface CurPathItemClickListener {
    void onItemClick(final List<Directory> curPath);
}
