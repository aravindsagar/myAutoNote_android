package paradigm.shift.myautonote.adapter;

import java.util.List;

import paradigm.shift.myautonote.data_model.Directory;

/**
 * Created by aravind on 11/21/17.
 */

public interface CurPathItemClickListener {
    void onItemClick(final List<Directory> curPath);
}
