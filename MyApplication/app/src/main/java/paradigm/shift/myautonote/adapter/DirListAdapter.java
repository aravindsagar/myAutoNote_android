package paradigm.shift.myautonote.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import paradigm.shift.myautonote.R;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_util.DataReader;

/**
 * Created by aravind on 11/19/17.
 */

public class DirListAdapter extends BaseAdapter implements TextView.OnEditorActionListener, View.OnFocusChangeListener {

    private static class ViewHolder {

        ImageView imageView;
        TextView textView;
        EditText editText;

        public ViewHolder(ImageView imageView, TextView textView, EditText editText) {
            this.imageView = imageView;
            this.textView = textView;
            this.editText = editText;
        }
    }

    private Directory myTopDir;
    private Directory myCurDir;
    private List<Directory> myCurPath;
    private List<String> myDirs;
    private List<String> myFiles;
    private final LayoutInflater myInflater;
    private final Context myContext;
    private int myEditablePosition = -1;
    private EditFinishedListener myEditFinishedListener;

    public DirListAdapter(final Context context) {
        myTopDir = DataReader.getInstance(context).getTopDir();
        setCurDir(myTopDir);
        myCurPath = new ArrayList<>();
        myCurPath.add(myTopDir);
        myInflater = LayoutInflater.from(context);
        myContext = context;
    }

    private void setCurDir(final Directory curDir) {
        myCurDir = curDir;
        myDirs = curDir.getSubdirectoryNames();
        myFiles = curDir.getFileNames();
        notifyDataSetChanged();
    }

    public void setCurDir(final Directory curDir, final List<Directory> curPath) {
        myCurPath = curPath;
        setCurDir(curDir);
    }

    /**
     * Called when data is changed on disk. We refresh the data in memory and the list view.
     */
    public void refreshTopDir() {
        myTopDir = DataReader.getInstance(myContext).getTopDir();
        Directory curDir = myTopDir;
        List<Directory> newCurPath = new ArrayList<>(myCurPath.size());
        newCurPath.add(myTopDir);
        int i = 1;
        for (; i < myCurPath.size(); i++) {
            Directory newCurDir = curDir.getSubDirectory(myCurPath.get(i).getName());
            if (newCurDir != null) {
                curDir = newCurDir;
                newCurPath.add(newCurDir);
            } else {
                break;
            }
        }
        setCurDir(curDir, newCurPath);
    }

    public List<String> getDirs() {
        return myDirs;
    }

    public List<String> getMyFiles() {
        return myFiles;
    }

    public Directory getCurDir() {
        return myCurDir;
    }

    @Override
    public int getCount() {
        return myDirs.size() + myFiles.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < myDirs.size()) {
            return myCurDir.getSubDirectory(myDirs.get(position));
        }
        return myCurDir.getFile(myFiles.get(position - myDirs.size()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create or reuse the layout objects as required.
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.list_item_dir_list, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder == null) {
            holder = new ViewHolder((ImageView) convertView.findViewById(R.id.dir_list_img),
                    (TextView) convertView.findViewById(R.id.dir_list_name),
                    (EditText) convertView.findViewById(R.id.dir_list_name_edit));
        }

        // Get the list item for current position.
        String item;
        if (position < myDirs.size()) {
            item = myDirs.get(position);
            holder.imageView.setImageResource(R.drawable.ic_folder);
        } else {
            item = myFiles.get(position - myDirs.size());
            holder.imageView.setImageResource(R.drawable.ic_file);
        }

        if (position == myEditablePosition) {
            holder.textView.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setText(item);
            holder.editText.setOnEditorActionListener(this);
            holder.editText.setOnFocusChangeListener(this);
            holder.editText.setSelectAllOnFocus(true);
            holder.editText.requestFocus();
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            holder.editText.setVisibility(View.GONE);
            holder.textView.setText(item);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * Navigates to the parent directory of current directory.
     * Returns true if we have a parent dir, false otherwise.
     */
    public boolean goBack() {
        if (myCurDir.getParent() != null) {
            setCurDir(myCurDir.getParent());
            myCurPath.remove(myCurPath.size()-1);
            return true;
        }
        return false;
    }

    public void itemClick(final int position) {
        if (position == myEditablePosition) {
            return;
        }

        final DataItem item = (DataItem) getItem(position);
        if (item instanceof Directory) {
            setCurDir((Directory) item);
            myCurPath.add((Directory) item);
        } else {
            //TODO: intent to open note edit activity.
        }
    }

    /**
     * Set a particular position editable.
     */
    public void setEditable(int position, EditFinishedListener listener) {
        myEditablePosition = position;
        myEditFinishedListener = listener;
    }

    public boolean isInTopDir() {
        return myCurDir == myTopDir;
    }

    public List<Directory> getCurPath() {
        return myCurPath;
    }

    public String[] getCurPathStr() {
        String[] curPath = new String[myCurPath.size()];
        int i = 0;
        for (Directory d : myCurPath) {
            curPath[i] = d.getName();
            i++;
        }
        return curPath;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            Log.d("Dir list adapter", "On editor action");
            if (myEditFinishedListener != null) {
                Log.d("Dir list adapter", "Calling listener with newText " + ((TextView) v).getText().toString());
                myEditFinishedListener.onEditFinished(myEditablePosition, ((TextView) v).getText().toString());
            }
            myEditablePosition = -1;
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            Log.d("Dir list adapter", "has focus");
//            ((Activity) myContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            Log.d("Dir list adapter", "lost focus");

        }
    }
}
