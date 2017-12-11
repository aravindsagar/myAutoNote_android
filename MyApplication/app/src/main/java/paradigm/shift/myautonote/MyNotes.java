package paradigm.shift.myautonote;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import paradigm.shift.myautonote.util.PreferenceHelper;

import static paradigm.shift.myautonote.WorkActivity.CUR_DIR;

public class MyNotes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private TextView myUsernameView;
    private Fragment myCurrentFragment;
    private DrawerLayout myDrawer;
    private boolean myIsFragmentAdded = false;
    private String[] myCurDirExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PreferenceHelper.getBoolean(this, R.string.pref_key_intro_done, false)) {
            startActivity(new Intent(this, IntroActivity.class));
        }

        setContentView(R.layout.activity_my_notes);
        setupNavDrawer();

        myCurDirExtra = getIntent().getStringArrayExtra(CUR_DIR);

        if (savedInstanceState == null) {
            setCurrentFragment(MyNotesFragment.getInstance(myCurDirExtra));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        myUsernameView.setText(PreferenceHelper.getString(this, R.string.pref_key_username, "Unknown user"));
    }

    private void setupNavDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, myDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        myDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myUsernameView = navigationView.getHeaderView(0).findViewById(R.id.text_username);

        TextView logoutBtn = findViewById(R.id.logout);
        logoutBtn.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        myCurDirExtra = intent.getStringArrayExtra(CUR_DIR);
        setCurrentFragment(MyNotesFragment.getInstance(myCurDirExtra));
    }

    /**
     * Back press should navigate to the previous folder if user is not in the top dir, else fallback
     * to default behaviour (exit the app).
     */
    @Override
    public void onBackPressed() {
        if (myDrawer.isDrawerOpen(GravityCompat.START)) {
            myDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (!(myCurrentFragment instanceof MyNotesFragment) || !((MyNotesFragment) myCurrentFragment).goBack()) {
                super.onBackPressed();
            }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_notes) {
            setCurrentFragment(MyNotesFragment.getInstance(myCurDirExtra));
        } else if (id == R.id.nav_trash) {
            setCurrentFragment(new TrashFragment());
        } else if (id == R.id.nav_about) {
            setCurrentFragment(new AboutFragment());
        }

        myDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setCurrentFragment(Fragment fragment) {
        myCurrentFragment = fragment;
        if (myIsFragmentAdded) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_main, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, fragment)
                    .commit();
            myIsFragmentAdded = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                PreferenceHelper.remove(this, R.string.pref_key_username);
                startActivity(new Intent(this, IntroActivity.class));
        }
    }
}
