package paradigm.shift.myautonote;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.metadata.TrashEntry;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.DataWriter;
import paradigm.shift.myautonote.data_util.MetadataDb;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrashFragment extends Fragment implements AdapterView.OnItemLongClickListener {

    private Executor myExecutor = Executors.newSingleThreadExecutor();
    private Handler myHandler = new Handler();
    private TrashEntry[] myData;
    private ListView myListView;
    private TextView myTextView;
    private ArrayAdapter<TrashEntry> myAdapter;

    public TrashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trash, container, false);
        myListView = rootView.findViewById(R.id.list_trash);
        myTextView = rootView.findViewById(R.id.text_nothing);
        myExecutor.execute(fetchData);
        return rootView;
    }

    private Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            myData = MetadataDb.getInstance(getContext()).metadataDao().loadAllTrashEntries();
            myHandler.post(populateData);
        }
    };

    private Runnable populateData = new Runnable() {
        @Override
        public void run() {
            if (myData.length > 0) {
                myTextView.setVisibility(View.GONE);
                myListView.setVisibility(View.VISIBLE);
                myAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_trash, myData);
                myListView.setAdapter(myAdapter);
                myListView.setOnItemLongClickListener(TrashFragment.this);
            } else {
                myTextView.setVisibility(View.VISIBLE);
                myListView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final TrashEntry entry = myData[position];
        AlertDialog.Builder bldr = new AlertDialog.Builder(getContext());
        bldr.setMessage(entry.fullName)
                .setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restoreItem(entry, getContext(), myExecutor, onRestore, myHandler);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(entry);
                    }
                }).show();
        return true;
    }

    private void deleteItem(final TrashEntry entry) {
        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final MetadataDb db = MetadataDb.getInstance(getContext());
                db.metadataDao().deleteTrashEntry(entry);
                fetchData.run();
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(myListView, "Item deleted", BaseTransientBottomBar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                db.metadataDao().insertTrashEntry(entry);
                                                fetchData.run();
                                            }
                                        });
                                    }
                                })
                                .show();
                    }
                });
            }
        });
    }

    private Runnable onRestore = new Runnable() {
        @Override
        public void run() {
            fetchData.run();
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(myListView, "Item restored", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            });
        }
    };

    public static void restoreItem(final TrashEntry entry, final Context context, final Executor executor, final Runnable onRestore, final Handler handler) {
        Log.d("Trash", entry.fullName);
        Log.d("Trash", entry.data);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                MetadataDb.getInstance(context).metadataDao().deleteTrashEntry(entry);
                final String[] dirParts = entry.fullName.split("/");
                final List<Directory> dir = new ArrayList<>(dirParts.length - 1);
                Directory curDir = DataReader.getInstance(context).getTopDir();
                dir.add(curDir);
                for (int i = 1; i < dirParts.length-1; i++) {
                    curDir = curDir.getSubDirectory(dirParts[i]);
                    if (curDir == null) {
                        // TODO create directory instead.
                        return;
                    }
                    dir.add(curDir);
                }
                JSONObject trashObj = null;
                try {
                    trashObj = new JSONObject(entry.data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JSONObject finalTrashObj = trashObj;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (finalTrashObj != null) {
                                DataWriter.getInstance(context).addFolder(dir, dirParts[dirParts.length - 1], finalTrashObj);
                            } else {
                                DataWriter.getInstance(context).addFile(dir, dirParts[dirParts.length - 1], entry.data);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });



                if (onRestore != null) {
                    onRestore.run();
                }
            }
        });
    }
}
