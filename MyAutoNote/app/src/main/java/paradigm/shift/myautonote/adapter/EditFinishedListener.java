package paradigm.shift.myautonote.adapter;

/**
 * Callback interface for notifying when an item name edit has completed in DirListAdapter.
 * This indicates that user has finished entering the name for a new folder, or renaming an existing
 * item.
 * Created by aravind on 11/22/17.
 */

public interface EditFinishedListener {
    void onEditFinished(int position, String newText);
}
