package paradigm.shift.myautonote;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.List;

import paradigm.shift.myautonote.adapter.CurPathAdapter;
import paradigm.shift.myautonote.adapter.CurPathItemClickListener;
import paradigm.shift.myautonote.adapter.DirListAdapter;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_util.DataReader;

public class MyNotes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, CurPathItemClickListener {

    private ListView myDirList;
    private DirListAdapter myDirListAdapter;
    private LinearLayout mySuggestionsLayout;
    private RecyclerView myCurPathView;
    private CurPathAdapter myCurPathAdapter;

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

        Log.d("debug", "HELLO WORLD");
        DataReader rd;
        try{
            rd = DataReader.getInstance(this);
            Log.d("debug", rd.getTopDir().toString());
        }catch (Exception e){
            Log.d("debug", "Failer");
            e.printStackTrace();
            return;
        }

        myDirList = (ListView) findViewById(R.id.list_view_dir_list);
        myDirListAdapter = new DirListAdapter(this, rd.getTopDir());
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
        final Dialog bottomDialog = new Dialog(this, R.style.MaterialDialogSheet);
        bottomDialog.getWindow().setContentView(R.layout.options_pop_up);
        bottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
//        bottomDialog.findViewById()
        bottomDialog.show();
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
}
