package group.g203.justalittlefit.database;

import android.os.AsyncTask;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import group.g203.justalittlefit.bus.AssignBus;
import group.g203.justalittlefit.bus.AssignDialogBus;
import group.g203.justalittlefit.bus.CreateEditExerciseBus;
import group.g203.justalittlefit.bus.CreateEditSetBus;
import group.g203.justalittlefit.bus.CreateEditWorkoutBus;
import group.g203.justalittlefit.bus.PeekLauncherBus;
import group.g203.justalittlefit.bus.SelectDialogBus;
import group.g203.justalittlefit.bus.TodayBus;
import group.g203.justalittlefit.bus.ViewBus;
import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;

/**
 * Runs database logic asynchronously using a {@link group.g203.justalittlefit.database.DbFunctionObject}
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
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.GET_ALL_UNASSIGNED_WORKOUTS:
                            return QueryExecutor.getUnassignedWorkoutsMap();
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
                        case DbConstants.UPDATE_WORKOUT:
                            return QueryExecutor.updateWorkout((Workout) dfo.getDbObject());
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
                        case DbConstants.UPDATE_EXERCISE:
                            return QueryExecutor.updateExercise((Exercise) dfo.getDbObject());
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
                        case DbConstants.UPDATE_SET:
                            return QueryExecutor.updateSet((Set) dfo.getDbObject());
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
            case Constants.PEEK_LAUNCHER:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_WORKOUTS_BY_DATE:
                                DateTime dateTime = (dfo.getDbObject() == null) ? new DateTime() :
                                        (DateTime) dfo.getDbObject();
                                return QueryExecutor.getWorkoutsByDate(dateTime);
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
                            case DbConstants.DELETE_WORKOUTS:
                                return QueryExecutor.deleteViewWorkouts((List<Workout>) dfo.getDbObject());
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
                        case DbConstants.INSERT_EXERCISE:
                            return QueryExecutor.createExerciseForToday((Exercise) dfo.getDbObject());
                        case DbConstants.INSERT_SET:
                            return QueryExecutor.createSetForToday((Set) dfo.getDbObject());
                        case DbConstants.DELETE_WORKOUT:
                        return QueryExecutor.deleteTodayWorkout((Workout) dfo.getDbObject());
                        case DbConstants.UPDATE_SET:
                            return QueryExecutor.updateSet((Set) dfo.getDbObject());
                        case DbConstants.UPDATE_WORKOUT:
                            return QueryExecutor.updateTodayWorkout((Workout) dfo.getDbObject());
                        case DbConstants.UPDATE_EXERCISE:
                            return QueryExecutor.updateTodayExercise((Exercise) dfo.getDbObject());
                    }
                }
                break;
            case Constants.SELECT_DIALOG:
                for (DbFunctionObject dfo : params) {
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.GET_FULL_WORKOUT:
                            return QueryExecutor.getWorkoutByNameAndDate(((Workout) dfo.getDbObject()).getName(),
                                    ((Workout) dfo.getDbObject()).getWorkoutDate());
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
            case Constants.PEEK_LAUNCHER:
                PeekLauncherBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.VIEW_TEXT:
                ViewBus.getInstance().post(new DbTaskResult(result));
            case Constants.TODAY:
                TodayBus.getInstance().post(new DbTaskResult(result));
            case Constants.SELECT_DIALOG:
                SelectDialogBus.getInstance().post(new DbTaskResult(result));
                break;
        }
    }
}
