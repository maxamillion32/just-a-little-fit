package ecap.studio.group.justalittlefit.database;

import android.os.AsyncTask;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import ecap.studio.group.justalittlefit.bus.AssignBus;
import ecap.studio.group.justalittlefit.bus.AssignDialogBus;
import ecap.studio.group.justalittlefit.bus.CreateEditWorkoutBus;
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
                                return WorkoutDbHelper.getUnassignedWorkouts();
                            case DbConstants.DELETE_WORKOUTS:
                                return WorkoutDbHelper.deleteUnassignedWorkouts();
                            case DbConstants.DELETE_WORKOUT:
                                return WorkoutDbHelper.deleteWorkout((Workout) dfo.getDbObject());
                            case DbConstants.INSERT_WORKOUT:
                                return WorkoutDbHelper.createWorkout((Workout) dfo.getDbObject());
                        }
                    } catch (SQLException e) {
                        return null;
                    }
                }
                break;
            case Constants.CREATE_EDIT_NO_SPACE:
                for (DbFunctionObject dfo : params) {
                    switch (dfo.getFunctionInt()) {
                        case DbConstants.INSERT_WORKOUT:
                            if (dfo.getDbObject() instanceof Workout) {
                                return WorkoutDbHelper.createWorkout((Workout) dfo.getDbObject());
                            }
                            break;
                        case DbConstants.UPDATE_WORKOUTS:
                            if (dfo.getDbObject() instanceof List) {
                                return WorkoutDbHelper.updateWorkouts((List<Workout>) dfo.getDbObject());
                            }
                            break;
                    }
                }
                break;
            case Constants.ASSIGN_DIALOG:
                for (DbFunctionObject dfo : params) {
                    try {
                        switch (dfo.getFunctionInt()) {
                            case DbConstants.GET_ALL_UNASSIGNED_WORKOUTS:
                                return WorkoutDbHelper.getUnassignedWorkouts();
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
                                    return WorkoutDbHelper.assignDatesToWorkouts((LinkedList<Object>) dfo.getDbObject());
                                }
                                break;
                            case DbConstants.DELETE_WORKOUTS:
                                if (dfo.getDbObject() instanceof List) {
                                    return WorkoutDbHelper.deleteWorkouts((List<Workout>) dfo.getDbObject());
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
                CreateEditWorkoutBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN_DIALOG:
                AssignDialogBus.getInstance().post(new DbTaskResult(result));
                break;
            case Constants.ASSIGN:
                AssignBus.getInstance().post(new DbTaskResult(result));
                break;
        }
    }
}
