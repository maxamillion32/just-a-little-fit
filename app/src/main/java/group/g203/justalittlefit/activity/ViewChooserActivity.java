package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.adapter.WorkoutRvNameAdapter;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AssignWorkoutDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.listener.AssignWorkoutDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * The activity that displays when there are multiple
 * {@link group.g203.justalittlefit.model.Workout} objects assigned.
 */
public class ViewChooserActivity extends BaseActivity implements AssignWorkoutDialogListener {

    private static final String DATE_FORMAT = "MMMM d, yyyy";

    @Bind(R.id.tvDate)
    TextView tvDate;
    @Bind(R.id.rvWorkoutName)
    RecyclerView rvWorkoutName;
    WorkoutRvNameAdapter workoutRvNameAdapter;
    List<Workout> workouts;
    boolean busRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                displayInfoDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
        frameLayout.removeAllViews();
        unregisterBus();
        this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBus();
        this.finish();
    }

    private List<Workout> getWorkouts() {
        Bundle extras = getIntent().getExtras();
        if (Utils.isInBundleAndValid(extras, Constants.WORKOUTS)) {
            return extras.getParcelableArrayList(Constants.WORKOUTS);
        } else {
            return null;
        }
    }

    private void setDisplayDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
        if (workouts.get(0) != null && workouts.get(0).getWorkoutDate() != null) {
            tvDate.setText(dateTimeFormatter.print(workouts.get(0).getWorkoutDate()));
        }
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.CHOOSER);
        dialog.show(fm, getString(R.string.infoDialogTagViewChooser));
    }

    private void setupRecyclerView() {
        rvWorkoutName.setLayoutManager(new LinearLayoutManager(this));
        rvWorkoutName.setItemAnimator(new DefaultItemAnimator());

        workoutRvNameAdapter = new WorkoutRvNameAdapter(
                new ArrayList<>(workouts), this);
        rvWorkoutName.setAdapter(workoutRvNameAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_view_chooser, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        workouts = getWorkouts();

        if (!Utils.collectionIsNullOrEmpty(workouts)) {
            if (Utils.isToday(workouts.get(0).getWorkoutDate())) {
                setTitle(R.string.today_title_string);
            } else {
                setTitle(R.string.view_title_string);
            }
        } else {
          setTitle(Constants.EMPTY_STRING);
        }

        setDisplayDate();
        setupRecyclerView();
        hideProgressDialog();
        handleBottomNaviDisplay(true);
        Bundle extras = getIntent().getExtras();
        String highlightKey = null;
        if (Utils.isInBundleAndValid(extras, Constants.TODAY_HIGHLIGHT_KEY)) {
            highlightKey = extras.getString(Constants.TODAY_HIGHLIGHT_KEY);
        }
        String naviCaseValue = (highlightKey == null) ? Constants.VIEW : highlightKey;
        handleNaviSelectionColor(naviCaseValue);
        registerBus();
    }

    @Override
    public void onAssignWorkoutClick(AssignWorkoutDialog dialog) {
        showProgressDialog();
        LinkedList<Object> dateTimeIdList = new LinkedList<>();
        dateTimeIdList.add(0, dialog.getDateTimes());
        dateTimeIdList.add(1, dialog.getSelectedWorkoutNames());

        DbFunctionObject createNewWorkoutDfo = new DbFunctionObject(dateTimeIdList, DbConstants.ASSIGN_WORKOUTS);
        new DbAsyncTask(Constants.ASSIGN).execute(createNewWorkoutDfo);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        Object eventResult = null;
        if (event != null) {
            eventResult = event.getResult();
        }
        if (eventResult != null && eventResult instanceof List) {
            List<Workout> assignedWorkouts = (List<Workout>) event.getResult();
            Utils.displayLongActionSnackbar(getBottomNaviView(), getString(R.string.workouts_assigned_successfully),
                    Constants.UNDO, undoAssignListener(assignedWorkouts),
                    ContextCompat.getColor(this, R.color.app_blue_gray));
        } else if (eventResult != null && eventResult instanceof String) {
            Utils.displayLongSimpleSnackbar(getBottomNaviView(), getString(R.string.removed_assigned_workouts_successfully));
        } else {
            Utils.displayLongSimpleSnackbar(getBottomNaviView(), getString(R.string.assign_workout_error));
        }
        hideProgressDialog();
    }

    private View.OnClickListener undoAssignListener(final List<Workout> workoutsToRemove) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbFunctionObject removeAssignedWorkouts = new DbFunctionObject(workoutsToRemove, DbConstants.DELETE_WORKOUTS);
                new DbAsyncTask(Constants.ASSIGN).execute(removeAssignedWorkouts);
            }
        };
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getBaseAssignBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getBaseAssignBus().unregister(this);
            busRegistered = false;
        }
    }
}
