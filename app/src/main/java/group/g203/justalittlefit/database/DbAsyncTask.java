package group.g203.justalittlefit.database;

import android.os.AsyncTask;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.BusFactory;
import group.g203.justalittlefit.util.Constants;

/**
 * Runs database logic asynchronously using a {@link group.g203.justalittlefit.database.DbFunctionObject}.
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
                        case DbConstants.REMOVE_WORKOUTS:
                            if (dfo.getDbObject() instanceof List) {
                                return QueryExecutor.undoAddedWorkouts((List<Workout>) dfo.getDbObject());
                            }
                        case DbConstants.ASSIGN_WORKOUTS:
                            if (dfo.getDbObject() instanceof LinkedList) {
                                try {
                                    return QueryExecutor.assignWorkouts((LinkedList<Object>) dfo.getDbObject());
                                } catch (SQLException e) {
                                    return null;
                                }
                            }
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
                        case DbConstants.REMOVE_WORKOUTS:
                            if (dfo.getDbObject() instanceof List) {
                                return QueryExecutor.undoAddedWorkouts((List<Workout>) dfo.getDbObject());
                            }
                        case DbConstants.ASSIGN_WORKOUTS:
                            if (dfo.getDbObject() instanceof LinkedList) {
                                try {
                                    return QueryExecutor.assignWorkouts((LinkedList<Object>) dfo.getDbObject());
                                } catch (SQLException e) {
                                    return null;
                                }
                            }
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
                        case DbConstants.REMOVE_WORKOUTS:
                            if (dfo.getDbObject() instanceof List) {
                                return QueryExecutor.undoAddedWorkouts((List<Workout>) dfo.getDbObject());
                            }
                        case DbConstants.ASSIGN_WORKOUTS:
                            if (dfo.getDbObject() instanceof LinkedList) {
                                try {
                                    return QueryExecutor.assignWorkouts((LinkedList<Object>) dfo.getDbObject());
                                } catch (SQLException e) {
                                    return null;
                                }
                            }
                    }
                }
            case Constants.ASSIGN_DIALOG:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_ALL_UNASSIGNED_WORKOUTS:
                                return QueryExecutor.getUnassignedWorkouts();
                            case DbConstants.GET_WORKOUTS_BY_DATE:
                                return QueryExecutor.getWorkoutsByDate((List<DateTime>) dfo.getDbObject());
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
                                break;
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.VIEW_TEXT:
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
                            return QueryExecutor.createExerciseForADate((Exercise) dfo.getDbObject());
                        case DbConstants.INSERT_SET:
                            return QueryExecutor.createSetForADate((Set) dfo.getDbObject());
                        case DbConstants.DELETE_WORKOUT:
                        return QueryExecutor.deleteWorkoutForADate((Workout) dfo.getDbObject());
                        case DbConstants.UPDATE_SET:
                            return QueryExecutor.updateSet((Set) dfo.getDbObject());
                        case DbConstants.UPDATE_WORKOUT:
                            return QueryExecutor.updateWorkoutForADate((Workout) dfo.getDbObject());
                        case DbConstants.UPDATE_EXERCISE:
                            return QueryExecutor.updateExerciseForADate((Exercise) dfo.getDbObject());
                        case DbConstants.REMOVE_WORKOUTS:
                            if (dfo.getDbObject() instanceof List) {
                                return QueryExecutor.undoAddedWorkouts((List<Workout>) dfo.getDbObject());
                            }
                        case DbConstants.ASSIGN_WORKOUTS:
                            if (dfo.getDbObject() instanceof LinkedList) {
                                try {
                                    return QueryExecutor.assignWorkouts((LinkedList<Object>) dfo.getDbObject());
                                } catch (SQLException e) {
                                    return null;
                                }
                            }
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
            case Constants.PAST_VIEW_TEXT:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_WORKOUTS_BY_DATE:
                                return QueryExecutor.getWorkoutsByDate((DateTime) dfo.getDbObject());
                            case DbConstants.REMOVE_WORKOUTS:
                                if (dfo.getDbObject() instanceof List) {
                                    return QueryExecutor.undoAddedWorkouts((List<Workout>) dfo.getDbObject());
                                }
                            case DbConstants.DELETE_WORKOUTS:
                                return QueryExecutor.deleteViewWorkouts((List<Workout>) dfo.getDbObject());
                            case DbConstants.ASSIGN_WORKOUTS:
                                if (dfo.getDbObject() instanceof LinkedList) {
                                    try {
                                        return QueryExecutor.assignWorkouts((LinkedList<Object>) dfo.getDbObject());
                                    } catch (SQLException e) {
                                        return null;
                                    }
                                }
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
        }
        return null;
    }

    @Override protected void onPostExecute(Object result) {
        switch (invokingClass) {
            case Constants.CREATE_EDIT_WORKOUT:
                BusFactory.getCreateEditWorkoutBus().post(new DbTaskResult(result));
                break;
            case Constants.CREATE_EDIT_EXERCISE:
                BusFactory.getCreateEditExerciseBus().post(new DbTaskResult(result));
                break;
            case Constants.CREATE_EDIT_SET:
                BusFactory.getCreateEditSetBus().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN_DIALOG:
                BusFactory.getAssignDialogBus().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN:
                BusFactory.getBaseAssignBus().post(new DbTaskResult(result));
                break;
            case Constants.PEEK_LAUNCHER:
                BusFactory.getPeekLauncherBus().post(new DbTaskResult(result));
                break;
            case Constants.VIEW_TEXT:
                BusFactory.getViewBus().post(new DbTaskResult(result));
            case Constants.SELECT_DIALOG:
                BusFactory.getSelectDialogBus().post(new DbTaskResult(result));
                break;
            case Constants.PAST_VIEW_TEXT:
                BusFactory.getPastViewBus().post(new DbTaskResult(result));
                break;
        }
    }
}
