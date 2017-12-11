package paradigm.shift.myautonote;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

import paradigm.shift.myautonote.adapter.DirListAdapter;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_util.DataWriter;
import paradigm.shift.myautonote.util.MiscUtils;

/**
 * Dialog fragment used to move items to another folder.
 * Created by aravind on 12/6/17.
 */

public class MoveItemDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, AdapterView.OnItemClickListener {
    private static final String ITEM_DIR = "item_dir";
    private static final String ITEM_NAME = "item_name";

    private ListView myDirList;
    private DirListAdapter myDirListAdapter;

    public static MoveItemDialogFragment getInstance(String[] itemDir, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(ITEM_DIR, itemDir);
        bundle.putString(ITEM_NAME, itemName);

        MoveItemDialogFragment fragment = new MoveItemDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public MoveItemDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = View.inflate(getContext(), R.layout.dialog_move_item, null);
        TextView tv = dialogView.findViewById(R.id.dialog_title);
        tv.setText(String.format(getString(R.string.move_item), getArguments().getString(ITEM_NAME)));
        myDirList = dialogView.findViewById(R.id.dir_list_dialog);
        myDirListAdapter = new DirListAdapter(getContext());
        myDirListAdapter.setShouldDimFiles(true);
        myDirList.setAdapter(myDirListAdapter);
        myDirList.setOnItemClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setPositiveButton("Move here", this)
                .setCancelable(true);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.d("Dialog", "onCLick");
        if (which == DialogInterface.BUTTON_POSITIVE) {
            try {
                Log.d("Dialog", "Moving");
                DataWriter.getInstance(getContext()).moveItem(
                        getArguments().getStringArray(ITEM_DIR),
                        MiscUtils.getCurPathStr(myDirListAdapter.getCurPath()),
                        getArguments().getString(ITEM_NAME)
                );
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar.make(myDirList, "Error moving item", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final DataItem item = (DataItem) myDirListAdapter.getItem(position);
        if (item instanceof Directory) {
            myDirListAdapter.itemClick(position);
            if (Arrays.equals(MiscUtils.getCurPathStr(myDirListAdapter.getCurPath()), getArguments().getStringArray(ITEM_DIR))){
                myDirListAdapter.getDirs().remove(getArguments().getString(ITEM_NAME));
                myDirListAdapter.notifyDataSetChanged();
            }
        }
    }
}
