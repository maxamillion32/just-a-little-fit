package ecap.studio.group.justalittlefit.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import ecap.studio.group.justalittlefit.model.Exercise;
import ecap.studio.group.justalittlefit.model.Workout;

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

    public static String deleteWorkouts(List<Workout> workouts) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            dao.delete(workouts);
        } catch (SQLException e) {
            return null;
        }
        return Boolean.TRUE.toString();
    }

    public static Workout deleteWorkout(Workout workout) {
        try {
            Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
            dao.delete(workout);
            return workout;
        } catch (SQLException e) {
            return null;
        }
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
            return null;
        }
    }

    public static Boolean createWorkout(Workout workout) {
        try {
            Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
            dao.create(workout);
            return true;
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

    public static Boolean updateWorkout(Workout workout) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            dao.update(workout);
            return true;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Set<Workout> updateWorkouts(final List<Workout> workouts) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        HashSet<Workout> workoutSet = new HashSet<>();
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
        workoutSet.addAll(workouts);
        return workoutSet;
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

    public static Integer deleteAllExercisesFromUI(List<Exercise> exercises) {
        if (deleteExercises(exercises) != null) {
            return exercises.size();
        } else {
            return null;
        }
    }

    public static String deleteExercises(List<Exercise> exercises) {
        Dao<Exercise, Integer> dao = DaoHelper.getInstance().getExerciseDao();
        try {
            dao.delete(exercises);
        } catch (SQLException e) {
            return null;
        }
        return Boolean.TRUE.toString();
    }

    public static Boolean createExercise(Exercise exercise) {
        try {
            Dao<Exercise, Integer> dao = DaoHelper.getInstance().getExerciseDao();
            dao.create(exercise);
            return true;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Boolean updateExercise(Exercise exercise) {
        Dao<Exercise, Integer> dao = DaoHelper.getInstance().getExerciseDao();
        try {
            dao.update(exercise);
            return true;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Set<Exercise> updateExercises(final List<Exercise> exercises) {
        Dao<Exercise, Integer> dao = DaoHelper.getInstance().getExerciseDao();
        HashSet<Exercise> exerciseSet = new HashSet<>();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (Exercise exercise : exercises) {
                        updateExercise(exercise);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
        exerciseSet.addAll(exercises);
        return exerciseSet;
    }

    public static List<Exercise> getExercisesByWorkout(final Workout workout) {
        Dao<Exercise, Integer> dao = DaoHelper.getInstance().getExerciseDao();
        QueryBuilder<Exercise, Integer> queryBuilder = dao.queryBuilder();
        List<Exercise> exercises;
        try {
            queryBuilder.where().in(DbConstants.WORKOUT_ID_COLUMN_NAME, workout);
            queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
            exercises = queryBuilder.query();
        } catch (SQLException e) {
            return null;
        }

        return exercises;
    }

    public static List<ecap.studio.group.justalittlefit.model.Set> getSetsByExercise(final Exercise exercise) {
        Dao<ecap.studio.group.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        QueryBuilder<ecap.studio.group.justalittlefit.model.Set, Integer> queryBuilder = dao.queryBuilder();
        List<ecap.studio.group.justalittlefit.model.Set> sets;
        try {
            queryBuilder.where().in(DbConstants.EXERCISE_ID_COLUMN_NAME, exercise);
            queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
            sets = queryBuilder.query();
        } catch (SQLException e) {
            return null;
        }

        return sets;
    }

    public static Boolean updateSet(ecap.studio.group.justalittlefit.model.Set set) {
        Dao<ecap.studio.group.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        try {
            dao.update(set);
            return true;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Set<ecap.studio.group.justalittlefit.model.Set> updateSets
            (final List<ecap.studio.group.justalittlefit.model.Set> sets) {
        Dao<ecap.studio.group.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        HashSet<ecap.studio.group.justalittlefit.model.Set> hashSet = new HashSet<>();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (ecap.studio.group.justalittlefit.model.Set set : sets) {
                        updateSet(set);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
        hashSet.addAll(sets);
        return hashSet;
    }

    public static String deleteSets(List<ecap.studio.group.justalittlefit.model.Set> sets) {
        Dao<ecap.studio.group.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        try {
            dao.delete(sets);
        } catch (SQLException e) {
            return null;
        }
        return Boolean.TRUE.toString();
    }
}
