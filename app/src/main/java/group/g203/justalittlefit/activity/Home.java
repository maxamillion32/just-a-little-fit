package group.g203.justalittlefit.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import group.g203.justalittlefit.R;
import group.g203.justalittlefit.database.DatabaseHelper;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AssignWorkoutDialog;
import group.g203.justalittlefit.dialog.InformationDialog;
import group.g203.justalittlefit.dialog.LibraryCreditsDialog;
import group.g203.justalittlefit.listener.AssignWorkoutDialogListener;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Home screen activity.
 */
public class Home extends BaseActivity implements AssignWorkoutDialogListener {
    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;
    boolean busRegistered;
    @Bind(R.id.homeLogoText)
    TextView homeTextView;

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
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                displayInfoDialog();
                break;
            case R.id.action_libs:
                displayLibsDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.closeAndReleaseDbHelper();
        unregisterBus();
    }

    @Override
    public void onResume() {
        super.onResume();
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_home, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.bind(this, frameLayout);
        setTitle(Constants.EMPTY_STRING);
        getHelper();
        this.formatHomeTextView();
        handleBottomNaviDisplay(true);
        registerBus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBus();
    }

    /**
     * Method used to get DatabaseHelper object.
     */
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    /**
     * Closes and releases DatabaseHelper object when activity is destroyed.
     */
    private void closeAndReleaseDbHelper() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private void formatHomeTextView() {
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.CUSTOM_FONT_TTF);
            homeTextView.setTypeface(face);
    }

    private void displayInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InformationDialog dialog = InformationDialog.newInstance(Constants.HOME);
        dialog.show(fm, getString(R.string.infoDialogTagHome));
    }

    private void displayLibsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        LibraryCreditsDialog dialog = LibraryCreditsDialog.newInstance();
        dialog.show(fm, getString(R.string.libCredDialogTag));
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
