package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
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
 * Assigns {@link group.g203.justalittlefit.model.Workout} objects to dates.
 */
public class Assign extends BaseActivity implements AssignWorkoutDialogListener {
    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String DATE_ERROR_PREFIX = "Selected dates must be between ";
    private static final String AND = " and ";

    @Bind(R.id.assignCalendar)
    CalendarPickerView assignCalendar;
    FloatingActionButton fab;
    CoordinatorLayout clFab;
    private ArrayList<DateTime> dateTimes;
    boolean busRegistered;
    boolean fabIsReady = false;

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

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.ASSIGN);
        dialog.show(fm, getString(R.string.infoDialogTagAssign));
    }

    private void initCalendarPicker(final BaseActivity activity) {
        DateTime now = new DateTime();
        DateTime maxDate = now.plusYears(1);

        assignCalendar.init(now.toDate(), maxDate.toDate()).inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        assignCalendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                DateTime minDate = new DateTime();
                DateTime maxDate = minDate.plusYears(1).minusDays(1);
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);

                Utils.displayLongSimpleSnackbar(activity.findViewById(R.id.fab),
                        DATE_ERROR_PREFIX + dateTimeFormatter.print(minDate) + AND +
                                dateTimeFormatter.print(maxDate));
            }
        });
    }

    private void setupFloatingActionButton(final BaseActivity activity) {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        clFab = (CoordinatorLayout) findViewById(R.id.clBase);
        clFab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignCalendar.getSelectedDates().isEmpty() && Utils.collectionIsNullOrEmpty(dateTimes)) {
                    Utils.displayLongSimpleSnackbar(view, getString(R.string.enforceDatesForAssignment));
                } else {
                    setDateTimes(Utils.dateListToDateTimeList(assignCalendar.getSelectedDates()));
                    FragmentManager fm = activity.getSupportFragmentManager();
                    AssignWorkoutDialog dialog = AssignWorkoutDialog.newInstance(dateTimes);
                    dialog.show(fm, getString(R.string.assignWorkoutDialogTag));
                }
            }
        });
        fabIsReady = true;
    }

    public void setDateTimes(ArrayList<DateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    public List<DateTime> getDateTimes() {
        return dateTimes;
    }

    @Override
    public void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    @Override
    public void onAssignWorkoutClick(AssignWorkoutDialog dialog) {
        showProgressDialog();
        LinkedList<Object> dateTimeIdList = new LinkedList<>();
        dateTimeIdList.add(0, getDateTimes());
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
            Utils.displayLongActionSnackbar(fab, getString(R.string.workouts_assigned_successfully),
                    Constants.UNDO, undoAssignListener(assignedWorkouts),
                    getResources().getColor(R.color.app_blue_gray));
            resetCalendarView();
        } else if (eventResult != null && eventResult instanceof String) {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.removed_assigned_workouts_successfully));
            resetCalendarView();
        } else {
            Utils.displayLongSimpleSnackbar(fab, getString(R.string.assign_workout_error));
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

    private void resetCalendarView() {
        DateTime now = new DateTime();
        DateTime maxDate = now.plusYears(1);
        assignCalendar.init(now.toDate(), maxDate.toDate()).inMode(CalendarPickerView.SelectionMode.MULTIPLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        final BaseActivity activity = this;
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_assign, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        initCalendarPicker(activity);
        setupFloatingActionButton(activity);
        setTitle(R.string.assign_title_string);
        registerBus();
        hideProgressDialog();
        handleDialogResponse();
        handleAssignWorkoutDialogDisplay();
        handleNaviSelectionColor(Constants.ASSIGN);
    }

    private void handleDialogResponse() {
        Bundle extras = getIntent().getExtras();
        if (Utils.isInBundleAndValid(extras, Constants.ASSIGN_DATE)) {
            dateTimes = new ArrayList<>(1);
            dateTimes.add((DateTime) extras.getSerializable(Constants.ASSIGN_DATE));
            do {
                if (fabIsReady) {
                    fab.performClick();
                }
            } while (!fabIsReady);
        } else {
            // do nothing
        }
    }

    private void handleAssignWorkoutDialogDisplay() {
        FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag(getString(R.string.assignWorkoutDialogTag)) != null) {
            AssignWorkoutDialog.getInstance().dismiss();
            if (dateTimes != null) {
                dateTimes.clear();
            } else {
                dateTimes = new ArrayList<>();
            }
        }
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getAssignBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getAssignBus().unregister(this);
            busRegistered = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
        frameLayout.removeAllViews();
        hideProgressDialog();
    }
}
