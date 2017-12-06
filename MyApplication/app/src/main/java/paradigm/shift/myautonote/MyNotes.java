package paradigm.shift.myautonote;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
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
import paradigm.shift.myautonote.data_util.DataChangedListener;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.DataWriter;
import paradigm.shift.myautonote.util.MiscUtils;
import paradigm.shift.myautonote.util.NewNoteSuggestionsGenerator;
import paradigm.shift.myautonote.util.PreferenceHelper;

import static paradigm.shift.myautonote.WorkActivity.CUR_DIR;
import static paradigm.shift.myautonote.WorkActivity.NOTE_CONTENT;
import static paradigm.shift.myautonote.WorkActivity.NOTE_TITLE;
import static paradigm.shift.myautonote.util.NewNoteSuggestionsGenerator.NUM_SUGGESTIONS;

public class MyNotes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, CurPathItemClickListener, View.OnClickListener,
        DataChangedListener, EditFinishedListener {

    private static final String NEW_NOTE_CONTENTS = "";
    private enum State {
        NORMAL, CREATING, RENAMING
    }

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
    private Button myNewFolderBtn;
    private Button myNewNoteBtn;
    private TextView myUsernameView;
    private Executor myExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onResume() {
        super.onResume();
        myUsernameView.setText(PreferenceHelper.getString(this, R.string.pref_key_username, "Unknown user"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PreferenceHelper.getBoolean(this, R.string.pref_key_intro_done, false)) {
            startActivity(new Intent(this, IntroActivity.class));
        }
        myHandler = new Handler();

        setContentView(R.layout.activity_my_notes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myUsernameView = navigationView.getHeaderView(0).findViewById(R.id.text_username);

        DataReader.getInstance(this).registerListener(this);

        myDirList = (ListView) findViewById(R.id.list_view_dir_list);
        myDirListAdapter = new DirListAdapter(this);
        myDirList.setAdapter(myDirListAdapter);
        myDirList.setOnItemClickListener(this);
        myDirList.setOnItemLongClickListener(this);

        mySuggestionsLayout = (LinearLayout) findViewById(R.id.suggestion_layout);
        myCurPathView = (RecyclerView) findViewById(R.id.cur_path_view);
        myCurPathAdapter = new CurPathAdapter(myDirListAdapter.getCurPath(), this);
        myCurPathView.setAdapter(myCurPathAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.ic_chevron_right_black_24dp));
        myCurPathView.setLayoutManager(mLayoutManager);
        myCurPathView.addItemDecoration(dividerItemDecoration);

        myNewFolderBtn = (Button)findViewById(R.id.btn_new_folder);
        myNewNoteBtn = (Button) findViewById(R.id.btn_new_note);
        myNewFolderBtn.setOnClickListener(this);
        myNewNoteBtn.setOnClickListener(this);

        myBottomBarCoordinatorLayout = findViewById(R.id.coordinator_bottom_bar);

        TextView logoutBtn = findViewById(R.id.logout);
        logoutBtn.setOnClickListener(this);

        String[] curDir = getIntent().getStringArrayExtra(CUR_DIR);
        setCurDir(curDir);

        myExecutor.execute(setupSuggestions);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String[] curDir = intent.getStringArrayExtra(CUR_DIR);
        setCurDir(curDir);
    }

    private void setCurDir(String[] curDir) {
        if (curDir == null) {
            return;
        }

        List<Directory> dir = new ArrayList<>(curDir.length);
        Directory topDir = DataReader.getInstance(this).getTopDir();
        dir.add(topDir);
        for (int i = 1; i < curDir.length; i++) {
            topDir = topDir.getSubDirectory(curDir[i]);
            if (topDir == null) {
                return;
            }
            dir.add(topDir);
        }
        Log.d("MyNotes", "Setting cur dir");
        myDirListAdapter.setCurDir(dir);
        setCurDirPathView();
    }

    private Runnable setupSuggestions = new Runnable() {
        @Override
        public void run() {
            final List<List<Directory>> suggestions = NewNoteSuggestionsGenerator.generateSuggestions(MyNotes.this);

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
                                myNewNoteBtn.performClick();
                            }
                        });
                    }
                }
            });
        }
    };

    /**
     * Back press should navigate to the previous folder if user is not in the top dir, else fallback
     * to default behaviour (exit the app).
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!myDirListAdapter.goBack()) {
                super.onBackPressed();
            }
            setCurDirPathView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handle click on the directory list view. It'll open the clicked item (directory or file).
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (myState != State.NORMAL) {
            Log.d("MyNotes", "Not acting on press");
            return;
        }

        final DataItem item = (DataItem) myDirListAdapter.getItem(position);

        if (!(item instanceof Directory)) {
            startWorkActivity(item.getName(), item.toString());
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

    private void startWorkActivity(final String name, final String s) {
        Intent intent = new Intent(this, WorkActivity.class);
        intent.putExtra(NOTE_TITLE, name);
        intent.putExtra(NOTE_CONTENT, s);
        intent.putExtra(CUR_DIR, myDirListAdapter.getCurPathStr());

        startActivity(intent);
    }

    /**
     * Invoked when user selects an entry from the path view at the top. This sets the current
     * directory to the one user clicked.
     * @param curPath
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

        myBottomDialog = new Dialog(this, R.style.MaterialDialogSheet);
        myBottomDialog.getWindow().setContentView(R.layout.options_pop_up);
        myBottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myBottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        myBottomDialog.findViewById(R.id.btn_rename).setOnClickListener(this);
        myBottomDialog.findViewById(R.id.btn_move).setOnClickListener(this);
        myBottomDialog.findViewById(R.id.btn_delete).setOnClickListener (this);
        myBottomDialog.show();
        return true;
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
                String newNoteName = getUnusedNewNoteName(myDirListAdapter.getCurDir());
                try {
                    DataWriter.getInstance(this).addFile(myDirListAdapter.getCurPath(), newNoteName, NEW_NOTE_CONTENTS);
                    startWorkActivity(newNoteName, NEW_NOTE_CONTENTS);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(myBottomBarCoordinatorLayout, "Error creating note", Snackbar.LENGTH_SHORT).show();
                }
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
                final boolean isDir;
                if (myDirListAdapter.getCurDir().getFile(myLastLongpressName) == null) {
                    isDir = true;
                } else {
                    isDir = false;
                }
                if (isDir) {
                    myDirListAdapter.getDirs().remove(myLastLongpressName);
                } else {
                    myDirListAdapter.getFiles().remove(myLastLongpressName);
                }
                myDirListAdapter.notifyDataSetChanged();
                Snackbar.make(myBottomBarCoordinatorLayout, "'" + myLastLongpressName + "' deleted",
                        Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDirListAdapter.refreshTopDir();
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                    try {
                                        DataWriter.getInstance(MyNotes.this).editFolder(myDirListAdapter.getCurPath(),
                                                myLastLongpressName, null);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Snackbar.make(myBottomBarCoordinatorLayout, "Error deleting item",
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                                super.onDismissed(transientBottomBar, event);

                            }
                        })
                        .show();
                break;

            case R.id.btn_move :
                break;

            case R.id.logout:
                PreferenceHelper.remove(this, R.string.pref_key_username);
                startActivity(new Intent(this, IntroActivity.class));
        }

    }

    private String getUnusedNewDirName(Directory d) {
        String st = "New folder";
        String unused = st;
        int i = 0;
        while (d.getSubDirectory(unused) != null) {
            i++;
            unused = st + " " + i;
        }
        return unused;
    }

    private String getUnusedNewNoteName(Directory d) {
        String st = "New Note";
        String unused = st;
        int i = 0;
        while (d.getFile(unused) != null) {
            i++;
            unused = st + " " + i;
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
                DataWriter.getInstance(this).addFolder(myDirListAdapter.getCurPath(), newText);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar.make(myBottomBarCoordinatorLayout, "Error creating folder", Snackbar.LENGTH_SHORT).show();
            }
        } else if (myState == State.RENAMING) {
            try {
                File file = myDirListAdapter.getCurDir().getFile(myLastLongpressName);
                if (file == null) {
                    // We are renaming a dir.
                    DataWriter.getInstance(this).editFolder(myDirListAdapter.getCurPath(), myLastLongpressName, newText);
                } else {
                    // We are renaming a file.
                    DataWriter.getInstance(this).editFile(myDirListAdapter.getCurPath(), myLastLongpressName, newText, file.getFileContents());
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
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!show) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else {
                Log.d("MyNotes", "Showing soft keyboard");
                imm.showSoftInput(view, 0);
            }
        }
    }
}
