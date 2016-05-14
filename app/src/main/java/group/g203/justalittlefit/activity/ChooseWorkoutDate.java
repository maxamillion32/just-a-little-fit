package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Activity that displays calendar to choose date to view a {@link group.g203.justalittlefit.model.Workout}
 */
public class ChooseWorkoutDate extends BaseActivity implements AssignWorkoutDialogListener {

    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String DATE_ERROR_PREFIX = "Selected dates must be between ";
    private static final String AND = " and ";

    @Bind(R.id.chooseCalendar)
    CalendarPickerView chooseCalendar;
    DateTime chosenDateTime;

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

    private void initCalendarPicker(final BaseActivity activity) {
        DateTime now = new DateTime();
        DateTime minDate = now.minusYears(5);
        DateTime maxDate = now.plusYears(5);
        chooseCalendar.init(minDate.toDate(), maxDate.toDate()).inMode(CalendarPickerView.SelectionMode.SINGLE);
        chooseCalendar.scrollToDate(now.toDate());

        chooseCalendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                DateTime minDate = new DateTime().minusYears(5);
                DateTime maxDate = minDate.plusYears(5).minusDays(1);
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);

                Utils.displayLongSimpleSnackbar(activity.findViewById(R.id.chooseCalendarLayout),
                        DATE_ERROR_PREFIX + dateTimeFormatter.print(minDate) + AND +
                                dateTimeFormatter.print(maxDate));
            }
        });

        chooseCalendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.DATE, date);
                chosenDateTime = new DateTime(date);
                Utils.launchViewActivity(activity, chosenDateTime);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.CHOOSE);
        dialog.show(fm, getString(R.string.infoDialogTagChooseWorkoutDate));
    }

    private void resetCalendarView() {
        DateTime now = new DateTime();
        DateTime minDate = now.minusYears(5);
        DateTime maxDate = now.plusYears(5);
        chooseCalendar.init(minDate.toDate(), maxDate.toDate()).inMode(CalendarPickerView.SelectionMode.SINGLE);
        chooseCalendar.scrollToDate(now.toDate());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        final BaseActivity activity = this;
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_choose_workout_date, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        initCalendarPicker(activity);
        setTitle(R.string.view_title_string);
        hideProgressDialog();
        resetCalendarView();
        handleBottomNaviDisplay(true);
        handleNaviSelectionColor(Constants.VIEW);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
        frameLayout.removeAllViews();
    }


    @Override
    public void onAssignWorkoutClick(AssignWorkoutDialog dialog) {
        showProgressDialog();
        LinkedList<Object> dateTimeIdList = new LinkedList<>();
        dateTimeIdList.add(0, dialog.getDateTimes());
        dateTimeIdList.add(1, dialog.getSelectedWorkoutNames());

        DbFunctionObject createNewWorkoutDfo = new DbFunctionObject(dateTimeIdList, DbConstants.ASSIGN_WORKOUTS);
        new DbAsyncTask(Constants.CHOOSE_WORKOUT_DATE).execute(createNewWorkoutDfo);
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
            resetCalendarView();
        } else if (eventResult != null && eventResult instanceof String) {
            Utils.displayLongSimpleSnackbar(getBottomNaviView(), getString(R.string.removed_assigned_workouts_successfully));
            resetCalendarView();
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
                new DbAsyncTask(Constants.CHOOSE_WORKOUT_DATE).execute(removeAssignedWorkouts);
            }
        };
    }
}
