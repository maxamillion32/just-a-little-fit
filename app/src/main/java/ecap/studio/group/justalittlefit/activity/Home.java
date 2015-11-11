package ecap.studio.group.justalittlefit.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import ecap.studio.group.justalittlefit.R;
import ecap.studio.group.justalittlefit.database.DatabaseHelper;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;


public class Home extends BaseNaviDrawerActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;
    private ProgressDialog progressDialog;
    @InjectViews({R.id.todayHomeText, R.id.createEditHomeText,
            R.id.assignHomeText, R.id.viewHomeText, R.id.homeLogoText})
    List<TextView> homeTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_home, null, false);
        frameLayout.addView(contentView, 0);
        ButterKnife.inject(this, frameLayout);
        setTitle(Constants.EMPTY_STRING);
        getHelper();
        this.formatHomeTextViews();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.closeAndReleaseDbHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        getHelper();
        dismissProgressDialog();
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_home);
        selectedItem.setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }

    @Override
    void setupDrawerContent(NavigationView navigationView) {
        // Check menu item of currently displayed activity
        MenuItem selectedItem = navigationView.getMenu().findItem(R.id.navi_home);
        selectedItem.setChecked(true);
        super.setupDrawerContent(navigationView);
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

    void addExercisesToWorkouts(int workoutId, List<Exercise>exerciseList) {
        Dao<Workout, Integer> workoutDao;
        try {
            workoutDao = getHelper().getWorkoutDao();
            Workout wo = workoutDao.queryForId(workoutId);
            ForeignCollection<Exercise> exercises = wo.getExercises();
            for (Exercise exercise : exerciseList) {
                exercises.add(exercise);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    void queryForSets() {
        try {
            Dao<Set, Integer> setDao = getHelper().getSetDao();
            List<Set> sets = setDao.queryForAll();
            Log.i(LOG_TAG, "setDao has this many sets: " + sets.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    List<Set> getSetsForDb() {
        ArrayList<Set> sets = new ArrayList<>();
        sets.add(new Set(10, Constants.LBS, Constants.WEIGHTS, 0, 50));
        sets.add(new Set(8, Constants.LBS, Constants.WEIGHTS, 1, 30));
        sets.add(new Set(6, Constants.LOGGED_TIMED, 1, 0, 2, 30));
        return sets;
    }

    void setSets(ForeignCollection<Set> setCollect, List<Set> sets) {
        for (Set set : sets) {
            setCollect.add(set);
        }
    }

    private void formatHomeTextViews() {
        for (TextView tv : homeTextViews) {
            Typeface face=Typeface.createFromAsset(getAssets(), Constants.CUSTOM_FONT_TTF);
            tv.setTypeface(face);
        }
    }

    @OnClick(R.id.todayHomeOption)
    void startTodayActivity() {
        Utils.launchTodayActivity(this);
    }

    @OnClick(R.id.createEditHomeOption)
    void startCreateEditActivity() {
        progressDialog = Utils.showProgressDialog(this);
        Intent intent = new Intent(this, CreateEditWorkout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.assignHomeOption)
    void startAssignActivity() {
        progressDialog = Utils.showProgressDialog(this);
        Intent intent = new Intent(this, Assign.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.viewHomeOption)
    void startViewWorkoutActivity() {
        progressDialog = Utils.showProgressDialog(this);
        Intent intent = new Intent(this, ChooseWorkoutDate.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
