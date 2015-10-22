package ecap.studio.group.justalittlefit.database;

import android.os.AsyncTask;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ecap.studio.group.justalittlefit.bus.AssignBus;
import ecap.studio.group.justalittlefit.bus.AssignDialogBus;
import ecap.studio.group.justalittlefit.bus.CreateEditExerciseBus;
import ecap.studio.group.justalittlefit.bus.CreateEditSetBus;
import ecap.studio.group.justalittlefit.bus.CreateEditWorkoutBus;
import ecap.studio.group.justalittlefit.bus.TodayBus;
import ecap.studio.group.justalittlefit.bus.TodayLauncherBus;
import ecap.studio.group.justalittlefit.bus.ViewBus;
import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Set;
import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;

/**
 * Runs database logic asynchronously using a {@link ecap.studio.group.justalittlefit.database.DbFunctionObject}
 */
public class DbAsyncTask extends AsyncTask<DbFunctionObject, Void, Object> {
    private final String LOG_TAG = getClass().getSimpleName();

    private String invokingClass;

    public DbAsyncTask() {
    }

    public DbAsyncTask(String invokingClass) {
        this.invokingClass = invokingClass;
    }

    @Override
    protected Object doInBackground(DbFunctionObject... params) {
        switch(invokingClass) {
            case Constants.CREATE_EDIT_WORKOUT:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_ALL_UNASSIGNED_WORKOUTS:
                                return QueryExecutor.getUnassignedWorkouts();
                            case DbConstants.DELETE_ALL_WORKOUTS:
                                return QueryExecutor.deleteUnassignedWorkouts();
                            case DbConstants.DELETE_WORKOUTS:
                                return QueryExecutor.deleteWorkouts((List<Workout>) dfo.getDbObject());
                            case DbConstants.DELETE_WORKOUT:
                                return QueryExecutor.deleteWorkout((Workout) dfo.getDbObject());
                            case DbConstants.INSERT_WORKOUT:
                                return QueryExecutor.createWorkout((Workout) dfo.getDbObject());
                            case DbConstants.UPDATE_WORKOUTS:
                                return QueryExecutor.updateWorkouts((List<Workout>) dfo.getDbObject());
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.CREATE_EDIT_EXERCISE:
                for (DbFunctionObject dfo : params) {
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.DELETE_ALL_EXERCISES:
                            return QueryExecutor.deleteAllExercisesFromUI((List<Exercise>) dfo.getDbObject());
                        case DbConstants.UPDATE_EXERCISES:
                            return QueryExecutor.updateExercises((List<Exercise>) dfo.getDbObject());
                        case DbConstants.INSERT_EXERCISE:
                            return QueryExecutor.createExercise((Exercise) dfo.getDbObject());
                        case DbConstants.GET_EXERCISES_BY_WORKOUT:
                            return QueryExecutor.getExercisesByWorkout((Workout) dfo.getDbObject());
                        case DbConstants.DELETE_EXERCISES:
                            return QueryExecutor.deleteExercises((List<Exercise>) dfo.getDbObject());
                    }
                }
                break;
            case Constants.CREATE_EDIT_SET:
                for (DbFunctionObject dfo : params) {
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.GET_SETS_BY_EXERCISE:
                            return QueryExecutor.getSetsByExercise((Exercise) dfo.getDbObject());
                        case DbConstants.UPDATE_SETS:
                            return QueryExecutor.updateSets((List<Set>) dfo.getDbObject());
                        case DbConstants.DELETE_SETS:
                            return QueryExecutor.deleteSets((List<Set>) dfo.getDbObject());
                        case DbConstants.DELETE_ALL_SETS:
                            return QueryExecutor.deleteAllSetsFromUI((List<Set>) dfo.getDbObject());
                        case DbConstants.INSERT_SET:
                            return QueryExecutor.createSet((Set) dfo.getDbObject());
                    }
                }
            case Constants.ASSIGN_DIALOG:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_ALL_UNASSIGNED_WORKOUTS:
                                return QueryExecutor.getUnassignedWorkouts();
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.TODAY_LAUNCHER:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_WORKOUTS_BY_DATE:
                                return QueryExecutor.getWorkoutsByDate(new DateTime());
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.ASSIGN:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.ASSIGN_WORKOUTS:
                                if (dfo.getDbObject() instanceof LinkedList) {
                                    return QueryExecutor.assignDatesToWorkouts((LinkedList<Object>) dfo.getDbObject());
                                }
                                break;
                            case DbConstants.DELETE_WORKOUTS:
                                if (dfo.getDbObject() instanceof List) {
                                    return QueryExecutor.deleteWorkouts((List<Workout>) dfo.getDbObject());
                                }
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.VIEW_TEXT:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_WORKOUTS_BY_DATE:
                                return QueryExecutor.getWorkoutsByDate((DateTime) dfo.getDbObject());
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.TODAY:
                for (DbFunctionObject dfo : params) {
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.GET_FULL_WORKOUT:
                            return QueryExecutor.getWorkoutByNameAndDate(((Workout) dfo.getDbObject()).getName(),
                                    ((Workout) dfo.getDbObject()).getWorkoutDate());
                        case DbConstants.DELETE_EXERCISES_AND_SETS:
                            return QueryExecutor.deleteExercisesAndSets((HashMap<String, Object>) dfo.getDbObject());
                        case DbConstants.UPDATE_EXERCISES_AND_SETS:
                            return QueryExecutor.updateExercisesAndSets((HashMap<String, Object>) dfo.getDbObject());
                    }
                }
                break;
        }
        return null;
    }

    @Override protected void onPostExecute(Object result) {
        switch (invokingClass) {
            case Constants.CREATE_EDIT_WORKOUT:
                CreateEditWorkoutBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.CREATE_EDIT_EXERCISE:
                CreateEditExerciseBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.CREATE_EDIT_SET:
                CreateEditSetBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN_DIALOG:
                AssignDialogBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN:
                AssignBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.TODAY_LAUNCHER:
                TodayLauncherBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.VIEW_TEXT:
                ViewBus.getInstance().post(new DbTaskResult(result));
            case Constants.TODAY:
                TodayBus.getInstance().post(new DbTaskResult(result));
        }
    }
}
