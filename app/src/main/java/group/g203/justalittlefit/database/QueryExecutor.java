package group.g203.justalittlefit.database;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Workout;
import group.g203.justalittlefit.util.Constants;

/**
 * Helper class that will handle database functionality on objects of type
 * {@link group.g203.justalittlefit.model.Workout}
 */
public class QueryExecutor {

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

    public static CloseableIterator<Workout> getUnassignedWorkoutsCi() throws SQLException {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
        Where where = queryBuilder.where();
        where.isNull(DbConstants.WORKOUT_DATE_COLUMN_NAME);
        queryBuilder.setWhere(where);
        queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
        CloseableIterator<Workout> workouts = dao.iterator(queryBuilder.prepare());
        return workouts;
    }

    public static HashMap<String, Object> getUnassignedWorkoutsMap() {
        HashMap<String, Object> map = new HashMap<>(2);

        try {
            CloseableIterator<Workout> iterator = getUnassignedWorkoutsCi();
            List<Workout> workoutList = getUnassignedWorkouts();
            map.put(Constants.ITERATOR, iterator);
            map.put(Constants.LIST, workoutList);
        } catch (SQLException e) {
            map = null;
        }

        return map;
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

    public static Integer deleteViewWorkouts(List<Workout> workouts) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            return dao.delete(workouts);
        } catch (SQLException e) {
            return null;
        }
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

