package ecap.studio.group.justalittlefit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.util.Utils;

public class BaseNaviDrawerActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    void setupDrawerContent(NavigationView navigationView) {
        final BaseNaviDrawerActivity activity = this;
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        String activityTitle = activity.getTitle().toString().trim();
                        String selectedTitle = menuItem.getTitle().toString().trim();
                        Intent selectedIntent;

                        if (selectedTitle.equals(activityTitle)) {
                             /* do nothing, menu item will be checked and close out drawer after
                             the completion of this if-else logic */
                        } else if (selectedTitle.equals(activity.getString(R.string.today_string).trim())) {
                            Utils.displayLongToast(activity, (activity.getString(R.string.today_string).trim()));
                        } else if (selectedTitle.equals(activity.getString(R.string.create_edit_string).trim())) {
                            Utils.displayLongToast(activity, (activity.getString(R.string.create_edit_string).trim()));
                        } else if (selectedTitle.equals(activity.getString(R.string.assign_string).trim())) {
                            Utils.displayLongToast(activity, (activity.getString(R.string.assign_string).trim()));
                        } else if (selectedTitle.equals(activity.getString(R.string.view_string).trim())) {
                            Utils.displayLongToast(activity, (activity.getString(R.string.view_string).trim()));
                        } else {
                            /* Shouldn't reach this but if so, doing nothing here
                            is harmless as the drawer will close */
                        }
                        activity.mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


