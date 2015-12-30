package group.g203.justalittlefit.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.adapter.ViewWorkoutPagerAdapter;
import group.g203.justalittlefit.bus.ViewBus;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AppBaseDialog;
import group.g203.justalittlefit.dialog.ConfirmDeleteTodayWorkoutDialog;
import group.g203.justalittlefit.dialog.DeleteWorkoutsFromViewDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.fragment.ViewWorkoutFragment;
import group.g203.justalittlefit.listener.ConfirmDeleteTodayWorkoutListener;
import group.g203.justalittlefit.listener.DeleteWorkoutsFromViewDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;
import me.relex.circleindicator.CircleIndicator;

public class ViewActivity extends BaseNaviDrawerActivity implements ConfirmDeleteTodayWorkoutListener, DeleteWorkoutsFromViewDialogListener {

    private final String LOG_TAG = getClass().getSimpleName();
    @InjectView(R.id.vpWorkouts)
    ViewPager vpWorkouts;
    @InjectView(R.id.circleIndicator)
    CircleIndicator circleIndicator;
    boolean busRegistered;
    DateTime dateTime;
    List<Workout> workouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
            hideProgressDialog();
        }
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        if (event == null || event.getResult() == null) {
            displayError();
        } else if (event.getResult() instanceof List) {
            workouts = (List<Workout>)event.getResult();
            if (Utils.collectionIsNullOrEmpty(workouts)) {
                this.finish();
                Utils.displayLongToast(this, getString(R.string.no_workouts_to_view));
            } else {
                setTitle(Utils.returnStandardDateString(dateTime));
                setViewPager(workouts);
            }
        } else if (event.getResult() instanceof Integer) {
            if (event.getResult() == workouts.size()) {
                Utils.displayLongToast(this, getString(R.string.workout_deleted));
                Intent intent = new Intent(this, Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Utils.displayLongToast(this, getString(R.string.workout_deleted));
                Intent intent = getIntent();
                finish();
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        } else {
            displayError();
        }
        hideProgressDialog();
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
        getMenuInflater().inflate(R.menu.menu_view_info_delete_single, menu);
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
            case R.id.action_delete:
                displayDeleteDialog();
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

    private void displayDeleteDialog() {
        if (Utils.collectionIsNullOrEmpty(workouts)) {
            Utils.displayLongToast(this, getString(R.string.workout_view_error));
        } else {
            FragmentManager fm = getSupportFragmentManager();
            if (workouts.size() == Constants.INT_ONE) {
                ConfirmDeleteTodayWorkoutDialog dialog = ConfirmDeleteTodayWorkoutDialog.newDeleteFromViewInstance();
                dialog.show(fm, getString(R.string.confirmDeleteTodayWorkoutDialogTag));
            } else {
                DeleteWorkoutsFromViewDialog dialog = DeleteWorkoutsFromViewDialog.getInstance(new ArrayList<>(workouts));
                dialog.show(fm, getString(R.string.deleteWorkoutsFromViewDialogTag));
            }
        }
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
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(R.string.view_title_string);
        displayWorkoutViews();
        registerBus();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
        hideProgressDialog();
    }

    @Override
    public void onDeleteTodayWorkoutClick(AppBaseDialog dialog) {
        handleDeleteViewWorkouts(workouts);
    }

    @Override
    public void onDeleteWorkoutsClick(DeleteWorkoutsFromViewDialog dialog) {
        handleDeleteViewWorkouts(dialog.getSelectedWorkouts());
    }

    void handleDeleteViewWorkouts(List<Workout> workouts) {
        DbFunctionObject deleteWorkoutsDfo = new DbFunctionObject(workouts, DbConstants.DELETE_WORKOUTS);
        new DbAsyncTask(Constants.VIEW_TEXT).execute(deleteWorkoutsDfo);
    }
}
