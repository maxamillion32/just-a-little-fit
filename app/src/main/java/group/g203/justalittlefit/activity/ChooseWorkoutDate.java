package group.g203.justalittlefit.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.SelectViewOrTodayDialog;
import group.g203.justalittlefit.listener.SelectViewOrTodayDialogListener;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

public class ChooseWorkoutDate extends BaseNaviDrawerActivity implements SelectViewOrTodayDialogListener {

    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final String DATE_ERROR_PREFIX = "Selected dates must be between ";
    private static final String AND = " and ";

    @InjectView(R.id.chooseCalendar)
    CalendarPickerView chooseCalendar;
    DateTime chosenDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BaseNaviDrawerActivity activity = this;
        super.onCreate(savedInstanceState);
        showProgressDialog();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_choose_workout_date, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        initCalendarPicker(activity);
        setTitle(R.string.view_title_string);
        hideProgressDialog();
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
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
    }

    private void initCalendarPicker(final BaseNaviDrawerActivity activity) {
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
                if (Utils.isToday(chosenDateTime)) {
                    displaySelectViewOrTodayDialog();
                } else {
                    Utils.launchViewActivity(activity, chosenDateTime);
                }
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

    private void displaySelectViewOrTodayDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SelectViewOrTodayDialog dialog = new SelectViewOrTodayDialog();
        dialog.show(fm, getString(R.string.selectViewOrTodayDialogTag));
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
        resetCalendarView();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_view);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideProgressDialog();
    }

    @Override
    public void onSelectViewDialog(SelectViewOrTodayDialog dialog) {
        Utils.launchViewActivity(this, chosenDateTime);
    }

    @Override
    public void onSelectTodayDialog(SelectViewOrTodayDialog dialog) {
        Utils.launchTodayActivity(this);
    }
}
