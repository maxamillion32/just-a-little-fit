package group.g203.justalittlefit.database;

import com.j256.ormlite.dao.Dao;

import group.g203.justalittlefit.model.Exercise;
import group.g203.justalittlefit.model.Set;
import group.g203.justalittlefit.model.Workout;

/**
 * Helper class that allows easy access to DAO objects.
 */
public class DaoHelper {

    private Dao<Workout, Integer> workoutDao;
    private Dao<Exercise, Integer> exerciseDao;
    private Dao<Set, Integer> setDao;
    private static DaoHelper instance;

    public DaoHelper() {
    }

    public static DaoHelper getInstance() {
        if (instance == null) {
            instance = new DaoHelper();
        }
        return instance;
    }

    public Dao<Workout, Integer> getWorkoutDao() {
        return this.getInstance().workoutDao;
    }

    public void setWorkoutDao(Dao<Workout, Integer> workoutDao) {
        this.getInstance().workoutDao = workoutDao;
    }

    public Dao<Exercise, Integer> getExerciseDao() {
        return this.getInstance().exerciseDao;
    }

    public void setExerciseDao(Dao<Exercise, Integer> exerciseDao) {
        this.getInstance().exerciseDao = exerciseDao;
    }

    public Dao<Set, Integer> getSetDao() {
        return this.getInstance().setDao;
    }

    public void setSetDao(Dao<Set, Integer> setDao) {
        this.getInstance().setDao = setDao;
    }

}

