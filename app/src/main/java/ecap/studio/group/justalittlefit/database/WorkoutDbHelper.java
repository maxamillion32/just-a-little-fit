package ecap.studio.group.justalittlefit.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import ecap.studio.group.justalittlefit.model.Workout;
import ecap.studio.group.justalittlefit.util.Constants;
import ecap.studio.group.justalittlefit.util.Utils;

/**
 * Helper class that will handle database functionality on objects of type
 * {@link ecap.studio.group.justalittlefit.model.Workout}
 */
public class WorkoutDbHelper {

    public static List<Workout> getUnassignedWorkouts() throws SQLException {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
        Where where = queryBuilder.where();
        where.isNull(DbConstants.WORKOUT_DATE_COLUMN_NAME);
        queryBuilder.setWhere(where);
        queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
        List<Workout> workouts = queryBuilder.query();
        return workouts;
    }

    public static Workout getWorkoutById(int id) throws SQLException {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        Workout workout = dao.queryForId(id);
        return workout;
    }

    public static Set deleteWorkouts(List<Workout> workouts) {
        Set<Integer> deletedIdSet = new HashSet<>();
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            dao.delete(workouts);
        } catch (SQLException e) {
            return null;
        }

        for (Workout workout : workouts) {
            deletedIdSet.add(workout.getWorkoutId());
        }

        return deletedIdSet;
    }

    public static Integer deleteUnassignedWorkouts() {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
            Where where = queryBuilder.where();
            where.isNull(DbConstants.WORKOUT_DATE_COLUMN_NAME);
            queryBuilder.setWhere(where);
            queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
            List<Workout> workouts = queryBuilder.query();
            return dao.delete(workouts);
        } catch (SQLException e) {
            return Constants.DB_ERR_CODE;
        }
    }

    public static Workout createWorkout(Workout workout) {
        try {
            Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
            dao.create(workout);
            return workout;
        } catch (SQLException e) {
            return null;
        }
    }

    public static List<Workout> createWorkouts(final List<Workout> workouts) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (Workout workout : workouts) {
                        createWorkout(workout);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
        return workouts;
    }

    public static void updateWorkout(Workout workout) throws SQLException {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        dao.update(workout);
    }

    public static List<Workout> updateWorkouts(final List<Workout> workouts) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (Workout workout : workouts) {
                        updateWorkout(workout);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
        return workouts;
    }

    public static List<Workout> assignDatesToWorkouts(LinkedList<Object> list) throws SQLException {
        List<DateTime> dateTimes = (List<DateTime>) list.get(0);
        List<String> workoutNames = (List<String>) list.get(1);
        List<Workout> workoutsToAssign = new ArrayList<>();

        for (DateTime dateTime : dateTimes) {
            for (String name : workoutNames) {
                workoutsToAssign.add(new Workout(name, dateTime));
            }
        }

        return createWorkouts(workoutsToAssign);
    }
}
