package ecap.studio.group.justalittlefit.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.bus.TodayLauncherBus;
import ecap.studio.group.justalittlefit.database.DbAsyncTask;
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.database.DbFunctionObject;
import ecap.studio.group.justalittlefit.database.DbTaskResult;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

public class TodayLauncher extends Fragment {

    private ProgressDialog progressDialog = null;

    public static final TodayLauncher getNewInstance() {
        TodayLauncher todayLauncher = new TodayLauncher();
        return todayLauncher;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        TodayLauncherBus.getInstance().register(this);
        progressDialog = Utils.showProgressDialog(getActivity());
        getTodaysWorkouts();
    }

    private void getTodaysWorkouts() {
        DbFunctionObject getTodaysWorkouts = new DbFunctionObject(new DateTime(), DbConstants.GET_WORKOUTS_BY_DATE);
        new DbAsyncTask(Constants.TODAY_LAUNCHER)
                .execute(getTodaysWorkouts);
    }

    @Subscribe
    public void onAsyncTaskResult(DbTaskResult event) {
        dismissProgressDialog();
        if (event == null || event.getResult() == null) {
            displayTodayWorkoutsError();
        } else if (event.getResult() instanceof List) {
            ArrayList<Workout> workouts = new ArrayList<>((List<Workout>) event.getResult());
            if (workouts.isEmpty()) {
                Utils.displayLongToast(getActivity(), getString(R.string.no_workouts_for_today));
            } else {
                //Todo change this to todayActivity
                Utils.displayLongToast(getActivity(), "Today there are workout(s)");
            }
        }
    }


    void displayTodayWorkoutsError() {
        String errorMsg = getString(R.string.today_workouts_error);
        Log.e("TodayLauncherTag", errorMsg);
        Utils.displayLongToast(getActivity(), errorMsg);
    }

    @Override
    public void onDestroy() {
        TodayLauncherBus.getInstance().unregister(this);
        super.onDestroy();
    }

    void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}


