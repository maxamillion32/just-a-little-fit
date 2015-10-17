package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.bus.TodayBus;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class TodayActivity extends BaseNaviDrawerActivity {

    boolean busRegistered;
    Workout todayWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_today, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.today_title_string);
        getWorkout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_info:
                displayInfoDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_today);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.TODAY);
        dialog.show(fm, getString(R.string.infoDialogTagToday));
    }


    private void getWorkout() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.WORKOUT)) {
            todayWorkout = extras.getParcelable(Constants.WORKOUT);
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            TodayBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            TodayBus.getInstance().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!busRegistered) {
            registerBus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
    }
}
