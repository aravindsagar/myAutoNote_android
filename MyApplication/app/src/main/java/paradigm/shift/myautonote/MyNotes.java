package paradigm.shift.myautonote;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.SearchView;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import paradigm.shift.myautonote.adapter.CurPathAdapter;
import paradigm.shift.myautonote.adapter.CurPathItemClickListener;
import paradigm.shift.myautonote.adapter.DirListAdapter;
import paradigm.shift.myautonote.adapter.EditFinishedListener;
import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_util.DataChangedListener;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.DataWriter;

public class MyNotes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, CurPathItemClickListener, View.OnClickListener,
        DataChangedListener, EditFinishedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        DataReader.getInstance(this).registerListener(this);

        myDirList = (ListView) findViewById(R.id.list_view_dir_list);
        myDirListAdapter = new DirListAdapter(this);
        myDirList.setAdapter(myDirListAdapter);
        myDirList.setOnItemClickListener(this);
        myDirList.setOnItemLongClickListener(this);

        mySuggestionsLayout = findViewById(R.id.suggestion_layout);
        myCurPathView = findViewById(R.id.cur_path_view);
        myCurPathAdapter = new CurPathAdapter(myDirListAdapter.getCurPath(), this);
        myCurPathView.setAdapter(myCurPathAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.ic_chevron_right_black_24dp));
        myCurPathView.setLayoutManager(mLayoutManager);
        myCurPathView.addItemDecoration(dividerItemDecoration);

        Button newFolderBtn = findViewById(R.id.btn_new_folder);
        newFolderBtn.setOnClickListener(this);
    }

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

        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

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
        final DataItem item = (DataItem) myDirListAdapter.getItem(position);
        if (!(item instanceof Directory)) {
            Intent intent = new Intent(this, WorkActivity.class);
            intent.putExtra("note_title", item.getName());
            intent.putExtra("note_content", item.getName());

            startActivity(intent);

        }
        if (myState != State.NORMAL) {
            Log.d("MyNotes", "Not acting on press");
            return;
        }

        Log.d("MyNotes", "Acting on press");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                myDirListAdapter.itemClick(position);
                setCurDirPathView();
            }
        }.execute();
    }

    /**
     * Invoked when user selects an entry from the path view at the top. This sets the current
     * directory to the one user clicked.
     * @param curPath
     */
    @Override
    public void onItemClick(List<Directory> curPath) {
        myDirListAdapter.setCurDir(curPath.get(curPath.size()-1), curPath);
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
        myBottomDialog.findViewById(R.id.btn_delete).setOnClickListener(this);
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
        } else {
            myCurPathView.setVisibility(View.VISIBLE);
            mySuggestionsLayout.setVisibility(View.GONE);
        }

        myCurPathAdapter.setDataset(myDirListAdapter.getCurPath());
        myCurPathAdapter.notifyDataSetChanged();
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
                setSoftKeyboard(true);
                break;

            case R.id.btn_rename :
                myBottomDialog.dismiss();
                myState = State.RENAMING;
                myDirListAdapter.setEditable(myEditPosition, this);
                myDirListAdapter.notifyDataSetChanged();
                myDirList.setSelection(myEditPosition);
                setSoftKeyboard(true);
                break;
            case R.id.btn_delete :
                myBottomDialog.dismiss();
                try {
                    DataWriter.getInstance(this).editFolder(myDirListAdapter.getCurPath(), myLastLongpressName, null);
                    Snackbar.make(findViewById(R.id.parent_layout), "'" + myLastLongpressName + "' deleted", Snackbar.LENGTH_SHORT);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.parent_layout), "Error deleting item", Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.btn_move :
                break;
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
                Snackbar.make(findViewById(R.id.parent_layout), "Error creating folder", Snackbar.LENGTH_SHORT);
            }
        } else if (myState == State.RENAMING) {
            try {
                DataWriter.getInstance(this).editFolder(myDirListAdapter.getCurPath(), myLastLongpressName, newText);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.parent_layout), "Error renaming folder", Snackbar.LENGTH_SHORT);
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