    public static Boolean deleteTodayWorkout(Workout workout) {
        try {
            Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
            dao.delete(workout);
            return true;
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
        try {
            List<DateTime> dateTimes = (List<DateTime>) list.get(0);
            List<String> workoutNames = (List<String>) list.get(1);
            List<Workout> workoutsToAssign = new ArrayList<>();

            List<Workout> workouts = getWorkoutsByName(workoutNames);

            for (DateTime dateTime : dateTimes) {
                for (Workout workout : workouts) {
                    if (getWorkoutByNameAndDate(workout.getName(), dateTime) == null) {
                        workout.setWorkoutDate(dateTime);
                        workoutsToAssign.add(workout);
                    }
                }
            }

            List<Workout> assignedWorkouts = createWorkouts(workoutsToAssign);
            List<Workout> completeDbWorkouts = addExercisesToWorkouts(assignedWorkouts);

            HashMap<Integer, List<group.g203.justalittlefit.model.Set>> mapOfSets =
                    new HashMap<>();

            int count = 0;

            for (Workout assignedWorkout : assignedWorkouts) {
                for (Exercise exercise : assignedWorkout.getExercises()) {
                    mapOfSets.put(count, new ArrayList<>(exercise.getSets()));
                    count++;
                }
            }

            count = 0;
            for (Workout completeDbWorkout : completeDbWorkouts) {
                for (Exercise exercise : completeDbWorkout.getExercises()) {
                    addSetsToExercises(exercise.getExerciseId(), mapOfSets.get(count));
                    mapOfSets.put(count, new ArrayList<>(exercise.getSets()));
                    count++;
                }
            }

            return assignedWorkouts;
        } catch (SQLException e) {
            return null;
        }
    }

    public static Workout getWorkoutByName(String name) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
            Where where = queryBuilder.where();
            where.eq(DbConstants.NAME_COLUMN_NAME, name);
            queryBuilder.setWhere(where);
            List<Workout> workouts = queryBuilder.query();
            return workouts.get(0);
        } catch (SQLException e) {
            return null;
        }
    }

    public static Workout getWorkoutByNameAndDate(String name, DateTime date) {
        DateTime startOfDay = date.withTimeAtStartOfDay();
        DateTime endOfDay = date.withTime(23, 59, 59, 999);
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        try {
            QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
            Where where = queryBuilder.where();
            where.eq(DbConstants.NAME_COLUMN_NAME, name);
            where.and();
            where.between(DbConstants.WORKOUT_DATE_COLUMN_NAME, startOfDay, endOfDay);
            queryBuilder.setWhere(where);
            List<Workout> workouts = queryBuilder.query();
            if (workouts.size() > 0) {
                return workouts.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public static List<Workout> getWorkoutsByName(final List<String> names) {
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        final List<Workout> workouts = new ArrayList<>();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (String name : names) {
                        workouts.add(getWorkoutByName(name));
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
        return workouts;
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

    public static Exercise createExerciseForToday(Exercise exercise) {
        if (createExercise(exercise) != null) {
            return exercise;
        } else {
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

    public static List<group.g203.justalittlefit.model.Set> getSetsByExercise(final Exercise exercise) {
        Dao<group.g203.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        QueryBuilder<group.g203.justalittlefit.model.Set, Integer> queryBuilder = dao.queryBuilder();
        List<group.g203.justalittlefit.model.Set> sets;
        try {
            queryBuilder.where().in(DbConstants.EXERCISE_ID_COLUMN_NAME, exercise);
            queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
            sets = queryBuilder.query();
        } catch (SQLException e) {
            return null;
        }

        return sets;
    }

    public static Double updateSet(group.g203.justalittlefit.model.Set set) {
        Dao<group.g203.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        try {
            return Double.parseDouble(dao.update(set) + Constants.EMPTY_STRING);
        } catch (SQLException e) {
            return null;
        }
    }

    public static Set<group.g203.justalittlefit.model.Set> updateSets
            (final List<group.g203.justalittlefit.model.Set> sets) {
        Dao<group.g203.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        HashSet<group.g203.justalittlefit.model.Set> hashSet = new HashSet<>();
        try {
            dao.callBatchTasks(new Callable<Void>() {
                public Void call() throws SQLException {
                    for (group.g203.justalittlefit.model.Set set : sets) {
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

    public static String deleteSets(List<group.g203.justalittlefit.model.Set> sets) {
        Dao<group.g203.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
        try {
            dao.delete(sets);
        } catch (SQLException e) {
            return null;
        }
        return Boolean.TRUE.toString();
    }

    public static Integer deleteAllSetsFromUI(List<group.g203.justalittlefit.model.Set> sets) {
        if (deleteSets(sets) != null) {
            return sets.size();
        } else {
            return null;
        }
    }

    public static Boolean createSet(group.g203.justalittlefit.model.Set set) {
        try {
            Dao<group.g203.justalittlefit.model.Set, Integer> dao = DaoHelper.getInstance().getSetDao();
            dao.create(set);
            return true;
        } catch (SQLException e) {
            return null;
        }
    }

    public static group.g203.justalittlefit.model.Set createSetForToday(group.g203.justalittlefit.model.Set set) {
        if (createSet(set) != null) {
            return set;
        } else {
            return null;
        }
    }

    public static String deleteExercisesAndSets(HashMap<String, Object> map) {
        try {
            Dao<Exercise, Integer> exerciseDao = DaoHelper.getInstance().getExerciseDao();
            HashSet<Exercise> exercisesToDelete = (HashSet<Exercise>) map.get(Constants.EXERCISES);
            exerciseDao.delete(exercisesToDelete);

            Dao<group.g203.justalittlefit.model.Set, Integer> setDao = DaoHelper.getInstance().getSetDao();
            HashSet<group.g203.justalittlefit.model.Set> setsToDelete =
                    (HashSet<group.g203.justalittlefit.model.Set>) map.get(Constants.SETS_NORM_CASE);
            setDao.delete(setsToDelete);
        } catch (SQLException e) {
            return null;
        }
        return Boolean.TRUE.toString();
    }

    public static HashMap updateExercisesAndSets(HashMap<String, Object> map) {
        List<Exercise> exercises = (ArrayList<Exercise>) map.get(Constants.EXERCISES);
        updateExercises(exercises);

        List<group.g203.justalittlefit.model.Set> sets =
                (ArrayList<group.g203.justalittlefit.model.Set>) map.get(Constants.SETS_NORM_CASE);
        updateSets(sets);

        return map;
    }

    public static List<Workout> getWorkoutsByDate(DateTime dateTime) throws SQLException {
        DateTime startOfDay = dateTime.withTimeAtStartOfDay();
        DateTime endOfDay = dateTime.withTime(23, 59, 59, 999);
        Dao<Workout, Integer> dao = DaoHelper.getInstance().getWorkoutDao();
        QueryBuilder<Workout, Integer> queryBuilder = dao.queryBuilder();
        Where where = queryBuilder.where();
        where.between(DbConstants.WORKOUT_DATE_COLUMN_NAME, startOfDay, endOfDay);
        queryBuilder.setWhere(where);
        queryBuilder.orderBy(DbConstants.ORDER_NUMBER_COLUMN_NAME, true);
        List<Workout> workouts = queryBuilder.query();
        return workouts;
    }


    public static List<Workout> addExercisesToWorkouts(List<Workout> workouts) throws SQLException {
        List<Workout> workoutsWithExercises = new ArrayList<>(workouts.size());
        Dao<Workout, Integer> workoutDao  = DaoHelper.getInstance().getWorkoutDao();
        for (Workout workout : workouts) {
            try {
                Workout wo = workoutDao.queryForId(workout.getWorkoutId());
                ForeignCollection<Exercise> exercises = wo.getExercises();
                List<Exercise> exerciseList = new ArrayList<>(workout.getExercises());
                for (Exercise exercise : exerciseList) {
                    exercises.add(exercise);
                }
                workoutsWithExercises.add(wo);
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
        return workoutsWithExercises;
    }

    public static void addSetsToExercises(int exerciseId,
                                          List<group.g203.justalittlefit.model.Set> sets) throws SQLException {
        Dao<Exercise, Integer> exerciseDao = DaoHelper.getInstance().getExerciseDao();
        try {
            Exercise ex = exerciseDao.queryForId(exerciseId);
            ForeignCollection<group.g203.justalittlefit.model.Set> setFc = ex.getSets();
            addSetsToFc(setFc, sets);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private static void addSetsToFc(ForeignCollection<group.g203.justalittlefit.model.Set> setCollect,
                                List<group.g203.justalittlefit.model.Set> sets) {
        for (group.g203.justalittlefit.model.Set set : sets) {
            group.g203.justalittlefit.model.Set newSet =
                    new group.g203.justalittlefit.model.Set(set.isComplete(), set.getReps(),
                    set.getWeight(), set.getHours(), set.getMinutes(), set.getSeconds(), set.getWeightTypeCode(),
                    set.getExerciseTypeCode(), set.getOrderNumber());
            setCollect.add(newSet);
        }
    }
}
