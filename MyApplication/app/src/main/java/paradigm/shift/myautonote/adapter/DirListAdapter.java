package paradigm.shift.myautonote.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import paradigm.shift.myautonote.R;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;

/**
 * Created by aravind on 11/19/17.
 */

public class DirListAdapter extends BaseAdapter {

    private static class ViewHolder {

        ImageView imageView;
        TextView textView;

        public ViewHolder(ImageView imageView, TextView textView) {
            this.imageView = imageView;
            this.textView = textView;
        }
    }

    private final Directory myTopDir;
    private Directory myCurDir;
    private List<Directory> myCurPath;
    private List<String> myDirs;
    private List<String> myFiles;
    private final LayoutInflater inflater;

    public DirListAdapter(final Context context, final Directory topDir){
        myTopDir = topDir;
        setCurDir(myTopDir);
        myCurPath = new ArrayList<>();
        myCurPath.add(myTopDir);
        inflater = LayoutInflater.from(context);
    }

    private void setCurDir(final Directory curDir) {
        myCurDir = curDir;
        myDirs = curDir.getSubdirectoryNames();
        myFiles = curDir.getFileNames();
        notifyDataSetChanged();
    }

    public void setCurDir(final Directory curDir, final List<Directory> curPath) {
        setCurDir(curDir);
        myCurPath = curPath;
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
            convertView = inflater.inflate(R.layout.list_item_dir_list, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder == null) {
            holder = new ViewHolder((ImageView) convertView.findViewById(R.id.dir_list_img),
                    (TextView) convertView.findViewById(R.id.dir_list_name));
        }

        // Get the list item for current position.
        final DataItem item = (DataItem) getItem(position);

        if (item instanceof Directory) {
            holder.imageView.setImageResource(R.drawable.ic_folder);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_file);
        }
        holder.textView.setText(item.getName());
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
        final DataItem item = (DataItem) getItem(position);
        if (item instanceof Directory) {
            setCurDir((Directory) item);
            myCurPath.add((Directory) item);
        } else {
            //TODO: intent to open note edit activity.
        }
    }

    public boolean isInTopDir() {
        return myCurDir == myTopDir;
    }

    public List<Directory> getCurPath() {
        return myCurPath;
    }
}
