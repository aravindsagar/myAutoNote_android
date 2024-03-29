package paradigm.shift.myautonote.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * List adapter which presents the app's directory structure to the user.
 * Created by aravind on 11/19/17.
 */

public class DirListAdapter extends BaseAdapter implements TextView.OnEditorActionListener, View.OnFocusChangeListener, TextWatcher {

    private static class ViewHolder {

        ImageView imageView;
        TextView textView;
        EditText editText;

        ViewHolder(ImageView imageView, TextView textView, EditText editText) {
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
    private Handler myHandler;
    private EditText myEditableField;
    private int myEditableFocusCount = 0;
    private boolean myShouldDimFiles = false;

    public DirListAdapter(final Context context) {
        // Set the top directory as the current directory.
        myTopDir = DataReader.getInstance(context).getTopDir();
        List<Directory> curPath = new ArrayList<>();
        curPath.add(myTopDir);
        setCurDir(curPath);

        myInflater = LayoutInflater.from(context);
        myContext = context;
        myHandler = new Handler();
    }

    public void setCurDir(final List<Directory> curPath) {
        myCurPath = curPath;
        myCurDir = curPath.get(curPath.size()-1);
        myDirs = myCurDir.getSubdirectoryNames();
        myFiles = myCurDir.getFileNames();
        notifyDataSetChanged();
    }

    /**
     * Called when data is changed on disk. We refresh the data in memory and the list view.
     */
    public void refreshTopDir() {
        myTopDir = DataReader.getInstance(myContext).getTopDir();
        Directory curDir = myTopDir;
        List<Directory> newCurPath = new ArrayList<>(myCurPath.size());
        newCurPath.add(myTopDir);
        for (int i = 1; i < myCurPath.size(); i++) {
            Directory newCurDir = curDir.getSubDirectory(myCurPath.get(i).getName());
            if (newCurDir != null) {
                curDir = newCurDir;
                newCurPath.add(newCurDir);
            } else {
                break;
            }
        }
        setCurDir(newCurPath);
    }

    public List<String> getDirs() {
        return myDirs;
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
            convertView.setAlpha(1.0f);
        } else {
            item = myFiles.get(position - myDirs.size());
            holder.imageView.setImageResource(R.drawable.ic_file);
            if (myShouldDimFiles) {
                convertView.setAlpha(0.5f);
            } else {
                convertView.setAlpha(1.0f);
            }
        }

        if (position == myEditablePosition) {
            holder.textView.setVisibility(View.GONE);
            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.setText(item);
            holder.editText.setOnEditorActionListener(this);
            holder.editText.setOnFocusChangeListener(this);
            holder.editText.addTextChangedListener(this);
            myEditableField = holder.editText;
            myEditableFocusCount += 1;
            if (myEditableFocusCount <= 5) {
                final ViewHolder finalHolder = holder;
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finalHolder.editText.setSelectAllOnFocus(true);
                        finalHolder.editText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) myContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(finalHolder.editText, 0);
                        }
                        finalHolder.editText.setSelection(0, finalHolder.editText.getText().length());
                    }
                }, 300);
            }
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
        if (myEditablePosition != -1 && myEditableField != null) {
            return onEditorAction(myEditableField, EditorInfo.IME_ACTION_DONE, null);
        }
        if (myCurPath.size() > 1) {
            myCurPath.remove(myCurPath.size()-1);
            setCurDir(myCurPath);
            return true;
        }
        return false;
    }

    public void itemClick(final int position) {
        if (position == myEditablePosition) {
            onEditorAction(myEditableField, EditorInfo.IME_ACTION_DONE, null);
            return;
        }

        final DataItem item = (DataItem) getItem(position);
        if (item instanceof Directory) {
            myCurPath.add((Directory) item);
            setCurDir(myCurPath);
        } // Opening a note is taken care of by MyNotes activity.
    }

    /**
     * Set a particular position editable.
     */
    public void setEditable(int position, EditFinishedListener listener) {
        myEditablePosition = position;
        myEditFinishedListener = listener;
        myEditableFocusCount = 0;
    }

    public boolean isInTopDir() {
        return myCurDir == myTopDir;
    }

    public List<Directory> getCurPath() {
        return myCurPath;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d("Dir list adapter", "On editor action");
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            if (myEditFinishedListener != null) {
                Log.d("Dir list adapter", "Calling listener with newText " + v.getText().toString());
                myEditFinishedListener.onEditFinished(myEditablePosition, v.getText().toString());
            }
            myEditablePosition = -1;
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            Log.d("Dir list adapter", "lost focus");
            String newText = ((TextView) v).getText().toString();
            setItemName(myEditablePosition, newText);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        setItemName(myEditablePosition, s.toString());
    }

    private void setItemName(int position, String value) {
        if (position >= 0) {
            if (position < myDirs.size()) {
                myDirs.set(position, value);
            } else {
                myFiles.set(position - myDirs.size(), value);
            }
        }
    }

    public void setShouldDimFiles(boolean shouldDimFiles) {
        this.myShouldDimFiles = shouldDimFiles;
    }
}
