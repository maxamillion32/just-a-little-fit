package ecap.studio.group.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.bus.AssignBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.dialog.AssignWorkoutDialog;
import ecap.studio.group.justalittlefit.listener.AssignWorkoutDialogListener;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class Assign extends BaseNaviDrawerActivity implements AssignWorkoutDialogListener {
    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String DATE_ERROR_PREFIX = "Selected dates must be between ";
    private static final String AND = " and ";

    @InjectView(R.id.assignCalendar)
    CalendarPickerView assignCalendar;
    private List<DateTime> dateTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BaseNaviDrawerActivity activity = this;
        super.onCreate(savedInstanceState);
        AssignBus.getInstance().register(this);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_assign, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        initCalendarPicker(activity);
        setupFloatingActionButton(activity);
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

    private void initCalendarPicker(final BaseNaviDrawerActivity activity) {
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        Date today = new Date();
        assignCalendar.init(today, nextYear.getTime()).inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        assignCalendar.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                DateTime minDate = new DateTime();
                DateTime maxDate = minDate.plusYears(1).minusDays(1);
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);

                Snackbar.make(activity.findViewById(R.id.fab), DATE_ERROR_PREFIX
                        + dateTimeFormatter.print(minDate) + AND + dateTimeFormatter.print(maxDate), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupFloatingActionButton(final BaseNaviDrawerActivity activity) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_plus_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignCalendar.getSelectedDates().isEmpty()) {
                    Snackbar.make(view, getString(R.string.enforceDatesForAssignment), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    setDateTimes(Utils.dateListToDateTimeList(assignCalendar.getSelectedDates()));
                    FragmentManager fm = activity.getSupportFragmentManager();
                    AssignWorkoutDialog dialog = new AssignWorkoutDialog();
                    dialog.show(fm, getString(R.string.assignWorkoutDialogTag));
                }
            }
        });
    }

    public void setDateTimes(List<DateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    public List<DateTime> getDateTimes() {
        return dateTimes;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAssignWorkoutClick(AssignWorkoutDialog dialog) {
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
            Utils.displayShortToast(this, getString(R.string.workouts_assigned_successfully));
        } else {
            Utils.displayLongToast(this, getString(R.string.assign_workout_error));
        }
    }
}
