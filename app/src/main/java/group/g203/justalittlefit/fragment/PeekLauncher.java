package group.g203.justalittlefit.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import group.g203.justalittlefit.R;
import group.g203.justalittlefit.activity.ViewActivity;
import group.g203.justalittlefit.activity.ViewChooserActivity;
import group.g203.justalittlefit.activity.ViewPastWorkout;
import group.g203.justalittlefit.database.DbAsyncTask;
import group.g203.justalittlefit.database.DbConstants;
import group.g203.justalittlefit.database.DbFunctionObject;
import group.g203.justalittlefit.database.DbTaskResult;
import group.g203.justalittlefit.dialog.AssignWorkoutDateWhenNoneDialog;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;
import group.g203.justalittlefit.util.Utils;

/**
 * Headless fragment used to check if a date contains any
 * {@link group.g203.justalittlefit.model.Workout} objects.
 */
public class PeekLauncher extends Fragment {
    private final String LOG_TAG = getClass().getSimpleName();
    private ProgressDialog progressDialog = null;
    boolean busRegistered;
    DateTime dateTime;

    public static final PeekLauncher getNewInstance(DateTime dateTime) {
        PeekLauncher peekLauncher = new PeekLauncher();
        Bundle bundle = new Bundle(Constants.INT_ONE);
        bundle.putSerializable(Constants.DATE_TIME, dateTime);
        peekLauncher.setArguments(bundle);
        return peekLauncher;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        registerBus();
        progressDialog = Utils.showProgressDialog(getActivity());
        dateTime = (DateTime) getArguments().getSerializable(Constants.DATE_TIME);
        getWorkouts(dateTime);
    }

    private void getWorkouts(DateTime dateTime) {
        DbFunctionObject getWorkouts = new DbFunctionObject(dateTime, DbConstants.GET_WORKOUTS_BY_DATE);
        new DbAsyncTask(Constants.PEEK_LAUNCHER)
                .execute(getWorkouts);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        dismissProgressDialog();
        if (event == null || event.getResult() == null) {
            displayWorkoutsError();
        } else if (event.getResult() instanceof List) {
            ArrayList<Workout> workouts = new ArrayList<>((List<Workout>) event.getResult());
            if (workouts.isEmpty()) {
                handleDisplayOptions();
            } else if (workouts.size() == Constants.INT_ONE) {
                Intent intent;
                Bundle bundle = new Bundle();

                if (Utils.isPriorToToday(dateTime)) {
                    intent = new Intent(getActivity(), ViewPastWorkout.class);
                    bundle.putSerializable(Constants.DATE, dateTime);
                } else {
                    bundle.putParcelable(Constants.WORKOUT, workouts.get(0));
                    if (dateTime == null || Utils.isToday(dateTime)) {
                        bundle.putString(Constants.TODAY_HIGHLIGHT_KEY, Constants.TODAY);
                    }
                    intent = new Intent(getActivity(), ViewActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            } else if (workouts.size() > Constants.INT_ONE) {
                Bundle bundle = new Bundle();
                Intent intent;

                if (Utils.isPriorToToday(dateTime)) {
                    intent = new Intent(getActivity(), ViewPastWorkout.class);
                    bundle.putSerializable(Constants.DATE, dateTime);
                } else {
                    intent = new Intent(getActivity(), ViewChooserActivity.class);
                    if (dateTime == null || Utils.isToday(dateTime)) {
                        bundle.putString(Constants.TODAY_HIGHLIGHT_KEY, Constants.TODAY);
                    }
                    bundle.putParcelableArrayList(Constants.WORKOUTS, workouts);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        }
    }

    void displayWorkoutsError() {
        String errorMsg = getString(R.string.workouts_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongToast(getActivity(), errorMsg);
    }

    private void handleDisplayOptions() {
        FragmentActivity activity = getActivity();
        if (activity != null && !Utils.isPriorToToday(dateTime)) {
            DateTime dialogDateTime = (dateTime == null) ? new DateTime() : dateTime;
            FragmentManager fm = activity.getSupportFragmentManager();
            AssignWorkoutDateWhenNoneDialog dialog = AssignWorkoutDateWhenNoneDialog.newInstance(dialogDateTime);
            dialog.show(fm, getString(R.string.assignWorkoutDateWhenNoneDialogTag));
        } else {
            if (dateTime != null) {
                Utils.displayLongToast(activity, getString(R.string.no_workouts_for_date));
            } else {
                Utils.displayLongToast(activity, getString(R.string.no_workouts_for_today));
            }
        }
    }

    @Override
    public void onPause() {
        unregisterBus();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterBus();
        super.onDestroy();
    }

    private void registerBus() {
        if (!busRegistered) {
            BusFactory.getPeekLauncherBus().register(this);
            busRegistered = true;
        }
    }

    private void unregisterBus() {
        if (busRegistered) {
            BusFactory.getPeekLauncherBus().unregister(this);
            busRegistered = false;
        }
    }

    void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}


