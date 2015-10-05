package ecap.studio.group.justalittlefit.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.adapter.ViewWorkoutPagerAdapter;
import ecap.studio.group.justalittlefit.bus.ViewBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.InformationDialog;
import ecap.studio.group.justalittlefit.fragment.ViewWorkoutFragment;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;
import me.relex.circleindicator.CircleIndicator;

public class ViewActivity extends BaseNaviDrawerActivity {

    private final String LOG_TAG = getClass().getSimpleName();
    @InjectView(R.id.vpWorkouts)
    ViewPager vpWorkouts;
    @InjectView(R.id.circleIndicator)
    CircleIndicator circleIndicator;
    boolean busRegistered;
    ProgressDialog progressDialog;
    DateTime dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BaseNaviDrawerActivity activity = this;
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.view_title_string);
        progressDialog = Utils.showProgressDialog(this);
        displayWorkoutViews();
    }

    private void displayWorkoutViews() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.DATE)) {
            dateTime = new DateTime(extras.getSerializable(Constants.DATE));
            DbFunctionObject getDatesWorkouts = new DbFunctionObject(dateTime, DbConstants.GET_WORKOUTS_BY_DATE);
            new DbAsyncTask(Constants.VIEW_TEXT)
                    .execute(getDatesWorkouts);
        } else {
            displayError();
        }
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        dismissProgressDialog();
        if (event == null || event.getResult() == null) {
            displayError();
        } else if (event.getResult() instanceof List) {
            if (Utils.collectionIsNullOrEmpty((List<Workout>)event.getResult())) {
                this.finish();
                Utils.displayLongToast(this, getString(R.string.no_workouts_to_view));
            } else {
                setTitle(Utils.returnStandardDateString(dateTime));
                setViewPager((List<Workout>) event.getResult());
            }
        } else {
            displayError();
        }
    }

    void displayError() {
        String errorMsg = getString(R.string.workout_view_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongToast(this, errorMsg);
    }


    void setViewPager(List<Workout> workouts) {
        List<Fragment> workoutFragments = createFrags(workouts);
        ViewWorkoutPagerAdapter viewWorkoutPagerAdapter = new ViewWorkoutPagerAdapter(getSupportFragmentManager(), workoutFragments);
        vpWorkouts.setAdapter(viewWorkoutPagerAdapter);
        circleIndicator.setViewPager(vpWorkouts);
        if (viewWorkoutPagerAdapter.getCount() <= Constants.INT_ONE) {
            circleIndicator.setVisibility(View.GONE);
        }
    }

    private List<Fragment> createFrags(List<Workout> workouts) {
        List<Fragment> workoutFrags = new ArrayList<>();
        for (Workout workout : workouts) {
            ViewWorkoutFragment frag = ViewWorkoutFragment.getNewInstance(workout);
            workoutFrags.add(frag);
        }
        return workoutFrags;
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
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

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.VIEW_TEXT);
        dialog.show(fm, getString(R.string.infoDialogTagView));
    }

    private void registerBus() {
        if (!busRegistered) {
            ViewBus.getInstance().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            ViewBus.getInstance().unregister(this);
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

    void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
