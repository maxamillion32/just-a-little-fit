package ecap.studio.group.justalittlefit.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.activity.TodayActivity;
import ecap.studio.group.justalittlefit.activity.TodayChooserActivity;
import ecap.studio.group.justalittlefit.activity.ViewActivity;
import ecap.studio.group.justalittlefit.bus.PeekLauncherBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class PeekLauncher extends Fragment {
    private final String LOG_TAG = getClass().getSimpleName();
    private ProgressDialog progressDialog = null;
    boolean isTodayLauncher;

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
        PeekLauncherBus.getInstance().register(this);
        progressDialog = Utils.showProgressDialog(getActivity());
        DateTime dateTime = (DateTime) getArguments().getSerializable(Constants.DATE_TIME);
        if (dateTime == null) {
            getTodaysWorkouts();
            isTodayLauncher = true;
        } else {
            getViewWorkouts(dateTime);
            isTodayLauncher = false;
        }
    }

    private void getTodaysWorkouts() {
        DbFunctionObject getTodaysWorkouts = new DbFunctionObject(null, DbConstants.GET_WORKOUTS_BY_DATE);
        new DbAsyncTask(Constants.PEEK_LAUNCHER)
                .execute(getTodaysWorkouts);
    }

    private void getViewWorkouts(DateTime dateTime) {
        DbFunctionObject getViewWorkouts = new DbFunctionObject(dateTime, DbConstants.GET_WORKOUTS_BY_DATE);
        new DbAsyncTask(Constants.PEEK_LAUNCHER)
                .execute(getViewWorkouts);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        dismissProgressDialog();
        if (isTodayLauncher) {
            if (event == null || event.getResult() == null) {
                displayTodayWorkoutsError();
            } else if (event.getResult() instanceof List) {
                ArrayList<Workout> workouts = new ArrayList<>((List<Workout>) event.getResult());
                if (workouts.isEmpty()) {
                    Utils.displayLongToast(getActivity(), getString(R.string.no_workouts_for_today));
                } else if (workouts.size() == Constants.INT_ONE) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.WORKOUT, workouts.get(0));

                    Intent intent = new Intent(getActivity(), TodayActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                } else if (workouts.size() > Constants.INT_ONE) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constants.WORKOUTS, workouts);

                    Intent intent = new Intent(getActivity(), TodayChooserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                }
            }
        } else {
            if (event == null || event.getResult() == null) {
                displayViewWorkoutsError();
            } else if (event.getResult() instanceof List) {
                ArrayList<Workout> workouts = new ArrayList<>((List<Workout>) event.getResult());
                if (workouts.isEmpty()) {
                    Utils.displayLongToast(getActivity(), getString(R.string.no_workouts_to_view));
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.DATE, workouts.get(0).getWorkoutDate());

                    Intent intent = new Intent(getActivity(), ViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                }
            }
        }
    }

    void displayTodayWorkoutsError() {
        String errorMsg = getString(R.string.today_workouts_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongToast(getActivity(), errorMsg);
    }

    void displayViewWorkoutsError() {
        String errorMsg = getString(R.string.workout_view_error);
        Log.e(LOG_TAG, errorMsg);
        Utils.displayLongToast(getActivity(), errorMsg);
    }

    @Override
    public void onDestroy() {
        PeekLauncherBus.getInstance().unregister(this);
        super.onDestroy();
    }

    void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}


