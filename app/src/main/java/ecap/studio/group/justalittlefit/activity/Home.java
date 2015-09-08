package ecap.studio.group.justalittlefit.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
import ecap.studio.group.justalittlefit.database.DbConstants;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;


public class Home extends Activity {
    private final String LOG_TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;
    @InjectViews({R.id.todayHomeText, R.id.createEditHomeText,
            R.id.assignHomeText, R.id.viewHomeText})
    List<TextView> homeTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        this.insertMockDataIfNeeded();
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

    /**
     * Inserts mock data for dev purposes on db creation (if needed).
     */
    void insertMockDataIfNeeded() {
        try {
            Dao<Workout, Integer> workoutDao = getHelper().getWorkoutDao();
            if (workoutDao.queryForAll().size() == 0) {
                Log.i(LOG_TAG, "Creating mock dev data");
                this.createMockData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts mock data for dev purposes on db creation.
     */
    void createMockData() {
        this.insertWorkoutsToDatabase();
        this.createExercisesForWorkouts();
        this.createSetsForWorkouts();
        this.queryForSets();
    }

    void insertWorkoutsToDatabase() {
        Workout workout = new Workout("Arms", 0);
        Workout workout1 = new Workout("Abs", 1);
        Workout workout2 = new Workout("Shoulders", 2);
        try {
            Dao<Workout, Integer> workoutDao = getHelper().getWorkoutDao();
            workoutDao.create(workout);
            workoutDao.assignEmptyForeignCollection(workout, DbConstants.EXERCISES);
            workoutDao.create(workout1);
            workoutDao.assignEmptyForeignCollection(workout1, DbConstants.EXERCISES);
            workoutDao.create(workout2);
            workoutDao.assignEmptyForeignCollection(workout2, DbConstants.EXERCISES);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void createExercisesForWorkouts() {
        Exercise ex = new Exercise("Push Ups", 0);
        Exercise ex1 = new Exercise("Curls", 1);

        Exercise ex2 = new Exercise("Situps-Weighted Situps", 0);
        Exercise ex3 = new Exercise("Gorilla ups-Captain raises", 1);

        Exercise ex4 = new Exercise("Shoulder press-Dips", 0);
        Exercise ex5 = new Exercise("Lat pull downs", 1);

        ArrayList<Exercise> exerciseList = new ArrayList<>();
        exerciseList.add(ex);
        exerciseList.add(ex1);

        ArrayList<Exercise> exerciseList2 = new ArrayList<>();
        exerciseList2.add(ex2);
        exerciseList2.add(ex3);

        ArrayList<Exercise> exerciseList3 = new ArrayList<>();
        exerciseList3.add(ex4);
        exerciseList3.add(ex5);

        this.addExercisesToWorkouts(1, exerciseList);
        this.addExercisesToWorkouts(2, exerciseList2);
        this.addExercisesToWorkouts(3, exerciseList3);
    }

    void createSetsForWorkouts() {
        try {
            Dao<Exercise, Integer> exerciseDao = getHelper().getExerciseDao();
            for (int i = 1; i < exerciseDao.queryForAll().size() + 1; i++) {
                Exercise ex = exerciseDao.queryForId(i);
                ForeignCollection<Set> sets;
                sets = ex.getSets();
                this.setSets(sets, this.getSetsForDb());

            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        sets.add(new Set(10, Constants.LBS, Constants.REPS, 0, 50));
        sets.add(new Set(8, Constants.LBS, Constants.REPS, 1, 30));
        sets.add(new Set(6, Constants.NA, Constants.LOGGED_TIMED, 1, 0, 2, 30));
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
        Toast.makeText(this,
                getString(R.string.today_string), Toast.LENGTH_SHORT)
                .show();
    }

    @OnClick(R.id.createEditHomeOption)
    void startCreateEditActivity() {
        Intent intent = new Intent(this, CreateEditWorkout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.assignHomeOption)
    void startAssignActivity() {
        Intent intent = new Intent(this, Assign.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(intent);
    }

    @OnClick(R.id.viewHomeOption)
    void startViewWorkoutActivity() {
        Toast.makeText(this,
                getString(R.string.view_string), Toast.LENGTH_SHORT)
                .show();
    }
}
