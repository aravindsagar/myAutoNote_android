package paradigm.shift.myautonote;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import paradigm.shift.myautonote.adapter.CurPathAdapter;
import paradigm.shift.myautonote.adapter.CurPathItemClickListener;
import paradigm.shift.myautonote.adapter.DirListAdapter;
import paradigm.shift.myautonote.adapter.EditFinishedListener;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.File;
import paradigm.shift.myautonote.data_model.metadata.TrashEntry;
import paradigm.shift.myautonote.data_util.DataChangedListener;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.DataWriter;
import paradigm.shift.myautonote.data_util.MetadataDb;
import paradigm.shift.myautonote.util.MiscUtils;
import paradigm.shift.myautonote.util.NewNoteSuggestionsGenerator;

import static paradigm.shift.myautonote.WorkActivity.CUR_DIR;
import static paradigm.shift.myautonote.WorkActivity.NOTE_TITLE;
import static paradigm.shift.myautonote.util.NewNoteSuggestionsGenerator.NUM_SUGGESTIONS;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyNotesFragment extends Fragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, CurPathItemClickListener, View.OnClickListener,
        DataChangedListener, EditFinishedListener {

    private static final String NEW_NOTE_CONTENTS = "";
    private enum State {
        NORMAL, CREATING, RENAMING
    }

    private View myRootView;
    private ListView myDirList;
    private DirListAdapter myDirListAdapter;
    private LinearLayout mySuggestionsLayout;
    private RecyclerView myCurPathView;
    private CurPathAdapter myCurPathAdapter;
    private State myState = State.NORMAL;
    private int myEditPosition = -1;
    private String myLastLongpressName;
    private Dialog myBottomDialog;
    private Handler myHandler;
    private CoordinatorLayout myBottomBarCoordinatorLayout;
    private Executor myExecutor = Executors.newSingleThreadExecutor();

    public MyNotesFragment() {
        // Required empty public constructor
    }

    public static MyNotesFragment getInstance(String[] curDir) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(CUR_DIR, curDir);
        MyNotesFragment fragobj = new MyNotesFragment();
        fragobj.setArguments(bundle);
        return fragobj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myRootView = inflater.inflate(R.layout.fragment_my_notes, container, false);

        myHandler = new Handler();
        setupViews();

        String[] curDir = getArguments().getStringArray(CUR_DIR);
        setCurDir(curDir);

        myExecutor.execute(setupSuggestions);

        return myRootView;
    }

    private void setupViews() {
        DataReader.getInstance(getActivity()).registerListener(this);

        myDirList = findViewById(R.id.list_view_dir_list);
        myDirListAdapter = new DirListAdapter(getActivity());
        myDirList.setAdapter(myDirListAdapter);
        myDirList.setOnItemClickListener(this);
        myDirList.setOnItemLongClickListener(this);

        mySuggestionsLayout = findViewById(R.id.suggestion_layout);
        myCurPathView = findViewById(R.id.cur_path_view);
        myCurPathAdapter = new CurPathAdapter(myDirListAdapter.getCurPath(), this);
        myCurPathView.setAdapter(myCurPathAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.ic_chevron_right_black_24dp));
        myCurPathView.setLayoutManager(mLayoutManager);
        myCurPathView.addItemDecoration(dividerItemDecoration);

        Button newFolderBtn, newNoteBtn;
        newFolderBtn = findViewById(R.id.btn_new_folder);
        newNoteBtn = findViewById(R.id.btn_new_note);
        newFolderBtn.setOnClickListener(this);
        newNoteBtn.setOnClickListener(this);

        myBottomBarCoordinatorLayout = findViewById(R.id.coordinator_bottom_bar);
    }

    private void setCurDir(String[] curDir) {
        if (curDir == null) {
            return;
        }

        List<Directory> dir = MiscUtils.getCurPathList(getContext(), curDir);
        if (dir == null) {
            return;
        }
        Log.d("MyNotes", "Setting cur dir");
        myDirListAdapter.setCurDir(dir);
        setCurDirPathView();
    }

    private Runnable setupSuggestions = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }
            final List<List<Directory>> suggestions = NewNoteSuggestionsGenerator.generateSuggestions(getActivity());

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (suggestions.size() < NUM_SUGGESTIONS) {
                        mySuggestionsLayout.setVisibility(View.GONE);
                        return;
                    }
                    final Button[] suggestionBtns = new Button[] {
                            findViewById(R.id.btn_suggestion_1),
                            findViewById(R.id.btn_suggestion_2),
                            findViewById(R.id.btn_suggestion_3)
                    };

                    for (int i = 0; i < NUM_SUGGESTIONS; i++) {
                        final List<Directory> suggestion = suggestions.get(i);
                        // No need to show "Home/" in suggestions.
                        if (suggestion.size() > 1) {
                            suggestionBtns[i].setText(MiscUtils.constructFullName(suggestion.subList(1, suggestion.size())));
                        } else {
                            suggestionBtns[i].setText(MiscUtils.constructFullName(suggestion));
                        }
                        suggestionBtns[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDirListAdapter.setCurDir(suggestion);
                                setCurDirPathView();
                                startNewNote();
                            }
                        });
                    }
                }
            });
        }
    };

    /**
     * Navigates to the parent directory of current directory.
     * Returns true if we have a parent dir, false otherwise.
     */
    public boolean goBack() {
        boolean val = myDirListAdapter.goBack();
        if (val) {
            setCurDirPathView();
        }
        return val;
    }

    /**
     * Handle click on the directory list view. It'll open the clicked item (directory or file).
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (myState != State.NORMAL) {
            goBack();
            setSoftKeyboard(false);
            Log.d("MyNotes", "Not acting on press");
            return;
        }

        final DataItem item = (DataItem) myDirListAdapter.getItem(position);

        if (!(item instanceof Directory)) {
            startWorkActivity(item.getName());
            return;
        }

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myDirListAdapter.itemClick(position);
                setCurDirPathView();
            }
        }, 100);
    }

    private void startWorkActivity(final String name) {
        Intent intent = new Intent(getContext(), WorkActivity.class);
        intent.putExtra(NOTE_TITLE, name);
        intent.putExtra(CUR_DIR, MiscUtils.getCurPathStr(myDirListAdapter.getCurPath()));

        startActivity(intent);
    }

    /**
     * Invoked when user selects an entry from the path view at the top. This sets the current
     * directory to the one user clicked.
     */
    @Override
    public void onItemClick(List<Directory> curPath) {
        myDirListAdapter.setCurDir(curPath);
        setCurDirPathView();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (myState != State.NORMAL) {
            Log.d("MyNotes", "Not acting on long press");
            return false;
        }

        Log.d("MyNotes", "Acting on long press");
        myEditPosition = position;
        myLastLongpressName = ((DataItem) myDirListAdapter.getItem(position)).getName();

        myBottomDialog = new Dialog(getActivity(), R.style.MaterialDialogSheet);
        if (myBottomDialog.getWindow() != null) {
            myBottomDialog.getWindow().setContentView(R.layout.options_pop_up);
            myBottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            myBottomDialog.getWindow().setGravity(Gravity.BOTTOM);
            myBottomDialog.findViewById(R.id.btn_rename).setOnClickListener(this);
            myBottomDialog.findViewById(R.id.btn_move).setOnClickListener(this);
            myBottomDialog.findViewById(R.id.btn_delete).setOnClickListener(this);
            myBottomDialog.show();
            return true;
        }
        return false;
    }

    /**
     * Refresh the current path view shown at top of the screen.
     */
    private void setCurDirPathView() {
        if (myDirListAdapter.isInTopDir()) {
            myCurPathView.setVisibility(View.GONE);
            mySuggestionsLayout.setVisibility(View.VISIBLE);
            myExecutor.execute(setupSuggestions);
        } else {
            myCurPathView.setVisibility(View.VISIBLE);
            mySuggestionsLayout.setVisibility(View.GONE);
        }

        myCurPathAdapter.setDataset(myDirListAdapter.getCurPath());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new_folder :
                myState = State.CREATING;
                String newName = getUnusedNewDirName(myDirListAdapter.getCurDir());
                myDirListAdapter.getDirs().add(newName);
                myEditPosition = myDirListAdapter.getDirs().size() - 1;
                myDirListAdapter.setEditable(myEditPosition, this);
                myDirListAdapter.notifyDataSetChanged();
                myDirList.setSelection(myEditPosition);
//                setSoftKeyboard(true);
                break;

            case R.id.btn_new_note:
                startNewNote();
                break;

            case R.id.btn_rename :
                myBottomDialog.dismiss();
                myState = State.RENAMING;
                myDirListAdapter.setEditable(myEditPosition, this);
                myDirListAdapter.notifyDataSetChanged();
                myDirList.setSelection(myEditPosition);
//                setSoftKeyboard(true);
                break;

            case R.id.btn_delete :
                myBottomDialog.dismiss();
                try {
                    DataWriter.getInstance(getContext()).editFolder(myDirListAdapter.getCurPath(),
                            myLastLongpressName, null);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(myBottomBarCoordinatorLayout, "Error deleting item",
                            Snackbar.LENGTH_SHORT).show();
                }

                // Store the deleted item name for enabling UNDO.
                StringBuilder sb = new StringBuilder();
                for (Directory d : myDirListAdapter.getCurPath()) {
                    sb.append(d.getName());
                    sb.append('/');
                }
                sb.append(myLastLongpressName);
                final String deletedItemFullName = sb.toString();

                Snackbar.make(myBottomBarCoordinatorLayout, "'" + myLastLongpressName + "' moved to trash", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        TrashEntry entry =
                                                MetadataDb.getInstance(getActivity()).metadataDao().loadTrashEntry(deletedItemFullName);
                                        TrashFragment.restoreItem(entry, getContext(), myExecutor, null, myHandler);
                                    }
                                });
                            }
                        })
                        .show();
                break;

            case R.id.btn_move:
                myBottomDialog.dismiss();
                MoveItemDialogFragment.getInstance(MiscUtils.getCurPathStr(myDirListAdapter.getCurPath()), myLastLongpressName)
                        .show(getActivity().getSupportFragmentManager(), "Move");
                break;
        }

    }

    private void startNewNote() {
        String newNoteName = getUnusedNewNoteName(myDirListAdapter.getCurDir());
        try {
            DataWriter.getInstance(getContext()).addFile(myDirListAdapter.getCurPath(), newNoteName, NEW_NOTE_CONTENTS);
            startWorkActivity(newNoteName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Snackbar.make(myBottomBarCoordinatorLayout, "Error creating note", Snackbar.LENGTH_SHORT).show();
        }
    }

    private String getUnusedNewDirName(final Directory d) {
        return getUnusedName(d, "New folder");
    }

    private String getUnusedNewNoteName(final Directory d) {
        return getUnusedName(d, "New note");
    }

    private String getUnusedName(final Directory d, final String base) {
        String unused = base;
        int i = 0;
        while (d.getFile(unused) != null || d.getFile(unused) != null) {
            i++;
            unused = base + " " + i;
        }
        return unused;
    }

    @Override
    public void onDataChanged() {
        myDirListAdapter.refreshTopDir();
        setCurDirPathView();
    }

    @Override
    public void onEditFinished(int position, String newText) {
        if (myState == State.CREATING) {
            try {
                DataWriter.getInstance(getContext()).addFolder(myDirListAdapter.getCurPath(), newText);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar.make(myBottomBarCoordinatorLayout, "Error creating folder", Snackbar.LENGTH_SHORT).show();
            }
        } else if (myState == State.RENAMING) {
            try {
                File file = myDirListAdapter.getCurDir().getFile(myLastLongpressName);
                if (file == null) {
                    // We are renaming a dir.
                    DataWriter.getInstance(getContext()).editFolder(myDirListAdapter.getCurPath(), myLastLongpressName, newText);
                } else {
                    // We are renaming a file.
                    DataWriter.getInstance(getContext()).editFile(myDirListAdapter.getCurPath(), myLastLongpressName, newText, file.getFileContents());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar.make(myBottomBarCoordinatorLayout, "Error renaming folder", Snackbar.LENGTH_SHORT).show();
            }
        }

        myState = State.NORMAL;
        myEditPosition = -1;
        setSoftKeyboard(false);
    }

    private void setSoftKeyboard(boolean show) {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                if (!show) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } else {
                    Log.d("MyNotes", "Showing soft keyboard");
                    imm.showSoftInput(view, 0);
                }
            }
        }
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return myRootView.findViewById(id);
    }
}
